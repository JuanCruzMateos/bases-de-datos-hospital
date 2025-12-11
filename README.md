# Hospital Management System 🏥
Spanish version available in [README_ES.md](README_ES.md).

**Bases de Datos** | FI UNMdP - Ingeniería en Informática

**Grupo 4:**
- Mateos, Juan Cruz
- San Pedro, Gianfranco

---

## 📋 Project Overview

Enterprise-grade **Hospital Management System** built with **Oracle Database** and **Java Swing**. Features comprehensive patient, doctor, and internment management with advanced reporting capabilities through stored procedures.

**Current Version:** 1.0-SNAPSHOT ✅

### Core Features

**Entity Management (CRUD):**
- 👤 **Pacientes** (Patients) - Full patient registry with medical history
- 👨‍⚕️ **Médicos** (Doctors) - Doctor profiles with specialties and vacation management
- 🛏️ **Internaciones** (Internments) - Complete hospitalization tracking
- 🏥 **Habitaciones & Camas** (Rooms & Beds) - Room and bed allocation
- 🏢 **Sectores** (Sectors) - Hospital sector organization
- 🕐 **Guardias** (Guard Shifts) - Medical guard shift scheduling

**Advanced Features:**
- 📊 **5 Stored Procedures** for complex operations:
  - Available beds query by sector/floor
  - Internment management with automatic bed assignment
  - Guard shift audit reports
  - Medical visit comments tracking
  - Vacation management with conflict validation
- 🔒 **Transaction Management** - ACID compliance with rollback support
- 🛡️ **Triggers** - Data integrity enforcement
- ⚡ **Indexes** - Optimized query performance
- 📝 **Comprehensive Logging** - Detailed operation logs

### Tech Stack

- **Language:** Java 8
- **UI Framework:** Swing (MVC pattern)
- **Database:** Oracle Database Free 23c (Docker)
- **Data Access:** JDBC with manual transaction control
- **Build Tool:** Maven 3.x
- **Architecture:** Layered (Presentation → Service → DAO → Database)

---

## 🚀 Quick Start

### Prerequisites

- **Docker** - For Oracle Database container
- **Java 8+** - JDK installation
- **Maven 3.x** - Build tool

### Launch Application

```bash
# 1. Start Oracle Database container
docker compose up -d

# 2. Wait for database initialization (~30 seconds)
# The database will auto-run all scripts in db_scripts/init/

# 3. Build and launch the application
mvn clean compile package
./launch-ui.sh

# Alternative: Run JAR directly
java -jar target/hospital-1.0-SNAPSHOT.jar
```

### First Time Setup

The database initialization includes:
- ✅ Table creation with PKs and FKs
- ✅ Indexes for performance optimization
- ✅ Triggers for data integrity
- ✅ Sample data for testing
- ✅ All 5 stored procedures

**Database ready when you see:** `DATABASE IS READY TO USE!` in Docker logs

---

## 🗄️ Database

### Entity-Relationship Model

The system implements a comprehensive hospital database with the following main entities:

**Core Entities:**
- **PERSONA** - Base entity for all individuals (inheritance pattern)
  - **PACIENTE** (Patient) - Extends Persona with medical history
  - **MEDICO** (Doctor) - Extends Persona with specialties
- **INTERNACION** (Internment) - Patient hospitalization records
- **HABITACION** (Room) - Hospital rooms organized by sector
- **CAMA** (Bed) - Individual beds within rooms
- **SECTOR** - Hospital departments/sectors
- **GUARDIA** (Guard Shift) - Medical guard assignments
- **ESPECIALIDAD** (Specialty) - Medical specialties
- **VACACIONES** (Vacation) - Doctor vacation periods
- **TURNO** (Shift) - Shift time definitions

**Relationship Tables:**
- **SE_ESPECIALIZA_EN** - Doctor-Specialty (M:N)
- **SE_UBICA** - Bed assignment history

### Stored Procedures

The system implements 5 critical stored procedures:

