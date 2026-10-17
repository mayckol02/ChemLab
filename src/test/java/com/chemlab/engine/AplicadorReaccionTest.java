package com.chemlab.engine;

import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.ResultadoEstequiometrico;
import com.chemlab.service.ConversorQuimico;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AplicadorReaccionTest {

    @Test
    void debeRetirarLosReactivosDelInventario() {

        Sustancia hidrogeno = new Sustancia(
                "H2",
                "Hidrógeno",
                "H2",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );

        Sustancia oxigeno = new Sustancia(
                "O2",
                "Oxígeno",
                "O2",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                32.00
        );

        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H2O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        Reaccion reaccion = new Reaccion(
                "combustion-hidrogeno",
                "Formación de agua",
                Map.of(
                        hidrogeno, 2,
                        oxigeno, 1
                ),
                Map.of(
                        agua, 2
                )
        );

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

        CalculadorEstequiometrico calculador =
                new CalculadorEstequiometrico(
                        new ConversorQuimico()
                );

        com.chemlab.model.entity.CondicionesExperimentales condiciones = 
                new com.chemlab.model.entity.CondicionesExperimentales(25.0, 1.0, 7.0, com.chemlab.model.enums.Atmosfera.AIRE);

        ResultadoEstequiometrico resultado =
                calculador.evaluar(
                        reaccion,
                        inventario,
                        condiciones
                ).getResultadoEstequiometrico();

        AplicadorReaccion aplicador =
                new AplicadorReaccion();

        aplicador.aplicar(
                reaccion,
                resultado,
                inventario
        );

        assertEquals(
                8.74,
                inventario.obtenerCantidad(hidrogeno),
                0.01
        );

        assertEquals(
                0.0,
                inventario.obtenerCantidad(oxigeno),
                0.0001
        );
    }

    @Test
    void debeAgregarLosProductosAlInventario() {

        Sustancia hidrogeno = new Sustancia(
                "H2",
                "Hidrógeno",
                "H2",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );

        Sustancia oxigeno = new Sustancia(
                "O2",
                "Oxígeno",
                "O2",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                32.00
        );

        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H2O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        Reaccion reaccion = new Reaccion(
                "combustion-hidrogeno",
                "Formación de agua",
                Map.of(
                        hidrogeno, 2,
                        oxigeno, 1
                ),
                Map.of(
                        agua, 2
                )
        );

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

        CalculadorEstequiometrico calculador =
                new CalculadorEstequiometrico(
                        new ConversorQuimico()
                );

        com.chemlab.model.entity.CondicionesExperimentales condiciones = 
                new com.chemlab.model.entity.CondicionesExperimentales(25.0, 1.0, 7.0, com.chemlab.model.enums.Atmosfera.AIRE);

        ResultadoEstequiometrico resultado =
                calculador.evaluar(
                        reaccion,
                        inventario,
                        condiciones
                ).getResultadoEstequiometrico();

        AplicadorReaccion aplicador =
                new AplicadorReaccion();

        aplicador.aplicar(
                reaccion,
                resultado,
                inventario
        );

        assertTrue(
                inventario.contiene(agua)
        );

        assertEquals(
                0.625,
                inventario.obtenerCantidad(agua),
                0.0001
        );

        assertEquals(
                UnidadCantidad.MOLES,
                inventario.obtenerUnidad(agua)
        );
    }
}