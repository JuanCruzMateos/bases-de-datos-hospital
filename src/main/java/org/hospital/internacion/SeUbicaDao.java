package org.hospital.internacion;

import java.util.List;
import org.hospital.exception.DataAccessException;

public interface SeUbicaDao {
    SeUbica create(SeUbica seUbica) throws DataAccessException;
    
    List<SeUbica> findByInternacion(int nroInternacion) throws DataAccessException;
    
    List<SeUbica> findByCama(int nroCama, int nroHabitacion) throws DataAccessException;
    
    List<SeUbica> findByHabitacion(int nroHabitacion) throws DataAccessException;
    
    List<SeUbica> findAll() throws DataAccessException;
    
    boolean delete(int nroInternacion, String fechaHoraIngreso) throws DataAccessException;
}

