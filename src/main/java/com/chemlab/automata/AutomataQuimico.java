package com.chemlab.automata;

import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Inventario;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AutomataQuimico {

    private final Map<String, EstadoQuimico> estadosPorId;

    private final Map<EstadoQuimico,
            Map<AccionLaboratorio, TransicionQuimica>>
            transicionesPorOrigen;

    private final Set<EstadoQuimico> estadosFinales;
    private final EstadoQuimico estadoInicial;

    private EstadoQuimico estadoActual;

    public AutomataQuimico(
            EstadoQuimico estadoInicial,
            Collection<EstadoQuimico> estados,
            Collection<TransicionQuimica> transiciones
    ) {
        this(
                estadoInicial,
                estados,
                transiciones,
                Set.of()
        );
    }

    public AutomataQuimico(
            EstadoQuimico estadoInicial,
            Collection<EstadoQuimico> estados,
            Collection<TransicionQuimica> transiciones,
            Collection<EstadoQuimico> estadosFinales
    ) {
        Objects.requireNonNull(
                estadoInicial,
                "El estado inicial no puede ser null"
        );

        Objects.requireNonNull(
                estados,
                "Los estados no pueden ser null"
        );

        Objects.requireNonNull(
                transiciones,
                "Las transiciones no pueden ser null"
        );

        Objects.requireNonNull(
                estadosFinales,
                "Los estados finales no pueden ser null"
        );

        Map<String, EstadoQuimico> estadosCopia =
                new HashMap<>();

        for (EstadoQuimico estado : estados) {
            EstadoQuimico estadoValidado =
                    Objects.requireNonNull(
                            estado,
                            "El estado no puede ser null"
                    );

            if (estadosCopia.putIfAbsent(
                    estadoValidado.getId(),
                    estadoValidado
            ) != null) {
                throw new IllegalArgumentException(
                        "No pueden existir estados con el mismo id"
                );
            }
        }

        EstadoQuimico estadoInicialRegistrado =
                estadosCopia.get(estadoInicial.getId());

        if (estadoInicialRegistrado == null) {
            throw new IllegalArgumentException(
                    "El estado inicial debe pertenecer al autómata"
            );
        }

        Set<EstadoQuimico> finalesCopia =
                new HashSet<>();

        for (EstadoQuimico estadoFinal : estadosFinales) {
            EstadoQuimico estadoFinalRegistrado =
                    estadosCopia.get(
                            Objects.requireNonNull(
                                    estadoFinal,
                                    "El estado final no puede ser null"
                            ).getId()
                    );

            if (estadoFinalRegistrado == null) {
                throw new IllegalArgumentException(
                        "Todo estado final debe pertenecer al autómata"
                );
            }

            finalesCopia.add(estadoFinalRegistrado);
        }

        Map<EstadoQuimico,
                Map<AccionLaboratorio, TransicionQuimica>>
                indiceTransiciones = new HashMap<>();

        for (TransicionQuimica transicion : transiciones) {
            TransicionQuimica transicionValidada =
                    Objects.requireNonNull(
                            transicion,
                            "La transición no puede ser null"
                    );

            EstadoQuimico origen = estadosCopia.get(
                    transicionValidada
                            .getEstadoOrigen()
                            .getId()
            );

            EstadoQuimico destino = estadosCopia.get(
                    transicionValidada
                            .getEstadoDestino()
                            .getId()
            );

            if (origen == null || destino == null) {
                throw new IllegalArgumentException(
                        "Toda transición debe usar estados registrados"
                );
            }

            AccionLaboratorio accion = transicionValidada
                    .getAccion();

            Map<AccionLaboratorio, TransicionQuimica>
                    transicionesDelEstado =
                    indiceTransiciones.computeIfAbsent(
                            origen,
                            clave -> new HashMap<>()
                    );

            if (transicionesDelEstado.putIfAbsent(
                    accion,
                    new TransicionQuimica(
                            origen,
                            transicionValidada.getAccion(),
                            transicionValidada.getCondicionesRequeridas(),
                            destino
                    )
            ) != null) {
                throw new IllegalArgumentException(
                        "No pueden existir dos transiciones "
                                + "para la misma acción "
                                + "desde un mismo estado"
                );
            }
        }

        for (EstadoQuimico estadoFinal : finalesCopia) {
            if (!indiceTransiciones.getOrDefault(
                    estadoFinal,
                    Map.of()
            ).isEmpty()) {
                throw new IllegalArgumentException(
                        "Un estado final no puede tener transiciones salientes"
                );
            }
        }

        Map<EstadoQuimico,
                Map<AccionLaboratorio, TransicionQuimica>>
                transicionesInmutables = new HashMap<>();

        for (Map.Entry<EstadoQuimico,
                Map<AccionLaboratorio, TransicionQuimica>> entrada
                : indiceTransiciones.entrySet()) {

            transicionesInmutables.put(
                    entrada.getKey(),
                    Collections.unmodifiableMap(
                            entrada.getValue()
                    )
            );
        }

        this.estadosPorId = Collections.unmodifiableMap(
                estadosCopia
        );

        this.transicionesPorOrigen =
                Collections.unmodifiableMap(
                        transicionesInmutables
                );

        this.estadosFinales = Collections.unmodifiableSet(
                finalesCopia
        );

        this.estadoInicial = estadoInicialRegistrado;
        this.estadoActual = estadoInicialRegistrado;
    }

    public EstadoQuimico getEstadoActual() {
        return estadoActual;
    }

    public EstadoQuimico getEstadoInicial() {
        return estadoInicial;
    }

    public Set<EstadoQuimico> getEstadosFinales() {
        return Collections.unmodifiableSet(estadosFinales);
    }

    public List<EstadoQuimico> getEstados() {
        return Collections.unmodifiableList(
                new ArrayList<>(
                        estadosPorId.values()
                )
        );
    }

    public boolean esEstadoFinalActual() {
        return estadosFinales.contains(estadoActual);
    }

    public List<TransicionQuimica>
    getTransicionesDisponibles() {
        Map<AccionLaboratorio, TransicionQuimica> transiciones =
                transicionesPorOrigen.getOrDefault(
                        estadoActual,
                        Map.of()
                );

        return Collections.unmodifiableList(
                new ArrayList<>(
                        transiciones.values()
                )
        );
    }

    /**
     * Devuelve las transiciones salientes desde un estado concreto,
     * independientemente del estado actual del autómata.
     * Útil para construir la guía visual del proceso.
     */
    public List<TransicionQuimica>
    getTransicionesDesdeEstado(EstadoQuimico estado) {
        Objects.requireNonNull(estado, "El estado no puede ser null");
        Map<AccionLaboratorio, TransicionQuimica> transiciones =
                transicionesPorOrigen.getOrDefault(estado, Map.of());
        return Collections.unmodifiableList(new ArrayList<>(transiciones.values()));
    }

    public boolean puedeTransicionar(
            AccionLaboratorio accion,
            CondicionesExperimentales condicionesActuales,
            Inventario inventarioActual
    ) {
        Objects.requireNonNull(
                accion,
                "La acción no puede ser null"
        );

        TransicionQuimica transicion = transicionesPorOrigen.getOrDefault(
                estadoActual,
                Map.of()
        ).get(accion);

        if (transicion == null) {
            return false;
        }

        return transicion.esValida(condicionesActuales, inventarioActual);
    }

    public EstadoQuimico transicionar(
            AccionLaboratorio accion,
            CondicionesExperimentales condicionesActuales,
            Inventario inventarioActual
    ) {
        Objects.requireNonNull(
                accion,
                "La acción no puede ser null"
        );

        TransicionQuimica transicion =
                transicionesPorOrigen.getOrDefault(
                        estadoActual,
                        Map.of()
                ).get(accion);

        if (transicion == null) {
            throw new IllegalStateException(
                    "La acción "
                            + accion
                            + " no está permitida desde el estado "
                            + estadoActual.getId()
            );
        }

        if (!transicion.esValida(condicionesActuales, inventarioActual)) {
            throw new IllegalStateException(
                    "Las condiciones actuales no permiten la acción " + accion
            );
        }

        estadoActual = transicion.getEstadoDestino();

        return estadoActual;
    }

    public void reiniciar() {
        estadoActual = estadoInicial;
    }
}