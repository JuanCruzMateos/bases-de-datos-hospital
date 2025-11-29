package org.hospital.feature.internacion.domain;

import java.sql.Timestamp;

/**
 * DTO for guard audit records.
 * Maps to sp_auditoria_guardias result.
 */
public class AuditoriaGuardia {
    private int idAuditoria;
    private Timestamp fechaHoraReg;
    private String usuarioBd;
    private String operacion;
    private Integer nroGuardia;
    private Timestamp fechaHoraGuard;
    private Integer matricula;
    private Integer codEspecialidad;
    private Integer idTurno;
    private String detalleOld;
    private String detalleNew;

    public AuditoriaGuardia() {
    }

    public AuditoriaGuardia(int idAuditoria, Timestamp fechaHoraReg, String usuarioBd, String operacion,
                           Integer nroGuardia, Timestamp fechaHoraGuard, Integer matricula, Integer codEspecialidad,
                           Integer idTurno, String detalleOld, String detalleNew) {
        this.idAuditoria = idAuditoria;
        this.fechaHoraReg = fechaHoraReg;
        this.usuarioBd = usuarioBd;
        this.operacion = operacion;
        this.nroGuardia = nroGuardia;
        this.fechaHoraGuard = fechaHoraGuard;
        this.matricula = matricula;
        this.codEspecialidad = codEspecialidad;
        this.idTurno = idTurno;
        this.detalleOld = detalleOld;
        this.detalleNew = detalleNew;
    }

    public int getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(int idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Timestamp getFechaHoraReg() {
        return fechaHoraReg;
    }

    public void setFechaHoraReg(Timestamp fechaHoraReg) {
        this.fechaHoraReg = fechaHoraReg;
    }

    public String getUsuarioBd() {
        return usuarioBd;
    }

    public void setUsuarioBd(String usuarioBd) {
        this.usuarioBd = usuarioBd;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public Integer getNroGuardia() {
        return nroGuardia;
    }

    public void setNroGuardia(Integer nroGuardia) {
        this.nroGuardia = nroGuardia;
    }

    public Timestamp getFechaHoraGuard() {
        return fechaHoraGuard;
    }

    public void setFechaHoraGuard(Timestamp fechaHoraGuard) {
        this.fechaHoraGuard = fechaHoraGuard;
    }

    public Integer getMatricula() {
        return matricula;
    }

    public void setMatricula(Integer matricula) {
        this.matricula = matricula;
    }

    public Integer getCodEspecialidad() {
        return codEspecialidad;
    }

    public void setCodEspecialidad(Integer codEspecialidad) {
        this.codEspecialidad = codEspecialidad;
    }

    public Integer getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Integer idTurno) {
        this.idTurno = idTurno;
    }

    public String getDetalleOld() {
        return detalleOld;
    }

    public void setDetalleOld(String detalleOld) {
        this.detalleOld = detalleOld;
    }

    public String getDetalleNew() {
        return detalleNew;
    }

    public void setDetalleNew(String detalleNew) {
        this.detalleNew = detalleNew;
    }

    @Override
    public String toString() {
        return "AuditoriaGuardia{" +
                "idAuditoria=" + idAuditoria +
                ", fechaHoraReg=" + fechaHoraReg +
                ", usuarioBd='" + usuarioBd + '\'' +
                ", operacion='" + operacion + '\'' +
                ", nroGuardia=" + nroGuardia +
                ", fechaHoraGuard=" + fechaHoraGuard +
                ", matricula=" + matricula +
                ", codEspecialidad=" + codEspecialidad +
                ", idTurno=" + idTurno +
                ", detalleOld='" + detalleOld + '\'' +
                ", detalleNew='" + detalleNew + '\'' +
                '}';
    }
}

