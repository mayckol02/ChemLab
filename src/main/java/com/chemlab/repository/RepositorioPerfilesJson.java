package com.chemlab.repository;

import com.chemlab.game.PerfilJugador;
import com.chemlab.game.ProgresoPerfil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Persistencia local de perfiles en archivos JSON independientes.
 */
public final class RepositorioPerfilesJson {

    private static final int LONGITUD_MAXIMA_NOMBRE = 24;

    private final Path directorio;
    private final ObjectMapper objectMapper;

    public RepositorioPerfilesJson() {
        this(Path.of(System.getProperty("user.home"), ".chemlab", "profiles"));
    }

    public RepositorioPerfilesJson(Path directorio) {
        this.directorio = Objects.requireNonNull(directorio, "El directorio no puede ser null");
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public List<PerfilJugador> listar() {
        try {
            if (Files.notExists(directorio)) {
                return List.of();
            }
            try (var archivos = Files.list(directorio)) {
                return archivos
                        .filter(Files::isRegularFile)
                        .filter(archivo -> archivo.getFileName().toString().endsWith(".json"))
                        .map(this::leerPerfil)
                        .sorted(Comparator.comparing(PerfilJugador::nombre, String.CASE_INSENSITIVE_ORDER))
                        .toList();
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron leer los perfiles de " + directorio, e);
        }
    }

    public PerfilJugador crear(String nombre) {
        String nombreValidado = validarNombre(nombre);
        boolean nombreEnUso = listar().stream()
                .anyMatch(perfil -> perfil.nombre().toLowerCase(Locale.ROOT)
                        .equals(nombreValidado.toLowerCase(Locale.ROOT)));
        if (nombreEnUso) {
            throw new IllegalArgumentException("Ya existe un perfil con ese nombre.");
        }
        PerfilJugador perfil = new PerfilJugador(
                UUID.randomUUID().toString(),
                nombreValidado,
                ProgresoPerfil.inicial()
        );
        guardar(perfil);
        return perfil;
    }

    public void guardar(PerfilJugador perfil) {
        Objects.requireNonNull(perfil, "El perfil no puede ser null");
        Path destino = rutaPerfil(perfil.id());
        Path temporal = null;
        try {
            Files.createDirectories(directorio);
            temporal = Files.createTempFile(directorio, perfil.id() + "-", ".tmp");
            objectMapper.writeValue(temporal.toFile(), perfil);
            try {
                Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el perfil \"" + perfil.nombre()
                    + "\" en " + destino, e);
        } finally {
            if (temporal != null) {
                try {
                    Files.deleteIfExists(temporal);
                } catch (IOException e) {
                    throw new IllegalStateException("No se pudo limpiar el archivo temporal del perfil "
                            + perfil.nombre(), e);
                }
            }
        }
    }

    private PerfilJugador leerPerfil(Path archivo) {
        try {
            PerfilJugador perfil = objectMapper.readValue(archivo.toFile(), PerfilJugador.class);
            if (!archivo.equals(rutaPerfil(perfil.id()))) {
                throw new IllegalStateException("El identificador del perfil no coincide con su archivo: "
                        + archivo);
            }
            return perfil;
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            throw new IllegalStateException("No se pudo leer el perfil guardado " + archivo, e);
        }
    }

    private Path rutaPerfil(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El identificador del perfil no es válido.", e);
        }
        return directorio.resolve(uuid + ".json");
    }

    private static String validarNombre(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("Escribe un nombre para el perfil.");
        }
        String nombreLimpio = nombre.trim();
        if (nombreLimpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre del perfil no puede estar vacío.");
        }
        if (nombreLimpio.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new IllegalArgumentException("El nombre del perfil no puede superar "
                    + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
        }
        if (nombreLimpio.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("El nombre del perfil contiene caracteres no permitidos.");
        }
        return nombreLimpio;
    }
}
