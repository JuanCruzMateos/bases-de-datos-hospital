# **Bases de Datos** | FI UNMdP

# *Ingeniería en Informática*

**Grupo 4**

- Bonifazi, Paula  
- Mateos, Juan Cruz  
- Navarro, Pablo  
- Parise, Thiago  
- San Pedro, Gianfranco


# Diagrama Entidad Relación

***Nota**: Se adjunta el diagrama también como un documento aparte para facilitar su lectura.*  

## Hipotesis

1. El hospital puede dar de alta un paciente sin la necesidad de registrar una internación.  
2. No es necesario cargar la foto del médico al momento de registrarlo.  
3. Una habitación no puede cambiar de sector.  
4. Los números de habitación son únicos en toda el hospital  y no se repiten dentro de cada piso.  
5. El estado de una habitación tiene valores predefinidos(Libre, Ocupada).  
6. La orientación de una habitación tiene valores predefinidos (Norte, Sur, Este, Oeste).  
7. Toda Habitación tiene al menos una Cama (participación total de Habitación en contiene)  
8. La fecha de finalización de una internación puede ser nula si la internación sigue en curso.  
9. Un médico puede tener más de una especialidad (cardinalidad N:M).  
10. Pueden existir especialidades que no sean atendidas por ningún médico.  
11. Un turno de ronda tiene horarios definidos (ej: Turno 1: 6-12hs, Turno 2: 12-18hs, etc.)  
12. El médico que hace el recorrido no necesariamente es el médico principal del paciente.  
13. La fecha de fin de internación puede ser nula si sigue en curso.

## Restricciones

1. Todo médico debe estar registrado también como persona.  
2. La fecha de inicio de una internación no puede ser posterior a su fecha de fin.  
3. Un paciente no puede tener dos internaciones activas simultáneamente.  
4. La fecha y hora de asignación de cama debe estar dentro del período de internación.  
5. El médico principal debe tener al menos una especialidad asociada con el sector.  
6. La fecha de un recorrido debe corresponder al día se la semana de la ronda.  
7. Un médico no puede realizar guardias en días consecutivos.   
8. No se pueden eliminar médicos que sean médicos principales de internaciones activas.  
9. No se pueden eliminar habitaciones con camas ocupadas.  
10. No se puede asignar una cama ya ocupada.  
11. Un médico no puede estar de guardia si está de vacaciones ese día.  
12. En una ronda dada no hay habitaciones repetidas.  
13. Existen 3 turnos de guardia por día por especialidad.  
14. El turno elegido para una guardia debe ser válido para la especialidad del médico que la realiza.

---

# Modelo Relacional

**Nota**: Un atributo en *itálica* significa que es FK. Adoptamos esta notación a modo de simplificar la escritura del MR.

### Persona(tipoDocumento, nroDocumento, nombre, apellido, tipo)  
CK \= PK \= {(tipoDocumento, nroDocumento)}  
FK={}

---

### Paciente(*tipoDocumento*, *nroDocumento*, fechaNacimiento, sexo)  
CK \= PK \= FK \= {(tipoDocumento, nroDocumento)}

Paciente.(tipoDocumento, nrodocumento) debe estar en Persona.(tipoDocumento, nrodocumento)  
Persona.(tipoDocumento, nrodocumento) puede no estar en Paciente.(tipoDocumento, nrodocumento)

---

### Medico(matricula, cuilcuit, fechaIngreso, foto, maxCantGuardia, periodoVacaciones, *tipoDocumento, nroDocumento)*  
CK \= { (tipoDocumento, nroDocumento), matricula, {cuilCuit} }  
PK \= {matricula}  
FK \= {(tipoDocumento, nroDocumento)}

Medico.(tipoDocumento, nrodocumento) debe estar en Persona.(tipoDocumento, nrodocumento)  
Persona.(tipoDocumento, nrodocumento) debe estar en Medico.(tipoDocumento, nrodocumento)

---

