package com.chemlab.ui.game;

import com.chemlab.game.procedure.ProtocoloProcedimiento;

import java.util.Objects;

/**
 * Construye mensajes de aprendizaje y corrección para las acciones de una práctica.
 */
public final class GeneradorRetroalimentacionPractica {

    public record Retroalimentacion(boolean positiva, String titulo, String detalle) {}

    private GeneradorRetroalimentacionPractica() {}

    public static Retroalimentacion generar(
            boolean accionValida,
            boolean practicaActiva,
            boolean modoLibre,
            String mensajeMotor,
            String estadoActual,
            ProtocoloProcedimiento.InfoPasoProcedimiento protocolo
    ) {
        Objects.requireNonNull(mensajeMotor, "El mensaje del motor no puede ser null");
        Objects.requireNonNull(estadoActual, "El estado actual no puede ser null");

        StringBuilder detalle = new StringBuilder(mensajeMotor)
                .append("\nEtapa actual: ").append(estadoActual).append('.');

        if (!practicaActiva) {
            detalle.append("\nLa práctica terminó; revisa el análisis y el registro en el cuaderno.");
        } else if (modoLibre) {
            if (accionValida) {
                detalle.append("\nObserva la muestra y compara los materiales y condiciones actuales "
                        + "con el objetivo de la práctica.");
            } else {
                detalle.append("\nQué revisar: materiales cargados, condiciones actuales y compatibilidad "
                        + "del equipo que aparecen en la consola.");
            }
        } else if (accionValida) {
            agregarSugerencia(detalle, protocolo, "Siguiente paso: ");
        } else if (protocolo != null) {
            agregarDetalle(detalle, protocolo.explicacionManejo(), "Qué revisar: ");
            agregarSugerencia(detalle, protocolo, "Para continuar: ");
        } else {
            detalle.append("\nQué revisar: la muestra, las condiciones indicadas y el equipo asignado.");
        }

        return new Retroalimentacion(
                accionValida,
                accionValida
                        ? modoLibre ? "OPERACIÓN REGISTRADA" : "PASO VALIDADO"
                        : "REVISA ESTE PASO",
                detalle.toString()
        );
    }

    private static void agregarSugerencia(
            StringBuilder detalle,
            ProtocoloProcedimiento.InfoPasoProcedimiento protocolo,
            String prefijo
    ) {
        if (protocolo != null) {
            agregarDetalle(detalle, protocolo.accionSiguienteSugerida(), prefijo);
        }
    }

    private static void agregarDetalle(StringBuilder detalle, String texto, String prefijo) {
        if (texto != null && !texto.isBlank()) {
            detalle.append('\n').append(prefijo).append(texto);
        }
    }
}
