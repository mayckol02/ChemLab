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
        Map<String, Integer> atomos,
        int coeficiente,
        String fase
) {
    public ResultadoAnalisisFormula {
        Objects.requireNonNull(formulaOriginal, "La fórmula original es obligatoria");
        Objects.requireNonNull(formulaNormalizada, "La fórmula normalizada es obligatoria");
        Objects.requireNonNull(arbol, "El árbol de derivación es obligatorio");
        if (coeficiente < 1) throw new IllegalArgumentException("El coeficiente debe ser positivo");
        fase = Objects.requireNonNull(fase, "La fase no puede ser null");
        atomos = Collections.unmodifiableMap(new LinkedHashMap<>(atomos));
    }

    public ResultadoAnalisisFormula(
            String formulaOriginal,
            String formulaNormalizada,
            NodoDerivacionFormula arbol,
            Map<String, Integer> atomos
    ) {
        this(formulaOriginal, formulaNormalizada, arbol, atomos, 1, "");
    }
}