### Especialidad(codEspecialidad, descripción, *idSector*)  
CK \= PK \= {codEspecialidad}  
FK \= {*idSector*}

Especialidad.idSector debe estar en Sector.idSector  
Sector.idSector debe estar en Especialidad.idSector

---

### Se\_Especializa\_En(*matricula*, *codEspecialidad*, haceGuardia)  
CK \= PK \= {(matricula, codEspecialidad)}  
FK \= {matricula, codEspecialidad}

Medico.matricula debe estar en Se\_Especializa\_En.matricula  
Especialidad.codEspecialidad puede no estar en Se\_Especializa\_En.codEspecialidad  
Se\_Especializa\_En.matricula debe estar en Medico.matricula  
Se\_Especializa\_En.codEspecialidad debe estar en Especialidad.codEspeciadlidad

---

### Internacion(nroInternacion, fechaInicio, fechaFin, *tipoDocumento, nroDocumento, matricula*)  
CK \= PK \= {nroInternacion}  
FK \= {(tipoDocumento, nroDocumento),  matricula}

Internacion.(tipoDocumento, nrodocumento) debe estar en Paciente.(tipoDocumento, nrodocumento)  
Paciente.(tipoDocumento, nrodocumento) puede no estar en Internacion.(tipoDocumento, nrodocumento)  
Internacion.matricula debe estar en Medico.matricula  
Medico.matricula puede no estar en Internacion.matricula

---

### Se\_Ubica(*nroInternacion*, *nroCama*, *nroHabitacion*,fechaYHoraDeIngreso)  
CK \= PK \= {(nroInternacion, nroCama, nroHabitacion, fechaYHoraDeIngreso)}  
FK \= {nroInternacion, (nroCama, nroHabitacion)}

Internacion.nroInternacion debe estar en Se\_Ubica.nroInternacion  
Se\_Ubica.nroInternacion debe estar en Internacion.nroInternacion  
Cama.(nroInternacion, nroCama) debe estar en Se\_Ubica.(nroInternacion, nroCama)  
Cama(*nroHabitacion*, nroCama, estado)  
CK \= PK \= {(nroCama, nroHabitacion)}  
FK \= {nroHabitacion}

Cama.nroHabitacion debe estar en Habitacion.nroHabitacion  
Habitacion.nroHabitacion debe estar en Cama.nroHabitacion

---

### Habitación(nroHabitacion, piso, orientación, *idSector*)  
CK \= PK \= {nroHabitacion}   
FK \= {idSector}

Habitacion.idSector debe estar en Sector.idSector  
Sector.idSector debe estar en Habitacion.idSector

---

### Sector(idSector, descripción)  
CK \= PK \= {idSector}

---

### Ronda(idRonda, diaDeLaSemana, turno)  
CK \= PK \= {idRonda} 

---

### Visita(*idRonda*, *nroHabitacion*)  
CK \= PK \= {(idRonda, nroHabitacion)}  
FK \= {idRonda, nroHabitacion}

Ronda.idRonda debe estar en Visita.idRonda  
Habitacion.nroHabitacion debe estar en Visita.nroHabitacion  
Visita.nroHabitacion debe estar en Habitacion.nroHabitacion  
Visita.idRonda debe estar en Ronda.idRonda

---

### Recorrido(idRecorrido, fechaDeRecorrido, horaInicio, horaFin, *idRonda, matricula*)  
CK \= PK \= {idRecorrido}  
FK \= {idRonda, matricula}

Recorrido.idRonda debe estar en Ronda.idRonda  
Recorrido.matricula debe estar en Medico.matricula  
Ronda.idRonda puede no estar en Recorrido.idRonda  
Medico.matriculal puede no estar en Recorrido.matricula

Comenta\_Sobre(*idRecorrido, nroInternacion*, comentario)  
CK \= PK \= {(idRecorrido, nroInternacion)}  
FK \= {idRecorrido, nroInternacion}

