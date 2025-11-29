package org.hospital.feature.internacion.repository;

import java.util.List;
import java.util.Optional;
import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.internacion.domain.Cama;

public interface CamaDao {
    Cama create(Cama cama) throws DataAccessException;
    
    Optional<Cama> findByNroCamaAndHabitacion(int nroCama, int nroHabitacion) throws DataAccessException;
    
    List<Cama> findAll() throws DataAccessException;
    
    List<Cama> findByHabitacion(int nroHabitacion) throws DataAccessException;
    
    List<Cama> findByEstado(String estado) throws DataAccessException;
    
    List<Cama> findAvailableByHabitacion(int nroHabitacion) throws DataAccessException;
    
    Cama update(Cama cama) throws DataAccessException;
    
    boolean delete(int nroCama, int nroHabitacion) throws DataAccessException;

    void agregarCama(int nroHabitacion, int nroCama) throws DataAccessException;

    void eliminarODesactivarCama(int nroHabitacion, int nroCama) throws DataAccessException;

}

