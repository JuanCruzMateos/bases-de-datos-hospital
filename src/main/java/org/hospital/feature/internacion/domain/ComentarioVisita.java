package org.hospital.feature.internacion.domain;

import java.sql.Date;
import java.sql.Time;

/**
 * DTO for visit comments.
 * Maps to sp_comentarios_visitas result.
 */
public class ComentarioVisita {
    private int nroInternacion;
    private String paciente;
    private String medico;
    private Date fechaRecorrido;
    private Time horaInicio;
    private Time horaFin;
    private String comentario;

    public ComentarioVisita() {
    }

    public ComentarioVisita(int nroInternacion, String paciente, String medico,
                            Date fechaRecorrido, Time horaInicio, Time horaFin, String comentario) {
        this.nroInternacion = nroInternacion;
        this.paciente = paciente;
        this.medico = medico;
        this.fechaRecorrido = fechaRecorrido;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.comentario = comentario;
    }

    public int getNroInternacion() {
        return nroInternacion;
    }

    public void setNroInternacion(int nroInternacion) {
        this.nroInternacion = nroInternacion;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public Date getFechaRecorrido() {
        return fechaRecorrido;
    }

    public void setFechaRecorrido(Date fechaRecorrido) {
        this.fechaRecorrido = fechaRecorrido;
    }

    public Time getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Time horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Time getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(Time horaFin) {
        this.horaFin = horaFin;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    @Override
    public String toString() {
        return "ComentarioVisita{" +
                "nroInternacion=" + nroInternacion +
                ", paciente='" + paciente + '\'' +
                ", medico='" + medico + '\'' +
                ", fechaRecorrido=" + fechaRecorrido +
                ", horaInicio=" + horaInicio +
                ", horaFin=" + horaFin +
                ", comentario='" + comentario + '\'' +
                '}';
    }
}

