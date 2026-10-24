package com.chemlab.game.world;

import com.chemlab.model.enums.TipoEquipo;

import java.util.Objects;
import java.util.Set;

/**
 * Puesto físico del mapa y reacciones que pueden ejecutarse en su equipo.
 * Las coordenadas pertenecen al mundo 2D; no contienen reglas químicas.
 */
public record EstacionLaboratorio(
        String id,
        String nombre,
        String descripcion,
        TipoEquipo tipoEquipo,
        double x,
        double y,
        double ancho,
        double alto,
        Set<String> reacciones
) {
    public EstacionLaboratorio {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la estación no puede estar vacío");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la estación no puede estar vacío");
        }
        Objects.requireNonNull(descripcion, "La descripción no puede ser null");
        Objects.requireNonNull(tipoEquipo, "El tipo de equipo no puede ser null");
        if (!Double.isFinite(x) || !Double.isFinite(y)
                || !Double.isFinite(ancho) || !Double.isFinite(alto)
                || ancho <= 0 || alto <= 0) {
            throw new IllegalArgumentException("El tamaño de la estación debe ser positivo");
        }
        reacciones = Set.copyOf(Objects.requireNonNull(reacciones, "Las reacciones no pueden ser null"));
    }

    public boolean admite(String idReaccion) {
        return idReaccion != null && reacciones.contains(idReaccion);
    }

    public double distanciaHasta(double puntoX, double puntoY) {
        double puntoMasCercanoX = Math.max(x, Math.min(puntoX, x + ancho));
        double puntoMasCercanoY = Math.max(y, Math.min(puntoY, y + alto));
        return Math.hypot(puntoX - puntoMasCercanoX, puntoY - puntoMasCercanoY);
    }

    public boolean contiene(double puntoX, double puntoY, double margen) {
        return puntoX + margen >= x
                && puntoX - margen <= x + ancho
                && puntoY + margen >= y
                && puntoY - margen <= y + alto;
    }
}
