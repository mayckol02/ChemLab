package com.chemlab.engine;

import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.service.ConversorQuimico;
import org.junit.jupiter.api.Test;
import com.chemlab.model.value.ResultadoEstequiometrico;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculadorEstequiometricoTest {

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
                32.00
        );
    }

    private Sustancia crearAgua() {
        return new Sustancia(
                "H2O",
                "Agua",
                "H₂O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );
    }

    private Reaccion crearReaccion(
            Sustancia hidrogeno,
            Sustancia oxigeno,
            Sustancia agua
    ) {
        return new Reaccion(
                "R001",
                "Formación de agua",
                Map.of(
                        hidrogeno, 2,
                        oxigeno, 1
                ),
                Map.of(
                        agua, 2
                )
        );
    }

    @Test
    void debeCalcularElAvanceMaximo() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();
        Sustancia agua = crearAgua();

        Reaccion reaccion =
                crearReaccion(
                        hidrogeno,
                        oxigeno,
                        agua
                );

        Inventario inventario = new Inventario();

        inventario.agregar(
                hidrogeno,
                10,
                UnidadCantidad.GRAMOS
        );

        inventario.agregar(
                oxigeno,
                10,
                UnidadCantidad.GRAMOS
        );

        CalculadorEstequiometrico calculador =
                new CalculadorEstequiometrico(
                        new ConversorQuimico()
                );

        double avance =
                calculador.calcularAvanceMaximo(
                        reaccion,
                        inventario
                );

        assertEquals(
                0.3125,
                avance,
                0.0001
        );
    }

    @Test
    void debeIdentificarElReactivoLimitante() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();
        Sustancia agua = crearAgua();

        Reaccion reaccion =
                crearReaccion(
                        hidrogeno,
                        oxigeno,
                        agua
                );

        Inventario inventario = new Inventario();

        inventario.agregar(
                hidrogeno,
                10,
                UnidadCantidad.GRAMOS
        );

        inventario.agregar(
                oxigeno,
                10,
                UnidadCantidad.GRAMOS
        );

        CalculadorEstequiometrico calculador =
                new CalculadorEstequiometrico(
                        new ConversorQuimico()
                );

        Sustancia limitante =
                calculador.obtenerReactivoLimitante(
                        reaccion,
                        inventario
                );

        assertEquals(
                oxigeno,
                limitante
        );
    }

    @Test
    void debeRetornarCeroSiFaltaUnReactivo() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();
        Sustancia agua = crearAgua();

        Reaccion reaccion =
                crearReaccion(
                        hidrogeno,
                        oxigeno,
                        agua
                );

        Inventario inventario = new Inventario();

        inventario.agregar(
                hidrogeno,
                10,
                UnidadCantidad.GRAMOS
        );

        CalculadorEstequiometrico calculador =
                new CalculadorEstequiometrico(
                        new ConversorQuimico()
                );

        assertEquals(
                0.0,
                calculador.calcularAvanceMaximo(
                        reaccion,
                        inventario
                )
        );
    }

    @Test
    void debeCalcularResultadoEstequiometricoCompleto() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();
        Sustancia agua = crearAgua();

        Reaccion reaccion =
                crearReaccion(
                        hidrogeno,
                        oxigeno,
                        agua
                );

        Inventario inventario = new Inventario();

        inventario.agregar(
                hidrogeno,
                10,
                UnidadCantidad.GRAMOS
        );

        inventario.agregar(
                oxigeno,
                10,
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

        assertNotNull(resultado);

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
                10.0,
                resultado
                        .getCantidadConsumida(oxigeno)
                        .getValor(),
                0.0001
        );
    }
}