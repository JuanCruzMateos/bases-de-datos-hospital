# Implementacion de Stored Procedures - Integracion UI

Este documento describe la implementacion de las pestañas de UI que invocan stored procedures en el Sistema de Gestion Hospitalaria.

## Tabla de contenidos
- [Resumen](#resumen)
- [Arquitectura](#arquitectura)
- [Implementaciones](#implementaciones)
  - [1. Camas Disponibles](#1-camas-disponibles)
  - [2. Visitas Medicas](#2-visitas-medicas)
- [Guia de uso](#guia-de-uso)
- [Detalles tecnicos](#detalles-tecnicos)
- [Compilacion y ejecucion](#compilacion-y-ejecucion)
- [Pruebas](#pruebas)
- [Dependencias de esquema](#dependencias-de-esquema)
- [Mejoras futuras](#mejoras-futuras)
- [Contribuyentes](#contribuyentes)
- [Historial de versiones](#historial-de-versiones)
- [Licencia](#licencia)

---

## Resumen

Esta implementacion agrega dos pestañas nuevas a la UI del sistema, integradas con stored procedures de Oracle para reportes y consultas:

1. **Camas Disponibles**: reporta camas libres por sector.
2. **Visitas Medicas**: historico de internaciones de un paciente y comentarios de visitas medicas.

Ambas siguen el patron MVC y mantienen la coherencia con la arquitectura existente.

---

## Arquitectura

Cada implementacion usa una arquitectura en capas:

```
┌─────────────────────────────────────────────┐
│              UI Layer (Swing)               │
│  Panel (View) + Controller                  │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           Service Layer                     │
│  Business Logic + Validation                │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           DAO Layer                         │
│  Database Access + JDBC                     │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│       Oracle Stored Procedures              │
│  sp_camas_disponibles_*                     │
│  sp_internaciones_paciente                  │
│  sp_comentarios_visitas                     │
└─────────────────────────────────────────────┘
```

### Componentes por implementacion

Cada una incluye:
- **DTOs** (Data Transfer Objects): POJOs que representan los result sets.
- **Interfaz DAO**: define los metodos de acceso a datos.
- **Implementacion DAO**: codigo JDBC que invoca los stored procedures.
- **Servicio**: logica de negocio y validaciones.
- **Panel de vista**: componentes Swing.
- **Controller**: manejo de eventos y orquestacion.

---

## Implementaciones

### 1. Camas Disponibles

Provee informacion actualizada de camas libres, organizadas por sector.

#### Stored procedures invocados

**`sp_camas_disponibles_resumen`**
- **Proposito**: devuelve resumen de camas libres agrupadas por sector.
- **Parametros**:
  - `p_resultado OUT SYS_REFCURSOR` (cursor de salida).
- **Devuelve**: `id_sector`, `descripcion`, `camas_libres`.

**`sp_camas_disponibles_detalle`**
- **Proposito**: devuelve el detalle de camas libres para un sector.
- **Parametros**:
  - `p_id_sector IN NUMBER` (ID de sector).
  - `p_resultado OUT SYS_REFCURSOR` (cursor de salida).
- **Devuelve**: `id_sector`, `descripcion`, `nro_habitacion`, `piso`, `orientacion`, `nro_cama`, `estado`.

#### Componentes creados

**DTOs:**
- `CamaDisponibleResumen.java` (sector, descripcion, cantidad de camas libres).
- `CamaDisponibleDetalle.java` (sector, habitacion, piso, orientacion, cama, estado).

**Capa DAO:**
- `CamaDisponibleDao.java` (interfaz).
- `CamaDisponibleDaoImpl.java` (JDBC con `CallableStatement`).

**Capa Servicio:**
- `CamaDisponibleService.java` (validacion y logica).

**Capa UI:**
- `CamaDisponiblePanel.java` (panel con dos tablas: resumen y detalle).
- `CamaDisponibleController.java` (eventos y carga de datos).

#### Funcionalidades

- **Auto-carga**: el resumen se carga al abrir la pestaña.
- **Seleccion interactiva**: click en un sector del resumen carga el detalle.
- **Busqueda manual**: ingresar ID de sector y cargar detalle.
- **Refresco**: recarga el resumen.
- **Limpiar**: limpia tabla de detalle y campos.

#### Ubicacion de codigo
```
src/main/java/org/hospital/internacion/
├─ CamaDisponibleResumen.java
├─ CamaDisponibleDetalle.java
├─ CamaDisponibleDao.java
├─ CamaDisponibleDaoImpl.java
└─ CamaDisponibleService.java

src/main/java/org/hospital/ui/
├─ view/CamaDisponiblePanel.java
└─ controller/CamaDisponibleController.java
```

---

### 2. Visitas Medicas

Permite consultar internaciones de un paciente y comentarios de sus visitas medicas.

#### Stored procedures invocados

**`sp_internaciones_paciente`**
- **Proposito**: devuelve la lista de internaciones de un paciente.
- **Parametros**:
  - `p_tipo_doc IN VARCHAR2` (DNI, LC, LE, CI, PASAPORTE).
  - `p_nro_doc IN VARCHAR2` (numero de documento).
  - `p_resultado OUT SYS_REFCURSOR` (cursor de salida).
- **Devuelve**: `nro_internacion`, `fecha_inicio`, `fecha_fin`.

**`sp_comentarios_visitas`**
- **Proposito**: devuelve comentarios de visitas medicas para una internacion.
- **Parametros**:
  - `p_nro_internacion IN NUMBER` (numero de internacion).
  - `p_resultado OUT SYS_REFCURSOR` (cursor de salida).
- **Devuelve**: `nro_internacion`, `paciente`, `medico`, `fecha_recorrido`, `hora_inicio`, `hora_fin`, `comentario`.

#### Componentes creados

**DTOs:**
- `InternacionPaciente.java` (internacion: numero, fecha inicio/fin, estado).
- `ComentarioVisita.java` (internacion, paciente, medico, fecha, hora, comentario).

**Capa DAO:**
- `VisitasMedicasDao.java` (interfaz para ambos procedures).
- `VisitasMedicasDaoImpl.java` (implementacion JDBC).

**Capa Servicio:**
- `VisitasMedicasService.java` (validaciones).

**Capa UI:**
- `VisitasMedicasPanel.java` (panel con busqueda de paciente y dos tablas).
- `VisitasMedicasController.java` (eventos para buscar y seleccionar).

#### Funcionalidades

- **Busqueda de paciente**: por tipo y numero de documento.
- **Estado**: muestra "EN CURSO" o "FINALIZADA".
- **Seleccion interactiva**: click en una internacion para cargar comentarios.
- **Carga manual**: boton "Ver Comentarios" tras seleccionar.
- **Comentarios detallados**: historial completo con medico y notas.
- **Limpiar**: limpia campos y tablas.

#### Notas de implementacion

**Calculo de estado**: el campo `estado` se calcula en la capa DAO a partir de `fecha_fin`:
```java
internacion.setEstado(fechaFin == null ? "EN CURSO" : "FINALIZADA");
```
Esto es necesario porque el stored procedure no devuelve el estado directamente.

#### Ubicacion de codigo
```
src/main/java/org/hospital/internacion/
├─ InternacionPaciente.java
├─ ComentarioVisita.java
├─ VisitasMedicasDao.java
├─ VisitasMedicasDaoImpl.java
└─ VisitasMedicasService.java

src/main/java/org/hospital/ui/
├─ view/VisitasMedicasPanel.java
└─ controller/VisitasMedicasController.java
```

---

## Guia de uso

### Pestaña Camas Disponibles

1. **Ver resumen**:
   - La pestaña abre con el resumen cargado.
   - Muestra sectores y cantidad de camas libres.
2. **Ver detalle**:
   - Opcion A: click en un sector de la tabla resumen.
   - Opcion B: ingresar ID de sector y click en "Cargar Detalle".
   - Muestra habitaciones, pisos y numeros de cama.
3. **Acciones**:
   - **Cargar Resumen**: recarga datos.
   - **Cargar Detalle**: carga detalle para el sector ingresado.
   - **Limpiar**: limpia tabla de detalle y campo de entrada.

### Pestaña Visitas Medicas

1. **Buscar paciente**:
   - Elegir tipo de documento (DNI, LC, LE, CI, PASAPORTE).
   - Ingresar numero de documento.
   - Click en "Buscar Internaciones".
2. **Ver internaciones**:
   - Tabla con todas las internaciones del paciente (inicio, fin, estado).
3. **Ver comentarios**:
   - Opcion A: click en una internacion (carga automatica).
   - Opcion B: seleccionar y click en "Ver Comentarios".
   - Tabla muestra fechas, medicos y comentarios.
4. **Acciones**:
   - **Buscar Internaciones**: ejecuta la busqueda.
   - **Ver Comentarios**: carga comentarios de la internacion seleccionada.
   - **Limpiar**: limpia campos y tablas.

---

## Detalles tecnicos

### Manejo de conexion JDBC

Uso de try-with-resources para manejar recursos:
```java
try (Connection conn = DatabaseConfig.getConnection();
     CallableStatement stmt = conn.prepareCall(sql)) {
    // Ejecutar procedure
    // Procesar resultados
}
```

### Llamada a stored procedures

Patron para procedures con cursor OUT:
```java
String sql = "{CALL procedure_name(?, ?)}";
CallableStatement stmt = conn.prepareCall(sql);

// Parametros IN
stmt.setInt(1, inputValue);

// Registrar OUT cursor
stmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

stmt.execute();

try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
    while (rs.next()) {
        // Procesar resultados
    }
}
```

### Manejo de errores

1. **DAO**: captura `SQLException`, loguea y lanza `DataAccessException`.
2. **Servicio**: valida parametros de entrada, lanza `IllegalArgumentException`.
3. **Controller**: captura excepciones y muestra mensajes al usuario.

Ejemplo:
```java
try {
    // Llamar servicio
} catch (IllegalArgumentException e) {
    showError("Error de validacion: " + e.getMessage());
} catch (DataAccessException e) {
    handleDataAccessException(e);
} catch (Exception e) {
    handleException(e);
}
```

### Logging

Java Logging API:
- **INFO**: acciones de usuario y entrada a metodos.
- **FINE**: detalles (cantidades de registros, etc.).
- **SEVERE**: errores de base de datos y excepciones.

Ejemplo:
```java
logger.info("Service: obteniendo resumen de camas disponibles");
logger.fine("DAO: se recuperaron " + resultados.size() + " registros");
logger.severe("Error de base al llamar sp_camas_disponibles_resumen: " + e.getMessage());
```

### Integracion UI

Las nuevas pestañas se registran en `HospitalUI.java`:
```java
// Crear paneles
camaDisponiblePanel = new CamaDisponiblePanel();
visitasMedicasPanel = new VisitasMedicasPanel();

// Agregar pestañas
tabbedPane.addTab("Camas Disponibles", new ImageIcon(),
                  camaDisponiblePanel, "Ver reportes de camas libres");
tabbedPane.addTab("Visitas Medicas", new ImageIcon(),
                  visitasMedicasPanel, "Internaciones y comentarios de visitas");

// Inicializar controllers
camaDisponibleController = new CamaDisponibleController(camaDisponiblePanel);
visitasMedicasController = new VisitasMedicasController(visitasMedicasPanel);
```

---

## Compilacion y ejecucion

### Compilar
```bash
mvn clean compile
```

### Empaquetar
```bash
mvn clean package
```

### Ejecutar
```bash
./launch-ui.sh
```
O bien:
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar
```

---

## Pruebas

### Escenarios sugeridos

#### Camas Disponibles
1. Cargar resumen: verificar que aparezcan todos los sectores.
2. Click en sector del resumen: verificar que cargue detalle.
3. Ingreso manual de ID de sector: verificar detalle.
4. ID invalido: mensaje de error.
5. Resultado vacio: notificacion al usuario.

#### Visitas Medicas
1. Paciente existente: internaciones visibles.
2. Paciente inexistente: resultado vacio informado.
3. Click en internacion: carga comentarios.
4. Documento vacio: error de validacion.
5. Internacion sin comentarios: resultado vacio informado.

---

## Dependencias de esquema

### Tablas requeridas
- `SECTOR`
- `HABITACION`
- `CAMA`
- `INTERNACION`
- `PACIENTE`
- `PERSONA`
- `COMENTA_SOBRE`
- `RECORRIDO`
- `MEDICO`

### Stored procedures requeridos
- `sp_camas_disponibles_resumen`
- `sp_camas_disponibles_detalle`
- `sp_internaciones_paciente`
- `sp_comentarios_visitas`

Ubicacion: `db_scripts/procedures/`

---

## Mejoras futuras

### Ideas

1. **Camas Disponibles**:
   - Exportar a CSV/Excel.
   - Filtro por piso u orientacion.
   - Grafico de disponibilidad.
   - Auto-refresco en tiempo real.

2. **Visitas Medicas**:
   - Filtro por rango de fechas.
   - Exportar historial.
   - Busqueda por medico.
   - Alta de comentarios desde la UI.
   - Impresion de resumen de visitas.

3. **General**:
   - Paginado para grandes volúmenes.
   - Filtros avanzados de busqueda.
   - Cache de datos para performance.
   - Exportar/Imprimir en PDF.

---

## Contribuyentes

**Grupo 4 - FI UNMdP**
- Bases de Datos
- Hospital Management System

---

## Historial de versiones

- **v1.1** (2024-11-26): se agrega pestaña Visitas Medicas.
- **v1.0** (2024-11-26): implementacion inicial con pestaña Camas Disponibles.

---

## Licencia

(c) 2024 Hospital Database System | Grupo 4 - FI UNMdP
