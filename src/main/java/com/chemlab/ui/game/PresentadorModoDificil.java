package com.chemlab.ui.game;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.game.ModoJuego;
import com.chemlab.game.OperacionesEquipo;
import com.chemlab.game.procedure.ProtocoloProcedimiento;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.game.world.EstacionLaboratorio;

import java.util.List;

public final class PresentadorModoDificil implements PresentadorModoJuego {

    @Override
    public ModoJuego modo() {
        return ModoJuego.DIFICIL;
    }

    @Override
    public boolean permiteAccesoLibre() {
        return true;
    }

    @Override
    public Experimento crearExperimento(FabricaExperimento fabrica, Reaccion reaccion) {
        return fabrica.crearExperimentoVacioParaReaccion(reaccion);
    }

    @Override
    public ProtocoloProcedimiento.InfoPasoProcedimiento obtenerProtocolo(
            String reaccionId,
            String estadoId
    ) {
        return null;
    }

    @Override
    public List<TransicionQuimica> obtenerTransiciones(AutomataQuimico automata, TipoEquipo equipo) {
        var estado = automata.getEstadoActual();
        return OperacionesEquipo.disponibles(equipo).stream()
                .map(accion -> new TransicionQuimica(estado, accion, null, estado))
                .toList();
    }

    @Override
    public String descripcionAlmacen() {
        return "Selecciona una práctica; en el laboratorio decidirás qué materiales cargar y qué operaciones realizar.";
    }

    @Override
    public String textoExperimento(Reaccion reaccion, Experimento experimento,
                                   EstacionLaboratorio estacion, boolean disponibles) {
        return "Experimento seleccionado: " + reaccion.getNombre()
                + (disponibles ? "" : "\nActiva en el almacén los reactivos de esta práctica.");
    }

    @Override
    public String textoInicio(EstacionLaboratorio estacion) {
        return "Experimento iniciado. Explora el laboratorio y elige cualquier puesto.";
    }

    @Override
    public boolean mostrarEcuacionReaccion() {
        return false;
    }
}
