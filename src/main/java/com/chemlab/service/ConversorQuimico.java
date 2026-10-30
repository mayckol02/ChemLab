package com.chemlab.service;

import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;

import java.util.Objects;

public class ConversorQuimico {

    public Cantidad convertirAMoles(
            Cantidad cantidad,
            Sustancia sustancia
    ) {
        Objects.requireNonNull(
                cantidad,
                "La cantidad no puede ser null"
        );

        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        if (cantidad.getUnidad() == UnidadCantidad.MOLES) {
            return cantidad;
        }

        if (!esMasa(cantidad.getUnidad())) {
            throw new IllegalArgumentException(
                    "Para convertir a moles se necesita una unidad de masa"
            );
        }

        double gramos = convertirAGramos(cantidad);

        double moles =
                gramos / sustancia.getMasaMolar();

        return new Cantidad(
                moles,
                UnidadCantidad.MOLES
        );
    }

    public Cantidad convertirDesdeMoles(
            Cantidad cantidad,
            Sustancia sustancia,
            UnidadCantidad unidadDestino
    ) {
        Objects.requireNonNull(
                cantidad,
                "La cantidad no puede ser null"
        );

        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        Objects.requireNonNull(
                unidadDestino,
                "La unidad destino no puede ser null"
        );

        if (cantidad.getUnidad() != UnidadCantidad.MOLES) {
            throw new IllegalArgumentException(
                    "La cantidad de entrada debe estar expresada en moles"
            );
        }

        if (unidadDestino == UnidadCantidad.MOLES) {
            return cantidad;
        }

        if (!esMasa(unidadDestino)) {
            throw new IllegalArgumentException(
                    "La unidad destino debe ser una unidad de masa"
            );
        }

        double gramos =
                cantidad.getValor()
                        * sustancia.getMasaMolar();

        return convertirDesdeGramos(
                gramos,
                unidadDestino
        );
    }

    private double convertirAGramos(
            Cantidad cantidad
    ) {
        return switch (cantidad.getUnidad()) {

            case MILIGRAMOS ->
                    cantidad.getValor() / 1000.0;

            case GRAMOS ->
                    cantidad.getValor();

            case KILOGRAMOS ->
                    cantidad.getValor() * 1000.0;

            default ->
                    throw new IllegalArgumentException(
                            "La unidad no es una unidad de masa"
                    );
        };
    }

    private Cantidad convertirDesdeGramos(
            double gramos,
            UnidadCantidad unidadDestino
    ) {
        double valor = switch (unidadDestino) {

            case MILIGRAMOS ->
                    gramos * 1000.0;

            case GRAMOS ->
                    gramos;

            case KILOGRAMOS ->
                    gramos / 1000.0;

            default ->
                    throw new IllegalArgumentException(
                            "La unidad destino no es una unidad de masa"
                    );
        };

        return new Cantidad(
                valor,
                unidadDestino
        );
    }

    private boolean esMasa(
            UnidadCantidad unidad
    ) {
        return unidad == UnidadCantidad.MILIGRAMOS
                || unidad == UnidadCantidad.GRAMOS
                || unidad == UnidadCantidad.KILOGRAMOS;
    }
}