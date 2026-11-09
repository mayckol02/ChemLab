package com.chemlab.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Registra el progreso del jugador: sustancias descubiertas, procesos completados
 * y el historial de experimentos realizados.
 */
public class CuadernoLaboratorio {

    private final Set<String> sustanciasDescubiertas;
    private final Set<String> procesosCompletados;
    private final List<RegistroExperimento> historial;

    public CuadernoLaboratorio() {
        this.sustanciasDescubiertas = new HashSet<>();
        this.procesosCompletados = new HashSet<>();
        this.historial = new ArrayList<>();
        
        // Elementos iniciales que el jugador conoce
        this.sustanciasDescubiertas.add("H2");
        this.sustanciasDescubiertas.add("O2");
        this.sustanciasDescubiertas.add("C");
        this.sustanciasDescubiertas.add("Fe");
        this.sustanciasDescubiertas.add("N2");
        this.sustanciasDescubiertas.add("Ni");
    }

    public void registrarDescubrimiento(Sustancia sustancia) {
        if (sustancia != null) {
            sustanciasDescubiertas.add(sustancia.getId());
        }
    }

    public void registrarProcesoCompletado(ProcesoQuimico proceso) {
        if (proceso != null) {
            procesosCompletados.add(proceso.getId());
        }
    }

    public void agregarRegistro(RegistroExperimento registro) {
        if (registro != null) {
            historial.add(registro);
        }
    }

    public void restaurar(
            Set<String> sustancias,
            Set<String> procesos,
            List<RegistroExperimento> registros
    ) {
        if (sustancias == null || procesos == null || registros == null) {
            throw new IllegalArgumentException("El progreso del cuaderno no puede contener null.");
        }
        sustancias.forEach(id -> sustanciasDescubiertas.add(
                java.util.Objects.requireNonNull(id, "El id de sustancia no puede ser null")));
        procesos.forEach(id -> procesosCompletados.add(
                java.util.Objects.requireNonNull(id, "El id de proceso no puede ser null")));
        registros.forEach(registro -> historial.add(
                java.util.Objects.requireNonNull(registro, "El registro no puede ser null")));
    }

    public boolean haDescubierto(Sustancia sustancia) {
        return sustancia != null && sustanciasDescubiertas.contains(sustancia.getId());
    }

    public boolean haCompletado(ProcesoQuimico proceso) {
        return proceso != null && procesosCompletados.contains(proceso.getId());
    }

    public Set<String> getSustanciasDescubiertas() {
        return Collections.unmodifiableSet(sustanciasDescubiertas);
    }

    public Set<String> getProcesosCompletados() {
        return Collections.unmodifiableSet(procesosCompletados);
    }

    public List<RegistroExperimento> getHistorial() {
        return Collections.unmodifiableList(historial);
    }
}
