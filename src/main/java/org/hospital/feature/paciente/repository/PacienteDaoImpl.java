package org.hospital.feature.paciente.repository;

import org.hospital.common.domain.Persona;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hospital.common.config.DatabaseConfig;
import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.paciente.domain.Paciente;

public class PacienteDaoImpl implements PacienteDao {
    private static final Logger logger = Logger.getLogger(PacienteDaoImpl.class.getName());
    private static final String INSERT_PERSONA_SQL = 
            "INSERT INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_PACIENTE_SQL = 
            "INSERT INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES (?, ?, ?, ?)";
    private static final String BASE_SELECT = 
            "SELECT p.tipo_documento, p.nro_documento, p.nombre, p.apellido, p.tipo, " +
            "pac.fecha_nacimiento, pac.sexo " +
            "FROM PACIENTE pac " +
            "JOIN PERSONA p ON p.tipo_documento = pac.tipo_documento AND p.nro_documento = pac.nro_documento";
    private static final String SELECT_BY_ID_SQL = BASE_SELECT + " WHERE pac.tipo_documento = ? AND pac.nro_documento = ?";
    private static final String SELECT_ALL_SQL = BASE_SELECT + " ORDER BY p.apellido, p.nombre";
    private static final String UPDATE_PERSONA_SQL = 
            "UPDATE PERSONA SET nombre = ?, apellido = ?, tipo = ? WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String UPDATE_PACIENTE_SQL = 
            "UPDATE PACIENTE SET fecha_nacimiento = ?, sexo = ? WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String DELETE_PACIENTE_SQL = "DELETE FROM PACIENTE WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String DELETE_PERSONA_SQL = "DELETE FROM PERSONA WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String SELECT_PERSONA_EXISTS_SQL =
            "SELECT 1 FROM PERSONA WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String COUNT_PACIENTE_BY_DOC_SQL =
            "SELECT COUNT(*) FROM PACIENTE WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String COUNT_MEDICO_BY_DOC_SQL =
            "SELECT COUNT(*) FROM MEDICO WHERE tipo_documento = ? AND nro_documento = ?";

    @Override
    public Paciente create(Paciente paciente) throws DataAccessException {
        logger.info("Creating paciente: " + paciente.getNroDocumento());
        validatePaciente(paciente);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);

