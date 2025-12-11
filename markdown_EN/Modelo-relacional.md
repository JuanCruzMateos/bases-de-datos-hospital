# **Databases** | FI UNMdP
Spanish version available in [../markdown_ES/Modelo-relacional.md](../markdown_ES/Modelo-relacional.md).

# *Computer Engineering*

**Project developed by:**
- **Mateos, Juan Cruz**
- **San Pedro, Gianfranco**

# Relational Model

**Note**: An attribute in *italics* means it is an FK. We adopted this notation to simplify writing the RM.

### Persona(tipoDocumento, nroDocumento, nombre, apellido, tipo)  
CK = PK = {(tipoDocumento, nroDocumento)}  
FK = {}

---

### Paciente(*tipoDocumento*, *nroDocumento*, fechaNacimiento, sexo)  
CK = PK = FK = {(tipoDocumento, nroDocumento)}

Paciente.(tipoDocumento, nrodocumento) must be in Persona.(tipoDocumento, nrodocumento)  
Persona.(tipoDocumento, nrodocumento) may not be in Paciente.(tipoDocumento, nrodocumento)

---

### Medico(matricula, cuilcuit, fechaIngreso, foto, maxCantGuardia, *tipoDocumento, nroDocumento)*  
CK = { (tipoDocumento, nroDocumento), matricula, {cuilCuit} }  
PK = {matricula}  
FK = {(tipoDocumento, nroDocumento)}

Medico.(tipoDocumento, nrodocumento) must be in Persona.(tipoDocumento, nrodocumento)  
Persona.(tipoDocumento, nrodocumento) must be in Medico.(tipoDocumento, nrodocumento)

---

### Especialidad(codEspecialidad, descripción, *idSector*)  
CK = PK = {codEspecialidad}  
FK = {*idSector*}

Especialidad.idSector must be in Sector.idSector  
Sector.idSector must be in Especialidad.idSector

---

### Se_Especializa_En(*matricula*, *codEspecialidad*, haceGuardia)  
CK = PK = {(matricula, codEspecialidad)}  
FK = {matricula, codEspecialidad}

Medico.matricula must be in Se_Especializa_En.matricula  
Especialidad.codEspecialidad may not be in Se_Especializa_En.codEspecialidad  
Se_Especializa_En.matricula must be in Medico.matricula  
Se_Especializa_En.codEspecialidad must be in Especialidad.codEspeciadlidad

---

### Internacion(nroInternacion, fechaInicio, fechaFin, *tipoDocumento, nroDocumento, matricula*)  
CK = PK = {nroInternacion}  
FK = {(tipoDocumento, nroDocumento),  matricula}

Internacion.(tipoDocumento, nrodocumento) must be in Paciente.(tipoDocumento, nrodocumento)  
Paciente.(tipoDocumento, nrodocumento) may not be in Internacion.(tipoDocumento, nrodocumento)  
Internacion.matricula must be in Medico.matricula  
Medico.matricula may not be in Internacion.matricula

---

### Se_Ubica(*nroInternacion*, *nroCama*, *nroHabitacion*,fechaYHoraDeIngreso)  
CK = PK = {(nroInternacion, nroCama, nroHabitacion, fechaYHoraDeIngreso)}  
FK = {nroInternacion, (nroCama, nroHabitacion)}

Internacion.nroInternacion must be in Se_Ubica.nroInternacion  
Se_Ubica.nroInternacion must be in Internacion.nroInternacion  
Cama.(nroInternacion, nroCama) must be in Se_Ubica.(nroInternacion, nroCama)  
Cama(*nroHabitacion*, nroCama, estado)  
CK = PK = {(nroCama, nroHabitacion)}  
FK = {nroHabitacion}

Cama.nroHabitacion must be in Habitacion.nroHabitacion  
Habitacion.nroHabitacion must be in Cama.nroHabitacion

---

### Habitación(nroHabitacion, piso, orientación, *idSector*)  
CK = PK = {nroHabitacion}   
FK = {idSector}

