package org.hospital.feature.internacion.domain;

/**
 * DTO for available beds detail by sector.
 * Maps to sp_camas_disponibles_detalle result.
 */
public class CamaDisponibleDetalle {
    private int idSector;
    private String descripcion;
    private int nroHabitacion;
    private int piso;
    private String orientacion;
    private int nroCama;
    private String estado;

    public CamaDisponibleDetalle() {
    }

    public CamaDisponibleDetalle(int idSector, String descripcion, int nroHabitacion, 
                                  int piso, String orientacion, int nroCama, String estado) {
        this.idSector = idSector;
        this.descripcion = descripcion;
        this.nroHabitacion = nroHabitacion;
        this.piso = piso;
        this.orientacion = orientacion;
        this.nroCama = nroCama;
        this.estado = estado;
    }

    public int getIdSector() {
        return idSector;
    }

    public void setIdSector(int idSector) {
        this.idSector = idSector;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getNroHabitacion() {
        return nroHabitacion;
    }

    public void setNroHabitacion(int nroHabitacion) {
        this.nroHabitacion = nroHabitacion;
    }

    public int getPiso() {
        return piso;
    }

    public void setPiso(int piso) {
        this.piso = piso;
    }

    public String getOrientacion() {
        return orientacion;
    }

    public void setOrientacion(String orientacion) {
        this.orientacion = orientacion;
    }

    public int getNroCama() {
        return nroCama;
    }

    public void setNroCama(int nroCama) {
        this.nroCama = nroCama;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "CamaDisponibleDetalle{" +
                "idSector=" + idSector +
                ", descripcion='" + descripcion + '\'' +
                ", nroHabitacion=" + nroHabitacion +
                ", piso=" + piso +
                ", orientacion='" + orientacion + '\'' +
                ", nroCama=" + nroCama +
                ", estado='" + estado + '\'' +
                '}';
    }
}

