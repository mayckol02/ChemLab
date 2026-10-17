package com.chemlab.model.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.chemlab.model.exception.ReaccionInvalidaException;
import com.chemlab.model.enums.TipoReaccion;

public class Reaccion {

    private final String id;
    private final String nombre;
    private final TipoReaccion tipo;

    private final Map<Sustancia, Integer> reactivos;
    private final Map<Sustancia, Integer> productos;
    private final CondicionesRequeridas condicionesRequeridas;

    public Reaccion(
            String id,
            String nombre,
            Map<Sustancia, Integer> reactivos,
            Map<Sustancia, Integer> productos
    ) {
        this(
            id, 
            nombre, 
            TipoReaccion.SINTESIS, 
            reactivos, 
            productos, 
            CondicionesRequeridas.sinCondicionesEspeciales()
        );
    }

    public Reaccion(
            String id,
            String nombre,
            TipoReaccion tipo,
            Map<Sustancia, Integer> reactivos,
            Map<Sustancia, Integer> productos,
            CondicionesRequeridas condicionesRequeridas
    ) {
        this.id = Objects.requireNonNull(
                id,
                "El id no puede ser null"
        );

        this.nombre = Objects.requireNonNull(
                nombre,
                "El nombre no puede ser null"
        );

        this.tipo = Objects.requireNonNull(
                tipo,
                "El tipo no puede ser null"
        );

        this.reactivos = validarYCopiar(
                reactivos,
                "Los reactivos"
        );

        this.productos = validarYCopiar(
                productos,
                "Los productos"
        );

        this.condicionesRequeridas = condicionesRequeridas != null 
                ? condicionesRequeridas 
                : CondicionesRequeridas.sinCondicionesEspeciales();

        if (this.reactivos.isEmpty()) {
            throw new ReaccionInvalidaException(
                    "Una reacción debe tener al menos un reactivo"
            );
        }

        if (this.productos.isEmpty()) {
            throw new ReaccionInvalidaException(
                    "Una reacción debe tener al menos un producto"
            );
        }
    }

    private Map<Sustancia, Integer> validarYCopiar(
            Map<Sustancia, Integer> sustancias,
            String nombreMapa
    ) {
        Objects.requireNonNull(
                sustancias,
                nombreMapa + " no pueden ser null"
        );

        Map<Sustancia, Integer> copia =
                new HashMap<>();

        for (Map.Entry<Sustancia, Integer> entrada
                : sustancias.entrySet()) {

            Sustancia sustancia =
                    Objects.requireNonNull(
                            entrada.getKey(),
                            "La sustancia no puede ser null"
                    );

            Integer coeficiente =
                    Objects.requireNonNull(
                            entrada.getValue(),
                            "El coeficiente no puede ser null"
                    );

            if (coeficiente <= 0) {
                throw new ReaccionInvalidaException(
                        "El coeficiente debe ser mayor que cero"
                );
            }

            copia.put(sustancia, coeficiente);
        }

        return copia;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Map<Sustancia, Integer> getReactivos() {
        return Collections.unmodifiableMap(reactivos);
    }

    public Map<Sustancia, Integer> getProductos() {
        return Collections.unmodifiableMap(productos);
    }

    public TipoReaccion getTipo() {
        return tipo;
    }

    public CondicionesRequeridas getCondicionesRequeridas() {
        return condicionesRequeridas;
    }
}