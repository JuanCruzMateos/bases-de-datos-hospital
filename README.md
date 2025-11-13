# Hospital Management System 🏥

**Bases de Datos** | FI UNMdP - Ingeniería en Informática

**Grupo 4:**
- Bonifazi, Paula
- Mateos, Juan Cruz
- Navarro, Pablo
- Parise, Thiago
- San Pedro, Gianfranco

---

## 🚀 Quick Start

This project provides **two complete interfaces** for managing hospital data:

### 1. Graphical UI (Swing) - ⭐ **Recommended**
```bash
./launch-ui.sh
```

### 2. Command Line Interface (CLI)
```bash
mvn clean package
java -jar target/hospital-1.0-SNAPSHOT.jar list-pacientes
```

---

## ✨ Features

- ✅ **Full CRUD Operations** - Create, Read, Update, Delete
- ✅ **Two Interfaces** - Graphical UI (Swing) + Command Line (CLI)
- ✅ **MVC Pattern** - Clean separation of concerns in UI
- ✅ **DAO Pattern** - Database abstraction layer
- ✅ **6 Entities** - Paciente, Medico, Sector, Habitacion, Internacion, Guardia
- ✅ **31 CLI Commands** - Comprehensive command-line interface
- ✅ **4 UI Panels** - User-friendly graphical interface
- ✅ **Transaction Management** - ACID compliance
- ✅ **Error Handling** - Robust and user-friendly
- ✅ **Complete Documentation** - 9 documentation files

---

## 📚 Documentation

### For Users
- **[SWING_UI_QUICK_START.md](SWING_UI_QUICK_START.md)** - Get started with the UI in 2 minutes ⚡
- **[QUICK_START.md](QUICK_START.md)** - CLI quick reference guide
- **[PROJECT_COMPLETE.md](PROJECT_COMPLETE.md)** - Full project overview

### For Developers
- **[UI_DOCUMENTATION.md](UI_DOCUMENTATION.md)** - Complete Swing UI guide (MVC pattern)
- **[CRUD_DOCUMENTATION.md](CRUD_DOCUMENTATION.md)** - Complete API and CLI documentation
- **[modelo-relacional.md](modelo-relacional.md)** - Database schema and relational model
- **[SWING_UI_SUMMARY.md](SWING_UI_SUMMARY.md)** - UI implementation summary
- **[CRUD_SUMMARY.md](CRUD_SUMMARY.md)** - DAO implementation summary

---

## 🏗️ Project Structure

```
hospital/
├── src/main/java/org/hospital/
│   ├── ui/                      # Swing UI (MVC Pattern) ✨ NEW!
│   │   ├── controller/          # Controllers (Business logic)
│   │   │   ├── BaseController.java
│   │   │   ├── PacienteController.java
│   │   │   ├── SectorController.java
│   │   │   ├── HabitacionController.java
│   │   │   └── InternacionController.java
│   │   ├── view/                # Views (UI components)
│   │   │   ├── PacientePanel.java
│   │   │   ├── SectorPanel.java
│   │   │   ├── HabitacionPanel.java
│   │   │   └── InternacionPanel.java
│   │   └── HospitalUI.java      # Main UI window
│   ├── paciente/                # Patient entity
│   ├── medico/                  # Doctor entity
│   ├── internacion/             # Hospitalization entities
│   ├── guardia/                 # Duty entities
│   ├── persona/                 # Person base entity
│   ├── config/                  # Configuration
│   ├── exception/               # Custom exceptions
│   ├── App.java                 # CLI application
│   └── AppUI.java               # UI launcher ✨ NEW!
├── db_scripts/
│   ├── init/                    # Database initialization scripts
│   └── procedures/              # Stored procedures
├── docs/                        # Additional documentation
└── *.md                         # README and guides
```

---

## 📊 Statistics

- **38 Java classes** compiled successfully
- **10 UI classes** implementing MVC pattern
- **6 entities** with full CRUD operations
- **31 CLI commands** available
- **4 UI panels** (Swing graphical interface)
- **9 documentation files**
- **~2,050 lines** of UI code
- **100% compilation success** ✅

---

## 🗄️ Database Setup

### Using Docker (Recommended)

```bash
# Start Oracle database container
docker compose up -d

# Connect to database
docker exec -it oracle-hospital sqlplus hospital/hospital123@//localhost:1521/FREEPDB1
```

### Initialize Database

Run the initialization scripts in order:

