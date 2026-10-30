package com.chemlab.ui.game;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.game.ModoJuego;
import com.chemlab.game.procedure.ProtocoloProcedimiento;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.game.world.EstacionLaboratorio;

import java.util.List;

public final class PresentadorModoTutorial implements PresentadorModoJuego {

    @Override
    public ModoJuego modo() {
        return ModoJuego.TUTORIAL;
    }

    @Override
    public boolean permiteAccesoLibre() {
        return false;
    }

    @Override
    public Experimento crearExperimento(FabricaExperimento fabrica, Reaccion reaccion) {
        return fabrica.crearExperimentoParaReaccion(reaccion);
    }

    @Override
    public ProtocoloProcedimiento.InfoPasoProcedimiento obtenerProtocolo(
            String reaccionId,
            String estadoId
    ) {
        return ProtocoloProcedimiento.obtenerProtocoloPaso(reaccionId, estadoId);
    }

    @Override
    public List<TransicionQuimica> obtenerTransiciones(AutomataQuimico automata, TipoEquipo equipo) {
        return automata.getTransicionesDisponibles();
    }

    @Override
    public String descripcionAlmacen() {
        return "Elige los reactivos disponibles y la práctica que quieres realizar.";
    }

    @Override
    public String textoExperimento(Reaccion reaccion, Experimento experimento,
                                   EstacionLaboratorio estacion, boolean disponibles) {
        String nombreEstacion = estacion == null ? "sin puesto asignado" : estacion.nombre();
        return "Seleccionado: " + reaccion.getNombre()
                + "\nPrimera estación: " + nombreEstacion
                + "\nObjetivo: " + experimento.getObjetivo().getDescripcion()
                + (disponibles ? "" : "\nFaltan reactivos marcados en el almacén.");
    }

    @Override
    public String textoInicio(EstacionLaboratorio estacion) {
        return "Experimento iniciado. Ve a "
                + (estacion == null ? "la estación indicada" : estacion.nombre()) + " y pulsa F.";
    }

    @Override
    public boolean mostrarEcuacionReaccion() {
        return true;
    }
}
