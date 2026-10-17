package com.chemlab.model.exception;

import com.chemlab.model.enums.TipoFalloReaccion;

public class ReaccionInvalidaException
        extends IllegalArgumentException {

    public ReaccionInvalidaException(
            String mensaje
    ) {
        super(mensaje);
    }

    public TipoFalloReaccion getTipoFallo() {
        return TipoFalloReaccion.REACCION_INVALIDA;
    }
}