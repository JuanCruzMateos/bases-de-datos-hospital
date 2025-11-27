package org.hospital.internacion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.sql.CallableStatement;

import org.hospital.config.DatabaseConfig;
import org.hospital.exception.DataAccessException;

public class InternacionDaoImpl implements InternacionDao {
    private static final Logger logger = Logger.getLogger(InternacionDaoImpl.class.getName());
    // private static final String INSERT_SQL = "INSERT INTO INTERNACION (fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) "
            // +
            // "VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula "
            +
            "FROM INTERNACION WHERE nro_internacion = ?";
    private static final String SELECT_ALL_SQL = "SELECT nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula "
            +
            "FROM INTERNACION ORDER BY fecha_inicio DESC";
    private static final String SELECT_BY_PACIENTE_SQL = "SELECT nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula "
            +
            "FROM INTERNACION WHERE tipo_documento = ? AND nro_documento = ? ORDER BY fecha_inicio DESC";
    private static final String SELECT_ACTIVAS_SQL = "SELECT nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula "
            +
            "FROM INTERNACION WHERE fecha_fin IS NULL ORDER BY fecha_inicio DESC";
    private static final String UPDATE_SQL = "UPDATE INTERNACION SET fecha_inicio = ?, fecha_fin = ?, tipo_documento = ?, "
            +
            "nro_documento = ?, matricula = ? WHERE nro_internacion = ?";
    private static final String DELETE_SQL = "DELETE FROM INTERNACION WHERE nro_internacion = ?";

    @Override
    public Internacion create(Internacion internacion) throws DataAccessException {
        // Compatibilidad para el código viejo
        return create(internacion, null, null);
    }

    @Override
    public Internacion create(Internacion internacion,
            Integer nroHabitacion,
            Integer nroCama) throws DataAccessException {
        logger.info("Creating internacion via sp_crear_internacion: paciente=" +
                internacion.getTipoDocumento() + "/" + internacion.getNroDocumento());
        validateInternacion(internacion);

        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);

            String call = "{ call sp_crear_internacion(?, ?, ?, ?, ?, ?, ?, ?) }";

            try (CallableStatement stmt = connection.prepareCall(call)) {

                // 1: tipo_documento
                stmt.setString(1, internacion.getTipoDocumento());
                // 2: nro_documento
                stmt.setString(2, internacion.getNroDocumento());
                // 3: matricula
                stmt.setLong(3, internacion.getMatricula());
                // 4: fecha_inicio
                stmt.setDate(4, toSqlDate(internacion.getFechaInicio()));
                // 5: fecha_fin
                if (internacion.getFechaFin() != null) {
                    stmt.setDate(5, toSqlDate(internacion.getFechaFin()));
                } else {
                    stmt.setNull(5, Types.DATE);
                }

                // 6 y 7: nro_habitacion / nro_cama opcionales
                if (nroHabitacion != null && nroCama != null) {
                    stmt.setInt(6, nroHabitacion);
                    stmt.setInt(7, nroCama);
                } else {
                    stmt.setNull(6, Types.INTEGER);
                    stmt.setNull(7, Types.INTEGER);
                }

                // 8: OUT nro_internacion
                stmt.registerOutParameter(8, Types.INTEGER);

                stmt.execute();

                int generatedId = stmt.getInt(8);
                internacion.setNroInternacion(generatedId);
            }

