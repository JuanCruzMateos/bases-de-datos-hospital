package org.hospital.feature.guardia.repository;

import java.util.List;
import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.guardia.domain.Turno;

public interface TurnoDao {
    List<Turno> findAll() throws DataAccessException;
}

