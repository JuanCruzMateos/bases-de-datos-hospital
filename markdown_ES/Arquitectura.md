# Hospital Management System - Arquitectura V1
Versión en inglés disponible en [../markdown_EN/Arquitectura.md](../markdown_EN/Arquitectura.md).

## ✅ Arquitectura en capas - Manejo manual de transacciones

```
┌──────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACION                      │
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
│  Comun: BaseController (logging + manejo de errores)         │
└──────────────────────┬───────────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────────┐
│                        SERVICE LAYER                         │
│                      (Logica de negocio)                    │
├──────────────────────────────────────────────────────────────┤
│  ✅ PacienteService        - Reglas de negocio de paciente   │
│  ✅ MedicoService          - Reglas de negocio de medico     │
│  ✅ SectorService          - Reglas de negocio de sector     │
│  ✅ HabitacionService      - Reglas de negocio de habitacion │
│  ✅ InternacionService     - Internaciones y camas           │
│  ✅ GuardiaService         - Reglas de guardias medicas      │
│  ✅ CamaService            - Manejo de camas y estado        │
│  ✅ CamaDisponibleService  - Reportes de camas disponibles   │
│  ✅ VisitasMedicasService  - Reportes de internaciones y     │
│                              comentarios de visitas          │
│  ✅ AuditoriaGuardiasService - Reportes de auditoria guardia │
│  ✅ VacacionesService      - Logica de vacaciones de medicos │
│                                                              │
│  Features:                                                   │
│  • Validacion de negocio                                     │
│  • Validacion cruzada entre entidades                        │
│  • Prevencion de duplicados                                  │
│  • Logging                                                   │
│  • Reglas de dominio especificas                             │
└──────────────────────┬───────────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────────┐
│                      DATA ACCESS LAYER                       │
│                     (Transacciones manuales)                 │
├──────────────────────────────────────────────────────────────┤
│  ✅ PacienteDaoImpl        (tx manual + logging)             │
│  ✅ MedicoDaoImpl          (tx manual + logging)             │
│  ✅ SectorDaoImpl          (tx manual + logging)             │
│  ✅ HabitacionDaoImpl      (tx manual + logging)             │
│  ✅ InternacionDaoImpl     (tx manual + logging)             │
│  ✅ GuardiaDaoImpl         (tx manual + logging)             │
│  ✅ EspecialidadDaoImpl    (tx manual + logging)             │
│  ✅ TurnoDaoImpl           (tx manual + logging)             │
│  ✅ CamaDaoImpl            (tx manual + logging)             │
│  ✅ SeUbicaDaoImpl         (tx manual + logging)             │
│  ✅ CamaDisponibleDaoImpl  (solo lectura, stored procedures) │
│  ✅ VisitasMedicasDaoImpl  (solo lectura, stored procedures) │
│  ✅ AuditoriaGuardiasDaoImpl (solo lectura, stored procedure)│
│  ✅ VacacionesDaoImpl      (PL/SQL transaccional)            │
│                                                              │
│  Patron de transaccion:                                      │
│  • connection = DriverManager.getConnection()                │
│  • connection.setAutoCommit(false)                           │
│  • Ejecutar SQL con PreparedStatement                        │
│  • connection.commit() en exito                              │
│  • connection.rollback() en error                            │
│  • connection.close() en finally                             │
└──────────────────────┬───────────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────────┐
│                  DATABASE CONNECTION                         │
│                    (JDBC DriverManager)                      │
├──────────────────────────────────────────────────────────────┤
│  ✅ JDBC basico via DriverManager                            │
│  ✅ Una conexion por operacion (sin pool en V1)              │
│  ✅ Propiedades desde application.properties                 │
│  ✅ Oracle JDBC Driver (ojdbc8)                              │
│                                                              │
│  V2 agregara: Connection Pooling (HikariCP)                  │
└──────────────────────┬───────────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────────┐
│                         DATABASE                             │
│                       (Oracle DB)                            │
└──────────────────────────────────────────────────────────────┘
```

---

## 📊 Matriz de consistencia

| Entidad / Modulo | Controller✅ | Service✅ | DAO            | Tx manual | Logging✅ |
|------------------|-------------|----------|----------------|----------|----------|
| Paciente         | ✅ Service  | ✅ Full  | ✅ Full        | ✅ Yes   | ✅ Full  |
| Medico           | ✅ Service  | ✅ Full  | ✅ Full        | ✅ Yes   | ✅ Full  |
| Sector           | ✅ Service  | ✅ Full  | ✅ Full        | ✅ Yes   | ✅ Full  |
| Habitacion       | ✅ Service  | ✅ Full  | ✅ Full        | ✅ Yes   | ✅ Full  |
| Internacion      | ✅ Service  | ✅ Full  | ✅ Full        | ✅ Yes   | ✅ Full  |
| Guardia          | ✅ Service  | ✅ Full  | ✅ Full        | ✅ Yes   | ✅ Full  |
| Cama             | N/A         | ✅ Full  | ✅ Full        | ✅ Yes   | ✅ Full  |
| SeUbica          | N/A         | via Cama | ✅ Full        | ✅ Yes   | ✅ Full  |
| Especialidad     | N/A         | via Medico| ✅ Full       | ✅ Yes   | ✅ Full  |
| Turno            | N/A         | N/A      | ✅ Full        | ✅ Yes   | ✅ Full  |
| Camas Disponibles| ✅ Service  | ✅ Full  | ✅ Lectura SP  | N/A      | ✅ Full  |
| Visitas Medicas  | ✅ Service  | ✅ Full  | ✅ Lectura SP  | N/A      | ✅ Full  |
| AuditoriaGuardias| ✅ Service  | ✅ Full  | ✅ Lectura SP  | N/A      | ✅ Full  |
| Vacaciones       | ✅ Service  | ✅ Full  | ✅ PL/SQL Tx   | ✅ Yes   | ✅ Full  |

