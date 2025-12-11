
# Hipótesis y restricciones de la implementación
Versión en inglés disponible en [../markdown_EN/Hipotesis y Restricciones.md](../markdown_EN/Hipotesis%20y%20Restricciones.md).

Este documento resume las **hipótesis de dominio** y las **restricciones / reglas de negocio** efectivamente implementadas en el sistema de gestión del hospital, tanto a nivel de:

- **Base de datos Oracle** (tablas, constraints, triggers y stored procedures).
- **Aplicación Java** (validaciones en servicios/controladores y restricciones de la UI).

Sirve como puente entre el modelo conceptual / relacional y la implementación real.

---

## 1. Hipótesis de dominio

### 1.1. Personas, médicos y pacientes

1. Todo médico es también una persona, y una misma persona puede ser médico y paciente a la vez.  
2. Se permite dar de alta un paciente sin que tenga una internación activa.

---

### 1.2. Habitaciones y camas

3. El número de habitación es único en todo el hospital (no solo dentro de un piso).  
4. El número de cama es único dentro de cada habitación.  
5. Toda habitación tiene al menos una cama (participación total de Habitación en la relación “contiene camas”).  
6. El estado de una cama solo puede tomar valores predefinidos: `LIBRE`, `OCUPADA` o `FUERA_DE_SERVICIO`.  
7. La orientación de una habitación solo puede tomar valores predefinidos: `NORTE`, `SUR`, `ESTE` u `OESTE`.

---

### 1.3. Internaciones y ubicación en camas

8. La fecha de fin de una internación puede ser nula mientras la internación siga en curso.  
9. La fecha de inicio de una internación no puede ser posterior a su fecha de fin (cuando esta no es nula).  
10. Un paciente no puede tener dos internaciones activas de forma simultánea.  
11. La fecha y hora de asignación de una cama (`SE_UBICA.fecha_hora_ingreso`) debe estar dentro del período de la internación correspondiente.

---

### 1.4. Rondas, recorridos y habitaciones visitadas

12. La fecha de un recorrido debe coincidir con el día de la semana configurado en la ronda asociada.  
13. En una ronda dada no puede haber habitaciones repetidas.  
14. Un turno de ronda se modela con tres turnos diarios con horarios fijos (por ejemplo:  
    - Turno 1: 6–12 hs  
    - Turno 2: 12–18 hs  
    - Turno 3: 18–24 hs)  
15. El médico que realiza el recorrido no necesariamente coincide con el médico principal del paciente internado.

---

### 1.5. Guardias

16. La fecha y hora de una guardia debe ser coherente con el turno seleccionado (mañana, tarde o noche).  
17. Un médico no puede realizar guardias en días consecutivos.  
18. Un médico no puede estar de guardia en un día que forme parte de su período de vacaciones.  
19. El turno elegido para una guardia debe ser válido para la especialidad con la que el médico atiende en esa guardia.

---

### 1.6. Otros supuestos

20. No es obligatorio cargar la foto del médico al momento de registrarlo.  
21. Un médico puede tener más de una especialidad (relación N:M entre médicos y especialidades).  
22. Pueden existir especialidades que, en un momento dado, no sean atendidas por ningún médico.

---

## 2. Restricciones y reglas de negocio implementadas

En esta sección se detallan las reglas que efectivamente se validan en la aplicación (Java + Oracle), muchas de ellas derivadas de las hipótesis anteriores.

---

### 2.1. Personas, pacientes y médicos

1. Los tipos de documento permitidos son únicamente: `DNI`, `LC` y `PASAPORTE`.  
2. En el alta de paciente o de médico:
   - Si la persona aún no existe, primero se inserta en `PERSONA`.  
   - Si ya existe (mismo par `tipo_documento` + `nro_documento`), se reutiliza esa fila, respetando la hipótesis de que una misma persona puede ser médico y paciente.
