# Create, delete, and update logic  
Spanish version available in [../markdown_ES/Stored-Procedures y Triggers.md](../markdown_ES/Stored-Procedures%20y%20Triggers.md).

## (Beds, Rooms, Sectors, and Internations)

This document summarizes the design decisions to handle **create, delete, and update** operations in the hospital internment module.

---

## 1. General model

Main entities:

- `SECTOR(id_sector, descripcion)`
- `HABITACION(nro_habitacion, piso, orientacion, id_sector)`
- `CAMA(nro_habitacion, nro_cama, estado)`
- `INTERNACION(nro_internacion, fecha_inicio, fecha_fin, ...)`
- `SE_UBICA(nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion)`

Key relationships:

- A **room** belongs to a **sector**  
  `HABITACION.id_sector → SECTOR.id_sector` (FK without `ON DELETE CASCADE`).

- A **bed** belongs to a **room**  
  `CAMA.nro_habitacion → HABITACION.nro_habitacion` (FK with `ON DELETE CASCADE`).

- An **internment** is placed in beds through `SE_UBICA`:  
  `SE_UBICA.nro_internacion → INTERNACION.nro_internacion` (FK with `ON DELETE CASCADE`).

Conceptually:

> An **internment** has many **locations** in `SE_UBICA`.  
> Each location references a **bed**, inside a **room**, which in turn belongs to a **sector**.

Note about delete cascade in HABITACION and CAMA:

- Although FK CAMA.nro_habitacion -> HABITACION.nro_habitacion has ON DELETE CASCADE, trigger `tr_habitacion_no_delete_if_used` prevents deleting rooms with history in SE_UBICA.  
- In practice, cascade delete applies only to “new” rooms whose beds were never used in internments.

Field `CAMA.estado` is used to mark availability (for example: `LIBRE`, `OCUPADA`, `FUERA_DE_SERVICIO`).

---

## 2. Create and delete criteria

### 2.1. Beds

**Central idea:**  
Beds can be physically removed only if they were never used; if they have history, they are disabled but preserved.

#### Bed creation

Done through SP `sp_agregar_cama`:

- Verifies that the **room exists**.
- Allows:
  - Providing an explicit bed number, as long as it does not already exist in that room, or
  - Generating the next sequential number within the room.
- Inserts the new bed with status `LIBRE`.

#### Bed delete / deactivation

Controlled with `sp_eliminar_o_desactivar_cama`:

1. Operates only on beds in `LIBRE` status.  
2. Checks whether there is **internment history** for that bed in `SE_UBICA`:
   - If it **has no history** → **physical DELETE** of the row in `CAMA`.
   - If it **has history** → **not deleted**; status is updated to `FUERA_DE_SERVICIO`.

Motivation:

- If the bed was never used, deleting it causes no inconsistencies.
- If it already participated in internments, deleting it would leave `SE_UBICA` rows pointing to a missing bed.  
  Therefore it is kept in the database but marked as unusable.

---

### 2.2. Rooms

Rooms represent the physical structure of the hospital.  
They have no status field and deletion is highly restricted.

#### Allowed updates

- Attributes that can be modified:
  - `piso`
  - `orientacion`
  - `id_sector` (reassign a room to another sector).

This allows reorganizing the hospital without losing history.

#### Room delete

Controlled with trigger `tr_habitacion_no_delete_if_used`:

- Before a `DELETE` on `HABITACION`, it validates that:

  1. There are no active internments in that room.  
  2. There is no usage history in `SE_UBICA`.

- If any of these conditions is met, the trigger **blocks the DELETE** and returns an error.

In practice:

- Only a “new” room that was never used can be deleted.
- If the room has history, it is kept to preserve traceability (cascade to CAMA only runs when there is no historical use).

---

### 2.3. Sectors

Sectors are a **logical division** of the hospital (cardiology, oncology, etc.).

Rules:

- `HABITACION.id_sector` references `SECTOR.id_sector` **without ON DELETE CASCADE**  
  → Rooms are not deleted when deleting a sector.
- `ESPECIALIDAD.id_sector` does use `ON DELETE CASCADE`, because specialties are considered logical “children” of the sector.

#### Sector delete

- A sector can only be deleted if it **no longer has associated rooms**.
- Typical flow:
  1. Reassign that sector’s rooms to other sectors (`UPDATE HABITACION SET id_sector = ...`).
  2. When the sector is left without child rooms, `DELETE` on `SECTOR` is allowed.

---

## 3. Stored Procedures (summary)

### 3.1. Bed management and census

**File:** `sp_camas_disponibles.sql`

- `sp_camas_disponibles_resumen`  
  Returns, by sector, the number of `LIBRE` beds.

- `sp_camas_disponibles_detalle`  
  Returns the detail of available beds for a sector (room, floor, orientation, bed, status).

- `sp_agregar_cama`  
  Encapsulates bed creation, validating the room and bed number uniqueness.

- `sp_eliminar_o_desactivar_cama`  
  Implements the delete policy:
  - Physical DELETE if there is no history in `SE_UBICA`.
  - Status change to `FUERA_DE_SERVICIO` if the bed was already used.

---

### 3.2. Internments and bed change

**File:** `sp_internaciones.sql`

