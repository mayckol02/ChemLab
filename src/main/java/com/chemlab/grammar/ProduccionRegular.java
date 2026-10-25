package com.chemlab.grammar;

import com.chemlab.model.enums.AccionLaboratorio;

import java.util.Objects;

/**
 * Producción lineal por la derecha: A → acción B, A → acción o A → ε.
 * La acción es un terminal del lenguaje de protocolos.
 */
public record ProduccionRegular(
        SimboloNoTerminal origen,
        AccionLaboratorio terminal,
        SimboloNoTerminal siguiente
) {

    public ProduccionRegular {
        Objects.requireNonNull(origen, "El lado izquierdo de la producción es obligatorio");
        if (terminal == null && siguiente != null) {
            throw new IllegalArgumentException("Una producción ε no puede tener un no terminal siguiente");
        }
    }

    public static ProduccionRegular avanzar(
            SimboloNoTerminal origen,
            AccionLaboratorio terminal,
            SimboloNoTerminal siguiente
    ) {
        return new ProduccionRegular(
                origen,
                Objects.requireNonNull(terminal, "El terminal de la producción es obligatorio"),
                Objects.requireNonNull(siguiente, "El destino de la producción es obligatorio")
        );
    }

    public static ProduccionRegular terminar(
            SimboloNoTerminal origen,
            AccionLaboratorio terminal
    ) {
        return new ProduccionRegular(
                origen,
                Objects.requireNonNull(terminal, "El terminal de la producción es obligatorio"),
                null
        );
    }

    public static ProduccionRegular epsilon(SimboloNoTerminal origen) {
        return new ProduccionRegular(origen, null, null);
    }

    public boolean esEpsilon() {
        return terminal == null;
    }
}
