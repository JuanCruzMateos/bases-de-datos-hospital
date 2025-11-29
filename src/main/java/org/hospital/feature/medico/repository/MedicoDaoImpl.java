package org.hospital.feature.medico.repository;

import org.hospital.feature.medico.domain.Especialidad;
import org.hospital.feature.medico.domain.Medico;
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

import org.hospital.common.config.DatabaseConfig;
import org.hospital.common.exception.DataAccessException;

public class MedicoDaoImpl implements MedicoDao {
    private static final Logger logger = Logger.getLogger(MedicoDaoImpl.class.getName());
    private static final String INSERT_PERSONA_SQL =
            "INSERT INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_MEDICO_SQL =
            "INSERT INTO MEDICO (matricula, cuil_cuit, fecha_ingreso, foto, max_cant_guardia, tipo_documento, nro_documento) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String BASE_SELECT =
            "SELECT m.matricula, m.cuil_cuit, m.fecha_ingreso, m.foto, m.max_cant_guardia, "
                    + "m.tipo_documento, m.nro_documento, "
                    + "p.nombre, p.apellido, p.tipo "
                    + "FROM MEDICO m "
                    + "JOIN PERSONA p ON p.tipo_documento = m.tipo_documento AND p.nro_documento = m.nro_documento";
    private static final String SELECT_BY_ID_SQL = BASE_SELECT + " WHERE m.matricula = ?";
    private static final String SELECT_ALL_SQL = BASE_SELECT + " ORDER BY p.apellido, p.nombre, m.matricula";
    private static final String UPDATE_PERSONA_SQL =
            "UPDATE PERSONA SET nombre = ?, apellido = ?, tipo = ? WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String UPDATE_MEDICO_SQL =
            "UPDATE MEDICO SET cuil_cuit = ?, fecha_ingreso = ?, foto = ?, max_cant_guardia = ? WHERE matricula = ?";
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
    private static final String SELECT_PERSONA_EXISTS_SQL =
            "SELECT 1 FROM PERSONA WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String COUNT_PACIENTE_BY_DOC_SQL =
            "SELECT COUNT(*) FROM PACIENTE WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String COUNT_MEDICO_BY_DOC_SQL =
            "SELECT COUNT(*) FROM MEDICO WHERE tipo_documento = ? AND nro_documento = ?";
    private static final String SELECT_ESPECIALIDADES_CODIGOS_SQL =
            "SELECT cod_especialidad FROM SE_ESPECIALIZA_EN WHERE matricula = ?";
    // Borrar una sola especialidad puntual del médico
    private static final String DELETE_ESPECIALIDAD_SQL =
            "DELETE FROM SE_ESPECIALIZA_EN WHERE matricula = ? AND cod_especialidad = ?";

    // Contar guardias asociadas a una combinación (matricula, cod_especialidad)
    private static final String COUNT_GUARDIA_BY_MEDICO_AND_ESPECIALIDAD_SQL =
            "SELECT COUNT(*) FROM GUARDIA WHERE matricula = ? AND cod_especialidad = ?";
    private static final String COUNT_GUARDIAS_BY_MEDICO_SQL =
            "SELECT COUNT(*) FROM GUARDIA WHERE matricula = ?";
    private static final String COUNT_INTERNACIONES_ACTIVAS_BY_MEDICO_SQL =
            "SELECT COUNT(*) FROM INTERNACION WHERE matricula = ? AND fecha_fin IS NULL";
    private static final String COUNT_RECORRIDOS_BY_MEDICO_SQL =
            "SELECT COUNT(*) FROM RECORRIDO WHERE matricula = ?";
    private static final String COUNT_COMENTARIOS_BY_MEDICO_SQL =
            "SELECT COUNT(*) FROM COMENTA_SOBRE cs " +
            "JOIN RECORRIDO r ON cs.id_recorrido = r.id_recorrido " +
            "WHERE r.matricula = ?";

    @Override
    public Medico create(Medico medico) throws DataAccessException {
        logger.info("Creating medico: " + medico.getMatricula());
        validateMedico(medico);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);

