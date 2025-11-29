package org.hospital.feature.internacion.service;

import org.hospital.feature.internacion.domain.ComentarioVisita;
import org.hospital.feature.internacion.domain.InternacionPaciente;
import org.hospital.feature.internacion.repository.VisitasMedicasDao;
import org.hospital.feature.internacion.repository.VisitasMedicasDaoImpl;
import org.hospital.feature.internacion.domain.*;
import org.hospital.feature.internacion.repository.*;

import java.util.List;
import java.util.logging.Logger;

import org.hospital.common.exception.DataAccessException;

/**
 * Service layer for Visitas Medicas reports.
 */
public class VisitasMedicasService {
    private static final Logger logger = Logger.getLogger(VisitasMedicasService.class.getName());
    private final VisitasMedicasDao dao;

    public VisitasMedicasService(VisitasMedicasDao dao) {
        this.dao = dao;
    }

    public VisitasMedicasService() {
        this(new VisitasMedicasDaoImpl());
    }

    /**
     * Get list of internations for a patient.
     * 
     * @param tipoDocumento Patient document type
     * @param nroDocumento Patient document number
     * @return List of patient's internations
     * @throws DataAccessException if database access fails
     */
    public List<InternacionPaciente> getInternacionesPaciente(String tipoDocumento, String nroDocumento) 
            throws DataAccessException {
        logger.info("Service: Getting internations for patient: " + tipoDocumento + " " + nroDocumento);
        
        if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de documento is required");
        }
        if (nroDocumento == null || nroDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("Número de documento is required");
        }
        
        return dao.getInternacionesPaciente(tipoDocumento.trim(), nroDocumento.trim());
    }

    /**
     * Get visit comments for a specific internation.
     * 
     * @param nroInternacion The internation ID
     * @return List of visit comments
     * @throws DataAccessException if database access fails
     */
    public List<ComentarioVisita> getComentariosVisitas(int nroInternacion) 
            throws DataAccessException {
        logger.info("Service: Getting visit comments for internation: " + nroInternacion);
        
        if (nroInternacion <= 0) {
            throw new IllegalArgumentException("Internation ID must be positive");
        }
        
        return dao.getComentariosVisitas(nroInternacion);
    }
}

