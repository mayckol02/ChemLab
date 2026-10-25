package com.chemlab.grammar.formula;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Árbol sintáctico y cantidades de átomos obtenidos al analizar una fórmula. */
public record ResultadoAnalisisFormula(
        String formulaOriginal,
        String formulaNormalizada,
        NodoDerivacionFormula arbol,
        Map<String, Integer> atomos
) {
    public ResultadoAnalisisFormula {
        Objects.requireNonNull(formulaOriginal, "La fórmula original es obligatoria");
        Objects.requireNonNull(formulaNormalizada, "La fórmula normalizada es obligatoria");
        Objects.requireNonNull(arbol, "El árbol de derivación es obligatorio");
        atomos = Collections.unmodifiableMap(new LinkedHashMap<>(atomos));
    }
}
