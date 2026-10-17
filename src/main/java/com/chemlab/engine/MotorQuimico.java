package com.chemlab.engine;

import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.value.ResultadoEstequiometrico;
import com.chemlab.service.ConversorQuimico;
import com.chemlab.model.value.EvaluacionReaccion;

import java.util.Objects;

public class MotorQuimico
        implements EjecutorReaccion {

    private final ReglaReaccion reglaReaccion;
    private final CalculadorEstequiometrico calculadorEstequiometrico;
    private final AplicadorReaccion aplicadorReaccion;

    public MotorQuimico(
            ReglaReaccion reglaReaccion,
            CalculadorEstequiometrico calculadorEstequiometrico,
            AplicadorReaccion aplicadorReaccion
    ) {
        this.reglaReaccion = Objects.requireNonNull(
                reglaReaccion,
                "La regla de reacción no puede ser null"
        );

        this.calculadorEstequiometrico =
                Objects.requireNonNull(
                        calculadorEstequiometrico,
                        "El calculador estequiométrico no puede ser null"
                );

        this.aplicadorReaccion =
                Objects.requireNonNull(
                        aplicadorReaccion,
                        "El aplicador de reacción no puede ser null"
                );
    }

    public MotorQuimico(
            ReglaReaccion reglaReaccion
    ) {
        this(
                reglaReaccion,
                new CalculadorEstequiometrico(
                        new ConversorQuimico()
                ),
                new AplicadorReaccion()
        );
    }

    @Override
    public ResultadoQuimico ejecutar(
            Reaccion reaccion,
            Experimento experimento
    ) {
        Objects.requireNonNull(
                reaccion,
                "La reacción no puede ser null"
        );

        Objects.requireNonNull(
                experimento,
                "El experimento no puede ser null"
        );

        EvaluacionReaccion evaluacion =
                calculadorEstequiometrico.evaluar(
                        reaccion,
                        experimento.getInventario(),
                        experimento.getCondiciones()
                );

        if (!evaluacion.esPosible()) {
            return new ResultadoQuimico(
                    java.util.Map.of(),
                    false
            );
        }

        ResultadoEstequiometrico resultadoEstequiometrico =
                evaluacion.getResultadoEstequiometrico();

        ResultadoQuimico resultado =
                reglaReaccion.ejecutar(
                        reaccion,
                        experimento,
                        resultadoEstequiometrico
                );

        if (!resultado.isExitoso()) {
            return resultado;
        }

        aplicadorReaccion.aplicar(
                reaccion,
                resultadoEstequiometrico,
                experimento.getInventario()
        );

        return resultado;
    }

    public boolean puedeEjecutarse(
            Reaccion reaccion,
            Experimento experimento
    ) {
        Objects.requireNonNull(
                reaccion,
                "La reacción no puede ser null"
        );

        Objects.requireNonNull(
                experimento,
                "El experimento no puede ser null"
        );

        return reglaReaccion.puedeEjecutarse(
                reaccion,
                experimento
        );
    }
}