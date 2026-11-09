package com.chemlab.factory;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.DefinicionPractica;
import com.chemlab.model.entity.Equipo;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Objetivo;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.UnidadCantidad;

import java.util.Objects;
import java.util.UUID;

/** Construye experimentos exclusivamente desde definiciones de práctica. */
public final class FabricaExperimento {

    private final CatalogoQuimico catalogo;

    public FabricaExperimento(CatalogoQuimico catalogo) {
        this.catalogo = Objects.requireNonNull(catalogo, "El catálogo no puede ser null");
    }

    public Experimento crearExperimentoParaReaccion(Reaccion reaccion) {
        Objects.requireNonNull(reaccion, "La reacción no puede ser null");
        DefinicionPractica definicion = reaccion.getDefinicionPractica()
                .orElseThrow(() -> new IllegalStateException(
                        "La reacción " + reaccion.getId() + " no tiene una práctica declarativa"));

        Inventario inventario = new Inventario();
        for (DefinicionPractica.MaterialInicial material : definicion.materiales()) {
            Sustancia sustancia = catalogo.buscarSustancia(material.sustanciaId())
                    .orElseThrow(() -> new IllegalStateException(
                            "La práctica " + reaccion.getId()
                                    + " referencia una sustancia inexistente: " + material.sustanciaId()));
            inventario.agregar(sustancia, material.cantidadGramos(), UnidadCantidad.GRAMOS);
        }

        return crearExperimento(reaccion, definicion, inventario,
                definicion.condicionesIniciales().temperatura(),
                definicion.condicionesIniciales().presion(),
                definicion.condicionesIniciales().ph(),
                definicion.condicionesIniciales().atmosfera());
    }

    public Experimento crearExperimentoVacioParaReaccion(Reaccion reaccion) {
        Objects.requireNonNull(reaccion, "La reacción no puede ser null");
        DefinicionPractica definicion = reaccion.getDefinicionPractica()
                .orElseThrow(() -> new IllegalStateException(
                        "La reacción " + reaccion.getId() + " no tiene una práctica declarativa"));
        return crearExperimento(reaccion, definicion, new Inventario(), 25.0, 1.0, 7.0, Atmosfera.AIRE);
    }

    private Experimento crearExperimento(
            Reaccion reaccion,
            DefinicionPractica definicion,
            Inventario inventario,
            double temperatura,
            double presion,
            double ph,
            Atmosfera atmosfera
    ) {
        Sustancia producto = catalogo.buscarSustancia(definicion.objetivo().productoId())
                .orElseThrow(() -> new IllegalStateException(
                        "La práctica " + reaccion.getId()
                                + " referencia un producto inexistente: "
                                + definicion.objetivo().productoId()));
        Equipo equipo = new Equipo(
                "EQ-" + reaccion.getId(),
                definicion.equipo().nombre(),
                definicion.equipo().tipo(),
                definicion.equipo().capacidad());
        Objetivo objetivo = new Objetivo(
                "OBJ-" + reaccion.getId(),
                definicion.objetivo().descripcion(),
                producto,
                definicion.objetivo().cantidadGramos());

        return new Experimento(
                "EXP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                reaccion.getNombre(),
                objetivo,
                inventario,
                equipo,
                new CondicionesExperimentales(temperatura, presion, ph, atmosfera));
    }
}
