package org.hospital.feature.paciente.repository;

import java.util.List;
import java.util.Optional;

import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.paciente.domain.Paciente;

public interface PacienteDao {
    Paciente create(Paciente paciente) throws DataAccessException;

    Optional<Paciente> findByTipoDocumentoAndNroDocumento(String tipoDocumento, String nroDocumento) throws DataAccessException;

    List<Paciente> findAll() throws DataAccessException;

    Paciente update(Paciente paciente) throws DataAccessException;

    boolean delete(String tipoDocumento, String nroDocumento) throws DataAccessException;
}
