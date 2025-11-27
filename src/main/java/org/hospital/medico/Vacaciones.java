package org.hospital.medico;

import java.time.LocalDate;
import java.util.Objects;

public class Vacaciones {
    private long matricula;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    
    // Optional: Medico reference for convenience
    private Medico medico;

    public Vacaciones() {
    }

    public Vacaciones(long matricula, LocalDate fechaInicio, LocalDate fechaFin) {
        this.matricula = matricula;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public long getMatricula() {
        return matricula;
    }

    public void setMatricula(long matricula) {
        this.matricula = matricula;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Vacaciones that = (Vacaciones) obj;
        return matricula == that.matricula &&
                Objects.equals(fechaInicio, that.fechaInicio) &&
                Objects.equals(fechaFin, that.fechaFin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matricula, fechaInicio, fechaFin);
    }

    @Override
    public String toString() {
        return "Vacaciones{" +
                "matricula=" + matricula +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                '}';
    }
}

