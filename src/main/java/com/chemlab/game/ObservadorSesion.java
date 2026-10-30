package com.chemlab.game;

import com.chemlab.automata.ResultadoAccion;
import com.chemlab.model.entity.Experimento;

/**
 * Observador de eventos de la sesión de juego (patrón Observer).
 *
 * <p>La UI implementa esta interfaz para recibir notificaciones de cambios
 * del dominio sin que el dominio conozca a la UI.</p>
 */
public interface ObservadorSesion {

    /**
     * Se invoca cuando el experimento ha iniciado correctamente.
     *
     * @param experimento experimento recién iniciado (solo lectura semántica).
     */
    void onExperimentoIniciado(Experimento experimento);

    /**
     * Se invoca cada vez que el jugador ejecuta una acción sobre el autómata.
     *
     * @param resultado resultado de la acción (estado actual, mensaje, etc.)
     */
    void onAccionEjecutada(ResultadoAccion resultado);

    /**
     * Se invoca cuando el experimento termina con éxito.
     *
     * @param experimento experimento ya completado.
     */
    void onExperimentoCompletado(Experimento experimento);

    /**
     * Se invoca cuando el jugador abandona el experimento.
     *
     * @param experimento experimento marcado como fallido.
     */
    void onExperimentoAbandonado(Experimento experimento);

    default void onExperimentoFallido(Experimento experimento, String motivo) {
        onExperimentoAbandonado(experimento);
    }
}
