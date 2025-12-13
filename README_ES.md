# Sistema de Gestión Hospitalaria 🏥
Versión en inglés disponible en [README.md](README.md).

---

## Indice
- [Descripción del Proyecto](#descripcion-del-proyecto)
- [Inicio Rápido](#inicio-rapido)
- [Base de Datos](#base-de-datos)
- [Arquitectura](#arquitectura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Documentación](#documentacion)
- [Desarrollo](#desarrollo)
- [Recursos Adicionales](#recursos-adicionales)
- [Objetivos del Proyecto y Resultados de Aprendizaje](#objetivos-del-proyecto-y-resultados-de-aprendizaje)
- [Licencia](#licencia)
- [Equipo](#equipo)

<a id="descripcion-del-proyecto"></a>
## 📋 Descripción del Proyecto

Sistema de Gestión Hospitalaria de nivel empresarial construido con **Oracle Database** y **Java Swing**. Ofrece gestión integral de pacientes, médicos e internaciones con capacidades avanzadas de reportes mediante stored procedures.

**Versión Actual:** 1.0-SNAPSHOT ✅

### Funcionalidades Principales

**Gestión de Entidades (CRUD):**
- 👤 **Pacientes** (Pacientes) - Registro completo de pacientes con historial médico
- 🧑‍⚕️ **Médicos** (Médicos) - Perfiles de médicos con especialidades y gestión de vacaciones
- 🛏️ **Internaciones** (Internaciones) - Seguimiento completo de hospitalizaciones
- 🏥 **Habitaciones & Camas** (Habitaciones & Camas) - Asignación de habitaciones y camas
- 🏢 **Sectores** (Sectores) - Organización de sectores del hospital
- 🕐 **Guardias** (Turnos de Guardia) - Planificación de guardias médicas

**Funcionalidades Avanzadas:**
- 📊 **5 Stored Procedures** para operaciones complejas:
  - Consulta de camas disponibles por sector/piso
  - Gestión de internaciones con asignación automática de cama
  - Reportes de auditoría de guardias
  - Seguimiento de comentarios de visitas médicas
  - Gestión de vacaciones con validación de conflictos
- 🔒 **Gestión de Transacciones** - Cumplimiento ACID con soporte de rollback
- 🛡️ **Triggers** - Aplicación de integridad de datos
- ⚡ **Índices** - Rendimiento de consultas optimizado
- 📝 **Registro Exhaustivo** - Logs detallados de operaciones

### Stack Tecnológico

- **Lenguaje:** Java 8
- **Framework UI:** Swing (patrón MVC)
- **Base de datos:** Oracle Database Free 23c (Docker)
- **Acceso a datos:** JDBC con control manual de transacciones
- **Herramienta de build:** Maven 3.x
- **Arquitectura:** por capas (Presentación → Servicio → DAO → Base de datos)

---

<a id="inicio-rapido"></a>
## 🚀 Inicio Rápido

### Requisitos Previos

- **Docker** - Para el contenedor de Oracle Database
- **Java 8+** - Instalación JDK
- **Maven 3.x** - Herramienta de build

### Lanzar la Aplicación

```bash
# 1. Iniciar contenedor de Oracle Database
docker compose up -d

# 2. Esperar la inicialización de la base (~30 segundos)
# La base ejecutará automáticamente todos los scripts en db_scripts/init/

# 3. Compilar y lanzar la aplicación
mvn clean compile package
./launch-ui.sh

# Alternativa: Ejecutar JAR directamente
java -jar target/hospital-1.0-SNAPSHOT.jar
```

### Configuración Inicial

La inicialización de la base de datos incluye:
- ✅ Creación de tablas con PK y FK
- ✅ Índices para optimización de performance
- ✅ Triggers para integridad de datos
- ✅ Datos de muestra para testing
- ✅ Los 5 stored procedures

**Base lista cuando veas:** `DATABASE IS READY TO USE!` en los logs de Docker

---

<a id="base-de-datos"></a>
## 🗄️ Base de Datos

### Modelo Entidad-Relación

El sistema implementa una base de datos hospitalaria integral con las siguientes entidades principales:

**Entidades Principales:**
- **PERSONA** - Entidad base para todos los individuos (patrón de herencia)
  - **PACIENTE** (Paciente) - Extiende Persona con historial médico
  - **MEDICO** (Médico) - Extiende Persona con especialidades
- **INTERNACION** (Internación) - Registros de hospitalización de pacientes
- **HABITACION** (Habitación) - Habitaciones organizadas por sector
- **CAMA** (Cama) - Camas individuales dentro de las habitaciones
- **SECTOR** - Sectores/departamentos del hospital
- **GUARDIA** (Guardia) - Asignaciones de guardias médicas
- **ESPECIALIDAD** (Especialidad) - Especialidades médicas
- **VACACIONES** (Vacaciones) - Períodos de vacaciones de médicos
- **TURNO** (Turno) - Definiciones de franjas horarias

**Tablas de Relación:**
- **SE_ESPECIALIZA_EN** - Relación Médico-Especialidad (M:N)
- **SE_UBICA** - Historial de asignación de camas

### Stored Procedures

El sistema implementa 5 stored procedures críticos:

1. **`sp_camas_disponibles`** - Consulta camas disponibles
   - Input: ID de Sector, número de Piso
   - Output: Disponibilidad de camas detallada con información de habitación

2. **`sp_internaciones`** - Gestionar internaciones
   - Operaciones: CREATE, UPDATE, DELETE
   - Funcionalidades: Asignación automática de cama, validación

3. **`sp_auditoria_guardias`** - Auditorías de guardias
   - Input: Documento de médico, rango de fechas
   - Output: Historial completo de guardias con detalles de turnos

4. **`sp_comentarios_visitas`** - Comentarios de visitas médicas
   - Input: Documento de paciente, número de internación
   - Output: Historial de visitas con comentarios de médicos

5. **`sp_vacaciones`** - Gestión de vacaciones
   - Operaciones: CREATE, UPDATE, DELETE, READ
   - Funcionalidades: Detección de conflictos, validación de superposición con guardias

### Información de Conexión

**Conexión Docker:**
```bash
# Conectar vía SQLPlus
docker exec -it oracle-hospital sqlplus hospital/hospital123@//localhost:1521/FREEPDB1

# Ver logs del contenedor
docker logs oracle-hospital
```

**Configuración DBeaver/SQL Developer:**
- **Host:** localhost
- **Port:** 1521
- **Service:** FREEPDB1
- **Username:** hospital
- **Password:** hospital123

**Inicialización de esquema:** Se ejecuta automáticamente al iniciar el contenedor mediante la carpeta `db_scripts/init/`

---

<a id="arquitectura"></a>
## 🏛️ Arquitectura

### Patrón de Arquitectura en Capas

La aplicación sigue una **arquitectura estrictamente en capas** con clara separación de responsabilidades:

```
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN (UI)                    │
│                     org.hospital.ui.view                        │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │PacientePanel │  │ MedicoPanel  │  │InternacPanel │  ...      │
│  │  (Swing UI)  │  │  (Swing UI)  │  │  (Swing UI)  │           │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘           │
│         │                 │                 │                   │
│  ┌──────▼─────────────────▼─────────────────▼─────────┐         │
│  │          Controladores (patrón MVC)                │         │
│  │     feature/*/controller/*Controller.java          │         │
│  │  • Manejar acciones de usuario                     │         │
│  │  • Coordinar entre Vista y Servicio                │         │
│  │  • Transformación de datos (Vista ↔ Dominio)       │         │
│  └──────────────────────────┬─────────────────────────┘         │
└─────────────────────────────┼───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                       CAPA DE SERVICIO                          │
│                 feature/*/service/*Service.java                 │
│                                                                 │
│  • Lógica de negocio y validación                               │
│  • Coordinación entre entidades                                 │
│  • Orquestación de transacciones                                │
│  • Manejo y transformación de excepciones                       │
└─────────────────────────────┼───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                CAPA DE ACCESO A DATOS (DAO)                     │
│              feature/*/repository/*Dao*.java                    │
│                                                                 │
│  • Operaciones CRUD                                             │
│  • Gestión manual de transacciones:                             │
│    - conn.setAutoCommit(false)                                  │
│    - execute operations                                         │
│    - conn.commit() or conn.rollback()                           │
│  • PreparedStatements (prevención de SQL injection)             │
│  • CallableStatements (Stored Procedures)                       │
└─────────────────────────────┼───────────────────────────────────┘
                              │
                         ┌────▼─────┐
                         │   JDBC   │
                         │DriverMgr │
                         │Connection│
                         │   Pool   │
                         └────┬─────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                     ORACLE DATABASE                             │
│ Tables │ Stored Procedures │ Triggers │ Indexes │ Constraints   │
└─────────────────────────────────────────────────────────────────┘
```

### Organización Package-by-Feature

El código sigue **Package-by-Feature** en lugar de Package-by-Layer, organizando el código por funcionalidades de negocio:

```
feature/{feature_name}/
├── domain/          # Entidades de dominio (POJOs)
├── repository/      # Objetos de acceso a datos (DAOs)
├── service/         # Servicios de lógica de negocio
├── controller/      # Controladores de UI (algunas funcionalidades)
└── ui/              # Componentes de vista (paneles Swing)
```

---

<a id="estructura-del-proyecto"></a>
## 📁 Estructura del Proyecto

Organización completa del proyecto con arquitectura **Package-by-Feature**:

```
hospital/
├── 📦 src/main/java/org/hospital/
│   ├── AppUI.java                    # Punto de entrada principal (lanza HospitalUI)
│   │
│   ├── common/                       # Infraestructura compartida
│   │   ├── config/
│   │   │   ├── DatabaseConfig.java   # Gestión de conexión JDBC
│   │   │   └── LoggerConfig.java     # Configuración centralizada de logging
│   │   ├── controller/
│   │   │   └── BaseController.java   # Utilidades comunes de controladores
│   │   ├── domain/
│   │   │   └── Persona.java          # Entidad base para herencia
│   │   └── exception/
│   │       └── DataAccessException.java  # Manejo de excepciones custom
│   │
│   ├── feature/                      # Módulos de funcionalidades de negocio
│   │   │
│   │   ├── paciente/                 # 👤 Gestión de Pacientes
│   │   │   ├── domain/
│   │   │   │   └── Paciente.java     # Entidad de paciente
│   │   │   ├── repository/
│   │   │   │   ├── PacienteDao.java          # Interfaz
│   │   │   │   └── PacienteDaoImpl.java      # Implementación JDBC
│   │   │   ├── service/
│   │   │   │   └── PacienteService.java      # Lógica de negocio
│   │   │   ├── controller/
│   │   │   │   └── PacienteController.java   # Controlador de UI
│   │   │   └── ui/
│   │   │       └── PacientePanel.java        # Vista Swing
│   │   │
│   │   ├── medico/                   # 🧑‍⚕️ Gestión de Médicos
│   │   │   ├── domain/
│   │   │   │   ├── Medico.java
│   │   │   │   ├── Especialidad.java         # Especialidad médica
│   │   │   │   └── Vacaciones.java           # Vacaciones del médico
│   │   │   ├── repository/
│   │   │   │   ├── MedicoDao.java / MedicoDaoImpl.java
│   │   │   │   ├── EspecialidadDao.java / EspecialidadDaoImpl.java
│   │   │   │   ├── VacacionesDao.java / VacacionesDaoImpl.java
│   │   │   │   └── SeEspecializaEnDao.java   # Relación Médico-Especialidad
│   │   │   ├── service/
│   │   │   │   ├── MedicoService.java
│   │   │   │   └── VacacionesService.java
│   │   │   ├── ui/
│   │   │   │   ├── MedicoController.java
│   │   │   │   ├── MedicoPanel.java
│   │   │   │   ├── VacacionesController.java
│   │   │   │   └── VacacionesPanel.java
│   │   │
│   │   ├── internacion/              # 🛏️ Gestión de Internaciones
│   │   │   ├── domain/
│   │   │   │   ├── Internacion.java          # Internación de paciente
│   │   │   │   ├── Cama.java                 # Cama
│   │   │   │   ├── Habitacion.java           # Habitación
│   │   │   │   ├── Sector.java               # Sector hospitalario
│   │   │   │   ├── SeUbica.java              # Historial de asignación de camas
│   │   │   │   ├── InternacionPaciente.java  # View model
│   │   │   │   ├── CamaDisponibleResumen.java
│   │   │   │   ├── CamaDisponibleDetalle.java
│   │   │   │   ├── ComentarioVisita.java     # Comentarios de visitas médicas
│   │   │   │   └── AuditoriaGuardia.java     # Registro de auditoría de guardias
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
│   │   └── guardia/                  # 🕐 Gestión de Guardias
│   │       ├── domain/
│   │       │   ├── Guardia.java              # Registro de guardia
│   │       │   └── Turno.java                # Definición de franja horaria
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
│   └── ui/                           # Infraestructura principal de UI
│       ├── HospitalUI.java           # Ventana principal de la aplicación (JFrame)
│       ├── common/                   # Componentes compartidos de UI
│       └── view/                     # Paneles de vista centralizados
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
│   ├── init/                         # Auto-ejecutados al iniciar el contenedor
│   │   ├── 00-create-user.sql        # Crear usuario hospital
│   │   ├── 01-drop-tables.sql        # Estado limpio
│   │   ├── 02-create-tables-pk.sql   # Crear tablas + PK
│   │   ├── 03-define-fk-constrains.sql # Agregar claves foráneas
│   │   ├── 04-init-db.sql            # Insertar datos de muestra
│   │   ├── 05-triggers.sql           # Triggers de integridad de datos
│   │   ├── 06-indexes.sql            # Índices de performance
│   │   └── 10-rebuild-hospital.sql   # Script de rebuild completo
│   ├── procedures/                   # Stored procedures
│   │   ├── sp_camas_disponibles.sql
│   │   ├── sp_internaciones.sql
│   │   ├── sp_auditoria_guardias.sql
│   │   ├── sp_comentarios_visitas.sql
│   │   └── sp_vacaciones.sql
│   ├── transactions/                 # Scripts de ejemplo de transacciones
│   │   ├── vacaciones.sql
│   │   └── call_sp_vacaciones.sql
│   └── useful.sql                    # Consultas utilitarias
│
├── 📝 markdown_ES/                   # Documentacion tecnica (ES)
│   └── Modelo-relacional.md         # Diagrama ER y diseño de base
├── 📝 markdown_EN/                   # Technical documentation (EN)
├── compose.yml                       # Docker Compose para Oracle DB
├── pom.xml                           # Dependencias Maven (Java 8)
├── launch-ui.sh                      # Script de lanzamiento rápido
├── oracle.md                         # Notas de configuración Oracle
└── README_ES.md                      # Este archivo
```

**Decisiones Arquitectónicas Clave:**

1. **Package-by-Feature** - Slices verticales para mejor cohesión
2. **DAO Pattern** - Abstracción de acceso a datos con interfaces
3. **Service Layer** - Lógica de negocio y validación centralizadas
4. **MVC Pattern** - Separación de responsabilidades en la capa UI
5. **Gestión Manual de Transacciones** - Control granular de commits/rollbacks

---

<a id="documentacion"></a>
## 📚 Documentación

### Recursos para Desarrolladores

**Arquitectura y Diseño:**
- [Arquitectura.md](markdown_ES/Arquitectura.md) - Documentación completa de la arquitectura del sistema
- [Modelo-relacional.md](markdown_ES/Modelo-relacional.md) - Modelo Entidad-Relación, hipótesis y restricciones de negocio
- [Hipotesis y Restricciones.md](markdown_ES/Hipotesis%20y%20Restricciones.md) - Reglas de negocio detalladas

**Implementación de Base de Datos:**
- [Stored-Procedures y Triggers.md](markdown_ES/Stored-Procedures%20y%20Triggers.md) - Especificaciones de SP
- [Implementacion Stored-Procedures y Triggers.md](markdown_ES/Implementacion%20Stored-Procedures%20y%20Triggers.md) - Detalles de implementación
- [Indices.md](markdown_ES/Indices.md) - Estrategia de índices y optimización de performance

**Scripts de Base de Datos:**
- `db_scripts/init/` - Scripts de inicialización de base (auto-ejecutados por Docker)
- `db_scripts/procedures/` - Código fuente de los 5 stored procedures
- `db_scripts/transactions/` - Ejemplos de uso de transacciones

### Documentación de Funcionalidades

#### 1. Gestión de Pacientes (`paciente`)
- Operaciones CRUD completas
- Seguimiento de historial médico
- Identificación basada en documento (DNI, LC, LE, CI, PASAPORTE)

#### 2. Gestión de Médicos (`medico`)
- Perfiles de médicos con múltiples especialidades
- Gestión de vacaciones con validación de conflictos (`sp_vacaciones`)
- Cumplimiento de límites de guardias

#### 3. Gestión de Internaciones (`internacion`)
- Ciclo completo de hospitalización
- Asignación automática de camas vía `sp_internaciones`
- Organización de habitaciones y sectores
- Seguimiento de visitas médicas con comentarios (`sp_comentarios_visitas`)

#### 4. Gestión de Guardias (`guardia`)
- Programación de turnos por turno (bloques horarios)
- Reportes de auditoría vía `sp_auditoria_guardias`
- Prevención de conflictos de vacaciones
- Cumplimiento de máximos de guardias por médico

#### 5. Consultas de Disponibilidad de Camas
- Disponibilidad de camas en tiempo real vía `sp_camas_disponibles`
- Filtro por sector y piso
- Información detallada de habitaciones y camas

### Gestión de Transacciones

La aplicación usa **control manual de transacciones** para consistencia de datos:

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

Logging centralizado vía `LoggerConfig.java`:
- **Ubicación:** directorio `logs/`
- **Formato:** Con timestamp y niveles de log (INFO, WARNING, SEVERE)
- **Cobertura:** Operaciones de base de datos, transacciones, errores

---

<a id="desarrollo"></a>
## 🛠️ Desarrollo

### Compilar el Proyecto

```bash
# Limpiar y compilar
mvn clean compile

# Ejecutar tests
mvn test

# Empaquetar JAR con dependencias
mvn clean package

# La salida estará en target/hospital-1.0-SNAPSHOT.jar
```

### Ejecutar la Aplicación

**Opción 1: Usando script de lanzamiento (Recomendado)**
```bash
./launch-ui.sh
```

**Opción 2: Ejecución directa del JAR**
```bash
java -jar target/hospital-1.0-SNAPSHOT.jar
```

**Opción 3: Desde Maven**
```bash
mvn exec:java -Dexec.mainClass="org.hospital.AppUI"
```

### Gestión de Base de Datos

**Iniciar base de datos:**
```bash
docker compose up -d
```

**Detener base de datos:**
```bash
docker compose down
```

**Ver logs:**
```bash
docker logs -f oracle-hospital
```

**Rebuild de base desde cero:**
```bash
docker compose down -v  # Remover volúmenes
docker compose up -d
# Esperar inicialización (~30 segundos)
```

**Conectarse a la base:**
```bash
# SQLPlus
docker exec -it oracle-hospital sqlplus hospital/hospital123@//localhost:1521/FREEPDB1

# Ejecutar script de rebuild manualmente
docker exec -i oracle-hospital sqlplus hospital/hospital123@//localhost:1521/FREEPDB1 < db_scripts/init/10-rebuild-hospital.sql
```

### Dependencias del Proyecto

**Dependencias de Runtime:**
- `ojdbc8` (23.4.0.24.05) - Driver JDBC de Oracle

**Plugins de Build:**
- `maven-compiler-plugin` (3.12.1) - Compilación Java 8
- `maven-shade-plugin` (3.5.1) - Creación de Fat JAR con dependencias

**Plataforma Objetivo:** Java 8 (compatible con Java 8+)

### Guías de Organización de Código

Al agregar nuevas funcionalidades, sigue el patrón establecido:

1. **Capa de Dominio** - Crear POJOs de entidad en `feature/{name}/domain/`
2. **Capa de Repositorio** - Crear interfaz DAO e implementación en `feature/{name}/repository/`
3. **Capa de Servicio** - Agregar lógica de negocio en `feature/{name}/service/`
4. **Capa de Controlador** - Crear controlador en `feature/{name}/controller/`
5. **Capa de Vista** - Construir UI Swing en `feature/{name}/ui/`

### Tareas Comunes de Desarrollo

**Agregar una nueva entidad:**
1. Crear tabla SQL en `db_scripts/init/02-create-tables-pk.sql`
2. Agregar claves foráneas en `03-define-fk-constrains.sql`
3. Agregar datos de muestra en `04-init-db.sql`
4. Crear clase de dominio extendiendo `Persona` si aplica
5. Implementar patrón DAO (interfaz + impl)
6. Agregar capa de servicio con validación
7. Construir componentes de UI (panel + controlador)

**Agregar un stored procedure:**
1. Crear archivo `.sql` en `db_scripts/procedures/`
2. Agregar al script de init o ejecutar manualmente
3. Crear método DAO usando `CallableStatement`
4. Exponer vía capa de servicio
5. Conectar a la UI

### Troubleshooting

**Problemas de conexión a base de datos:**
- Asegurar que el contenedor Docker esté corriendo: `docker ps`
- Verificar logs: `docker logs oracle-hospital`
- Verificar string de conexión en `DatabaseConfig.java`
- Por defecto: `jdbc:oracle:thin:@localhost:1521:FREEPDB1`

**Fallos de build:**
- Asegurar que Java 8+ esté instalado: `java -version`
- Limpiar caché de Maven: `mvn clean`
- Verificar versión de Maven: `mvn -version` (se requiere 3.x)

**La UI no levanta:**
- Verificar que el JAR se haya construido: `ls -lh target/hospital-1.0-SNAPSHOT.jar`
- Revisar excepciones en la salida de consola
- Asegurar que la base esté accesible antes de iniciar la UI

<a id="recursos-adicionales"></a>
## 📖 Recursos Adicionales

### Oracle Database

- [Oracle Database Documentation](https://docs.oracle.com/en/database/oracle/oracle-database/)
- [Oracle SQL Language Reference](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/)
- [PL/SQL Language Reference](https://docs.oracle.com/en/database/oracle/oracle-database/19/lnpls/)
- [Oracle Live SQL](https://www.oracle.com/database/technologies/oracle-live-sql/) - Práctica de SQL interactiva

### Java y JDBC

- [JDBC API Documentation](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/)
- [Java Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Maven Documentation](https://maven.apache.org/guides/)

### Documentación del Proyecto

Todas las especificaciones y documentos de diseño detallados están disponibles en las carpetas `markdown_ES/` (Español) y `markdown_EN/` (Inglés).

---

<a id="objetivos-del-proyecto-y-resultados-de-aprendizaje"></a>
## 🎯 Objetivos del Proyecto y Resultados de Aprendizaje

Este proyecto demuestra:

✅ **Diseño de Base de Datos**
- Modelado Entidad-Relación
- Normalización (3FN)
- Relaciones complejas (1:1, 1:N, N:M)
- Patrones de herencia (Persona → Paciente/Medico)

✅ **Dominio de SQL**
- DDL (Data Definition Language) - Tablas, constraints
- DML (Data Manipulation Language) - Operaciones CRUD
- Stored Procedures con lógica compleja
- Triggers para integridad de datos
- Índices para optimización de performance

✅ **Arquitectura de Aplicación**
- Arquitectura en capas (Presentación → Servicio → DAO → Base de datos)
- Organización Package-by-Feature
- Patrón DAO para abstracción de acceso a datos
- Patrón MVC en la capa UI
- Gestión de transacciones

✅ **Prácticas de Ingeniería de Software**
- Organización de código limpio
- Separación de responsabilidades
- Manejo de excepciones
- Logging y debugging
- Control de versiones (Git)

✅ **Tecnologías Empresariales**
- JDBC para conectividad a base de datos
- Gestión de conexiones
- PreparedStatements (prevención de SQL injection)
- CallableStatements (invocación de stored procedures)
- Control manual de transacciones

---

<a id="licencia"></a>
## 📄 Licencia

**Proyecto Académico** - Universidad Nacional de Mar del Plata  
Facultad de Ingeniería - Ingeniería en Informática  
Curso: Bases de Datos

---

<a id="equipo"></a>
## 👥 Equipo

**Grupo 4:**
- **Mateos, Juan Cruz**
- **San Pedro, Gianfranco**

*Semestre Spring 2025*

---
