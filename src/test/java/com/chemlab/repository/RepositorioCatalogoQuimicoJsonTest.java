package com.chemlab.repository;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioCatalogoQuimicoJsonTest {

    @Test
    void debeCargarElCatalogoDesdeJson() {
        RepositorioCatalogoQuimico repositorio =
                new RepositorioCatalogoQuimicoJson();

        CatalogoQuimico catalogo = repositorio.cargar();

        assertEquals(33, catalogo.obtenerSustancias().size());
        assertEquals(19, catalogo.obtenerReacciones().size());

        Sustancia agua = catalogo.buscarSustancia("H2O")
                .orElseThrow();

        Reaccion formacionAgua =
                catalogo.buscarReaccion("R_H2O")
                        .orElseThrow();

        assertEquals("Agua", agua.getNombre());
        assertEquals(
                2,
                formacionAgua.getProductos().get(agua)
        );
    }

    @Test
    void debeCrearUnCatalogoNuevoEnCadaCarga() {
        RepositorioCatalogoQuimico repositorio =
                new RepositorioCatalogoQuimicoJson();

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

    @Test
    void debeFallarCuandoNoExisteElRecurso() {
        RepositorioCatalogoQuimico repositorio =
                new RepositorioCatalogoQuimicoJson(
                        "catalogo-inexistente.json"
                );

        assertThrows(
                IllegalStateException.class,
                repositorio::cargar
        );
    }

    @Test
    void debeFallarCuandoLaReaccionReferenciaSustanciaInexistente() {
        RepositorioCatalogoQuimico repositorio =
                new RepositorioCatalogoQuimicoJson(
                        "catalogo-quimico-invalido.json"
                );

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                repositorio::cargar
        );

        assertTrue(
                error.getMessage().contains("X")
        );
    }
}