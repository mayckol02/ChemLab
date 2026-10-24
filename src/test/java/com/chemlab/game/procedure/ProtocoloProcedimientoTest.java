package com.chemlab.game.procedure;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de ProtocoloProcedimiento - Universo Químico y Manejo Físico")
class ProtocoloProcedimientoTest {

    @Test
    @DisplayName("El diamante debe tener dos rutas científicas: HPHT y CVD")
    void diamanteDebeTenerRutasHphtYCvd() {
        List<ProtocoloProcedimiento.InfoRutaSintesis> rutas =
                ProtocoloProcedimiento.obtenerRutasDisponibles("R_DIAMANTE_HPHT");

        assertEquals(2, rutas.size(), "Diamante debe tener 2 rutas");

        var rutaHpht = rutas.stream()
                .filter(r -> r.idReaccion().equals("R_DIAMANTE_HPHT"))
                .findFirst().orElseThrow();
        assertTrue(rutaHpht.nombreRuta().contains("HPHT"));
        assertTrue(rutaHpht.equipo().contains("Prensa"));
        assertTrue(rutaHpht.metodo().contains("Fe/Ni") || rutaHpht.metodo().contains("solvente"));

        var rutaCvd = rutas.stream()
                .filter(r -> r.idReaccion().equals("R_DIAMANTE_CVD"))
                .findFirst().orElseThrow();
        assertTrue(rutaCvd.nombreRuta().contains("CVD"));
        assertTrue(rutaCvd.equipo().contains("Plasma") || rutaCvd.equipo().contains("Microondas"));
    }

    @Test
    @DisplayName("El dióxido de carbono debe tener rutas múltiples (combustión C, combustión CH4, calcinación CaCO3)")
    void co2DebeTenerRutasMultiples() {
        List<ProtocoloProcedimiento.InfoRutaSintesis> rutas =
                ProtocoloProcedimiento.obtenerRutasDisponibles("R_CO2");

        assertTrue(rutas.size() >= 3, "CO2 debe tener al menos 3 rutas alternativas");

        assertTrue(rutas.stream().anyMatch(r -> r.idReaccion().equals("R_CO2")));
        assertTrue(rutas.stream().anyMatch(r -> r.idReaccion().equals("R_CH4")));
        assertTrue(rutas.stream().anyMatch(r -> r.idReaccion().equals("R_CALCINACION_CACO3")));
    }

    @Test
    @DisplayName("El procedimiento HPHT debe reflejar condiciones extremas y separación física de elementos")
    void procedimientoHphtReflejaCondicionesReales() {
        var pasoPrep = ProtocoloProcedimiento.obtenerProtocoloPaso("R_DIAMANTE_HPHT", "PREPARACION");
        assertEquals(55000.0, pasoPrep.presionObjetivo(), 1.0);
        assertEquals(1500.0, pasoPrep.temperaturaObjetivo(), 1.0);
        assertTrue(pasoPrep.explicacionManejo().contains("SEPARADOS"));

        var pasoCargado = ProtocoloProcedimiento.obtenerProtocoloPaso("R_DIAMANTE_HPHT", "CARGADO");
        assertTrue(pasoCargado.reactivosFases().stream()
                .anyMatch(rf -> rf.estadoManejo() == ProtocoloProcedimiento.EstadoManejo.SEPARADO_EN_CAMARA));

        var pasoFinal = ProtocoloProcedimiento.obtenerProtocoloPaso("R_DIAMANTE_HPHT", "qF");
        assertTrue(pasoFinal.reactivosFases().stream()
                .anyMatch(rf -> rf.estadoManejo() == ProtocoloProcedimiento.EstadoManejo.PRODUCTO_FORMADO));
    }

    @Test
    @DisplayName("El procedimiento CVD debe operar a baja presión con plasma y gases homogéneos")
    void procedimientoCvdReflejaCondicionesReales() {
        var pasoPlasma = ProtocoloProcedimiento.obtenerProtocoloPaso("R_DIAMANTE_CVD", "PLASMA");
        assertEquals(0.05, pasoPlasma.presionObjetivo(), 0.01);
        assertEquals(1000.0, pasoPlasma.temperaturaObjetivo(), 1.0);
        assertTrue(pasoPlasma.equipoTecnico().contains("Plasma") || pasoPlasma.equipoTecnico().contains("CVD"));
        assertTrue(pasoPlasma.reactivosFases().stream()
                .anyMatch(rf -> rf.estadoManejo() == ProtocoloProcedimiento.EstadoManejo.ACTIVADO_EN_PLASMA));
    }

    @Test
    @DisplayName("Todas las 25 reacciones del catálogo deben devolver un protocolo válido y no nulo")
    void todasLasReaccionesTienenProtocoloValido() {
        CatalogoQuimico catalogo = CatalogoQuimico.predeterminado();
        assertEquals(25, catalogo.obtenerReacciones().size(), "Deben existir 25 reacciones en el catálogo");

        for (Reaccion r : catalogo.obtenerReacciones()) {
            var pasoPrep = ProtocoloProcedimiento.obtenerProtocoloPaso(r.getId(), "q0");
            assertNotNull(pasoPrep, "Protocolo no debe ser nulo para " + r.getId());
            assertNotNull(pasoPrep.equipoTecnico(), "Equipo no debe ser nulo para " + r.getId());
            assertFalse(pasoPrep.reactivosFases().isEmpty(), "Debe tener reactivos para " + r.getId());
            assertNotNull(pasoPrep.explicacionManejo(), "Explicación no debe ser nula para " + r.getId());
            assertNotNull(pasoPrep.accionSiguienteSugerida(), "Acción siguiente no debe ser nula para " + r.getId());
        }
    }

    @Test
    @DisplayName("Todas las 25 reacciones deben generar un autómata y experimento funcional")
    void todasLasReaccionesGeneranAutomataYExperimento() {
        CatalogoQuimico catalogo = CatalogoQuimico.predeterminado();
        FabricaExperimento fabrica = new FabricaExperimento(catalogo);

        for (Reaccion r : catalogo.obtenerReacciones()) {
            var automata = FabricaAutomata.crearAutomataParaReaccion(r.getId());
            assertNotNull(automata, "Autómata debe existir para " + r.getId());
            assertFalse(automata.getEstados().isEmpty(), "Autómata debe tener estados para " + r.getId());

            Experimento exp = fabrica.crearExperimentoParaReaccion(r);
            assertNotNull(exp, "Experimento debe crearse para " + r.getId());
            assertNotNull(exp.getCondiciones(), "Condiciones deben existir para " + r.getId());
            assertFalse(exp.getInventario().estaVacio(), "Inventario debe tener reactivos para " + r.getId());
        }
    }
}
