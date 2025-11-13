package org.hospital.persona;

import java.util.Objects;

public abstract class Persona {
    private String tipoDocumento;
    private String nroDocumento;
    private String nombre;
    private String apellido;
    private String tipo;

    public Persona() {
    }

    public Persona(String tipoDocumento, String nroDocumento, String nombre, String apellido, String tipo) {
        this.tipoDocumento = tipoDocumento;
        this.nroDocumento = nroDocumento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipo = tipo;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNroDocumento() {
        return nroDocumento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public void setNroDocumento(String nroDocumento) {
        this.nroDocumento = nroDocumento;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Persona persona = (Persona) obj;
        return Objects.equals(tipoDocumento, persona.tipoDocumento) && Objects.equals(nroDocumento, persona.nroDocumento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoDocumento, nroDocumento);
    }
}
