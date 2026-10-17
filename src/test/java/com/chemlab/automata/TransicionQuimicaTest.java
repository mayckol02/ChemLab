package com.chemlab.automata;

import com.chemlab.model.entity.CondicionesRequeridas;
import com.chemlab.model.enums.AccionLaboratorio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransicionQuimicaTest {

    @Test
    void debeCrearUnaTransicionValida() {
        EstadoQuimico origen = new EstadoQuimico(
                "INICIAL",
                "Mezcla inicial"
        );

        EstadoQuimico destino = new EstadoQuimico(
                "AGUA_FORMADA",
                "Agua formada"
        );

        TransicionQuimica transicion =
                new TransicionQuimica(
                        origen,
                        AccionLaboratorio.CARGAR_REACTIVO,
                        CondicionesRequeridas.sinCondicionesEspeciales(),
                        destino
                );

        assertEquals(
                origen,
                transicion.getEstadoOrigen()
        );

        assertEquals(
                AccionLaboratorio.CARGAR_REACTIVO,
                transicion.getAccion()
        );

        assertEquals(
                destino,
                transicion.getEstadoDestino()
        );
    }

    @Test
    void noDebePermitirEstadoOrigenNulo() {
        EstadoQuimico destino = new EstadoQuimico(
                "DESTINO",
                "Destino"
        );

        assertThrows(
                NullPointerException.class,
                () -> new TransicionQuimica(
                        null,
                        AccionLaboratorio.CARGAR_REACTIVO,
                        CondicionesRequeridas.sinCondicionesEspeciales(),
                        destino
                )
        );
    }

    @Test
    void noDebePermitirAccionNula() {
        EstadoQuimico origen = new EstadoQuimico(
                "INICIAL",
                "Mezcla inicial"
        );
        EstadoQuimico destino = new EstadoQuimico(
                "DESTINO",
                "Destino"
        );

        assertThrows(
                NullPointerException.class,
                () -> new TransicionQuimica(
                        origen,
                        null,
                        CondicionesRequeridas.sinCondicionesEspeciales(),
                        destino
                )
        );
    }
}