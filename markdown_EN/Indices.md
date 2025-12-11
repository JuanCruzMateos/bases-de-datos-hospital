# Proposed indexes and relationship to requirements
Spanish version available in [../markdown_ES/Indices.md](../markdown_ES/Indices.md).

This document summarizes **which indexes we created/suggested** on the hospital database, **why** we need them, and **which functional requirements they help fulfill**.

🟦 Internaciones / beds → “List of available beds by sector”

🟩 Medical tracking → “List of visit comments for a patient in an internment”

🟥 Guard audit → “Audit of users who change guard data”

## 1. Stored procedures and triggers involved

### 🟦 Available beds by sector (Internaciones)

Requirement:
> List with the number of available beds per sector and the detailed list of those beds.

Stored procedures:

- `sp_camas_disponibles_resumen`
  - Returns, per sector, the number of **LIBRE** beds.
- `sp_camas_disponibles_detalle`
  - Returns the detail (rooms/beds) filtering by **LIBRE** status.
- `sp_agregar_cama`
- `sp_eliminar_o_desactivar_cama`
  - Keep consistency of bed stock (creation/deletion/disablement) used by the listings.

Relevant triggers:

- `tr_se_ubica_cama_estado`
  - Updates the bed `estado` (LIBRE/OCUPADA/FUERA_DE_SERVICIO) when inserting/updating `SE_UBICA`
    so that the bed listings always work against `CAMA.estado` without recalculating full history.

---

### 🟩 Medical visit comments for a patient in an internment

Requirement:
> List of medical visit comments for a patient in a given internment.

Stored procedures:

- `sp_internaciones_paciente`
  - Given a patient (`tipo_documento`, `nro_documento`),
    returns their internments so the user can choose one.
- `sp_comentarios_visitas`
  - Given `nro_internacion` and optional filters,
    returns the medical visits and comments associated with that internment.

Key tables in these SPs:

- `INTERNACION`
- `PACIENTE` / `PERSONA`
- `RECORRIDO`, `RONDA`, `VISITA`
- `COMENTA_SOBRE`

---

### 🟥 User audit on guard shifts

Requirement:
> Audit of the users who make changes to data that affect the guard assignment process.

Stored procedures:

- `sp_auditoria_guardias`
  - Lists changes recorded in `AUDITORIA_GUARDIA`
    with optional filters by user, from/to date.

Triggers:

- `tr_aud_guardia`
  - Fires on changes to `GUARDIA` and records in `AUDITORIA_GUARDIA`:
    `usuario_bd`, `fecha_hora_reg`, action, etc.

---

## 2. Indexes and justification by table

> Note: All **PKs** already have an automatic **unique B-tree index** created by Oracle.
> Here we list only the **additional (non-unique) indexes** that we added/suggest.

### 2.1. HABITACION

- **Non-unique B-tree index**  
  `idx_habitacion_sector (id_sector)` 🟦

  **Reason:**
  - The bed SPs (`sp_camas_disponibles_%`) need to group and filter by **sector**.
  - Typical query: JOIN `SECTOR` → `HABITACION` → `CAMA` with filters by `id_sector`.
  - This index speeds up:
    - “Available beds by sector”
    - Detailed listings of beds in a sector.

---

### 2.2. CAMA

- **Non-unique composite B-tree index**  
  `idx_cama_habitacion_estado (nro_habitacion, estado)` 🟦

  **Reason:**
  - Bed availability listings often query:
    - `WHERE estado = 'LIBRE'`
    - JOIN with `HABITACION` by `nro_habitacion`.
  - This index allows:
    - Quickly finding **LIBRE** beds within a room.
    - Efficient group by / counts by sector + status.
  - Positively impacts:
    - `sp_camas_disponibles_resumen`
    - `sp_camas_disponibles_detalle`
    - Automatic bed assignment logic in internments.

---

### 2.3. SE_UBICA

- **Non-unique composite B-tree index**  
  `idx_se_ubica_internacion_fecha (nro_internacion, fecha_hora_ingreso)`

  **Reason:**
  - Queries to see the location history of an internment:
    - `WHERE nro_internacion = :p_nro_internacion`
      `ORDER BY fecha_hora_ingreso`
  - Used in internal internment audit queries as well as business validations (consistency between internment period and bed occupation).
  - Not directly tied to the three highlighted requirements,
    but improves performance of checks and historical queries related to them.

---

### 2.4. INTERNACION

