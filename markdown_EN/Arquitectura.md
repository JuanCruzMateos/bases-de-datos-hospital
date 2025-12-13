# Hospital Management System - Architecture V1
Spanish version available in [../markdown_ES/Arquitectura.md](../markdown_ES/Arquitectura.md).

## ✅ Layered architecture - Manual transaction handling

```
┌──────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│                         (Controllers)                        │
├──────────────────────────────────────────────────────────────┤
│  ✅ PacienteController    → PacienteService                  │
│  ✅ MedicoController      → MedicoService                    │
│  ✅ SectorController      → SectorService                    │
│  ✅ HabitacionController  → HabitacionService                │
│  ✅ InternacionController → InternacionService               │
│  ✅ GuardiaController     → GuardiaService                   │
│  ✅ CamaDisponibleController → CamaDisponibleService         │
│  ✅ VisitasMedicasController  → VisitasMedicasService        │
│  ✅ AuditoriaGuardiasController → AuditoriaGuardiasService   │
│  ✅ VacacionesController  → VacacionesService                │
│                                                              │
│  Common: BaseController (logging + error handling)           │
└──────────────────────┬───────────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────────┐
│                        SERVICE LAYER                         │
│                      (Business logic)                        │
├──────────────────────────────────────────────────────────────┤
│  ✅ PacienteService        - Patient business rules           │
│  ✅ MedicoService          - Doctor business rules            │
│  ✅ SectorService          - Sector business rules            │
│  ✅ HabitacionService      - Room business rules              │
│  ✅ InternacionService     - Internments and beds             │
│  ✅ GuardiaService         - Medical guard rules              │
│  ✅ CamaService            - Bed handling and status          │
│  ✅ CamaDisponibleService  - Available beds reports           │
│  ✅ VisitasMedicasService  - Internment reports and           │
│                              visit comments                   │
│  ✅ AuditoriaGuardiasService - Guard audit reports            │
│  ✅ VacacionesService      - Doctor vacation logic            │
│                                                              │
│  Features:                                                   │
│  • Business validation                                       │
│  • Cross-entity validation                                   │
│  • Duplicate prevention                                      │
│  • Logging                                                   │
│  • Domain-specific rules                                     │
└──────────────────────┬───────────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────────┐
│                      DATA ACCESS LAYER                       │
│                     (Manual transactions)                    │
├──────────────────────────────────────────────────────────────┤
│  ✅ PacienteDaoImpl        (manual tx + logging)             │
│  ✅ MedicoDaoImpl          (manual tx + logging)             │
│  ✅ SectorDaoImpl          (manual tx + logging)             │
│  ✅ HabitacionDaoImpl      (manual tx + logging)             │
│  ✅ InternacionDaoImpl     (manual tx + logging)             │
│  ✅ GuardiaDaoImpl         (manual tx + logging)             │
│  ✅ EspecialidadDaoImpl    (manual tx + logging)             │
│  ✅ TurnoDaoImpl           (manual tx + logging)             │
│  ✅ CamaDaoImpl            (manual tx + logging)             │
│  ✅ SeUbicaDaoImpl         (manual tx + logging)             │
│  ✅ CamaDisponibleDaoImpl  (read-only, stored procedures)    │
│  ✅ VisitasMedicasDaoImpl  (read-only, stored procedures)    │
│  ✅ AuditoriaGuardiasDaoImpl (read-only, stored procedure)   │
│  ✅ VacacionesDaoImpl      (transactional PL/SQL)            │
│                                                              │
│  Transaction pattern:                                        │
│  • connection = DriverManager.getConnection()                │
│  • connection.setAutoCommit(false)                           │
│  • Execute SQL with PreparedStatement                        │
│  • connection.commit() on success                            │
│  • connection.rollback() on error                            │
│  • connection.close() in finally                             │
└──────────────────────┬───────────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────────┐
│                  DATABASE CONNECTION                         │
│                    (JDBC DriverManager)                      │
├──────────────────────────────────────────────────────────────┤
│  ✅ Basic JDBC via DriverManager                             │
│  ✅ One connection per operation (no pool in V1)             │
│  ✅ Properties from application.properties                   │
│  ✅ Oracle JDBC Driver (ojdbc8)                              │
│                                                              │
│  V2 will add: Connection Pooling (HikariCP)                  │
└──────────────────────┬───────────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────────┐
│                         DATABASE                             │
│                       (Oracle DB)                            │
└──────────────────────────────────────────────────────────────┘
```

---

## 📊 Consistency matrix

