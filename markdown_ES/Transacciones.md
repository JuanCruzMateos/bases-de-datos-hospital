# Transacciones y control de concurrencia
Versión en inglés disponible en [../markdown_EN/Transacciones.md](../markdown_EN/Transacciones.md).

Este documento detalla como manejamos **transacciones** y **concurrencia** en el modulo de vacaciones y guardias del hospital.

---

## 1. Objetivos de negocio

- Evitar que un medico tenga **guardias** dentro de su periodo de **vacaciones**.
- Evitar **solapamientos** de vacaciones para el mismo medico.
- Garantizar que las validaciones y el alta de vacaciones sean **atomicas**: o se aplican todas o no se aplica ninguna.
- Protegerse frente a **condiciones de carrera** cuando hay varias sesiones modificando GUARDIA o VACACIONES al mismo tiempo.

El objetivo es que estas reglas se cumplan incluso bajo alta concurrencia.

---

## 2. Piezas principales

- Script de transaccion manual: `db_scripts/transactions/vacaciones.sql`
- Script de llamada al stored procedure con control transaccional: `db_scripts/transactions/call_sp_vacaciones.sql`
- Stored procedure de negocio: `db_scripts/procedures/sp_vacaciones.sql` (`sp_agregar_vacaciones`)
- DAO Java equivalente: `VacacionesDaoImpl.createWithTransaction` (`src/main/java/org/hospital/feature/medico/repository/VacacionesDaoImpl.java`)

Estas piezas comparten la misma idea de control de transaccion, implementada en diferentes capas.

---

## 3. Transaccion manual en SQL: `vacaciones.sql`

Este script muestra una **transaccion completa** en PL/SQL para insertar un periodo de vacaciones.

### 3.1. Flujo general

1. Configura el **schema**:  
   `ALTER SESSION SET CURRENT_SCHEMA = hospital;`
2. Declara variables de entrada: `v_matricula`, `v_fecha_inicio`, `v_fecha_fin`.
3. Inicia la transaccion:
   - `SAVEPOINT inicio_transaccion;`
   - `SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;`
4. Bloquea la tabla `GUARDIA` en modo `SHARE ROW EXCLUSIVE` para evitar cambios concurrentes mientras se valida.
5. Ejecuta validaciones de negocio dentro de la misma transaccion:
   - Rango de fechas valido (`fecha_inicio <= fecha_fin` y no nulas).
   - El medico existe en `MEDICO`.
   - No hay vacaciones solapadas en `VACACIONES` para ese medico.
   - No hay guardias en `GUARDIA` dentro del periodo solicitado.
6. Si todo es correcto, inserta en `VACACIONES` y hace `COMMIT`.
7. Si ocurre cualquier error:
   - `ROLLBACK TO inicio_transaccion;`
   - Muestra mensajes con `DBMS_OUTPUT` y re-lanza la excepcion.
   - Si el error es `ORA-08177` (conflicto de serializacion), deja una nota recomendando **reintentar**.

### 3.2. Nivel de aislamiento SERIALIZABLE

Se usa:

```sql
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
```

Motivos:

- Evitar lecturas sucias y resultados inconsistentes mientras se validan vacaciones y guardias.
- Asegurar que, entre las consultas de validacion y el `INSERT`, otra sesion no pueda introducir un estado que viole las reglas sin que lo detectemos.
- Aceptar que, en caso de conflicto, Oracle lance un error de serializacion (que se captura y se indica como "reintentar").

### 3.3. Uso de SAVEPOINT

El script define un:

- `SAVEPOINT inicio_transaccion;`

ante cualquier error:

- `ROLLBACK TO inicio_transaccion;`

Esto permite revertir solo los cambios de la transaccion actual sin afectar otras operaciones previas en la sesion.

---

## 4. Stored procedure `sp_agregar_vacaciones`

Archivo: `db_scripts/procedures/sp_vacaciones.sql`

Este SP encapsula la **logica de negocio** para altas de vacaciones, incluyendo validaciones similares a las del script manual.

Puntos clave:

