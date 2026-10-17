package com.chemlab.catalogo;

import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoSustanciasTest {

    @Test
    void debeContenerLasSustanciasPredeterminadas() {
        CatalogoSustancias catalogo =
                CatalogoSustancias.predeterminado();

        assertEquals(6, catalogo.obtenerTodas().size());
        assertTrue(catalogo.contiene("H2"));
        assertTrue(catalogo.contiene("O2"));
        assertTrue(catalogo.contiene("H2O"));
        assertTrue(catalogo.contiene("CO2"));
    }

    @Test
    void debeBuscarUnaSustanciaPorId() {
        CatalogoSustancias catalogo =
                CatalogoSustancias.predeterminado();

        Sustancia agua = catalogo.buscarPorId("H2O")
                .orElseThrow();

        assertEquals("Agua", agua.getNombre());
        assertEquals("H₂O", agua.getFormula());
        assertEquals(18.015, agua.getMasaMolar());
    }

    @Test
    void debeDevolverVacioCuandoLaSustanciaNoExiste() {
        CatalogoSustancias catalogo =
                CatalogoSustancias.predeterminado();

        assertTrue(
                catalogo.buscarPorId("SUSTANCIA_INEXISTENTE")
                        .isEmpty()
        );
    }

    @Test
    void noDebePermitirIdsDuplicados() {
        Sustancia primera = new Sustancia(
                "H2",
                "Hidrógeno",
                "H₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );

        Sustancia segunda = new Sustancia(
                "H2",
                "Hidrógeno alternativo",
                "H₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new CatalogoSustancias(
                        List.of(primera, segunda)
                )
        );
    }

    @Test
    void noDebeAlterarElCatalogoCuandoSeModificaUnaCopia() {
        CatalogoSustancias catalogo =
                CatalogoSustancias.predeterminado();

        Sustancia agua = catalogo.buscarPorId("H2O")
                .orElseThrow();

        agua.cambiarEstado(EstadoMateria.GASEOSO);

        Sustancia aguaDelCatalogo =
                catalogo.buscarPorId("H2O")
                        .orElseThrow();

        assertEquals(
                EstadoMateria.LIQUIDO,
                aguaDelCatalogo.getEstadoMateria()
        );
    }
}