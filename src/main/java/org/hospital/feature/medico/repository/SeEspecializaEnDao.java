package org.hospital.feature.medico.repository;

import org.hospital.common.exception.DataAccessException;

/**
 * DAO para la tabla SE_ESPECIALIZA_EN.
 * Permite verificar si un médico tiene una especialidad dada.
 */
public interface SeEspecializaEnDao {

    /**
     * Devuelve true si existe un registro (matricula, cod_especialidad)
     * en SE_ESPECIALIZA_EN.
     */
    boolean existsByMatriculaAndEspecialidad(long matricula, int codEspecialidad)
            throws DataAccessException;
}