- No abre una transaccion nueva, pero:
  - Realiza todas las validaciones (medico existe, solapamientos, conflictos con guardias).
  - hace `INSERT` en `VACACIONES` cuando todo es valido.
- Maneja la transaccion con:
  - `COMMIT;` al final si todo sale bien.
  - En el `EXCEPTION` hace `ROLLBACK;`, escribe mensajes con `DBMS_OUTPUT` y **re-lanza** la excepcion.
- Trata especificamente el error `-8177` (conflicto de serializacion) con un mensaje explicito para indicar que otra transaccion modifico los datos.

Conclusion: `sp_agregar_vacaciones` asume que es la **unidad atomica de trabajo** y se auto-commitea.

---

## 5. Script `call_sp_vacaciones.sql`

Archivo: `db_scripts/transactions/call_sp_vacaciones.sql`

Muestra como invocar `sp_agregar_vacaciones` dentro de un bloque PL/SQL con control explicito:

- Define variables (matricula, fecha_inicio, fecha_fin).
- Crea un `SAVEPOINT inicio_transaccion;` y establece `SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;` antes de llamar al SP.
- Llama a `sp_agregar_vacaciones(...)`.
- En el bloque `EXCEPTION`:
  - `ROLLBACK TO inicio_transaccion;`
  - Muestra mensajes de error, incluyendo manejo especial para `ORA-08177`.
  - Re-lanza la excepcion.

Nota importante: el `COMMIT` final ocurre **dentro** de `sp_agregar_vacaciones`. El bloque de llamada solo protege con savepoint y rollback parcial.

---

## 6. Implementacion en Java: `VacacionesDaoImpl.createWithTransaction`

Clase: `src/main/java/org/hospital/feature/medico/repository/VacacionesDaoImpl.java`

Metodo: `createWithTransaction(Vacaciones vacaciones)`

Este metodo construye y ejecuta un **bloque PL/SQL dinamico** que replica la logica de `vacaciones.sql`:

- Declara `v_matricula`, `v_fecha_inicio`, `v_fecha_fin` usando parametros JDBC.
- Ejecuta:
  - `SAVEPOINT inicio_transaccion;`
  - `SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;`
  - `LOCK TABLE GUARDIA IN SHARE ROW EXCLUSIVE MODE;`
- Valida:
  - Rango de fechas.
  - No nulls.
  - Existencia del medico.
  - Solapamiento de vacaciones.
  - Conflictos con guardias.
- Inserta en `VACACIONES` si todo es correcto.
- En el `EXCEPTION` del bloque PL/SQL:
  - Hace `ROLLBACK TO inicio_transaccion;`
  - Propaga el error a JDBC.
- El DAO captura `SQLException` y la envuelve en `DataAccessException` para la capa superior.

De este modo, la aplicacion Java obtiene el mismo comportamiento transaccional que los scripts SQL.

---

## 7. Relacion con triggers y restricciones

Las transacciones de vacaciones se coordinan con otras reglas del sistema:

- Trigger `tr_guardia_no_vacaciones` sobre `GUARDIA`:
  - Impide asignar una guardia a un medico que esta de vacaciones ese dia.
- FK `VACACIONES.matricula -> MEDICO.matricula` con `ON DELETE CASCADE`:
  - Si se elimina un medico, se eliminan sus vacaciones asociadas.

Las transacciones en `VACACIONES` aseguran que el conjunto `(MEDICO, VACACIONES, GUARDIA)` permanezca consistente incluso bajo concurrencia.

---

## 8. Resumen

- Se usa **SERIALIZABLE** para las operaciones criticas de vacaciones, priorizando consistencia sobre concurrencia maxima.
- Los **SAVEPOINTS** permiten revertir solo la operacion actual sin afectar otros cambios de la sesion.
- La logica de negocio se implementa tanto en scripts SQL como en un **stored procedure** y en un **DAO Java**, manteniendo el mismo comportamiento transaccional.
- Los **triggers** y **FKs** complementan estas transacciones para garantizar reglas de negocio a nivel de esquema.
