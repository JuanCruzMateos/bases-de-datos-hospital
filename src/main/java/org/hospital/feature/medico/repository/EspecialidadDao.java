package org.hospital.feature.medico.repository;

import java.util.List;
import java.util.Optional;

import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.medico.domain.Especialidad;

public interface EspecialidadDao {
    Especialidad create(Especialidad especialidad) throws DataAccessException;

    Optional<Especialidad> findByCodEspecialidad(int codEspecialidad) throws DataAccessException;

    List<Especialidad> findAll() throws DataAccessException;

    Especialidad update(Especialidad especialidad) throws DataAccessException;

    boolean delete(int codEspecialidad) throws DataAccessException;
}
