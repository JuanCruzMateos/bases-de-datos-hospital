package org.hospital.guardia;

import java.util.List;
import org.hospital.exception.DataAccessException;

public interface TurnoDao {
    List<Turno> findAll() throws DataAccessException;
}

