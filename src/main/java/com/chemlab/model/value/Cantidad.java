package com.chemlab.model.value;

import com.chemlab.model.enums.UnidadCantidad;

import java.util.Objects;

public class Cantidad {

    private final double valor;
    private final UnidadCantidad unidad;

    public Cantidad(
            double valor,
            UnidadCantidad unidad
    ) {
        if (valor < 0) {
            throw new IllegalArgumentException(
                    "El valor no puede ser negativo"
            );
        }

        this.unidad = Objects.requireNonNull(
                unidad,
                "La unidad no puede ser null"
        );

        this.valor = valor;
    }

    public double getValor() {
        return valor;
    }

    public UnidadCantidad getUnidad() {
        return unidad;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Cantidad cantidad)) {
            return false;
        }

        return Double.compare(
                valor,
                cantidad.valor
        ) == 0
                && unidad == cantidad.unidad;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                valor,
                unidad
        );
    }

    @Override
    public String toString() {
        return valor + " " + unidad;
    }
}