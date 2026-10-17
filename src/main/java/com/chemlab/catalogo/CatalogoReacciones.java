package com.chemlab.catalogo;

import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class CatalogoReacciones {

    private final Map<String, Reaccion> reaccionesPorId;

    public CatalogoReacciones(
            Collection<Reaccion> reacciones
    ) {
        Objects.requireNonNull(
                reacciones,
                "Las reacciones no pueden ser null"
        );

        Map<String, Reaccion> copia =
                new HashMap<>();

        for (Reaccion reaccion : reacciones) {
            Reaccion reaccionCopiada = copiar(
                    Objects.requireNonNull(
                            reaccion,
                            "La reacción no puede ser null"
                    )
            );

            if (copia.putIfAbsent(
                    reaccionCopiada.getId(),
                    reaccionCopiada
            ) != null) {
                throw new IllegalArgumentException(
                        "No pueden existir reacciones con el mismo id"
                );
            }
        }

        this.reaccionesPorId =
                Collections.unmodifiableMap(copia);
    }

    public static CatalogoReacciones predeterminado(
            CatalogoSustancias catalogoSustancias
    ) {
        Objects.requireNonNull(
                catalogoSustancias,
                "El catálogo de sustancias no puede ser null"
        );

        Sustancia hidrogeno = obtenerSustancia(
                catalogoSustancias,
                "H2"
        );

        Sustancia oxigeno = obtenerSustancia(
                catalogoSustancias,
                "O2"
        );

        Sustancia agua = obtenerSustancia(
                catalogoSustancias,
                "H2O"
        );

        Sustancia carbono = obtenerSustancia(
                catalogoSustancias,
                "C"
        );

        Sustancia dioxidoCarbono = obtenerSustancia(
                catalogoSustancias,
                "CO2"
        );

        Reaccion formacionAgua = new Reaccion(
                "R_H2O",
                "Formación de agua",
                Map.of(
                        hidrogeno, 2,
                        oxigeno, 1
                ),
                Map.of(agua, 2)
        );

        Reaccion combustionCarbono = new Reaccion(
                "R_CO2",
                "Combustión del carbono",
                Map.of(
                        carbono, 1,
                        oxigeno, 1
                ),
                Map.of(dioxidoCarbono, 1)
        );

        return new CatalogoReacciones(
                List.of(
                        formacionAgua,
                        combustionCarbono
                )
        );
    }

    public Optional<Reaccion> buscarPorId(
            String id
    ) {
        Objects.requireNonNull(
                id,
                "El id no puede ser null"
        );

        Reaccion reaccion = reaccionesPorId.get(id);

        return reaccion == null
                ? Optional.empty()
                : Optional.of(copiar(reaccion));
    }

    public boolean contiene(
            String id
    ) {
        Objects.requireNonNull(
                id,
                "El id no puede ser null"
        );

        return reaccionesPorId.containsKey(id);
    }

    public List<Reaccion> obtenerTodas() {
        List<Reaccion> copia = new ArrayList<>();

        for (Reaccion reaccion
                : reaccionesPorId.values()) {

            copia.add(copiar(reaccion));
        }

        return Collections.unmodifiableList(copia);
    }

    private static Sustancia obtenerSustancia(
            CatalogoSustancias catalogoSustancias,
            String id
    ) {
        return catalogoSustancias.buscarPorId(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe la sustancia requerida: "
                                        + id
                        )
                );
    }

    private static Reaccion copiar(
            Reaccion reaccion
    ) {
        return new Reaccion(
                reaccion.getId(),
                reaccion.getNombre(),
                copiarSustancias(reaccion.getReactivos()),
                copiarSustancias(reaccion.getProductos())
        );
    }

    private static Map<Sustancia, Integer> copiarSustancias(
            Map<Sustancia, Integer> sustancias
    ) {
        Map<Sustancia, Integer> copia =
                new HashMap<>();

        for (Map.Entry<Sustancia, Integer> entrada
                : sustancias.entrySet()) {

            copia.put(
                    copiarSustancia(entrada.getKey()),
                    entrada.getValue()
            );
        }

        return copia;
    }

    private static Sustancia copiarSustancia(
            Sustancia sustancia
    ) {
        return new Sustancia(
                sustancia.getId(),
                sustancia.getNombre(),
                sustancia.getFormula(),
                sustancia.getTipo(),
                sustancia.getEstadoMateria(),
                sustancia.getMasaMolar()
        );
    }
}