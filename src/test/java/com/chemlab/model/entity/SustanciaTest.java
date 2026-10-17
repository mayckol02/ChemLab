package com.chemlab.model.entity;

import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SustanciaTest {

    @Test
    void debeCrearUnaSustanciaValida() {

        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H₂O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        assertEquals("H2O", agua.getId());
        assertEquals("Agua", agua.getNombre());
        assertEquals("H₂O", agua.getFormula());
        assertEquals(TipoSustancia.COMPUESTO, agua.getTipo());
        assertEquals(EstadoMateria.LIQUIDO, agua.getEstadoMateria());
        assertEquals(18.015, agua.getMasaMolar());
    }

    @Test
    void debePermitirCambiarElEstadoDeLaSustancia() {

        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H₂O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        agua.cambiarEstado(EstadoMateria.GASEOSO);

        assertEquals(
                EstadoMateria.GASEOSO,
                agua.getEstadoMateria()
        );
    }

    @Test
    void noDebePermitirMasaMolarNegativa() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Sustancia(
                        "X",
                        "Sustancia inválida",
                        "X",
                        TipoSustancia.ELEMENTO,
                        EstadoMateria.SOLIDO,
                        -1
                )
        );
    }

    @Test
    void sustanciasConElMismoIdDebenSerIguales() {

        Sustancia primera = new Sustancia(
                "H2O",
                "Agua",
                "H₂O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        Sustancia segunda = new Sustancia(
                "H2O",
                "Agua",
                "H₂O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }
}