Recorrido.idRecorrido debe estar en Comenta\_Sobre.idRecorrido  
Internacion.nroInternacion puede no estar en Comenta\_Sobre.nroInternacion  
Comenta\_Sobre.idRecorrido debe estar en Recorrido.idRecorrido  
Comenta\_Sobre.nroInternacion debe estar en Internacion.nroInternacion

---

### Guardia(nroGuardia, fechaYHora, *matricula, codEspecialidad, idTurno*)  
CK \= PK \= {nroGuardia}   
FK \= {(matrícula, codEspecialidad), idTurno}

Guardia.(matrícula ,codEspecialidad) debe estar en Se\_Especializia\_En.(matrícula, codEspecialidad)  
Se\_Especializia\_En.(matrícula, codEspecialidad) puede no estar en Guardia.(matrícula, codEspecialidad)  
Guardia.idTurno debe estar en Turno.idTurno  
Turno.idTurno puede no estar en Guardia.IdTurno

---

### Turno(idTurno, horario)  
CK \= PK \= {idTurno} 

---

### Atiende(codEspecialidad, idTurno)  
CK \= PK \= {(codEspecialidad, idTurno)}  
FK \= {codEspecialidad, idTurno} 

Atiende.codEspecialidad debe estar en Especialidad.codEspecialidad  
Atiende.idTurno debe estar en Turno.idTurno  
Especialidad.codEspecialidad debe estar en Atiende.codEspecialidad  
Turno.idTurno puede no estar en Atiende.idTurno

# Formas Normales

Persona( tipoDocumento, nroDocumento, nombre, apellido, tipo )  
CK \= PK \= { (tipoDocumento, nroDocumento) }

F \= { (tipoDocumento, nroDocumento) → nombre, (tipoDocumento, nroDocumento) → apellido,  
(tipoDocumento, nroDocumento) → tipo }

Verificó Fmin:

* **Descomponer** las dependencias para que cada una tenga **un solo atributo** en el lado derecho: Cumple  
* **Eliminar atributos redundantes del lado izquierdo** (probar si alguno puede quitarse sin alterar la equivalencia): Cumple  
* **Eliminar dependencias redundantes** (probar si alguna DF puede eliminarse sin cambiar la clausura total): Cumple

Entonces: Fmin \= F

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { tipoDocumento, nroDocumento}  
  * Atributos no primos: { nombre, apellido, tipo }  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * (tipoDocumento, nroDocumento) → nombre : X es superclave  
  * (tipoDocumento, nroDocumento) → apellido : X es superclave  
  * (tipoDocumento, nroDocumento) → tipo : X es superclave  
* FNBC: Todo determinante X es superclave: Cumple  
  * { (tipoDocumento, nroDocumento) } es superclave

Paciente( *tipoDocumento*, *nroDocumento*, fechaNacimiento, sexo )  
CK \= PK \= FK \= { (tipoDocumento, nroDocumento) }

Fmin \= { (tipoDocumento, nroDocumento) → fechaNacimiento,   
(tipoDocumento, nroDocumento) → sexo }

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { tipoDocumento, nroDocumento}  
  * Atributos no primos: { fechaNacimiento, sexo }  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * (tipoDocumento, nroDocumento) → fechaNacimiento: X es superclave  
  * (tipoDocumento, nroDocumento) → sexo: X es superclave  
* FNBC: Todo determinante X es superclave: Cumple  
  * { (tipoDocumento, nroDocumento) } es superclave

Medico( matricula, cuilcuit, fechaIngreso, foto, maxCantGuardia, periodoVacaciones, *tipoDocumento, nroDocumento )*  
CK \= { {matricula}, {cuilCuit}, (tipoDocumento, nroDocumento) }  
PK \= { matricula }  
FK \= { (tipoDocumento, nroDocumento) }

F \= {matricula → cuilcuit, matricula → fechaIngreso, matricula → foto, matricula → maxCantGuardia, matricula → periodoVacaciones, matricula → (tipoDocumento, nroDocumento), (tipoDocumento, nroDocumento) → matricula, cuilcuit → matricula}

Verificó Fmin:

