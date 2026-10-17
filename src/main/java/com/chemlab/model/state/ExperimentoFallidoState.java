package com.chemlab.model.state;

import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.enums.EstadoExperimento;

public class ExperimentoFallidoState
        implements EstadoExperimentoState {

    @Override
    public void iniciar(Experimento experimento) {
        throw new IllegalStateException(
                "Un experimento fallido no puede reiniciarse"
        );
    }

    @Override
    public void completar(
            Experimento experimento,
            ResultadoQuimico resultado
    ) {
        throw new IllegalStateException(
                "Un experimento fallido no puede completarse"
        );
    }

    @Override
    public void fallar(Experimento experimento) {
        throw new IllegalStateException(
                "El experimento ya está marcado como fallido"
        );
    }

    @Override
    public EstadoExperimento getTipo() {
        return EstadoExperimento.FALLIDO;
    }
}