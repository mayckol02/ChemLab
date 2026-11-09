package com.chemlab.factory;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.EstadoExperimento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FabricaExperimentoTest {

    private CatalogoQuimico catalogo;
    private FabricaExperimento fabrica;

    @BeforeEach
    void setUp() {
        catalogo = CatalogoQuimico.predeterminado();
        fabrica = new FabricaExperimento(catalogo);
    }

    @Test
    void cadaReaccionDelCatalogoCreaExperimentoDesdeSuDefinicionJson() {
        for (var reaccion : catalogo.obtenerReacciones()) {
            Experimento experimento = fabrica.crearExperimentoParaReaccion(reaccion);

            assertNotNull(experimento);
            assertEquals(reaccion.getNombre(), experimento.getNombre());
            assertFalse(experimento.getInventario().estaVacio(), reaccion.getId());
            assertNotNull(experimento.getEquipo(), reaccion.getId());
            assertNotNull(experimento.getCondiciones(), reaccion.getId());
            assertTrue(reaccion.getProductos().containsKey(experimento.getObjetivo().getSustanciaEsperada()),
                    reaccion.getId());
            assertEquals(EstadoExperimento.NO_INICIADO, experimento.getEstado());
        }
    }

    @Test
    void objetivosDePracticasRepresentativasSalenDeJson() {
        assertEquals("H2O", crear("R_H2O").getObjetivo().getSustanciaEsperada().getId());
        assertEquals("CO2", crear("R_CO2").getObjetivo().getSustanciaEsperada().getId());
        assertEquals("NH3", crear("R_NH3").getObjetivo().getSustanciaEsperada().getId());
        assertEquals("NaCl", crear("R_NaCl").getObjetivo().getSustanciaEsperada().getId());
        assertEquals("Fe2O3", crear("R_Fe2O3").getObjetivo().getSustanciaEsperada().getId());
        assertEquals("HCl", crear("R_HCl").getObjetivo().getSustanciaEsperada().getId());
    }

    @Test
    void idsDeExperimentosSonUnicos() {
        assertNotEquals(crear("R_H2O").getId(), crear("R_H2O").getId());
    }

    @Test
    void practicaMgOConfiguraObjetivoEquipoCondicionesYMaterialesDesdeJson() {
        Experimento experimento = crear("R_MgO");
        var magnesio = catalogo.buscarSustancia("Mg").orElseThrow();

        assertEquals("MgO", experimento.getObjetivo().getSustanciaEsperada().getId());
        assertEquals(64.4864, experimento.getObjetivo().getCantidadMinima(), 0.0001);
        assertEquals("Cámara de Combustión Sellada", experimento.getEquipo().getNombre());
        assertEquals(400, experimento.getCondiciones().getTemperatura());
        assertEquals(Atmosfera.OXIGENO, experimento.getCondiciones().getAtmosfera());
        assertEquals(48.61, experimento.getInventario().obtenerCantidad(magnesio), 0.0001);
    }

    @Test
    void modoDificilConservaEquipoObjetivoYCondicionesPeroIniciaSinInventario() {
        var reaccion = catalogo.buscarReaccion("R_NH3").orElseThrow();
        Experimento experimento = fabrica.crearExperimentoVacioParaReaccion(reaccion);

        assertTrue(experimento.getInventario().estaVacio());
        assertEquals("NH3", experimento.getObjetivo().getSustanciaEsperada().getId());
        assertEquals("Reactor de alta presión", experimento.getEquipo().getNombre());
        assertEquals(25, experimento.getCondiciones().getTemperatura());
        assertEquals(Atmosfera.AIRE, experimento.getCondiciones().getAtmosfera());
    }

    @Test
    void debeFallarSiElCatalogoEsNull() {
        assertThrows(NullPointerException.class, () -> new FabricaExperimento(null));
    }

    private Experimento crear(String idReaccion) {
        return fabrica.crearExperimentoParaReaccion(
                catalogo.buscarReaccion(idReaccion).orElseThrow());
    }
}
