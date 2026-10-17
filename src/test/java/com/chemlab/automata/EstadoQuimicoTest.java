package com.chemlab.automata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoQuimicoTest {

    @Test
    void debeCrearUnEstadoQuimicoValido() {
        EstadoQuimico estado = new EstadoQuimico(
                "INICIAL",
                "Mezcla inicial"
        );

        assertEquals("INICIAL", estado.getId());
        assertEquals("Mezcla inicial", estado.getNombre());
    }

    @Test
    void noDebePermitirIdVacio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EstadoQuimico(
                        " ",
                        "Estado"
                )
        );
    }

    @Test
    void noDebePermitirNombreVacio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EstadoQuimico(
                        "INICIAL",
                        ""
                )
        );
    }

    @Test
    void estadosConElMismoIdDebenSerIguales() {
        EstadoQuimico primero = new EstadoQuimico(
                "INICIAL",
                "Mezcla inicial"
        );

        EstadoQuimico segundo = new EstadoQuimico(
                "INICIAL",
                "Otro nombre"
        );

        assertEquals(primero, segundo);
        assertEquals(
                primero.hashCode(),
                segundo.hashCode()
        );
    }
}