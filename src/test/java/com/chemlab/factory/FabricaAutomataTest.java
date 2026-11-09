package com.chemlab.factory;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Inventario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FabricaAutomataTest {

    @Test
    void automataEstandarDebeAceptarLaSecuenciaComunCompleta() {
        AutomataQuimico automata = FabricaAutomata.crearAutomataEstandar();
        var condiciones = new CondicionesExperimentales(25, 1, 7, Atmosfera.AIRE);

        assertEquals("PREPARACION", automata.getEstadoInicial().getId());
        assertFalse(automata.esEstadoFinalActual());
        automata.transicionar(AccionLaboratorio.CARGAR_REACTIVO, condiciones, new Inventario());
        automata.transicionar(AccionLaboratorio.MEZCLAR, condiciones, new Inventario());
        automata.transicionar(AccionLaboratorio.CALENTAR, condiciones, new Inventario());
        automata.transicionar(AccionLaboratorio.FINALIZAR, condiciones, new Inventario());

        assertTrue(automata.esEstadoFinalActual());
        assertEquals("COMPLETADO", automata.getEstadoActual().getId());
    }

    @Test
    void automataSimpleDebeCompletarConUnaAccion() {
        AutomataQuimico automata = FabricaAutomata.crearAutomataSimple();

        automata.transicionar(
                AccionLaboratorio.CARGAR_REACTIVO,
                new CondicionesExperimentales(25, 1, 7, Atmosfera.AIRE),
                new Inventario());

        assertTrue(automata.esEstadoFinalActual());
    }

    @Test
    void todasLasPracticasDelCatalogoCompilanSuGramaticaDeclarativa() {
        var catalogo = CatalogoQuimico.predeterminado();

        assertEquals(25, catalogo.obtenerReacciones().size());
        for (var reaccion : catalogo.obtenerReacciones()) {
            var definicion = reaccion.getDefinicionPractica().orElseThrow(
                    () -> new AssertionError("Falta práctica JSON para " + reaccion.getId()));
            AutomataQuimico automata = FabricaAutomata.crearAutomataParaReaccion(reaccion);

            assertEquals(definicion.etapas().size(), automata.getEstados().size(), reaccion.getId());
            assertEquals(1, automata.getEstadosFinales().size(), reaccion.getId());
            var terminales = definicion.producciones().stream()
                    .filter(produccion -> produccion.terminal() != null)
                    .map(produccion -> produccion.origenId() + ":" + produccion.terminal()
                            + ":" + produccion.siguienteId())
                    .toList();
            var transiciones = automata.getEstados().stream()
                    .flatMap(estado -> automata.getTransicionesDesdeEstado(estado).stream()
                            .map(transicion -> estado.getId() + ":" + transicion.getAccion()
                                    + ":" + transicion.getEstadoDestino().getId()))
                    .toList();
            assertEquals(terminales.size(), transiciones.size(), reaccion.getId());
            assertTrue(terminales.containsAll(transiciones), reaccion.getId());
        }
    }

    @Test
    void automataHphtConservaSuSecuenciaDeclarada() {
        var reaccion = CatalogoQuimico.predeterminado()
                .buscarReaccion("R_DIAMANTE_HPHT").orElseThrow();
        AutomataQuimico automata = FabricaAutomata.crearAutomataParaReaccion(reaccion);
        var condiciones = new CondicionesExperimentales(25, 1, 7, Atmosfera.AIRE);

        List<AccionLaboratorio> secuencia = List.of(
                AccionLaboratorio.CARGAR_REACTIVO,
                AccionLaboratorio.AGREGAR_CATALIZADOR,
                AccionLaboratorio.COMPRIMIR,
                AccionLaboratorio.CALENTAR,
                AccionLaboratorio.FINALIZAR);
        for (AccionLaboratorio accion : secuencia) {
            automata.transicionar(accion, condiciones, new Inventario());
        }

        assertTrue(automata.esEstadoFinalActual());
    }

    @Test
    void reaccionSinPracticaDeclarativaFallaEnLugarDeUsarAutómataAproximado() {
        var reaccion = CatalogoQuimico.predeterminado().obtenerReacciones().getFirst();
        var sinDefinicion = new com.chemlab.model.entity.Reaccion(
                reaccion.getId(),
                reaccion.getNombre(),
                reaccion.getTipo(),
                reaccion.getReactivos(),
                reaccion.getProductos(),
                reaccion.getCondicionesRequeridas());

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> FabricaAutomata.crearAutomataParaReaccion(sinDefinicion));
        assertTrue(error.getMessage().contains("práctica declarativa"));
    }
}
