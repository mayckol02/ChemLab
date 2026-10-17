package com.chemlab.catalogo;

import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoReaccionesTest {

    @Test
    void debeContenerLasReaccionesPredeterminadas() {
        CatalogoReacciones catalogo =
                crearCatalogoPredeterminado();

        assertEquals(2, catalogo.obtenerTodas().size());
        assertTrue(catalogo.contiene("R_H2O"));
        assertTrue(catalogo.contiene("R_CO2"));
    }

    @Test
    void debeBuscarLaReaccionDeFormacionDeAgua() {
        CatalogoSustancias sustancias =
                CatalogoSustancias.predeterminado();

        CatalogoReacciones catalogo =
                CatalogoReacciones.predeterminado(
                        sustancias
                );

        Reaccion reaccion = catalogo.buscarPorId("R_H2O")
                .orElseThrow();

        Sustancia hidrogeno = sustancias.buscarPorId("H2")
                .orElseThrow();

        Sustancia oxigeno = sustancias.buscarPorId("O2")
                .orElseThrow();

        assertEquals(
                2,
                reaccion.getReactivos().get(hidrogeno)
        );

        assertEquals(
                1,
                reaccion.getReactivos().get(oxigeno)
        );
    }

    @Test
    void debeDevolverVacioCuandoLaReaccionNoExiste() {
        CatalogoReacciones catalogo =
                crearCatalogoPredeterminado();

        assertTrue(
                catalogo.buscarPorId("R_INEXISTENTE")
                        .isEmpty()
        );
    }

    @Test
    void noDebePermitirReaccionesConElMismoId() {
        CatalogoSustancias sustancias =
                CatalogoSustancias.predeterminado();

        Reaccion primera = new Reaccion(
                "R_DUPLICADA",
                "Primera",
                Map.of(
                        sustancias.buscarPorId("H2")
                                .orElseThrow(),
                        2
                ),
                Map.of(
                        sustancias.buscarPorId("H2O")
                                .orElseThrow(),
                        2
                )
        );

        Reaccion segunda = new Reaccion(
                "R_DUPLICADA",
                "Segunda",
                Map.of(
                        sustancias.buscarPorId("C")
                                .orElseThrow(),
                        1
                ),
                Map.of(
                        sustancias.buscarPorId("CO2")
                                .orElseThrow(),
                        1
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new CatalogoReacciones(
                        List.of(primera, segunda)
                )
        );
    }

    @Test
    void noDebeAlterarElCatalogoCuandoSeModificaUnaCopia() {
        CatalogoReacciones catalogo =
                crearCatalogoPredeterminado();

        Reaccion reaccion = catalogo.buscarPorId("R_H2O")
                .orElseThrow();

        Sustancia hidrogeno = reaccion.getReactivos()
                .keySet()
                .stream()
                .filter(
                        sustancia -> sustancia.getId()
                                .equals("H2")
                )
                .findFirst()
                .orElseThrow();

        hidrogeno.cambiarEstado(
                EstadoMateria.LIQUIDO
        );

        Reaccion reaccionOriginal =
                catalogo.buscarPorId("R_H2O")
                        .orElseThrow();

        Sustancia hidrogenoOriginal =
                reaccionOriginal.getReactivos()
                        .keySet()
                        .stream()
                        .filter(
                                sustancia -> sustancia.getId()
                                        .equals("H2")
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                EstadoMateria.GASEOSO,
                hidrogenoOriginal.getEstadoMateria()
        );
    }

    private CatalogoReacciones crearCatalogoPredeterminado() {
        return CatalogoReacciones.predeterminado(
                CatalogoSustancias.predeterminado()
        );
    }
}