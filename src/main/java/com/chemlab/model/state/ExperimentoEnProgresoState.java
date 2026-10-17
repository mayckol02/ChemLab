package com.chemlab.model.state;

import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.enums.EstadoExperimento;

public class ExperimentoEnProgresoState
        implements EstadoExperimentoState {

    @Override
    public void iniciar(Experimento experimento) {
        throw new IllegalStateException(
                "El experimento ya está en progreso"
        );
    }

    @Override
    public void completar(
            Experimento experimento,
            ResultadoQuimico resultado
    ) {
        if (resultado == null) {
            throw new IllegalArgumentException(
                    "El resultado no puede ser null"
            );
        }

        if (resultado.isExitoso()) {
            experimento.cambiarEstado(
                    new ExperimentoCompletadoState()
            );
        } else {
            experimento.cambiarEstado(
                    new ExperimentoFallidoState()
            );
        }

        experimento.establecerResultado(resultado);
    }

    @Override
    public void fallar(Experimento experimento) {
        experimento.cambiarEstado(
                new ExperimentoFallidoState()
        );
    }

    @Override
    public EstadoExperimento getTipo() {
        return EstadoExperimento.EN_PROGRESO;
    }
}