- **Non-unique composite B-tree index**  
  `idx_internacion_paciente (tipo_documento, nro_documento)` 🟩

  **Reason:**
  - `sp_internaciones_paciente` does:

    ```sql
    SELECT ...
      FROM INTERNACION i
      JOIN PACIENTE p ON ...
     WHERE p.tipo_documento = p_tipo_doc
       AND p.nro_documento  = p_nro_doc;
    ```

  - To get internments for a specific patient,
    this ends up using `INTERNACION.tipo_documento` + `INTERNACION.nro_documento`
    (FK to PACIENTE).
  - This index:
    - Speeds up the first step of the flow “choose a patient internment”.
    - Directly impacts requirement 🟩 of **visit comments**,
      because without this prior list of internments, you cannot select
      the internment to view comments for.

---

### 2.5. COMENTA_SOBRE

- **Non-unique B-tree index**  
  `idx_comenta_sobre_internacion (nro_internacion)` 🟩

  **Reason:**
  - `sp_comentarios_visitas` filters by a chosen internment:

    ```sql
    WHERE cs.nro_internacion = p_nro_internacion
    ```

  - The PK of `COMENTA_SOBRE` is `(id_recorrido, nro_internacion)`, so
    Oracle already has an index on that combination.
  - However, when filtering exclusively by `nro_internacion`, it is useful
    to have an index with `nro_internacion` as the first component.
  - Result:
    - Much faster search of “all comments for an internment”.
    - Directly benefits requirement 🟩 of **medical tracking**.

---

### 2.6. AUDITORIA_GUARDIA

- **Non-unique composite B-tree index**  
  `idx_aud_guardia_usuario_fecha (usuario_bd, fecha_hora_reg)` 🟥

  **Reason:**
  - `sp_auditoria_guardias` supports optional filters:

    ```sql
    WHERE (p_usuario IS NULL OR usuario_bd = p_usuario)
      AND (p_desde  IS NULL OR fecha_hora_reg >= p_desde)
      AND (p_hasta  IS NULL OR fecha_hora_reg <= p_hasta)
    ```

  - Typical uses:
    - See all changes made by a **user** in a date range.
    - Explore what guard changes occurred in a specific period.
  - This index:
    - Allows efficiently combining filter by `usuario_bd` and date range
      (prefix search + range).
    - Directly impacts requirement 🟥 of **guard audit**.

---

### 2.7. GUARDIA

> No index is marked with a color here because there is no specific guard listing stored procedure among the three original requirements, but there is a lot of Java business logic + triggers tied to guards (max monthly guards, no consecutive days, etc.).

- **Non-unique composite B-tree index**  
  `idx_guardia_medico_fecha (matricula, fecha_hora)`

  **Reason:**
  - Business logic for guards (in Java services) uses typical queries like:
    - “How many guard shifts does this doctor have in this month?”
    - “Do they have a guard the day before or after this date?”
  - All these queries always use:
    - `WHERE matricula = :p_matricula`  
      and filters/ranges on `fecha_hora`.
  - This index:
    - Speeds up business validations on guards.
    - Reduces the cost of checks before inserting/updating guards.

---

### 2.8. PACIENTE / PERSONA / MEDICO

In these cases:

- **PKs**:
  - `PERSONA (tipo_documento, nro_documento)`
  - `PACIENTE (tipo_documento, nro_documento)`
  - `MEDICO (matricula)`
- Typical queries:
  - Direct search by PK (document or matricula).
  - Joins by these PK / FK.

Given that:

- Oracle already generates **unique B-tree indexes** for these PKs,
- And the most frequent queries use those keys,

for now there is **no need** to add additional indexes on these tables
for the three highlighted requirements (🟦, 🟩, 🟥).

---

## 3. Visual summary by requirement

- 🟦 **Available beds by sector**
  - `idx_habitacion_sector (id_sector)`
  - `idx_cama_habitacion_estado (nro_habitacion, estado)`

- 🟩 **Medical visit comments**
  - `idx_internacion_paciente (tipo_documento, nro_documento)`
  - `idx_comenta_sobre_internacion (nro_internacion)`

- 🟥 **Guard audit**
  - `idx_aud_guardia_usuario_fecha (usuario_bd, fecha_hora_reg)`

The remaining proposed indexes complement overall application performance
(internment history, bed movements, guard validations, etc.),
but are not directly tied to a single reporting requirement.