Habitacion.idSector must be in Sector.idSector  
Sector.idSector must be in Habitacion.idSector

---

### Sector(idSector, descripción)  
CK = PK = {idSector}

---

### Ronda(idRonda, diaDeLaSemana, turno)  
CK = PK = {idRonda} 

---

### Visita(*idRonda*, *nroHabitacion*)  
CK = PK = {(idRonda, nroHabitacion)}  
FK = {idRonda, nroHabitacion}

Ronda.idRonda must be in Visita.idRonda  
Habitacion.nroHabitacion must be in Visita.nroHabitacion  
Visita.nroHabitacion must be in Habitacion.nroHabitacion  
Visita.idRonda must be in Ronda.idRonda

---

### Recorrido(idRecorrido, fechaDeRecorrido, horaInicio, horaFin, *idRonda, matricula*)  
CK = PK = {idRecorrido}  
FK = {idRonda, matricula}

Recorrido.idRonda must be in Ronda.idRonda  
Recorrido.matricula must be in Medico.matricula  
Ronda.idRonda may not be in Recorrido.idRonda  
Medico.matriculal may not be in Recorrido.matricula

---

### Comenta_Sobre(*idRecorrido, nroInternacion*, comentario)  
CK = PK = {(idRecorrido, nroInternacion)}  
FK = {idRecorrido, nroInternacion}

Recorrido.idRecorrido must be in Comenta_Sobre.idRecorrido  
Internacion.nroInternacion may not be in Comenta_Sobre.nroInternacion  
Comenta_Sobre.idRecorrido must be in Recorrido.idRecorrido  
Comenta_Sobre.nroInternacion must be in Internacion.nroInternacion

---

### Guardia(nroGuardia, fechaYHora, *matricula, codEspecialidad, idTurno*)  
CK = PK = {nroGuardia}   
FK = {(matrícula, codEspecialidad), idTurno}

Guardia.(matrícula ,codEspecialidad) must be in Se_Especializia_En.(matrícula, codEspecialidad)  
Se_Especializia_En.(matrícula, codEspecialidad) may not be in Guardia.(matrícula, codEspecialidad)  
Guardia.idTurno must be in Turno.idTurno  
Turno.idTurno may not be in Guardia.IdTurno

---

### Turno(idTurno, horario)  
CK = PK = {idTurno} 

---

### Atiende(codEspecialidad, idTurno)  
CK = PK = {(codEspecialidad, idTurno)}  
FK = {codEspecialidad, idTurno} 

Atiende.codEspecialidad must be in Especialidad.codEspecialidad  
Atiende.idTurno must be in Turno.idTurno  
Especialidad.codEspecialidad must be in Atiende.codEspecialidad  
Turno.idTurno may not be in Atiende.idTurno

---

### Vacaciones(*matricula*, fechaInicio, fechaFin)  
CK = PK = {(matricula, fechaInicio, fechaFin)}  
FK = {matricula}

Vacaciones.matricula must be in Medico.matricula  
Medico.matricula must be in Vacaciones.matricula

---

# Normal forms

Persona( tipoDocumento, nroDocumento, nombre, apellido, tipo )  
CK = PK = { (tipoDocumento, nroDocumento) }

F = { (tipoDocumento, nroDocumento) → nombre, (tipoDocumento, nroDocumento) → apellido,  
(tipoDocumento, nroDocumento) → tipo }

Fmin verified:

* **Decompose** dependencies so each one has **a single attribute** on the right side: Pass  
* **Remove redundant attributes on the left side** (test if any can be removed without altering equivalence): Pass  
* **Remove redundant dependencies** (test if any FD can be removed without changing total closure): Pass

Therefore: Fmin = F

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { tipoDocumento, nroDocumento}  
  * Non-prime attributes: { nombre, apellido, tipo }  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * (tipoDocumento, nroDocumento) → nombre : X is superkey  
  * (tipoDocumento, nroDocumento) → apellido : X is superkey  
  * (tipoDocumento, nroDocumento) → tipo : X is superkey  
