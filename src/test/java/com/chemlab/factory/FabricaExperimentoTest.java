package com.chemlab.factory;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.enums.EstadoExperimento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FabricaExperimentoTest {

    private FabricaExperimento fabrica;

    @BeforeEach
    void setUp() {
        fabrica = new FabricaExperimento(CatalogoQuimico.predeterminado());
    }

    @Test
    void debeCrearExperimentoSintesisAguaConInventarioPrecargado() {
        Experimento exp = fabrica.crearExperimentoSintesisAgua();

        assertNotNull(exp);
        assertEquals("Síntesis de Agua", exp.getNombre());
        assertFalse(exp.getInventario().estaVacio(),
                "El inventario debe tener reactivos precargados");
        assertEquals(EstadoExperimento.NO_INICIADO, exp.getEstado());
    }

    @Test
    void debeCrearExperimentoCombustionCarbonoConInventarioPrecargado() {
        Experimento exp = fabrica.crearExperimentoCombustionCarbono();

        assertNotNull(exp);
        assertEquals("Combustión de Carbono", exp.getNombre());
        assertFalse(exp.getInventario().estaVacio(),
                "El inventario debe tener reactivos precargados");
        assertEquals(EstadoExperimento.NO_INICIADO, exp.getEstado());
    }

    @Test
    void debeCrearExperimentoSintesisAmoniacoConInventarioPrecargado() {
        Experimento exp = fabrica.crearExperimentoSintesisAmoniaco();

        assertNotNull(exp);
        assertEquals("Síntesis de Amoníaco (Haber-Bosch)", exp.getNombre());
        assertFalse(exp.getInventario().estaVacio(),
                "El inventario debe tener N2 y H2 precargados");
        assertEquals(EstadoExperimento.NO_INICIADO, exp.getEstado());
    }

    @Test
    void experimentoAmoniacoDebeApuntarAlNH3ComoObjetivo() {
        Experimento exp = fabrica.crearExperimentoSintesisAmoniaco();
        assertEquals(
                "NH3",
                exp.getObjetivo().getSustanciaEsperada().getId()
        );
    }

    @Test
    void debeCrearExperimentoSintesisNaClConInventarioPrecargado() {
        Experimento exp = fabrica.crearExperimentoSintesisNaCl();

        assertNotNull(exp);
        assertEquals("Síntesis de Cloruro de Sodio", exp.getNombre());
        assertFalse(exp.getInventario().estaVacio(),
                "El inventario debe tener Na y Cl2 precargados");
        assertEquals(EstadoExperimento.NO_INICIADO, exp.getEstado());
    }

    @Test
    void experimentoNaClDebeApuntarAlNaClComoObjetivo() {
        Experimento exp = fabrica.crearExperimentoSintesisNaCl();
        assertEquals(
                "NaCl",
                exp.getObjetivo().getSustanciaEsperada().getId()
        );
    }

    @Test
    void debeCrearExperimentoOxidacionHierroConInventarioPrecargado() {
        Experimento exp = fabrica.crearExperimentoOxidacionHierro();

        assertNotNull(exp);
        assertEquals("Oxidación del Hierro", exp.getNombre());
        assertFalse(exp.getInventario().estaVacio(),
                "El inventario debe tener Fe y O2 precargados");
        assertEquals(EstadoExperimento.NO_INICIADO, exp.getEstado());
    }

    @Test
    void experimentoHierroDebeApuntarAlFe2O3ComoObjetivo() {
        Experimento exp = fabrica.crearExperimentoOxidacionHierro();
        assertEquals(
                "Fe2O3",
                exp.getObjetivo().getSustanciaEsperada().getId()
        );
    }

    @Test
    void cadaExperimentoDebeGenerarIdUnico() {
        Experimento exp1 = fabrica.crearExperimentoSintesisAgua();
        Experimento exp2 = fabrica.crearExperimentoSintesisAgua();

        assertNotEquals(
                exp1.getId(),
                exp2.getId(),
                "Cada llamada debe generar un id único"
        );
    }

    @Test
    void experimentoSintesisDebeApuntarAlH2OComoObjetivo() {
        Experimento exp = fabrica.crearExperimentoSintesisAgua();
        assertEquals(
                "H2O",
                exp.getObjetivo().getSustanciaEsperada().getId()
        );
    }

    @Test
    void debeFallarSiElCatalogoEsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new FabricaExperimento(null)
        );
    }

    @Test
    void debeCrearExperimentoSintesisHClConInventarioPrecargado() {
        Experimento exp = fabrica.crearExperimentoSintesisHCl();

        assertNotNull(exp);
        assertEquals("Síntesis de Ácido Clorhídrico", exp.getNombre());
        assertFalse(exp.getInventario().estaVacio(),
                "El inventario debe tener H2 y Cl2 precargados");
        assertEquals(EstadoExperimento.NO_INICIADO, exp.getEstado());
    }

    @Test
    void experimentoHClDebeApuntarAlHClComoObjetivo() {
        Experimento exp = fabrica.crearExperimentoSintesisHCl();
        assertEquals(
                "HCl",
                exp.getObjetivo().getSustanciaEsperada().getId()
        );
    }

    @Test
    void experimentoHClDebeTenerCantidadMinimaDeHClDefinida() {
        Experimento exp = fabrica.crearExperimentoSintesisHCl();
        assertTrue(exp.getObjetivo().getCantidadMinima() > 0,
                "La cantidad mínima de HCl debe ser positiva");
    }
}
