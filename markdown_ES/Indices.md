# Índices propuestos y relación con los requerimientos
Versión en inglés disponible en [../markdown_EN/Indices.md](../markdown_EN/Indices.md).

Este documento resume **qué índices creamos/sugerimos** sobre la base de datos del hospital,
**por qué** los necesitamos y **qué requerimientos funcionales ayudan a cumplir**.

🟦 Internaciones / camas → “Listado de camas disponibles por sector”

🟩 Seguimiento médico → “Listado de comentarios de visitas a un paciente en una internación”

🟥 Auditoría guardias → “Auditoría sobre usuarios que cambian datos de guardias”

## 1. Stored procedures y triggers involucrados

### 🟦 Camas disponibles por sector (Internaciones)

Requerimiento:
> Listado con la cantidad de camas disponibles de cada sector y el listado de detalle de esas camas.

Stored procedures:

- `sp_camas_disponibles_resumen`
  - Devuelve, por sector, la cantidad de camas **LIBRES**.
- `sp_camas_disponibles_detalle`
  - Devuelve el detalle (habitaciones / camas) filtrando por estado **LIBRE**.
- `sp_agregar_cama`
- `sp_eliminar_o_desactivar_cama`
  - Mantienen consistencia del stock de camas (creación/eliminación/deshabilitado) que luego usan los listados.

Triggers relevantes:

- `tr_se_ubica_cama_estado`
  - Actualiza el `estado` de la cama (LIBRE/OCUPADA/FUERA_DE_SERVICIO) cuando se inserta/actualiza en `SE_UBICA`
    para que los listados de camas trabajen siempre contra `CAMA.estado` sin recalcular todo el historial.

---

### 🟩 Comentarios de visitas médicas a un paciente en una internación

Requerimiento:
> Listado de los comentarios de las visitas médicas a un paciente en una cierta internación.

Stored procedures:

- `sp_internaciones_paciente`
  - Dado un paciente (`tipo_documento`, `nro_documento`),
    devuelve sus internaciones para que el usuario elija una.
- `sp_comentarios_visitas`
  - Dados `nro_internacion` y eventualmente filtros,
    devuelve las visitas médicas y comentarios asociados a esa internación.

Tablas clave en estos SP:

- `INTERNACION`
- `PACIENTE` / `PERSONA`
- `RECORRIDO`, `RONDA`, `VISITA`
- `COMENTA_SOBRE`

---

### 🟥 Auditoría de usuarios sobre guardias

Requerimiento:
> Auditoría sobre los usuarios que hacen cambios a datos que afectan el proceso de asignación de guardias.

Stored procedures:

- `sp_auditoria_guardias`
  - Lista los cambios registrados en `AUDITORIA_GUARDIA`
    con filtros opcionales por usuario, fecha desde / hasta.

Triggers:

- `tr_aud_guardia`
  - Se dispara ante cambios en `GUARDIA` y registra en `AUDITORIA_GUARDIA`:
    `usuario_bd`, `fecha_hora_reg`, acción, etc.

---

## 2. Índices y justificación por tabla

> Nota: Todas las **PK** ya tienen automáticamente un **índice único B-tree** creado por Oracle.
> Aquí listamos solo los **índices adicionales (no únicos)** que agregamos/sugerimos.

### 2.1. HABITACION

- **Índice no único B-tree**  
  `idx_habitacion_sector (id_sector)` 🟦

  **Motivo:**
  - Los SP de camas (`sp_camas_disponibles_%`) necesitan agrupar y filtrar por **sector**.
  - La consulta típica: JOIN `SECTOR` → `HABITACION` → `CAMA` con filtros por `id_sector`.
  - Este índice acelera:
    - “Camas libres por sector”
    - Listados de detalle de camas de un sector.

---

### 2.2. CAMA

- **Índice no único compuesto B-tree**  
  `idx_cama_habitacion_estado (nro_habitacion, estado)` 🟦

  **Motivo:**
  - Los listados de camas libres consultan muchas veces:
    - `WHERE estado = 'LIBRE'`
    - JOIN con `HABITACION` por `nro_habitacion`.
  - Este índice permite:
    - Encontrar rápidamente camas **LIBRES** dentro de una habitación.
    - Hacer group by / counts por sector + estado de forma eficiente.
  - Afecta positivamente:
    - `sp_camas_disponibles_resumen`
    - `sp_camas_disponibles_detalle`
    - Lógica de asignación automática de camas en internaciones.

---

### 2.3. SE_UBICA

- **Índice no único compuesto B-tree**  
  `idx_se_ubica_internacion_fecha (nro_internacion, fecha_hora_ingreso)`

  **Motivo:**
  - Consultas para ver el historial de ubicación de una internación:
    - `WHERE nro_internacion = :p_nro_internacion`
      `ORDER BY fecha_hora_ingreso`
  - Se usa tanto en consultas de auditoría interna de internaciones
    como en validaciones de negocio (consistencia entre período de internación y ocupación de cama).
  - No está ligado directamente a los tres requerimientos marcados,
    pero mejora el rendimiento de verificaciones y consultas históricas relacionadas.

---

### 2.4. INTERNACION