* BCNF: Every determinant X is a superkey: Pass  
  * { (tipoDocumento, nroDocumento) } is superkey

Paciente( *tipoDocumento*, *nroDocumento*, fechaNacimiento, sexo )  
CK = PK = FK = { (tipoDocumento, nroDocumento) }

Fmin = { (tipoDocumento, nroDocumento) → fechaNacimiento,   
(tipoDocumento, nroDocumento) → sexo }

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { tipoDocumento, nroDocumento}  
  * Non-prime attributes: { fechaNacimiento, sexo }  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * (tipoDocumento, nroDocumento) → fechaNacimiento: X is superkey  
  * (tipoDocumento, nroDocumento) → sexo: X is superkey  
* BCNF: Every determinant X is a superkey: Pass  
  * { (tipoDocumento, nroDocumento) } is superkey

Medico( matricula, cuilcuit, fechaIngreso, foto, maxCantGuardia, *tipoDocumento, nroDocumento )*  
CK = { {matricula}, {cuilCuit}, (tipoDocumento, nroDocumento) }  
PK = { matricula }  
FK = { (tipoDocumento, nroDocumento) }

F = {matricula → cuilcuit, matricula → fechaIngreso, matricula → foto, matricula → maxCantGuardia, matricula → (tipoDocumento, nroDocumento), (tipoDocumento, nroDocumento) → matricula, cuilcuit → matricula}

Fmin verified:

* **Decompose** dependencies so each one has **a single attribute** on the right side: Pass  
  * F’ = {matricula → cuilcuit, matricula → fechaIngreso, matricula → foto, matricula → maxCantGuardia, , matricula → *tipoDocumento,* matricula → *nroDocumento*, (*tipoDocumento, nroDocumento*) → matricula, cuilcuit → matricula}  
* **Remove redundant attributes on the left side** (test if any can be removed without altering equivalence): Pass  
* **Remove redundant dependencies** (test if any FD can be removed without changing total closure): Pass

Therefore: Fmin = F’

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { matricula, cuilCuit, tipoDocumento, nroDocumento }  
  * Non-prime attributes: { fechaIngreso, foto, maxCantGuardia}  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * matricula → cuilcuit: X is superkey and A is a prime attribute of R  
  * matricula → fechaIngreso: X is superkey  
  * matricula → foto: X is superkey  
  * matricula → maxCantGuardia: X is superkey  
  * (*tipoDocumento, nroDocumento*) → matricula: X is superkey and A is a prime attribute of R  
  * cuilcuit → matricula: X is superkey and A is a prime attribute of R  
* BCNF: Every determinant X is a superkey: Pass  
  * { matricula, cuilCuit, (tipoDocumento, nroDocumento) } are superkey

Especialidad( codEspecialidad, descripción, *idSector* )  
CK = PK = { codEspecialidad }  
FK = { *idSector* }

F = { codEspecialidad → descripción, codEspecialidad → idSector }

Fmin verified:

* **Decompose** dependencies so each one has **a single attribute** on the right side: Pass  
* **Remove redundant attributes on the left side** (test if any can be removed without altering equivalence): Pass  
* **Remove redundant dependencies** (test if any FD can be removed without changing total closure): Pass

Therefore: Fmin = F

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { codEspecialidad }  
  * Non-prime attributes: { descripción, idSector  }  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * codEspecialidad → descripción: X is superkey  
  * codEspecialidad → idSector: X is superkey  
* BCNF: Every determinant X is a superkey: Pass  
  * { codEspecialidad } is superkey

Se_Especializa_En( *matrícula*, *codEspecialidad*, haceGuardia )  
CK = PK = { (matricula, codEspecialidad) }  
FK = { matricula, codEspecialidad }

F = { (matricula, codEspecialidad) → haceGuardia }

Fmin verified:

* **Decompose** dependencies so each one has **a single attribute** on the right side: Pass  
* **Remove redundant attributes on the left side** (test if any can be removed without altering equivalence): Pass  
* **Remove redundant dependencies** (test if any FD can be removed without changing total closure): Pass

Therefore: Fmin = F

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { matricula, codEspecialidad }  
  * Non-prime attributes: { haceGuardia }  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * (matricula, codEspecialidad) → haceGuardia: X is superkey  
* BCNF: Every determinant X is a superkey: Pass  
  * { (matricula, codEspecialidad) } is superkey

Internacion( nroInternacion, fechaInicio, fechaFin, *tipoDocumento, nroDocumento, matricula* )  
CK = PK = { nroInternacion }  
FK = { (tipoDocumento, nroDocumento),  matricula }

F = { nroInternacion → fechaInicio, nroInternacion → fechaFin, nroInternacion → tipoDocumento, nroInternacion → nroDocumento, nroInternacion → matricula }

Fmin verified:

* **Decompose** dependencies so each one has **a single attribute** on the right side: Pass  
* **Remove redundant attributes on the left side** (test if any can be removed without altering equivalence): Pass  
* **Remove redundant dependencies** (test if any FD can be removed without changing total closure): Pass

Therefore: Fmin = F

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { nroInternacion }  
  * Non-prime attributes: { fechaInicio, fechaFin, tipoDocumento, nroDocumento, matricula}  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * nroInternacion → fechaInicio: X is superkey  
  * nroInternacion → fechaFin: X is superkey  
  * nroInternacion → tipoDocumento: X is superkey  
  * nroInternacion → nroDocumento: X is superkey  
  * nroInternacion → matricula: X is superkey  
* BCNF: Every determinant X is a superkey: Pass  
  * { nroInternacion } is superkey

Se_Ubica( *nroInternacion*, fechaYHoraDeIngreso, *nroCama*, *nroHabitacion* )  
CK = PK = {(nroInternacion, fechaYHoraDeIngreso), (nroCama, nroHabitacion, fechaYHoraDeIngreso)}  
FK = { nroInternacion, (nroCama, nroHabitacion) }

F = { (nroCama, nroHabitacion, fechaYHoraDeIngreso) → nroInternacion,   
(nroInternacion, fechaYHoraDeIngreso) → (nroCama, nroHabitacion) }

Notes:

* nroInternacion → nroCama : Not correct because an internment can change beds multiple times. There is not a single nroCama per nroInternacion  
* nroInternacion → nroHabitacion: Same as above (changing bed can change the room)  
* (nroInternacion, nroCama, nroHabitacion) → fechaYHoraDeIngreso: Not correct because this would say that for a given internment and bed there is a single admission timestamp, but it could return to the same bed later (another move). Therefore not valid

Fmin verified:

* **Decompose** dependencies so each one has **a single attribute** on the right side: Pass  
  * F’ = { (nroCama, nroHabitacion, fechaYHoraDeIngreso) → nroInternacion, (nroInternacion, fechaYHoraDeIngreso) → nroCama, (nroInternacion, fechaYHoraDeIngreso) → nroHabitacion }  
* **Remove redundant attributes on the left side** (test if any can be removed without altering equivalence): Pass  
* **Remove redundant dependencies** (test if any FD can be removed without changing total closure): Pass

Therefore: Fmin = F’

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { nroInternacion, fechaYHoraDeIngreso, nroCama, nroHabitacion }  
  * Non-prime attributes: { Empty }  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * (nroCama, nroHabitacion, fechaYHoraDeIngreso) → nroInternacion: X is superkey  
  * (nroInternacion, fechaYHoraDeIngreso) → nroCama: X is superkey  
  * (nroInternacion, fechaYHoraDeIngreso) → nroHabitacion: X is superkey  
* BCNF: Every determinant X is a superkey: Pass  
  * { (nroCama, nroHabitacion, fechaYHoraDeIngreso), (nroInternacion, fechaYHoraDeIngreso) } are superkeys