| Entity / Module  | Controller✅ | Service✅ | DAO              | Manual Tx | Logging✅ |
|------------------|-------------|----------|------------------|-----------|----------|
| Paciente         | ✅ Service  | ✅ Full  | ✅ Full          | ✅ Yes    | ✅ Full  |
| Medico           | ✅ Service  | ✅ Full  | ✅ Full          | ✅ Yes    | ✅ Full  |
| Sector           | ✅ Service  | ✅ Full  | ✅ Full          | ✅ Yes    | ✅ Full  |
| Habitacion       | ✅ Service  | ✅ Full  | ✅ Full          | ✅ Yes    | ✅ Full  |
| Internacion      | ✅ Service  | ✅ Full  | ✅ Full          | ✅ Yes    | ✅ Full  |
| Guardia          | ✅ Service  | ✅ Full  | ✅ Full          | ✅ Yes    | ✅ Full  |
| Cama             | N/A         | ✅ Full  | ✅ Full          | ✅ Yes    | ✅ Full  |
| SeUbica          | N/A         | via Cama | ✅ Full          | ✅ Yes    | ✅ Full  |
| Especialidad     | N/A         | via Medico| ✅ Full         | ✅ Yes    | ✅ Full  |
| Turno            | N/A         | N/A      | ✅ Full          | ✅ Yes    | ✅ Full  |
| Camas Disponibles| ✅ Service  | ✅ Full  | ✅ SP read-only  | N/A       | ✅ Full  |
| Visitas Medicas  | ✅ Service  | ✅ Full  | ✅ SP read-only  | N/A       | ✅ Full  |
| AuditoriaGuardias| ✅ Service  | ✅ Full  | ✅ SP read-only  | N/A       | ✅ Full  |
| Vacaciones       | ✅ Service  | ✅ Full  | ✅ PL/SQL Tx     | ✅ Yes    | ✅ Full  |

Legend:
- ✅ Fully implemented
- N/A - Not applicable (e.g., no dedicated controller)
- Manual Tx - Explicit JDBC transaction handling

---

## 🎯 Completed implementations

### ✅ Presentation layer (controllers)

Common controller pattern:

```java
public class XxxController extends BaseController {
    private XxxService service;
    private XxxPanel view;

    public XxxController(XxxPanel view) {
        this.view = view;
        this.service = new XxxService();
        initController();
    }

    private void initController() {
        // Register button and table listeners
    }
}
```

Key examples:

- `PacienteController`
  - Uses `PacienteService`.
  - Shows business validations to the user.
- `HabitacionController`
  - Validates existing sector and room data.
- `InternacionController`
  - Validates patient, doctor, and prevents duplicate active internments.
- `GuardiaController`
  - Combines doctor, specialty, and shift in the UI.
- `CamaDisponibleController`
  - Automatically loads bed summary and allows viewing detail.
- `VisitasMedicasController`
  - Searches internments by document type and number and shows comments.
- `AuditoriaGuardiasController`
  - Lists audit records of guard changes.
- `VacacionesController`
  - Coordinates create/update/delete of vacations with detailed feedback.

---

### ✅ Service layer (business logic)

All services follow the shown pattern and handle:

- Validating input data.
- Applying business rules (dates, states, restrictions).
- Encapsulating shared logic (for example, computing internment state).
- Delegating to the corresponding DAO.

Most relevant services:

- `CamaDisponibleService`
  - Exposes methods `getResumen()` and `getDetalle(idSector)`.
  - Does not manage its own transactions because these are read-only queries.

- `VisitasMedicasService`
  - Validates document type and number.
  - Calls `VisitasMedicasDao` to obtain internments and comments.

- `AuditoriaGuardiasService`
  - Validates date range when a filter is used.
  - Straight delegation to `AuditoriaGuardiasDao`.

- `VacacionesService`
  - Orchestrates vacation rules:
    - Valid date range.
    - Deletion and recreation of periods with validation.
  - Uses transactional methods from `VacacionesDaoImpl`.

---

### ✅ Data access layer (DAOs)

Classic CRUD DAOs use manual transaction handling as shown in the diagram. Typical example:

```java
connection = DatabaseConfig.getConnection();
connection.setAutoCommit(false);
// SQL...
connection.commit();
```

On error:

```java
connection.rollback();
```

Always:

```java
connection.close();
```

#### Read-only DAOs with stored procedures

These DAOs do not open explicit transactions; they rely on `CallableStatement` and iterate cursors:

- `CamaDisponibleDaoImpl`
  - `{CALL sp_camas_disponibles_resumen(?)}`
  - `{CALL sp_camas_disponibles_detalle(?, ?)}`

- `VisitasMedicasDaoImpl`
  - `{CALL sp_internaciones_paciente(?, ?, ?)}`
  - `{CALL sp_comentarios_visitas(?, ?)}`

- `AuditoriaGuardiasDaoImpl`
  - `{CALL sp_auditoria_guardias(?, ?, ?, ?)}`

#### DAO with transactional PL/SQL: VacacionesDaoImpl

`VacacionesDaoImpl` includes methods:

- `createWithTransaction(Vacaciones v)`
- `updateWithTransaction(oldV, newV)`

Both build a PL/SQL block that:

- Defines local variables (matricula, fecha_inicio, fecha_fin).
- Executes `SAVEPOINT inicio_transaccion;`.
- Executes `SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;`.
- Locks `GUARDIA` in `SHARE ROW EXCLUSIVE` mode.
- Verifies:
  - Date range.
  - Existing doctor.
  - Vacation overlap.
  - Guard shifts within the period.
- Inserts or updates in `VACACIONES`.
- On exception does `ROLLBACK TO inicio_transaccion;` and propagates the error.

More details in [Transacciones.md](Transacciones.md).

---

## 🔄 Architecture consistency status - V1

Overall status:

- Services created for all modules.
- All controllers use services (no direct DAOs).
- CRUD DAOs use manual transaction handling.
- Report DAOs use read-only stored procedures.
- Vacaciones combines SP and transactional PL/SQL for complex rules.
- Logging and error handling in all layers.

V1 keeps the philosophy:

- Simple, explicit, and educational.
- Easy to follow to learn layered patterns and transactions.

V2 could add:

- Connection pool (HikariCP).
- `TransactionManager` utility to reduce repetitive code.
- Unit and integration tests.
