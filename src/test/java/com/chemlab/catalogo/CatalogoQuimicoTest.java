package com.chemlab.catalogo;

import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoQuimicoTest {

    private CatalogoQuimico catalogo;

    @BeforeEach
    void setUp() {
        catalogo = CatalogoQuimico.predeterminado();
    }

    @Test
    void debeCrearElCatalogoPredeterminado() {
        // Universo químico completo
        assertEquals(36, catalogo.obtenerSustancias().size());
        assertEquals(25, catalogo.obtenerReacciones().size());

        assertTrue(catalogo.buscarSustancia("H2O").isPresent());
        assertTrue(catalogo.buscarReaccion("R_H2O").isPresent());
    }

    // --- Sustancias nuevas ---

    @Test
    void debeContenerNitrogeno() {
        var n2 = catalogo.buscarSustancia("N2");
        assertTrue(n2.isPresent(), "El catálogo debe contener N2");
        assertEquals("Nitrógeno", n2.get().getNombre());
        assertEquals(EstadoMateria.GASEOSO, n2.get().getEstadoMateria());
    }

    @Test
    void debeContenerAmoniaco() {
        var nh3 = catalogo.buscarSustancia("NH3");
        assertTrue(nh3.isPresent(), "El catálogo debe contener NH3");
        assertEquals("Amoníaco", nh3.get().getNombre());
        assertEquals(TipoSustancia.COMPUESTO, nh3.get().getTipo());
    }

    @Test
    void debeContenerSodioYCloro() {
        assertTrue(catalogo.buscarSustancia("Na").isPresent(), "Debe contener Na");
        assertTrue(catalogo.buscarSustancia("Cl2").isPresent(), "Debe contener Cl2");
    }

    @Test
    void debeContenerNaCl() {
        var nacl = catalogo.buscarSustancia("NaCl");
        assertTrue(nacl.isPresent(), "El catálogo debe contener NaCl");
        assertEquals("Cloruro de sodio", nacl.get().getNombre());
        assertEquals(EstadoMateria.SOLIDO, nacl.get().getEstadoMateria());
    }

    @Test
    void debeContenerHCl() {
        var hcl = catalogo.buscarSustancia("HCl");
        assertTrue(hcl.isPresent(), "El catálogo debe contener HCl");
        assertEquals(TipoSustancia.COMPUESTO, hcl.get().getTipo());
    }

    @Test
    void debeContenerFe2O3() {
        var fe2o3 = catalogo.buscarSustancia("Fe2O3");
        assertTrue(fe2o3.isPresent(), "El catálogo debe contener Fe2O3");
        assertEquals("Óxido de hierro(III)", fe2o3.get().getNombre());
        assertEquals(EstadoMateria.SOLIDO, fe2o3.get().getEstadoMateria());
    }

    // --- Reacciones nuevas ---

    @Test
    void debeContenerReaccionHaberBosch() {
        var rxn = catalogo.buscarReaccion("R_NH3");
        assertTrue(rxn.isPresent(), "Debe existir la reacción R_NH3");
        assertEquals("Síntesis de amoníaco (Haber-Bosch)", rxn.get().getNombre());
        assertTrue(rxn.get().getReactivos().containsKey(
                catalogo.buscarSustancia("N2").orElseThrow()
        ));
    }

    @Test
    void debeContenerReaccionNaCl() {
        assertTrue(catalogo.buscarReaccion("R_NaCl").isPresent());
    }

    @Test
    void debeContenerReaccionHCl() {
        assertTrue(catalogo.buscarReaccion("R_HCl").isPresent());
    }

    @Test
    void debeContenerReaccionFe2O3() {
        assertTrue(catalogo.buscarReaccion("R_Fe2O3").isPresent());
    }

    // --- Casos originales ---

    @Test
    void debeMantenerAisladasLasCopiasDeSustancias() {
        Sustancia agua = catalogo.buscarSustancia("H2O").orElseThrow();
        agua.cambiarEstado(EstadoMateria.GASEOSO);

        Sustancia aguaOriginal = catalogo.buscarSustancia("H2O").orElseThrow();
        assertEquals(EstadoMateria.LIQUIDO, aguaOriginal.getEstadoMateria());
    }

    @Test
    void noDebePermitirReaccionConSustanciaNoRegistrada() {
        Sustancia hidrogeno = new Sustancia(
                "H2",
                "Hidrógeno",
                "H₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );

        Sustancia sustanciaNoRegistrada = new Sustancia(
                "X",
                "Sustancia desconocida",
                "X",
                TipoSustancia.COMPUESTO,
                EstadoMateria.SOLIDO,
                10.0
        );

        CatalogoSustancias sustancias = new CatalogoSustancias(List.of(hidrogeno));
        CatalogoReacciones reacciones = new CatalogoReacciones(
                List.of(new Reaccion(
                        "R_X",
                        "Reacción inválida",
                        Map.of(hidrogeno, 1),
                        Map.of(sustanciaNoRegistrada, 1)
                ))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new CatalogoQuimico(sustancias, reacciones)
        );
    }

    @Test
    void debeDevolverVacioParaIdsInexistentes() {
        assertTrue(catalogo.buscarSustancia("X").isEmpty());
        assertTrue(catalogo.buscarReaccion("R_X").isEmpty());
    }
}