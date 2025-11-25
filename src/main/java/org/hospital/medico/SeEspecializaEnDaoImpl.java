package org.hospital.medico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.hospital.config.DatabaseConfig;
import org.hospital.exception.DataAccessException;

/**
 * Implementación JDBC del DAO de SE_ESPECIALIZA_EN.
 */
public class SeEspecializaEnDaoImpl implements SeEspecializaEnDao {

    private static final Logger logger = Logger.getLogger(SeEspecializaEnDaoImpl.class.getName());

    // Si quisieras exigir además hace_guardia = 1, agregás "AND hace_guardia = 1"
    private static final String EXISTS_SQL =
        "SELECT 1 FROM SE_ESPECIALIZA_EN " +
        "WHERE matricula = ? AND cod_especialidad = ?";

    @Override
    public boolean existsByMatriculaAndEspecialidad(long matricula, int codEspecialidad)
            throws DataAccessException {

        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(EXISTS_SQL)) {
                stmt.setLong(1, matricula);
                stmt.setInt(2, codEspecialidad);

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();  // true si encontró al menos una fila
                }
            }
        } catch (SQLException e) {
            logger.severe("Error comprobando SE_ESPECIALIZA_EN para matricula="
                    + matricula + ", cod_especialidad=" + codEspecialidad
                    + ": " + e.getMessage());
            throw new DataAccessException(
                    "Error comprobando la combinación médico/especialidad", e);
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
}
