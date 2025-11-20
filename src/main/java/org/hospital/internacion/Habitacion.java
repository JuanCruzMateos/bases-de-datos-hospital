package org.hospital.internacion;

import java.util.Objects;

public class Habitacion {
    private int nroHabitacion;
    private int piso;
    private String orientacion;
    private int idSector;

    public Habitacion() {
    }

    public Habitacion(int nroHabitacion, int piso, String orientacion, int idSector) {
        this.nroHabitacion = nroHabitacion;
        this.piso = piso;
        this.orientacion = orientacion;
        this.idSector = idSector;
    }

    public Habitacion(int piso, String orientacion, int idSector) {
        this.piso = piso;
        this.orientacion = orientacion;
        this.idSector = idSector;
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

    public int getIdSector() {
        return idSector;
    }

    public void setIdSector(int idSector) {
        this.idSector = idSector;
    }

    @Override
    public String toString() {
        return "Habitacion [nroHabitacion=" + nroHabitacion + ", piso=" + piso + 
               ", orientacion=" + orientacion + ", idSector=" + idSector + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Habitacion habitacion = (Habitacion) obj;
        return nroHabitacion == habitacion.nroHabitacion && piso == habitacion.piso && 
               idSector == habitacion.idSector && Objects.equals(orientacion, habitacion.orientacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nroHabitacion, piso, orientacion, idSector);
    }
}
