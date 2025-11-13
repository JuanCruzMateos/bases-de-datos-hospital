package org.hospital.guardia;

import java.util.List;
import java.util.Optional;

import org.hospital.exception.DataAccessException;

public interface GuardiaDao {
    Guardia create(Guardia guardia) throws DataAccessException;

    Optional<Guardia> findById(int nroGuardia) throws DataAccessException;

    List<Guardia> findAll() throws DataAccessException;

    List<Guardia> findByMedico(long matricula) throws DataAccessException;

    List<Guardia> findByEspecialidad(int codEspecialidad) throws DataAccessException;

    Guardia update(Guardia guardia) throws DataAccessException;

    boolean delete(int nroGuardia) throws DataAccessException;
}

