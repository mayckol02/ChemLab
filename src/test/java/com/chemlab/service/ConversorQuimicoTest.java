package com.chemlab.service;

import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConversorQuimicoTest {

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
    void debeConvertirGramosAMoles() {

        Sustancia agua = crearAgua();

        Cantidad resultado =
                new ConversorQuimico()
                        .convertirAMoles(
                                new Cantidad(
                                        36.03,
                                        UnidadCantidad.GRAMOS
                                ),
                                agua
                        );

        assertEquals(
                2.0,
                resultado.getValor(),
                0.000001
        );

        assertEquals(
                UnidadCantidad.MOLES,
                resultado.getUnidad()
        );
    }

    @Test
    void debeConvertirKilogramosAMoles() {

        Sustancia agua = crearAgua();

        Cantidad resultado =
                new ConversorQuimico()
                        .convertirAMoles(
                                new Cantidad(
                                        0.018015,
                                        UnidadCantidad.KILOGRAMOS
                                ),
                                agua
                        );

        assertEquals(
                1.0,
                resultado.getValor(),
                0.000001
        );
    }

    @Test
    void debeConservarUnaCantidadQueYaEstaEnMoles() {
        Sustancia agua = crearAgua();
        Cantidad cantidad = new Cantidad(2, UnidadCantidad.MOLES);

        Cantidad resultado = new ConversorQuimico().convertirAMoles(cantidad, agua);

        assertSame(cantidad, resultado);
    }

    @Test
    void debeConvertirMolesAGramos() {

        Sustancia agua = crearAgua();

        Cantidad resultado =
                new ConversorQuimico()
                        .convertirDesdeMoles(
                                new Cantidad(
                                        2,
                                        UnidadCantidad.MOLES
                                ),
                                agua,
                                UnidadCantidad.GRAMOS
                        );

        assertEquals(
                36.03,
                resultado.getValor(),
                0.000001
        );

        assertEquals(
                UnidadCantidad.GRAMOS,
                resultado.getUnidad()
        );
    }

    @Test
    void debeConvertirMolesAKilogramos() {

        Sustancia agua = crearAgua();

        Cantidad resultado =
                new ConversorQuimico()
                        .convertirDesdeMoles(
                                new Cantidad(
                                        2,
                                        UnidadCantidad.MOLES
                                ),
                                agua,
                                UnidadCantidad.KILOGRAMOS
                        );

        assertEquals(
                0.03603,
                resultado.getValor(),
                0.000001
        );
    }

    @Test
    void debeConservarMolesAlConvertirDesdeMolesAHaciaMoles() {
        Sustancia agua = crearAgua();
        Cantidad cantidad = new Cantidad(2, UnidadCantidad.MOLES);

        Cantidad resultado = new ConversorQuimico().convertirDesdeMoles(
                cantidad, agua, UnidadCantidad.MOLES);

        assertSame(cantidad, resultado);
    }

    @Test
    void noDebeConvertirVolumenAMoles() {

        Sustancia agua = crearAgua();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ConversorQuimico()
                                .convertirAMoles(
                                        new Cantidad(
                                                1,
                                                UnidadCantidad.LITROS
                                        ),
                                        agua
                                )
        );
    }

    @Test
    void noDebeConvertirMolesAVolumen() {

        Sustancia agua = crearAgua();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ConversorQuimico()
                                .convertirDesdeMoles(
                                        new Cantidad(
                                                1,
                                                UnidadCantidad.MOLES
                                        ),
                                        agua,
                                        UnidadCantidad.LITROS
                                )
        );
    }

    @Test
    void debeRechazarCantidadNull() {

        Sustancia agua = crearAgua();

        assertThrows(
                NullPointerException.class,
                () ->
                        new ConversorQuimico()
                                .convertirAMoles(
                                        null,
                                        agua
                                )
        );
    }

    @Test
    void debeRechazarSustanciaNull() {

        assertThrows(
                NullPointerException.class,
                () ->
                        new ConversorQuimico()
                                .convertirAMoles(
                                        new Cantidad(
                                                10,
                                                UnidadCantidad.GRAMOS
                                        ),
                                        null
                                )
        );
    }
}