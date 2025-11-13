package org.hospital.internacion;

import java.util.List;
import java.util.Optional;

import org.hospital.exception.DataAccessException;

public interface SectorDao {
    Sector create(Sector sector) throws DataAccessException;

    Optional<Sector> findById(int idSector) throws DataAccessException;

    List<Sector> findAll() throws DataAccessException;

    Sector update(Sector sector) throws DataAccessException;

    boolean delete(int idSector) throws DataAccessException;
}

