package org.hospital.medico;

import java.util.List;
import java.util.Optional;

import org.hospital.exception.DataAccessException;

public interface MedicoDao {
    Medico create(Medico medico) throws DataAccessException;

    Optional<Medico> findByMatricula(long matricula) throws DataAccessException;

    List<Medico> findAll() throws DataAccessException;

    Medico update(Medico medico) throws DataAccessException;

    boolean delete(long matricula) throws DataAccessException;      
}

