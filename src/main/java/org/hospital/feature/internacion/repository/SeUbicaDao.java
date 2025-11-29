package org.hospital.feature.internacion.repository;

import java.util.List;
import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.internacion.domain.SeUbica;

public interface SeUbicaDao {
    SeUbica create(SeUbica seUbica) throws DataAccessException;
    
    List<SeUbica> findByInternacion(int nroInternacion) throws DataAccessException;
    
    List<SeUbica> findByCama(int nroCama, int nroHabitacion) throws DataAccessException;
    
    List<SeUbica> findByHabitacion(int nroHabitacion) throws DataAccessException;
    
    List<SeUbica> findAll() throws DataAccessException;
    
    boolean delete(int nroInternacion, String fechaHoraIngreso) throws DataAccessException;
}

