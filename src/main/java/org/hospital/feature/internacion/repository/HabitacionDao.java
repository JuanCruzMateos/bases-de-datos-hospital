package org.hospital.feature.internacion.repository;

import java.util.List;
import java.util.Optional;

import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.internacion.domain.Habitacion;

public interface HabitacionDao {
    Habitacion create(Habitacion habitacion) throws DataAccessException;

    Optional<Habitacion> findById(int nroHabitacion) throws DataAccessException;

    List<Habitacion> findAll() throws DataAccessException;

    List<Habitacion> findBySector(int idSector) throws DataAccessException;

    Habitacion update(Habitacion habitacion) throws DataAccessException;

    boolean delete(int nroHabitacion) throws DataAccessException;
}

