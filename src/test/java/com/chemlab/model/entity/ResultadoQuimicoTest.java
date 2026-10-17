package com.chemlab.model.entity;

import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResultadoQuimicoTest {

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

    @Test
    void debeCrearUnResultadoExitoso() {

        Sustancia agua = crearAgua();

        ResultadoQuimico resultado =
                new ResultadoQuimico(
                        Map.of(
                                agua,
                                new Cantidad(
                                        1.5,
                                        UnidadCantidad.MOLES
                                )
                        ),
                        true
                );

        assertEquals(
                1.5,
                resultado
                        .getCantidad(agua)
                        .getValor(),
                0.000001
        );
        assertEquals(
                UnidadCantidad.MOLES,
                resultado
                        .getCantidad(agua)
                        .getUnidad()
        );
        assertTrue(
                resultado.isExitoso()
        );
    }

    @Test
    void debePermitirMultiplesProductos() {

        Sustancia agua = crearAgua();

        Sustancia hidrogeno = new Sustancia(
                "H2",
                "Hidrógeno",
                "H₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );

        ResultadoQuimico resultado =
                new ResultadoQuimico(
                        Map.of(
                                agua,
                                new Cantidad(
                                        2.0,
                                        UnidadCantidad.MOLES
                                ),
                                hidrogeno,
                                new Cantidad(
                                        1.0,
                                        UnidadCantidad.MOLES
                                )
                        ),
                        true
                );

        assertEquals(
                2.0,
                resultado
                        .getCantidad(agua)
                        .getValor(),
                0.000001
        );

        assertEquals(
                1.0,
                resultado
                        .getCantidad(hidrogeno)
                        .getValor(),
                0.000001
        );

        assertEquals(
                UnidadCantidad.MOLES,
                resultado
                        .getCantidad(agua)
                        .getUnidad()
        );

        assertEquals(
                UnidadCantidad.MOLES,
                resultado
                        .getCantidad(hidrogeno)
                        .getUnidad()
        );
    }

    @Test
    void debeCrearUnResultadoFallido() {

        Sustancia agua = crearAgua();

        ResultadoQuimico resultado =
                new ResultadoQuimico(
                        Map.of(
                                agua,
                                new Cantidad(
                                        0.0,
                                        UnidadCantidad.MOLES
                                )
                        ),
                        false
                );

        assertEquals(
                0.0,
                resultado
                        .getCantidad(agua)
                        .getValor(),
                0.000001
        );
        assertEquals(
                UnidadCantidad.MOLES,
                resultado
                        .getCantidad(agua)
                        .getUnidad()
        );
        assertFalse(
                resultado.isExitoso()
        );
    }

    @Test
    void noDebePermitirCantidadNegativa() {

        Sustancia agua = crearAgua();

        assertThrows(
                IllegalArgumentException.class,
                () -> new ResultadoQuimico(
                        Map.of(
                                agua,
                                new Cantidad(
                                        -1.0,
                                        UnidadCantidad.MOLES
                                )
                        ),
                        false
                )
        );
    }

    @Test
    void losProductosNoDebenPoderModificarElResultado() {

        Sustancia agua = crearAgua();

        ResultadoQuimico resultado =
                new ResultadoQuimico(
                        Map.of(
                                agua,
                                new Cantidad(
                                        1.0,
                                        UnidadCantidad.MOLES
                                )
                        ),
                        true
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> resultado.getProductos().clear()
        );
    }
}