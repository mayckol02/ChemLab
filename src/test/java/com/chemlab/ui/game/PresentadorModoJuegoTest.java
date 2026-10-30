package com.chemlab.ui.game;

import com.chemlab.automata.TransicionQuimica;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.game.ModoJuego;
import com.chemlab.game.OperacionesEquipo;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.enums.TipoEquipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PresentadorModoJuegoTest {

    private CatalogoQuimico catalogo;
    private FabricaExperimento fabrica;
    private Reaccion reaccion;

    @BeforeEach
    void setUp() {
        catalogo = CatalogoQuimico.predeterminado();
        fabrica = new FabricaExperimento(catalogo);
        reaccion = catalogo.buscarReaccion("R_H2O").orElseThrow();
    }

    @Test
    void tutorialConservaExperimentoYTransicionesGuiadas() {
        PresentadorModoJuego presentador = PresentadorModoJuego.para(ModoJuego.TUTORIAL);
        var automata = FabricaAutomata.crearAutomataParaReaccion(reaccion.getId());

        assertInstanceOf(PresentadorModoTutorial.class, presentador);
        assertFalse(presentador.permiteAccesoLibre());
        assertTrue(presentador.mostrarEcuacionReaccion());
        assertFalse(presentador.crearExperimento(fabrica, reaccion)
                .getInventario().obtenerCantidades().isEmpty());
        assertEquals(automata.getTransicionesDisponibles(),
                presentador.obtenerTransiciones(automata, TipoEquipo.MATRAZ));
    }

    @Test
    void dificilUsaExperimentoVacioYOperacionesDelEquipoSinProtocolo() {
        PresentadorModoJuego presentador = PresentadorModoJuego.para(ModoJuego.DIFICIL);
        var automata = FabricaAutomata.crearAutomataParaReaccion(reaccion.getId());

        assertInstanceOf(PresentadorModoDificil.class, presentador);
        assertTrue(presentador.permiteAccesoLibre());
        assertFalse(presentador.mostrarEcuacionReaccion());
        assertNull(presentador.obtenerProtocolo(reaccion.getId(), automata.getEstadoActual().getId()));
        assertTrue(presentador.crearExperimento(fabrica, reaccion)
                .getInventario().obtenerCantidades().isEmpty());

        var acciones = presentador.obtenerTransiciones(automata, TipoEquipo.MATRAZ).stream()
                .map(TransicionQuimica::getAccion)
                .toList();
        assertEquals(Set.copyOf(OperacionesEquipo.disponibles(TipoEquipo.MATRAZ)),
                Set.copyOf(acciones));
    }
}
