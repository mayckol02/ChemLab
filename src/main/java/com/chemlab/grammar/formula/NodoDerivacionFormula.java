package com.chemlab.grammar.formula;

import java.util.List;
import java.util.Objects;

/** Nodo inmutable del árbol de derivación de una fórmula química. */
public record NodoDerivacionFormula(String regla, String valor, List<NodoDerivacionFormula> hijos) {

    public NodoDerivacionFormula {
        Objects.requireNonNull(regla, "La regla del nodo es obligatoria");
        Objects.requireNonNull(valor, "El valor del nodo es obligatorio");
        hijos = List.copyOf(hijos);
    }

    public String etiqueta() {
        return valor.isBlank() ? regla : regla + "  ·  " + valor;
    }
}
