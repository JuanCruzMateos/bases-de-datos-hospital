package org.hospital.medico;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.Objects;

import org.hospital.persona.Persona;

public class Medico extends Persona {
    private long matricula;
    private String cuilCuit;
    private LocalDate fechaIngreso;
    private byte[] foto;
    private int maxCantGuardia;
    private String periodoVacaciones;
    private Set<Especialidad> especialidades;

    public Medico() {
        super();
    }

    public Medico(String tipoDocumento, String nroDocumento, String nombre, String apellido, String tipo,
            long matricula, String cuilCuit, LocalDate fechaIngreso, byte[] foto,
            int maxCantGuardia, String periodoVacaciones, Set<Especialidad> especialidades) {
        super(tipoDocumento, nroDocumento, nombre, apellido, tipo);
        this.matricula = matricula;
        this.cuilCuit = cuilCuit;
        this.fechaIngreso = fechaIngreso;
        this.foto = foto;
        this.maxCantGuardia = maxCantGuardia;
        this.periodoVacaciones = periodoVacaciones;
        this.especialidades = especialidades;
    }

    public long getMatricula() {
        return matricula;
    }

    public void setMatricula(long matricula) {
        this.matricula = matricula;
    }

    public String getCuilCuit() {
        return cuilCuit;
    }

    public void setCuilCuit(String cuilCuit) {
        this.cuilCuit = cuilCuit;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public int getMaxCantGuardia() {
        return maxCantGuardia;
    }

    public void setMaxCantGuardia(int maxCantGuardia) {
        this.maxCantGuardia = maxCantGuardia;
    }

    public String getPeriodoVacaciones() {
        return periodoVacaciones;
    }

    public void setPeriodoVacaciones(String periodoVacaciones) {
        this.periodoVacaciones = periodoVacaciones;
    }

    public Set<Especialidad> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(Set<Especialidad> especialidades) {
        this.especialidades = especialidades;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Medico medico = (Medico) obj;
        return matricula == medico.matricula &&
                maxCantGuardia == medico.maxCantGuardia &&
                Objects.equals(cuilCuit, medico.cuilCuit) &&
                Objects.equals(fechaIngreso, medico.fechaIngreso) &&
                Arrays.equals(foto, medico.foto) &&
                Objects.equals(periodoVacaciones, medico.periodoVacaciones) &&
                Objects.equals(especialidades, medico.especialidades);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), matricula, cuilCuit, fechaIngreso, maxCantGuardia,
                periodoVacaciones, especialidades);
        result = 31 * result + Arrays.hashCode(foto);
        return result;
    }

    @Override
    public String toString() {
        return "Medico{" +
                "matricula=" + matricula +
                ", cuilCuit='" + cuilCuit + '\'' +
                ", nombre='" + getNombre() + '\'' +
                ", apellido='" + getApellido() + '\'' +
                ", fechaIngreso=" + fechaIngreso +
                ", maxCantGuardia=" + maxCantGuardia +
                ", periodoVacaciones='" + periodoVacaciones + '\'' +
                ", especialidades=" + especialidades +
                ", foto=" + (foto != null ? "[" + foto.length + " bytes]" : "null") + '}';
    }
}