- `sp_crear_internacion`  
  - Inserts the internment.
  - Assigns initial bed (specific or first `LIBRE`).
  - Inserts the first location in `SE_UBICA`.  
    Trigger `tr_se_ubica_cama_estado` marks the bed as `OCUPADA`.

- `sp_cambiar_cama_internacion`  
  - Allows changing beds for an active internment.
  - Frees the previous bed.
  - Inserts a new location in `SE_UBICA` with the new bed (set to `OCUPADA` via trigger).

- `sp_internaciones_paciente`  
  Lists patient internments and returns `nro_internacion`, `fecha_inicio`, and `fecha_fin`; column `estado` is calculated in Java depending on whether `fecha_fin` is null or not.

- `sp_historial_ubicaciones_internacion`  
  Returns the location history of an internment (rooms, beds, floor, orientation, and sector).

---

### 3.3. Visit comments

**File:** `sp_comentarios_visitas.sql`

- `sp_comentarios_visitas`  
  Lists, for an internment:
  - Patient
  - Doctor who performed the visit
  - Date and time of the walkthrough
  - Recorded comment

---

### 3.4. Guard audit

**File:** `sp_auditoria_guardias.sql`

- `sp_auditoria_guardias`  
  Queries table `AUDITORIA_GUARDIA` with filters by user and date range.  
  Returns operation type, affected guard, and old/new values.

---

### 3.5. Doctor vacations

**File:** `sp_vacaciones.sql`

- `sp_agregar_vacaciones`  
  Manages creation of a vacation period for a doctor, applying these business validations:  
  - Start and end dates cannot be null and must satisfy `fecha_inicio <= fecha_fin`.  
  - Doctor must exist in tables `MEDICO` and `PERSONA`.  
  - Overlapping vacations are not allowed for the same doctor.  
  - Vacations are not allowed on dates where the doctor has guard shifts in `GUARDIA`.

---

## 4. Triggers (summary)

**File:** `05-triggers.sql`

### 4.1. `tr_aud_guardia`

- AFTER INSERT / UPDATE / DELETE ON `GUARDIA`.
- Inserts into `AUDITORIA_GUARDIA`:
  - Date/time
  - User
  - Operation type
  - Previous and new values.

### 4.2. `tr_se_ubica_cama_estado`

- BEFORE INSERT ON `SE_UBICA`.

Responsibilities:

1. Verify that the bed exists and is `LIBRE`.
2. Validate that the admission date is within the internment period.
3. Change bed status to `OCUPADA`.

### 4.3. `tr_internacion_libera_cama`

- AFTER UPDATE OF `fecha_fin` ON `INTERNACION`  
  (only when it goes from NULL to a non-null value).

Responsibilities:

1. Find the last bed used by the internment.
2. Mark that bed as `LIBRE`.

### 4.4. `tr_habitacion_no_delete_if_used`

- BEFORE DELETE ON `HABITACION`.

Blocks deletion when:

1. There are active internments in that room, or
2. There is history in `SE_UBICA` for that room.

---

### 4.5. `trg_int_paciente_medico_distintos`

- BEFORE INSERT / UPDATE OF `tipo_documento`, `nro_documento`, `matricula` ON `INTERNACION`.
- Prevents the primary doctor from being the same person as the patient (errors -20001 / -20002).

### 4.6. `tr_medico_cuil_dni`

- BEFORE INSERT / UPDATE OF `tipo_documento`, `cuil_cuit` ON `MEDICO`.
- If `tipo_doc` is `DNI`, requires `cuil_cuit` to contain the document number (error -20001).

### 4.7. `tr_internacion_unica_activa`

- BEFORE INSERT / UPDATE OF `fecha_fin` ON `INTERNACION`.
- Prevents a patient from having more than one active internment (counts other active ones and raises -20050).

### 4.8. `tr_medico_especialidad_sector_ubica`

- BEFORE INSERT ON `SE_UBICA`.
- Validates that the primary doctor has a specialty in the sector of the room where the patient is placed (errors -20051 / -20052).

### 4.9. `tr_medico_especialidad_sector_int`

- BEFORE INSERT / UPDATE OF `matricula` ON `INTERNACION`.
- If the internment already has locations, verifies that the assigned doctor has a specialty in the sector of the current room (errors -20053 / -20054).

### 4.10. `tr_guardia_no_vacaciones`

- BEFORE INSERT / UPDATE OF `matricula`, `fecha_hora` ON `GUARDIA`.
- Blocks assigning a guard to a doctor who is on vacation on that date (error -20110).

---

## 5. Final summary

- **Beds**
  - Creation via SP, controlling numbering and room.
  - Physical delete only if never used.
  - With history → marked as `FUERA_DE_SERVICIO`.

- **Rooms**
  - Represent physical structure.
  - Not deleted if they have active internments or history.
  - Can be modified (floor, orientation, sector).

- **Sectors**
  - Group rooms and specialties.
  - Can only be deleted when they no longer have associated rooms (rooms reassigned first).

- **Stored procedures** encapsulate business logic.  
- **Triggers** ensure that:
  - Bed status is consistent with `SE_UBICA` and `INTERNACION`.
  - Rooms with history cannot be deleted.
  - Guards have automatic auditing and are not assigned if the doctor is on vacation.
  - The primary doctor is different from the patient and CUIL/CUIT contains the DNI when appropriate.
  - A patient does not have two active internments.
  - The primary doctor has a specialty in the sector of the assigned room (when placing or changing doctor).

