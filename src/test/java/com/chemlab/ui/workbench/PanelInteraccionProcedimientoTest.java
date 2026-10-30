package com.chemlab.ui.workbench;

import com.chemlab.model.enums.AccionLaboratorio;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PanelInteraccionProcedimientoTest {

    @Test
    void difficultModeAllowsConfirmingReactantLoadAfterAtLeastOneMaterial() {
        Set<String> requeridos = Set.of("H2", "O2");

        assertFalse(PanelInteraccionProcedimiento.cargaCompleta(
                true, AccionLaboratorio.CARGAR_REACTIVO, Set.of(), requeridos));
        assertTrue(PanelInteraccionProcedimiento.cargaCompleta(
                true, AccionLaboratorio.CARGAR_REACTIVO, Set.of("H2"), requeridos));
    }

    @Test
    void tutorialStillRequiresAllReactants() {
        Set<String> requeridos = Set.of("H2", "O2");

        assertFalse(PanelInteraccionProcedimiento.cargaCompleta(
                false, AccionLaboratorio.CARGAR_REACTIVO, Set.of("H2"), requeridos));
        assertTrue(PanelInteraccionProcedimiento.cargaCompleta(
                false, AccionLaboratorio.CARGAR_REACTIVO, requeridos, requeridos));
    }

    @Test
    void noModeCanConfirmReactantLoadWithoutAnyDefinedOrPlacedMaterials() {
        assertFalse(PanelInteraccionProcedimiento.cargaCompleta(
                true, AccionLaboratorio.CARGAR_REACTIVO, Set.of(), Set.of()));
        assertFalse(PanelInteraccionProcedimiento.cargaCompleta(
                false, AccionLaboratorio.CARGAR_REACTIVO, Set.of(), Set.of()));
    }

    @Test
    void noModeCanConfirmAddingWaterOrCatalystWithoutAvailableMaterials() {
        assertFalse(PanelInteraccionProcedimiento.cargaCompleta(
                true, AccionLaboratorio.AGREGAR_AGUA, Set.of(), Set.of()));
        assertFalse(PanelInteraccionProcedimiento.cargaCompleta(
                true, AccionLaboratorio.AGREGAR_CATALIZADOR, Set.of(), Set.of()));
    }

    @Test
    void hardModeCatalystStepRequiresOnlyItsDefinedCatalystMaterials() {
        Set<String> catalizadores = Set.of("Fe", "Ni");

        assertFalse(PanelInteraccionProcedimiento.cargaCompleta(
                true, AccionLaboratorio.AGREGAR_CATALIZADOR, Set.of("Fe"), catalizadores));
        assertTrue(PanelInteraccionProcedimiento.cargaCompleta(
                true, AccionLaboratorio.AGREGAR_CATALIZADOR, catalizadores, catalizadores));
    }
}
