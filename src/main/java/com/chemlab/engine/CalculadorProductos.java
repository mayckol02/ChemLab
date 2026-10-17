package com.chemlab.engine;

import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CalculadorProductos {

    public Map<Sustancia, Cantidad> calcular(
            Reaccion reaccion,
            double avance
    ) {
        Objects.requireNonNull(
                reaccion,
                "La reacción no puede ser null"
        );

        if (avance < 0) {
            throw new IllegalArgumentException(
                    "El avance no puede ser negativo"
            );
        }

        Map<Sustancia, Cantidad> productos =
                new HashMap<>();

        for (Map.Entry<Sustancia, Integer> entrada
                : reaccion.getProductos().entrySet()) {

            Sustancia producto = entrada.getKey();
            int coeficiente = entrada.getValue();

            double molesProducidos =
                    avance * coeficiente;

            productos.put(
                    producto,
                    new Cantidad(
                            molesProducidos,
                            UnidadCantidad.MOLES
                    )
            );
        }

        return productos;
    }
}