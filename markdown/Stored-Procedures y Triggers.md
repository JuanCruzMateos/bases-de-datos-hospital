# Lógica de altas, bajas y modificaciones  
## (Camas, Habitaciones, Sectores e Internaciones)

Este documento resume las decisiones de diseño para manejar **altas, bajas y modificaciones** en el módulo de internaciones del hospital.

---

## 1. Modelo general

Entidades principales:

- `SECTOR(id_sector, descripcion)`
- `HABITACION(nro_habitacion, piso, orientacion, id_sector)`
- `CAMA(nro_habitacion, nro_cama, estado)`
- `INTERNACION(nro_internacion, fecha_inicio, fecha_fin, ...)`
- `SE_UBICA(nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion)`

Relaciones clave:

- Una **habitación** pertenece a un **sector**  
  `HABITACION.id_sector → SECTOR.id_sector` (FK sin `ON DELETE CASCADE`).

- Una **cama** pertenece a una **habitación**  
  `CAMA.nro_habitacion → HABITACION.nro_habitacion` (FK con `ON DELETE CASCADE`).

- Una **internación** se ubica en camas a través de `SE_UBICA`:  
  `SE_UBICA.nro_internacion → INTERNACION.nro_internacion` (FK con `ON DELETE CASCADE`).

En términos conceptuales:

> Una **internación** tiene muchas **ubicaciones** en `SE_UBICA`.  
> Cada ubicación referencia una **cama**, dentro de una **habitación**, que a su vez pertenece a un **sector**.

Nota sobre delete cascade en HABITACION y CAMA:

- Aunque la FK CAMA.nro_habitacion -> HABITACION.nro_habitacion tiene ON DELETE CASCADE, el trigger `tr_habitacion_no_delete_if_used` impide borrar habitaciones con historial en SE_UBICA.  
- En la practica, el borrado en cascada solo se aplica para habitaciones "nuevas", cuyas camas nunca fueron usadas en internaciones.

El campo `CAMA.estado` se usa para marcar la disponibilidad (por ejemplo: `LIBRE`, `OCUPADA`, `FUERA_DE_SERVICIO`).

---

## 2. Criterios de altas y bajas

### 2.1. Camas

**Idea central:**  
Las camas pueden eliminarse físicamente solo si nunca fueron usadas; si tienen historial, se deshabilitan pero se conservan.

#### Alta de cama

Se hace mediante el SP `sp_agregar_cama`:

- Verifica que la **habitación exista**.
- Permite:
  - Indicar un número de cama explícito, siempre que no exista ya en esa habitación, o
  - Generar el siguiente número correlativo dentro de la habitación.
- Inserta la cama nueva con estado `LIBRE`.

#### Baja / desactivación de cama

Se controla con `sp_eliminar_o_desactivar_cama`:

1. Solo opera sobre camas en estado `LIBRE`.  
2. Se consulta si existe **historial de internaciones** para esa cama en `SE_UBICA`:
   - Si **no tiene historial** → se hace **DELETE físico** de la fila en `CAMA`.
   - Si **tiene historial** → **no se borra**; se actualiza a estado `FUERA_DE_SERVICIO`.

Motivación:

- Si la cama nunca se usó, borrarla no genera inconsistencias.
- Si ya participó en internaciones, borrarla dejaría filas de `SE_UBICA` apuntando a una cama inexistente.  
  Por eso se mantiene en la base pero marcada como no utilizable.

---

### 2.2. Habitaciones

Las habitaciones representan la estructura física del hospital.  
No tienen campo de estado y su baja está muy restringida.

#### Modificaciones permitidas

- Se pueden modificar atributos como:
  - `piso`
  - `orientacion`
  - `id_sector` (reasignar una habitación a otro sector).

Esto permite reorganizar el hospital sin perder historial.

#### Baja de habitación

Se controla con el trigger `tr_habitacion_no_delete_if_used`:

- Antes de un `DELETE` sobre `HABITACION`, valida que:

  1. No existan internaciones activas en esa habitación.  
  2. No haya historial de uso en `SE_UBICA`.

- Si alguna de estas condiciones se cumple, el trigger **bloquea el DELETE** y devuelve un error.

