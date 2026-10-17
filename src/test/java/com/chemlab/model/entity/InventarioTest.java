package com.chemlab.model.entity;

import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventarioTest {

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
    void debeAgregarUnaSustancia() {

        Inventario inventario = new Inventario();
        Sustancia agua = crearAgua();

        inventario.agregar(
                agua,
                10,
                UnidadCantidad.MOLES
        );

        assertTrue(inventario.contiene(agua));
        assertEquals(10, inventario.obtenerCantidad(agua));
        assertEquals(
                UnidadCantidad.MOLES,
                inventario.obtenerUnidad(agua)
        );
    }

    @Test
    void debeAcumularCantidadesDeLaMismaSustancia() {

        Inventario inventario = new Inventario();
        Sustancia agua = crearAgua();

        inventario.agregar(
                agua,
                10,
                UnidadCantidad.MOLES
        );

        inventario.agregar(
                agua,
                5,
                UnidadCantidad.MOLES
        );

        assertEquals(15, inventario.obtenerCantidad(agua));
    }

    @Test
    void debeRetirarUnaCantidad() {

        Inventario inventario = new Inventario();
        Sustancia agua = crearAgua();

        inventario.agregar(
                agua,
                10,
                UnidadCantidad.MOLES
        );

        inventario.retirar(agua, 4);

        assertEquals(6, inventario.obtenerCantidad(agua));
    }

    @Test
    void debeEliminarLaSustanciaCuandoLaCantidadLlegaACero() {

        Inventario inventario = new Inventario();
        Sustancia agua = crearAgua();

        inventario.agregar(
                agua,
                10,
                UnidadCantidad.MOLES
        );

        inventario.retirar(agua, 10);

        assertFalse(inventario.contiene(agua));
        assertEquals(0, inventario.obtenerCantidad(agua));
    }

    @Test
    void noDebePermitirRetirarMasDeLoDisponible() {

        Inventario inventario = new Inventario();
        Sustancia agua = crearAgua();

        inventario.agregar(
                agua,
                5,
                UnidadCantidad.MOLES
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inventario.retirar(agua, 10)
        );
    }

    @Test
    void noDebePermitirCantidadCero() {

        Inventario inventario = new Inventario();
        Sustancia agua = crearAgua();

        assertThrows(
                IllegalArgumentException.class,
                () -> inventario.agregar(
                        agua,
                        0,
                        UnidadCantidad.MOLES
                )
        );
    }

    @Test
    void inventarioNuevoDebeEstarVacio() {

        Inventario inventario = new Inventario();

        assertTrue(inventario.estaVacio());
    }

    @Test
    void debeConvertirUnidadesAlAgregarLaMismaSustancia() {

        Inventario inventario = new Inventario();

        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H2O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        inventario.agregar(
                agua,
                1.0,
                UnidadCantidad.GRAMOS
        );

        inventario.agregar(
                agua,
                500.0,
                UnidadCantidad.MILIGRAMOS
        );

        assertEquals(
                1.5,
                inventario.obtenerCantidad(agua),
                0.0001
        );

        assertEquals(
                UnidadCantidad.GRAMOS,
                inventario.obtenerUnidad(agua)
        );
    }

    @Test
    void debeConvertirUnidadesAlRetirar() {

        Inventario inventario = new Inventario();

        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H2O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        inventario.agregar(
                agua,
                2.0,
                UnidadCantidad.GRAMOS
        );

        inventario.retirar(
                agua,
                new Cantidad(
                        500.0,
                        UnidadCantidad.MILIGRAMOS
                )
        );

        assertEquals(
                1.5,
                inventario.obtenerCantidad(agua),
                0.0001
        );
    }
}