            // 1) Verificar si la PERSONA ya existe
            boolean personaExiste = false;
            try (PreparedStatement checkStmt = connection.prepareStatement(SELECT_PERSONA_EXISTS_SQL)) {
                checkStmt.setString(1, paciente.getTipoDocumento());
                checkStmt.setString(2, paciente.getNroDocumento());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        personaExiste = true;
                    }
                }
            }

            // 2) Insertar en PERSONA solo si NO existe
            if (!personaExiste) {
                logger.info("Persona does not exist yet, inserting into PERSONA for paciente " 
                        + paciente.getTipoDocumento() + " " + paciente.getNroDocumento());
                try (PreparedStatement personaStmt = connection.prepareStatement(INSERT_PERSONA_SQL)) {
                    personaStmt.setString(1, paciente.getTipoDocumento());
                    personaStmt.setString(2, paciente.getNroDocumento());
                    personaStmt.setString(3, paciente.getNombre());
                    personaStmt.setString(4, paciente.getApellido());
                    personaStmt.setString(5, paciente.getTipo());
                    personaStmt.executeUpdate();
                }
            } else {
                logger.info("Persona already exists for paciente " 
                        + paciente.getTipoDocumento() + " " + paciente.getNroDocumento()
                        + ", skipping INSERT into PERSONA");
            }

            // 3) Insertar SIEMPRE en PACIENTE (si ya existía el paciente, fallará por PK, como corresponde)
            try (PreparedStatement pacienteStmt = connection.prepareStatement(INSERT_PACIENTE_SQL)) {
                pacienteStmt.setString(1, paciente.getTipoDocumento());
                pacienteStmt.setString(2, paciente.getNroDocumento());
                pacienteStmt.setDate(3, toSqlDate(paciente.getFechaNacimiento()));
                pacienteStmt.setString(4, String.valueOf(paciente.getSexo()));
                pacienteStmt.executeUpdate();
            }

            connection.commit();
            logger.info("Successfully created paciente: " + paciente.getNroDocumento());
            return paciente;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for paciente creation");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to create paciente: " + e.getMessage());
            throw new DataAccessException("Error creating paciente", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }
   
    @Override
    public Optional<Paciente> findByTipoDocumentoAndNroDocumento(String tipoDocumento, String nroDocumento) 
            throws DataAccessException {
        logger.fine("Finding paciente: " + tipoDocumento + " " + nroDocumento);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
                statement.setString(1, tipoDocumento);
                statement.setString(2, nroDocumento);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapToPaciente(resultSet));
                    }
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find paciente: " + e.getMessage());
            throw new DataAccessException("Error finding paciente", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public List<Paciente> findAll() throws DataAccessException {
        logger.fine("Finding all pacientes");
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet resultSet = statement.executeQuery()) {
                List<Paciente> pacientes = new ArrayList<>();
                while (resultSet.next()) {
                    pacientes.add(mapToPaciente(resultSet));
                }
                logger.fine("Found " + pacientes.size() + " pacientes");
                return pacientes;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find pacientes: " + e.getMessage());
            throw new DataAccessException("Error finding pacientes", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public Paciente update(Paciente paciente) throws DataAccessException {
        logger.info("Updating paciente: " + paciente.getNroDocumento());
        validatePaciente(paciente);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            // Update PERSONA table first
            try (PreparedStatement personaStmt = connection.prepareStatement(UPDATE_PERSONA_SQL)) {
                personaStmt.setString(1, paciente.getNombre());
                personaStmt.setString(2, paciente.getApellido());
                personaStmt.setString(3, paciente.getTipo());
                personaStmt.setString(4, paciente.getTipoDocumento());
                personaStmt.setString(5, paciente.getNroDocumento());
                personaStmt.executeUpdate();
            }
            
            // Then update PACIENTE table
            try (PreparedStatement pacienteStmt = connection.prepareStatement(UPDATE_PACIENTE_SQL)) {
                pacienteStmt.setDate(1, toSqlDate(paciente.getFechaNacimiento()));
                pacienteStmt.setString(2, String.valueOf(paciente.getSexo()));
                pacienteStmt.setString(3, paciente.getTipoDocumento());
                pacienteStmt.setString(4, paciente.getNroDocumento());
                pacienteStmt.executeUpdate();
            }
            
            connection.commit();
            logger.info("Successfully updated paciente: " + paciente.getNroDocumento());
            return paciente;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for paciente update");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to update paciente: " + e.getMessage());
            throw new DataAccessException("Error updating paciente", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean delete(String tipoDocumento, String nroDocumento) throws DataAccessException {
        logger.info("Deleting paciente: " + tipoDocumento + " " + nroDocumento);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);

            // 1) Borrar de PACIENTE
            try (PreparedStatement pacienteStmt = connection.prepareStatement(DELETE_PACIENTE_SQL)) {
                pacienteStmt.setString(1, tipoDocumento);
                pacienteStmt.setString(2, nroDocumento);
                int rows = pacienteStmt.executeUpdate();
                if (rows == 0) {
                    connection.rollback();
                    logger.warning("Paciente not found for deletion: " + tipoDocumento + " " + nroDocumento);
                    return false;
                }
            }

            // 2) Verificar si la PERSONA sigue teniendo algún rol (PACIENTE o MEDICO)
            int countPacientes = 0;
            int countMedicos = 0;

            try (PreparedStatement countPacStmt = connection.prepareStatement(COUNT_PACIENTE_BY_DOC_SQL)) {
                countPacStmt.setString(1, tipoDocumento);
                countPacStmt.setString(2, nroDocumento);
                try (ResultSet rs = countPacStmt.executeQuery()) {
                    if (rs.next()) {
                        countPacientes = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement countMedStmt = connection.prepareStatement(COUNT_MEDICO_BY_DOC_SQL)) {
                countMedStmt.setString(1, tipoDocumento);
                countMedStmt.setString(2, nroDocumento);
                try (ResultSet rs = countMedStmt.executeQuery()) {
                    if (rs.next()) {
                        countMedicos = rs.getInt(1);
                    }
                }
            }

            // 3) Solo borrar de PERSONA si ya no tiene NINGÚN rol
            if (countPacientes == 0 && countMedicos == 0) {
                logger.info("Persona has no remaining roles, deleting from PERSONA: "
                        + tipoDocumento + " " + nroDocumento);
                try (PreparedStatement personaStmt = connection.prepareStatement(DELETE_PERSONA_SQL)) {
                    personaStmt.setString(1, tipoDocumento);
                    personaStmt.setString(2, nroDocumento);
                    personaStmt.executeUpdate();
                }
            } else {
                logger.info("Persona still has other roles (paciente/medico), skipping delete from PERSONA: "
                        + tipoDocumento + " " + nroDocumento);
            }

            connection.commit();
            logger.info("Successfully deleted paciente: " + tipoDocumento + " " + nroDocumento);
            return true;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for paciente deletion");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to delete paciente: " + e.getMessage());
            throw new DataAccessException("Error deleting paciente", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }


    private Paciente mapToPaciente(ResultSet resultSet) throws SQLException {
        return new Paciente(
                resultSet.getString("tipo_documento"),
                resultSet.getString("nro_documento"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido"),
                resultSet.getString("tipo"),
                toLocalDate(resultSet.getDate("fecha_nacimiento")),
                resultSet.getString("sexo").charAt(0)
        );
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private Date toSqlDate(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    private void validatePaciente(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("paciente must not be null");
        }
        if (paciente.getTipoDocumento() == null) {
            throw new IllegalArgumentException("tipoDocumento must not be null");
        }
        if (paciente.getNroDocumento() == null) {
            throw new IllegalArgumentException("nroDocumento must not be null");
        }
    }
}

