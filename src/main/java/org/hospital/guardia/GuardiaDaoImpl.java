package org.hospital.guardia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hospital.config.DatabaseConfig;
import org.hospital.exception.DataAccessException;

public class GuardiaDaoImpl implements GuardiaDao {
    private static final Logger logger = Logger.getLogger(GuardiaDaoImpl.class.getName());
    private static final String INSERT_SQL = 
            "INSERT INTO GUARDIA (fecha_hora, matricula, cod_especialidad, id_turno) " +
            "VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = 
            "SELECT nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno " +
            "FROM GUARDIA WHERE nro_guardia = ?";
    private static final String SELECT_ALL_SQL = 
            "SELECT nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno " +
            "FROM GUARDIA ORDER BY fecha_hora DESC";
    private static final String SELECT_BY_MEDICO_SQL = 
            "SELECT nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno " +
            "FROM GUARDIA WHERE matricula = ? ORDER BY fecha_hora DESC";
    private static final String SELECT_BY_ESPECIALIDAD_SQL = 
            "SELECT nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno " +
            "FROM GUARDIA WHERE cod_especialidad = ? ORDER BY fecha_hora DESC";
    private static final String UPDATE_SQL = 
            "UPDATE GUARDIA SET fecha_hora = ?, matricula = ?, cod_especialidad = ?, " +
            "id_turno = ? WHERE nro_guardia = ?";
    private static final String DELETE_SQL = 
            "DELETE FROM GUARDIA WHERE nro_guardia = ?";

    @Override
    public Guardia create(Guardia guardia) throws DataAccessException {
        logger.info("Creating guardia: matricula=" + guardia.getMatricula() + ", fecha=" + guardia.getFechaHora());
        validateGuardia(guardia);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, 
                    new String[]{"nro_guardia"})) {
                statement.setTimestamp(1, toSqlTimestamp(guardia.getFechaHora()));
                statement.setLong(2, guardia.getMatricula());
                statement.setInt(3, guardia.getCodEspecialidad());
                statement.setInt(4, guardia.getIdTurno());
                statement.executeUpdate();
                
                // Get generated ID
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        guardia.setNroGuardia(generatedKeys.getInt(1));
                    }
                }
            }
            
            connection.commit();
            logger.info("Successfully created guardia: " + guardia.getNroGuardia());
            return guardia;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for guardia creation");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to create guardia: " + e.getMessage());
            throw new DataAccessException("Error creating guardia", e);
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
    public Optional<Guardia> findById(int nroGuardia) throws DataAccessException {
        logger.fine("Finding guardia by id: " + nroGuardia);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
                statement.setInt(1, nroGuardia);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapToGuardia(resultSet));
                    }
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find guardia: " + e.getMessage());
            throw new DataAccessException("Error finding guardia", e);
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
    public List<Guardia> findAll() throws DataAccessException {
        logger.fine("Finding all guardias");
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet resultSet = statement.executeQuery()) {
                List<Guardia> guardias = new ArrayList<>();
                while (resultSet.next()) {
                    guardias.add(mapToGuardia(resultSet));
                }
                logger.fine("Found " + guardias.size() + " guardias");
                return guardias;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find guardias: " + e.getMessage());
            throw new DataAccessException("Error finding guardias", e);
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
    public List<Guardia> findByMedico(long matricula) throws DataAccessException {
        logger.fine("Finding guardias by medico: " + matricula);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_MEDICO_SQL)) {
                statement.setLong(1, matricula);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Guardia> guardias = new ArrayList<>();
                    while (resultSet.next()) {
                        guardias.add(mapToGuardia(resultSet));
                    }
                    return guardias;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find guardias by medico: " + e.getMessage());
            throw new DataAccessException("Error finding guardias by medico", e);
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
    public List<Guardia> findByEspecialidad(int codEspecialidad) throws DataAccessException {
        logger.fine("Finding guardias by especialidad: " + codEspecialidad);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ESPECIALIDAD_SQL)) {
                statement.setInt(1, codEspecialidad);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Guardia> guardias = new ArrayList<>();
                    while (resultSet.next()) {
                        guardias.add(mapToGuardia(resultSet));
                    }
                    return guardias;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find guardias by especialidad: " + e.getMessage());
            throw new DataAccessException("Error finding guardias by especialidad", e);
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
    public Guardia update(Guardia guardia) throws DataAccessException {
        logger.info("Updating guardia: " + guardia.getNroGuardia());
        validateGuardia(guardia);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
                statement.setTimestamp(1, toSqlTimestamp(guardia.getFechaHora()));
                statement.setLong(2, guardia.getMatricula());
                statement.setInt(3, guardia.getCodEspecialidad());
                statement.setInt(4, guardia.getIdTurno());
                statement.setInt(5, guardia.getNroGuardia());
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException("Guardia not found with nro: " + guardia.getNroGuardia());
                }
            }
            
            connection.commit();
            logger.info("Successfully updated guardia: " + guardia.getNroGuardia());
            return guardia;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for guardia update");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to update guardia: " + e.getMessage());
            throw new DataAccessException("Error updating guardia", e);
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
    public boolean delete(int nroGuardia) throws DataAccessException {
        logger.info("Deleting guardia: " + nroGuardia);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
                statement.setInt(1, nroGuardia);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    logger.info("Successfully deleted guardia: " + nroGuardia);
                    return true;
                } else {
                    connection.rollback();
                    logger.warning("Guardia not found for deletion: " + nroGuardia);
                    return false;
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for guardia deletion");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to delete guardia: " + e.getMessage());
            throw new DataAccessException("Error deleting guardia", e);
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

    private Guardia mapToGuardia(ResultSet resultSet) throws SQLException {
        return new Guardia(
                resultSet.getInt("nro_guardia"),
                toLocalDateTime(resultSet.getTimestamp("fecha_hora")),
                resultSet.getLong("matricula"),
                resultSet.getInt("cod_especialidad"),
                resultSet.getInt("id_turno")
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private Timestamp toSqlTimestamp(LocalDateTime dateTime) {
        return dateTime == null ? null : Timestamp.valueOf(dateTime);
    }

    private void validateGuardia(Guardia guardia) {
        if (guardia == null) {
            throw new IllegalArgumentException("guardia must not be null");
        }
        if (guardia.getFechaHora() == null) {
            throw new IllegalArgumentException("fechaHora must not be null");
        }
    }
}
