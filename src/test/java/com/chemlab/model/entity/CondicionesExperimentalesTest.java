package com.chemlab.model.entity;

import com.chemlab.model.enums.Atmosfera;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CondicionesExperimentalesTest {

    @Test
    void debeCrearCondicionesValidas() {

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25,
                        1,
                        7,
                        Atmosfera.AIRE
                );

        assertEquals(25, condiciones.getTemperatura());
        assertEquals(1, condiciones.getPresion());
        assertEquals(7, condiciones.getPh());
        assertEquals(
                Atmosfera.AIRE,
                condiciones.getAtmosfera()
        );
    }

    @Test
    void debePermitirCambiarTemperatura() {

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25,
                        1,
                        7,
                        Atmosfera.AIRE
                );

        condiciones.cambiarTemperatura(100);

        assertEquals(
                100,
                condiciones.getTemperatura()
        );
    }

    @Test
    void debePermitirCambiarPresion() {

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25,
                        1,
                        7,
                        Atmosfera.AIRE
                );

        condiciones.cambiarPresion(5);

        assertEquals(
                5,
                condiciones.getPresion()
        );
    }

    @Test
    void debePermitirCambiarPh() {

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25,
                        1,
                        7,
                        Atmosfera.AIRE
                );

        condiciones.cambiarPh(3);

        assertEquals(
                3,
                condiciones.getPh()
        );
    }

    @Test
    void debePermitirCambiarAtmosfera() {

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25,
                        1,
                        7,
                        Atmosfera.AIRE
                );

        condiciones.cambiarAtmosfera(
                Atmosfera.OXIGENO
        );

        assertEquals(
                Atmosfera.OXIGENO,
                condiciones.getAtmosfera()
        );
    }

    @Test
    void noDebePermitirPresionNegativa() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CondicionesExperimentales(
                        25,
                        -1,
                        7,
                        Atmosfera.AIRE
                )
        );
    }

    @Test
    void noDebePermitirPhFueraDeRango() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CondicionesExperimentales(
                        25,
                        1,
                        15,
                        Atmosfera.AIRE
                )
        );
    }

    @Test
    void noDebePermitirPhNegativo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CondicionesExperimentales(
                        25,
                        1,
                        -1,
                        Atmosfera.AIRE
                )
        );
    }
}