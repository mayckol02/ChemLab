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
