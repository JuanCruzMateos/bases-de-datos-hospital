package org.hospital.feature.internacion.repository;

import org.hospital.feature.internacion.domain.ComentarioVisita;
import org.hospital.feature.internacion.domain.InternacionPaciente;
import java.util.List;
import org.hospital.common.exception.DataAccessException;

/**
 * DAO interface for Visitas Medicas stored procedures.
 */
public interface VisitasMedicasDao {
    
    /**
     * Get list of internations for a patient.
     * Calls sp_internaciones_paciente.
     * 
     * @param tipoDocumento Patient document type
     * @param nroDocumento Patient document number
     * @return List of patient's internations
     * @throws DataAccessException if database access fails
     */
    List<InternacionPaciente> getInternacionesPaciente(String tipoDocumento, String nroDocumento) 
            throws DataAccessException;
    
    /**
     * Get visit comments for a specific internation.
     * Calls sp_comentarios_visitas.
     * 
     * @param nroInternacion The internation ID
     * @return List of visit comments
     * @throws DataAccessException if database access fails
     */
    List<ComentarioVisita> getComentariosVisitas(int nroInternacion) 
            throws DataAccessException;
}

