package org.hospital.feature.guardia.repository;

import java.util.List;
import java.util.Optional;

import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.guardia.domain.Guardia;

public interface GuardiaDao {
    Guardia create(Guardia guardia) throws DataAccessException;

    Optional<Guardia> findById(int nroGuardia) throws DataAccessException;

    List<Guardia> findAll() throws DataAccessException;

    List<Guardia> findByMedico(long matricula) throws DataAccessException;

    List<Guardia> findByEspecialidad(int codEspecialidad) throws DataAccessException;

    Guardia update(Guardia guardia) throws DataAccessException;

    boolean delete(int nroGuardia) throws DataAccessException;

    int countGuardiasByMedicoAndMonth(long matricula, int year, int month)
        throws DataAccessException;

}

