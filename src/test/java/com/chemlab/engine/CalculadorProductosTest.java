package com.chemlab.engine;

import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculadorProductosTest {

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
                "R1",
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
    void debeCalcularProductosSegunElAvance() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();
        Sustancia agua = crearAgua();

        Reaccion reaccion =
                crearReaccion(
                        hidrogeno,
                        oxigeno,
                        agua
                );

        CalculadorProductos calculador =
                new CalculadorProductos();

        Map<Sustancia, Cantidad> productos =
                calculador.calcular(
                        reaccion,
                        0.3125
                );

        assertTrue(
                productos.containsKey(agua)
        );

        Cantidad cantidadAgua =
                productos.get(agua);

        assertEquals(
                0.625,
                cantidadAgua.getValor(),
                0.000001
        );

        assertEquals(
                UnidadCantidad.MOLES,
                cantidadAgua.getUnidad()
        );
    }

    @Test
    void debePermitirAvanceCero() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();
        Sustancia agua = crearAgua();

        Reaccion reaccion =
                crearReaccion(
                        hidrogeno,
                        oxigeno,
                        agua
                );

        CalculadorProductos calculador =
                new CalculadorProductos();

        Map<Sustancia, Cantidad> productos =
                calculador.calcular(
                        reaccion,
                        0
                );

        assertEquals(
                0.0,
                productos.get(agua).getValor(),
                0.000001
        );
    }

    @Test
    void noDebePermitirAvanceNegativo() {

        Sustancia agua = crearAgua();

        Reaccion reaccion =
                new Reaccion(
                        "R1",
                        "Prueba",
                        Map.of(agua, 1),
                        Map.of(agua, 1)
                );

        CalculadorProductos calculador =
                new CalculadorProductos();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        calculador.calcular(
                                reaccion,
                                -1
                        )
        );
    }
}