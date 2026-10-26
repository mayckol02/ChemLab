package com.chemlab.grammar.formula;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TablaPeriodicaTest {

    @Test
    void conservaLos118ElementosReconocidosPorElAnalizador() {
        assertEquals(118, TablaPeriodica.todos().size());
        assertTrue(TablaPeriodica.contiene("H"));
        assertTrue(TablaPeriodica.contiene("Fe"));
        assertTrue(TablaPeriodica.contiene("Og"));
        assertFalse(TablaPeriodica.contiene("Xx"));
    }

    @Test
    void centralizaNombresClasificacionYNumerosDeOxidacionExistentes() {
        TablaPeriodica.ElementoQuimico hierro = TablaPeriodica.buscar("Fe").orElseThrow();
        TablaPeriodica.ElementoQuimico oxigeno = TablaPeriodica.buscar("O").orElseThrow();
        TablaPeriodica.ElementoQuimico manganeso = TablaPeriodica.buscar("Mn").orElseThrow();

        assertEquals("hierro", hierro.nombre());
        assertEquals(Set.of(2, 3), hierro.numerosOxidacion());
        assertTrue(oxigeno.noMetal());
        assertEquals(Set.of(2, 4, 7), manganeso.numerosOxidacion());
    }

    @Test
    void tablaYDatosDeCadaElementoSonInmutables() {
        assertThrows(UnsupportedOperationException.class, () ->
                TablaPeriodica.todos().add(TablaPeriodica.buscar("H").orElseThrow()));
        assertThrows(UnsupportedOperationException.class, () ->
                TablaPeriodica.buscar("Fe").orElseThrow().numerosOxidacion().add(4));
    }

    @Test
    void parserYNomenclaturaConsumenElMismoRegistroDeElementos() {
        ResultadoAnalisisFormula analisis = new AnalizadorFormulaQuimica().analizar("Fe₂O₃");
        NomenclaturaQuimica nomenclatura = new NomenclaturaQuimica();

        assertEquals(List.of("hierro", "oxígeno"),
                analisis.atomos().keySet().stream()
                        .map(simbolo -> nomenclatura.nombreElemento(simbolo).orElseThrow())
                        .toList());
        assertEquals("Óxido de hierro (III)", nomenclatura.nombrar(analisis, null).nombre());
    }
}
