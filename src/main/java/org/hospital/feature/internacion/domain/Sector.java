package org.hospital.feature.internacion.domain;

import java.util.Objects;

public class Sector {
    private int idSector;
    private String descripcion;

    public Sector() {
    }

    public Sector(int idSector, String descripcion) {
        this.idSector = idSector;
        this.descripcion = descripcion;
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

    @Override
    public String toString() {
        return "Sector [idSector=" + idSector + ", descripcion=" + descripcion + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Sector sector = (Sector) obj;
        return idSector == sector.idSector && Objects.equals(descripcion, sector.descripcion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSector, descripcion);
    }
}
