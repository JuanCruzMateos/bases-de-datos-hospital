package org.hospital.feature.guardia.domain;

import java.util.Objects;

public class Turno {
    private int idTurno;
    private String horario;

    public Turno() {
    }

    public Turno(int idTurno, String horario) {
        this.idTurno = idTurno;
        this.horario = horario;
    }

    public int getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(int idTurno) {
        this.idTurno = idTurno;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    @Override
    public String toString() {
        return "Turno [idTurno=" + idTurno + ", horario=" + horario + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Turno turno = (Turno) obj;
        return idTurno == turno.idTurno && Objects.equals(horario, turno.horario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTurno, horario);
    }
}