1. **`sp_camas_disponibles`** - Query available beds
   - Input: Sector ID, Floor number
   - Output: Detailed bed availability with room info

2. **`sp_internaciones`** - Manage internments
   - Operations: CREATE, UPDATE, DELETE
   - Features: Automatic bed assignment, validation

3. **`sp_auditoria_guardias`** - Guard shift audits
   - Input: Doctor document, date range
   - Output: Complete guard history with shift details

4. **`sp_comentarios_visitas`** - Medical visit comments
   - Input: Patient document, internment number
   - Output: Visit history with doctor comments

5. **`sp_vacaciones`** - Vacation management
   - Operations: CREATE, UPDATE, DELETE, READ
   - Features: Conflict detection, guard overlap validation

### Connection Info

**Docker Connection:**
```bash
# Connect via SQLPlus
docker exec -it oracle-hospital sqlplus hospital/hospital123@//localhost:1521/FREEPDB1

# Check container logs
docker logs oracle-hospital
```

**DBeaver/SQL Developer Configuration:**
- **Host:** localhost
- **Port:** 1521
- **Service:** FREEPDB1
- **Username:** hospital
- **Password:** hospital123

**Schema Initialization:** Auto-runs on container start via `db_scripts/init/` folder

---

## 🏛️ Architecture

### Layered Architecture Pattern

