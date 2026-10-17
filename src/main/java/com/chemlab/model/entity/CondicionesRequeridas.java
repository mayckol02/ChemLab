package com.chemlab.model.entity;

import com.chemlab.model.enums.Atmosfera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value object que define las condiciones mínimas o máximas necesarias
 * para que una reacción química o proceso pueda llevarse a cabo.
 */
public class CondicionesRequeridas {

    private final double tempMin;
    private final double tempMax;
    private final double presionMin;
    private final double presionMax;
    private final List<String> catalizadoresRequeridos;
    private final Atmosfera atmosferaRequerida;
    private final boolean requiereIgnicion;

    public CondicionesRequeridas(
            double tempMin,
            double tempMax,
            double presionMin,
            double presionMax,
            List<String> catalizadoresRequeridos,
            Atmosfera atmosferaRequerida,
            boolean requiereIgnicion
    ) {
        if (tempMin > tempMax) {
            throw new IllegalArgumentException(
                    "La temperatura mínima no puede ser mayor a la máxima"
            );
        }

        if (presionMin > presionMax) {
            throw new IllegalArgumentException(
                    "La presión mínima no puede ser mayor a la máxima"
            );
        }

        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.presionMin = presionMin;
        this.presionMax = presionMax;
        
        this.catalizadoresRequeridos = catalizadoresRequeridos != null
                ? new ArrayList<>(catalizadoresRequeridos)
                : new ArrayList<>();
                
        this.atmosferaRequerida = atmosferaRequerida;
        this.requiereIgnicion = requiereIgnicion;
    }

    public static CondicionesRequeridas sinCondicionesEspeciales() {
        return new CondicionesRequeridas(
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                0,
                Double.POSITIVE_INFINITY,
                List.of(),
                null,
                false
        );
    }

    public double getTempMin() {
        return tempMin;
    }

    public double getTempMax() {
        return tempMax;
    }

    public double getPresionMin() {
        return presionMin;
    }

    public double getPresionMax() {
        return presionMax;
    }

    public List<String> getCatalizadoresRequeridos() {
        return Collections.unmodifiableList(catalizadoresRequeridos);
    }

    public Atmosfera getAtmosferaRequerida() {
        return atmosferaRequerida;
    }

    public boolean isRequiereIgnicion() {
        return requiereIgnicion;
    }

    /**
     * Evalúa si unas condiciones dadas y un inventario cumplen
     * con los requisitos. La ignición y otros eventos discretos
     * deberán ser manejados por el autómata.
     */
    public boolean cumple(
            CondicionesExperimentales condicionesActuales,
            Inventario inventarioActual
    ) {
        if (condicionesActuales == null || inventarioActual == null) {
            return false;
        }

        if (condicionesActuales.getTemperatura() < tempMin
                || condicionesActuales.getTemperatura() > tempMax) {
            return false;
        }

        if (condicionesActuales.getPresion() < presionMin
                || condicionesActuales.getPresion() > presionMax) {
            return false;
        }

        if (atmosferaRequerida != null
                && condicionesActuales.getAtmosfera() != atmosferaRequerida) {
            return false;
        }

        // Verifica si todos los catalizadores requeridos están presentes
        for (String catId : catalizadoresRequeridos) {
            boolean encontrado = false;
            for (Sustancia sust : inventarioActual.obtenerCantidades().keySet()) {
                if (sust.getId().equals(catId)) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                return false;
            }
        }

        return true;
    }
}
