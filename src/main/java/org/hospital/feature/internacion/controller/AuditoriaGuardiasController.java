package org.hospital.feature.internacion.controller;

import java.util.List;

import org.hospital.common.controller.BaseController;
import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.internacion.domain.AuditoriaGuardia;
import org.hospital.feature.internacion.service.AuditoriaGuardiasService;
import org.hospital.feature.internacion.ui.AuditoriaGuardiasPanel;

/**
 * Controller for Auditoria Guardias reports.
 * Manages interaction between view and service layer.
 */
public class AuditoriaGuardiasController extends BaseController {
    private AuditoriaGuardiasPanel view;
    private AuditoriaGuardiasService service;
    
    public AuditoriaGuardiasController(AuditoriaGuardiasPanel view) {
        this.view = view;
        this.service = new AuditoriaGuardiasService();
        initController();
    }
    
    private void initController() {
        view.getBtnActualizar().addActionListener(e -> loadAuditoria());
        // Load initial data on startup
        loadAuditoria();
    }
    
    private void loadAuditoria() {
        try {
            logger.info("Loading all guard audit records");
            
            // Load all audit records without filters
            List<AuditoriaGuardia> auditorias = service.getAuditoriaGuardias(null, null, null);
            
            view.updateTable(auditorias);
            logger.fine("Loaded " + auditorias.size() + " audit records");
            
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
}

