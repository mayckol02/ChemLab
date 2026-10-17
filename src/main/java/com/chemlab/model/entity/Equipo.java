package com.chemlab.model.entity;

import com.chemlab.model.enums.TipoEquipo;

import java.util.Objects;

public class Equipo {

    private final String id;
    private final String nombre;
    private final TipoEquipo tipo;
    private final double capacidad;

    public Equipo(
            String id,
            String nombre,
            TipoEquipo tipo,
            double capacidad
    ) {
        this.id = Objects.requireNonNull(id, "El id no puede ser null");
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser null");
        this.tipo = Objects.requireNonNull(tipo, "El tipo de equipo no puede ser null");

        if (capacidad <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor que cero"
            );
        }

        this.capacidad = capacidad;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoEquipo getTipo() {
        return tipo;
    }

    public double getCapacidad() {
        return capacidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Equipo equipo)) {
            return false;
        }

        return id.equals(equipo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " [" + tipo + "]";
    }
}