package com.chemlab.model.value;

import com.chemlab.model.entity.Sustancia;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResultadoEstequiometrico {

    private final Sustancia reactivoLimitante;
    private final double avanceMaximo;
    private final Map<Sustancia, Cantidad> cantidadesConsumidas;

    public ResultadoEstequiometrico(
            Sustancia reactivoLimitante,
            double avanceMaximo,
            Map<Sustancia, Cantidad> cantidadesConsumidas
    ) {
        if (avanceMaximo < 0) {
            throw new IllegalArgumentException(
                    "El avance máximo no puede ser negativo"
            );
        }

        this.reactivoLimitante = Objects.requireNonNull(
                reactivoLimitante,
                "El reactivo limitante no puede ser null"
        );

        Objects.requireNonNull(
                cantidadesConsumidas,
                "Las cantidades consumidas no pueden ser null"
        );

        this.avanceMaximo = avanceMaximo;

        Map<Sustancia, Cantidad> copia = new HashMap<>();

        for (Map.Entry<Sustancia, Cantidad> entrada
                : cantidadesConsumidas.entrySet()) {

            Sustancia sustancia = Objects.requireNonNull(
                    entrada.getKey(),
                    "La sustancia no puede ser null"
            );

            Cantidad cantidad = Objects.requireNonNull(
                    entrada.getValue(),
                    "La cantidad no puede ser null"
            );

            copia.put(
                    sustancia,
                    cantidad
            );
        }

        this.cantidadesConsumidas = copia;
    }

    public Sustancia getReactivoLimitante() {
        return reactivoLimitante;
    }

    public double getAvanceMaximo() {
        return avanceMaximo;
    }

    public Map<Sustancia, Cantidad> getCantidadesConsumidas() {
        return Collections.unmodifiableMap(
                cantidadesConsumidas
        );
    }

    public Cantidad getCantidadConsumida(
            Sustancia sustancia
    ) {
        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        return cantidadesConsumidas.get(sustancia);
    }
}