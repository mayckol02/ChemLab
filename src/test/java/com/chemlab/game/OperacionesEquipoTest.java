package com.chemlab.game;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EstadoQuimico;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.game.procedure.PlanificadorEstacionesProcedimiento;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.TipoEquipo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperacionesEquipoTest {

    @Test
    void cadaTransicionDeCatalogoTieneOperacionEnElPuestoAsignado() {
        CatalogoQuimico catalogo = CatalogoQuimico.predeterminado();
        FabricaExperimento fabrica = new FabricaExperimento(catalogo);
        PlanificadorEstacionesProcedimiento planificador = new PlanificadorEstacionesProcedimiento();

        for (Reaccion reaccion : catalogo.obtenerReacciones()) {
            AutomataQuimico automata = FabricaAutomata.crearAutomataParaReaccion(reaccion.getId());
            TipoEquipo equipoPrincipal = fabrica.crearExperimentoParaReaccion(reaccion).getEquipo().getTipo();
            for (EstadoQuimico estado : automata.getEstados()) {
                for (var transicion : automata.getTransicionesDesdeEstado(estado)) {
                    TipoEquipo equipo = planificador.equipoRequerido(
                            reaccion.getId(),
                            estado.getId(),
                            transicion.getAccion(),
                            equipoPrincipal
                    );
                    assertTrue(
                            OperacionesEquipo.disponibles(equipo).contains(transicion.getAccion()),
                            () -> reaccion.getId() + " / " + estado.getId() + " / "
                                    + transicion.getAccion() + " no está disponible en " + equipo
                    );
                }
            }
        }
    }

    @Test
    void elMatrazPermiteMontarElSistemaYSeleccionarElMetodoDeEvaporacion() {
        assertTrue(OperacionesEquipo.disponibles(TipoEquipo.MATRAZ)
                .contains(AccionLaboratorio.MONTAR_EQUIPO));
        assertTrue(OperacionesEquipo.disponibles(TipoEquipo.MATRAZ)
                .contains(AccionLaboratorio.SELECCIONAR_METODO));
    }

    @Test
    void separacionDeSolucionSalinaSigueLaSecuenciaDeMontajeCargaYEvaporacion() {
        AutomataQuimico automata = FabricaAutomata.crearAutomataParaReaccion("R_SEPARACION_NACL");
        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(25, 1, 7, Atmosfera.AIRE);
        Inventario inventario = new Inventario();
        List<AccionLaboratorio> secuencia = List.of(
                AccionLaboratorio.MONTAR_EQUIPO,
                AccionLaboratorio.CARGAR_REACTIVO,
                AccionLaboratorio.SELECCIONAR_METODO,
                AccionLaboratorio.CALENTAR,
                AccionLaboratorio.FINALIZAR
        );

        for (AccionLaboratorio accion : secuencia) {
            assertTrue(automata.puedeTransicionar(accion, condiciones, inventario), accion.toString());
            automata.transicionar(accion, condiciones, inventario);
        }

        assertTrue(automata.esEstadoFinalActual());
    }

    @Test
    void mezclaDeCombustionSeRealizaEnElMatrazAntesDeIrAlHorno() {
        PlanificadorEstacionesProcedimiento planificador = new PlanificadorEstacionesProcedimiento();

        assertEquals(
                TipoEquipo.MATRAZ,
                planificador.equipoRequerido("R_CO2", "CARGADO", AccionLaboratorio.MEZCLAR, TipoEquipo.HORNO)
        );
    }
}
