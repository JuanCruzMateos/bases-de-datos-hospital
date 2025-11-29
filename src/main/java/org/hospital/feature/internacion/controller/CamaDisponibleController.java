package org.hospital.feature.internacion.controller;

import java.util.List;

import org.hospital.common.controller.BaseController;
import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.internacion.domain.CamaDisponibleResumen;
import org.hospital.feature.internacion.domain.CamaDisponibleDetalle;
import org.hospital.feature.internacion.service.CamaDisponibleService;
import org.hospital.feature.internacion.ui.CamaDisponiblePanel;

/**
 * Controller for Camas Disponibles reports.
 * Manages interaction between view and service layer.
 */
public class CamaDisponibleController extends BaseController {
    private CamaDisponiblePanel view;
    private CamaDisponibleService service;
    
    public CamaDisponibleController(CamaDisponiblePanel view) {
        this.view = view;
        this.service = new CamaDisponibleService();
        initController();
        loadResumen(); // Auto-load summary on initialization
    }
    
    private void initController() {
        view.getBtnLoadResumen().addActionListener(e -> loadResumen());
        view.getBtnLoadDetalle().addActionListener(e -> loadDetalle());
        view.getBtnClear().addActionListener(e -> view.clearAll());
        
        // When user clicks on resumen table, load that sector's detail
        view.getTableResumen().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Integer idSector = view.getSelectedSectorFromResumen();
                if (idSector != null) {
                    view.setIdSector(String.valueOf(idSector));
                    loadDetalle();
                }
            }
        });
    }
    
    private void loadResumen() {
        try {
            logger.info("Loading available beds summary");
            List<CamaDisponibleResumen> resumenes = service.getResumen();
            view.updateResumenTable(resumenes);
            logger.fine("Loaded " + resumenes.size() + " summary records");
            
            if (resumenes.isEmpty()) {
                showSuccess("Consulta exitosa. No hay camas disponibles en ningún sector");
            }
            
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void loadDetalle() {
        try {
            logger.info("User initiating load detail");
            
            String idSectorStr = view.getIdSector();
            if (idSectorStr.isEmpty()) {
                showError("Por favor ingrese o seleccione un ID de Sector");
                return;
            }
            
            int idSector = Integer.parseInt(idSectorStr);
            logger.info("Loading available beds detail for sector: " + idSector);
            
            List<CamaDisponibleDetalle> detalles = service.getDetalle(idSector);
            view.updateDetalleTable(detalles);
            logger.fine("Loaded " + detalles.size() + " detail records");
            
            if (detalles.isEmpty()) {
                showSuccess("Consulta exitosa. No hay camas disponibles en el sector " + idSector);
            }
            
        } catch (NumberFormatException e) {
            showError("ID de Sector debe ser un número válido");
        } catch (IllegalArgumentException e) {
            showError("Error de validación: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
}

