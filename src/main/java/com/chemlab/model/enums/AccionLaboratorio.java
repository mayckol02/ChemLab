package com.chemlab.model.enums;

/**
 * Representa los símbolos de entrada (eventos discretos) que pueden
 * provocar transiciones de estado en el autómata de un proceso químico.
 */
public enum AccionLaboratorio {

    /** Carga de reactivos principales al sistema o cámara. */
    CARGAR_REACTIVO,

    /** Agregado de un catalizador, semilla o componente auxiliar. */
    AGREGAR_CATALIZADOR,

    /** Aumento de temperatura o activación de fuente de calor. */
    CALENTAR,

    /** Reducción de temperatura activa. */
    ENFRIAR,

    /** Aumento de la presión en la cámara. */
    COMPRIMIR,

    /** Reducción o liberación de presión. */
    DESCOMPRIMIR,

    /** Acción física de mezclar o agitar. */
    MEZCLAR,

    /** Chispa, llama o fuente de energía puntual para iniciar. */
    ACTIVAR_IGNICION,

    /** Tiempo de residencia o de espera para que ocurra la reacción. */
    ESPERAR,

    /** Parada del proceso o extracción del producto. */
    FINALIZAR,

    /** Acción específica de agregar agua (hidratación, disolución). */
    AGREGAR_AGUA,

    /** Generación de plasma, usado típicamente en CVD. */
    INICIAR_PLASMA,

    /** Selección del método a utilizar para un proceso. */
    SELECCIONAR_METODO
}
