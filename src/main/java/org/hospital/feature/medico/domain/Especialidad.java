package org.hospital.feature.medico.domain;

import java.util.Objects;

public class Especialidad {
    private int codEspecialidad;
    private String descripcion;
    private int idSector;

    public Especialidad(int codEspecialidad, String descripcion, int idSector) {
        this.codEspecialidad = codEspecialidad;
        this.descripcion = descripcion;
        this.idSector = idSector;
    }

    public int getCodEspecialidad() {
        return codEspecialidad;
    }

    public void setCodEspecialidad(int codEspecialidad) {
        this.codEspecialidad = codEspecialidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getIdSector() {
        return idSector;
    }

    public void setIdSector(int idSector) {
        this.idSector = idSector;
    }

    @Override
    public String toString() {
        return "Especialidad [codEspecialidad=" + codEspecialidad + ", descripcion=" + descripcion + ", idSector=" + idSector + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Especialidad especialidad = (Especialidad) obj;
        return codEspecialidad == especialidad.codEspecialidad;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codEspecialidad);
    }
}