The application follows a **strict layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER (UI)                      │
│                     org.hospital.ui.view                        │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │PacientePanel │  │ MedicoPanel  │  │InternacPanel │  ...      │
│  │  (Swing UI)  │  │  (Swing UI)  │  │  (Swing UI)  │           │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘           │
│         │                 │                 │                   │
│  ┌──────▼─────────────────▼─────────────────▼─────────┐         │
│  │            Controllers (MVC Pattern)               │         │
│  │       feature/*/controller/*Controller.java        │         │
│  │  • Handle user actions                             │         │
│  │  • Coordinate between View and Service             │         │
│  │  • Data transformation (View ↔ Domain)             │         │
│  └──────────────────────────┬─────────────────────────┘         │
└─────────────────────────────┼───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                       SERVICE LAYER                             │
│                  feature/*/service/*Service.java                │
│                                                                 │
│  • Business Logic & Validation                                  │
│  • Cross-entity coordination                                    │
│  • Transaction orchestration                                    │
│  • Exception handling and transformation                        │
└─────────────────────────────┼───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                   DATA ACCESS LAYER (DAO)                       │
│               feature/*/repository/*Dao*.java                   │
│                                                                 │
│  • CRUD Operations                                              │
│  • Manual Transaction Management:                               │
│    - conn.setAutoCommit(false)                                  │
│    - execute operations                                         │
│    - conn.commit() or conn.rollback()                           │
│  • PreparedStatements (SQL injection prevention)                │
│  • CallableStatements (Stored Procedures)                       │
└─────────────────────────────┼───────────────────────────────────┘
                              │
                        ┌─────▼──────┐
                        │    JDBC    │
                        │DriverMgr   │
                        │ Connection │
                        │   Pool     │
                        └─────┬──────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                      ORACLE DATABASE                            │
│  Tables │ Stored Procedures │ Triggers │ Indexes │ Constraints  │
└─────────────────────────────────────────────────────────────────┘
```

### Package-by-Feature Organization

The codebase follows **Package-by-Feature** instead of Package-by-Layer, organizing code by business features:

```
feature/{feature_name}/
├── domain/          # Domain entities (POJOs)
├── repository/      # Data access objects (DAOs)
├── service/         # Business logic services
├── controller/      # UI controllers (some features)
└── ui/              # View components (Swing panels)
```

---

## 📁 Project Structure

Complete project organization with **Package-by-Feature** architecture:

```
hospital/
├── 📦 src/main/java/org/hospital/
│   ├── AppUI.java                    # Main entry point (launches HospitalUI)
│   │
│   ├── common/                       # Shared infrastructure
│   │   ├── config/
│   │   │   ├── DatabaseConfig.java   # JDBC connection management
│   │   │   └── LoggerConfig.java     # Centralized logging setup
│   │   ├── controller/
│   │   │   └── BaseController.java   # Common controller utilities
│   │   ├── domain/
│   │   │   └── Persona.java          # Base entity for inheritance
│   │   └── exception/
│   │       └── DataAccessException.java  # Custom exception handling
│   │
│   ├── feature/                      # Business feature modules
│   │   │
│   │   ├── paciente/                 # 👤 Patient Management
│   │   │   ├── domain/
│   │   │   │   └── Paciente.java     # Patient entity
│   │   │   ├── repository/
│   │   │   │   ├── PacienteDao.java          # Interface
│   │   │   │   └── PacienteDaoImpl.java      # JDBC implementation
│   │   │   ├── service/
│   │   │   │   └── PacienteService.java      # Business logic
│   │   │   ├── controller/
│   │   │   │   └── PacienteController.java   # UI controller
│   │   │   └── ui/
│   │   │       └── PacientePanel.java        # Swing view
│   │   │
│   │   ├── medico/                   # 👨‍⚕️ Doctor Management
│   │   │   ├── domain/
│   │   │   │   ├── Medico.java
│   │   │   │   ├── Especialidad.java         # Medical specialty
│   │   │   │   └── Vacaciones.java           # Doctor vacations
│   │   │   ├── repository/
│   │   │   │   ├── MedicoDao.java / MedicoDaoImpl.java
│   │   │   │   ├── EspecialidadDao.java / EspecialidadDaoImpl.java
│   │   │   │   ├── VacacionesDao.java / VacacionesDaoImpl.java
│   │   │   │   └── SeEspecializaEnDao.java   # Doctor-Specialty relation
│   │   │   ├── service/
│   │   │   │   ├── MedicoService.java
│   │   │   │   └── VacacionesService.java
│   │   │   ├── ui/
│   │   │   │   ├── MedicoController.java
│   │   │   │   ├── MedicoPanel.java
│   │   │   │   ├── VacacionesController.java
│   │   │   │   └── VacacionesPanel.java
│   │   │
│   │   ├── internacion/              # 🛏️ Hospitalization Management
│   │   │   ├── domain/
│   │   │   │   ├── Internacion.java          # Patient internment
│   │   │   │   ├── Cama.java                 # Bed
│   │   │   │   ├── Habitacion.java           # Room
│   │   │   │   ├── Sector.java               # Hospital sector
│   │   │   │   ├── SeUbica.java              # Bed assignment history
│   │   │   │   ├── InternacionPaciente.java  # View model
│   │   │   │   ├── CamaDisponibleResumen.java
│   │   │   │   ├── CamaDisponibleDetalle.java
│   │   │   │   ├── ComentarioVisita.java     # Medical visit comments
│   │   │   │   └── AuditoriaGuardia.java     # Guard audit record
│   │   │   ├── repository/
│   │   │   │   ├── InternacionDao.java / InternacionDaoImpl.java
│   │   │   │   ├── CamaDao.java / CamaDaoImpl.java
│   │   │   │   ├── HabitacionDao.java / HabitacionDaoImpl.java
│   │   │   │   ├── SectorDao.java / SectorDaoImpl.java
│   │   │   │   ├── SeUbicaDao.java / SeUbicaDaoImpl.java
│   │   │   │   ├── CamaDisponibleDao.java    # sp_camas_disponibles
│   │   │   │   ├── VisitasMedicasDao.java    # sp_comentarios_visitas
│   │   │   │   └── AuditoriaGuardiasDao.java # sp_auditoria_guardias
│   │   │   ├── service/
│   │   │   │   ├── InternacionService.java
│   │   │   │   ├── CamaService.java
│   │   │   │   ├── HabitacionService.java
│   │   │   │   ├── SectorService.java
│   │   │   │   ├── CamaDisponibleService.java
│   │   │   │   ├── VisitasMedicasService.java
│   │   │   │   └── AuditoriaGuardiasService.java
│   │   │   ├── controller/
│   │   │   │   ├── InternacionController.java
│   │   │   │   ├── HabitacionController.java
│   │   │   │   ├── SectorController.java
│   │   │   │   ├── CamaDisponibleController.java
│   │   │   │   ├── VisitasMedicasController.java
│   │   │   │   └── AuditoriaGuardiasController.java
│   │   │   └── ui/
│   │   │       ├── InternacionPanel.java
│   │   │       ├── HabitacionPanel.java
│   │   │       ├── CamaDisponiblePanel.java
│   │   │       ├── VisitasMedicasPanel.java
│   │   │       └── AuditoriaGuardiasPanel.java
│   │   │
│   │   └── guardia/                  # 🕐 Guard Shift Management
│   │       ├── domain/
│   │       │   ├── Guardia.java              # Guard shift record
│   │       │   └── Turno.java                # Shift time definition
│   │       ├── repository/
│   │       │   ├── GuardiaDao.java / GuardiaDaoImpl.java
│   │       │   └── TurnoDao.java / TurnoDaoImpl.java
│   │       ├── service/
│   │       │   └── GuardiaService.java
│   │       ├── controller/
│   │       │   └── GuardiaController.java
│   │       └── ui/
│   │           └── GuardiaPanel.java
│   │
│   └── ui/                           # Main UI infrastructure
│       ├── HospitalUI.java           # Main application window (JFrame)
│       ├── common/                   # Shared UI components
│       └── view/                     # Centralized view panels
│           ├── PacientePanel.java
│           ├── MedicoPanel.java
│           ├── InternacionPanel.java
│           ├── GuardiaPanel.java
│           ├── HabitacionPanel.java
│           ├── SectorPanel.java
│           ├── CamaDisponiblePanel.java
│           └── VisitasMedicasPanel.java
│
├── 🗄️ db_scripts/
│   ├── init/                         # Auto-executed on container start
│   │   ├── 00-create-user.sql        # Create hospital user
│   │   ├── 01-drop-tables.sql        # Clean slate
│   │   ├── 02-create-tables-pk.sql   # Create tables + PKs
│   │   ├── 03-define-fk-constrains.sql # Add foreign keys
│   │   ├── 04-init-db.sql            # Insert sample data
│   │   ├── 05-triggers.sql           # Data integrity triggers
│   │   ├── 06-indexes.sql            # Performance indexes
│   │   └── 10-rebuild-hospital.sql   # Full rebuild script
│   ├── procedures/                   # Stored procedures
│   │   ├── sp_camas_disponibles.sql
│   │   ├── sp_internaciones.sql
│   │   ├── sp_auditoria_guardias.sql
│   │   ├── sp_comentarios_visitas.sql
│   │   └── sp_vacaciones.sql
│   ├── transactions/                 # Example transaction scripts
│   │   ├── vacaciones.sql
│   │   └── call_sp_vacaciones.sql
│   └── useful.sql                    # Utility queries
│
├── 📝 markdown/                      # Technical documentation
│   ├── ARCHITECTURE.md               # Complete architecture guide
│   ├── Hipotesis y Restricciones.md  # Business rules & constraints
│   ├── Implementacion Stored-Procedures y Triggers.md
│   ├── Stored-Procedures y Triggers.md
│   └── Indices.md                    # Index strategy
│
├── compose.yml                       # Docker Compose for Oracle DB
├── pom.xml                           # Maven dependencies (Java 8)
├── launch-ui.sh                      # Quick launch script
├── modelo-relacional.md              # ER diagram & database design
├── oracle.md                         # Oracle setup notes
└── README.md                         # This file
```

**Key Architectural Decisions:**

1. **Package-by-Feature** - Vertical slices for better cohesion
2. **DAO Pattern** - Abstracts database access with interfaces
3. **Service Layer** - Centralized business logic and validation
4. **MVC Pattern** - Separation of concerns in UI layer
5. **Manual Transaction Management** - Fine-grained control over commits/rollbacks

---

## 📚 Documentation

### Developer Resources

**Architecture & Design:**
- [ARCHITECTURE.md](markdown/ARCHITECTURE.md) - Complete system architecture documentation
- [modelo-relacional.md](modelo-relacional.md) - Entity-Relationship model, hypothesis, and business constraints
- [Hipotesis y Restricciones.md](markdown/Hipotesis%20y%20Restricciones.md) - Detailed business rules

**Database Implementation:**
- [Stored-Procedures y Triggers.md](markdown/Stored-Procedures%20y%20Triggers.md) - SP specifications
- [Implementacion Stored-Procedures y Triggers.md](markdown/Implementacion%20Stored-Procedures%20y%20Triggers.md) - Implementation details
- [Indices.md](markdown/Indices.md) - Index strategy and performance optimization

**Database Scripts:**
- `db_scripts/init/` - Database initialization scripts (auto-run by Docker)
- `db_scripts/procedures/` - All 5 stored procedures source code
- `db_scripts/transactions/` - Example transaction usage

### Key Features Documentation

#### 1. Patient Management (`paciente`)
- Full CRUD operations
- Medical history tracking
- Document-based identification (DNI, LC, LE, CI, PASAPORTE)

#### 2. Doctor Management (`medico`)
- Doctor profiles with multiple specialties
- Vacation management with conflict validation (`sp_vacaciones`)
- Guard shift limits enforcement

#### 3. Internment Management (`internacion`)
- Complete hospitalization lifecycle
- Automatic bed assignment via `sp_internaciones`
- Room and sector organization
- Medical visit tracking with comments (`sp_comentarios_visitas`)

#### 4. Guard Shift Management (`guardia`)
- Shift scheduling by turno (time blocks)
- Audit reports via `sp_auditoria_guardias`
- Vacation conflict prevention
- Maximum shifts per doctor enforcement

#### 5. Bed Availability Queries
- Real-time bed availability via `sp_camas_disponibles`
- Filter by sector and floor
- Detailed room and bed information

### Transaction Management

The application uses **manual transaction control** for data consistency:

```java
Connection conn = DatabaseConfig.getConnection();
try {
    conn.setAutoCommit(false);  // Start transaction
    
    // Execute operations
    // ...
    
    conn.commit();  // Commit if successful
} catch (SQLException e) {
    conn.rollback();  // Rollback on error
    throw new DataAccessException("Operation failed", e);
} finally {
    conn.setAutoCommit(true);
}
```

### Logging

Centralized logging via `LoggerConfig.java`:
- **Location:** `logs/` directory
- **Format:** Timestamped with log levels (INFO, WARNING, SEVERE)
- **Coverage:** Database operations, transactions, errors

---

## 🛠️ Development

### Building the Project

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package JAR with dependencies
mvn clean package

# The output will be in target/hospital-1.0-SNAPSHOT.jar
```

### Running the Application

**Option 1: Using launch script (Recommended)**
```bash
./launch-ui.sh
```

**Option 2: Direct JAR execution**
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar
```

**Option 3: From Maven**
```bash
mvn exec:java -Dexec.mainClass="org.hospital.AppUI"
```

### Database Management

**Start database:**
```bash
docker compose up -d
```

**Stop database:**
```bash
docker compose down
```

**View logs:**
```bash
docker logs -f oracle-hospital
```

**Rebuild database from scratch:**
```bash
docker compose down -v  # Remove volumes
docker compose up -d
# Wait for initialization (~30 seconds)
```

**Connect to database:**
```bash
# SQLPlus
docker exec -it oracle-hospital sqlplus hospital/hospital123@//localhost:1521/FREEPDB1

# Execute rebuild script manually
docker exec -i oracle-hospital sqlplus hospital/hospital123@//localhost:1521/FREEPDB1 < db_scripts/init/10-rebuild-hospital.sql
```

### Project Dependencies

**Runtime Dependencies:**
- `ojdbc8` (23.4.0.24.05) - Oracle JDBC driver

**Build Plugins:**
- `maven-compiler-plugin` (3.12.1) - Java 8 compilation
- `maven-shade-plugin` (3.5.1) - Fat JAR creation with dependencies

**Target Platform:** Java 8 (compatible with Java 8+)

### Code Organization Guidelines

When adding new features, follow the established pattern:

1. **Domain Layer** - Create entity POJOs in `feature/{name}/domain/`
2. **Repository Layer** - Create DAO interface and implementation in `feature/{name}/repository/`
3. **Service Layer** - Add business logic in `feature/{name}/service/`
4. **Controller Layer** - Create controller in `feature/{name}/controller/`
5. **View Layer** - Build Swing UI in `feature/{name}/ui/`

### Common Development Tasks

**Add a new entity:**
1. Create SQL table in `db_scripts/init/02-create-tables-pk.sql`
2. Add foreign keys in `03-define-fk-constrains.sql`
3. Add sample data in `04-init-db.sql`
4. Create domain class extending `Persona` if applicable
5. Implement DAO pattern (interface + impl)
6. Add service layer with validation
7. Build UI components (panel + controller)

**Add a stored procedure:**
1. Create `.sql` file in `db_scripts/procedures/`
2. Add to init script or execute manually
3. Create DAO method using `CallableStatement`
4. Expose through service layer
5. Connect to UI

### Troubleshooting

**Database connection issues:**
- Ensure Docker container is running: `docker ps`
- Check logs: `docker logs oracle-hospital`
- Verify connection string in `DatabaseConfig.java`
- Default: `jdbc:oracle:thin:@localhost:1521:FREEPDB1`

**Build failures:**
- Ensure Java 8+ is installed: `java -version`
- Clean Maven cache: `mvn clean`
- Check Maven version: `mvn -version` (3.x required)

**UI not launching:**
- Verify JAR was built: `ls -lh target/hospital-1.0-SNAPSHOT.jar`
- Check for exceptions in console output
- Ensure database is accessible before starting UI

## 📖 Additional Resources

### Oracle Database

- [Oracle Database Documentation](https://docs.oracle.com/en/database/oracle/oracle-database/)
- [Oracle SQL Language Reference](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/)
- [PL/SQL Language Reference](https://docs.oracle.com/en/database/oracle/oracle-database/19/lnpls/)
- [Oracle Live SQL](https://www.oracle.com/database/technologies/oracle-live-sql/) - Interactive SQL practice

### Java & JDBC

- [JDBC API Documentation](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/)
- [Java Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Maven Documentation](https://maven.apache.org/guides/)

### Project Documentation

All detailed specifications and design documents are available in the `markdown/` folder.

---

## 🎯 Project Goals & Learning Outcomes

This project demonstrates:

✅ **Database Design**
- Entity-Relationship modeling
- Normalization (3NF)
- Complex relationships (1:1, 1:N, N:M)
- Inheritance patterns (Persona → Paciente/Medico)

✅ **SQL Proficiency**
- DDL (Data Definition Language) - Tables, constraints
- DML (Data Manipulation Language) - CRUD operations
- Stored Procedures with complex logic
- Triggers for data integrity
- Indexes for performance optimization

✅ **Application Architecture**
- Layered architecture (Presentation → Service → DAO → Database)
- Package-by-Feature organization
- DAO pattern for data access abstraction
- MVC pattern in UI layer
- Transaction management

✅ **Software Engineering Practices**
- Clean code organization
- Separation of concerns
- Exception handling
- Logging and debugging
- Version control (Git)

✅ **Enterprise Technologies**
- JDBC for database connectivity
- Connection management
- PreparedStatements (SQL injection prevention)
- CallableStatements (stored procedure invocation)
- Manual transaction control

---

## 📄 License

**Academic Project** - Universidad Nacional de Mar del Plata  
Facultad de Ingeniería - Ingeniería en Informática  
Course: Bases de Datos

---

## 👥 Team

**Grupo 4:**
- **Mateos, Juan Cruz**
- **San Pedro, Gianfranco**

*Spring 2025 Semester*

---
