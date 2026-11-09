package com.chemlab.factory;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EstadoQuimico;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.grammar.CompiladorGramaticaRegular;
import com.chemlab.model.entity.CondicionesRequeridas;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.enums.AccionLaboratorio;

import java.util.List;
import java.util.Objects;

/** Crea autómatas genéricos o compila el protocolo declarativo de una reacción. */
public final class FabricaAutomata {

    private FabricaAutomata() {}

    public static AutomataQuimico crearAutomataEstandar() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de reactivos");
        EstadoQuimico preparado = new EstadoQuimico("PREPARADO", "Reactivos cargados");
        EstadoQuimico mezclado = new EstadoQuimico("MEZCLADO", "Mezcla en progreso");
        EstadoQuimico reaccionando = new EstadoQuimico("REACCIONANDO", "Reacción en curso");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Proceso completado");
        List<EstadoQuimico> estados = List.of(preparacion, preparado, mezclado, reaccionando, completado);
        List<TransicionQuimica> transiciones = List.of(
                transicion(preparacion, AccionLaboratorio.CARGAR_REACTIVO, preparado),
                transicion(preparado, AccionLaboratorio.MEZCLAR, mezclado),
                transicion(mezclado, AccionLaboratorio.CALENTAR, reaccionando),
                transicion(reaccionando, AccionLaboratorio.FINALIZAR, completado));
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataSimple() {
        EstadoQuimico inicial = new EstadoQuimico("INICIAL", "Estado inicial");
        EstadoQuimico listo = new EstadoQuimico("LISTO", "Listo para reaccionar");
        return new AutomataQuimico(
                inicial,
                List.of(inicial, listo),
                List.of(transicion(inicial, AccionLaboratorio.CARGAR_REACTIVO, listo)),
                List.of(listo));
    }

    public static AutomataQuimico crearAutomataParaReaccion(Reaccion reaccion) {
        Objects.requireNonNull(reaccion, "La reacción es obligatoria");
        var definicion = reaccion.getDefinicionPractica()
                .orElseThrow(() -> new IllegalStateException(
                        "La reacción " + reaccion.getId() + " no tiene una práctica declarativa"));
        return CompiladorGramaticaRegular.compilar(definicion.crearGramatica(reaccion.getId()));
    }

    private static TransicionQuimica transicion(
            EstadoQuimico origen,
            AccionLaboratorio accion,
            EstadoQuimico destino
    ) {
        return new TransicionQuimica(
                origen, accion, CondicionesRequeridas.sinCondicionesEspeciales(), destino);
    }
}
