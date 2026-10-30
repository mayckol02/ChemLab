package com.chemlab.game;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.engine.MotorExperimentacionLibre;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.factory.FabricaSesion;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.EstadoRegistroExperimento;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModoDificilTest {

    private CatalogoQuimico catalogo;
    private FabricaExperimento fabrica;

    @BeforeEach
    void setUp() {
        catalogo = CatalogoQuimico.predeterminado();
        fabrica = new FabricaExperimento(catalogo);
    }

    @Test
    void sesionExistenteConservaTutorialComoModoPredeterminado() {
        assertEquals(
                ModoJuego.TUTORIAL,
                FabricaSesion.crearSesion(
                        catalogo,
                        FabricaAutomata.crearAutomataSimple()
                ).getModoJuego()
        );
    }

    @Test
    void cambiarModoDuranteExperimentoLoRegistraYPermiteCambiarDeNuevoConHistorial() {
        SesionJuego sesion = crearSesionDificil();
        Reaccion reaccion = catalogo.buscarReaccion("R_H2O").orElseThrow();
        sesion.iniciar(
                fabrica.crearExperimentoVacioParaReaccion(reaccion),
                reaccion,
                FabricaAutomata.crearAutomataSimple()
        );

        sesion.seleccionarModo(ModoJuego.TUTORIAL);

        assertEquals(ModoJuego.TUTORIAL, sesion.getModoJuego());
        assertFalse(sesion.isActiva());
        assertEquals(EstadoRegistroExperimento.ABANDONADO,
                sesion.getCuaderno().getHistorial().getFirst().getEstadoFinal());
        assertEquals(ModoJuego.DIFICIL,
                sesion.getCuaderno().getHistorial().getFirst().getModoJuego());

        sesion.seleccionarModo(ModoJuego.DIFICIL);

        assertEquals(ModoJuego.DIFICIL, sesion.getModoJuego());
        assertEquals(1, sesion.getCuaderno().getHistorial().size());
    }

    @Test
    void cambiarDesdeTutorialDuranteExperimentoRegistraElIntentoEnModoOriginal() {
        SesionJuego sesion = FabricaSesion.crearSesion(
                catalogo,
                FabricaAutomata.crearAutomataSimple()
        );
        Reaccion reaccion = catalogo.buscarReaccion("R_H2O").orElseThrow();
        sesion.iniciar(
                fabrica.crearExperimentoParaReaccion(reaccion),
                reaccion,
                FabricaAutomata.crearAutomataSimple()
        );

        sesion.seleccionarModo(ModoJuego.DIFICIL);

        assertEquals(ModoJuego.DIFICIL, sesion.getModoJuego());
        assertFalse(sesion.isActiva());
        assertEquals(ModoJuego.TUTORIAL,
                sesion.getCuaderno().getHistorial().getFirst().getModoJuego());
        assertEquals(EstadoRegistroExperimento.FALLIDO,
                sesion.getCuaderno().getHistorial().getFirst().getEstadoFinal());
    }

    @Test
    void permiteSolicitarComoMaximoDosPistasYLasOcultaAlEntrarAlPuesto() {
        SesionJuego sesion = crearSesionDificil();
        Reaccion reaccion = catalogo.buscarReaccion("R_H2O").orElseThrow();
        Experimento experimento = fabrica.crearExperimentoVacioParaReaccion(reaccion);
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());

        var primeraPista = sesion.solicitarPista("Mesa de síntesis");
        assertTrue(primeraPista.isPresent());
        assertTrue(primeraPista.orElseThrow().contains("Mesa de síntesis"));
        assertTrue(primeraPista.orElseThrow().contains("Desde el punto actual"));
        assertTrue(sesion.solicitarPista("Mesa de síntesis").isPresent());
        assertTrue(sesion.solicitarPista().isEmpty());
        assertEquals(2, sesion.getCantidadPistasSolicitadas());

        sesion.salirPuesto();
        sesion.registrarEntradaPuesto("Mesa de síntesis", TipoEquipo.MATRAZ);
        assertTrue(sesion.solicitarPista().isEmpty());
    }

    @Test
    void accionFueraDeSecuenciaNoBloqueaLaSesionYSeRegistraComoInvalida() {
        SesionJuego sesion = crearSesionDificil();
        Reaccion reaccion = catalogo.buscarReaccion("R_H2O").orElseThrow();
        Experimento experimento = fabrica.crearExperimentoVacioParaReaccion(reaccion);
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());
        sesion.registrarEntradaPuesto("Mesa de síntesis", TipoEquipo.MATRAZ);

        var resultado = sesion.ejecutarAccion(AccionLaboratorio.MEZCLAR);

        assertFalse(resultado.isExitoso());
        assertTrue(sesion.isActiva());
        assertSame(
                sesion.getAutomataActual().getEstadoInicial(),
                sesion.getAutomataActual().getEstadoActual()
        );
    }

    @Test
    void finalizarAntesDelEstadoFinalRegistraUnIntentoFallidoConSuEstado() {
        SesionJuego sesion = crearSesionDificil();
        Reaccion reaccion = catalogo.buscarReaccion("R_H2O").orElseThrow();
        Experimento experimento = fabrica.crearExperimentoVacioParaReaccion(reaccion);
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());
        sesion.registrarEntradaPuesto("Mesa de síntesis", TipoEquipo.MATRAZ);

        sesion.ejecutarAccion(AccionLaboratorio.FINALIZAR);

        var registro = sesion.getCuaderno().getHistorial().getFirst();
        assertFalse(sesion.isActiva());
        assertEquals(EstadoRegistroExperimento.FALLIDO, registro.getEstadoFinal());
        assertEquals(ModoJuego.DIFICIL, registro.getModoJuego());
        assertFalse(registro.getMotivoFinalizacion().isBlank());
        assertTrue(registro.getEventos().stream().anyMatch(evento -> evento.contains("Puesto visitado")));
        assertEquals(1, registro.getPasosTransicion().size());
    }

    @Test
    void motorLibreNoInventaProductoParaNaClEnPrensaHpht() {
        Reaccion diamante = catalogo.buscarReaccion("R_DIAMANTE_HPHT").orElseThrow();
        Experimento configuracion = fabrica.crearExperimentoParaReaccion(diamante);
        Inventario inventario = new Inventario();
        var nacl = catalogo.buscarSustancia("NaCl").orElseThrow();
        inventario.agregar(nacl, 1, UnidadCantidad.GRAMOS);
        Experimento experimento = new Experimento(
                "EXP-PRUEBA",
                "NaCl en prensa HPHT",
                configuracion.getObjetivo(),
                inventario,
                configuracion.getEquipo(),
                new CondicionesExperimentales(1500, 55000, 7, Atmosfera.VACIO)
        );

        var resultado = new MotorExperimentacionLibre(catalogo)
                .evaluarReaccion(AccionLaboratorio.ESPERAR, TipoEquipo.REACTOR, experimento);

        assertTrue(resultado.isEmpty());
        assertEquals(1, inventario.obtenerCantidad(nacl));
        assertFalse(inventario.contiene(
                catalogo.buscarSustancia("Na").orElseThrow()
        ));
    }

    @Test
    void motorLibreEjecutaUnaReaccionCatalogadaYActualizaElInventario() {
        Reaccion combustion = catalogo.buscarReaccion("R_CO2").orElseThrow();
        Experimento experimento = fabrica.crearExperimentoParaReaccion(combustion);

        var resultado = new MotorExperimentacionLibre(catalogo)
                .evaluarReaccion(AccionLaboratorio.ESPERAR, TipoEquipo.HORNO, experimento);

        assertTrue(resultado.isPresent());
        assertEquals("R_CO2", resultado.orElseThrow().reaccion().getId());
        var co2 = catalogo.buscarSustancia("CO2").orElseThrow();
        assertTrue(experimento.getInventario().contiene(co2));
        assertEquals(1.0, experimento.getInventario().obtenerCantidad(co2), 1.0e-9);
        assertEquals(UnidadCantidad.MOLES, experimento.getInventario().obtenerUnidad(co2));
        assertFalse(experimento.getInventario().contiene(
                catalogo.buscarSustancia("C").orElseThrow()
        ));
        assertFalse(experimento.getInventario().contiene(
                catalogo.buscarSustancia("O2").orElseThrow()
        ));
    }

    @Test
    void elInventarioCompartidoConservaProductosAlCambiarDePuesto() {
        Reaccion combustion = catalogo.buscarReaccion("R_CO2").orElseThrow();
        Experimento experimento = fabrica.crearExperimentoParaReaccion(combustion);
        SesionJuego sesion = crearSesionDificil();
        sesion.iniciar(experimento, combustion, FabricaAutomata.crearAutomataParaReaccion(combustion.getId()));

        sesion.registrarEntradaPuesto("Mesa de síntesis", TipoEquipo.MATRAZ);
        assertTrue(sesion.ejecutarAccion(AccionLaboratorio.CARGAR_REACTIVO).isExitoso());
        assertTrue(sesion.ejecutarAccion(AccionLaboratorio.MEZCLAR).isExitoso());

        var carbono = catalogo.buscarSustancia("C").orElseThrow();
        var oxigeno = catalogo.buscarSustancia("O2").orElseThrow();
        assertTrue(sesion.obtenerMaterialesDisponibles().containsKey(carbono));
        assertTrue(sesion.obtenerMaterialesDisponibles().containsKey(oxigeno));

        sesion.salirPuesto();
        sesion.registrarEntradaPuesto("Zona térmica", TipoEquipo.HORNO);
        assertTrue(sesion.ejecutarAccion(AccionLaboratorio.ACTIVAR_IGNICION).isExitoso());

        var co2 = catalogo.buscarSustancia("CO2").orElseThrow();
        assertEquals(1.0, sesion.obtenerMaterialesDisponibles().get(co2).getValor(), 1.0e-9);

        sesion.salirPuesto();
        sesion.registrarEntradaPuesto("Mesa de síntesis", TipoEquipo.MATRAZ);
        assertEquals(1.0, sesion.obtenerMaterialesDisponibles().get(co2).getValor(), 1.0e-9);
        sesion.salirPuesto();
        sesion.registrarEntradaPuesto("Zona térmica", TipoEquipo.HORNO);
        assertTrue(sesion.ejecutarAccion(AccionLaboratorio.FINALIZAR).isCompletado());
        assertEquals(1.0, experimento.getInventario().obtenerCantidad(co2), 1.0e-9);
    }

    @Test
    void productoGeneradoEnUnaEstacionPuedeSerConsumidoEnLaSiguiente() {
        Reaccion formacionAgua = catalogo.buscarReaccion("R_H2O").orElseThrow();
        Reaccion corrosion = catalogo.buscarReaccion("R_CORROSION_FE").orElseThrow();
        Experimento configuracion = fabrica.crearExperimentoParaReaccion(corrosion);
        Inventario inventario = new Inventario();
        inventario.agregar(catalogo.buscarSustancia("Fe").orElseThrow(), 223.38, UnidadCantidad.GRAMOS);
        inventario.agregar(catalogo.buscarSustancia("O2").orElseThrow(), 288.0, UnidadCantidad.GRAMOS);
        inventario.agregar(catalogo.buscarSustancia("H2").orElseThrow(), 24.192, UnidadCantidad.GRAMOS);
        Experimento experimento = new Experimento(
                configuracion.getId(),
                configuracion.getNombre(),
                configuracion.getObjetivo(),
                inventario,
                configuracion.getEquipo(),
                configuracion.getCondiciones()
        );
        MotorExperimentacionLibre motor = new MotorExperimentacionLibre(catalogo);

        var agua = motor.evaluarReaccion(
                AccionLaboratorio.MEZCLAR,
                TipoEquipo.MATRAZ,
                experimento,
                formacionAgua
        );
        assertTrue(agua.isPresent());
        var aguaDisponible = catalogo.buscarSustancia("H2O").orElseThrow();
        assertEquals(12.0, inventario.obtenerCantidad(aguaDisponible), 1.0e-9);

        var herrumbre = motor.evaluarReaccion(
                AccionLaboratorio.ESPERAR,
                TipoEquipo.BANDEJA,
                experimento,
                corrosion
        );

        assertTrue(herrumbre.isPresent());
        assertEquals("R_CORROSION_FE", herrumbre.orElseThrow().reaccion().getId());
        assertEquals(4.0, inventario.obtenerCantidad(
                catalogo.buscarSustancia("FeOH3").orElseThrow()), 1.0e-9);
    }

    @Test
    void tutorialRegistraComoFallidoUnProcedimientoSinResultadoQuimico() {
        SesionJuego sesion = FabricaSesion.crearSesion(
                catalogo,
                FabricaAutomata.crearAutomataSimple()
        );
        Reaccion reaccion = catalogo.buscarReaccion("R_H2O").orElseThrow();
        Experimento experimento = fabrica.crearExperimentoVacioParaReaccion(reaccion);
        sesion.iniciar(experimento, reaccion, FabricaAutomata.crearAutomataSimple());

        var resultado = sesion.ejecutarAccion(AccionLaboratorio.CARGAR_REACTIVO);

        assertFalse(resultado.isCompletado());
        assertFalse(sesion.isActiva());
        assertEquals(EstadoRegistroExperimento.FALLIDO,
                sesion.getCuaderno().getHistorial().getFirst().getEstadoFinal());
        assertEquals(com.chemlab.model.enums.EstadoExperimento.FALLIDO, experimento.getEstado());
    }

    private SesionJuego crearSesionDificil() {
        return FabricaSesion.crearSesion(
                catalogo,
                FabricaAutomata.crearAutomataSimple(),
                ModoJuego.DIFICIL
        );
    }
}