* **Descomponer** las dependencias para que cada una tenga **un solo atributo** en el lado derecho: Cumple  
  * F’ \= {matricula → cuilcuit, matricula → fechaIngreso, matricula → foto, matricula → maxCantGuardia, matricula → periodoVacaciones, matricula → *tipoDocumento,* matricula → *nroDocumento*, (*tipoDocumento, nroDocumento*) → matricula, cuilcuit → matricula}  
* **Eliminar atributos redundantes del lado izquierdo** (probar si alguno puede quitarse sin alterar la equivalencia): Cumple  
* **Eliminar dependencias redundantes** (probar si alguna DF puede eliminarse sin cambiar la clausura total): Cumple

Entonces: Fmin \= F’

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { matricula, cuilCuit, tipoDocumento, nroDocumento }  
  * Atributos no primos: { fechaIngreso, foto, maxCantGuardia, periodoVacaciones }  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * matricula → cuilcuit: X es superclave y  A es atributo primo de R  
  * matricula → fechaIngreso: X es superclave  
  * matricula → foto: X es superclave  
  * matricula → maxCantGuardia: X es superclave  
  * (*tipoDocumento, nroDocumento*) → matricula: : X es superclave y  A es atributo primo de R  
  * cuilcuit → matricula: X es superclave y  A es atributo primo de R  
* FNBC: Todo determinante X es superclave: Cumple  
  * { matricula, cuilCuit, (tipoDocumento, nroDocumento) } son superclave

Especialidad( codEspecialidad, descripción, *idSector* )  
CK \= PK \= { codEspecialidad }  
FK \= { *idSector* }

F \= { codEspecialidad → descripción, codEspecialidad → idSector }

Verificó Fmin:

* **Descomponer** las dependencias para que cada una tenga **un solo atributo** en el lado derecho: Cumple  
* **Eliminar atributos redundantes del lado izquierdo** (probar si alguno puede quitarse sin alterar la equivalencia): Cumple  
* **Eliminar dependencias redundantes** (probar si alguna DF puede eliminarse sin cambiar la clausura total): Cumple

Entonces: Fmin \= F

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { codEspecialidad }  
  * Atributos no primos: { descripción, idSector  }  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * codEspecialidad → descripción: X es superclave  
  * codEspecialidad → idSector: X es superclave  
* FNBC: Todo determinante X es superclave: Cumple  
  * { codEspecialidad } es superclave

Se\_Especializa\_En( *matrícula*, *codEspecialidad*, haceGuardia )  
CK \= PK \= { (matricula, codEspecialidad) }  
FK \= { matricula, codEspecialidad }

F \= { (matricula, codEspecialidad) → haceGuardia }

Verificó Fmin:

* **Descomponer** las dependencias para que cada una tenga **un solo atributo** en el lado derecho: Cumple  
* **Eliminar atributos redundantes del lado izquierdo** (probar si alguno puede quitarse sin alterar la equivalencia): Cumple  
* **Eliminar dependencias redundantes** (probar si alguna DF puede eliminarse sin cambiar la clausura total): Cumple

Entonces: Fmin \= F

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { matricula, codEspecialidad }  
  * Atributos no primos: { haceGuardia }  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * (matricula, codEspecialidad) → haceGuardia: X es superclave  
* FNBC: Todo determinante X es superclave: Cumple  
  * { (matricula, codEspecialidad) } es superclave

Internacion( nroInternacion, fechaInicio, fechaFin, *tipoDocumento, nroDocumento, matricula* )  
CK \= PK \= { nroInternacion }  
FK \= { (tipoDocumento, nroDocumento),  matricula }

F \= { nroInternacion → fechaInicio, nroInternacion → fechaFin, nroInternacion → tipoDocumento, nroInternacion → nroDocumento, nroInternacion → matricula }

Verificó Fmin:

* **Descomponer** las dependencias para que cada una tenga **un solo atributo** en el lado derecho: Cumple  
* **Eliminar atributos redundantes del lado izquierdo** (probar si alguno puede quitarse sin alterar la equivalencia): Cumple  
* **Eliminar dependencias redundantes** (probar si alguna DF puede eliminarse sin cambiar la clausura total): Cumple

