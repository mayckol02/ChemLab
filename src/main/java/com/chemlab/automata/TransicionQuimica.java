package com.chemlab.automata;

import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.entity.CondicionesRequeridas;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Inventario;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TransicionQuimica {

    private final EstadoQuimico estadoOrigen;
    private final AccionLaboratorio accion;
    private final CondicionesRequeridas condicionesRequeridas;
    private final EstadoQuimico estadoDestino;

    public TransicionQuimica(
            EstadoQuimico estadoOrigen,
            AccionLaboratorio accion,
            CondicionesRequeridas condicionesRequeridas,
            EstadoQuimico estadoDestino) {
        this.estadoOrigen = Objects.requireNonNull(
                estadoOrigen,
                "El estado origen no puede ser null");

        this.accion = Objects.requireNonNull(
                accion,
                "La acción no puede ser null");

        this.condicionesRequeridas = condicionesRequeridas;

        this.estadoDestino = Objects.requireNonNull(
                estadoDestino,
                "El estado destino no puede ser null");
    }

    public EstadoQuimico getEstadoOrigen() {
        return estadoOrigen;
    }

    public AccionLaboratorio getAccion() {
        return accion;
    }

    public CondicionesRequeridas getCondicionesRequeridas() {
        return condicionesRequeridas;
    }

    public EstadoQuimico getEstadoDestino() {
        return estadoDestino;
    }

    public boolean esValida(
            CondicionesExperimentales condicionesActuales,
            Inventario inventarioActual) {
        if (condicionesRequeridas == null) {
            return true;
        }
        return condicionesRequeridas.cumple(condicionesActuales, inventarioActual);
    }
}