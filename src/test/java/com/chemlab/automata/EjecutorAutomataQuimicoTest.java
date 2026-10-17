package com.chemlab.automata;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.engine.EjecutorReaccion;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.CondicionesRequeridas;
import com.chemlab.model.entity.Equipo;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Objetivo;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.model.value.Cantidad;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class EjecutorAutomataQuimicoTest {

    @Test
    void debeAvanzarElAutomataYCompletarCuandoLaReaccionEsExitosa() {
        CatalogoQuimico catalogo =
                CatalogoQuimico.predeterminado();

        Reaccion reaccionAgua =
                catalogo.buscarReaccion("R_H2O")
                        .orElseThrow();

        EjecutorReaccion ejecutorExitoso =
                (reaccion, experimento) ->
                        new ResultadoQuimico(
                                Map.<Sustancia, Cantidad>of(),
                                true
                        );

        AutomataQuimico automata =
                crearAutomata();

        EjecutorAutomataQuimico ejecutor =
                new EjecutorAutomataQuimico(
                        ejecutorExitoso,
                        automata
                );

        ResultadoAccion resultado = ejecutor.ejecutar(
                AccionLaboratorio.CARGAR_REACTIVO,
                reaccionAgua,
                crearExperimento(catalogo)
        );

        assertTrue(resultado.isExitoso());
        assertTrue(resultado.isCompletado());
        assertEquals(
                "AGUA_FORMADA",
                automata.getEstadoActual().getId()
        );
    }

    @Test
    void noDebeEjecutarUnaAccionNoPermitida() {
        CatalogoQuimico catalogo =
                CatalogoQuimico.predeterminado();

        Reaccion reaccionAgua =
                catalogo.buscarReaccion("R_H2O")
                        .orElseThrow();

        AtomicBoolean fueEjecutada =
                new AtomicBoolean(false);

        EjecutorReaccion ejecutor =
                (reaccion, experimento) -> {
                    fueEjecutada.set(true);

                    return new ResultadoQuimico(
                            Map.<Sustancia, Cantidad>of(),
                            true
                    );
                };

        AutomataQuimico automata =
                crearAutomata();

        EjecutorAutomataQuimico ejecutorAutomata =
                new EjecutorAutomataQuimico(
                        ejecutor,
                        automata
                );

        ResultadoAccion resultado = ejecutorAutomata.ejecutar(
                AccionLaboratorio.MEZCLAR,
                reaccionAgua,
                crearExperimento(catalogo)
        );

        assertFalse(resultado.isExitoso());
        assertFalse(fueEjecutada.get());
        assertEquals(
                "INICIAL",
                automata.getEstadoActual().getId()
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

        return new AutomataQuimico(
                inicial,
                List.of(
                        inicial,
                        aguaFormada
                ),
                List.of(
                        new TransicionQuimica(
                                inicial,
                                AccionLaboratorio.CARGAR_REACTIVO,
                                CondicionesRequeridas.sinCondicionesEspeciales(),
                                aguaFormada
                        )
                ),
                List.of(aguaFormada)
        );
    }

    private Experimento crearExperimento(
            CatalogoQuimico catalogo
    ) {
        Sustancia agua = catalogo.buscarSustancia("H2O")
                .orElseThrow();

        Objetivo objetivo = new Objetivo(
                "O001",
                "Obtener agua",
                agua,
                1.0
        );

        Equipo equipo = new Equipo(
                "E001",
                "Recipiente",
                TipoEquipo.RECIPIENTE,
                100.0
        );

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25.0,
                        1.0,
                        7.0,
                        Atmosfera.AIRE
                );

        return new Experimento(
                "EXP001",
                "Experimento de prueba",
                objetivo,
                new Inventario(),
                equipo,
                condiciones
        );
    }
}