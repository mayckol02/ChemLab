package com.chemlab.model.entity;

import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import com.chemlab.service.ConversorQuimico;
import com.chemlab.service.ConversorUnidades;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Inventario {

    private final Map<Sustancia, Cantidad> cantidades;
    private final ConversorUnidades conversorUnidades;
    private final ConversorQuimico conversorQuimico;

    public Inventario() {
        this.cantidades = new HashMap<>();
        this.conversorUnidades = new ConversorUnidades();
        this.conversorQuimico = new ConversorQuimico();
    }

    public void agregar(
            Sustancia sustancia,
            double cantidad,
            UnidadCantidad unidad
    ) {
        agregar(
                sustancia,
                new Cantidad(cantidad, unidad)
        );
    }

    public void agregar(
            Sustancia sustancia,
            Cantidad cantidad
    ) {
        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        Objects.requireNonNull(
                cantidad,
                "La cantidad no puede ser null"
        );

        if (cantidad.getValor() <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
            );
        }

        Cantidad cantidadActual =
                cantidades.get(sustancia);

        if (cantidadActual == null) {
            cantidades.put(
                    sustancia,
                    cantidad
            );
            return;
        }

        Cantidad cantidadConvertida = convertirParaUnidadExistente(
                sustancia,
                cantidad,
                cantidadActual.getUnidad()
        );

        double nuevoValor =
                cantidadActual.getValor()
                        + cantidadConvertida.getValor();

        cantidades.put(
                sustancia,
                new Cantidad(
                        nuevoValor,
                        cantidadActual.getUnidad()
                )
        );
    }

    /**
     * Mantiene una unidad estable por sustancia y permite sumar moles y masa
     * usando la masa molar de esa sustancia. La conversión entre unidades de
     * masa o volumen sigue delegada al conversor físico habitual.
     */
    private Cantidad convertirParaUnidadExistente(
            Sustancia sustancia,
            Cantidad cantidad,
            UnidadCantidad unidadDestino
    ) {
        if (cantidad.getUnidad() == UnidadCantidad.MOLES
                && esUnidadDeMasa(unidadDestino)) {
            return conversorQuimico.convertirDesdeMoles(
                    cantidad,
                    sustancia,
                    unidadDestino
            );
        }

        if (cantidad.getUnidad() != UnidadCantidad.MOLES
                && unidadDestino == UnidadCantidad.MOLES
                && esUnidadDeMasa(cantidad.getUnidad())) {
            return conversorQuimico.convertirAMoles(cantidad, sustancia);
        }

        return conversorUnidades.convertir(cantidad, unidadDestino);
    }

    private boolean esUnidadDeMasa(UnidadCantidad unidad) {
        return unidad == UnidadCantidad.MILIGRAMOS
                || unidad == UnidadCantidad.GRAMOS
                || unidad == UnidadCantidad.KILOGRAMOS;
    }

    public void retirar(
            Sustancia sustancia,
            double cantidad
    ) {
        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
            );
        }

        Cantidad cantidadActual =
                cantidades.get(sustancia);

        if (cantidadActual == null) {
            throw new IllegalArgumentException(
                    "La sustancia no existe en el inventario"
            );
        }

        retirar(
                sustancia,
                new Cantidad(
                        cantidad,
                        cantidadActual.getUnidad()
                )
        );
    }

    public void retirar(
            Sustancia sustancia,
            Cantidad cantidad
    ) {
        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        Objects.requireNonNull(
                cantidad,
                "La cantidad no puede ser null"
        );

        Cantidad cantidadActual =
                cantidades.get(sustancia);

        if (cantidadActual == null) {
            throw new IllegalArgumentException(
                    "La sustancia no existe en el inventario"
            );
        }

        Cantidad cantidadConvertida =
                conversorUnidades.convertir(
                        cantidad,
                        cantidadActual.getUnidad()
                );

        if (cantidadActual.getValor()
                < cantidadConvertida.getValor()) {

            throw new IllegalArgumentException(
                    "No hay suficiente cantidad de la sustancia"
            );
        }

        double nuevoValor =
                cantidadActual.getValor()
                        - cantidadConvertida.getValor();

        if (nuevoValor == 0) {
            cantidades.remove(sustancia);
        } else {
            cantidades.put(
                    sustancia,
                    new Cantidad(
                            nuevoValor,
                            cantidadActual.getUnidad()
                    )
            );
        }
    }

    public double obtenerCantidad(
            Sustancia sustancia
    ) {
        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        Cantidad cantidad =
                cantidades.get(sustancia);

        return cantidad == null
                ? 0.0
                : cantidad.getValor();
    }

    public UnidadCantidad obtenerUnidad(
            Sustancia sustancia
    ) {
        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        Cantidad cantidad =
                cantidades.get(sustancia);

        return cantidad == null
                ? null
                : cantidad.getUnidad();
    }

    public Cantidad obtenerCantidadCompleta(
            Sustancia sustancia
    ) {
        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        return cantidades.get(sustancia);
    }

    public boolean contiene(
            Sustancia sustancia
    ) {
        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        return cantidades.containsKey(sustancia);
    }

    public Map<Sustancia, Cantidad> obtenerCantidades() {
        return Collections.unmodifiableMap(cantidades);
    }

    public boolean estaVacio() {
        return cantidades.isEmpty();
    }
}