En la práctica:

- Solo se puede borrar una habitación “nueva” que nunca haya sido usada.
- Si la habitación tiene historial, se mantiene para preservar la trazabilidad (el cascade a CAMA solo se ejecuta cuando no hay uso histórico).

---

### 2.3. Sectores

Los sectores son una **división lógica** del hospital (cardiología, oncología, etc.).

Reglas:

- `HABITACION.id_sector` referencia a `SECTOR.id_sector` **sin ON DELETE CASCADE**  
  → No se borran habitaciones al borrar un sector.
- `ESPECIALIDAD.id_sector` sí usa `ON DELETE CASCADE`, porque las especialidades se consideran “hijas” lógicas del sector.

#### Baja de sector

- Un sector solo se puede eliminar si ya **no tiene habitaciones asociadas**.
- Flujo típico:
  1. Reasignar las habitaciones de ese sector a otros sectores (`UPDATE HABITACION SET id_sector = ...`).
  2. Cuando el sector queda sin habitaciones hijas, se puede hacer `DELETE` sobre `SECTOR`.

---

## 3. Stored Procedures (resumen)

### 3.1. Gestión y censo de camas

**Archivo:** `sp_camas_disponibles.sql`

- `sp_camas_disponibles_resumen`  
  Devuelve, por sector, la cantidad de camas `LIBRE`.

- `sp_camas_disponibles_detalle`  
  Devuelve el detalle de camas libres para un sector (habitación, piso, orientación, cama, estado).

- `sp_agregar_cama`  
  Encapsula el alta de camas, validando la habitación y la unicidad del número de cama.

- `sp_eliminar_o_desactivar_cama`  
  Implementa la política de baja:
  - DELETE físico si no hay historial en `SE_UBICA`.
  - Cambio de estado a `FUERA_DE_SERVICIO` si la cama ya fue usada.

---

### 3.2. Internaciones y cambio de cama

**Archivo:** `sp_internaciones.sql`

- `sp_crear_internacion`  
  - Inserta la internación.
  - Asigna cama inicial (específica o primera `LIBRE`).
  - Inserta la primera ubicación en `SE_UBICA`.  
    El trigger `tr_se_ubica_cama_estado` marca la cama como `OCUPADA`.

- `sp_cambiar_cama_internacion`  
  - Permite cambiar de cama una internación activa.
  - Libera la cama anterior.
  - Inserta una nueva ubicación en `SE_UBICA` con la cama nueva (que pasa a `OCUPADA` vía trigger).

- `sp_internaciones_paciente`  
  Lista las internaciones de un paciente y devuelve `nro_internacion`, `fecha_inicio` y `fecha_fin`; la columna `estado` se calcula en Java segun si `fecha_fin` es nula o no.

- `sp_historial_ubicaciones_internacion`  
  Devuelve el historial de ubicaciones de una internación (habitaciones, camas, piso, orientación y sector).

---

### 3.3. Comentarios de visitas

**Archivo:** `sp_comentarios_visitas.sql`

- `sp_comentarios_visitas`  
  Lista, para una internación:
  - Paciente
  - Médico que realizó la visita
  - Fecha y horario del recorrido
  - Comentario registrado

---

### 3.4. Auditoría de guardias

**Archivo:** `sp_auditoria_guardias.sql`

- `sp_auditoria_guardias`  
  Consulta la tabla `AUDITORIA_GUARDIA` con filtros por usuario y rango de fechas.  
  Devuelve tipo de operación, guardia afectada y valores antiguos/nuevos.

---

### 3.5. Vacaciones de medicos

**Archivo:** `sp_vacaciones.sql`

- `sp_agregar_vacaciones`  
  Gestiona el alta de un periodo de vacaciones para un medico, aplicando las siguientes validaciones de negocio:  
  - Las fechas de inicio y fin no pueden ser nulas y deben cumplir `fecha_inicio <= fecha_fin`.  
  - El medico debe existir en las tablas `MEDICO` y `PERSONA`.  
  - No se permiten vacaciones que se solapen con otras ya registradas para el mismo medico.  
  - No se permiten vacaciones en fechas donde el medico tenga guardias cargadas en `GUARDIA`.

