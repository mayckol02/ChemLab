package com.chemlab.automata;

import com.chemlab.model.entity.CondicionesRequeridas;
import com.chemlab.model.enums.AccionLaboratorio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Inventario;
import com.chemlab.model.enums.Atmosfera;

import static org.junit.jupiter.api.Assertions.*;

class AutomataQuimicoTest {

    private final CondicionesExperimentales condiciones = new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE);
    private final Inventario inventario = new Inventario();

    @Test
    void debeTransicionarCuandoLaAccionEstaPermitida() {
        AutomataQuimico automata = crearAutomata();

        assertTrue(
                automata.puedeTransicionar(AccionLaboratorio.CARGAR_REACTIVO, condiciones, inventario)
        );

        EstadoQuimico nuevoEstado =
                automata.transicionar(AccionLaboratorio.CARGAR_REACTIVO, condiciones, inventario);

        assertEquals("AGUA_FORMADA", nuevoEstado.getId());
        assertEquals(
                nuevoEstado,
                automata.getEstadoActual()
        );
    }

    @Test
    void noDebePermitirAccionNoDefinidaDesdeEstadoActual() {
        AutomataQuimico automata = crearAutomata();

        assertFalse(
                automata.puedeTransicionar(AccionLaboratorio.MEZCLAR, condiciones, inventario)
        );

        assertThrows(
                IllegalStateException.class,
                () -> automata.transicionar(
                        AccionLaboratorio.MEZCLAR, condiciones, inventario
                )
        );

        assertEquals(
                "INICIAL",
                automata.getEstadoActual().getId()
        );
    }

    @Test
    void debeReiniciarAlEstadoInicial() {
        AutomataQuimico automata = crearAutomata();

        automata.transicionar(AccionLaboratorio.CARGAR_REACTIVO, condiciones, inventario);
        automata.reiniciar();

        assertEquals(
                "INICIAL",
                automata.getEstadoActual().getId()
        );
    }

    @Test
    void noDebePermitirTransicionesConEstadosNoRegistrados() {
        EstadoQuimico inicial = new EstadoQuimico(
                "INICIAL",
                "Mezcla inicial"
        );

        EstadoQuimico destinoNoRegistrado =
                new EstadoQuimico(
                        "AGUA_FORMADA",
                        "Agua formada"
                );

        TransicionQuimica transicion =
                new TransicionQuimica(
                        inicial,
                        AccionLaboratorio.CARGAR_REACTIVO,
                        CondicionesRequeridas.sinCondicionesEspeciales(),
                        destinoNoRegistrado
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new AutomataQuimico(
                        inicial,
                        List.of(inicial),
                        List.of(transicion)
                )
        );
    }

    private AutomataQuimico crearAutomata() {
        EstadoQuimico inicial = new EstadoQuimico(
                "INICIAL",
                "Mezcla inicial"
        );

        EstadoQuimico aguaFormada =
                new EstadoQuimico(
                        "AGUA_FORMADA",
                        "Agua formada"
                );

        TransicionQuimica transicion =
                new TransicionQuimica(
                        inicial,
                        AccionLaboratorio.CARGAR_REACTIVO,
                        CondicionesRequeridas.sinCondicionesEspeciales(),
                        aguaFormada
                );

        return new AutomataQuimico(
                inicial,
                List.of(
                        inicial,
                        aguaFormada
                ),
                List.of(transicion)
        );
    }
}