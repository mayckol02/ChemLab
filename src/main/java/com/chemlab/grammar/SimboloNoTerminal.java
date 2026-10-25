package com.chemlab.grammar;

import java.util.Objects;

/** Un estado simbólico de una gramática regular de protocolos. */
public record SimboloNoTerminal(String id, String nombre) {

    public SimboloNoTerminal {
        Objects.requireNonNull(id, "El id del no terminal es obligatorio");
        Objects.requireNonNull(nombre, "El nombre del no terminal es obligatorio");
        if (id.isBlank() || nombre.isBlank()) {
            throw new IllegalArgumentException("El id y el nombre no pueden estar vacíos");
        }
    }
}
