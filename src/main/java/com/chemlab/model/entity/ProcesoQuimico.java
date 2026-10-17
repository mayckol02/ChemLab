package com.chemlab.model.entity;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EjecutorAutomataQuimico;
import com.chemlab.automata.ResultadoAccion;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.engine.EjecutorReaccion;

import java.util.Objects;

/**
 * Entidad que encapsula un proceso químico de alto nivel (como HPHT o Haber-Bosch).
 * Combina un Autómata Químico que gestiona los estados con la Reacción Objetivo
 * que se ejecutará al completar el proceso.
 */
public class ProcesoQuimico {

    private final String id;
    private final String nombre;
    private final String descripcion;
    private final AutomataQuimico automata;
    private final Reaccion reaccionObjetivo;
    private final EjecutorAutomataQuimico ejecutor;

    private boolean completado;

    public ProcesoQuimico(
            String id,
            String nombre,
            String descripcion,
            AutomataQuimico automata,
            Reaccion reaccionObjetivo,
            EjecutorReaccion ejecutorReaccion
    ) {
        this.id = Objects.requireNonNull(id, "El id no puede ser null");
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser null");
        this.descripcion = descripcion != null ? descripcion : "";
        this.automata = Objects.requireNonNull(automata, "El autómata no puede ser null");
        this.reaccionObjetivo = Objects.requireNonNull(reaccionObjetivo, "La reacción objetivo no puede ser null");
        
        Objects.requireNonNull(ejecutorReaccion, "El ejecutor de reacción no puede ser null");
        this.ejecutor = new EjecutorAutomataQuimico(ejecutorReaccion, automata);
        
        this.completado = false;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public AutomataQuimico getAutomata() {
        return automata;
    }

    public Reaccion getReaccionObjetivo() {
        return reaccionObjetivo;
    }

    public boolean isCompletado() {
        return completado;
    }

    /**
     * Aplica una acción sobre este proceso. Si el autómata alcanza un estado final,
     * se ejecuta la reacción objetivo.
     */
    public ResultadoAccion aplicarAccion(
            AccionLaboratorio accion,
            Experimento experimento
    ) {
        if (completado) {
            return ResultadoAccion.fallo(
                    automata.getEstadoActual(),
                    "El proceso ya ha sido completado."
            );
        }

        ResultadoAccion resultado = ejecutor.ejecutar(accion, reaccionObjetivo, experimento);

        if (resultado.isProcesoCompleto()) {
            this.completado = true;
        }

        return resultado;
    }

    public void reiniciar() {
        this.automata.reiniciar();
        this.completado = false;
    }
}