---

## 4. Triggers (resumen)

**Archivo:** `05-triggers.sql`

### 4.1. `tr_aud_guardia`

- AFTER INSERT / UPDATE / DELETE ON `GUARDIA`.
- Inserta en `AUDITORIA_GUARDIA`:
  - Fecha/hora
  - Usuario
  - Tipo de operación
  - Valores anteriores y nuevos.

### 4.2. `tr_se_ubica_cama_estado`

- BEFORE INSERT ON `SE_UBICA`.

Responsabilidades:

1. Verificar que la cama exista y esté `LIBRE`.
2. Validar que la fecha de ingreso esté dentro del período de internación.
3. Cambiar el estado de la cama a `OCUPADA`.

### 4.3. `tr_internacion_libera_cama`

- AFTER UPDATE OF `fecha_fin` ON `INTERNACION`  
  (solo cuando pasa de NULL a un valor no nulo).

Responsabilidades:

1. Buscar la última cama utilizada por la internación.
2. Marcar esa cama como `LIBRE`.

### 4.4. `tr_habitacion_no_delete_if_used`

- BEFORE DELETE ON `HABITACION`.

Bloquea el borrado cuando:

1. Hay internaciones activas en esa habitación, o
2. Existe historial en `SE_UBICA` para esa habitación.

---

### 4.5. `trg_int_paciente_medico_distintos`

- BEFORE INSERT / UPDATE OF `tipo_documento`, `nro_documento`, `matricula` ON `INTERNACION`.
- Impide que el medico principal sea la misma persona que el paciente (errores -20001 / -20002).

### 4.6. `tr_medico_cuil_dni`

- BEFORE INSERT / UPDATE OF `tipo_documento`, `cuil_cuit` ON `MEDICO`.
- Si el tipo_doc es `DNI`, obliga a que el `cuil_cuit` contenga el numero de documento (error -20001).

### 4.7. `tr_internacion_unica_activa`

- BEFORE INSERT / UPDATE OF `fecha_fin` ON `INTERNACION`.
- Evita que un paciente tenga mas de una internacion activa (cuenta otras activas y lanza -20050).

### 4.8. `tr_medico_especialidad_sector_ubica`

- BEFORE INSERT ON `SE_UBICA`.
- Valida que el medico principal tenga alguna especialidad en el sector de la habitacion donde se ubica al paciente (errores -20051 / -20052).

### 4.9. `tr_medico_especialidad_sector_int`

- BEFORE INSERT / UPDATE OF `matricula` ON `INTERNACION`.
- Si la internacion ya tiene ubicaciones, verifica que el medico asignado tenga especialidad en el sector de la habitacion actual (errores -20053 / -20054).

### 4.10. `tr_guardia_no_vacaciones`

- BEFORE INSERT / UPDATE OF `matricula`, `fecha_hora` ON `GUARDIA`.
- Bloquea asignar una guardia a un medico que esta de vacaciones en esa fecha (error -20110).

---

## 5. Resumen final

- **Camas**
  - Alta por SP, controlando numeración y habitación.
  - Baja física solo si nunca fueron usadas.
  - Con historial → se marcan como `FUERA_DE_SERVICIO`.

- **Habitaciones**
  - Representan estructura física.
  - No se borran si tienen internaciones activas o historial.
  - Se pueden modificar (piso, orientación, sector).

- **Sectores**
  - Agrupan habitaciones y especialidades.
  - Solo se pueden borrar cuando ya no tienen habitaciones asociadas (habitaciones reasignadas antes).

- **Stored procedures** encapsulan la lógica de negocio.  
- **Triggers** garantizan que:
  - El estado de las camas sea coherente con `SE_UBICA` e `INTERNACION`.
  - Las habitaciones con historial no puedan eliminarse.
  - Las guardias tengan auditoría automática y no se asignen si el médico está de vacaciones.
  - El médico principal sea distinto del paciente y el CUIL/CUIT contenga el DNI cuando corresponde.
  - Un paciente no tenga dos internaciones activas.
  - El médico principal tenga especialidad en el sector de la habitación asignada (al ubicar o al cambiar médico).

