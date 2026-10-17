package com.chemlab.engine;

import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.value.Cantidad;
import com.chemlab.model.value.ResultadoEstequiometrico;

import java.util.Map;
import java.util.Objects;

public class AplicadorReaccion {

    private final CalculadorProductos calculadorProductos;

    public AplicadorReaccion() {
        this.calculadorProductos = new CalculadorProductos();
    }

    public void aplicar(
            Reaccion reaccion,
            ResultadoEstequiometrico resultado,
            Inventario inventario
    ) {
        Objects.requireNonNull(
                reaccion,
                "La reacción no puede ser null"
        );

        Objects.requireNonNull(
                resultado,
                "El resultado estequiométrico no puede ser null"
        );

        Objects.requireNonNull(
                inventario,
                "El inventario no puede ser null"
        );

        retirarReactivos(
                resultado,
                inventario
        );

        agregarProductos(
                reaccion,
                resultado,
                inventario
        );
    }

    private void retirarReactivos(
            ResultadoEstequiometrico resultado,
            Inventario inventario
    ) {
        for (Map.Entry<Sustancia, Cantidad> entrada
                : resultado.getCantidadesConsumidas().entrySet()) {

            Sustancia reactivo = entrada.getKey();
            Cantidad cantidadConsumida = entrada.getValue();

            inventario.retirar(
                    reactivo,
                    cantidadConsumida
            );
        }
    }

    private void agregarProductos(
            Reaccion reaccion,
            ResultadoEstequiometrico resultado,
            Inventario inventario
    ) {
        Map<Sustancia, Cantidad> productos =
                calculadorProductos.calcular(
                        reaccion,
                        resultado.getAvanceMaximo()
                );

        for (Map.Entry<Sustancia, Cantidad> entrada
                : productos.entrySet()) {

            inventario.agregar(
                    entrada.getKey(),
                    entrada.getValue()
            );
        }
    }
}