package com.chemlab.model.entity;

import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.grammar.ResultadoDerivacion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DefinicionPracticaTest {

    @Test
    void rechazaEtapasInalcanzablesAntesDeHabilitarLaPractica() {
        var inicio = etapa("INICIO");
        var fin = etapa("FIN");
        var inalcanzable = etapa("AISLADA");

        assertThrows(IllegalArgumentException.class, () -> new DefinicionPractica(
                "BASICO",
                "Comprender una reacción.",
                new DefinicionPractica.ObjetivoPractica("Obtener agua.", "H2O", 1),
                new DefinicionPractica.EquipoPractica("Matraz", TipoEquipo.MATRAZ, 500),
                new DefinicionPractica.CondicionesIniciales(25, 1, 7, Atmosfera.AIRE),
                List.of(new DefinicionPractica.MaterialInicial("H2", 1)),
                List.of(inicio, fin, inalcanzable),
                "INICIO",
                List.of(new DefinicionPractica.ProduccionGramatical(
                        "INICIO", AccionLaboratorio.FINALIZAR, "FIN"),
                        new DefinicionPractica.ProduccionGramatical("FIN", null, null))));
    }

    @Test
    void produceUnaGramaticaRegularQueAceptaLaSecuenciaDeLaPractica() {
        var inicio = etapa("INICIO");
        var fin = etapa("FIN");
        var practica = new DefinicionPractica(
                "BASICO",
                "Comprender una reacción.",
                new DefinicionPractica.ObjetivoPractica("Obtener agua.", "H2O", 1),
                new DefinicionPractica.EquipoPractica("Matraz", TipoEquipo.MATRAZ, 500),
                new DefinicionPractica.CondicionesIniciales(25, 1, 7, Atmosfera.AIRE),
                List.of(new DefinicionPractica.MaterialInicial("H2", 1)),
                List.of(inicio, fin),
                "INICIO",
                List.of(new DefinicionPractica.ProduccionGramatical(
                                "INICIO", AccionLaboratorio.FINALIZAR, "FIN"),
                        new DefinicionPractica.ProduccionGramatical("FIN", null, null)));

        ResultadoDerivacion resultado = practica.crearGramatica("PRUEBA")
                .derivar(List.of(AccionLaboratorio.FINALIZAR));

        org.junit.jupiter.api.Assertions.assertTrue(resultado.aceptada());
    }

    private static DefinicionPractica.EtapaPractica etapa(String id) {
        return new DefinicionPractica.EtapaPractica(
                id, id, "Matraz", 25, 25, 1, 1, "Aire",
                "No requerido", "Guía de la etapa.", "Continúa.");
    }
}
