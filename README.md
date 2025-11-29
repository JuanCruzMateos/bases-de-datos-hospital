# Hospital Management System 🏥

**Bases de Datos** | FI UNMdP - Ingeniería en Informática

**Grupo 4:**
- Bonifazi, Paula
- Mateos, Juan Cruz
- Navarro, Pablo
- Parise, Thiago
- San Pedro, Gianfranco

---

## 📋 Project Status

Full-stack hospital management system with **Oracle Database**, **Java Swing UI**, and **layered architecture**.

**Current Version:** 1.0-SNAPSHOT ✅

**Core Features:**
- Complete CRUD operations for all entities (Patients, Doctors, Internments, Guards, Rooms, Sectors)
- 5 stored procedures: available beds, internments, guard audits, visit comments, vacation management
- Triggers and indexes for data integrity and performance
- Transaction management with rollback support
- Comprehensive logging system

**Tech Stack:**
- Java 8 + Swing (MVC pattern)
- Oracle Database Free (Docker container)
- JDBC + manual transaction control
- Maven build system

---

## 🚀 Quick Start

```bash
# Start database
docker compose up -d

# Launch application
mvn clean package
./launch-ui.sh
```

---

## 🗄️ Database

### Database Schema (Simplified)

```
    ┌─────────────┐
    │   PERSONA   │
    │─────────────│
    │ nro_doc (PK)│
    │ nombre      │
    │ apellido    │
    │ ...         │
    └──────┬──────┘
           │
           ├─────────────────┐
           │                 │
    ┌──────▼──────┐   ┌──────▼──────┐
    │  PACIENTE   │   │   MEDICO    │
    │─────────────│   │─────────────│
    │ nro_doc (PK)│   │ nro_doc (PK)│
    │ nro_hist_cl │   │ matricula   │
    │ edad        │   │ max_guardias│
    └──────┬──────┘   └──────┬──────┘
           │                 │
           │                 │ ┌──────────────┐
           │                 ├─┤ ESPECIALIDAD │
           │                 │ │──────────────│
           │                 │ │ id_esp  (PK) │
           │                 │ │ descripcion  │
           │                 │ └──────────────┘
           │                 │
           │         ┌───────▼─────────┐
           │         │   VACACIONES    │
           │         │─────────────────│
           │         │ id_vacaciones   │
           │         │ desde / hasta   │
           │         │ nro_doc_medico  │
           │         └─────────────────┘
           │
    ┌──────▼──────────┐        ┌─────────────┐
    │  INTERNACION    │◄───────┤    CAMA     │
    │─────────────────│        │─────────────│
    │ id_intern  (PK) │        │ id_cama (PK)│
    │ fecha_ingreso   │        │ nro_habitac │
    │ fecha_egreso    │        │ disponible  │
    │ nro_doc_pacient │        └──────┬──────┘
    │ nro_doc_medico  │               │
    └─────────────────┘               │
                                ┌─────▼──────┐
    ┌─────────────┐             │ HABITACION │
    │   GUARDIA   │             │────────────│
    │─────────────│             │ nro_habitac│
    │ id_guardia  │             │ id_sector  │
    │ fecha_desde │             │ piso       │
    │ fecha_hasta │             │ orientacion│
    │ nro_doc_med │             └──────┬─────┘
    │ id_turno    │                    │
    └─────────────┘              ┌─────▼─────┐
                                 │  SECTOR   │
                                 │───────────│
                                 │ id_sector │
                                 │ descripcion│
                                 └───────────┘

    Stored Procedures:
    ├─ sp_camas_disponibles      (Available beds by sector/floor)
    ├─ sp_internaciones          (Internment management)
    ├─ sp_auditoria_guardias     (Guard shift audits)
    ├─ sp_comentarios_visitas    (Medical visit comments)
    └─ sp_vacaciones             (Vacation management + validation)
```

### Connection Info

**Connection (Docker):**
```bash
docker exec -it oracle-hospital sqlplus hospital/hospital123@//localhost:1521/FREEPDB1
```

**Schema Initialization:** Auto-runs on container start (`db_scripts/init/`)
- Tables with PKs and FKs
- Indexes for performance
- Triggers for data integrity
- Initial sample data
- 5 stored procedures

**DBeaver Config:** `localhost:1521/FREEPDB1` · User: `hospital` · Pass: `hospital123`

---