```bash
cd db_scripts/init
# 1. Create user
sqlplus sys/password@//localhost:1521/FREEPDB1 as sysdba @00-create-user.sql
# 2. Drop existing tables
sqlplus hospital/hospital123@//localhost:1521/FREEPDB1 @01-drop-tables.sql
# 3. Create tables
sqlplus hospital/hospital123@//localhost:1521/FREEPDB1 @02-create-tables-pk.sql
# 4. Define constraints
sqlplus hospital/hospital123@//localhost:1521/FREEPDB1 @03-define-fk-constrains.sql
# 5. Initialize data
sqlplus hospital/hospital123@//localhost:1521/FREEPDB1 @04-init-db.sql
```

### Database Connection (DBeaver)
- **User:** hospital
- **Password:** hospital123
- **Service Name:** FREEPDB1
- **Host:** localhost
- **Port:** 1521

---

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
db.url=jdbc:oracle:thin:@localhost:1521/FREEPDB1
db.user=hospital
db.password=hospital123
```

---

## 🎯 Usage Examples

### Swing UI

**Launch:**
```bash
./launch-ui.sh
```

**Features:**
- Click tabs to switch entities (Pacientes, Sectores, Habitaciones, Internaciones)
- Fill form and click "Create" to add records
- Click table row to load into form
- Click "Update" to modify selected record
- Click "Delete" to remove selected record
- Click "Refresh" to reload data

### CLI

**List all patients:**
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar list-pacientes
```

**Create a patient:**
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar create-paciente DNI 12345678 Juan Perez PACIENTE 1990-01-15 M
```

**Get specific patient:**
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar get-paciente DNI 12345678
```

**List active hospitalizations:**
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar list-internaciones-activas
```

**Show all commands:**
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar
```

---

## 🎨 Swing UI Screenshots

The UI provides a professional, user-friendly interface with:
- **Tabbed navigation** for different entities
- **Form-based input** with validation
- **Data tables** with sorting and selection
- **Dialog boxes** for feedback
- **Color-coded header** and clean design

---

## 🏛️ Architecture

### MVC Pattern (Swing UI)
```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│     View     │────▶│  Controller  │────▶│    Model     │
│   (Panel)    │◀────│   (Logic)    │◀────│   (DAO)      │
└──────────────┘     └──────────────┘     └──────────────┘
```

### DAO Pattern (Data Access)
```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Controller  │────▶│   DAO Impl   │────▶│   Database   │
│  or CLI      │◀────│  (CRUD ops)  │◀────│   (Oracle)   │
└──────────────┘     └──────────────┘     └──────────────┘
```

---

## 🧪 Testing

### Manual Testing
```bash
# Build
mvn clean package

# Test UI
./launch-ui.sh

# Test CLI
./examples.sh
```

---

## 📖 Additional Resources

### Oracle Documentation
- [SQL Reference](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/toc.htm)
- [Live SQL](https://www.oracle.com/database/technologies/oracle-live-sql/)
- [Free SQL](https://freesql.com/)
- [Container Registry](https://container-registry.oracle.com/ords/f?p=113:4:8843924309712:::4:P4_REPOSITORY,AI_REPOSITORY,AI_REPOSITORY_NAME,P4_REPOSITORY_NAME,P4_EULA_ID,P4_BUSINESS_AREA_ID:1863,1863,Oracle%20Database%20Free,Oracle%20Database%20Free,1,0&cs=3eoUjM-yDyac21yxmpGLVQVShx4ETDBJX8IZOB3uDaxo6UzmLf0zlojb_f0KK67YrnUASCWGldHZ_ntvGjKKpYA)

### Project Documentation
- See [modelo-relacional.md](modelo-relacional.md) for complete database schema
- See [UI_DOCUMENTATION.md](UI_DOCUMENTATION.md) for MVC pattern details
- See [CRUD_DOCUMENTATION.md](CRUD_DOCUMENTATION.md) for API documentation

---

## 🤝 Contributing

This project was developed as part of the Bases de Datos course at FI UNMdP.

**Grupo 4 Members:**
- Bonifazi, Paula
- Mateos, Juan Cruz
- Navarro, Pablo
- Parise, Thiago
- San Pedro, Gianfranco

---

## 📄 License

Academic project for educational purposes.  
Universidad Nacional de Mar del Plata - Facultad de Ingeniería

---

## ✅ Project Status

**COMPLETE** ✅

- ✅ Database schema design and implementation
- ✅ Full CRUD operations for 6 entities
- ✅ CLI interface with 31 commands
- ✅ Swing UI with MVC pattern (4 panels)
- ✅ DAO pattern implementation
- ✅ Transaction management
- ✅ Error handling
- ✅ Complete documentation
- ✅ Launch scripts
- ✅ Example data and scripts

**Ready for use!** 🚀
