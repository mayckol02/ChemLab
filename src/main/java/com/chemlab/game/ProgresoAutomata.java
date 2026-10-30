package com.chemlab.game;

import com.chemlab.model.entity.RegistroExperimento;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ProgresoAutomata {

    private ProgresoAutomata() {}

    public static Set<String> estadosAlcanzadosCorrectamente(
            String estadoInicial,
            List<RegistroExperimento.PasoTransicion> pasos
    ) {
        Set<String> alcanzados = new LinkedHashSet<>();
        agregar(alcanzados, estadoInicial);
        for (RegistroExperimento.PasoTransicion paso : pasos) {
            if (paso != null && paso.valida()) {
                agregar(alcanzados, paso.estadoOrigen());
                agregar(alcanzados, paso.estadoDestino());
            }
        }
        return Collections.unmodifiableSet(alcanzados);
    }

    private static void agregar(Set<String> estados, String estado) {
        if (estado != null && !estado.isBlank()) {
            estados.add(estado);
        }
    }
}
