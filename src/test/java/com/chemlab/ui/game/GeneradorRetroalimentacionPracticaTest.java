package com.chemlab.ui.game;

import com.chemlab.game.procedure.ProtocoloProcedimiento;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorRetroalimentacionPracticaTest {

    @Test
    void pasoValidoIndicaLaEtapaYLaSiguienteAccion() {
        ProtocoloProcedimiento.InfoPasoProcedimiento protocolo = protocolo(
                "Ejecuta CALENTAR para continuar."
        );

        var feedback = GeneradorRetroalimentacionPractica.generar(
                true, true, false, "La carga fue aceptada.", "CARGADO", protocolo);

        assertTrue(feedback.positiva());
        assertEquals("PASO VALIDADO", feedback.titulo());
        assertTrue(feedback.detalle().contains("Etapa actual: CARGADO."));
        assertTrue(feedback.detalle().contains("Siguiente paso: Ejecuta CALENTAR para continuar."));
    }

    @Test
    void pasoIncorrectoEnModoGuiadoExplicaQueCorregirYComoSeguir() {
        ProtocoloProcedimiento.InfoPasoProcedimiento protocolo = new ProtocoloProcedimiento.InfoPasoProcedimiento(
                "R_H2O", "PREPARACION", "Preparación", "Matraz", 25, 100, 1, 1,
                "Aire", "Ninguno", List.of(), "Mantén los reactivos separados.", "Carga ambos reactivos."
        );

        var feedback = GeneradorRetroalimentacionPractica.generar(
                false, true, false, "La operación no es válida.", "PREPARACION", protocolo);

        assertFalse(feedback.positiva());
        assertEquals("REVISA ESTE PASO", feedback.titulo());
        assertTrue(feedback.detalle().contains("Qué revisar: Mantén los reactivos separados."));
        assertTrue(feedback.detalle().contains("Para continuar: Carga ambos reactivos."));
    }

    @Test
    void modoLibreRecomiendaRevisarMaterialesCondicionesYEquipo() {
        var feedback = GeneradorRetroalimentacionPractica.generar(
                false, true, true, "No se formaron productos.", "q0", null);

        assertTrue(feedback.detalle().contains("materiales cargados"));
        assertTrue(feedback.detalle().contains("condiciones actuales"));
        assertTrue(feedback.detalle().contains("compatibilidad del equipo"));
    }

    @Test
    void operacionValidaEnModoLibreInvitaAObservarLaMuestra() {
        var feedback = GeneradorRetroalimentacionPractica.generar(
                true, true, true, "Operación registrada.", "q1", null);

        assertEquals("OPERACIÓN REGISTRADA", feedback.titulo());
        assertTrue(feedback.detalle().contains("Observa la muestra"));
    }

    @Test
    void practicaFinalizadaDirigeAlAnalisisYNoAlSiguientePaso() {
        var feedback = GeneradorRetroalimentacionPractica.generar(
                true, false, false, "Práctica completada.", "q5", protocolo("Sugerencia no aplicable."));

        assertTrue(feedback.detalle().contains("revisa el análisis"));
        assertFalse(feedback.detalle().contains("Sugerencia no aplicable."));
    }

    private static ProtocoloProcedimiento.InfoPasoProcedimiento protocolo(String sugerencia) {
        return new ProtocoloProcedimiento.InfoPasoProcedimiento(
                "R_H2O", "CARGADO", "Reactivos cargados", "Matraz", 25, 100, 1, 1,
                "Aire", "Ninguno", List.of(), "Reactivos preparados.", sugerencia
        );
    }
}
