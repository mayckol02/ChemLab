package com.chemlab.model.entity;

import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;

import java.util.Objects;

public class Sustancia {

    private final String id;
    private final String nombre;
    private final String formula;
    private final TipoSustancia tipo;
    private EstadoMateria estadoMateria;
    private final double masaMolar;

    public Sustancia(
            String id,
            String nombre,
            String formula,
            TipoSustancia tipo,
            EstadoMateria estadoMateria,
            double masaMolar
    ) {
        this.id = Objects.requireNonNull(id, "El id no puede ser null");
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser null");
        this.formula = Objects.requireNonNull(formula, "La fórmula no puede ser null");
        this.tipo = Objects.requireNonNull(tipo, "El tipo no puede ser null");
        this.estadoMateria = Objects.requireNonNull(
                estadoMateria,
                "El estado de la materia no puede ser null"
        );

        if (masaMolar <= 0) {
            throw new IllegalArgumentException(
                    "La masa molar debe ser mayor que cero"
            );
        }

        this.masaMolar = masaMolar;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFormula() {
        return formula;
    }

    public TipoSustancia getTipo() {
        return tipo;
    }

    public EstadoMateria getEstadoMateria() {
        return estadoMateria;
    }

    public double getMasaMolar() {
        return masaMolar;
    }

    public void cambiarEstado(EstadoMateria nuevoEstado) {
        this.estadoMateria = Objects.requireNonNull(
                nuevoEstado,
                "El nuevo estado no puede ser null"
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Sustancia sustancia)) {
            return false;
        }

        return id.equals(sustancia.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " (" + formula + ")";
    }
}