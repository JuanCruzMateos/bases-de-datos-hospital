package org.hospital.internacion;

import java.util.List;
import org.hospital.exception.DataAccessException;

/**
 * DAO interface for Camas Disponibles stored procedures.
 */
public interface CamaDisponibleDao {
    
    /**
     * Get summary of available beds by sector.
     * Calls sp_camas_disponibles_resumen.
     * 
     * @return List of available beds summary by sector
     * @throws DataAccessException if database access fails
     */
    List<CamaDisponibleResumen> getResumen() throws DataAccessException;
    
    /**
     * Get detailed list of available beds for a specific sector.
     * Calls sp_camas_disponibles_detalle.
     * 
     * @param idSector The sector ID to filter by
     * @return List of available beds with details
     * @throws DataAccessException if database access fails
     */
    List<CamaDisponibleDetalle> getDetalle(int idSector) throws DataAccessException;
}

