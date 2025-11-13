package org.hospital.medico;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.hospital.config.DatabaseConfig;
import org.hospital.exception.DataAccessException;

public class MedicoDaoImpl implements MedicoDao {
    private static final Logger logger = Logger.getLogger(MedicoDaoImpl.class.getName());
    private static final String INSERT_PERSONA_SQL =
            "INSERT INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_MEDICO_SQL =
            "INSERT INTO MEDICO (matricula, cuil_cuit, fecha_ingreso, foto, max_cant_guardia, periodo_vacaciones, tipo_documento, nro_documento) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String BASE_SELECT =
            "SELECT m.matricula, m.cuil_cuit, m.fecha_ingreso, m.foto, m.max_cant_guardia, m.periodo_vacaciones, "
                    + "m.tipo_documento, m.nro_documento, "
                    + "p.nombre, p.apellido, p.tipo "
                    + "FROM MEDICO m "
                    + "JOIN PERSONA p ON p.tipo_documento = m.tipo_documento AND p.nro_documento = m.nro_documento";
    private static final String SELECT_BY_ID_SQL = BASE_SELECT + " WHERE m.matricula = ?";
    private static final String SELECT_ALL_SQL = BASE_SELECT + " ORDER BY p.apellido, p.nombre, m.matricula";
    private static final String UPDATE_PERSONA_SQL =
            "UPDATE PERSONA SET nombre = ?, apellido = ?, tipo = ? WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String UPDATE_MEDICO_SQL =
            "UPDATE MEDICO SET cuil_cuit = ?, fecha_ingreso = ?, foto = ?, max_cant_guardia = ?, periodo_vacaciones = ? WHERE matricula = ?";
    private static final String DELETE_MEDICO_SQL = "DELETE FROM MEDICO WHERE matricula = ?";
    private static final String DELETE_PERSONA_SQL = "DELETE FROM PERSONA WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String INSERT_ESPECIALIDAD_SQL =
            "INSERT INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (?, ?, 0)";
    private static final String DELETE_ALL_ESPECIALIDADES_SQL =
            "DELETE FROM SE_ESPECIALIZA_EN WHERE matricula = ?";
    private static final String SELECT_ESPECIALIDADES_SQL =
            "SELECT e.cod_especialidad, e.descripcion, e.id_sector " +
            "FROM ESPECIALIDAD e " +
            "JOIN SE_ESPECIALIZA_EN se ON e.cod_especialidad = se.cod_especialidad " +
            "WHERE se.matricula = ?";

