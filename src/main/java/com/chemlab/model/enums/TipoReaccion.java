package com.chemlab.model.enums;

/**
 * Clasifica el tipo de proceso químico representado por una Reaccion.
 * Permite al motor y a la UI diferenciar comportamientos y condiciones
 * típicas de cada categoría sin necesidad de hardcodear lógica por ID.
 */
public enum TipoReaccion {

    /** Dos o más sustancias se combinan para formar una sola. */
    SINTESIS,

    /** Reacción rápida y exotérmica con liberación de luz y calor. */
    COMBUSTION,

    /** Un elemento o compuesto gana oxígeno o pierde electrones. */
    OXIDACION,

    /** Una sustancia disuelta se separa como sólido de la solución. */
    PRECIPITACION,

    /** Un sólido se disuelve en un solvente formando una solución. */
    DISOLUCION,

    /**
     * Proceso de alta presión y alta temperatura (High Pressure High
     * Temperature) para síntesis de materiales como el diamante.
     */
    HPHT,

    /**
     * Deposición química desde fase vapor (Chemical Vapour Deposition)
     * para crecimiento de películas o cristales.
     */
    CVD,

    /**
     * Proceso Haber-Bosch para síntesis de amoníaco a partir
     * de N₂ e H₂ con catalizador a alta presión y temperatura.
     */
    HABER_BOSCH,

    /** Degradación electroquímica de un metal por humedad y oxígeno. */
    CORROSION,

    /** Reacción de un óxido o sal con agua. */
    HIDRATACION,

    /** Separación física de componentes de una mezcla. */
    SEPARACION,

    /** Transferencia de electrones entre un oxidante y un reductor. */
    REDOX
}
