# Implementation hypotheses and constraints
Spanish version available in [../markdown_ES/Hipotesis y Restricciones.md](../markdown_ES/Hipotesis%20y%20Restricciones.md).

This document summarizes the **domain hypotheses** and the **constraints/business rules** effectively implemented in the hospital management system, both at the level of:

- **Oracle database** (tables, constraints, triggers, and stored procedures).
- **Java application** (validations in services/controllers and UI restrictions).

It serves as a bridge between the conceptual/relational model and the actual implementation.

---

## 1. Domain hypotheses

### 1.1. People, doctors, and patients

1. Every doctor is also a person, and the same person can be both doctor and patient.  
2. A patient can be registered without having an active internment.

---

### 1.2. Rooms and beds

3. The room number is unique across the entire hospital (not only on a floor).  
4. The bed number is unique within each room.  
5. Every room has at least one bed (total participation of Room in the “contains beds” relationship).  
6. A bed status can only take predefined values: `LIBRE`, `OCUPADA`, or `FUERA_DE_SERVICIO`.  
7. A room orientation can only take predefined values: `NORTE`, `SUR`, `ESTE`, or `OESTE`.

---

### 1.3. Internments and bed assignment

8. The end date of an internment can be null while the internment is still in progress.  
9. The start date of an internment cannot be later than its end date (when it is not null).  
10. A patient cannot have two active internments simultaneously.  
11. The bed assignment timestamp (`SE_UBICA.fecha_hora_ingreso`) must fall within the period of the corresponding internment.

---

### 1.4. Rounds, walkthroughs, and visited rooms

12. The date of a walkthrough must match the weekday configured in the associated round.  
13. A given round cannot include repeated rooms.  
14. A round shift is modeled with three daily fixed shifts (for example:  
    - Shift 1: 6–12 hs  
    - Shift 2: 12–18 hs  
    - Shift 3: 18–24 hs)  
15. The doctor performing the walkthrough does not necessarily match the primary doctor of the interned patient.

---

### 1.5. Guard shifts

16. The date and time of a guard must be coherent with the selected shift (morning, afternoon, or night).  
17. A doctor cannot work guard shifts on consecutive days.  
18. A doctor cannot be on guard during days that fall within their vacation period.  
19. The shift chosen for a guard must be valid for the specialty with which the doctor attends that guard.

---

### 1.6. Other assumptions

20. Uploading a doctor photo is not mandatory at registration time.  
21. A doctor may have more than one specialty (N:M relationship between doctors and specialties).  
22. There may be specialties that, at a given time, are not assigned to any doctor.

---

## 2. Implemented constraints and business rules

This section details the rules that are effectively validated in the application (Java + Oracle), many of them derived from the hypotheses above.

---

### 2.1. People, patients, and doctors

1. The only allowed document types are: `DNI`, `LC`, and `PASAPORTE`.  
2. When creating a patient or doctor:
   - If the person does not exist yet, they are inserted first in `PERSONA`.  
   - If they already exist (same pair `tipo_documento` + `nro_documento`), that row is reused, respecting the hypothesis that the same person can be doctor and patient.
3. In the UI, **document type** and **document number** are not editable once the person (patient/doctor) is created.  
4. For people, patients, and doctors only non-key data can be modified (name, surname, sex, birthdate, CUIL/CUIT, etc.); logical keys (`tipo_documento`, `nro_documento`, `matricula`) are not modified.

---

### 2.2. Doctors, specialties, and guard shifts

5. When creating a doctor it is mandatory to assign at least one specialty; if none is set, an error like the following is shown:  
   > “Medico must have at least one especialidad. Use the 'Add Especialidad' button.”

6. Field `maxCantGuardia` (maximum monthly guards per doctor):
   - Can be modified freely.  
   - Does not delete or alter existing guards.  
   - Applies only when creating new guards: when registering a guard the system counts how many that doctor has in the month/year of the new guard and blocks the operation if the maximum is exceeded.

7. Management of doctor specialties (`SE_ESPECIALIZA_EN`) – **conservative approach**:
   - When removing a specialty:
     - If the doctor has guards associated to that specialty, the transaction is `ROLLBACK`ed and a message like this is shown:  
       > “No se puede quitar la especialidad X del médico Y porque tiene guardias asociadas a esa especialidad.”
     - If no guards are associated, the corresponding row in `SE_ESPECIALIZA_EN` is deleted.
   - There is also logic to prevent a doctor from ending up without any specialty: removing the last specialty is not allowed; a message like this is shown:  
     > “Medico must have at least one especialidad.”

8. A doctor cannot be deleted if:
   - They are the primary doctor for active internments, or  
   - They have associated guards, or  
   - They have references in other entities (rounds, walkthroughs, comments, etc.).

9. Guard shifts enforce the following validations:
   - It is not allowed to create or modify a guard whose date/time is more than 1 year in the past.  
   - The guard time must fall within the time range of the selected shift (morning, afternoon, or night).  
   - The shift must be valid for the specialty (according to table `ATIENDE`).  
   - The doctor must actually have the selected specialty (according to `SE_ESPECIALIZA_EN`).

---

### 2.3. Sectors, rooms, and beds

10. A sector can only be deleted if it **has no associated rooms**.  
    Because `HABITACION.id_sector` references `SECTOR` without `ON DELETE CASCADE`, rooms must first be reassigned or correctly removed; only then can `DELETE` be issued on `SECTOR`.

11. In the UI, room number (`nro_habitacion`, PK) is not editable; it is defined only at creation.

12. Only “new” rooms can be deleted, meaning those with no usage history (no internments or records in `SE_UBICA`).  
    If they have history, the system blocks deletion (analogous to “do not delete entities with medical history”).

13. For beds, the following logic applies:
    - If a bed **has no history** in `SE_UBICA`, it can be physically removed (`DELETE` in `CAMA`).  
    - If a bed **has history**, it is not deleted: its status is set to `FUERA_DE_SERVICIO`.  
      That is, beds with history are not deleted, only disabled.

---

### 2.4. Final notes

- This document summarizes the main rules that impact **create, delete, and update** operations of the most relevant entities in the system (patients, doctors, internments, rooms, beds, guard shifts, rounds, and walkthroughs).  
- For more detail on the technical implementation of triggers and stored procedures, see:  
  `Stored-Procedures y Triggers.md`.
