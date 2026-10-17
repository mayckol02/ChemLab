package com.chemlab.automata;

import com.chemlab.model.entity.CondicionesRequeridas;
import com.chemlab.model.enums.AccionLaboratorio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import java.util.List;
import java.util.Set;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Inventario;
import com.chemlab.model.enums.Atmosfera;

import static org.junit.jupiter.api.Assertions.*;

class AutomataQuimicoEstadoFinalTest {

    private final CondicionesExperimentales condiciones = new CondicionesExperimentales(25.0, 1.0, 7.0, Atmosfera.AIRE);
    private final Inventario inventario = new Inventario();

    @Test
    void debeReconocerElEstadoFinalActual() {
        EstadoQuimico inicial = new EstadoQuimico(
                "INICIAL",
                "Mezcla inicial"
        );

        EstadoQuimico aguaFormada =
                new EstadoQuimico(
                        "AGUA_FORMADA",
                        "Agua formada"
                );

        AutomataQuimico automata =
                new AutomataQuimico(
                        inicial,
                        List.of(
                                inicial,
                                aguaFormada
                        ),
                        List.of(
                                new TransicionQuimica(
                                        inicial,
                                        AccionLaboratorio.CARGAR_REACTIVO,
                                        CondicionesRequeridas.sinCondicionesEspeciales(),
                                        aguaFormada
                                )
                        ),
                        List.of(aguaFormada)
                );

        assertFalse(automata.esEstadoFinalActual());

        automata.transicionar(AccionLaboratorio.CARGAR_REACTIVO, condiciones, inventario);

        assertTrue(automata.esEstadoFinalActual());
    }

    @Test
    void noDebePermitirTransicionesSalientesDesdeEstadoFinal() {
        EstadoQuimico inicial = new EstadoQuimico(
                "INICIAL",
                "Mezcla inicial"
        );

        EstadoQuimico aguaFormada =
                new EstadoQuimico(
                        "AGUA_FORMADA",
                        "Agua formada"
                );

        EstadoQuimico carbonoOxidado =
                new EstadoQuimico(
                        "CARBONO_OXIDADO",
                        "Carbono oxidado"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new AutomataQuimico(
                        inicial,
                        List.of(
                                inicial,
                                aguaFormada,
                                carbonoOxidado
                        ),
                        List.of(
                                new TransicionQuimica(
                                        inicial,
                                        AccionLaboratorio.CARGAR_REACTIVO,
                                        CondicionesRequeridas.sinCondicionesEspeciales(),
                                        aguaFormada
                                ),
                                new TransicionQuimica(
                                        aguaFormada,
                                        AccionLaboratorio.MEZCLAR,
                                        CondicionesRequeridas.sinCondicionesEspeciales(),
                                        carbonoOxidado
                                )
                        ),
                        List.of(aguaFormada)
                )
        );
    }
}