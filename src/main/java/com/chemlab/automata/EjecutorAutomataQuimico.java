package com.chemlab.automata;

import com.chemlab.engine.EjecutorReaccion;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.enums.AccionLaboratorio;

import java.util.Objects;

public final class EjecutorAutomataQuimico {

    private final EjecutorReaccion ejecutorReaccion;
    private AutomataQuimico automataQuimico;

    public EjecutorAutomataQuimico(
            EjecutorReaccion ejecutorReaccion,
            AutomataQuimico automataQuimico
    ) {
        this.ejecutorReaccion = Objects.requireNonNull(
                ejecutorReaccion,
                "El ejecutor de reacción no puede ser null"
        );

        this.automataQuimico = Objects.requireNonNull(
                automataQuimico,
                "El autómata químico no puede ser null"
        );
    }

    public void setAutomataQuimico(AutomataQuimico automataQuimico) {
        this.automataQuimico = Objects.requireNonNull(automataQuimico, "El autómata químico no puede ser null");
    }

    public AutomataQuimico getAutomataQuimico() {
        return this.automataQuimico;
    }

    public ResultadoAccion ejecutar(
            AccionLaboratorio accion,
            Reaccion reaccionObjetivo,
            Experimento experimento
    ) {
        Objects.requireNonNull(
                accion,
                "La acción no puede ser null"
        );
        
        Objects.requireNonNull(
                reaccionObjetivo,
                "La reacción objetivo no puede ser null"
        );

        Objects.requireNonNull(
                experimento,
                "El experimento no puede ser null"
        );

        if (!automataQuimico.puedeTransicionar(
                accion,
                experimento.getCondiciones(),
                experimento.getInventario()
        )) {
            return ResultadoAccion.fallo(
                    automataQuimico.getEstadoActual(),
                    "La acción " + accion + " no es válida desde el estado actual con las condiciones dadas"
            );
        }

        automataQuimico.transicionar(
                accion,
                experimento.getCondiciones(),
                experimento.getInventario()
        );

        if (automataQuimico.esEstadoFinalActual()) {
            ResultadoQuimico resultado = ejecutorReaccion.ejecutar(
                    reaccionObjetivo,
                    experimento
            );
            return ResultadoAccion.completado(
                    automataQuimico.getEstadoActual(),
                    resultado,
                    "Proceso completado. Reacción ejecutada."
            );
        }

        return ResultadoAccion.exito(
                automataQuimico.getEstadoActual(),
                "Acción aplicada exitosamente."
        );
    }
}