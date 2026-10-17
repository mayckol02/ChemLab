package com.chemlab.model.state;

import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.enums.EstadoExperimento;

public class ExperimentoNoIniciadoState
        implements EstadoExperimentoState {

    @Override
    public void iniciar(Experimento experimento) {
        experimento.cambiarEstado(
                new ExperimentoEnProgresoState()
        );
    }

    @Override
    public void completar(
            Experimento experimento,
            ResultadoQuimico resultado
    ) {
        throw new IllegalStateException(
                "No se puede completar un experimento que no ha iniciado"
        );
    }

    @Override
    public void fallar(Experimento experimento) {
        throw new IllegalStateException(
                "No se puede marcar como fallido un experimento que no ha iniciado"
        );
    }

    @Override
    public EstadoExperimento getTipo() {
        return EstadoExperimento.NO_INICIADO;
    }
}