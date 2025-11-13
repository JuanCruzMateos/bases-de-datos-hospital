package org.hospital.internacion;

import java.util.Objects;

/**
 * Model class representing a bed (Cama) in the hospital.
 * Each bed belongs to a room and has a state (available, occupied, maintenance).
 */
public class Cama {
    private int nroCama;
    private int nroHabitacion;
    private String estado;  // DISPONIBLE, OCUPADA, MANTENIMIENTO

    public Cama() {
    }

    public Cama(int nroCama, int nroHabitacion, String estado) {
        this.nroCama = nroCama;
        this.nroHabitacion = nroHabitacion;
        this.estado = estado;
    }

    public int getNroCama() {
        return nroCama;
    }

    public void setNroCama(int nroCama) {
        this.nroCama = nroCama;
    }

    public int getNroHabitacion() {
        return nroHabitacion;
    }

    public void setNroHabitacion(int nroHabitacion) {
        this.nroHabitacion = nroHabitacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Cama [nroCama=" + nroCama + ", nroHabitacion=" + nroHabitacion + 
               ", estado=" + estado + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Cama cama = (Cama) obj;
        return nroCama == cama.nroCama && nroHabitacion == cama.nroHabitacion &&
               Objects.equals(estado, cama.estado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nroCama, nroHabitacion, estado);
    }
}
