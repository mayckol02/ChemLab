package com.chemlab.engine;

import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoFalloReaccion;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.EvaluacionReaccion;
import com.chemlab.service.ConversorQuimico;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculadorEstequiometricoFalloTest {

    private final CalculadorEstequiometrico calculador =
            new CalculadorEstequiometrico(
                    new ConversorQuimico()
            );

    @Test
    void debeIndicarCuandoFaltaUnReactivo() {
        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();

        Inventario inventario = new Inventario();
        inventario.agregar(
                hidrogeno,
                10.0,
                UnidadCantidad.GRAMOS
        );

        EvaluacionReaccion evaluacion = calculador.evaluar(
                crearReaccion(hidrogeno, oxigeno),
                inventario,
                new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE)
        );

        assertFalse(evaluacion.esPosible());
        assertNull(evaluacion.getResultadoEstequiometrico());
        assertEquals(
                TipoFalloReaccion.REACTIVO_INEXISTENTE,
                evaluacion.getTipoFallo()
        );
        assertTrue(
                evaluacion.getMotivo().contains(
                        oxigeno.getNombre()
                )
        );
    }

    @Test
    void debeIndicarCantidadInsuficienteParaAvanceSolicitado() {
        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();

        Inventario inventario = new Inventario();
        inventario.agregar(
                hidrogeno,
                10.0,
                UnidadCantidad.GRAMOS
        );
        inventario.agregar(
                oxigeno,
                10.0,
                UnidadCantidad.GRAMOS
        );

        EvaluacionReaccion evaluacion = calculador.evaluar(
                crearReaccion(hidrogeno, oxigeno),
                inventario,
                new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE),
                1.0
        );

        assertFalse(evaluacion.esPosible());
        assertEquals(
                TipoFalloReaccion.CANTIDAD_INSUFICIENTE,
                evaluacion.getTipoFallo()
        );
        assertTrue(
                evaluacion.getMotivo().contains(
                        oxigeno.getNombre()
                )
        );
    }

    @Test
    void debePermitirAvanceFraccionarioSinAvanceSolicitado() {
        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();

        Inventario inventario = new Inventario();
        inventario.agregar(
                hidrogeno,
                10.0,
                UnidadCantidad.GRAMOS
        );
        inventario.agregar(
                oxigeno,
                10.0,
                UnidadCantidad.GRAMOS
        );

        EvaluacionReaccion evaluacion = calculador.evaluar(
                crearReaccion(hidrogeno, oxigeno),
                inventario,
                new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE)
        );

        assertTrue(evaluacion.esPosible());
        assertEquals(
                TipoFalloReaccion.NINGUNO,
                evaluacion.getTipoFallo()
        );
        assertEquals(
                0.3125,
                evaluacion
                        .getResultadoEstequiometrico()
                        .getAvanceMaximo(),
                0.0001
        );
    }

    @Test
    void debeRechazarAvanceSolicitadoNoPositivo() {
        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculador.evaluar(
                        crearReaccion(hidrogeno, oxigeno),
                        new Inventario(),
                        new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE),
                        0.0
                )
        );
    }

    private Reaccion crearReaccion(
            Sustancia hidrogeno,
            Sustancia oxigeno
    ) {
        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H₂O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        return new Reaccion(
                "R001",
                "Formación de agua",
                Map.of(
                        hidrogeno, 2,
                        oxigeno, 1
                ),
                Map.of(agua, 2)
        );
    }

    private Sustancia crearHidrogeno() {
        return new Sustancia(
                "H2",
                "Hidrógeno",
                "H₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );
    }

    private Sustancia crearOxigeno() {
        return new Sustancia(
                "O2",
                "Oxígeno",
                "O₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                32.0
        );
    }
}