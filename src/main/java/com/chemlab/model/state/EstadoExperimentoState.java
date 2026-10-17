package com.chemlab.model.state;

import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.enums.EstadoExperimento;

public interface EstadoExperimentoState {

    void iniciar(Experimento experimento);

    void completar(
            Experimento experimento,
            ResultadoQuimico resultado
    );

    void fallar(Experimento experimento);

    EstadoExperimento getTipo();
}