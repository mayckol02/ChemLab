package com.chemlab.automata;

import com.chemlab.model.entity.ResultadoQuimico;

/**
 * Value object que representa el resultado de aplicar una AccionLaboratorio
 * sobre un ProcesoQuimico.
 */
public final class ResultadoAccion {

    private final boolean transicionExitosa;
    private final EstadoQuimico estadoActual;
    private final boolean procesoCompleto;
    private final ResultadoQuimico resultadoQuimico;
    private final String mensaje;

    private ResultadoAccion(
            boolean transicionExitosa,
            EstadoQuimico estadoActual,
            boolean procesoCompleto,
            ResultadoQuimico resultadoQuimico,
            String mensaje
    ) {
        this.transicionExitosa = transicionExitosa;
        this.estadoActual = estadoActual;
        this.procesoCompleto = procesoCompleto;
        this.resultadoQuimico = resultadoQuimico;
        this.mensaje = mensaje != null ? mensaje : "";
    }

    public static ResultadoAccion exito(EstadoQuimico estadoActual, String mensaje) {
        return new ResultadoAccion(true, estadoActual, false, null, mensaje);
    }

    public static ResultadoAccion completado(EstadoQuimico estadoActual, ResultadoQuimico resultado, String mensaje) {
        return new ResultadoAccion(true, estadoActual, true, resultado, mensaje);
    }

    public static ResultadoAccion fallo(EstadoQuimico estadoActual, String mensaje) {
        return new ResultadoAccion(false, estadoActual, false, null, mensaje);
    }

    public boolean isTransicionExitosa() {
        return transicionExitosa;
    }

    /** Alias expresivo de {@link #isTransicionExitosa()}. */
    public boolean isExitoso() {
        return transicionExitosa;
    }

    public EstadoQuimico getEstadoActual() {
        return estadoActual;
    }

    public boolean isProcesoCompleto() {
        return procesoCompleto;
    }

    /** Alias expresivo de {@link #isProcesoCompleto()}. */
    public boolean isCompletado() {
        return procesoCompleto;
    }

    public ResultadoQuimico getResultadoQuimico() {
        return resultadoQuimico;
    }

    /** Devuelve el resultado químico envuelto en un {@link java.util.Optional}. */
    public java.util.Optional<ResultadoQuimico> getResultadoQuimicoOpcional() {
        return java.util.Optional.ofNullable(resultadoQuimico);
    }

    public String getMensaje() {
        return mensaje;
    }
}