            // 1) Verificar si la PERSONA ya existe
            boolean personaExiste = false;
            try (PreparedStatement checkStmt = connection.prepareStatement(SELECT_PERSONA_EXISTS_SQL)) {
                checkStmt.setString(1, medico.getTipoDocumento());
                checkStmt.setString(2, medico.getNroDocumento());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        personaExiste = true;
                    }
                }
            }

            // 2) Insertar en PERSONA solo si NO existe
            if (!personaExiste) {
                logger.info("Persona does not exist yet, inserting into PERSONA for medico "
                        + medico.getTipoDocumento() + " " + medico.getNroDocumento());
                try (PreparedStatement personaStmt = connection.prepareStatement(INSERT_PERSONA_SQL)) {
                    personaStmt.setString(1, medico.getTipoDocumento());
                    personaStmt.setString(2, medico.getNroDocumento());
                    personaStmt.setString(3, medico.getNombre());
                    personaStmt.setString(4, medico.getApellido());
                    personaStmt.setString(5, medico.getTipo());
                    personaStmt.executeUpdate();
                }
            } else {
                logger.info("Persona already exists for medico "
                        + medico.getTipoDocumento() + " " + medico.getNroDocumento()
                        + ", skipping INSERT into PERSONA");
            }

            // 3) Insertar en MEDICO
            try (PreparedStatement medicoStmt = connection.prepareStatement(INSERT_MEDICO_SQL)) {
                medicoStmt.setLong(1, medico.getMatricula());
                medicoStmt.setString(2, medico.getCuilCuit());
                medicoStmt.setDate(3, toSqlDate(medico.getFechaIngreso()));
                if (medico.getFoto() != null) {
                    medicoStmt.setBytes(4, medico.getFoto());
                } else {
                    medicoStmt.setNull(4, java.sql.Types.BLOB);
                }
                medicoStmt.setInt(5, medico.getMaxCantGuardia());
                medicoStmt.setString(6, medico.getTipoDocumento());
                medicoStmt.setString(7, medico.getNroDocumento());
                medicoStmt.executeUpdate();
            }

            // 4) Insertar especialidades
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

            // 1) Actualizar PERSONA
            try (PreparedStatement personaStmt = connection.prepareStatement(UPDATE_PERSONA_SQL)) {
                personaStmt.setString(1, medico.getNombre());
                personaStmt.setString(2, medico.getApellido());
                personaStmt.setString(3, medico.getTipo());
                personaStmt.setString(4, medico.getTipoDocumento());
                personaStmt.setString(5, medico.getNroDocumento());
                personaStmt.executeUpdate();
            }

            // 2) Actualizar MEDICO
            try (PreparedStatement medicoStmt = connection.prepareStatement(UPDATE_MEDICO_SQL)) {
                medicoStmt.setString(1, medico.getCuilCuit());
                medicoStmt.setDate(2, toSqlDate(medico.getFechaIngreso()));
                if (medico.getFoto() != null) {
                    medicoStmt.setBytes(3, medico.getFoto());
                } else {
                    medicoStmt.setNull(3, Types.BLOB);
                }
                medicoStmt.setInt(4, medico.getMaxCantGuardia());
                medicoStmt.setLong(5, medico.getMatricula());
                medicoStmt.executeUpdate();
            }

            // 3) Actualizar SE_ESPECIALIZA_EN de forma conservadora

            // 3.a) Especialidades actuales en BD
            Set<Integer> actuales = getEspecialidadesCodigos(medico.getMatricula(), connection);

            // 3.b) Especialidades nuevas desde el formulario
            Set<Integer> nuevas = new HashSet<>();
            if (medico.getEspecialidades() != null) {
                for (Especialidad esp : medico.getEspecialidades()) {
                    nuevas.add(esp.getCodEspecialidad());
                }
            }

            // 3.c) Diferencias
            Set<Integer> aAgregar = new HashSet<>(nuevas);
            aAgregar.removeAll(actuales);     // en nuevas pero no en actuales

            Set<Integer> aQuitar = new HashSet<>(actuales);
            aQuitar.removeAll(nuevas);        // estaban antes y ahora desaparecieron

            // 3.d) Insertar solo las nuevas
            if (!aAgregar.isEmpty()) {
                try (PreparedStatement insertStmt = connection.prepareStatement(INSERT_ESPECIALIDAD_SQL)) {
                    for (Integer codEsp : aAgregar) {
                        insertStmt.setLong(1, medico.getMatricula());
                        insertStmt.setInt(2, codEsp);
                        insertStmt.executeUpdate();
                    }
                }
            }

            // 3.e) Quitar solo las que no tengan guardias asociadas
            if (!aQuitar.isEmpty()) {
                for (Integer codEsp : aQuitar) {

                    if (hasGuardiasForEspecialidad(medico.getMatricula(), codEsp, connection)) {
                        // Deshacemos todo el update del médico
                        try {
                            connection.rollback();
                        } catch (SQLException ex) {
                            logger.severe("Failed to rollback transaction after guardia check: " + ex.getMessage());
                        }

                        throw new IllegalArgumentException(
                            "No se puede quitar la especialidad con código " + codEsp +
                            " del médico " + medico.getMatricula() +
                            " porque tiene guardias asociadas a esa especialidad."
                        );
                    }

                    try (PreparedStatement deleteStmt = connection.prepareStatement(DELETE_ESPECIALIDAD_SQL)) {
                        deleteStmt.setLong(1, medico.getMatricula());
                        deleteStmt.setInt(2, codEsp);
                        deleteStmt.executeUpdate();
                    }
                }
            }

            // 4) Confirmar transacción
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

            // 1) Obtener datos de PERSONA asociados al médico
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

            // Validar que no tenga dependencias activas en otras tablas
            validateMedicoDeletable(connection, matricula);

            // 2) Borrar especialidades (FK)
            try (PreparedStatement especialidadStmt = connection.prepareStatement(DELETE_ALL_ESPECIALIDADES_SQL)) {
                especialidadStmt.setLong(1, matricula);
                especialidadStmt.executeUpdate();
            }

            // 3) Borrar de MEDICO
            try (PreparedStatement medicoStmt = connection.prepareStatement(DELETE_MEDICO_SQL)) {
                medicoStmt.setLong(1, matricula);
                medicoStmt.executeUpdate();
            }

            // 4) Verificar si la PERSONA sigue teniendo algún rol
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

            // 5) Solo borrar PERSONA si ya no tiene roles
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



    private void validateMedicoDeletable(Connection connection, long matricula) throws SQLException {
        long internacionesActivas = countByMedico(connection, COUNT_INTERNACIONES_ACTIVAS_BY_MEDICO_SQL, matricula);
        if (internacionesActivas > 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el medico " + matricula +
                            " porque es medico principal de internaciones activas (" + internacionesActivas + ").");
        }

        long guardias = countByMedico(connection, COUNT_GUARDIAS_BY_MEDICO_SQL, matricula);
        if (guardias > 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el medico " + matricula +
                            " porque tiene guardias asociadas (" + guardias + ").");
        }

        long recorridos = countByMedico(connection, COUNT_RECORRIDOS_BY_MEDICO_SQL, matricula);
        long comentarios = countByMedico(connection, COUNT_COMENTARIOS_BY_MEDICO_SQL, matricula);
        if (recorridos > 0 || comentarios > 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el medico " + matricula +
                            " porque tiene recorridos/rondas o comentarios asociados.");
        }
    }

    private long countByMedico(Connection connection, String sql, long matricula) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, matricula);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }
        }
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

    /**
     * Devuelve el conjunto de códigos de especialidad que el médico
     * tiene actualmente registrados en SE_ESPECIALIZA_EN.
     */
    private Set<Integer> getEspecialidadesCodigos(long matricula, Connection connection) throws SQLException {
        Set<Integer> codigos = new HashSet<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ESPECIALIDADES_CODIGOS_SQL)) {
            stmt.setLong(1, matricula);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    codigos.add(rs.getInt("cod_especialidad"));
                }
            }
        }
        return codigos;
    }

    /**
     * Indica si el médico tiene al menos una guardia registrada
     * para una especialidad dada.
     */
    private boolean hasGuardiasForEspecialidad(long matricula, int codEspecialidad, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(COUNT_GUARDIA_BY_MEDICO_AND_ESPECIALIDAD_SQL)) {
            stmt.setLong(1, matricula);
            stmt.setInt(2, codEspecialidad);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
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
