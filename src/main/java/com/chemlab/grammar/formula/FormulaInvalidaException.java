package com.chemlab.grammar.formula;

/** Error sintáctico con posición para mostrar una explicación en el taller. */
public final class FormulaInvalidaException extends IllegalArgumentException {

    private final int posicion;

    public FormulaInvalidaException(String mensaje, int posicion) {
        super(mensaje);
        this.posicion = Math.max(0, posicion);
    }

    /** Posición base cero dentro de la fórmula normalizada. */
    public int getPosicion() {
        return posicion;
    }
}
