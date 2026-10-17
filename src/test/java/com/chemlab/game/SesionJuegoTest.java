package com.chemlab.game;

import com.chemlab.automata.ResultadoAccion;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.factory.FabricaSesion;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.EstadoExperimento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SesionJuegoTest {

    private SesionJuego sesion;
    private Experimento experimento;
    private Reaccion reaccion;
    private CatalogoQuimico catalogo;

    @BeforeEach
    void setUp() {
        catalogo = CatalogoQuimico.predeterminado();

        sesion = FabricaSesion.crearSesion(
                catalogo,
                FabricaAutomata.crearAutomataSimple()
        );

        experimento = new FabricaExperimento(catalogo)
                .crearExperimentoSintesisAgua();

        reaccion = catalogo.buscarReaccion("R_H2O").orElseThrow();
    }

    // -----------------------------------------------------------
    // iniciar()
    // -----------------------------------------------------------

    @Test
    void sesionNoDebeEstarActivaAntesDeIniciar() {
        assertFalse(sesion.isActiva());
    }

    @Test
    void debeActivarseCuandoSeInicia() {
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());
        assertTrue(sesion.isActiva());
    }

    @Test
    void experimentoDebeEstarEnProgresoTrasIniciar() {
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());
        assertEquals(EstadoExperimento.EN_PROGRESO, experimento.getEstado());
    }

    @Test
    void noDebePermitirIniciarDosSesionesSimultaneas() {
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());

        Experimento otro = new FabricaExperimento(catalogo)
                .crearExperimentoSintesisAgua();

        assertThrows(
                IllegalStateException.class,
                () -> sesion.iniciar(otro, reaccion, FabricaAutomata.crearAutomataSimple())
        );
    }

    @Test
    void debeRechazarExperimentoNull() {
        assertThrows(
                NullPointerException.class,
                () -> sesion.iniciar(null, reaccion, FabricaAutomata.crearAutomataSimple())
        );
    }

    // -----------------------------------------------------------
    // ejecutarAccion()
    // -----------------------------------------------------------

    @Test
    void ejecutarAccionSinSesionActivaDebeLanzarExcepcion() {
        assertThrows(
                IllegalStateException.class,
                () -> sesion.ejecutarAccion(AccionLaboratorio.CARGAR_REACTIVO)
        );
    }

    @Test
    void accionExitosaDebeRetornarResultadoExitoso() {
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());

        ResultadoAccion resultado =
                sesion.ejecutarAccion(AccionLaboratorio.CARGAR_REACTIVO);

        assertTrue(resultado.isExitoso());
    }

    @Test
    void accionFinalDebeCompletarElExperimento() {
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());

        // El autómata simple tiene una sola transición: CARGAR_REACTIVO → LISTO (final)
        ResultadoAccion resultado =
                sesion.ejecutarAccion(AccionLaboratorio.CARGAR_REACTIVO);

        assertTrue(resultado.isCompletado());
        assertFalse(sesion.isActiva(),
                "La sesión debe cerrarse al completarse el experimento");
    }

    // -----------------------------------------------------------
    // abandonar()
    // -----------------------------------------------------------

    @Test
    void abandonarSinSesionActivaDebeLanzarExcepcion() {
        assertThrows(
                IllegalStateException.class,
                () -> sesion.abandonar()
        );
    }

    @Test
    void abandonarDebeMarcarElExperimentoComoFallido() {
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());
        sesion.abandonar();

        assertEquals(EstadoExperimento.FALLIDO, experimento.getEstado());
        assertFalse(sesion.isActiva());
    }

    // -----------------------------------------------------------
    // CuadernoLaboratorio
    // -----------------------------------------------------------

    @Test
    void cuadernoDebeEstarVacioAlIniciarSesion() {
        assertTrue(sesion.getCuaderno().getHistorial().isEmpty(),
                "El historial debe estar vacío al inicio");
    }

    @Test
    void cuadernoDebeRegistrarExperimentoAbandonado() {
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());
        sesion.abandonar();

        assertEquals(1, sesion.getCuaderno().getHistorial().size());
        assertFalse(sesion.getCuaderno().getHistorial().get(0).isExitoso(),
                "El registro de un experimento abandonado debe marcarse como no exitoso");
    }

    @Test
    void cuadernoDebeRegistrarExperimentoCompletado() {
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());
        sesion.ejecutarAccion(AccionLaboratorio.CARGAR_REACTIVO);

        assertEquals(1, sesion.getCuaderno().getHistorial().size());
        assertTrue(sesion.getCuaderno().getHistorial().get(0).isExitoso(),
                "El registro de un experimento completado debe marcarse como exitoso");
    }

    @Test
    void cuadernoDebeAcumularVariosRegistros() {
        // Primer experimento: completado
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());
        sesion.ejecutarAccion(AccionLaboratorio.CARGAR_REACTIVO);

        // Segundo experimento: abandonado
        SesionJuego sesion2 = FabricaSesion.crearSesion(
                catalogo,
                FabricaAutomata.crearAutomataSimple()
        );
        Experimento exp2 = new FabricaExperimento(catalogo)
                .crearExperimentoCombustionCarbono();
        Reaccion reaccion2 = catalogo.buscarReaccion("R_CO2").orElseThrow();

        // El cuaderno de sesion solo registra sus propios experimentos
        assertEquals(1, sesion.getCuaderno().getHistorial().size());

        sesion2.iniciar(exp2, reaccion2, FabricaAutomata.crearAutomataSimple());
        sesion2.abandonar();

        assertEquals(1, sesion2.getCuaderno().getHistorial().size());
        assertEquals("Combustión de Carbono",
                sesion2.getCuaderno().getHistorial().get(0).getNombreExperimento());
    }

    @Test
    void cuadernoDebeConservarNombreDelExperimentoEnElRegistro() {
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());
        sesion.abandonar();

        assertEquals(
                "Síntesis de Agua",
                sesion.getCuaderno().getHistorial().get(0).getNombreExperimento()
        );
    }

    // -----------------------------------------------------------
    // Observer
    // -----------------------------------------------------------

    @Test
    void debeNotificarOnExperimentoIniciadoAlAgregarObservador() {
        List<Experimento> notificados = new ArrayList<>();

        sesion.agregarObservador(new ObservadorSesion() {
            @Override
            public void onExperimentoIniciado(Experimento e) {
                notificados.add(e);
            }
            @Override public void onAccionEjecutada(ResultadoAccion r) {}
            @Override public void onExperimentoCompletado(Experimento e) {}
            @Override public void onExperimentoAbandonado(Experimento e) {}
        });

        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());

        assertEquals(1, notificados.size());
        assertSame(experimento, notificados.get(0));
    }

    @Test
    void debeNotificarOnExperimentoAbandonadoAlAbandonar() {
        List<Experimento> notificados = new ArrayList<>();

        sesion.agregarObservador(new ObservadorSesion() {
            @Override public void onExperimentoIniciado(Experimento e) {}
            @Override public void onAccionEjecutada(ResultadoAccion r) {}
            @Override public void onExperimentoCompletado(Experimento e) {}
            @Override
            public void onExperimentoAbandonado(Experimento e) {
                notificados.add(e);
            }
        });

        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());
        sesion.abandonar();

        assertEquals(1, notificados.size());
    }

    @Test
    void debePermitirQuitarObservador() {
        List<String> llamadas = new ArrayList<>();

        ObservadorSesion obs = new ObservadorSesion() {
            @Override
            public void onExperimentoIniciado(Experimento e) {
                llamadas.add("iniciado");
            }
            @Override public void onAccionEjecutada(ResultadoAccion r) {}
            @Override public void onExperimentoCompletado(Experimento e) {}
            @Override public void onExperimentoAbandonado(Experimento e) {}
        };

        sesion.agregarObservador(obs);
        sesion.quitarObservador(obs);
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());

        assertTrue(llamadas.isEmpty(),
                "El observador quitado no debe recibir notificaciones");
    }
}
