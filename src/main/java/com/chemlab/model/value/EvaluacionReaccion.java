package com.chemlab.model.value;

import com.chemlab.model.enums.TipoFalloReaccion;

import java.util.Objects;

public class EvaluacionReaccion {

    private final boolean posible;
    private final ResultadoEstequiometrico resultadoEstequiometrico;
    private final String motivo;
    private final TipoFalloReaccion tipoFallo;

    private EvaluacionReaccion(
            boolean posible,
            ResultadoEstequiometrico resultadoEstequiometrico,
            String motivo,
            TipoFalloReaccion tipoFallo
    ) {
        this.posible = posible;
        this.resultadoEstequiometrico =
                resultadoEstequiometrico;
        this.motivo = motivo;
        this.tipoFallo = tipoFallo;
    }

    public static EvaluacionReaccion exitosa(
            ResultadoEstequiometrico resultado
    ) {
        return new EvaluacionReaccion(
                true,
                Objects.requireNonNull(
                        resultado,
                        "El resultado no puede ser null"
                ),
                null,
                TipoFalloReaccion.NINGUNO
        );
    }

    public static EvaluacionReaccion fallida(
            TipoFalloReaccion tipoFallo,
            String motivo
    ) {
        Objects.requireNonNull(
                tipoFallo,
                "El tipo de fallo no puede ser null"
        );

        if (tipoFallo == TipoFalloReaccion.NINGUNO) {
            throw new IllegalArgumentException(
                    "Una evaluación fallida debe tener un tipo de fallo"
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "El motivo no puede estar vacío"
            );
        }

        return new EvaluacionReaccion(
                false,
                null,
                motivo,
                tipoFallo
        );
    }

    public boolean esPosible() {
        return posible;
    }

    public ResultadoEstequiometrico getResultadoEstequiometrico() {
        return resultadoEstequiometrico;
    }

    public String getMotivo() {
        return motivo;
    }

    public TipoFalloReaccion getTipoFallo() {
        return tipoFallo;
    }
}