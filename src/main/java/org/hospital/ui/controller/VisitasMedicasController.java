package org.hospital.ui.controller;

import java.util.List;
import org.hospital.exception.DataAccessException;
import org.hospital.internacion.InternacionPaciente;
import org.hospital.internacion.ComentarioVisita;
import org.hospital.internacion.VisitasMedicasService;
import org.hospital.ui.view.VisitasMedicasPanel;

/**
 * Controller for Visitas Medicas reports.
 * Manages interaction between view and service layer.
 */
public class VisitasMedicasController extends BaseController {
    private VisitasMedicasPanel view;
    private VisitasMedicasService service;
    
    public VisitasMedicasController(VisitasMedicasPanel view) {
        this.view = view;
        this.service = new VisitasMedicasService();
        initController();
    }
    
    private void initController() {
        view.getBtnLoadInternaciones().addActionListener(e -> loadInternaciones());
        view.getBtnLoadComentarios().addActionListener(e -> loadComentarios());
        view.getBtnClear().addActionListener(e -> view.clearAll());
        
        // When user clicks on internaciones table, load that internation's comments
        view.getTableInternaciones().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Integer nroInternacion = view.getSelectedInternacion();
                if (nroInternacion != null) {
                    loadComentariosByInternacion(nroInternacion);
                }
            }
        });
    }
    
    private void loadInternaciones() {
        try {
            logger.info("Loading patient internations");
            
            String tipoDoc = view.getTipoDocumento();
            String nroDoc = view.getNroDocumento();
            
            if (nroDoc.isEmpty()) {
                showError("Por favor ingrese el número de documento del paciente");
                return;
            }
            
            List<InternacionPaciente> internaciones = service.getInternacionesPaciente(tipoDoc, nroDoc);
            view.updateInternacionesTable(internaciones);
            logger.fine("Loaded " + internaciones.size() + " internations");
            
            if (internaciones.isEmpty()) {
                showSuccess("No se encontraron internaciones para el paciente " + tipoDoc + " " + nroDoc);
            }
            
        } catch (IllegalArgumentException e) {
            showError("Error de validación: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void loadComentarios() {
        Integer nroInternacion = view.getSelectedInternacion();
        if (nroInternacion == null) {
            showError("Por favor seleccione una internación de la tabla");
            return;
        }
        loadComentariosByInternacion(nroInternacion);
    }
    
    private void loadComentariosByInternacion(int nroInternacion) {
        try {
            logger.info("Loading visit comments for internation: " + nroInternacion);
            
            List<ComentarioVisita> comentarios = service.getComentariosVisitas(nroInternacion);
            view.updateComentariosTable(comentarios);
            logger.fine("Loaded " + comentarios.size() + " comments");
            
            if (comentarios.isEmpty()) {
                showSuccess("No se encontraron comentarios de visitas para la internación " + nroInternacion);
            }
            
        } catch (IllegalArgumentException e) {
            showError("Error de validación: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
}

