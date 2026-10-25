package com.chemlab.grammar;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EstadoQuimico;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.model.entity.CondicionesRequeridas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Traduce las producciones regulares deterministas al autómata finito del juego. */
public final class CompiladorGramaticaRegular {

    private static final String ID_FINAL_SINTETICO = "__ACEPTACION__";

    private CompiladorGramaticaRegular() {}

    public static AutomataQuimico compilar(GramaticaRegular gramatica) {
        Map<String, EstadoQuimico> estados = new LinkedHashMap<>();
        for (SimboloNoTerminal simbolo : gramatica.getNoTerminales()) {
            estados.put(simbolo.id(), new EstadoQuimico(simbolo.id(), simbolo.nombre()));
        }

        boolean requiereAceptacionSintetica = gramatica.getProducciones().stream()
                .anyMatch(produccion -> !produccion.esEpsilon() && produccion.siguiente() == null);
        String idAceptacionSintetica = null;
        if (requiereAceptacionSintetica) {
            idAceptacionSintetica = ID_FINAL_SINTETICO;
            while (estados.containsKey(idAceptacionSintetica)) idAceptacionSintetica += "_";
            estados.put(idAceptacionSintetica,
                    new EstadoQuimico(idAceptacionSintetica, "Secuencia aceptada"));
        }

        Set<EstadoQuimico> finales = new HashSet<>();
        List<TransicionQuimica> transiciones = new ArrayList<>();
        for (ProduccionRegular produccion : gramatica.getProducciones()) {
            EstadoQuimico origen = estados.get(produccion.origen().id());
            if (produccion.esEpsilon()) {
                finales.add(origen);
                continue;
            }
            String idDestino = produccion.siguiente() == null
                    ? idAceptacionSintetica : produccion.siguiente().id();
            transiciones.add(new TransicionQuimica(
                    origen,
                    produccion.terminal(),
                    CondicionesRequeridas.sinCondicionesEspeciales(),
                    estados.get(idDestino)
            ));
        }
        return new AutomataQuimico(
                estados.get(gramatica.getInicial().id()),
                estados.values(),
                transiciones,
                finales
        );
    }
}
