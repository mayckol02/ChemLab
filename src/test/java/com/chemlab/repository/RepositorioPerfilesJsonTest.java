package com.chemlab.repository;

import com.chemlab.game.PerfilJugador;
import com.chemlab.game.ProgresoPerfil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositorioPerfilesJsonTest {

    @TempDir
    Path directorioTemporal;

    @Test
    void creaListaYGuardaProgresoPorPerfil() {
        RepositorioPerfilesJson repositorio =
                new RepositorioPerfilesJson(directorioTemporal.resolve("profiles"));
        PerfilJugador ana = repositorio.crear("Ana");
        PerfilJugador luis = repositorio.crear("Luis");
        ProgresoPerfil progreso = new ProgresoPerfil(
                2,
                true,
                Set.of("H2", "O2", "H2O"),
                Set.of("R_H2O"),
                java.util.List.of()
        );

        repositorio.guardar(ana.conProgreso(progreso));

        PerfilJugador anaCargada = repositorio.listar().stream()
                .filter(perfil -> perfil.id().equals(ana.id()))
                .findFirst()
                .orElseThrow();
        PerfilJugador luisCargado = repositorio.listar().stream()
                .filter(perfil -> perfil.id().equals(luis.id()))
                .findFirst()
                .orElseThrow();

        assertEquals(progreso, anaCargada.progreso());
        assertEquals(ProgresoPerfil.inicial(), luisCargado.progreso());
        assertNotEquals(ana.id(), luis.id());
    }

    @Test
    void rechazaNombresVaciosYDuplicadosSinDistinguirMayusculas() {
        RepositorioPerfilesJson repositorio =
                new RepositorioPerfilesJson(directorioTemporal.resolve("profiles"));
        repositorio.crear("Ana");

        assertThrows(IllegalArgumentException.class, () -> repositorio.crear("  "));
        assertThrows(IllegalArgumentException.class, () -> repositorio.crear("aNA"));
        assertThrows(IllegalArgumentException.class,
                () -> repositorio.crear("Este nombre supera el maximo permitido"));
    }

    @Test
    void informaLosArchivosDePerfilCorruptos() throws Exception {
        Path directorio = directorioTemporal.resolve("profiles");
        Files.createDirectories(directorio);
        Files.writeString(directorio.resolve("perfil-corrupto.json"), "{ no es json }");
        RepositorioPerfilesJson repositorio = new RepositorioPerfilesJson(directorio);

        IllegalStateException error = assertThrows(IllegalStateException.class, repositorio::listar);
        assertTrue(error.getMessage().contains("perfil-corrupto.json"));
    }
}
