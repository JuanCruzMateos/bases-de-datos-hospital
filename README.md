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

Launch the Hospital Management System UI:

```bash
./launch-ui.sh
```

Or run the JAR directly:
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar
```

---

## ✨ Features

- ✅ **Full CRUD Operations** - Create, Read, Update, Delete via graphical interface
- ✅ **Swing UI** - Professional graphical interface with MVC pattern
- ✅ **MVC Pattern** - Clean separation of concerns in UI
- ✅ **DAO Pattern** - Database abstraction layer
- ✅ **6 Entities** - Paciente, Medico, Sector, Habitacion, Internacion, Guardia
- ✅ **8 UI Tabs** - 6 CRUD tabs + 2 Report tabs
- ✅ **Stored Procedures** - 4 stored procedures integrated with UI ⭐
- ✅ **Report Tabs** - Camas Disponibles & Visitas Médicas ⭐
- ✅ **Transaction Management** - ACID compliance
- ✅ **Error Handling** - Robust and user-friendly
- ✅ **Complete Documentation** - 10 documentation files

---

## 📚 Documentation

### For Users
- **[SWING_UI_QUICK_START.md](SWING_UI_QUICK_START.md)** - Get started with the UI in 2 minutes ⚡
- **[PROJECT_COMPLETE.md](PROJECT_COMPLETE.md)** - Full project overview

### For Developers
- **[UI_DOCUMENTATION.md](UI_DOCUMENTATION.md)** - Complete Swing UI guide (MVC pattern)
- **[STORED_PROCEDURES_IMPLEMENTATION.md](STORED_PROCEDURES_IMPLEMENTATION.md)** - Stored procedures integration guide ⭐
- **[CRUD_DOCUMENTATION.md](CRUD_DOCUMENTATION.md)** - Complete DAO and data access documentation
- **[modelo-relacional.md](modelo-relacional.md)** - Database schema and relational model
- **[SWING_UI_SUMMARY.md](SWING_UI_SUMMARY.md)** - UI implementation summary
- **[CRUD_SUMMARY.md](CRUD_SUMMARY.md)** - DAO implementation summary

---

## 🏗️ Project Structure

```
hospital/
├── src/main/java/org/hospital/
│   ├── ui/                      # Swing UI (MVC Pattern)
│   │   ├── controller/          # Controllers (Business logic)
│   │   │   ├── BaseController.java
│   │   │   ├── PacienteController.java
│   │   │   ├── SectorController.java
│   │   │   ├── HabitacionController.java
│   │   │   ├── InternacionController.java
│   │   │   ├── GuardiaController.java
│   │   │   ├── CamaDisponibleController.java   # ⭐ NEW!
│   │   │   └── VisitasMedicasController.java   # ⭐ NEW!
│   │   ├── view/                # Views (UI components)
│   │   │   ├── PacientePanel.java
│   │   │   ├── MedicoPanel.java
│   │   │   ├── SectorPanel.java
│   │   │   ├── HabitacionPanel.java
│   │   │   ├── InternacionPanel.java
│   │   │   ├── GuardiaPanel.java
│   │   │   ├── CamaDisponiblePanel.java        # ⭐ NEW!
│   │   │   └── VisitasMedicasPanel.java        # ⭐ NEW!
│   │   └── HospitalUI.java      # Main UI window
│   ├── paciente/                # Patient entity
│   ├── medico/                  # Doctor entity
│   ├── internacion/             # Hospitalization entities
│   │                            # + Stored procedures DTOs ⭐ NEW!
│   ├── guardia/                 # Duty entities
│   ├── persona/                 # Person base entity
│   ├── config/                  # Configuration
│   ├── exception/               # Custom exceptions
│   └── AppUI.java               # Application launcher
├── db_scripts/
│   ├── init/                    # Database initialization scripts
│   └── procedures/              # Stored procedures
├── docs/                        # Additional documentation
└── *.md                         # README and guides
```

---

## 📊 Statistics

- **72 Java classes** compiled successfully
- **16 UI classes** implementing MVC pattern
- **6 entities** with full CRUD operations
- **8 UI tabs** (6 CRUD + 2 Reports)
- **4 stored procedures** integrated with UI ⭐
- **10 documentation files**
- **~3,500 lines** of UI code
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

## 🎯 Usage Guide

### Launching the Application

**Using the launch script:**
```bash
./launch-ui.sh
```

**Or run the JAR directly:**
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar
```

### Using the Interface

**Navigation:**
- Click tabs to switch between different sections:
  - **CRUD Tabs**: Pacientes, Medicos, Sectores, Habitaciones, Internaciones, Guardias
  - **Report Tabs**: Camas Disponibles, Visitas Médicas ⭐

**CRUD Operations:**
- **Create**: Fill the form and click "Create" to add new records
- **Read**: Browse data in the table, click "Refresh" to reload
- **Update**: Click a table row to load it into the form, modify, and click "Update"
- **Delete**: Select a row and click "Delete" to remove the record
- **Clear**: Click "Clear" to reset the form

**Report Features:**
- **Camas Disponibles**: 
  - View summary of available beds by sector
  - Click on a sector to see detailed room and bed information
- **Visitas Médicas**: 
  - Search patient internations by document type and number
  - Click on an internation to view medical visit comments

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

### Building and Running
```bash
# Build the project
mvn clean package

# Launch the UI
./launch-ui.sh

# Or run directly
java -jar target/hospital-1.0-SNAPSHOT.jar
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
- ✅ Full CRUD operations for 6 entities via UI
- ✅ Swing UI with MVC pattern (8 tabs)
- ✅ DAO pattern implementation
- ✅ Stored procedures integration (4 procedures) ⭐
- ✅ Report tabs (Camas Disponibles & Visitas Médicas) ⭐
- ✅ Transaction management
- ✅ Error handling and validation
- ✅ Complete documentation
- ✅ Launch scripts
- ✅ Example database data

**Ready for use!** 🚀
