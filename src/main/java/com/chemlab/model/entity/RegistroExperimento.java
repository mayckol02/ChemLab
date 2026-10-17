package com.chemlab.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Registra un intento de experimento o proceso químico, guardando su estado final,
 * las acciones ejecutadas, los detalles del autómata y el resultado.
 */
public class RegistroExperimento {

    public static record PasoTransicion(
            String estadoOrigen,
            String accion,
            String estadoDestino,
            boolean valida
    ) {}

    private final LocalDateTime fecha;
    private final String nombreExperimento;
    private final String idProceso;
    private final Map<String, String> accionesEjecutadas;
    private final ResultadoQuimico resultado;
    private final boolean exitoso;

    private final String estadoInicial;
    private final List<String> estadosFinales;
    private final List<String> todosLosEstados;
    private final List<PasoTransicion> pasosTransicion;

    public RegistroExperimento(
            String nombreExperimento,
            String idProceso,
            Map<String, String> accionesEjecutadas,
            ResultadoQuimico resultado,
            boolean exitoso
    ) {
        this(
                nombreExperimento,
                idProceso,
                accionesEjecutadas,
                resultado,
                exitoso,
                "q0",
                List.of(),
                List.of(),
                List.of()
        );
    }

    public RegistroExperimento(
            String nombreExperimento,
            String idProceso,
            Map<String, String> accionesEjecutadas,
            ResultadoQuimico resultado,
            boolean exitoso,
            String estadoInicial,
            List<String> estadosFinales,
            List<String> todosLosEstados,
            List<PasoTransicion> pasosTransicion
    ) {
        this.fecha = LocalDateTime.now();
        this.nombreExperimento = Objects.requireNonNull(nombreExperimento, "El nombre no puede ser null");
        this.idProceso = idProceso;
        this.accionesEjecutadas = new LinkedHashMap<>(
                accionesEjecutadas != null ? accionesEjecutadas : Map.of()
        );
        this.resultado = resultado;
        this.exitoso = exitoso;
        this.estadoInicial = estadoInicial != null ? estadoInicial : "q0";
        this.estadosFinales = estadosFinales != null ? List.copyOf(estadosFinales) : List.of();
        this.todosLosEstados = todosLosEstados != null ? List.copyOf(todosLosEstados) : List.of();
        this.pasosTransicion = pasosTransicion != null ? List.copyOf(pasosTransicion) : List.of();
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getNombreExperimento() {
        return nombreExperimento;
    }

    public String getIdProceso() {
        return idProceso;
    }

    public Map<String, String> getAccionesEjecutadas() {
        return Collections.unmodifiableMap(accionesEjecutadas);
    }

    public ResultadoQuimico getResultado() {
        return resultado;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getEstadoInicial() {
        return estadoInicial;
    }

    public List<String> getEstadosFinales() {
        return estadosFinales;
    }

    public List<String> getTodosLosEstados() {
        return todosLosEstados;
    }

    public List<PasoTransicion> getPasosTransicion() {
        return pasosTransicion;
    }
}

