package org.hospital.feature.internacion.domain;

/**
 * DTO for available beds summary by sector.
 * Maps to sp_camas_disponibles_resumen result.
 */
public class CamaDisponibleResumen {
    private int idSector;
    private String descripcion;
    private int camasLibres;

    public CamaDisponibleResumen() {
    }

    public CamaDisponibleResumen(int idSector, String descripcion, int camasLibres) {
        this.idSector = idSector;
        this.descripcion = descripcion;
        this.camasLibres = camasLibres;
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

    public int getCamasLibres() {
        return camasLibres;
    }

    public void setCamasLibres(int camasLibres) {
        this.camasLibres = camasLibres;
    }

    @Override
    public String toString() {
        return "CamaDisponibleResumen{" +
                "idSector=" + idSector +
                ", descripcion='" + descripcion + '\'' +
                ", camasLibres=" + camasLibres +
                '}';
    }
}

