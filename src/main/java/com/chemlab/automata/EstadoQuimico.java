package com.chemlab.automata;

import java.util.Objects;

public final class EstadoQuimico {

    private final String id;
    private final String nombre;

    public EstadoQuimico(
            String id,
            String nombre
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "El id del estado no puede estar vacío"
            );
        }

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del estado no puede estar vacío"
            );
        }

        this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(
            Object objeto
    ) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof EstadoQuimico otro)) {
            return false;
        }

        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre;
    }
}