package com.chemlab.game;

import com.chemlab.model.entity.RegistroExperimento;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgresoAutomataTest {

    @Test
    void soloDesbloqueaEstadosDelInicioYTransicionesValidas() {
        List<RegistroExperimento.PasoTransicion> pasos = List.of(
                new RegistroExperimento.PasoTransicion("q0", "MEZCLAR", "q1", true),
                new RegistroExperimento.PasoTransicion("q1", "CALENTAR", "q2", false),
                new RegistroExperimento.PasoTransicion("q1", "ESPERAR", "q3", true)
        );

        assertEquals(
                Set.of("q0", "q1", "q3"),
                ProgresoAutomata.estadosAlcanzadosCorrectamente("q0", pasos)
        );
    }

    @Test
    void conservaSoloElEstadoInicialCuandoAunNoHayPasosValidos() {
        assertEquals(
                Set.of("PREPARACION"),
                ProgresoAutomata.estadosAlcanzadosCorrectamente("PREPARACION", List.of())
        );
    }
}
