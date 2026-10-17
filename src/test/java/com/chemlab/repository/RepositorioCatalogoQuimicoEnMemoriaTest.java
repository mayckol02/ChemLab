package com.chemlab.repository;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioCatalogoQuimicoEnMemoriaTest {

    @Test
    void debeCargarElCatalogoPredeterminado() {
        RepositorioCatalogoQuimico repositorio =
                new RepositorioCatalogoQuimicoEnMemoria();

        CatalogoQuimico catalogo = repositorio.cargar();

        assertTrue(
                catalogo.buscarSustancia("H2O").isPresent()
        );

        assertTrue(
                catalogo.buscarReaccion("R_H2O").isPresent()
        );
    }

    @Test
    void debeDevolverElCatalogoInyectado() {
        CatalogoQuimico catalogoEsperado =
                CatalogoQuimico.predeterminado();

        RepositorioCatalogoQuimico repositorio =
                new RepositorioCatalogoQuimicoEnMemoria(
                        catalogoEsperado
                );

        CatalogoQuimico catalogoObtenido =
                repositorio.cargar();

        assertTrue(
                catalogoObtenido.buscarSustancia("CO2")
                        .isPresent()
        );

        assertTrue(
                catalogoObtenido.buscarReaccion("R_CO2")
                        .isPresent()
        );
    }

    @Test
    void noDebeAlterarseElCatalogoEntreCargas() {
        RepositorioCatalogoQuimico repositorio =
                new RepositorioCatalogoQuimicoEnMemoria();

        Sustancia agua = repositorio.cargar()
                .buscarSustancia("H2O")
                .orElseThrow();

        agua.cambiarEstado(EstadoMateria.GASEOSO);

        Sustancia aguaRecargada = repositorio.cargar()
                .buscarSustancia("H2O")
                .orElseThrow();

        assertEquals(
                EstadoMateria.LIQUIDO,
                aguaRecargada.getEstadoMateria()
        );
    }
}