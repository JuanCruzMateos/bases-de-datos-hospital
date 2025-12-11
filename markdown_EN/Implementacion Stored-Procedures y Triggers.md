# Stored Procedure Implementation - UI Integration
Spanish version available in [../markdown_ES/Implementacion Stored-Procedures y Triggers.md](../markdown_ES/Implementacion%20Stored-Procedures%20y%20Triggers.md).

This document describes the implementation of the UI tabs that invoke stored procedures in the Hospital Management System.

## Table of contents
- [Summary](#summary)
- [Architecture](#architecture)
- [Implementations](#implementations)
  - [1. Camas Disponibles](#1-camas-disponibles)
  - [2. Visitas Medicas](#2-visitas-medicas)
  - [3. Auditoria de Guardias](#3-auditoria-de-guardias)
- [User guide](#user-guide)
- [Technical details](#technical-details)
- [Build and run](#build-and-run)
- [Tests](#tests)
- [Schema dependencies](#schema-dependencies)
- [Future improvements](#future-improvements)
- [Contributors](#contributors)
- [Version history](#version-history)
- [License](#license)

---

## Summary

This implementation adds two new UI tabs to the system, integrated with Oracle stored procedures for reports and queries:

1. **Camas Disponibles**: reports available beds by sector.
2. **Visitas Medicas**: history of a patient's internments and medical visit comments.

Both follow the MVC pattern and stay aligned with the existing architecture.

---

## Architecture

Each implementation uses a layered architecture:

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
│  sp_auditoria_guardias                      │
└─────────────────────────────────────────────┘
```

Stored procedures used in these layers:
- sp_camas_disponibles_resumen
- sp_camas_disponibles_detalle
- sp_internaciones_paciente
- sp_comentarios_visitas
- sp_auditoria_guardias

### Components per implementation

Each one includes:
- **DTOs** (Data Transfer Objects): POJOs representing the result sets.
- **DAO interface**: defines data access methods.
- **DAO implementation**: JDBC code invoking the stored procedures.
- **Service**: business logic and validations.
- **View panel**: Swing components.
- **Controller**: event handling and orchestration.

---

## Implementations

### 1. Camas Disponibles

Provides up-to-date information on available beds, organized by sector.

#### Stored procedures invoked

**`sp_camas_disponibles_resumen`**
- **Purpose**: returns a summary of available beds grouped by sector.
- **Parameters**:
  - `p_resultado OUT SYS_REFCURSOR` (output cursor).
- **Returns**: `id_sector`, `descripcion`, `camas_libres`.

**`sp_camas_disponibles_detalle`**
- **Purpose**: returns the detail of available beds for a sector.
- **Parameters**:
  - `p_id_sector IN NUMBER` (sector ID).
  - `p_resultado OUT SYS_REFCURSOR` (output cursor).
- **Returns**: `id_sector`, `descripcion`, `nro_habitacion`, `piso`, `orientacion`, `nro_cama`, `estado`.

#### Components created

**DTOs:**
- `CamaDisponibleResumen.java` (sector, description, number of available beds).
- `CamaDisponibleDetalle.java` (sector, room, floor, orientation, bed, status).

**DAO layer:**
- `CamaDisponibleDao.java` (interface).
- `CamaDisponibleDaoImpl.java` (JDBC with `CallableStatement`).

**Service layer:**
- `CamaDisponibleService.java` (validation and logic).

**UI layer:**
- `CamaDisponiblePanel.java` (panel with two tables: summary and detail).
- `CamaDisponibleController.java` (events and data loading).

#### Features

- **Auto-load**: the summary loads when the tab opens.
- **Interactive selection**: clicking a sector in the summary loads the detail.
- **Manual search**: enter sector ID and load detail.
- **Refresh**: reloads the summary.
- **Clear**: clears detail table and fields.

#### Code location
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

Allows querying a patient's internments and comments from their medical visits.

#### Stored procedures invoked

**`sp_internaciones_paciente`**
- **Purpose**: returns the list of internments for a patient.
- **Parameters**:
  - `p_tipo_doc IN VARCHAR2` (DNI, LC, LE, CI, PASAPORTE).
  - `p_nro_doc IN VARCHAR2` (document number).
  - `p_resultado OUT SYS_REFCURSOR` (output cursor).
- **Returns**: `nro_internacion`, `fecha_inicio`, `fecha_fin`.

**`sp_comentarios_visitas`**
- **Purpose**: returns medical visit comments for an internment.
- **Parameters**:
  - `p_nro_internacion IN NUMBER` (internment number).
  - `p_resultado OUT SYS_REFCURSOR` (output cursor).
- **Returns**: `nro_internacion`, `paciente`, `medico`, `fecha_recorrido`, `hora_inicio`, `hora_fin`, `comentario`.

#### Components created

**DTOs:**
- `InternacionPaciente.java` (internment: number, start/end date, status).
- `ComentarioVisita.java` (internment, patient, doctor, date, time, comment).

**DAO layer:**
- `VisitasMedicasDao.java` (interface for both procedures).
- `VisitasMedicasDaoImpl.java` (JDBC implementation).

**Service layer:**
- `VisitasMedicasService.java` (validations).

**UI layer:**
- `VisitasMedicasPanel.java` (panel with patient search and two tables).
- `VisitasMedicasController.java` (events for searching and selecting).

#### Features

- **Patient search**: by document type and number.
- **Status**: shows "EN CURSO" or "FINALIZADA".
- **Interactive selection**: click an internment to load comments.
- **Manual load**: **Ver Comentarios** button after selecting.
- **Detailed comments**: complete history with doctor and notes.
- **Clear**: clears fields and tables.

#### Implementation notes

**Status calculation**: field `estado` is calculated in the DAO layer from `fecha_fin`:
```java
internacion.setEstado(fechaFin == null ? "EN CURSO" : "FINALIZADA");
```
This is necessary because the stored procedure does not return the state directly.

#### Code location
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

### 3. Auditoria de Guardias

Allows viewing the change history of guard shifts recorded by audit triggers.

#### Stored procedures invoked

**`sp_auditoria_guardias`**
- **Purpose**: returns audit records from table `AUDITORIA_GUARDIA`.
- **Parameters**:
  - `p_usuario IN VARCHAR2` (database user, can be null for all).
  - `p_desde IN TIMESTAMP` (from date/time, optional).
  - `p_hasta IN TIMESTAMP` (to date/time, optional).
  - `p_resultado OUT SYS_REFCURSOR` (output cursor).
- **Returns**: `id_auditoria`, `fecha_hora_reg`, `usuario_bd`, `operacion`, `nro_guardia`, `fecha_hora_guard`, `matricula`, `cod_especialidad`, `id_turno`, `detalle_old`, `detalle_new`.

#### Components created

**DTOs:**
- `AuditoriaGuardia.java` (id, dates, user, operation type, guard, doctor, specialty, shift, and before/after details).

**DAO layer:**
- `AuditoriaGuardiasDao.java` (interface for the report).
- `AuditoriaGuardiasDaoImpl.java` (JDBC implementation calling `sp_auditoria_guardias` with optional filters).

**Service layer:**
- `AuditoriaGuardiasService.java` (validates the date range and delegates to the DAO).

**UI layer:**
- `AuditoriaGuardiasPanel.java` (table with all audit fields and Refresh button).
- `AuditoriaGuardiasController.java` (initial load and data reload on Refresh).

#### Features

- **Initial load**: when opening the tab, all available audit records are loaded.
- **Manual refresh**: **Actualizar** button to reload data from the database.
- **Detailed view**: for each change, shows user, operation type (INSERT, UPDATE, DELETE), affected guard, and old/new values.

#### Code location
```
src/main/java/org/hospital/feature/internacion/
... domain/AuditoriaGuardia.java
... repository/AuditoriaGuardiasDao.java
... repository/AuditoriaGuardiasDaoImpl.java
... service/AuditoriaGuardiasService.java

src/main/java/org/hospital/ui/
... view/AuditoriaGuardiasPanel.java
... controller/AuditoriaGuardiasController.java
```

---

## User guide

### Camas Disponibles tab

1. **View summary**:
   - The tab opens with the summary loaded.
   - Shows sectors and number of available beds.
2. **View detail**:
   - Option A: click a sector in the summary table.
   - Option B: enter sector ID and click "Cargar Detalle".
   - Shows rooms, floors, and bed numbers.
3. **Actions**:
   - **Cargar Resumen**: reloads data.
   - **Cargar Detalle**: loads detail for the entered sector.
   - **Limpiar**: clears detail table and input field.

### Visitas Medicas tab

1. **Search patient**:
   - Choose document type (DNI, LC, LE, CI, PASAPORTE).
   - Enter document number.
   - Click "Buscar Internaciones".
2. **View internments**:
   - Table with all patient internments (start, end, status).
3. **View comments**:
   - Option A: click an internment (automatic load).
   - Option B: select and click "Ver Comentarios".
   - Table shows dates, doctors, and comments.
4. **Actions**:
   - **Buscar Internaciones**: executes the search.
   - **Ver Comentarios**: loads comments for the selected internment.
   - **Limpiar**: clears fields and tables.

---

## Technical details

### JDBC connection handling

Use try-with-resources to handle resources:
```java
try (Connection conn = DatabaseConfig.getConnection();
     CallableStatement stmt = conn.prepareCall(sql)) {
    // Execute procedure
    // Process results
}
```

### Calling stored procedures

Pattern for procedures with OUT cursor:
```java
String sql = "{CALL procedure_name(?, ?)}";
CallableStatement stmt = conn.prepareCall(sql);

// IN parameters
stmt.setInt(1, inputValue);

// Register OUT cursor
stmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

stmt.execute();

try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
    while (rs.next()) {
        // Process results
    }
}
```

### Error handling

1. **DAO**: captures `SQLException`, logs, and throws `DataAccessException`.
2. **Service**: validates input parameters, throws `IllegalArgumentException`.
3. **Controller**: captures exceptions and shows messages to the user.

Example:
```java
try {
    // Call service
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
- **INFO**: user actions and method entry.
- **FINE**: details (record counts, etc.).
- **SEVERE**: database errors and exceptions.

Example:
```java
logger.info("Service: obteniendo resumen de camas disponibles");
logger.fine("DAO: se recuperaron " + resultados.size() + " registros");
logger.severe("Error de base al llamar sp_camas_disponibles_resumen: " + e.getMessage());
```

### UI integration

The new tabs are registered in `HospitalUI.java`:
```java
// Create panels
camaDisponiblePanel = new CamaDisponiblePanel();
visitasMedicasPanel = new VisitasMedicasPanel();

// Add tabs
tabbedPane.addTab("Camas Disponibles", new ImageIcon(),
                  camaDisponiblePanel, "Ver reportes de camas libres");
tabbedPane.addTab("Visitas Medicas", new ImageIcon(),
                  visitasMedicasPanel, "Internaciones y comentarios de visitas");

// Initialize controllers
camaDisponibleController = new CamaDisponibleController(camaDisponiblePanel);
visitasMedicasController = new VisitasMedicasController(visitasMedicasPanel);
```

---

## Build and run

### Compile
```bash
mvn clean compile
```

### Package
```bash
mvn clean package
```

### Run
```bash
./launch-ui.sh
```
Or:
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar
```

---

## Tests

### Suggested scenarios

#### Camas Disponibles
1. Load summary: verify all sectors appear.
2. Click sector in summary: verify detail loads.
3. Manual entry of sector ID: verify detail.
4. Invalid ID: error message.
5. Empty result: notification to the user.

#### Visitas Medicas
1. Existing patient: internments visible.
2. Nonexistent patient: empty result reported.
3. Click internment: loads comments.
4. Empty document: validation error.
5. Internment without comments: empty result reported.

---

## Schema dependencies

### Required tables
- `SECTOR`
- `HABITACION`
- `CAMA`
- `INTERNACION`
- `PACIENTE`
- `PERSONA`
- `COMENTA_SOBRE`
- `RECORRIDO`
- `MEDICO`

### Required stored procedures
- `sp_camas_disponibles_resumen`
- `sp_camas_disponibles_detalle`
- `sp_internaciones_paciente`
- `sp_comentarios_visitas`
- `sp_auditoria_guardias`

Location: `db_scripts/procedures/`

---

## Future improvements

### Ideas

1. **Camas Disponibles**:
   - Export to CSV/Excel.
   - Filter by floor or orientation.
   - Availability chart.
   - Real-time auto-refresh.

2. **Visitas Medicas**:
   - Date range filter.
   - Export history.
   - Search by doctor.
   - Add comments from the UI.
   - Print visit summary.

3. **General**:
   - Pagination for large volumes.
   - Advanced search filters.
   - Data cache for performance.
   - Export/Print to PDF.

---

## Contributors

- **Mateos, Juan Cruz**
- **San Pedro, Gianfranco**

---

## Version history

- **v1.1** (2024-11-26): adds Visitas Medicas tab.
- **v1.0** (2024-11-26): initial implementation with Camas Disponibles tab.

---

## License

(c) 2024 Hospital Database System | Project developed by Juan and Gianfranco - FI UNMdP
