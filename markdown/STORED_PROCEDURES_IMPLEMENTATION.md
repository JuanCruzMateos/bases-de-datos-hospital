# Stored Procedures Implementation - UI Integration

This document describes the implementation of UI tabs for calling stored procedures in the Hospital Management System.

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Implementations](#implementations)
  - [1. Camas Disponibles (Available Beds)](#1-camas-disponibles-available-beds)
  - [2. Visitas Médicas (Medical Visits)](#2-visitas-médicas-medical-visits)
- [Usage Guide](#usage-guide)
- [Technical Details](#technical-details)

---

## Overview

This implementation adds two new tabs to the Hospital Management System UI, each integrating with Oracle stored procedures to provide reporting and query functionality:

1. **Camas Disponibles** - Reports on available beds by sector
2. **Visitas Médicas** - Patient internations and medical visit comments

Both implementations follow the existing MVC (Model-View-Controller) pattern and maintain consistency with the application's architecture.

---

## Architecture

Each implementation follows a layered architecture:

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

### Components per Implementation

Each implementation includes:
- **DTOs** (Data Transfer Objects) - POJOs representing result sets
- **DAO Interface** - Defines data access methods
- **DAO Implementation** - JDBC code calling stored procedures
- **Service** - Business logic and validation
- **View Panel** - Swing UI components
- **Controller** - Event handling and orchestration

---

## Implementations

### 1. Camas Disponibles (Available Beds)

Provides real-time information about available beds in the hospital, organized by sector.

#### Stored Procedures Called

**`sp_camas_disponibles_resumen`**
- **Purpose**: Returns summary of free beds grouped by sector
- **Parameters**: 
  - `p_resultado OUT SYS_REFCURSOR` - Result cursor
- **Returns**: `id_sector`, `descripcion`, `camas_libres`

**`sp_camas_disponibles_detalle`**
- **Purpose**: Returns detailed list of free beds for a specific sector
- **Parameters**:
  - `p_id_sector IN NUMBER` - Sector ID
  - `p_resultado OUT SYS_REFCURSOR` - Result cursor
- **Returns**: `id_sector`, `descripcion`, `nro_habitacion`, `piso`, `orientacion`, `nro_cama`, `estado`

#### Components Created

**DTOs:**
- `CamaDisponibleResumen.java` - Summary data (sector, description, free beds count)
- `CamaDisponibleDetalle.java` - Detailed data (sector, room, floor, orientation, bed, status)

**DAO Layer:**
- `CamaDisponibleDao.java` - Interface
- `CamaDisponibleDaoImpl.java` - JDBC implementation using `CallableStatement`

**Service Layer:**
- `CamaDisponibleService.java` - Validation and business logic

**UI Layer:**
- `CamaDisponiblePanel.java` - Swing panel with two tables (summary and detail)
- `CamaDisponibleController.java` - Event handling and data loading

#### Features

✅ **Auto-load**: Summary loads automatically when tab is opened
✅ **Interactive Selection**: Click on a sector in summary to see details
✅ **Manual Search**: Enter sector ID manually to load details
✅ **Refresh**: Reload summary data on demand
✅ **Clear**: Clear detail table and input fields

#### Code Location
```
src/main/java/org/hospital/internacion/
├── CamaDisponibleResumen.java
├── CamaDisponibleDetalle.java
├── CamaDisponibleDao.java
├── CamaDisponibleDaoImpl.java
└── CamaDisponibleService.java

src/main/java/org/hospital/ui/
├── view/CamaDisponiblePanel.java
└── controller/CamaDisponibleController.java
```

---

### 2. Visitas Médicas (Medical Visits)

Provides access to patient internation history and medical visit comments.

#### Stored Procedures Called

**`sp_internaciones_paciente`**
- **Purpose**: Returns list of internations for a specific patient
- **Parameters**:
  - `p_tipo_doc IN VARCHAR2` - Document type (DNI, LC, LE, CI, PASAPORTE)
  - `p_nro_doc IN VARCHAR2` - Document number
  - `p_resultado OUT SYS_REFCURSOR` - Result cursor
- **Returns**: `nro_internacion`, `fecha_inicio`, `fecha_fin`

**`sp_comentarios_visitas`**
- **Purpose**: Returns medical visit comments for a specific internation
- **Parameters**:
  - `p_nro_internacion IN NUMBER` - Internation number
  - `p_resultado OUT SYS_REFCURSOR` - Result cursor
- **Returns**: `nro_internacion`, `paciente`, `medico`, `fecha_recorrido`, `hora_inicio`, `hora_fin`, `comentario`

#### Components Created

**DTOs:**
- `InternacionPaciente.java` - Internation data (number, start date, end date, status)
- `ComentarioVisita.java` - Visit comment (internation, patient, doctor, date, time, comment)

**DAO Layer:**
- `VisitasMedicasDao.java` - Interface for both procedures
- `VisitasMedicasDaoImpl.java` - JDBC implementation

**Service Layer:**
- `VisitasMedicasService.java` - Validation for both operations

**UI Layer:**
- `VisitasMedicasPanel.java` - Panel with patient search and two tables
- `VisitasMedicasController.java` - Event handling for search and selection

#### Features

✅ **Patient Search**: Search by document type and number
✅ **Status Display**: Shows "EN CURSO" (ongoing) or "FINALIZADA" (finished)
✅ **Interactive Selection**: Click on an internation to load its comments
✅ **Manual Load**: Use "Ver Comentarios" button after selection
✅ **Detailed Comments**: Shows full visit history with doctor names and comments
✅ **Clear**: Reset all fields and tables

#### Special Implementation Notes

**Status Calculation**: The `estado` field is calculated in the DAO layer based on `fecha_fin`:
```java
internacion.setEstado(fechaFin == null ? "EN CURSO" : "FINALIZADA");
```

This was necessary because the stored procedure doesn't return the status field directly.

#### Code Location
```
src/main/java/org/hospital/internacion/
├── InternacionPaciente.java
├── ComentarioVisita.java
├── VisitasMedicasDao.java
├── VisitasMedicasDaoImpl.java
└── VisitasMedicasService.java

src/main/java/org/hospital/ui/
├── view/VisitasMedicasPanel.java
└── controller/VisitasMedicasController.java
```

---

## Usage Guide

### Camas Disponibles Tab

1. **View Summary**:
   - Tab opens with summary automatically loaded
   - Shows sectors and count of free beds

2. **View Details**:
   - **Option A**: Click on any sector row in the summary table
   - **Option B**: Enter sector ID and click "Cargar Detalle"
   - Details show specific rooms, floors, and bed numbers

3. **Actions**:
   - **Cargar Resumen**: Refresh the summary data
   - **Cargar Detalle**: Load details for entered sector ID
   - **Limpiar**: Clear detail table and input field

### Visitas Médicas Tab

1. **Search Patient**:
   - Select document type from dropdown (DNI, LC, LE, CI, PASAPORTE)
   - Enter document number
   - Click "Buscar Internaciones"

2. **View Internations**:
   - Table shows all internations for the patient
   - Displays start date, end date, and status

3. **View Comments**:
   - **Option A**: Click on any internation row (auto-loads)
   - **Option B**: Select internation and click "Ver Comentarios"
   - Comments table shows visit history with dates, doctors, and notes

4. **Actions**:
   - **Buscar Internaciones**: Search for patient internations
   - **Ver Comentarios**: Load comments for selected internation
   - **Limpiar**: Clear all fields and tables

---

## Technical Details

### JDBC Connection Handling

All DAO implementations use try-with-resources for proper resource management:

```java
try (Connection conn = DatabaseConfig.getConnection();
     CallableStatement stmt = conn.prepareCall(sql)) {
    // Execute procedure
    // Process results
}
```

### Calling Stored Procedures

**Pattern for procedures with OUT cursor parameter:**

```java
String sql = "{CALL procedure_name(?, ?)}";
CallableStatement stmt = conn.prepareCall(sql);

// Set IN parameters
stmt.setInt(1, inputValue);

// Register OUT parameter for cursor
stmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);

// Execute
stmt.execute();

// Get result set from cursor
try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
    while (rs.next()) {
        // Process results
    }
}
```

### Error Handling

All implementations include comprehensive error handling:

1. **DAO Layer**: Catches `SQLException`, logs errors, throws `DataAccessException`
2. **Service Layer**: Validates input parameters, throws `IllegalArgumentException`
3. **Controller Layer**: Catches exceptions, displays user-friendly messages

Example:
```java
try {
    // Call service
} catch (IllegalArgumentException e) {
    showError("Error de validación: " + e.getMessage());
} catch (DataAccessException e) {
    handleDataAccessException(e);
} catch (Exception e) {
    handleException(e);
}
```

### Logging

All components use Java Logging API:
- **INFO**: User actions, method entry points
- **FINE**: Detailed operation results (e.g., record counts)
- **SEVERE**: Database errors, exceptions

Example:
```java
logger.info("Service: Getting available beds summary");
logger.fine("DAO: Retrieved " + resultados.size() + " records");
logger.severe("Database error calling sp_camas_disponibles_resumen: " + e.getMessage());
```

### UI Integration

New tabs are registered in `HospitalUI.java`:

```java
// Create panels
camaDisponiblePanel = new CamaDisponiblePanel();
visitasMedicasPanel = new VisitasMedicasPanel();

// Add tabs
tabbedPane.addTab("Camas Disponibles", new ImageIcon(), 
                  camaDisponiblePanel, "View Available Beds Reports");
tabbedPane.addTab("Visitas Médicas", new ImageIcon(), 
                  visitasMedicasPanel, "View Patient Internations & Visit Comments");

// Initialize controllers
camaDisponibleController = new CamaDisponibleController(camaDisponiblePanel);
visitasMedicasController = new VisitasMedicasController(visitasMedicasPanel);
```

---

## Building and Running

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

Or directly:
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar
```

---

## Testing

### Test Scenarios

#### Camas Disponibles
1. ✅ Load summary - verify all sectors appear
2. ✅ Click sector in summary - verify details load
3. ✅ Manual sector ID entry - verify details load
4. ✅ Invalid sector ID - verify error message
5. ✅ Empty result set - verify user notification

#### Visitas Médicas
1. ✅ Search existing patient - verify internations appear
2. ✅ Search non-existent patient - verify empty result message
3. ✅ Click internation - verify comments load
4. ✅ Empty document field - verify validation error
5. ✅ Internation with no comments - verify empty result message

---

## Database Schema Dependencies

### Required Tables
- `SECTOR`
- `HABITACION`
- `CAMA`
- `INTERNACION`
- `PACIENTE`
- `PERSONA`
- `COMENTA_SOBRE`
- `RECORRIDO`
- `MEDICO`

### Required Stored Procedures
- `sp_camas_disponibles_resumen`
- `sp_camas_disponibles_detalle`
- `sp_internaciones_paciente`
- `sp_comentarios_visitas`

Location: `db_scripts/procedures/`

---

## Future Enhancements

### Potential Improvements

1. **Camas Disponibles**:
   - Export to CSV/Excel
   - Filter by floor or orientation
   - Visual bed availability chart
   - Real-time auto-refresh

2. **Visitas Médicas**:
   - Date range filtering
   - Export visit history
   - Search by doctor
   - Add new comments from UI
   - Print visit summary report

3. **General**:
   - Pagination for large result sets
   - Advanced search filters
   - Data caching for better performance
   - Print/PDF export functionality

---

## Contributors

**Grupo 4 - FI UNMdP**
- Bases de Datos
- Hospital Management System

---

## Version History

- **v1.1** (2024-11-26): Added Visitas Médicas tab
- **v1.0** (2024-11-26): Initial implementation with Camas Disponibles tab

---

## License

© 2024 Hospital Database System | Grupo 4 - FI UNMdP

