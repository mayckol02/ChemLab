package com.chemlab.grammar.formula;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NomenclaturaQuimicaTest {

    private final AnalizadorFormulaQuimica analizador = new AnalizadorFormulaQuimica();
    private final NomenclaturaQuimica nomenclatura = new NomenclaturaQuimica();

    @Test
    void oxidoDeHierroExplicaLaReglaStockYElBalanceDeCargas() {
        ResultadoAnalisisFormula analisis = analizador.analizar("Fe₂O₃");

        ResultadoNombreQuimico nombre = nomenclatura.nombrar(analisis, "Óxido de hierro(III)");

        assertEquals("Óxido de hierro (III)", nombre.nombre());
        assertTrue(nombre.reglaAplicada().contains("Stock"));
        assertTrue(nombre.pasos().stream().anyMatch(paso -> paso.contains("+3")));
        assertTrue(nombre.pasos().stream().anyMatch(paso -> paso.contains("-6")));
    }

    @Test
    void elementoCompuestoYGrupoAnidadoConservanNombresYCantidades() {
        ResultadoAnalisisFormula analisis = analizador.analizar("Ca₃(PO₄)₂");

        assertEquals("calcio", nomenclatura.nombreElemento("Ca").orElseThrow());
        assertEquals("fósforo", nomenclatura.nombreElemento("P").orElseThrow());
        assertEquals("oxígeno", nomenclatura.nombreElemento("O").orElseThrow());
        assertEquals(3, analisis.atomos().get("Ca"));
        assertEquals(2, analisis.atomos().get("P"));
        assertEquals(8, analisis.atomos().get("O"));
    }

    @Test
    void conservaElNombreTradicionalDelAguaDelCatalogo() {
        ResultadoNombreQuimico nombre = nomenclatura.nombrar(analizador.analizar("H₂O"), "Agua");

        assertEquals("Agua", nombre.nombre());
        assertTrue(nombre.pasos().stream().anyMatch(paso -> paso.contains("catálogo")));
    }

    @Test
    void construyeElNombreDeUnOxidoMolecularNoRegistrado() {
        ResultadoNombreQuimico nombre = nomenclatura.nombrar(analizador.analizar("NO₂"), null);

        assertEquals("dióxido de nitrógeno", nombre.nombre());
        assertTrue(nombre.reglaAplicada().contains("óxidos moleculares"));
    }

    @Test
    void noInventaNomenclaturaParaUnaFamiliaNoImplementada() {
        ResultadoNombreQuimico nombre = nomenclatura.nombrar(analizador.analizar("NaCl"), null);

        assertEquals("Compuesto sin nombre registrado", nombre.nombre());
        assertTrue(nombre.pasos().getLast().contains("evita inventar"));
    }

    @Test
    void separaCoeficienteEstequiometricoDeLaFormulaYConservaElNombreDelHidroxido() {
        ResultadoAnalisisFormula analisis = analizador.analizar("4Fe(OH)₃");

        assertEquals(4, analisis.coeficiente());
        assertEquals("Fe(OH)3", analisis.formulaNormalizada());
        assertEquals(1, analisis.atomos().get("Fe"));
        assertEquals(3, analisis.atomos().get("O"));
        assertEquals(3, analisis.atomos().get("H"));
        ResultadoNombreQuimico nombre = nomenclatura.nombrar(analisis, "Herrumbre / Hidróxido de hierro");
        assertEquals("Hidróxido de hierro (III)", nombre.nombre());
        assertTrue(nombre.pasos().stream().anyMatch(paso -> paso.contains("carga total −3")));
    }

    @Test
    void aceptaEtiquetasDeFaseYNormalizaLaFormulaParaElCatalogo() {
        ResultadoAnalisisFormula analisis = analizador.analizar("NaCl (ac)");

        assertEquals("NaCl", analisis.formulaNormalizada());
        assertEquals("(ac)", analisis.fase());
        assertEquals(1, analisis.coeficiente());
        assertEquals(1, analisis.atomos().get("Na"));
        assertEquals(1, analisis.atomos().get("Cl"));
    }
}
