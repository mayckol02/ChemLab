package com.chemlab.engine;

import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.value.ResultadoEstequiometrico;

public interface ReglaReaccion {

    boolean puedeEjecutarse(
            Reaccion reaccion,
            Experimento experimento
    );

    ResultadoQuimico ejecutar(
            Reaccion reaccion,
            Experimento experimento
    );

    ResultadoQuimico ejecutar(
            Reaccion reaccion,
            Experimento experimento,
            ResultadoEstequiometrico resultadoEstequiometrico
    );

}