3. En la interfaz, el **tipo de documento** y el **número de documento** no son editables una vez creada la persona (paciente/médico).  
4. Para personas, pacientes y médicos solo se pueden modificar datos no clave (nombre, apellido, sexo, fecha de nacimiento, CUIL/CUIT, etc.); las claves lógicas (`tipo_documento`, `nro_documento`, `matricula`) no se modifican.

---

### 2.2. Médicos, especialidades y guardias

5. En el alta de un médico es obligatorio asignarle al menos una especialidad; si no tiene ninguna, se muestra un error del estilo:  
   > “Medico must have at least one especialidad. Use the 'Add Especialidad' button.”

6. Campo `maxCantGuardia` (máximo de guardias mensuales por médico):
   - Puede modificarse libremente.  
   - No borra ni altera guardias ya existentes.  
   - Se aplica solo al crear nuevas guardias: al registrar una guardia se cuenta cuántas tiene ese médico en el mes/año de la nueva guardia y se bloquea la operación si se supera el máximo.

7. Gestión de especialidades del médico (`SE_ESPECIALIZA_EN`) – **enfoque conservador**:
   - Al quitar una especialidad:
     - Si el médico tiene guardias asociadas a esa especialidad, se hace `ROLLBACK` de la transacción y se muestra un mensaje del tipo:  
       > “No se puede quitar la especialidad X del médico Y porque tiene guardias asociadas a esa especialidad.”
     - Si no tiene guardias asociadas, se borra la fila correspondiente de `SE_ESPECIALIZA_EN`.
   - Además, existe lógica para impedir que un médico se quede sin ninguna especialidad: no se permite eliminar la última especialidad; se muestra un mensaje tipo:  
     > “Medico must have at least one especialidad.”

8. Un médico no se puede eliminar si:
   - Es médico principal de internaciones activas, o  
   - Tiene guardias asociadas, o  
   - Tiene referencias en otras entidades (rondas, recorridos, comentarios, etc.).

9. En guardias se aplican las siguientes validaciones:
   - No se permite crear ni modificar una guardia cuya fecha/hora sea más de 1 año en el pasado.  
   - Se valida que la hora de la guardia se encuentre dentro del rango horario del turno seleccionado (mañana, tarde o noche).  
   - Se valida que el turno elegido sea válido para la especialidad (de acuerdo a la tabla `ATIENDE`).  
   - Se valida que el médico tenga efectivamente la especialidad seleccionada (de acuerdo a `SE_ESPECIALIZA_EN`).

---

### 2.3. Sectores, habitaciones y camas

10. Un sector solo puede eliminarse si **no tiene habitaciones asociadas**.  
    Dado que `HABITACION.id_sector` referencia a `SECTOR` sin `ON DELETE CASCADE`, primero deben reasignarse o eliminarse correctamente las habitaciones; recién entonces se puede hacer `DELETE` en `SECTOR`.

11. En la interfaz, el número de habitación (`nro_habitacion`, PK) no es modificable; solo se define en el alta.

12. Solo se pueden borrar habitaciones “nuevas”, es decir, sin historial de uso (sin internaciones ni registros en `SE_UBICA`).  
    Si tienen historia, el sistema bloquea la eliminación (criterio análogo a “no borrar entidades con historia clínica”).

13. Para las camas se aplica la siguiente lógica:
    - Si una cama **no tiene historial** en `SE_UBICA`, puede eliminarse físicamente (`DELETE` en `CAMA`).  
    - Si una cama **tiene historial**, no se elimina: se cambia su estado a `FUERA_DE_SERVICIO`.  
      Es decir, las camas con historia no se borran, solo se deshabilitan.

---

### 2.4. Notas finales

- Este documento resume las reglas principales que impactan en **altas, bajas y modificaciones** de las entidades más relevantes del sistema (pacientes, médicos, internaciones, habitaciones, camas, guardias, rondas y recorridos).  
- Para más detalle sobre la implementación técnica de triggers y stored procedures, ver el archivo:  
  `Stored-Procedures y Triggers.md`.
