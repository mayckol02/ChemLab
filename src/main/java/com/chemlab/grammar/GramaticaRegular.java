package com.chemlab.grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.chemlab.model.enums.AccionLaboratorio;

/** Gramática regular determinista para describir protocolos como palabras de acciones. */
public final class GramaticaRegular {

    private final String id;
    private final SimboloNoTerminal inicial;
    private final List<SimboloNoTerminal> noTerminales;
    private final List<ProduccionRegular> producciones;
    private final Map<String, SimboloNoTerminal> noTerminalPorId;
    private final Map<String, Map<AccionLaboratorio, ProduccionRegular>> produccionesPorAccion;
    private final Map<String, ProduccionRegular> produccionesEpsilon;

    public GramaticaRegular(
            String id,
            SimboloNoTerminal inicial,
            Collection<SimboloNoTerminal> noTerminales,
            Collection<ProduccionRegular> producciones
    ) {
        Objects.requireNonNull(id, "El id de la gramática es obligatorio");
        Objects.requireNonNull(inicial, "El símbolo inicial es obligatorio");
        Objects.requireNonNull(noTerminales, "Los no terminales son obligatorios");
        Objects.requireNonNull(producciones, "Las producciones son obligatorias");
        if (id.isBlank()) throw new IllegalArgumentException("El id de la gramática no puede estar vacío");

        Map<String, SimboloNoTerminal> simbolos = new HashMap<>();
        for (SimboloNoTerminal simbolo : noTerminales) {
            Objects.requireNonNull(simbolo, "La lista no puede contener no terminales nulos");
            if (simbolos.putIfAbsent(simbolo.id(), simbolo) != null) {
                throw new IllegalArgumentException("No terminal duplicado: " + simbolo.id());
            }
        }
        if (!simbolos.containsKey(inicial.id())) {
            throw new IllegalArgumentException("El símbolo inicial debe pertenecer a la gramática");
        }

        List<ProduccionRegular> copiaProducciones = new ArrayList<>();
        for (ProduccionRegular produccion : producciones) {
            Objects.requireNonNull(produccion, "La lista no puede contener producciones nulas");
            SimboloNoTerminal origen = simbolos.get(produccion.origen().id());
            if (origen == null) {
                throw new IllegalArgumentException("No existe el lado izquierdo " + produccion.origen().id());
            }
            SimboloNoTerminal siguiente = produccion.siguiente() == null
                    ? null : simbolos.get(produccion.siguiente().id());
            if (produccion.siguiente() != null && siguiente == null) {
                throw new IllegalArgumentException("No existe el no terminal destino " + produccion.siguiente().id());
            }
            copiaProducciones.add(new ProduccionRegular(origen, produccion.terminal(), siguiente));
        }
        copiaProducciones = List.copyOf(copiaProducciones);
        Map<String, Map<AccionLaboratorio, ProduccionRegular>> porAccion = new HashMap<>();
        Map<String, ProduccionRegular> epsilonPorOrigen = new HashMap<>();
        Map<String, Set<AccionLaboratorio>> terminalesPorOrigen = new HashMap<>();
        for (ProduccionRegular produccion : copiaProducciones) {
            if (produccion.esEpsilon()) {
                if (epsilonPorOrigen.putIfAbsent(produccion.origen().id(), produccion) != null) {
                    throw new IllegalArgumentException("Producción ε duplicada para " + produccion.origen().id());
                }
            } else {
                Map<AccionLaboratorio, ProduccionRegular> alternativas =
                        porAccion.computeIfAbsent(produccion.origen().id(), ignorado -> new HashMap<>());
                if (alternativas.putIfAbsent(produccion.terminal(), produccion) != null) {
                    throw new IllegalArgumentException("La gramática debe ser determinista por acción: "
                            + produccion.origen().id() + " / " + produccion.terminal());
                }
                terminalesPorOrigen.computeIfAbsent(produccion.origen().id(), ignorado -> new HashSet<>())
                        .add(produccion.terminal());
            }
        }
        for (String idNoTerminal : epsilonPorOrigen.keySet()) {
            if (!terminalesPorOrigen.getOrDefault(idNoTerminal, Set.of()).isEmpty()) {
                throw new IllegalArgumentException("Un símbolo con producción ε no puede tener salidas: " + idNoTerminal);
            }
        }

        this.id = id;
        this.inicial = simbolos.get(inicial.id());
        this.noTerminales = List.copyOf(simbolos.values().stream()
                .sorted(java.util.Comparator.comparing(SimboloNoTerminal::id)).toList());
        this.producciones = copiaProducciones;
        this.noTerminalPorId = Map.copyOf(simbolos);
        Map<String, Map<AccionLaboratorio, ProduccionRegular>> inmutables = new HashMap<>();
        porAccion.forEach((origen, alternativas) -> inmutables.put(origen, Map.copyOf(alternativas)));
        this.produccionesPorAccion = Map.copyOf(inmutables);
        this.produccionesEpsilon = Map.copyOf(epsilonPorOrigen);
    }

