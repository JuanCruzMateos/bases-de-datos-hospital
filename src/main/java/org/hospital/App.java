package org.hospital;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.hospital.exception.DataAccessException;
import org.hospital.medico.Medico;
import org.hospital.medico.MedicoDao;
import org.hospital.medico.MedicoDaoImpl;
import org.hospital.paciente.Paciente;
import org.hospital.paciente.PacienteDao;
import org.hospital.paciente.PacienteDaoImpl;
import org.hospital.internacion.Sector;
import org.hospital.internacion.SectorDao;
import org.hospital.internacion.SectorDaoImpl;
import org.hospital.internacion.Habitacion;
import org.hospital.internacion.HabitacionDao;
import org.hospital.internacion.HabitacionDaoImpl;
import org.hospital.internacion.Internacion;
import org.hospital.internacion.InternacionDao;
import org.hospital.internacion.InternacionDaoImpl;
import org.hospital.guardia.Guardia;
import org.hospital.guardia.GuardiaDao;
import org.hospital.guardia.GuardiaDaoImpl;

public class App {
    private static final MedicoDao medicoDao = new MedicoDaoImpl();
    private static final PacienteDao pacienteDao = new PacienteDaoImpl();
    private static final SectorDao sectorDao = new SectorDaoImpl();
    private static final HabitacionDao habitacionDao = new HabitacionDaoImpl();
    private static final InternacionDao internacionDao = new InternacionDaoImpl();
    private static final GuardiaDao guardiaDao = new GuardiaDaoImpl();

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
        } else {
            try {
                String command = args[0].toLowerCase();
                switch (command) {
                    // Medico commands
                    case "list-medicos":
                        listMedicos();
                        break;
                    case "get-medico":
                        getMedico(args);
                        break;
                    case "create-medico":
                        createMedico(args);
                        break;
                    case "update-medico":
                        updateMedico(args);
                        break;
                    case "delete-medico":
                        deleteMedico(args);
                        break;
                    // Paciente commands
                    case "list-pacientes":
                        listPacientes();
                        break;
                    case "get-paciente":
                        getPaciente(args);
                        break;
                    case "create-paciente":
                        createPaciente(args);
                        break;
                    case "update-paciente":
                        updatePaciente(args);
                        break;
                    case "delete-paciente":
                        deletePaciente(args);
                        break;
                    // Sector commands
                    case "list-sectores":
                        listSectores();
                        break;
                    case "get-sector":
                        getSector(args);
                        break;
                    case "create-sector":
                        createSector(args);
                        break;
                    case "update-sector":
                        updateSector(args);
                        break;
                    case "delete-sector":
                        deleteSector(args);
                        break;
                    // Habitacion commands
                    case "list-habitaciones":
                        listHabitaciones();
                        break;
                    case "get-habitacion":
                        getHabitacion(args);
                        break;
                    case "create-habitacion":
                        createHabitacion(args);
                        break;
                    case "update-habitacion":
                        updateHabitacion(args);
                        break;
                    case "delete-habitacion":
                        deleteHabitacion(args);
                        break;
                    // Internacion commands
                    case "list-internaciones":
                        listInternaciones();
                        break;
                    case "list-internaciones-activas":
                        listInternacionesActivas();
                        break;
                    case "get-internacion":
                        getInternacion(args);
                        break;
                    case "create-internacion":
                        createInternacion(args);
                        break;
                    case "update-internacion":
                        updateInternacion(args);
                        break;
                    case "delete-internacion":
                        deleteInternacion(args);
                        break;
                    // Guardia commands
                    case "list-guardias":
                        listGuardias();
                        break;
                    case "get-guardia":
                        getGuardia(args);
                        break;
                    case "create-guardia":
                        createGuardia(args);
                        break;
                    case "update-guardia":
                        updateGuardia(args);
                        break;
                    case "delete-guardia":
                        deleteGuardia(args);
                        break;
                    default:
                        System.err.println("Unknown command: " + command);
                        printUsage();
                        break;
                }
            } catch (IllegalArgumentException | DateTimeParseException e) {
                System.err.println("Argument error: " + e.getMessage());
            } catch (DataAccessException e) {
                System.err.println("Database error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void listMedicos() {
        List<Medico> medicos = medicoDao.findAll();
        if (medicos.isEmpty()) {
            System.out.println("No medicos found.");
            return;
        }
        medicos.forEach(App::printMedico);
    }

    private static void getMedico(String[] args) {
        requireArgs(args, 2, "get-medico <matricula>");
        long matricula = parseMatricula(args[1]);
        Optional<Medico> medico = medicoDao.findByMatricula(matricula);
        if (medico.isPresent()) {
            printMedico(medico.get());
        } else {
            System.out.println("Medico not found.");
        }
    }

    private static void createMedico(String[] args) {
        requireArgs(args, 11,
                "create-medico <tipoDocumento> <nroDocumento> <nombre> <apellido> <tipo> <matricula> <cuilCuit> <fechaIngreso> <maxCantGuardia> <periodoVacaciones>");
        long matricula = parseMatricula(args[6]);
        LocalDate fechaIngreso = LocalDate.parse(args[8]);
        int maxCantGuardia = parseInteger(args[9], "maxCantGuardia");
        Medico medico = new Medico(args[1], args[2], args[3], args[4], args[5],
                matricula, args[7], fechaIngreso, null, maxCantGuardia, args[10], new HashSet<>());
        Medico created = medicoDao.create(medico);
        System.out.println("Medico created successfully.");
        printMedico(created);
    }

    private static void updateMedico(String[] args) {
        requireArgs(args, 11,
                "update-medico <tipoDocumento> <nroDocumento> <nombre> <apellido> <tipo> <matricula> <cuilCuit> <fechaIngreso> <maxCantGuardia> <periodoVacaciones>");
        long matricula = parseMatricula(args[6]);
        LocalDate fechaIngreso = LocalDate.parse(args[8]);
        int maxCantGuardia = parseInteger(args[9], "maxCantGuardia");
        Medico medico = new Medico(args[1], args[2], args[3], args[4], args[5],
                matricula, args[7], fechaIngreso, null, maxCantGuardia, args[10], new HashSet<>());
        Medico updated = medicoDao.update(medico);
        System.out.println("Medico updated successfully.");
        printMedico(updated);
    }

    private static void deleteMedico(String[] args) {
        requireArgs(args, 2, "delete-medico <matricula>");
        long matricula = parseMatricula(args[1]);
        boolean deleted = medicoDao.delete(matricula);
        if (deleted) {
            System.out.println("Medico deleted successfully.");
        } else {
            System.out.println("Medico not found with matricula: " + matricula);
        }
    }

    private static void printMedico(Medico medico) {
        System.out.printf("Medico[%d] %s - %s %s (%s-%s), tipo=%s, ingreso=%s, maxGuardia=%d%n",
                medico.getMatricula(),
                medico.getCuilCuit(),
                medico.getNombre(),
                medico.getApellido(),
                medico.getTipoDocumento(),
                medico.getNroDocumento(),
                medico.getTipo(),
                medico.getFechaIngreso(),
                medico.getMaxCantGuardia());
    }

    private static long parseMatricula(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("matricula must be a number");
        }
    }

    private static int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a number");
        }
    }

    private static void requireArgs(String[] args, int expected, String usage) {
        if (args.length < expected) {
            throw new IllegalArgumentException("Usage: " + usage);
        }
    }

    // ==================== PACIENTE COMMANDS ====================
    
    private static void listPacientes() {
        List<Paciente> pacientes = pacienteDao.findAll();
        if (pacientes.isEmpty()) {
            System.out.println("No pacientes found.");
            return;
        }
        pacientes.forEach(App::printPaciente);
    }

    private static void getPaciente(String[] args) {
        requireArgs(args, 3, "get-paciente <tipoDocumento> <nroDocumento>");
        Optional<Paciente> paciente = pacienteDao.findByTipoDocumentoAndNroDocumento(args[1], args[2]);
        if (paciente.isPresent()) {
            printPaciente(paciente.get());
        } else {
            System.out.println("Paciente not found.");
        }
    }

    private static void createPaciente(String[] args) {
        requireArgs(args, 7, "create-paciente <tipoDocumento> <nroDocumento> <nombre> <apellido> <tipo> <fechaNacimiento> <sexo>");
        LocalDate fechaNacimiento = LocalDate.parse(args[5]);
        char sexo = args[6].charAt(0);
        Paciente paciente = new Paciente(args[1], args[2], args[3], args[4], "PACIENTE", fechaNacimiento, sexo);
        Paciente created = pacienteDao.create(paciente);
        System.out.println("Paciente created successfully.");
        printPaciente(created);
    }

    private static void updatePaciente(String[] args) {
        requireArgs(args, 7, "update-paciente <tipoDocumento> <nroDocumento> <nombre> <apellido> <tipo> <fechaNacimiento> <sexo>");
        LocalDate fechaNacimiento = LocalDate.parse(args[5]);
        char sexo = args[6].charAt(0);
        Paciente paciente = new Paciente(args[1], args[2], args[3], args[4], "PACIENTE", fechaNacimiento, sexo);
        Paciente updated = pacienteDao.update(paciente);
        System.out.println("Paciente updated successfully.");
        printPaciente(updated);
    }

    private static void deletePaciente(String[] args) {
        requireArgs(args, 3, "delete-paciente <tipoDocumento> <nroDocumento>");
        boolean deleted = pacienteDao.delete(args[1], args[2]);
        if (deleted) {
            System.out.println("Paciente deleted successfully.");
        } else {
            System.out.println("Paciente not found.");
        }
    }

    private static void printPaciente(Paciente paciente) {
        System.out.printf("Paciente[%s-%s] %s %s, nacimiento=%s, sexo=%s%n",
                paciente.getTipoDocumento(),
                paciente.getNroDocumento(),
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getFechaNacimiento(),
                paciente.getSexo());
    }

    // ==================== SECTOR COMMANDS ====================
    
    private static void listSectores() {
        List<Sector> sectores = sectorDao.findAll();
        if (sectores.isEmpty()) {
            System.out.println("No sectores found.");
            return;
        }
        sectores.forEach(App::printSector);
    }

    private static void getSector(String[] args) {
        requireArgs(args, 2, "get-sector <idSector>");
        int idSector = parseInteger(args[1], "idSector");
        Optional<Sector> sector = sectorDao.findById(idSector);
        if (sector.isPresent()) {
            printSector(sector.get());
        } else {
            System.out.println("Sector not found.");
        }
    }

    private static void createSector(String[] args) {
        requireArgs(args, 2, "create-sector <descripcion>");
        Sector sector = new Sector(0, args[1]);
        Sector created = sectorDao.create(sector);
        System.out.println("Sector created successfully.");
        printSector(created);
    }

    private static void updateSector(String[] args) {
        requireArgs(args, 3, "update-sector <idSector> <descripcion>");
        int idSector = parseInteger(args[1], "idSector");
        Sector sector = new Sector(idSector, args[2]);
        Sector updated = sectorDao.update(sector);
        System.out.println("Sector updated successfully.");
        printSector(updated);
    }

    private static void deleteSector(String[] args) {
        requireArgs(args, 2, "delete-sector <idSector>");
        int idSector = parseInteger(args[1], "idSector");
        boolean deleted = sectorDao.delete(idSector);
        if (deleted) {
            System.out.println("Sector deleted successfully.");
        } else {
            System.out.println("Sector not found.");
        }
    }

    private static void printSector(Sector sector) {
        System.out.printf("Sector[%d] %s%n", sector.getIdSector(), sector.getDescripcion());
    }

    // ==================== HABITACION COMMANDS ====================
    
    private static void listHabitaciones() {
        List<Habitacion> habitaciones = habitacionDao.findAll();
        if (habitaciones.isEmpty()) {
            System.out.println("No habitaciones found.");
            return;
        }
        habitaciones.forEach(App::printHabitacion);
    }

    private static void getHabitacion(String[] args) {
        requireArgs(args, 2, "get-habitacion <nroHabitacion>");
        int nroHabitacion = parseInteger(args[1], "nroHabitacion");
        Optional<Habitacion> habitacion = habitacionDao.findById(nroHabitacion);
        if (habitacion.isPresent()) {
            printHabitacion(habitacion.get());
        } else {
            System.out.println("Habitacion not found.");
        }
    }

    private static void createHabitacion(String[] args) {
        requireArgs(args, 4, "create-habitacion <piso> <orientacion> <idSector>");
        int piso = parseInteger(args[1], "piso");
        int idSector = parseInteger(args[3], "idSector");
        Habitacion habitacion = new Habitacion(0, piso, args[2], idSector);
        Habitacion created = habitacionDao.create(habitacion);
        System.out.println("Habitacion created successfully.");
        printHabitacion(created);
    }

    private static void updateHabitacion(String[] args) {
        requireArgs(args, 5, "update-habitacion <nroHabitacion> <piso> <orientacion> <idSector>");
        int nroHabitacion = parseInteger(args[1], "nroHabitacion");
        int piso = parseInteger(args[2], "piso");
        int idSector = parseInteger(args[4], "idSector");
        Habitacion habitacion = new Habitacion(nroHabitacion, piso, args[3], idSector);
        Habitacion updated = habitacionDao.update(habitacion);
        System.out.println("Habitacion updated successfully.");
        printHabitacion(updated);
    }

    private static void deleteHabitacion(String[] args) {
        requireArgs(args, 2, "delete-habitacion <nroHabitacion>");
        int nroHabitacion = parseInteger(args[1], "nroHabitacion");
        boolean deleted = habitacionDao.delete(nroHabitacion);
        if (deleted) {
            System.out.println("Habitacion deleted successfully.");
        } else {
            System.out.println("Habitacion not found.");
        }
    }

    private static void printHabitacion(Habitacion habitacion) {
        System.out.printf("Habitacion[%d] piso=%d, orientacion=%s, sector=%d%n",
                habitacion.getNroHabitacion(),
                habitacion.getPiso(),
                habitacion.getOrientacion(),
                habitacion.getIdSector());
    }

    // ==================== INTERNACION COMMANDS ====================
    
    private static void listInternaciones() {
        List<Internacion> internaciones = internacionDao.findAll();
        if (internaciones.isEmpty()) {
            System.out.println("No internaciones found.");
            return;
        }
        internaciones.forEach(App::printInternacion);
    }

    private static void listInternacionesActivas() {
        List<Internacion> internaciones = internacionDao.findActivasInternaciones();
        if (internaciones.isEmpty()) {
            System.out.println("No active internaciones found.");
            return;
        }
        internaciones.forEach(App::printInternacion);
    }

    private static void getInternacion(String[] args) {
        requireArgs(args, 2, "get-internacion <nroInternacion>");
        int nroInternacion = parseInteger(args[1], "nroInternacion");
        Optional<Internacion> internacion = internacionDao.findById(nroInternacion);
        if (internacion.isPresent()) {
            printInternacion(internacion.get());
        } else {
            System.out.println("Internacion not found.");
        }
    }

    private static void createInternacion(String[] args) {
        requireArgs(args, 6, "create-internacion <fechaInicio> <fechaFin|null> <tipoDocumento> <nroDocumento> <matricula>");
        LocalDate fechaInicio = LocalDate.parse(args[1]);
        LocalDate fechaFin = args[2].equalsIgnoreCase("null") ? null : LocalDate.parse(args[2]);
        long matricula = parseMatricula(args[5]);
        Internacion internacion = new Internacion(0, fechaInicio, fechaFin, args[3], args[4], matricula);
        Internacion created = internacionDao.create(internacion);
        System.out.println("Internacion created successfully.");
        printInternacion(created);
    }

    private static void updateInternacion(String[] args) {
        requireArgs(args, 7, "update-internacion <nroInternacion> <fechaInicio> <fechaFin|null> <tipoDocumento> <nroDocumento> <matricula>");
        int nroInternacion = parseInteger(args[1], "nroInternacion");
        LocalDate fechaInicio = LocalDate.parse(args[2]);
        LocalDate fechaFin = args[3].equalsIgnoreCase("null") ? null : LocalDate.parse(args[3]);
        long matricula = parseMatricula(args[6]);
        Internacion internacion = new Internacion(nroInternacion, fechaInicio, fechaFin, args[4], args[5], matricula);
        Internacion updated = internacionDao.update(internacion);
        System.out.println("Internacion updated successfully.");
        printInternacion(updated);
    }

    private static void deleteInternacion(String[] args) {
        requireArgs(args, 2, "delete-internacion <nroInternacion>");
        int nroInternacion = parseInteger(args[1], "nroInternacion");
        boolean deleted = internacionDao.delete(nroInternacion);
        if (deleted) {
            System.out.println("Internacion deleted successfully.");
        } else {
            System.out.println("Internacion not found.");
        }
    }

    private static void printInternacion(Internacion internacion) {
        System.out.printf("Internacion[%d] inicio=%s, fin=%s, paciente=%s-%s, medico=%d%n",
                internacion.getNroInternacion(),
                internacion.getFechaInicio(),
                internacion.getFechaFin() != null ? internacion.getFechaFin() : "En curso",
                internacion.getTipoDocumento(),
                internacion.getNroDocumento(),
                internacion.getMatricula());
    }

    // ==================== GUARDIA COMMANDS ====================
    
    private static void listGuardias() {
        List<Guardia> guardias = guardiaDao.findAll();
        if (guardias.isEmpty()) {
            System.out.println("No guardias found.");
            return;
        }
        guardias.forEach(App::printGuardia);
    }

    private static void getGuardia(String[] args) {
        requireArgs(args, 2, "get-guardia <nroGuardia>");
        int nroGuardia = parseInteger(args[1], "nroGuardia");
        Optional<Guardia> guardia = guardiaDao.findById(nroGuardia);
        if (guardia.isPresent()) {
            printGuardia(guardia.get());
        } else {
            System.out.println("Guardia not found.");
        }
    }

    private static void createGuardia(String[] args) {
        requireArgs(args, 5, "create-guardia <fechaHora> <matricula> <codEspecialidad> <idTurno>");
        LocalDateTime fechaHora = LocalDateTime.parse(args[1]);
        long matricula = parseMatricula(args[2]);
        int codEspecialidad = parseInteger(args[3], "codEspecialidad");
        int idTurno = parseInteger(args[4], "idTurno");
        Guardia guardia = new Guardia(0, fechaHora, matricula, codEspecialidad, idTurno);
        Guardia created = guardiaDao.create(guardia);
        System.out.println("Guardia created successfully.");
        printGuardia(created);
    }

    private static void updateGuardia(String[] args) {
        requireArgs(args, 6, "update-guardia <nroGuardia> <fechaHora> <matricula> <codEspecialidad> <idTurno>");
        int nroGuardia = parseInteger(args[1], "nroGuardia");
        LocalDateTime fechaHora = LocalDateTime.parse(args[2]);
        long matricula = parseMatricula(args[3]);
        int codEspecialidad = parseInteger(args[4], "codEspecialidad");
        int idTurno = parseInteger(args[5], "idTurno");
        Guardia guardia = new Guardia(nroGuardia, fechaHora, matricula, codEspecialidad, idTurno);
        Guardia updated = guardiaDao.update(guardia);
        System.out.println("Guardia updated successfully.");
        printGuardia(updated);
    }

    private static void deleteGuardia(String[] args) {
        requireArgs(args, 2, "delete-guardia <nroGuardia>");
        int nroGuardia = parseInteger(args[1], "nroGuardia");
        boolean deleted = guardiaDao.delete(nroGuardia);
        if (deleted) {
            System.out.println("Guardia deleted successfully.");
        } else {
            System.out.println("Guardia not found.");
        }
    }

    private static void printGuardia(Guardia guardia) {
        System.out.printf("Guardia[%d] fechaHora=%s, medico=%d, especialidad=%d, turno=%d%n",
                guardia.getNroGuardia(),
                guardia.getFechaHora(),
                guardia.getMatricula(),
                guardia.getCodEspecialidad(),
                guardia.getIdTurno());
    }

    // ==================== USAGE ====================
    
    private static void printUsage() {
        System.out.println("Hospital CLI - available commands:");
        System.out.println();
        System.out.println("=== MEDICO ===");
        System.out.println("  list-medicos");
        System.out.println("  get-medico <matricula>");
        System.out.println("  create-medico <tipoDocumento> <nroDocumento> <nombre> <apellido> <tipo> <matricula> <cuilCuit> <fechaIngreso> <maxCantGuardia> <periodoVacaciones>");
        System.out.println("  update-medico <tipoDocumento> <nroDocumento> <nombre> <apellido> <tipo> <matricula> <cuilCuit> <fechaIngreso> <maxCantGuardia> <periodoVacaciones>");
        System.out.println("  delete-medico <matricula>");
        System.out.println();
        System.out.println("=== PACIENTE ===");
        System.out.println("  list-pacientes");
        System.out.println("  get-paciente <tipoDocumento> <nroDocumento>");
        System.out.println("  create-paciente <tipoDocumento> <nroDocumento> <nombre> <apellido> <tipo> <fechaNacimiento> <sexo>");
        System.out.println("  update-paciente <tipoDocumento> <nroDocumento> <nombre> <apellido> <tipo> <fechaNacimiento> <sexo>");
        System.out.println("  delete-paciente <tipoDocumento> <nroDocumento>");
        System.out.println();
        System.out.println("=== SECTOR ===");
        System.out.println("  list-sectores");
        System.out.println("  get-sector <idSector>");
        System.out.println("  create-sector <descripcion>");
        System.out.println("  update-sector <idSector> <descripcion>");
        System.out.println("  delete-sector <idSector>");
        System.out.println();
        System.out.println("=== HABITACION ===");
        System.out.println("  list-habitaciones");
        System.out.println("  get-habitacion <nroHabitacion>");
        System.out.println("  create-habitacion <piso> <orientacion> <idSector>");
        System.out.println("  update-habitacion <nroHabitacion> <piso> <orientacion> <idSector>");
        System.out.println("  delete-habitacion <nroHabitacion>");
        System.out.println();
        System.out.println("=== INTERNACION ===");
        System.out.println("  list-internaciones");
        System.out.println("  list-internaciones-activas");
        System.out.println("  get-internacion <nroInternacion>");
        System.out.println("  create-internacion <fechaInicio> <fechaFin|null> <tipoDocumento> <nroDocumento> <matricula>");
        System.out.println("  update-internacion <nroInternacion> <fechaInicio> <fechaFin|null> <tipoDocumento> <nroDocumento> <matricula>");
        System.out.println("  delete-internacion <nroInternacion>");
        System.out.println();
        System.out.println("=== GUARDIA ===");
        System.out.println("  list-guardias");
        System.out.println("  get-guardia <nroGuardia>");
        System.out.println("  create-guardia <fechaHora> <matricula> <codEspecialidad> <idTurno>");
        System.out.println("  update-guardia <nroGuardia> <fechaHora> <matricula> <codEspecialidad> <idTurno>");
        System.out.println("  delete-guardia <nroGuardia>");
    }
}