Leyenda:
- ✅ Implementado completamente
- N/A - No aplica (por ejemplo, sin controller dedicado)
- Tx manual - Manejo explicito de transacciones JDBC

---

## 🎯 Implementaciones completadas

### ✅ Capa de presentacion (controllers)

Patron comun en controllers:

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
        // Registrar listeners de botones y tablas
    }
}
```

Ejemplos clave:

- `PacienteController`
  - Usa `PacienteService`.
  - Muestra validaciones de negocio al usuario.
- `HabitacionController`
  - Valida sector existente y datos de habitacion.
- `InternacionController`
  - Valida paciente, medico y previene internacion activa duplicada.
- `GuardiaController`
  - Combina medico, especialidad y turno en la UI.
- `CamaDisponibleController`
  - Carga automaticamente resumen de camas y permite ver el detalle.
- `VisitasMedicasController`
  - Busca internaciones por tipo y numero de documento y permite ver comentarios.
- `AuditoriaGuardiasController`
  - Lista registros de auditoria de cambios en guardias.
- `VacacionesController`
  - Coordina altas, cambios y bajas de vacaciones con feedback detallado.

---

### ✅ Capa de servicios (logica de negocio)

Todos los servicios siguen el patron mostrado y se encargan de:

- Validar datos de entrada.
- Aplicar reglas de negocio (fechas, estados, restricciones).
- Encapsular logica comun (por ejemplo, calculo de estado de internacion).
- Delegar al DAO correspondiente.

Servicios mas relevantes:

- `CamaDisponibleService`
  - Exponde metodos `getResumen()` y `getDetalle(idSector)`.
  - No maneja transacciones propias, ya que son consultas de lectura.

- `VisitasMedicasService`
  - Valida tipo y numero de documento.
  - Llama a `VisitasMedicasDao` para obtener internaciones y comentarios.

- `AuditoriaGuardiasService`
  - Valida rango de fechas cuando se usa filtro.
  - Delegacion simple a `AuditoriaGuardiasDao`.

- `VacacionesService`
  - Orquesta reglas de vacaciones:
    - Rango de fechas valido.
    - Eliminacion y recreacion de periodos con validacion.
  - Usa metodos transaccionales de `VacacionesDaoImpl`.

---

### ✅ Capa de acceso a datos (DAOs)

Para DAOs CRUD clasicos se usa manejo manual de transacciones como se
mostro en el diagrama. Ejemplo tipico:

```java
connection = DatabaseConfig.getConnection();
connection.setAutoCommit(false);
// SQL...
connection.commit();
```

Ante error:

```java
connection.rollback();
```

Y siempre:

```java
connection.close();
```

#### DAOs de solo lectura con stored procedures

Estos DAOs no abren transacciones explicitas; se limitan a usar
`CallableStatement` y recorrer cursores:

- `CamaDisponibleDaoImpl`
  - `{CALL sp_camas_disponibles_resumen(?)}`
  - `{CALL sp_camas_disponibles_detalle(?, ?)}`

- `VisitasMedicasDaoImpl`
  - `{CALL sp_internaciones_paciente(?, ?, ?)}`
  - `{CALL sp_comentarios_visitas(?, ?)}`

- `AuditoriaGuardiasDaoImpl`
  - `{CALL sp_auditoria_guardias(?, ?, ?, ?)}`

#### DAO con PL/SQL transaccional: VacacionesDaoImpl

`VacacionesDaoImpl` incluye metodos:

- `createWithTransaction(Vacaciones v)`
- `updateWithTransaction(oldV, newV)`

Ambos construyen un bloque PL/SQL que:

- Define variables locales (matricula, fecha_inicio, fecha_fin).
- Ejecuta `SAVEPOINT inicio_transaccion;`.
- Ejecuta `SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;`.
- Bloquea `GUARDIA` en modo `SHARE ROW EXCLUSIVE`.
- Verifica:
  - Rango de fechas.
  - Medico existente.
  - Solapamiento de vacaciones.
  - Guardias dentro del periodo.
- Inserta o actualiza en `VACACIONES`.
- En excepcion hace `ROLLBACK TO inicio_transaccion;` y propaga el error.

Mas detalles en [Transacciones.md](Transacciones.md).

---

## 🔄 Estado de consistencia de arquitectura - V1

Estado general:

- Servicios creados para todos los modulos.
- Todos los controllers usan servicios (no DAOs directos).
- DAOs CRUD usan manejo manual de transacciones.
- DAOs de reportes usan stored procedures de solo lectura.
- Vacaciones combina SP y PL/SQL transaccional para reglas complejas.
- Logging y manejo de errores en todas las capas.

V1 mantiene la filosofia:

- Simple, explicito y educativo.
- Facil de seguir para aprender patrones de capas y transacciones.

V2 podra agregar:

- Pool de conexiones (HikariCP).
- Utilidad `TransactionManager` para reducir codigo repetido.
- Pruebas unitarias e integracion.