    @Override
    public Medico create(Medico medico) throws DataAccessException {
        logger.info("Creating medico: " + medico.getMatricula());
        validateMedico(medico);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            // Insert into PERSONA table first
            try (PreparedStatement personaStmt = connection.prepareStatement(INSERT_PERSONA_SQL)) {
                personaStmt.setString(1, medico.getTipoDocumento());
                personaStmt.setString(2, medico.getNroDocumento());
                personaStmt.setString(3, medico.getNombre());
                personaStmt.setString(4, medico.getApellido());
                personaStmt.setString(5, medico.getTipo());
                personaStmt.executeUpdate();
            }
            
            // Then insert into MEDICO table
            try (PreparedStatement medicoStmt = connection.prepareStatement(INSERT_MEDICO_SQL)) {
                bindMedico(medicoStmt, medico);
                medicoStmt.executeUpdate();
            }

            // Finally insert especialidades into SE_ESPECIALIZA_EN table
            if (medico.getEspecialidades() != null && !medico.getEspecialidades().isEmpty()) {
                try (PreparedStatement especialidadStmt = connection.prepareStatement(INSERT_ESPECIALIDAD_SQL)) {
                    for (Especialidad especialidad : medico.getEspecialidades()) {
                        especialidadStmt.setLong(1, medico.getMatricula());
                        especialidadStmt.setInt(2, especialidad.getCodEspecialidad());
                        especialidadStmt.executeUpdate();
                    }
                }
            }
            
            connection.commit();
            logger.info("Successfully created medico: " + medico.getMatricula());
            return medico;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for medico creation");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to create medico: " + e.getMessage());
            throw new DataAccessException("Error creating medico", e);
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
    public Optional<Medico> findByMatricula(long matricula) throws DataAccessException {
        logger.fine("Finding medico by matricula: " + matricula);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
                statement.setLong(1, matricula);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapToMedico(resultSet, connection));
                    }
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find medico: " + e.getMessage());
            throw new DataAccessException("Error finding medico", e);
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
    public List<Medico> findAll() throws DataAccessException {
        logger.fine("Finding all medicos");
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet resultSet = statement.executeQuery()) {
                List<Medico> medicos = new ArrayList<>();
                while (resultSet.next()) {
                    medicos.add(mapToMedico(resultSet, connection));
                }
                logger.fine("Found " + medicos.size() + " medicos");
                return medicos;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find medicos: " + e.getMessage());
            throw new DataAccessException("Error finding medicos", e);
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
    public Medico update(Medico medico) throws DataAccessException {
        logger.info("Updating medico: " + medico.getMatricula());
        validateMedico(medico);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            // Update PERSONA table first
            try (PreparedStatement personaStmt = connection.prepareStatement(UPDATE_PERSONA_SQL)) {
                personaStmt.setString(1, medico.getNombre());
                personaStmt.setString(2, medico.getApellido());
                personaStmt.setString(3, medico.getTipo());
                personaStmt.setString(4, medico.getTipoDocumento());
                personaStmt.setString(5, medico.getNroDocumento());
                personaStmt.executeUpdate();
            }
            
            // Then update MEDICO table
            try (PreparedStatement medicoStmt = connection.prepareStatement(UPDATE_MEDICO_SQL)) {
                medicoStmt.setString(1, medico.getCuilCuit());
                medicoStmt.setDate(2, toSqlDate(medico.getFechaIngreso()));
                if (medico.getFoto() != null) {
                    medicoStmt.setBytes(3, medico.getFoto());
                } else {
                    medicoStmt.setNull(3, Types.BLOB);
                }
                medicoStmt.setInt(4, medico.getMaxCantGuardia());
                medicoStmt.setString(5, medico.getPeriodoVacaciones());
                medicoStmt.setLong(6, medico.getMatricula());
                medicoStmt.executeUpdate();
            }

            // Update SE_ESPECIALIZA_EN table - delete all and re-insert
            try (PreparedStatement deleteStmt = connection.prepareStatement(DELETE_ALL_ESPECIALIDADES_SQL)) {
                deleteStmt.setLong(1, medico.getMatricula());
                deleteStmt.executeUpdate();
            }
            
            if (medico.getEspecialidades() != null && !medico.getEspecialidades().isEmpty()) {
                try (PreparedStatement insertStmt = connection.prepareStatement(INSERT_ESPECIALIDAD_SQL)) {
                    for (Especialidad especialidad : medico.getEspecialidades()) {
                        insertStmt.setLong(1, medico.getMatricula());
                        insertStmt.setInt(2, especialidad.getCodEspecialidad());
                        insertStmt.executeUpdate();
                    }
                }
            }
            
            connection.commit();
            logger.info("Successfully updated medico: " + medico.getMatricula());
            return medico;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for medico update");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to update medico: " + e.getMessage());
            throw new DataAccessException("Error updating medico", e);
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
    public boolean delete(long matricula) throws DataAccessException {
        logger.info("Deleting medico: " + matricula);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            // First get the persona info before deleting (within same transaction)
            String tipoDocumento = null;
            String nroDocumento = null;
            
            try (PreparedStatement selectStmt = connection.prepareStatement(
                    "SELECT tipo_documento, nro_documento FROM MEDICO WHERE matricula = ?")) {
                selectStmt.setLong(1, matricula);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        tipoDocumento = rs.getString("tipo_documento");
                        nroDocumento = rs.getString("nro_documento");
                    } else {
                        connection.rollback();
                        logger.warning("Medico not found for deletion: " + matricula);
                        return false;
                    }
                }
            }
            
            // Delete from SE_ESPECIALIZA_EN table first (due to FK constraint)
            try (PreparedStatement especialidadStmt = connection.prepareStatement(DELETE_ALL_ESPECIALIDADES_SQL)) {
                especialidadStmt.setLong(1, matricula);
                especialidadStmt.executeUpdate();
            }
            
            // Then delete from MEDICO table
            try (PreparedStatement medicoStmt = connection.prepareStatement(DELETE_MEDICO_SQL)) {
                medicoStmt.setLong(1, matricula);
                medicoStmt.executeUpdate();
            }
            
            // Finally delete from PERSONA table
            try (PreparedStatement personaStmt = connection.prepareStatement(DELETE_PERSONA_SQL)) {
                personaStmt.setString(1, tipoDocumento);
                personaStmt.setString(2, nroDocumento);
                personaStmt.executeUpdate();
            }
            
            connection.commit();
            logger.info("Successfully deleted medico: " + matricula);
            return true;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for medico deletion");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to delete medico: " + e.getMessage());
            throw new DataAccessException("Error deleting medico", e);
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

    private void bindMedico(PreparedStatement statement, Medico medico) throws SQLException {
        statement.setLong(1, medico.getMatricula());
        statement.setString(2, medico.getCuilCuit());
        statement.setDate(3, toSqlDate(medico.getFechaIngreso()));
        if (medico.getFoto() != null) {
            statement.setBytes(4, medico.getFoto());
        } else {
            statement.setNull(4, Types.BLOB);
        }
        statement.setInt(5, medico.getMaxCantGuardia());
        statement.setString(6, medico.getPeriodoVacaciones());
        statement.setString(7, medico.getTipoDocumento());
        statement.setString(8, medico.getNroDocumento());
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private Date toSqlDate(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    private Medico mapToMedico(ResultSet resultSet, Connection connection) throws SQLException, DataAccessException {
        long matricula = resultSet.getLong("matricula");
        List<Especialidad> especialidadesList = getEspecialidades(matricula, connection);
        Set<Especialidad> especialidades = new HashSet<>(especialidadesList);
        
        return new Medico(
                resultSet.getString("tipo_documento"),
                resultSet.getString("nro_documento"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido"),
                resultSet.getString("tipo"),
                matricula,
                resultSet.getString("cuil_cuit"),
                toLocalDate(resultSet.getDate("fecha_ingreso")),
                resultSet.getBytes("foto"),
                resultSet.getInt("max_cant_guardia"),
                resultSet.getString("periodo_vacaciones"),
                especialidades
        );
    }

    private List<Especialidad> getEspecialidades(long matricula, Connection connection) throws DataAccessException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ESPECIALIDADES_SQL)) {
            statement.setLong(1, matricula);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Especialidad> especialidades = new ArrayList<>();
                while (resultSet.next()) {
                    especialidades.add(new Especialidad(
                            resultSet.getInt("cod_especialidad"),
                            resultSet.getString("descripcion"),
                            resultSet.getInt("id_sector")
                    ));
                }
                return especialidades;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving especialidades for medico", e);
        }
    }  

    private void validateMedico(Medico medico) {
        if (medico == null) {
            throw new IllegalArgumentException("medico must not be null");
        }
        if (medico.getTipoDocumento() == null) {
            throw new IllegalArgumentException("tipoDocumento must not be null");
        }
        if (medico.getNroDocumento() == null) {
            throw new IllegalArgumentException("nroDocumento must not be null");
        }
    }
}