            connection.commit();
            logger.info("Successfully created internacion: " + internacion.getNroInternacion());
            return internacion;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for internacion creation");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            throw new DataAccessException("Error creating internacion via stored procedure", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void changeBed(int nroInternacion, int nroHabitacion, int nroCama) throws DataAccessException {
        logger.info("Changing bed via sp_cambiar_cama_internacion: nro_internacion=" +
                nroInternacion + ", hab=" + nroHabitacion + ", cama=" + nroCama);

        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);

            // p_fecha_ingreso tiene DEFAULT SYSTIMESTAMP, así que usamos solo 3 params
            String call = "{ call sp_cambiar_cama_internacion(?, ?, ?) }";

            try (CallableStatement stmt = connection.prepareCall(call)) {
                stmt.setInt(1, nroInternacion);
                stmt.setInt(2, nroHabitacion);
                stmt.setInt(3, nroCama);
                stmt.execute();
            }

            connection.commit();
            logger.info("Bed changed successfully for internacion: " + nroInternacion);

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for bed change of internacion " + nroInternacion);
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            throw new DataAccessException("Error changing bed via stored procedure", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public Optional<Internacion> findById(int nroInternacion) throws DataAccessException {
        logger.fine("Finding internacion by id: " + nroInternacion);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
                statement.setInt(1, nroInternacion);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapToInternacion(resultSet));
                    }
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find internacion: " + e.getMessage());
            throw new DataAccessException("Error finding internacion", e);
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
    public List<Internacion> findAll() throws DataAccessException {
        logger.fine("Finding all internaciones");
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                    ResultSet resultSet = statement.executeQuery()) {
                List<Internacion> internaciones = new ArrayList<>();
                while (resultSet.next()) {
                    internaciones.add(mapToInternacion(resultSet));
                }
                logger.fine("Found " + internaciones.size() + " internaciones");
                return internaciones;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find internaciones: " + e.getMessage());
            throw new DataAccessException("Error finding internaciones", e);
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
    public List<Internacion> findByPaciente(String tipoDocumento, String nroDocumento)
            throws DataAccessException {
        logger.fine("Finding internaciones by paciente: " + tipoDocumento + "/" + nroDocumento);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_PACIENTE_SQL)) {
                statement.setString(1, tipoDocumento);
                statement.setString(2, nroDocumento);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Internacion> internaciones = new ArrayList<>();
                    while (resultSet.next()) {
                        internaciones.add(mapToInternacion(resultSet));
                    }
                    return internaciones;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find internaciones by paciente: " + e.getMessage());
            throw new DataAccessException("Error finding internaciones by paciente", e);
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
    public List<Internacion> findActivasInternaciones() throws DataAccessException {
        logger.fine("Finding active internaciones");
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ACTIVAS_SQL);
                    ResultSet resultSet = statement.executeQuery()) {
                List<Internacion> internaciones = new ArrayList<>();
                while (resultSet.next()) {
                    internaciones.add(mapToInternacion(resultSet));
                }
                return internaciones;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find active internaciones: " + e.getMessage());
            throw new DataAccessException("Error finding active internaciones", e);
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
    public Internacion update(Internacion internacion) throws DataAccessException {
        logger.info("Updating internacion: " + internacion.getNroInternacion());
        validateInternacion(internacion);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
                statement.setDate(1, toSqlDate(internacion.getFechaInicio()));
                if (internacion.getFechaFin() != null) {
                    statement.setDate(2, toSqlDate(internacion.getFechaFin()));
                } else {
                    statement.setNull(2, Types.DATE);
                }
                statement.setString(3, internacion.getTipoDocumento());
                statement.setString(4, internacion.getNroDocumento());
                statement.setLong(5, internacion.getMatricula());
                statement.setInt(6, internacion.getNroInternacion());
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException("Internacion not found with nro: " + internacion.getNroInternacion());
                }
            }

            connection.commit();
            logger.info("Successfully updated internacion: " + internacion.getNroInternacion());
            return internacion;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for internacion update");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to update internacion: " + e.getMessage());
            throw new DataAccessException("Error updating internacion", e);
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
    public boolean delete(int nroInternacion) throws DataAccessException {
        logger.info("Deleting internacion: " + nroInternacion);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
                statement.setInt(1, nroInternacion);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    logger.info("Successfully deleted internacion: " + nroInternacion);
                    return true;
                } else {
                    connection.rollback();
                    logger.warning("Internacion not found for deletion: " + nroInternacion);
                    return false;
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for internacion deletion");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to delete internacion: " + e.getMessage());
            throw new DataAccessException("Error deleting internacion", e);
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

    private Internacion mapToInternacion(ResultSet resultSet) throws SQLException {
        return new Internacion(
                resultSet.getInt("nro_internacion"),
                toLocalDate(resultSet.getDate("fecha_inicio")),
                toLocalDate(resultSet.getDate("fecha_fin")),
                resultSet.getString("tipo_documento"),
                resultSet.getString("nro_documento"),
                resultSet.getLong("matricula"));
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private Date toSqlDate(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    private void validateInternacion(Internacion internacion) {
        if (internacion == null) {
            throw new IllegalArgumentException("internacion must not be null");
        }
        if (internacion.getFechaInicio() == null) {
            throw new IllegalArgumentException("fechaInicio must not be null");
        }
        if (internacion.getTipoDocumento() == null) {
            throw new IllegalArgumentException("tipoDocumento must not be null");
        }
        if (internacion.getNroDocumento() == null) {
            throw new IllegalArgumentException("nroDocumento must not be null");
        }
        if (internacion.getFechaFin() != null &&
                internacion.getFechaInicio().isAfter(internacion.getFechaFin())) {
            throw new IllegalArgumentException("fechaInicio must be before or equal to fechaFin");
        }
    }
}
