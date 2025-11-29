package org.hospital.feature.internacion.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Internacion {
    private int nroInternacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipoDocumento;
    private String nroDocumento;
    private long matricula;

    public Internacion() {
    }

    public Internacion(int nroInternacion, LocalDate fechaInicio, LocalDate fechaFin, 
                       String tipoDocumento, String nroDocumento, long matricula) {
        this.nroInternacion = nroInternacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipoDocumento = tipoDocumento;
        this.nroDocumento = nroDocumento;
        this.matricula = matricula;
    }

    public int getNroInternacion() {
        return nroInternacion;
    }

    public void setNroInternacion(int nroInternacion) {
        this.nroInternacion = nroInternacion;
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

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNroDocumento() {
        return nroDocumento;
    }

    public void setNroDocumento(String nroDocumento) {
        this.nroDocumento = nroDocumento;
    }

    public long getMatricula() {
        return matricula;
    }

    public void setMatricula(long matricula) {
        this.matricula = matricula;
    }

    @Override
    public String toString() {
        return "Internacion [nroInternacion=" + nroInternacion + ", fechaInicio=" + fechaInicio + 
               ", fechaFin=" + fechaFin + ", tipoDocumento=" + tipoDocumento + 
               ", nroDocumento=" + nroDocumento + ", matricula=" + matricula + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Internacion internacion = (Internacion) obj;
        return nroInternacion == internacion.nroInternacion && matricula == internacion.matricula &&
               Objects.equals(fechaInicio, internacion.fechaInicio) && 
               Objects.equals(fechaFin, internacion.fechaFin) &&
               Objects.equals(tipoDocumento, internacion.tipoDocumento) &&
               Objects.equals(nroDocumento, internacion.nroDocumento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nroInternacion, fechaInicio, fechaFin, tipoDocumento, nroDocumento, matricula);
    }
}
