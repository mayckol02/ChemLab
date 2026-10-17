package com.chemlab.model.entity;

import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.TipoFalloReaccion;
import com.chemlab.model.exception.ReaccionInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReaccionTest {

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
                31.998
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

    @Test
    void debeCrearUnaReaccionValida() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();
        Sustancia agua = crearAgua();

        Reaccion reaccion = new Reaccion(
                "RX_001",
                "Formación de agua",

                Map.of(
                        hidrogeno, 2,
                        oxigeno, 1
                ),

                Map.of(
                        agua, 2
                )
        );

        assertEquals(
                "RX_001",
                reaccion.getId()
        );

        assertEquals(
                "Formación de agua",
                reaccion.getNombre()
        );

        assertEquals(
                2,
                reaccion.getReactivos().get(hidrogeno)
        );

        assertEquals(
                1,
                reaccion.getReactivos().get(oxigeno)
        );

        assertEquals(
                2,
                reaccion.getProductos().get(agua)
        );
    }

    @Test
    void noDebePermitirReaccionSinReactivos() {

        Sustancia agua = crearAgua();

        ReaccionInvalidaException error = assertThrows(
                ReaccionInvalidaException.class,
                () -> new Reaccion(
                        "RX_002",
                        "Reacción inválida",
                        Map.of(),
                        Map.of(agua, 1)
                )
        );

        assertEquals(
                TipoFalloReaccion.REACCION_INVALIDA,
                error.getTipoFallo()
        );
    }

    @Test
    void noDebePermitirReaccionSinProductos() {

        Sustancia hidrogeno = crearHidrogeno();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Reaccion(
                        "RX_003",
                        "Reacción inválida",
                        Map.of(hidrogeno, 1),
                        Map.of()
                )
        );
    }

    @Test
    void noDebePermitirCoeficienteCero() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia agua = crearAgua();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Reaccion(
                        "RX_004",
                        "Reacción inválida",
                        Map.of(hidrogeno, 0),
                        Map.of(agua, 1)
                )
        );
    }

    @Test
    void noDebePermitirCoeficienteNegativo() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia agua = crearAgua();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Reaccion(
                        "RX_005",
                        "Reacción inválida",
                        Map.of(hidrogeno, -1),
                        Map.of(agua, 1)
                )
        );
    }

    @Test
    void losReactivosNoDebenPoderModificarLaReaccion() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia agua = crearAgua();

        Reaccion reaccion = new Reaccion(
                "RX_006",
                "Formación de agua",
                Map.of(hidrogeno, 2),
                Map.of(agua, 2)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> reaccion.getReactivos().clear()
        );
    }
}