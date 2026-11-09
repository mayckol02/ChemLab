package com.chemlab.game;

import com.chemlab.catalogo.CatalogoQuimico;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GestorMisionesTest {

    @Test
    void rutaIniciaConPrimeraMisionDesbloqueada() {
        GestorMisiones gestor = new GestorMisiones();

        assertFalse(gestor.estaIniciada());
        assertTrue(gestor.iniciarRuta());
        assertEquals("fundamentos-agua", gestor.obtenerMisionActual().orElseThrow().id());
        assertTrue(gestor.estaDesbloqueada(gestor.obtenerMisiones().getFirst()));
        assertFalse(gestor.estaDesbloqueada(gestor.obtenerMisiones().get(1)));
    }

    @Test
    void todasLasMisionesApuntanAReaccionesDelCatalogo() {
        CatalogoQuimico catalogo = CatalogoQuimico.predeterminado();
        GestorMisiones gestor = new GestorMisiones();

        assertTrue(gestor.obtenerMisiones().stream()
                .allMatch(mision -> catalogo.buscarReaccion(mision.reaccionId()).isPresent()));
    }

    @Test
    void soloCompletarPracticaActualDesbloqueaSiguienteMision() {
        GestorMisiones gestor = new GestorMisiones();
        gestor.iniciarRuta();

        assertFalse(gestor.registrarPracticaCompletada("R_CO2"));
        assertEquals(0, gestor.obtenerCantidadCompletadas());

        assertTrue(gestor.registrarPracticaCompletada("R_H2O"));
        assertEquals("combustion-carbono", gestor.obtenerMisionActual().orElseThrow().id());
        assertTrue(gestor.estaDesbloqueada(gestor.obtenerMisiones().get(1)));
        assertFalse(gestor.estaDesbloqueada(gestor.obtenerMisiones().get(2)));
    }

    @Test
    void noAvanzaSiLaRutaNoSeInicio() {
        GestorMisiones gestor = new GestorMisiones();

        assertFalse(gestor.registrarPracticaCompletada("R_H2O"));
        assertEquals(0, gestor.obtenerCantidadCompletadas());
    }

    @Test
    void completarUltimaMisionFinalizaRutaYNoPermiteAvanzarMas() {
        GestorMisiones gestor = new GestorMisiones();
        gestor.iniciarRuta();

        for (GestorMisiones.Mision mision : gestor.obtenerMisiones()) {
            assertTrue(gestor.registrarPracticaCompletada(mision.reaccionId()));
        }

        assertTrue(gestor.estaCompletada());
        assertEquals(gestor.obtenerMisiones().size(), gestor.obtenerCantidadCompletadas());
        assertTrue(gestor.obtenerMisionActual().isEmpty());
        assertFalse(gestor.iniciarRuta());
        assertFalse(gestor.registrarPracticaCompletada("R_H2O"));
        assertTrue(gestor.obtenerMisiones().stream().allMatch(gestor::estaDesbloqueada));
    }
}
