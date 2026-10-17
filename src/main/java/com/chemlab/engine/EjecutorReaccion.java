package com.chemlab.engine;

import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.ResultadoQuimico;

public interface EjecutorReaccion {

    ResultadoQuimico ejecutar(
            Reaccion reaccion,
            Experimento experimento
    );
}