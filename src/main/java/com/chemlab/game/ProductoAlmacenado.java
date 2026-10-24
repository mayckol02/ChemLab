package com.chemlab.game;

import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.value.Cantidad;
import com.chemlab.game.world.EstacionLaboratorio;

import java.time.LocalDateTime;
import java.util.Objects;

/** Producto experimental que el jugador recogió y guardó en el almacén del laboratorio. */
public record ProductoAlmacenado(
        String experimentoOrigen,
        Sustancia sustancia,
        Cantidad cantidad,
        LocalDateTime fechaObtencion,
        EstacionLaboratorio estacionOrigen
) {
    public ProductoAlmacenado {
        experimentoOrigen = Objects.requireNonNull(experimentoOrigen, "El experimento origen es obligatorio");
        Objects.requireNonNull(sustancia, "La sustancia es obligatoria");
        Objects.requireNonNull(cantidad, "La cantidad es obligatoria");
        Objects.requireNonNull(fechaObtencion, "La fecha de obtención es obligatoria");
        Objects.requireNonNull(estacionOrigen, "La estación de origen es obligatoria");
    }
}
