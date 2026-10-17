package com.chemlab.service;

import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConversorUnidadesTest {

    private final ConversorUnidades conversor =
            new ConversorUnidades();

    @Test
    void debeConvertirKilogramosAGramos() {

        Cantidad resultado = conversor.convertir(
                new Cantidad(
                        2,
                        UnidadCantidad.KILOGRAMOS
                ),
                UnidadCantidad.GRAMOS
        );

        assertEquals(
                2000,
                resultado.getValor()
        );

        assertEquals(
                UnidadCantidad.GRAMOS,
                resultado.getUnidad()
        );
    }

    @Test
    void debeConvertirGramosAMiligramos() {

        Cantidad resultado = conversor.convertir(
                new Cantidad(
                        5,
                        UnidadCantidad.GRAMOS
                ),
                UnidadCantidad.MILIGRAMOS
        );

        assertEquals(
                5000,
                resultado.getValor()
        );
    }

    @Test
    void debeConvertirLitrosAMililitros() {

        Cantidad resultado = conversor.convertir(
                new Cantidad(
                        3,
                        UnidadCantidad.LITROS
                ),
                UnidadCantidad.MILILITROS
        );

        assertEquals(
                3000,
                resultado.getValor()
        );
    }

    @Test
    void debeConvertirMililitrosALitros() {

        Cantidad resultado = conversor.convertir(
                new Cantidad(
                        750,
                        UnidadCantidad.MILILITROS
                ),
                UnidadCantidad.LITROS
        );

        assertEquals(
                0.75,
                resultado.getValor(),
                0.000001
        );
    }

    @Test
    void convertirAMismaUnidadDebeConservarElValor() {

        Cantidad cantidad = new Cantidad(
                25,
                UnidadCantidad.GRAMOS
        );

        Cantidad resultado = conversor.convertir(
                cantidad,
                UnidadCantidad.GRAMOS
        );

        assertEquals(
                cantidad,
                resultado
        );
    }

    @Test
    void noDebeConvertirMasaAVolumen() {

        assertThrows(
                IllegalArgumentException.class,
                () -> conversor.convertir(
                        new Cantidad(
                                10,
                                UnidadCantidad.GRAMOS
                        ),
                        UnidadCantidad.LITROS
                )
        );
    }

    @Test
    void noDebeConvertirMolesAGramos() {

        assertThrows(
                IllegalArgumentException.class,
                () -> conversor.convertir(
                        new Cantidad(
                                2,
                                UnidadCantidad.MOLES
                        ),
                        UnidadCantidad.GRAMOS
                )
        );
    }
}