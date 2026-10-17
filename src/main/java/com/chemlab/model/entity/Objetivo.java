package com.chemlab.model.entity;

import java.util.Objects;

public class Objetivo {

    private final String id;
    private final String descripcion;
    private final Sustancia sustanciaEsperada;
    private final double cantidadMinima;

    public Objetivo(
            String id,
            String descripcion,
            Sustancia sustanciaEsperada,
            double cantidadMinima
    ) {
        this.id = Objects.requireNonNull(
                id,
                "El id no puede ser null"
        );

        this.descripcion = Objects.requireNonNull(
                descripcion,
                "La descripción no puede ser null"
        );

        this.sustanciaEsperada = Objects.requireNonNull(
                sustanciaEsperada,
                "La sustancia esperada no puede ser null"
        );

        if (cantidadMinima <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad mínima debe ser mayor que cero"
            );
        }

        this.cantidadMinima = cantidadMinima;
    }

    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Sustancia getSustanciaEsperada() {
        return sustanciaEsperada;
    }

    public double getCantidadMinima() {
        return cantidadMinima;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Objetivo objetivo)) {
            return false;
        }

        return id.equals(objetivo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return descripcion;
    }
}