package com.chemlab.catalogo;

import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class CatalogoSustancias {

    private final Map<String, Sustancia> sustanciasPorId;

    public CatalogoSustancias(
            Collection<Sustancia> sustancias
    ) {
        Objects.requireNonNull(
                sustancias,
                "Las sustancias no pueden ser null"
        );

        Map<String, Sustancia> copia =
                new HashMap<>();

        for (Sustancia sustancia : sustancias) {
            Sustancia sustanciaCopiada = copiar(
                    Objects.requireNonNull(
                            sustancia,
                            "La sustancia no puede ser null"
                    )
            );

            if (copia.putIfAbsent(
                    sustanciaCopiada.getId(),
                    sustanciaCopiada
            ) != null) {
                throw new IllegalArgumentException(
                        "No pueden existir sustancias con el mismo id"
                );
            }
        }

        this.sustanciasPorId =
                Collections.unmodifiableMap(copia);
    }

    public static CatalogoSustancias predeterminado() {
        return new CatalogoSustancias(
                List.of(
                        new Sustancia(
                                "H2",
                                "Hidrógeno",
                                "H₂",
                                TipoSustancia.ELEMENTO,
                                EstadoMateria.GASEOSO,
                                2.016
                        ),
                        new Sustancia(
                                "O2",
                                "Oxígeno",
                                "O₂",
                                TipoSustancia.ELEMENTO,
                                EstadoMateria.GASEOSO,
                                32.0
                        ),
                        new Sustancia(
                                "H2O",
                                "Agua",
                                "H₂O",
                                TipoSustancia.COMPUESTO,
                                EstadoMateria.LIQUIDO,
                                18.015
                        ),
                        new Sustancia(
                                "C",
                                "Carbono",
                                "C",
                                TipoSustancia.ELEMENTO,
                                EstadoMateria.SOLIDO,
                                12.011
                        ),
                        new Sustancia(
                                "CO2",
                                "Dióxido de carbono",
                                "CO₂",
                                TipoSustancia.COMPUESTO,
                                EstadoMateria.GASEOSO,
                                44.01
                        ),
                        new Sustancia(
                                "Fe",
                                "Hierro",
                                "Fe",
                                TipoSustancia.ELEMENTO,
                                EstadoMateria.SOLIDO,
                                55.845
                        )
                )
        );
    }

    public Optional<Sustancia> buscarPorId(
            String id
    ) {
        Objects.requireNonNull(
                id,
                "El id no puede ser null"
        );

        Sustancia sustancia = sustanciasPorId.get(id);

        return sustancia == null
                ? Optional.empty()
                : Optional.of(copiar(sustancia));
    }

    public boolean contiene(
            String id
    ) {
        Objects.requireNonNull(
                id,
                "El id no puede ser null"
        );

        return sustanciasPorId.containsKey(id);
    }

    public List<Sustancia> obtenerTodas() {
        List<Sustancia> copia = new ArrayList<>();

        for (Sustancia sustancia
                : sustanciasPorId.values()) {

            copia.add(copiar(sustancia));
        }

        return Collections.unmodifiableList(copia);
    }

    private static Sustancia copiar(
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