- **Índice no único compuesto B-tree**  
  `idx_internacion_paciente (tipo_documento, nro_documento)` 🟩

  **Motivo:**
  - `sp_internaciones_paciente` hace:

    ```sql
    SELECT ...
      FROM INTERNACION i
      JOIN PACIENTE p ON ...
     WHERE p.tipo_documento = p_tipo_doc
       AND p.nro_documento  = p_nro_doc;
    ```

  - Para obtener internaciones de un paciente concreto,
    esto termina usando `INTERNACION.tipo_documento` + `INTERNACION.nro_documento`
    (FK hacia PACIENTE).
  - Este índice:
    - Acelera el primer paso del flujo “elegir internación de un paciente”.
    - Impacta directamente en el requerimiento 🟩 de **comentarios de visitas**,
      ya que sin esta lista previa de internaciones, no se puede seleccionar
      la internación sobre la cual ver comentarios.

---

### 2.5. COMENTA_SOBRE

- **Índice no único B-tree**  
  `idx_comenta_sobre_internacion (nro_internacion)` 🟩

  **Motivo:**
  - `sp_comentarios_visitas` filtra por una internación elegida:

    ```sql
    WHERE cs.nro_internacion = p_nro_internacion
    ```

  - La PK de `COMENTA_SOBRE` es `(id_recorrido, nro_internacion)`, por lo que
    Oracle ya tiene un índice sobre esa combinación.
  - Sin embargo, al filtrar exclusivamente por `nro_internacion`, conviene
    un índice que tenga `nro_internacion` como primer componente.
  - Resultado:
    - Búsqueda mucho más rápida de “todos los comentarios de una internación”.
    - Beneficia directamente el requerimiento 🟩 de **seguimiento médico**.

---

### 2.6. AUDITORIA_GUARDIA

- **Índice no único compuesto B-tree**  
  `idx_aud_guardia_usuario_fecha (usuario_bd, fecha_hora_reg)` 🟥

  **Motivo:**
  - `sp_auditoria_guardias` soporta filtros opcionales:

    ```sql
    WHERE (p_usuario IS NULL OR usuario_bd = p_usuario)
      AND (p_desde  IS NULL OR fecha_hora_reg >= p_desde)
      AND (p_hasta  IS NULL OR fecha_hora_reg <= p_hasta)
    ```

  - Los usos típicos:
    - Ver todos los cambios hechos por un **usuario** en un rango de fechas.
    - Explorar qué cambios de guardias hubo en un período específico.
  - Este índice:
    - Permite combinar filtro por `usuario_bd` y rango de fechas
      de forma eficiente (búsqueda por prefijo + rango).
    - Impacta directamente en el requerimiento 🟥 de **auditoría de guardias**.

---

### 2.7. GUARDIA

> Aquí no marcamos ningún índice con color porque no hay un stored procedure
> específico de listado de guardias en los tres requerimientos originales,
> pero sí hay mucha lógica de negocio en Java + triggers asociada a guardias
> (máx. guardias mensuales, no días consecutivos, etc.).

- **Índice no único compuesto B-tree**  
  `idx_guardia_medico_fecha (matricula, fecha_hora)`

  **Motivo:**
  - La lógica de negocio para guardias (en servicios Java) hace consultas típicas del estilo:
    - “¿Cuántas guardias tiene este médico en este mes?”
    - “¿Tiene guardia el día anterior o posterior a esta fecha?”
  - Todas estas consultas usan siempre:
    - `WHERE matricula = :p_matricula`  
      y filtros/rangos sobre `fecha_hora`.
  - Este índice:
    - Acelera las validaciones de negocio sobre guardias.
    - Reduce el costo de las verificaciones previas a insertar/actualizar guardias.

---

### 2.8. PACIENTE / PERSONA / MEDICO

En estos casos:

- **PKs**:
  - `PERSONA (tipo_documento, nro_documento)`
  - `PACIENTE (tipo_documento, nro_documento)`
  - `MEDICO (matricula)`
- Consultas típicas:
  - Búsqueda directa por PK (documento o matrícula).
  - Joins por estas PK / FK.

Dado que:

- Oracle ya genera **índices únicos B-tree** para estas PK,
- Y que las consultas más frecuentes usan justamente esas claves,

por ahora **no es necesario** agregar índices adicionales sobre estas tablas
para los tres requerimientos marcados (🟦, 🟩, 🟥).

---

## 3. Resumen visual por requerimiento

- 🟦 **Camas disponibles por sector**
  - `idx_habitacion_sector (id_sector)`
  - `idx_cama_habitacion_estado (nro_habitacion, estado)`

- 🟩 **Comentarios de visitas médicas**
  - `idx_internacion_paciente (tipo_documento, nro_documento)`
  - `idx_comenta_sobre_internacion (nro_internacion)`

- 🟥 **Auditoría de guardias**
  - `idx_aud_guardia_usuario_fecha (usuario_bd, fecha_hora_reg)`

El resto de los índices propuestos complementan la performance general de la aplicación
(historial de internaciones, movimientos de cama, validaciones sobre guardias, etc.),
pero no están directamente atados a un solo requerimiento de reporte.
