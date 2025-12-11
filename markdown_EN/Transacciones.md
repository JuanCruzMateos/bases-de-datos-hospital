# Transactions and concurrency control
Spanish version available in [../markdown_ES/Transacciones.md](../markdown_ES/Transacciones.md).

This document details how we handle **transactions** and **concurrency** in the hospital's vacation and guard module.

---

## 1. Business objectives

- Prevent a doctor from having **guard shifts** during their **vacation** period.
- Prevent **overlapping** vacations for the same doctor.
- Ensure vacation validations and creation are **atomic**: either everything is applied or nothing is.
- Protect against **race conditions** when multiple sessions modify GUARDIA or VACACIONES at the same time.

The goal is to meet these rules even under high concurrency.

---

## 2. Main pieces

- Manual transaction script: `db_scripts/transactions/vacaciones.sql`
- Script to call the stored procedure with transactional control: `db_scripts/transactions/call_sp_vacaciones.sql`
- Business stored procedure: `db_scripts/procedures/sp_vacaciones.sql` (`sp_agregar_vacaciones`)
- Equivalent Java DAO: `VacacionesDaoImpl.createWithTransaction` (`src/main/java/org/hospital/feature/medico/repository/VacacionesDaoImpl.java`)

These pieces share the same transaction control idea, implemented in different layers.

---

## 3. Manual transaction in SQL: `vacaciones.sql`

This script shows a **full transaction** in PL/SQL to insert a vacation period.

### 3.1. General flow

1. Configure **schema**:  
   `ALTER SESSION SET CURRENT_SCHEMA = hospital;`
2. Declare input variables: `v_matricula`, `v_fecha_inicio`, `v_fecha_fin`.
3. Start transaction:
   - `SAVEPOINT inicio_transaccion;`
   - `SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;`
4. Lock table `GUARDIA` in `SHARE ROW EXCLUSIVE` mode to avoid concurrent changes while validating.
5. Run business validations within the same transaction:
   - Valid date range (`fecha_inicio <= fecha_fin` and not null).
   - Doctor exists in `MEDICO`.
   - No overlapping vacations in `VACACIONES` for that doctor.
   - No guard shifts in `GUARDIA` within the requested period.
6. If all is correct, insert into `VACACIONES` and `COMMIT`.
7. If any error occurs:
   - `ROLLBACK TO inicio_transaccion;`
   - Show messages with `DBMS_OUTPUT` and re-raise the exception.
   - If the error is `ORA-08177` (serialization conflict), leave a note recommending **retry**.

### 3.2. SERIALIZABLE isolation level

Used:

```sql
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
```

Reasons:

- Avoid dirty reads and inconsistent results while validating vacations and guards.
- Ensure that between validations and `INSERT`, another session cannot introduce a state that violates the rules without us detecting it.
- Accept that, in case of conflict, Oracle raises a serialization error (captured and indicated as “retry”).

### 3.3. Use of SAVEPOINT

The script defines:

- `SAVEPOINT inicio_transaccion;`

on any error:

- `ROLLBACK TO inicio_transaccion;`

This allows reverting only the changes of the current transaction without affecting other operations in the session.

---

## 4. Stored procedure `sp_agregar_vacaciones`

File: `db_scripts/procedures/sp_vacaciones.sql`

This SP encapsulates the **business logic** for creating vacations, including validations similar to the manual script.

Key points:

- Does not open a new transaction, but:
  - Performs all validations (doctor exists, overlaps, guard conflicts).
  - Does `INSERT` into `VACACIONES` when all is valid.
- Manages the transaction with:
  - `COMMIT;` at the end if everything succeeds.
  - In `EXCEPTION` it does `ROLLBACK;`, writes messages with `DBMS_OUTPUT`, and **re-raises** the exception.
- Specifically handles error `-8177` (serialization conflict) with an explicit message to indicate another transaction modified the data.

Conclusion: `sp_agregar_vacaciones` assumes it is the **atomic unit of work** and self-commits.

---

## 5. Script `call_sp_vacaciones.sql`

File: `db_scripts/transactions/call_sp_vacaciones.sql`

Shows how to call `sp_agregar_vacaciones` inside a PL/SQL block with explicit control:

- Defines variables (matricula, fecha_inicio, fecha_fin).
- Creates `SAVEPOINT inicio_transaccion;` and sets `SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;` before calling the SP.
- Calls `sp_agregar_vacaciones(...)`.
- In the `EXCEPTION` block:
  - `ROLLBACK TO inicio_transaccion;`
  - Shows error messages, including special handling for `ORA-08177`.
  - Re-raises the exception.

Important note: the final `COMMIT` happens **inside** `sp_agregar_vacaciones`. The call block only protects with savepoint and partial rollback.

---

## 6. Implementation in Java: `VacacionesDaoImpl.createWithTransaction`

Class: `src/main/java/org/hospital/feature/medico/repository/VacacionesDaoImpl.java`

Method: `createWithTransaction(Vacaciones vacaciones)`

This method builds and executes a **dynamic PL/SQL block** that mirrors the logic of `vacaciones.sql`:

- Declares `v_matricula`, `v_fecha_inicio`, `v_fecha_fin` using JDBC parameters.
- Executes:
  - `SAVEPOINT inicio_transaccion;`
  - `SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;`
  - `LOCK TABLE GUARDIA IN SHARE ROW EXCLUSIVE MODE;`
- Validates:
  - Date range.
  - Not null.
  - Doctor existence.
  - Vacation overlap.
  - Conflicts with guards.
- Inserts into `VACACIONES` if all is correct.
- In the PL/SQL block `EXCEPTION`:
  - Does `ROLLBACK TO inicio_transaccion;`
  - Propagates the error to JDBC.
- The DAO captures `SQLException` and wraps it in `DataAccessException` for the upper layer.

This way, the Java application gets the same transactional behavior as the SQL scripts.

---

## 7. Relationship with triggers and constraints

Vacation transactions are coordinated with other system rules:

- Trigger `tr_guardia_no_vacaciones` on `GUARDIA`:
  - Prevents assigning a guard to a doctor who is on vacation that day.
- FK `VACACIONES.matricula -> MEDICO.matricula` with `ON DELETE CASCADE`:
  - If a doctor is deleted, their associated vacations are deleted.

Transactions in `VACACIONES` ensure the set `(MEDICO, VACACIONES, GUARDIA)` remains consistent even under concurrency.

---

## 8. Summary

- **SERIALIZABLE** is used for critical vacation operations, prioritizing consistency over maximum concurrency.
- **SAVEPOINTS** allow reverting only the current operation without affecting other session changes.
- Business logic is implemented in SQL scripts, a **stored procedure**, and a **Java DAO**, keeping the same transactional behavior.
- **Triggers** and **FKs** complement these transactions to enforce business rules at the schema level.

