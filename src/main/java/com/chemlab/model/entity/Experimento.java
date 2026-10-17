package com.chemlab.model.entity;

import com.chemlab.model.enums.EstadoExperimento;
import com.chemlab.model.state.EstadoExperimentoState;
import com.chemlab.model.state.ExperimentoNoIniciadoState;

import java.util.Objects;

public class Experimento {

    private final String id;
    private final String nombre;
    private final Objetivo objetivo;
    private final Inventario inventario;
    private final Equipo equipo;
    private final CondicionesExperimentales condiciones;

    private EstadoExperimentoState estado;
    private ResultadoQuimico resultado;

    public Experimento(
            String id,
            String nombre,
            Objetivo objetivo,
            Inventario inventario,
            Equipo equipo,
            CondicionesExperimentales condiciones
    ) {
        this.id = Objects.requireNonNull(
                id,
                "El id no puede ser null"
        );

        this.nombre = Objects.requireNonNull(
                nombre,
                "El nombre no puede ser null"
        );

        this.objetivo = Objects.requireNonNull(
                objetivo,
                "El objetivo no puede ser null"
        );

        this.inventario = Objects.requireNonNull(
                inventario,
                "El inventario no puede ser null"
        );

        this.equipo = Objects.requireNonNull(
                equipo,
                "El equipo no puede ser null"
        );

        this.condiciones = Objects.requireNonNull(
                condiciones,
                "Las condiciones no pueden ser null"
        );

        this.estado = new ExperimentoNoIniciadoState();
        this.resultado = null;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Objetivo getObjetivo() {
        return objetivo;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public CondicionesExperimentales getCondiciones() {
        return condiciones;
    }

    public EstadoExperimento getEstado() {
        return estado.getTipo();
    }

    public ResultadoQuimico getResultado() {
        return resultado;
    }

    public void iniciar() {
        estado.iniciar(this);
    }

    public void completar(ResultadoQuimico resultado) {
        estado.completar(this, resultado);
    }

    public void fallar() {
        estado.fallar(this);
    }

    public void cambiarEstado(
            EstadoExperimentoState nuevoEstado
    ) {
        this.estado = Objects.requireNonNull(
                nuevoEstado,
                "El estado no puede ser null"
        );
    }

    public void establecerResultado(
            ResultadoQuimico resultado
    ) {
        this.resultado = Objects.requireNonNull(
                resultado,
                "El resultado no puede ser null"
        );
    }
}