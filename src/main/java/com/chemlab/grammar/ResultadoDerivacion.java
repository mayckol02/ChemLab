package com.chemlab.grammar;

import java.util.List;

/** Resultado de seguir una secuencia de acciones en una gramática regular. */
public record ResultadoDerivacion(
        List<ProduccionRegular> produccionesAplicadas,
        SimboloNoTerminal simboloPendiente,
        boolean aceptada,
        int accionesConsumidas,
        String mensaje
) {
    public ResultadoDerivacion {
        produccionesAplicadas = List.copyOf(produccionesAplicadas);
        mensaje = mensaje == null ? "" : mensaje;
    }

    public boolean esPrefijoValido(int totalAcciones) {
        return accionesConsumidas == totalAcciones;
    }
}
