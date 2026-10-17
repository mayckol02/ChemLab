package com.chemlab.model.state;

import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.enums.EstadoExperimento;

public class ExperimentoCompletadoState
        implements EstadoExperimentoState {

    @Override
    public void iniciar(Experimento experimento) {
        throw new IllegalStateException(
                "El experimento ya está completado"
        );
    }

    @Override
    public void completar(
            Experimento experimento,
            ResultadoQuimico resultado
    ) {
        throw new IllegalStateException(
                "El experimento ya está completado"
        );
    }

    @Override
    public void fallar(Experimento experimento) {
        throw new IllegalStateException(
                "Un experimento completado no puede pasar a fallido"
        );
    }

    @Override
    public EstadoExperimento getTipo() {
        return EstadoExperimento.COMPLETADO;
    }
}