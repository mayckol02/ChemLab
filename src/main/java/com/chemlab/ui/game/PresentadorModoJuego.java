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

public sealed interface PresentadorModoJuego
        permits PresentadorModoTutorial, PresentadorModoDificil {

    static PresentadorModoJuego para(ModoJuego modo) {
        return switch (modo) {
            case TUTORIAL -> new PresentadorModoTutorial();
            case DIFICIL -> new PresentadorModoDificil();
        };
    }

    ModoJuego modo();

    boolean permiteAccesoLibre();

    Experimento crearExperimento(FabricaExperimento fabrica, Reaccion reaccion);

    ProtocoloProcedimiento.InfoPasoProcedimiento obtenerProtocolo(String reaccionId, String estadoId);

    List<TransicionQuimica> obtenerTransiciones(AutomataQuimico automata, TipoEquipo equipo);

    String descripcionAlmacen();

    String textoExperimento(Reaccion reaccion, Experimento experimento,
                            EstacionLaboratorio estacion, boolean reactivosDisponibles);

    String textoInicio(EstacionLaboratorio estacion);

    boolean mostrarEcuacionReaccion();
}