Entonces: Fmin \= F

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { nroInternacion }  
  * Atributos no primos: { fechaInicio, fechaFin, tipoDocumento, nroDocumento, matricula}  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * nroInternacion → fechaInicio: X es superclave  
  * nroInternacion → fechaFin: X es superclave  
  * nroInternacion → tipoDocumento: X es superclave  
  * nroInternacion → nroDocumento: X es superclave  
  * nroInternacion → matricula: X es superclave  
* FNBC: Todo determinante X es superclave: Cumple  
  * { nroInternacion } es superclave

Se\_Ubica( *nroInternacion*, fechaYHoraDeIngreso, *nroCama*, *nroHabitacion* )  
CK \= PK \= {(nroInternacion, fechaYHoraDeIngreso), (nroCama, nroHabitacion, fechaYHoraDeIngreso)}  
FK \= { nroInternacion, (nroCama, nroHabitacion) }

F \= { (nroCama, nroHabitacion, fechaYHoraDeIngreso) → nroInternacion,   
(nroInternacion, fechaYHoraDeIngreso) → (nroCama, nroHabitacion) }

Notas:

* nroInternacion → nroCama : No es correcta ya que una internación puede cambiar de cama varias veces. No hay una única nroCama por nroInternacion  
* nroInternacion → nroHabitacion: Idem la anterior (al mover la cama podés cambiar de habitación)  
* (nroInternacion, nroCama, nroHabitacion) → fechaYHoraDeIngreso: No es correcta ya que esto diría que para una internación y una cama dadas hay una sola fecha/hora de ingreso, pero podría volver a la misma cama más adelante (otro movimiento). Por eso no es válido

Verificó Fmin:

* **Descomponer** las dependencias para que cada una tenga **un solo atributo** en el lado derecho: Cumple  
  * F’ \= { (nroCama, nroHabitacion, fechaYHoraDeIngreso) → nroInternacion, (nroInternacion, fechaYHoraDeIngreso) → nroCama, (nroInternacion, fechaYHoraDeIngreso) → nroHabitacion }  
* **Eliminar atributos redundantes del lado izquierdo** (probar si alguno puede quitarse sin alterar la equivalencia): Cumple  
* **Eliminar dependencias redundantes** (probar si alguna DF puede eliminarse sin cambiar la clausura total): Cumple

Entonces: Fmin \= F’

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { nroInternacion, fechaYHoraDeIngreso, nroCama, nroHabitacion }  
  * Atributos no primos: { Vacío }  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * (nroCama, nroHabitacion, fechaYHoraDeIngreso) → nroInternacion: X es superclave  
  * (nroInternacion, fechaYHoraDeIngreso) → nroCama: X es superclave  
  * (nroInternacion, fechaYHoraDeIngreso) → nroHabitacion: X es superclave  
* FNBC: Todo determinante X es superclave: Cumple  
  * { (nroCama, nroHabitacion, fechaYHoraDeIngreso), (nroInternacion, fechaYHoraDeIngreso) } son superclaves

Cama( *nroHabitacion*, nroCama, estado )  
CK \= PK \= { (nroCama, nroHabitacion) }  
FK \= { nroHabitacion }

F \= { (nroCama, nroHabitacion) → estado }

Verificó Fmin:

* **Descomponer** las dependencias para que cada una tenga **un solo atributo** en el lado derecho: Cumple  
* **Eliminar atributos redundantes del lado izquierdo** (probar si alguno puede quitarse sin alterar la equivalencia): Cumple  
* **Eliminar dependencias redundantes** (probar si alguna DF puede eliminarse sin cambiar la clausura total): Cumple

Entonces: Fmin \= F

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { nroCama, nroHabitacion }  
  * Atributos no primos: { estado }  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * (nroCama, nroHabitacion) → estado : X es superclave  
