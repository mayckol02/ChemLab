package com.chemlab.engine;

import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoFalloReaccion;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.EvaluacionReaccion;
import com.chemlab.model.value.ResultadoEstequiometrico;
import com.chemlab.service.ConversorQuimico;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluacionReaccionComportamientoTest {

    private final CalculadorEstequiometrico calculador =
            new CalculadorEstequiometrico(
                    new ConversorQuimico()
            );

    @Test
    void debeDevolverResultadoEstequiometricoCorrecto() {
        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();

        Inventario inventario = crearInventario(
                hidrogeno,
                oxigeno
        );

        com.chemlab.model.entity.CondicionesExperimentales condiciones = 
                new com.chemlab.model.entity.CondicionesExperimentales(25.0, 1.0, 7.0, com.chemlab.model.enums.Atmosfera.AIRE);

        EvaluacionReaccion evaluacion = calculador.evaluar(
                crearReaccion(hidrogeno, oxigeno),
                inventario,
                condiciones
        );

        assertTrue(evaluacion.esPosible());
        assertEquals(
                TipoFalloReaccion.NINGUNO,
                evaluacion.getTipoFallo()
        );

        ResultadoEstequiometrico resultado =
                evaluacion.getResultadoEstequiometrico();

        assertEquals(
                oxigeno,
                resultado.getReactivoLimitante()
        );

        assertEquals(
                0.3125,
                resultado.getAvanceMaximo(),
                0.0001
        );

        assertEquals(
                1.26,
                resultado
                        .getCantidadConsumida(hidrogeno)
                        .getValor(),
                0.0001
        );

        assertEquals(
                10.0,
                resultado
                        .getCantidadConsumida(oxigeno)
                        .getValor(),
                0.0001
        );

        assertEquals(
                UnidadCantidad.GRAMOS,
                resultado
                        .getCantidadConsumida(hidrogeno)
                        .getUnidad()
        );
    }

    @Test
    void evaluarNoDebeModificarElInventario() {
        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();

        Inventario inventario = crearInventario(
                hidrogeno,
                oxigeno
        );

        com.chemlab.model.entity.CondicionesExperimentales condiciones = 
                new com.chemlab.model.entity.CondicionesExperimentales(25.0, 1.0, 7.0, com.chemlab.model.enums.Atmosfera.AIRE);

        calculador.evaluar(
                crearReaccion(hidrogeno, oxigeno),
                inventario,
                condiciones
        );

        assertEquals(
                10.0,
                inventario.obtenerCantidad(hidrogeno),
                0.0001
        );

        assertEquals(
                10.0,
                inventario.obtenerCantidad(oxigeno),
                0.0001
        );

        assertEquals(
                UnidadCantidad.GRAMOS,
                inventario.obtenerUnidad(hidrogeno)
        );

        assertEquals(
                UnidadCantidad.GRAMOS,
                inventario.obtenerUnidad(oxigeno)
        );
    }

    private Inventario crearInventario(
            Sustancia hidrogeno,
            Sustancia oxigeno
    ) {
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

        return inventario;
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