package org.hospital.feature.internacion.domain;

import java.sql.Date;

/**
 * DTO for patient internations.
 * Maps to sp_internaciones_paciente result.
 */
public class InternacionPaciente {
    private int nroInternacion;
    private Date fechaInicio;
    private Date fechaFin;
    private String estado;

    public InternacionPaciente() {
    }

    public InternacionPaciente(int nroInternacion, Date fechaInicio, Date fechaFin, String estado) {
        this.nroInternacion = nroInternacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getNroInternacion() {
        return nroInternacion;
    }

    public void setNroInternacion(int nroInternacion) {
        this.nroInternacion = nroInternacion;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "InternacionPaciente{" +
                "nroInternacion=" + nroInternacion +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", estado='" + estado + '\'' +
                '}';
    }
}

