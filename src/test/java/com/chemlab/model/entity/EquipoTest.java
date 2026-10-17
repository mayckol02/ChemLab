package com.chemlab.model.entity;

import com.chemlab.model.enums.TipoEquipo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EquipoTest {

    @Test
    void debeCrearUnEquipoValido() {

        Equipo vaso = new Equipo(
                "VASO_001",
                "Vaso de precipitados",
                TipoEquipo.RECIPIENTE,
                500
        );

        assertEquals("VASO_001", vaso.getId());
        assertEquals("Vaso de precipitados", vaso.getNombre());
        assertEquals(TipoEquipo.RECIPIENTE, vaso.getTipo());
        assertEquals(500, vaso.getCapacidad());
    }

    @Test
    void noDebePermitirCapacidadNegativa() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Equipo(
                        "VASO_002",
                        "Vaso inválido",
                        TipoEquipo.RECIPIENTE,
                        -100
                )
        );
    }

    @Test
    void noDebePermitirCapacidadCero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Equipo(
                        "VASO_003",
                        "Vaso inválido",
                        TipoEquipo.RECIPIENTE,
                        0
                )
        );
    }

    @Test
    void equiposConElMismoIdDebenSerIguales() {

        Equipo primero = new Equipo(
                "MECHERO_001",
                "Mechero",
                TipoEquipo.MECHERO,
                100
        );

        Equipo segundo = new Equipo(
                "MECHERO_001",
                "Mechero",
                TipoEquipo.MECHERO,
                100
        );

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }
}