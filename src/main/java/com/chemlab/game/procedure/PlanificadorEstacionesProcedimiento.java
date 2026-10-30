package com.chemlab.game.procedure;

import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.TipoEquipo;

import java.util.Objects;

/**
 * Decide en qué puesto debe ejecutarse el siguiente paso. Mantiene la ruta
 * física separada del autómata químico y de la vista.
 */
public final class PlanificadorEstacionesProcedimiento {

    public TipoEquipo equipoRequerido(
            String idReaccion,
            String idEstado,
            AccionLaboratorio accion,
            TipoEquipo equipoPrincipal
    ) {
        Objects.requireNonNull(idReaccion, "El id de reacción no puede ser null");
        Objects.requireNonNull(idEstado, "El id de estado no puede ser null");
        Objects.requireNonNull(accion, "La acción no puede ser null");
        Objects.requireNonNull(equipoPrincipal, "El equipo principal no puede ser null");

        if ("R_DIAMANTE_CVD".equals(idReaccion)) {
            return switch (idEstado) {
                case "PREPARACION" -> TipoEquipo.MATRAZ;
                default -> TipoEquipo.CAMARA_SELLADA;
            };
        }

        if ("R_DIAMANTE_HPHT".equals(idReaccion)) {
            return switch (idEstado) {
                case "PREPARACION", "CARGADO" -> TipoEquipo.MATRAZ;
                default -> TipoEquipo.REACTOR;
            };
        }

        if ("R_CO2".equals(idReaccion) && accion == AccionLaboratorio.MEZCLAR) {
            return TipoEquipo.MATRAZ;
        }

        if (accion == AccionLaboratorio.CARGAR_REACTIVO
                && equipoPrincipal != TipoEquipo.MATRAZ
                && equipoPrincipal != TipoEquipo.BANDEJA) {
            return TipoEquipo.MATRAZ;
        }

        return equipoPrincipal;
    }
}
