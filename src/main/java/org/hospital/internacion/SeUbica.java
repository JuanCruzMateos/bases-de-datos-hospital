package org.hospital.internacion;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model class representing the bed assignment relationship (Se_Ubica).
 * Tracks which bed in which room a patient's internacion is assigned to, and when.
 */
public class SeUbica {
    private int nroInternacion;
    private LocalDateTime fechaHoraIngreso;
    private int nroCama;
    private int nroHabitacion;

    public SeUbica() {
    }

    public SeUbica(int nroInternacion, LocalDateTime fechaHoraIngreso, int nroCama, int nroHabitacion) {
        this.nroInternacion = nroInternacion;
        this.fechaHoraIngreso = fechaHoraIngreso;
        this.nroCama = nroCama;
        this.nroHabitacion = nroHabitacion;
    }

    public int getNroInternacion() {
        return nroInternacion;
    }

    public void setNroInternacion(int nroInternacion) {
        this.nroInternacion = nroInternacion;
    }

    public LocalDateTime getFechaHoraIngreso() {
        return fechaHoraIngreso;
    }

    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) {
        this.fechaHoraIngreso = fechaHoraIngreso;
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

    @Override
    public String toString() {
        return "SeUbica [nroInternacion=" + nroInternacion + ", fechaHoraIngreso=" + fechaHoraIngreso +
               ", nroCama=" + nroCama + ", nroHabitacion=" + nroHabitacion + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        SeUbica seUbica = (SeUbica) obj;
        return nroInternacion == seUbica.nroInternacion && nroCama == seUbica.nroCama &&
               nroHabitacion == seUbica.nroHabitacion &&
               Objects.equals(fechaHoraIngreso, seUbica.fechaHoraIngreso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nroInternacion, fechaHoraIngreso, nroCama, nroHabitacion);
    }
}

