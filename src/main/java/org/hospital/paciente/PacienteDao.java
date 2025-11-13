package org.hospital.paciente;

import java.util.List;
import java.util.Optional;

import org.hospital.exception.DataAccessException;

public interface PacienteDao {
    Paciente create(Paciente paciente) throws DataAccessException;

    Optional<Paciente> findByTipoDocumentoAndNroDocumento(String tipoDocumento, String nroDocumento) throws DataAccessException;

    List<Paciente> findAll() throws DataAccessException;

    Paciente update(Paciente paciente) throws DataAccessException;

    boolean delete(String tipoDocumento, String nroDocumento) throws DataAccessException;
}
