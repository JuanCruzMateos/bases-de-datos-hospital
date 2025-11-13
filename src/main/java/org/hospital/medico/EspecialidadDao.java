package org.hospital.medico;

import java.util.List;
import java.util.Optional;

import org.hospital.exception.DataAccessException;

public interface EspecialidadDao {
    Especialidad create(Especialidad especialidad) throws DataAccessException;

    Optional<Especialidad> findByCodEspecialidad(int codEspecialidad) throws DataAccessException;

    List<Especialidad> findAll() throws DataAccessException;

    Especialidad update(Especialidad especialidad) throws DataAccessException;

    boolean delete(int codEspecialidad) throws DataAccessException;
}