## 🏛️ Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                         │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │  Swing View  │  │  Swing View  │  │  Swing View  │  ...   │
│  │   (Panel)    │  │   (Panel)    │  │   (Panel)    │        │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘        │
│         │                  │                  │                 │
│  ┌──────▼──────────────────▼──────────────────▼───────┐       │
│  │              Controllers (MVC)                      │       │
│  │       feature/*/ui/*Controller.java                 │       │
│  └──────────────────────────┬──────────────────────────┘       │
└─────────────────────────────┼───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                      SERVICE LAYER                              │
│                   feature/*/service/*Service.java               │
│                                                                 │
│  • Business Logic & Validation                                  │
│  • Cross-entity validation                                      │
│  • Transaction coordination                                     │
└─────────────────────────────┼───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                    DATA ACCESS LAYER (DAO)                      │
│                  feature/*/repository/*Dao*.java                │
│                                                                 │
│  • CRUD Operations + Manual Transactions                        │
│  • PreparedStatements (SQL injection safe)                      │
│  • setAutoCommit(false) → execute → commit/rollback             │
└─────────────────────────────┼───────────────────────────────────┘
                              │
                        ┌─────▼──────┐
                        │    JDBC    │
                        │ DriverMgr  │
                        └─────┬──────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                      ORACLE DATABASE                            │
│  Tables │ Stored Procedures │ Triggers │ Indexes │ Constraints │
└─────────────────────────────────────────────────────────────────┘
```

### Feature Module Pattern

Each feature (`paciente`, `medico`, `internacion`, `guardia`) follows:

```
feature/{name}/
├── domain/          # Business entities
├── repository/      # Data access (DAO)
├── service/         # Business logic
└── ui/              # Controllers & Views
```

---

## 📁 Project Structure

**Package-by-Feature Architecture** - Production-ready organization:

```
hospital/
├── 📦 src/main/java/org/hospital/
│   ├── AppUI.java                    # Main entry point
│   │
│   ├── common/                       # Shared components
│   │   ├── config/
│   │   │   ├── DatabaseConfig.java   # JDBC connection manager
│   │   │   └── LoggerConfig.java     # Logging configuration
│   │   ├── exception/
│   │   │   └── DataAccessException.java
│   │   └── domain/
│   │       └── Persona.java          # Base entity
│   │
│   ├── feature/                      # Feature modules
│   │   ├── paciente/                 # Patient management
│   │   │   ├── domain/
│   │   │   │   └── Paciente.java
│   │   │   ├── repository/
│   │   │   │   ├── PacienteDao.java
│   │   │   │   └── PacienteDaoImpl.java
│   │   │   ├── service/
│   │   │   │   └── PacienteService.java
│   │   │   └── ui/
│   │   │       ├── PacienteController.java
│   │   │       └── PacientePanel.java
│   │   │
│   │   ├── medico/                   # Doctor management
│   │   │   ├── domain/
│   │   │   │   ├── Medico.java
│   │   │   │   ├── Especialidad.java
│   │   │   │   └── Vacaciones.java
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── ui/
│   │   │
│   │   ├── internacion/              # Hospitalization
│   │   │   ├── domain/
│   │   │   │   ├── Internacion.java
│   │   │   │   ├── Cama.java
│   │   │   │   ├── Habitacion.java
│   │   │   │   └── Sector.java
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── ui/
│   │   │
│   │   └── guardia/                  # Guard shifts
│   │       ├── domain/
│   │       │   ├── Guardia.java
│   │       │   └── Turno.java
│   │       ├── repository/
│   │       ├── service/
│   │       └── ui/
│   │
│   └── ui/                           # Main UI components
│       ├── HospitalUI.java           # Main window
│       └── common/
│           └── BaseController.java
│
├── 🗄️ db_scripts/
│   ├── init/                         # Auto-run on Docker start
│   │   ├── 02-create-tables-pk.sql
│   │   ├── 04-init-db.sql           # Sample data
│   │   ├── 05-triggers.sql
│   │   └── 06-indexes.sql
│   └── procedures/                   # Stored Procedures
│       ├── sp_camas_disponibles.sql
│       ├── sp_internaciones.sql
│       ├── sp_auditoria_guardias.sql
│       ├── sp_comentarios_visitas.sql
│       └── sp_vacaciones.sql
│
├── compose.yml                       # Docker Oracle setup
├── pom.xml                           # Maven config (Java 8)
└── launch-ui.sh                      # Quick start script
```

**Architecture Benefits:**
- ✅ **Feature cohesion** - All code for a feature in one place
- ✅ **Clear boundaries** - Easy to understand and navigate
- ✅ **Scalability** - Can evolve features independently
- ✅ **Production-ready** - Industry standard pattern

---

## 📚 Documentation

**Developer Guides:**
- [ARCHITECTURE.md](markdown/ARCHITECTURE.md) - Complete architecture overview
- [modelo-relacional.md](modelo-relacional.md) - Database schema and ER model
- [Stored-Procedures y Triggers.md](markdown/Stored-Procedures%20y%20Triggers.md) - SP implementation
- [Indices.md](markdown/Indices.md) - Index strategy

---

## 📖 Resources

**Oracle:** [SQL Reference](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/) · [Live SQL](https://www.oracle.com/database/technologies/oracle-live-sql/)

**Project Docs:** See `markdown/` folder for detailed specifications

---

## 📄 License

Academic project - Universidad Nacional de Mar del Plata, Facultad de Ingeniería
