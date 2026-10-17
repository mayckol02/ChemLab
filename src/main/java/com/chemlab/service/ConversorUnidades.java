package com.chemlab.service;

import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;

import java.util.Objects;

public class ConversorUnidades {

    public Cantidad convertir(
            Cantidad cantidad,
            UnidadCantidad unidadDestino
    ) {
        Objects.requireNonNull(
                cantidad,
                "La cantidad no puede ser null"
        );

        Objects.requireNonNull(
                unidadDestino,
                "La unidad destino no puede ser null"
        );

        if (cantidad.getUnidad() == unidadDestino) {
            return cantidad;
        }

        double valorConvertido = convertirValor(
                cantidad.getValor(),
                cantidad.getUnidad(),
                unidadDestino
        );

        return new Cantidad(
                valorConvertido,
                unidadDestino
        );
    }

    private double convertirValor(
            double valor,
            UnidadCantidad unidadOrigen,
            UnidadCantidad unidadDestino
    ) {

        if (esMasa(unidadOrigen) && esMasa(unidadDestino)) {
            return convertirMasa(
                    valor,
                    unidadOrigen,
                    unidadDestino
            );
        }

        if (esVolumen(unidadOrigen) && esVolumen(unidadDestino)) {
            return convertirVolumen(
                    valor,
                    unidadOrigen,
                    unidadDestino
            );
        }

        if (unidadOrigen == UnidadCantidad.MOLES
                && unidadDestino == UnidadCantidad.MOLES) {
            return valor;
        }

        throw new IllegalArgumentException(
                "No se puede convertir "
                        + unidadOrigen
                        + " a "
                        + unidadDestino
        );
    }

    private double convertirMasa(
            double valor,
            UnidadCantidad origen,
            UnidadCantidad destino
    ) {

        double gramos = switch (origen) {
            case MILIGRAMOS -> valor / 1000.0;
            case GRAMOS -> valor;
            case KILOGRAMOS -> valor * 1000.0;
            default -> throw new IllegalArgumentException(
                    "La unidad no es de masa"
            );
        };

        return switch (destino) {
            case MILIGRAMOS -> gramos * 1000.0;
            case GRAMOS -> gramos;
            case KILOGRAMOS -> gramos / 1000.0;
            default -> throw new IllegalArgumentException(
                    "La unidad no es de masa"
            );
        };
    }

    private double convertirVolumen(
            double valor,
            UnidadCantidad origen,
            UnidadCantidad destino
    ) {

        double mililitros = switch (origen) {
            case MILILITROS -> valor;
            case LITROS -> valor * 1000.0;
            default -> throw new IllegalArgumentException(
                    "La unidad no es de volumen"
            );
        };

        return switch (destino) {
            case MILILITROS -> mililitros;
            case LITROS -> mililitros / 1000.0;
            default -> throw new IllegalArgumentException(
                    "La unidad no es de volumen"
            );
        };
    }

    private boolean esMasa(UnidadCantidad unidad) {
        return unidad == UnidadCantidad.MILIGRAMOS
                || unidad == UnidadCantidad.GRAMOS
                || unidad == UnidadCantidad.KILOGRAMOS;
    }

    private boolean esVolumen(UnidadCantidad unidad) {
        return unidad == UnidadCantidad.MILILITROS
                || unidad == UnidadCantidad.LITROS;
    }
}