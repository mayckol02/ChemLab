package com.chemlab.factory;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Inventario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FabricaAutomataTest {

    @Test
    void automataEstandarDebeEmpezarEnPreparacion() {
        AutomataQuimico automata = FabricaAutomata.crearAutomataEstandar();

        assertEquals(
                "PREPARACION",
                automata.getEstadoActual().getId()
        );
    }

    @Test
    void automataEstandarDebeAceptarCargarReactivoEnEstadoInicial() {
        AutomataQuimico automata = FabricaAutomata.crearAutomataEstandar();

        assertTrue(
                automata.puedeTransicionar(
                        AccionLaboratorio.CARGAR_REACTIVO,
                        new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE),
                        new Inventario()
                )
        );
    }

    @Test
    void automataEstandarNoDebeEstarEnEstadoFinalAlInicio() {
        AutomataQuimico automata = FabricaAutomata.crearAutomataEstandar();
        assertFalse(automata.esEstadoFinalActual());
    }

    @Test
    void automataSimpleDebeEmpezarEnEstadoInicial() {
        AutomataQuimico automata = FabricaAutomata.crearAutomataSimple();

        assertEquals(
                "INICIAL",
                automata.getEstadoActual().getId()
        );
    }

    @Test
    void automataSimpleDebeLlegarAEstadoFinalConUnaAccion() {
        AutomataQuimico automata = FabricaAutomata.crearAutomataSimple();

        automata.transicionar(
                AccionLaboratorio.CARGAR_REACTIVO,
                new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE), new Inventario()
        );

        assertTrue(automata.esEstadoFinalActual());
    }

    @Test
    void automataEstandarDebeCompletarseEnCuatroAcciones() {
        AutomataQuimico automata = FabricaAutomata.crearAutomataEstandar();

        automata.transicionar(AccionLaboratorio.CARGAR_REACTIVO, new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE), new Inventario());
        automata.transicionar(AccionLaboratorio.MEZCLAR, new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE), new Inventario());
        automata.transicionar(AccionLaboratorio.CALENTAR, new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE), new Inventario());
        assertTrue(automata.esEstadoFinalActual());
        assertEquals("COMPLETADO", automata.getEstadoActual().getId());
    }

    @Test
    void automataDiamanteHPHTDebeTenerEstadosInicialYFinalBienDefinidos() {
        AutomataQuimico automata = FabricaAutomata.crearAutomataDiamanteHPHT();

        assertNotNull(automata.getEstadoInicial());
        assertEquals("PREPARACION", automata.getEstadoInicial().getId());
        assertEquals(1, automata.getEstadosFinales().size());
        assertTrue(automata.getEstadosFinales().stream().anyMatch(e -> e.getId().equals("COMPLETADO")));

        CondicionesExperimentales cond = new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE);
        Inventario inv = new Inventario();

        automata.transicionar(AccionLaboratorio.CARGAR_REACTIVO, cond, inv);
        automata.transicionar(AccionLaboratorio.AGREGAR_CATALIZADOR, cond, inv);
        automata.transicionar(AccionLaboratorio.COMPRIMIR, cond, inv);
        automata.transicionar(AccionLaboratorio.CALENTAR, cond, inv);
        automata.transicionar(AccionLaboratorio.FINALIZAR, cond, inv);

        assertTrue(automata.esEstadoFinalActual());
    }

    @Test
    void automataParaReaccionesDebeResolverTodasLasReaccionesDelUniverso() {
        assertNotNull(FabricaAutomata.crearAutomataParaReaccion("R_H2O"));
        assertNotNull(FabricaAutomata.crearAutomataParaReaccion("R_CH4"));
        assertNotNull(FabricaAutomata.crearAutomataParaReaccion("R_DIAMANTE_HPHT"));
        assertNotNull(FabricaAutomata.crearAutomataParaReaccion("R_DIAMANTE_CVD"));
        assertNotNull(FabricaAutomata.crearAutomataParaReaccion("R_SO2"));
        assertNotNull(FabricaAutomata.crearAutomataParaReaccion("R_CuO"));
    }
}
