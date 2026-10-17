package com.chemlab.model.value;

import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResultadoEstequiometricoTest {

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

    @Test
    void debeCrearResultadoEstequiometrico() {

        Sustancia oxigeno = crearOxigeno();
        Sustancia hidrogeno = crearHidrogeno();

        Cantidad cantidadOxigeno =
                new Cantidad(
                        10,
                        UnidadCantidad.GRAMOS
                );

        Cantidad cantidadHidrogeno =
                new Cantidad(
                        1.26,
                        UnidadCantidad.GRAMOS
                );

        ResultadoEstequiometrico resultado =
                new ResultadoEstequiometrico(
                        oxigeno,
                        0.3125,
                        Map.of(
                                oxigeno, cantidadOxigeno,
                                hidrogeno, cantidadHidrogeno
                        )
                );

        assertEquals(
                oxigeno,
                resultado.getReactivoLimitante()
        );

        assertEquals(
                0.3125,
                resultado.getAvanceMaximo(),
                0.000001
        );

        assertEquals(
                10,
                resultado
                        .getCantidadConsumida(oxigeno)
                        .getValor(),
                0.000001
        );

        assertEquals(
                UnidadCantidad.GRAMOS,
                resultado
                        .getCantidadConsumida(oxigeno)
                        .getUnidad()
        );
    }

    @Test
    void noDebePermitirAvanceNegativo() {

        Sustancia oxigeno = crearOxigeno();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ResultadoEstequiometrico(
                                oxigeno,
                                -1,
                                Map.of()
                        )
        );
    }

    @Test
    void lasCantidadesNoDebenPoderModificarElResultado() {

        Sustancia oxigeno = crearOxigeno();

        ResultadoEstequiometrico resultado =
                new ResultadoEstequiometrico(
                        oxigeno,
                        1,
                        Map.of(
                                oxigeno,
                                new Cantidad(
                                        10,
                                        UnidadCantidad.GRAMOS
                                )
                        )
                );

        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        resultado
                                .getCantidadesConsumidas()
                                .clear()
        );
    }
}