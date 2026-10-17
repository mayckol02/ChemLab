package com.chemlab.model.value;

import com.chemlab.model.enums.UnidadCantidad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CantidadTest {

    @Test
    void debeCrearUnaCantidad() {

        Cantidad cantidad = new Cantidad(
                10.5,
                UnidadCantidad.GRAMOS
        );

        assertEquals(
                10.5,
                cantidad.getValor()
        );

        assertEquals(
                UnidadCantidad.GRAMOS,
                cantidad.getUnidad()
        );
    }

    @Test
    void debeRechazarValoresNegativos() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Cantidad(
                        -1,
                        UnidadCantidad.GRAMOS
                )
        );
    }

    @Test
    void debeRechazarUnidadNull() {

        assertThrows(
                NullPointerException.class,
                () -> new Cantidad(
                        10,
                        null
                )
        );
    }

    @Test
    void debePermitirCero() {

        Cantidad cantidad = new Cantidad(
                0,
                UnidadCantidad.GRAMOS
        );

        assertEquals(
                0,
                cantidad.getValor()
        );
    }

    @Test
    void dosCantidadesIgualesDebenSerIguales() {

        Cantidad cantidad1 = new Cantidad(
                10,
                UnidadCantidad.GRAMOS
        );

        Cantidad cantidad2 = new Cantidad(
                10,
                UnidadCantidad.GRAMOS
        );

        assertEquals(
                cantidad1,
                cantidad2
        );
    }

    @Test
    void cantidadesConDistintaUnidadNoDebenSerIguales() {

        Cantidad gramos = new Cantidad(
                10,
                UnidadCantidad.GRAMOS
        );

        Cantidad moles = new Cantidad(
                10,
                UnidadCantidad.MOLES
        );

        assertNotEquals(
                gramos,
                moles
        );
    }
}