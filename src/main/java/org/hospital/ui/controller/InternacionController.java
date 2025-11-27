package org.hospital.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.hospital.exception.DataAccessException;
import org.hospital.internacion.Internacion;
import org.hospital.internacion.InternacionService;
import org.hospital.ui.view.InternacionPanel;
import org.hospital.internacion.SeUbica;


/**
 * Controller for Internacion (Hospitalization) operations.
 * Uses service layer for business logic and data access.
 */
public class InternacionController extends BaseController {
    private InternacionPanel view;
    private InternacionService service;
    
    public InternacionController(InternacionPanel view) {
        this.view = view;
        this.service = new InternacionService();
        initController();
        loadInternaciones();
    }
    
    private void initController() {
        view.getBtnCreate().addActionListener(e -> createInternacion());
        view.getBtnUpdate().addActionListener(e -> updateInternacion());
        view.getBtnDelete().addActionListener(e -> deleteInternacion());
        view.getBtnRefresh().addActionListener(e -> loadInternaciones());
        view.getBtnFilterActivas().addActionListener(e -> loadInternacionesActivas());
        view.getBtnClear().addActionListener(e -> view.clearForm());
        
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.loadSelectedToForm();
                loadCurrentBedForSelectedInternacion();
            }
        });
    }
    
    private void loadCurrentBedForSelectedInternacion() {
        try {
            String nroIntStr = view.getNroInternacion();
            if (nroIntStr == null || nroIntStr.trim().isEmpty()) {
                // Nada seleccionado
                view.setNroHabitacion("");
                view.setNroCama("");
                return;
            }

            int nroInt = Integer.parseInt(nroIntStr);

            // Pedimos TODAS las ubicaciones de esa internación
            List<SeUbica> asignaciones = service.getBedAssignments(nroInt);

            if (asignaciones == null || asignaciones.isEmpty()) {
                // No tiene ubicaciones registradas todavía
                view.setNroHabitacion("");
                view.setNroCama("");
                return;
            }

            // Como SeUbicaDao.findByInternacion ordena por fecha_hora_ingreso,
            // la última posición es la cama actual
            SeUbica ultima = asignaciones.get(asignaciones.size() - 1);

            view.setNroHabitacion(String.valueOf(ultima.getNroHabitacion()));
            view.setNroCama(String.valueOf(ultima.getNroCama()));

        } catch (NumberFormatException ex) {
            logger.warning("Invalid nroInternacion when loading bed info: " + ex.getMessage());
            view.setNroHabitacion("");
            view.setNroCama("");
        } catch (DataAccessException ex) {
            handleDataAccessException(ex);
        } catch (Exception ex) {
            // Por si acaso
            handleException(ex);
        }
    }

    private void loadInternaciones() {
        try {
            logger.info("Loading all internaciones");
            List<Internacion> internaciones = service.getAllInternaciones();
            view.updateTable(internaciones);
            logger.fine("Loaded " + internaciones.size() + " internaciones");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }
    
    private void loadInternacionesActivas() {
        try {
            logger.info("Loading active internaciones");
            List<Internacion> internaciones = service.getActivasInternaciones();
            view.updateTable(internaciones);
            logger.fine("Loaded " + internaciones.size() + " active internaciones");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }
    
    private void createInternacion() {
        try {
            logger.info("User initiating create internacion");
            
            if (!validateInputs()) return;
            
            // 1) Fechas
            LocalDate fechaInicio = LocalDate.parse(view.getFechaInicio());
            LocalDate fechaFin = null;
            if (!view.isActiva() && !view.getFechaFin().isEmpty()) {
                fechaFin = LocalDate.parse(view.getFechaFin());
            }

            // 2) Matrícula (obligatoria)
            long matricula = Long.parseLong(view.getMatricula());

            // 3) Habitación / Cama (opcionales)
            Integer nroHabitacion = null;
            Integer nroCama = null;
            String habText = view.getNroHabitacion();
            String camaText = view.getNroCama();

            // Caso A: alguno de los dos vino informado, pero no ambos: error
            if ((!habText.isEmpty() && camaText.isEmpty()) ||
                (habText.isEmpty() && !camaText.isEmpty())) {
                showError("Si indica Nro Habitación debe indicar también Nro Cama (y viceversa).");
                return;
            }

            // Caso B: ambos vienen completo: parsear a Integer
            if (!habText.isEmpty() && !camaText.isEmpty()) {
                nroHabitacion = Integer.valueOf(habText);
                nroCama = Integer.valueOf(camaText);
            }
            // Caso C: ambos vacíos: se quedan en null, la BD elegirá cama libre

            // 4) Armar objeto de dominio
            Internacion internacion = new Internacion(
                0,                      // nro_internacion lo genera la BD
                fechaInicio,
                fechaFin,
                view.getTipoDocumento(),
                view.getNroDocumento(),
                matricula
            );

            // 5) Llamar al service con cama/habitación opcionales
            // (lo vamos a implementar en InternacionService)
            service.createInternacion(internacion, nroHabitacion, nroCama);

            showSuccess("Internación created successfully!");
            view.clearForm();
            loadInternaciones();

        } catch (DateTimeParseException e) {
            showError("Invalid date format. Use YYYY-MM-DD");
        } catch (NumberFormatException e) {
            // Ahora puede fallar matrícula, habitación o cama
            showError("Formato numérico inválido en Matrícula / Nro Habitación / Nro Cama");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void updateInternacion() {
        try {
            logger.info("User initiating update internacion");

            if (view.getNroInternacion().isEmpty()) {
                showError("Debe seleccionar una internación para actualizar.");
                return;
            }

            if (!validateInputs()) return;

            int nroInt = Integer.parseInt(view.getNroInternacion());

            // Fechas
            LocalDate fechaInicio = LocalDate.parse(view.getFechaInicio());
            LocalDate fechaFin = null;
            if (!view.isActiva() && !view.getFechaFin().isEmpty()) {
                fechaFin = LocalDate.parse(view.getFechaFin());
            }

            // Matrícula
            long matricula = Long.parseLong(view.getMatricula());

            // Habitación / cama (opcionales)
            String habText = view.getNroHabitacion();
            String camaText = view.getNroCama();
            Integer nuevaHab = null;
            Integer nuevaCama = null;

            // Si toca uno tiene que tocar el otro
            if ((!habText.isEmpty() && camaText.isEmpty()) ||
                (habText.isEmpty() && !camaText.isEmpty())) {
                showError("Si modifica Nro Habitación debe indicar también Nro Cama (y viceversa).");
                return;
            }

            // Si ambos vienen completos → parsear
            if (!habText.isEmpty() && !camaText.isEmpty()) {
                nuevaHab = Integer.valueOf(habText);
                nuevaCama = Integer.valueOf(camaText);
            }

            // Armar objeto de dominio
            Internacion internacion = new Internacion(
                nroInt,
                fechaInicio,
                fechaFin,
                view.getTipoDocumento(),
                view.getNroDocumento(),
                matricula
            );

            // 1) Actualizar datos básicos de la internación
            service.updateInternacion(internacion);

            // 2) Si se indicó una cama, ver si cambió y, si cambió, llamar al SP
            if (nuevaHab != null && nuevaCama != null) {
                // Traemos TODAS las ubicaciones para esa internación
                List<SeUbica> asignaciones = service.getBedAssignments(nroInt);
                SeUbica ultima = null;
                if (asignaciones != null && !asignaciones.isEmpty()) {
                    ultima = asignaciones.get(asignaciones.size() - 1);
                }

                Integer habActual = (ultima != null ? ultima.getNroHabitacion() : null);
                Integer camaActual = (ultima != null ? ultima.getNroCama() : null);

                // Solo llamamos al SP si realmente hay cambio
                if (habActual == null ||
                    !habActual.equals(nuevaHab) ||
                    !camaActual.equals(nuevaCama)) {
                    service.changeBed(nroInt, nuevaHab, nuevaCama);
                }
            }

            showSuccess("Internación updated successfully!");
            view.clearForm();
            loadInternaciones();

        } catch (DateTimeParseException e) {
            showError("Invalid date format. Use YYYY-MM-DD");
        } catch (NumberFormatException e) {
            showError("Formato numérico inválido en Matrícula / Nro Habitación / Nro Cama");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void deleteInternacion() {
        try {
            logger.info("User initiating delete internacion");
            
            if (view.getNroInternacion().isEmpty()) {
                showError("Please select an internación to delete");
                return;
            }
            
            if (!showConfirmation("Are you sure you want to delete this internación?")) {
                return;
            }
            
            int nroInt = Integer.parseInt(view.getNroInternacion());
            boolean deleted = service.deleteInternacion(nroInt);
            if (deleted) {
                showSuccess("Internación deleted successfully!");
                view.clearForm();
                loadInternaciones();
            } else {
                showError("Internación not found");
            }
            
        } catch (NumberFormatException e) {
            showError("Invalid number format");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private boolean validateInputs() {
        if (view.getFechaInicio().isEmpty()) {
            showError("Fecha Inicio is required");
            return false;
        }
        if (view.getTipoDocumento().isEmpty()) {
            showError("Tipo Documento is required");
            return false;
        }
        if (view.getNroDocumento().isEmpty()) {
            showError("Nro Documento is required");
            return false;
        }
        if (view.getMatricula().isEmpty()) {
            showError("Matrícula is required");
            return false;
        }
        return true;
    }
}

