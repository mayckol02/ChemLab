package com.chemlab.game;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.factory.FabricaSesion;
import com.chemlab.game.procedure.PlanificadorEstacionesProcedimiento;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.enums.EstadoRegistroExperimento;
import com.chemlab.model.enums.TipoEquipo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentosCatalogoIntegracionTest {

    private final CatalogoQuimico catalogo = CatalogoQuimico.predeterminado();
    private final FabricaExperimento fabrica = new FabricaExperimento(catalogo);
    private final PlanificadorEstacionesProcedimiento planificador =
            new PlanificadorEstacionesProcedimiento();

    @Test
    void cadaExperimentoDelCatalogoAvanzaYFinalizaEnAmbosModos() {
        for (ModoJuego modo : ModoJuego.values()) {
            for (Reaccion reaccion : catalogo.obtenerReacciones()) {
                verificarRecorridoCompleto(modo, reaccion);
            }
        }
    }

    private void verificarRecorridoCompleto(ModoJuego modo, Reaccion reaccion) {
        AutomataQuimico automata = FabricaAutomata.crearAutomataParaReaccion(reaccion.getId());
        Experimento experimento = fabrica.crearExperimentoParaReaccion(reaccion);
        SesionJuego sesion = FabricaSesion.crearSesion(catalogo, automata, modo);
        sesion.iniciar(experimento, reaccion, automata);

        int pasosRestantes = automata.getEstados().size();
        while (sesion.isActiva() && pasosRestantes-- > 0) {
            TransicionQuimica transicion = automata.getTransicionesDisponibles().stream()
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            reaccion.getId() + " quedó bloqueada en "
                                    + automata.getEstadoActual().getId()));

            if (modo == ModoJuego.DIFICIL) {
                TipoEquipo equipo = planificador.equipoRequerido(
                        reaccion.getId(),
                        automata.getEstadoActual().getId(),
                        transicion.getAccion(),
                        experimento.getEquipo().getTipo()
                );
                sesion.registrarEntradaPuesto(equipo.name(), equipo);
            }

            var resultado = sesion.ejecutarAccion(transicion.getAccion());
            assertTrue(resultado.isExitoso(), () -> reaccion.getId() + " en " + modo
                    + " falló en " + automata.getEstadoActual().getId()
                    + ": " + resultado.getMensaje()
                    + " · inventario=" + (sesion.isActiva()
                    ? sesion.obtenerMaterialesDisponibles()
                    : sesion.getCuaderno().getHistorial().getFirst().getInventarioFinal())
                    + " · eventos=" + (sesion.isActiva()
                    ? List.of()
                    : sesion.getCuaderno().getHistorial().getFirst().getEventos()));
        }

        assertFalse(sesion.isActiva(), () -> reaccion.getId() + " no finalizó en " + modo);
        assertEquals(EstadoRegistroExperimento.COMPLETADO,
                sesion.getCuaderno().getHistorial().getFirst().getEstadoFinal(),
                () -> reaccion.getId() + " no completó en " + modo);
        assertTrue(automata.esEstadoFinalActual(), reaccion.getId() + " no alcanzó un estado final");
    }
}
