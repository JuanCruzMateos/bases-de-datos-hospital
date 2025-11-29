package org.hospital.feature.paciente.domain;

import java.time.LocalDate;
import java.util.Objects;

import org.hospital.common.domain.Persona;

public class Paciente extends Persona {
    private LocalDate fechaNacimiento;
    private char sexo;

    public Paciente() {
        super();
    }

    public Paciente(String tipoDocumento, String nroDocumento, String nombre, String apellido, String tipo,
            LocalDate fechaNacimiento, char sexo) {
        super(tipoDocumento, nroDocumento, nombre, apellido, tipo);
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public char getSexo() {
        return sexo;
    }

    public void setSexo(char sexo) {
        this.sexo = sexo;
    }

    @Override
    public String toString() {
        return "Paciente [fechaNacimiento=" + fechaNacimiento + ", sexo=" + sexo + ", tipoDocumento="
                + getTipoDocumento() + ", nroDocumento=" + getNroDocumento() + ", nombre=" + getNombre() + ", apellido="
                + getApellido() + ", tipo=" + getTipo() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Paciente paciente = (Paciente) obj;
        return Objects.equals(getTipoDocumento(), paciente.getTipoDocumento()) &&
               Objects.equals(getNroDocumento(), paciente.getNroDocumento()) &&
               Objects.equals(fechaNacimiento, paciente.fechaNacimiento) && 
               sexo == paciente.sexo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTipoDocumento(), getNroDocumento(), fechaNacimiento, sexo);
    }
}
