package org.hospital.feature.guardia.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Guardia {
    private int nroGuardia;
    private LocalDateTime fechaHora;
    private long matricula;
    private int codEspecialidad;
    private int idTurno;

    public Guardia() {
    }

    public Guardia(int nroGuardia, LocalDateTime fechaHora, long matricula, 
                   int codEspecialidad, int idTurno) {
        this.nroGuardia = nroGuardia;
        this.fechaHora = fechaHora;
        this.matricula = matricula;
        this.codEspecialidad = codEspecialidad;
        this.idTurno = idTurno;
    }

    public int getNroGuardia() {
        return nroGuardia;
    }

    public void setNroGuardia(int nroGuardia) {
        this.nroGuardia = nroGuardia;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public long getMatricula() {
        return matricula;
    }

    public void setMatricula(long matricula) {
        this.matricula = matricula;
    }

    public int getCodEspecialidad() {
        return codEspecialidad;
    }

    public void setCodEspecialidad(int codEspecialidad) {
        this.codEspecialidad = codEspecialidad;
    }

    public int getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(int idTurno) {
        this.idTurno = idTurno;
    }

    @Override
    public String toString() {
        return "Guardia [nroGuardia=" + nroGuardia + ", fechaHora=" + fechaHora + 
               ", matricula=" + matricula + ", codEspecialidad=" + codEspecialidad + 
               ", idTurno=" + idTurno + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Guardia guardia = (Guardia) obj;
        return nroGuardia == guardia.nroGuardia && matricula == guardia.matricula &&
               codEspecialidad == guardia.codEspecialidad && idTurno == guardia.idTurno &&
               Objects.equals(fechaHora, guardia.fechaHora);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nroGuardia, fechaHora, matricula, codEspecialidad, idTurno);
    }
}