    public String getId() {
        return id;
    }

    public SimboloNoTerminal getInicial() {
        return inicial;
    }

    public List<SimboloNoTerminal> getNoTerminales() {
        return noTerminales;
    }

    public List<ProduccionRegular> getProducciones() {
        return producciones;
    }

    public Set<AccionLaboratorio> getTerminales() {
        Set<AccionLaboratorio> terminales = new HashSet<>();
        producciones.stream().map(ProduccionRegular::terminal)
                .filter(Objects::nonNull).forEach(terminales::add);
        return Set.copyOf(terminales);
    }

    public SimboloNoTerminal buscarNoTerminal(String simboloId) {
        return noTerminalPorId.get(simboloId);
    }

    public ProduccionRegular buscarProduccion(String simboloId, AccionLaboratorio terminal) {
        return produccionesPorAccion.getOrDefault(simboloId, Map.of()).get(terminal);
    }

    public ProduccionRegular buscarEpsilon(String simboloId) {
        return produccionesEpsilon.get(simboloId);
    }

    /** Deriva la palabra indicada y diferencia una palabra completa de un prefijo válido. */
    public ResultadoDerivacion derivar(List<AccionLaboratorio> acciones) {
        Objects.requireNonNull(acciones, "La secuencia de acciones no puede ser null");
        List<ProduccionRegular> aplicadas = new ArrayList<>();
        SimboloNoTerminal actual = inicial;
        boolean terminada = false;

        for (int indice = 0; indice < acciones.size(); indice++) {
            AccionLaboratorio accion = Objects.requireNonNull(acciones.get(indice), "La acción no puede ser null");
            if (terminada) {
                return new ResultadoDerivacion(aplicadas, null, false, indice,
                        "La secuencia contiene acciones después de llegar a un estado final.");
            }
            ProduccionRegular produccion = buscarProduccion(actual.id(), accion);
            if (produccion == null) {
                return new ResultadoDerivacion(aplicadas, actual, false, indice,
                        "No hay una producción para " + accion + " desde " + actual.nombre() + ".");
            }
            aplicadas.add(produccion);
            if (produccion.siguiente() == null) {
                terminada = true;
            } else {
                actual = noTerminalPorId.get(produccion.siguiente().id());
            }
        }

        if (terminada) {
            return new ResultadoDerivacion(aplicadas, null, true, acciones.size(), "Palabra aceptada.");
        }
        ProduccionRegular epsilon = buscarEpsilon(actual.id());
        if (epsilon != null) {
            aplicadas.add(epsilon);
            return new ResultadoDerivacion(aplicadas, null, true, acciones.size(), "Palabra aceptada.");
        }
        return new ResultadoDerivacion(aplicadas, actual, false, acciones.size(),
                "Prefijo válido; falta continuar desde " + actual.nombre() + ".");
    }
}
