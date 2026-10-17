package com.chemlab.model.entity;

import com.chemlab.model.value.Cantidad;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResultadoQuimico {

    private final Map<Sustancia, Cantidad> productos;
    private final boolean exitoso;

    public ResultadoQuimico(
            Map<Sustancia, Cantidad> productos,
            boolean exitoso
    ) {
        Objects.requireNonNull(
                productos,
                "Los productos no pueden ser null"
        );

        this.productos = validarYCopiar(productos);
        this.exitoso = exitoso;
    }

    private Map<Sustancia, Cantidad> validarYCopiar(
            Map<Sustancia, Cantidad> productos
    ) {
        Map<Sustancia, Cantidad> copia = new HashMap<>();

        for (Map.Entry<Sustancia, Cantidad> entrada
                : productos.entrySet()) {

            Sustancia sustancia =
                    Objects.requireNonNull(
                            entrada.getKey(),
                            "La sustancia no puede ser null"
                    );

            Cantidad cantidad =
                    Objects.requireNonNull(
                            entrada.getValue(),
                            "La cantidad no puede ser null"
                    );

            copia.put(
                    sustancia,
                    cantidad
            );
        }

        return copia;
    }

    public Map<Sustancia, Cantidad> getProductos() {
        return Collections.unmodifiableMap(productos);
    }

    public Cantidad getCantidad(Sustancia sustancia) {
        Objects.requireNonNull(
                sustancia,
                "La sustancia no puede ser null"
        );

        return productos.get(sustancia);
    }

    public boolean isExitoso() {
        return exitoso;
    }
}