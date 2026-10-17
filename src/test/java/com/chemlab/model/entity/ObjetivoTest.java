package com.chemlab.model.entity;

import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjetivoTest {

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
    void debeCrearUnObjetivoValido() {

        Sustancia agua = crearAgua();

        Objetivo objetivo = new Objetivo(
                "OBJ_001",
                "Obtener agua",
                agua,
                1
        );

        assertEquals("OBJ_001", objetivo.getId());
        assertEquals(
                "Obtener agua",
                objetivo.getDescripcion()
        );
        assertEquals(
                agua,
                objetivo.getSustanciaEsperada()
        );
        assertEquals(
                1,
                objetivo.getCantidadMinima()
        );
    }

    @Test
    void noDebePermitirCantidadMinimaCero() {

        Sustancia agua = crearAgua();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Objetivo(
                        "OBJ_002",
                        "Objetivo inválido",
                        agua,
                        0
                )
        );
    }

    @Test
    void objetivosConElMismoIdDebenSerIguales() {

        Sustancia agua = crearAgua();

        Objetivo primero = new Objetivo(
                "OBJ_001",
                "Obtener agua",
                agua,
                1
        );

        Objetivo segundo = new Objetivo(
                "OBJ_001",
                "Obtener agua",
                agua,
                1
        );

        assertEquals(primero, segundo);
        assertEquals(
                primero.hashCode(),
                segundo.hashCode()
        );
    }
}