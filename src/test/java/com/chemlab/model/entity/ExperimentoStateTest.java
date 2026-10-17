package com.chemlab.model.entity;

import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.EstadoExperimento;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.model.enums.TipoSustancia;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentoStateTest {

    private Experimento crearExperimento() {

        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H2O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        Objetivo objetivo = new Objetivo(
                "OBJ-001",
                "Obtener agua",
                agua,
                1.0
        );

        Inventario inventario = new Inventario();

        Equipo equipo = new Equipo(
                "EQ-001",
                "Recipiente",
                TipoEquipo.RECIPIENTE,
                1000
        );

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25.0,
                        1.0,
                        7.0,
                        Atmosfera.AIRE
                );

        return new Experimento(
                "EXP-001",
                "Experimento de prueba",
                objetivo,
                inventario,
                equipo,
                condiciones
        );
    }

    private ResultadoQuimico resultadoExitoso() {

        return new ResultadoQuimico(
                Map.of(),
                true
        );
    }

    private ResultadoQuimico resultadoFallido() {

        return new ResultadoQuimico(
                Map.of(),
                false
        );
    }

    @Test
    void debeIniciarExperimento() {

        Experimento experimento =
                crearExperimento();

        assertEquals(
                EstadoExperimento.NO_INICIADO,
                experimento.getEstado()
        );

        experimento.iniciar();

        assertEquals(
                EstadoExperimento.EN_PROGRESO,
                experimento.getEstado()
        );
    }

    @Test
    void noDebeIniciarDosVeces() {

        Experimento experimento =
                crearExperimento();

        experimento.iniciar();

        assertThrows(
                IllegalStateException.class,
                experimento::iniciar
        );
    }

    @Test
    void debeCompletarExperimentoExitoso() {

        Experimento experimento =
                crearExperimento();

        experimento.iniciar();

        ResultadoQuimico resultado =
                resultadoExitoso();

        experimento.completar(resultado);

        assertEquals(
                EstadoExperimento.COMPLETADO,
                experimento.getEstado()
        );

        assertSame(
                resultado,
                experimento.getResultado()
        );
    }

    @Test
    void debeMarcarExperimentoComoFallido() {

        Experimento experimento =
                crearExperimento();

        experimento.iniciar();

        ResultadoQuimico resultado =
                resultadoFallido();

        experimento.completar(resultado);

        assertEquals(
                EstadoExperimento.FALLIDO,
                experimento.getEstado()
        );

        assertSame(
                resultado,
                experimento.getResultado()
        );
    }

    @Test
    void noDebeCompletarExperimentoSinIniciar() {

        Experimento experimento =
                crearExperimento();

        assertThrows(
                IllegalStateException.class,
                () -> experimento.completar(
                        resultadoExitoso()
                )
        );
    }

    /*@Test
    void noDebeMarcarComoFallidoExperimentoSinIniciar() {

        Experimento experimento =
                crearExperimento();

        assertThrows(
                IllegalStateException.class,
                () -> experimento.cambiarEstado(
                        new com.chemlab.model.state.ExperimentoFallidoState()
                )
        );
    }*/

    @Test
    void noDebeIniciarExperimentoCompletado() {

        Experimento experimento =
                crearExperimento();

        experimento.iniciar();
        experimento.completar(
                resultadoExitoso()
        );

        assertThrows(
                IllegalStateException.class,
                experimento::iniciar
        );
    }

    @Test
    void noDebeCompletarExperimentoCompletado() {

        Experimento experimento =
                crearExperimento();

        experimento.iniciar();
        experimento.completar(
                resultadoExitoso()
        );

        assertThrows(
                IllegalStateException.class,
                () -> experimento.completar(
                        resultadoExitoso()
                )
        );
    }

    @Test
    void noDebeIniciarExperimentoFallido() {

        Experimento experimento =
                crearExperimento();

        experimento.iniciar();
        experimento.completar(
                resultadoFallido()
        );

        assertThrows(
                IllegalStateException.class,
                experimento::iniciar
        );
    }

    @Test
    void noDebeCompletarExperimentoFallido() {

        Experimento experimento =
                crearExperimento();

        experimento.iniciar();
        experimento.completar(
                resultadoFallido()
        );

        assertThrows(
                IllegalStateException.class,
                () -> experimento.completar(
                        resultadoExitoso()
                )
        );
    }
}