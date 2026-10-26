package com.chemlab.grammar.formula;

import java.util.List;
import java.util.Objects;

/** Nombre del compuesto y pasos de nomenclatura derivados de una fórmula. */
public record ResultadoNombreQuimico(
        String nombre,
        String reglaAplicada,
        List<String> pasos
) {
    public ResultadoNombreQuimico {
        Objects.requireNonNull(nombre, "El nombre químico es obligatorio");
        Objects.requireNonNull(reglaAplicada, "La regla de nomenclatura es obligatoria");
        pasos = List.copyOf(pasos);
    }
}