* FNBC: Todo determinante X es superclave: Cumple  
  * { (nroCama, nroHabitacion) } es superclave

Habitación( nroHabitacion, piso, orientación, *idSector* )  
CK \= PK \= { nroHabitacion }   
FK \= { idSector }

F \= { nroHabitacion → piso, nroHabitacion → orientación, nroHabitacion → idSector }

Verificó Fmin:

* **Descomponer** las dependencias para que cada una tenga **un solo atributo** en el lado derecho: Cumple  
* **Eliminar atributos redundantes del lado izquierdo** (probar si alguno puede quitarse sin alterar la equivalencia): Cumple  
* **Eliminar dependencias redundantes** (probar si alguna DF puede eliminarse sin cambiar la clausura total): Cumple

Entonces: Fmin \= F

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { nroHabitacion }  
  * Atributos no primos: { piso, orientación, idSector }  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * nroHabitacion → piso: X es superclave  
  * nroHabitacion → orientación: X es superclave  
  * nroHabitacion → idSector: X es superclave  
* FNBC: Todo determinante X es superclave: Cumple  
  * { nroHabitacion } es superclave

Sector( idSector, descripción )  
CK \= PK \= { idSector }

F \= { idSector → descripción }

Verificó Fmin:

* **Descomponer** las dependencias para que cada una tenga **un solo atributo** en el lado derecho: Cumple  
* **Eliminar atributos redundantes del lado izquierdo** (probar si alguno puede quitarse sin alterar la equivalencia): Cumple  
* **Eliminar dependencias redundantes** (probar si alguna DF puede eliminarse sin cambiar la clausura total): Cumple

Entonces: Fmin \= F

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { idSector }  
  * Atributos no primos: { descripción }  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * idSector → descripción: X es superclave  
* FNBC: Todo determinante X es superclave: Cumple  
  * { idSector } es superclave

Ronda( idRonda, diaDeLaSemana, turno )  
CK \= PK \= { idRonda } 

F \= { idRonda → diaDeLaSemana, idRonda→ turno}

Verificó Fmin:

* **Descomponer** las dependencias para que cada una tenga **un solo atributo** en el lado derecho: Cumple  
* **Eliminar atributos redundantes del lado izquierdo** (probar si alguno puede quitarse sin alterar la equivalencia): Cumple  
* **Eliminar dependencias redundantes** (probar si alguna DF puede eliminarse sin cambiar la clausura total): Cumple

Entonces: Fmin \= F

Verificó FN:

* 1FN: Todos los atributos son atómicos: Cumple  
* 2FN: Cada atributo NO primo NO es parcialmente dependiente de alguna clave de R (dependencia total): Cumple  
  * Atributos primos: { idRonda }  
  * Atributos no primos: { diaDeLaSemana, turno }  
* 3FN: Si para cada DF X → A se cumple: a) X es superclave de R, o b) A es atributo primo de R : Cumple  
  * idRonda → diaDeLaSemana: X es superclave  
  * idRonda → turno: X es superclave  
* FNBC: Todo determinante X es superclave: Cumple  
  * { idRonda } es superclave



## Conclusión

En este trabajo práctico se desarrolló un diseño de base de datos a partir de una problemática extraída del mundo real utilizando las herramientas   
conceptuales y procedimentales vistas en clase. A partir del análisis de los requerimientos y la identificación de las dependencias funcionales, se aplicaron las reglas de normalización correspondientes hasta alcanzar la Forma Normal de Boyce-Codd (FNBC). Este nivel de normalización asegura la eliminación de redundancias y anomalías de actualización, inserción y eliminación, permitiendo que la estructura final sea robusta y escalable frente a posibles modificaciones futuras.

Asimismo, la implementación del modelo relacional resultante facilita consultas más claras y precisas, manteniendo la coherencia de los datos y optimizando su administración. En conclusión, el proyecto cumple adecuadamente con los objetivos planteados, logrando una base de datos correctamente normalizada y alineada con buenas prácticas de diseño.  