Cama( *nroHabitacion*, nroCama, estado )  
CK = PK = { (nroCama, nroHabitacion) }  
FK = { nroHabitacion }

F = { (nroCama, nroHabitacion) → estado }

Fmin verified:

* **Decompose** dependencies so each one has **a single attribute** on the right side: Pass  
* **Remove redundant attributes on the left side** (test if any can be removed without altering equivalence): Pass  
* **Remove redundant dependencies** (test if any FD can be removed without changing total closure): Pass

Therefore: Fmin = F

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { nroCama, nroHabitacion }  
  * Non-prime attributes: { estado }  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * (nroCama, nroHabitacion) → estado : X is superkey  
* BCNF: Every determinant X is a superkey: Pass  
  * { (nroCama, nroHabitacion) } is superkey

Habitación( nroHabitacion, piso, orientación, *idSector* )  
CK = PK = { nroHabitacion }   
FK = { idSector }

F = { nroHabitacion → piso, nroHabitacion → orientación, nroHabitacion → idSector }

Fmin verified:

* **Decompose** dependencies so each one has **a single attribute** on the right side: Pass  
* **Remove redundant attributes on the left side** (test if any can be removed without altering equivalence): Pass  
* **Remove redundant dependencies** (test if any FD can be removed without changing total closure): Pass

Therefore: Fmin = F

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { nroHabitacion }  
  * Non-prime attributes: { piso, orientación, idSector }  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * nroHabitacion → piso: X is superkey  
  * nroHabitacion → orientación: X is superkey  
  * nroHabitacion → idSector: X is superkey  
* BCNF: Every determinant X is a superkey: Pass  
  * { nroHabitacion } is superkey

Sector( idSector, descripción )  
CK = PK = { idSector }

F = { idSector → descripción }

Fmin verified:

* **Decompose** dependencies so each one has **a single attribute** on the right side: Pass  
* **Remove redundant attributes on the left side** (test if any can be removed without altering equivalence): Pass  
* **Remove redundant dependencies** (test if any FD can be removed without changing total closure): Pass

Therefore: Fmin = F

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { idSector }  
  * Non-prime attributes: { descripción }  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * idSector → descripción: X is superkey  
* BCNF: Every determinant X is a superkey: Pass  
  * { idSector } is superkey

Ronda( idRonda, diaDeLaSemana, turno )  
CK = PK = { idRonda } 

F = { idRonda → diaDeLaSemana, idRonda→ turno}

Fmin verified:

* **Decompose** dependencies so each one has **a single attribute** on the right side: Pass  
* **Remove redundant attributes on the left side** (test if any can be removed without altering equivalence): Pass  
* **Remove redundant dependencies** (test if any FD can be removed without changing total closure): Pass

Therefore: Fmin = F

NF verified:

* 1NF: All attributes are atomic: Pass  
* 2NF: Each NON-prime attribute is not partially dependent on any key of R (total dependency): Pass  
  * Prime attributes: { idRonda }  
  * Non-prime attributes: { diaDeLaSemana, turno }  
* 3NF: For every FD X → A: a) X is a superkey of R, or b) A is a prime attribute of R : Pass  
  * idRonda → diaDeLaSemana: X is superkey  
  * idRonda → turno: X is superkey  
* BCNF: Every determinant X is a superkey: Pass  
  * { idRonda } is superkey



## Conclusion

In this practical assignment, a database design was developed based on a real-world problem using the conceptual and procedural tools covered in class. From the requirements analysis and the identification of functional dependencies, the corresponding normalization rules were applied until reaching Boyce-Codd Normal Form (BCNF). This level of normalization ensures the elimination of redundancies and update, insert, and delete anomalies, allowing the final structure to be robust and scalable for potential future changes.

Likewise, the implementation of the resulting relational model facilitates clearer and more precise queries, maintaining data consistency and optimizing its management. In conclusion, the project adequately meets the objectives set, achieving a properly normalized database aligned with good design practices.  

