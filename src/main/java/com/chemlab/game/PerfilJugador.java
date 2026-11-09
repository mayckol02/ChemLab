package com.chemlab.game;

import java.util.Objects;
import java.util.UUID;

/**
 * Perfil local del jugador y su progreso asociado.
 */
public record PerfilJugador(String id, String nombre, ProgresoPerfil progreso) {

    public PerfilJugador {
        Objects.requireNonNull(id, "El id del perfil no puede ser null");
        Objects.requireNonNull(nombre, "El nombre del perfil no puede ser null");
        Objects.requireNonNull(progreso, "El progreso del perfil no puede ser null");
        UUID.fromString(id);
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del perfil no puede estar vacío.");
        }
    }

    public PerfilJugador conProgreso(ProgresoPerfil nuevoProgreso) {
        return new PerfilJugador(id, nombre, nuevoProgreso);
    }
}
