package com.chemlab.engine;

import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.value.Cantidad;
import com.chemlab.model.value.ResultadoEstequiometrico;
import com.chemlab.service.ConversorQuimico;

import java.util.Map;

public class ReglaReaccionBasica implements ReglaReaccion {

    private final CalculadorEstequiometrico calculadorEstequiometrico;
    private final CalculadorProductos calculadorProductos;

    public ReglaReaccionBasica() {

        ConversorQuimico conversorQuimico =
                new ConversorQuimico();

        this.calculadorEstequiometrico =
                new CalculadorEstequiometrico(
                        conversorQuimico
                );

        this.calculadorProductos =
                new CalculadorProductos();
    }

    @Override
    public boolean puedeEjecutarse(
            Reaccion reaccion,
            Experimento experimento
    ) {
        if (reaccion == null || experimento == null) {
            return false;
        }

        ResultadoEstequiometrico resultado =
                calculadorEstequiometrico.calcular(
                        reaccion,
                        experimento.getInventario(),
                        experimento.getCondiciones()
                );

        return resultado != null
                && resultado.getAvanceMaximo() > 0;
    }

    @Override
    public ResultadoQuimico ejecutar(
            Reaccion reaccion,
            Experimento experimento
    ) {

        if (!puedeEjecutarse(reaccion, experimento)) {

            return new ResultadoQuimico(
                    Map.of(),
                    false
            );
        }

        ResultadoEstequiometrico resultadoEstequiometrico =
                calculadorEstequiometrico.calcular(
                        reaccion,
                        experimento.getInventario(),
                        experimento.getCondiciones()
                );

        Map<Sustancia, Cantidad> productos =
                calculadorProductos.calcular(
                        reaccion,
                        resultadoEstequiometrico
                                .getAvanceMaximo()
                );

        return new ResultadoQuimico(
                productos,
                true
        );
    }

    @Override
    public ResultadoQuimico ejecutar(
            Reaccion reaccion,
            Experimento experimento,
            ResultadoEstequiometrico resultadoEstequiometrico
    ) {

        if (reaccion == null
                || experimento == null
                || resultadoEstequiometrico == null) {

            return new ResultadoQuimico(
                    Map.of(),
                    false
            );
        }

        Map<Sustancia, Cantidad> productos =
                calculadorProductos.calcular(
                        reaccion,
                        resultadoEstequiometrico
                                .getAvanceMaximo()
                );

        return new ResultadoQuimico(
                productos,
                true
        );
    }
}