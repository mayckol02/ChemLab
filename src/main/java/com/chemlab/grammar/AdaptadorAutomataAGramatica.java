package com.chemlab.grammar;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EstadoQuimico;
import com.chemlab.automata.TransicionQuimica;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Obtiene una gramática regular equivalente para documentar protocolos ya definidos como autómatas. */
public final class AdaptadorAutomataAGramatica {

    private AdaptadorAutomataAGramatica() {}

    public static GramaticaRegular convertir(String id, AutomataQuimico automata) {
        List<EstadoQuimico> estados = automata.getEstados().stream()
                .sorted(Comparator.comparing(EstadoQuimico::getId)).toList();
        List<SimboloNoTerminal> noTerminales = estados.stream()
                .map(estado -> new SimboloNoTerminal(estado.getId(), estado.getNombre()))
                .toList();
        List<ProduccionRegular> producciones = new ArrayList<>();
        Set<String> finales = new HashSet<>(automata.getEstadosFinales().stream()
                .map(EstadoQuimico::getId).toList());

        for (EstadoQuimico estado : estados) {
            SimboloNoTerminal origen = buscar(noTerminales, estado.getId());
            for (TransicionQuimica transicion : automata.getTransicionesDesdeEstado(estado).stream()
                    .sorted(Comparator.comparing(TransicionQuimica::getAccion)).toList()) {
                SimboloNoTerminal destino = buscar(noTerminales, transicion.getEstadoDestino().getId());
                producciones.add(ProduccionRegular.avanzar(origen, transicion.getAccion(), destino));
            }
            if (finales.contains(estado.getId())) producciones.add(ProduccionRegular.epsilon(origen));
        }
        return new GramaticaRegular(id, buscar(noTerminales, automata.getEstadoInicial().getId()),
                noTerminales, producciones);
    }

    private static SimboloNoTerminal buscar(List<SimboloNoTerminal> simbolos, String id) {
        return simbolos.stream().filter(simbolo -> simbolo.id().equals(id)).findFirst().orElseThrow();
    }
}
