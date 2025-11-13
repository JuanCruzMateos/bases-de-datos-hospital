package org.hospital.internacion;

import java.util.List;
import java.util.Optional;
import org.hospital.exception.DataAccessException;

public interface CamaDao {
    Cama create(Cama cama) throws DataAccessException;
    
    Optional<Cama> findByNroCamaAndHabitacion(int nroCama, int nroHabitacion) throws DataAccessException;
    
    List<Cama> findAll() throws DataAccessException;
    
    List<Cama> findByHabitacion(int nroHabitacion) throws DataAccessException;
    
    List<Cama> findByEstado(String estado) throws DataAccessException;
    
    List<Cama> findAvailableByHabitacion(int nroHabitacion) throws DataAccessException;
    
    Cama update(Cama cama) throws DataAccessException;
    
    boolean delete(int nroCama, int nroHabitacion) throws DataAccessException;
}

