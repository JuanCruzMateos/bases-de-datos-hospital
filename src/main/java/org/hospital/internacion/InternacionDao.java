package org.hospital.internacion;

import java.util.List;
import java.util.Optional;

import org.hospital.exception.DataAccessException;

public interface InternacionDao {
    Internacion create(Internacion internacion) throws DataAccessException;

    Internacion create(Internacion internacion, Integer nroHabitacion, Integer nroCama) throws DataAccessException;

    void changeBed(int nroInternacion, int nroHabitacion, int nroCama) throws DataAccessException;

    Optional<Internacion> findById(int nroInternacion) throws DataAccessException;

    List<Internacion> findAll() throws DataAccessException;

    List<Internacion> findByPaciente(String tipoDocumento, String nroDocumento) throws DataAccessException;

    List<Internacion> findActivasInternaciones() throws DataAccessException;

    Internacion update(Internacion internacion) throws DataAccessException;

    boolean delete(int nroInternacion) throws DataAccessException;
}

