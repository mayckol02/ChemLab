package com.chemlab.repository;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.catalogo.CatalogoReacciones;
import com.chemlab.catalogo.CatalogoSustancias;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RepositorioCatalogoQuimicoJson
        implements RepositorioCatalogoQuimico {

    private static final String RECURSO_PREDETERMINADO =
            "catalogo-quimico.json";

    private final String recurso;
    private final ObjectMapper objectMapper;

    public RepositorioCatalogoQuimicoJson() {
        this(RECURSO_PREDETERMINADO);
    }

    public RepositorioCatalogoQuimicoJson(
            String recurso
    ) {
        if (recurso == null || recurso.isBlank()) {
            throw new IllegalArgumentException(
                    "El recurso no puede estar vacío"
            );
        }

        this.recurso = recurso;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public CatalogoQuimico cargar() {
        try (InputStream entrada = obtenerRecurso()) {
            JsonNode raiz = objectMapper.readTree(entrada);

            return construirCatalogo(raiz);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo leer el recurso JSON: "
                            + recurso,
                    e
            );
        }
    }

    private InputStream obtenerRecurso() {
        InputStream entrada = getClass()
                .getClassLoader()
                .getResourceAsStream(recurso);

        if (entrada == null) {
            throw new IllegalStateException(
                    "No se encontró el recurso JSON: "
                            + recurso
            );
        }

        return entrada;
    }

    private CatalogoQuimico construirCatalogo(
            JsonNode raiz
    ) {
        if (raiz == null || !raiz.isObject()) {
            throw new IllegalStateException(
                    "El catálogo JSON debe contener un objeto raíz"
            );
        }

        List<Sustancia> sustancias =
                leerSustancias(raiz);

        CatalogoSustancias catalogoSustancias =
                new CatalogoSustancias(sustancias);

        List<Reaccion> reacciones =
                leerReacciones(
                        raiz,
                        catalogoSustancias
                );

        return new CatalogoQuimico(
                catalogoSustancias,
                new CatalogoReacciones(reacciones)
        );
    }

    private List<Sustancia> leerSustancias(
            JsonNode raiz
    ) {
        JsonNode nodosSustancias =
                obtenerArreglo(raiz, "sustancias");

        List<Sustancia> sustancias =
                new ArrayList<>();

        for (JsonNode nodo : nodosSustancias) {
            sustancias.add(
                    new Sustancia(
                            leerTexto(nodo, "id", "sustancia"),
                            leerTexto(nodo, "nombre", "sustancia"),
                            leerTexto(nodo, "formula", "sustancia"),
                            leerEnum(
                                    nodo,
                                    "tipo",
                                    "sustancia",
                                    TipoSustancia.class
                            ),
                            leerEnum(
                                    nodo,
                                    "estadoMateria",
                                    "sustancia",
                                    EstadoMateria.class
                            ),
                            leerMasaMolar(nodo)
                    )
            );
        }

        return sustancias;
    }

    private List<Reaccion> leerReacciones(
            JsonNode raiz,
            CatalogoSustancias catalogoSustancias
    ) {
        JsonNode nodosReacciones =
                obtenerArreglo(raiz, "reacciones");

        List<Reaccion> reacciones =
                new ArrayList<>();

        for (JsonNode nodo : nodosReacciones) {
            String id = leerTexto(
                    nodo,
                    "id",
                    "reacción"
            );

            reacciones.add(
                    new Reaccion(
                            id,
                            leerTexto(
                                    nodo,
                                    "nombre",
                                    "reacción"
                            ),
                            leerParticipantes(
                                    nodo,
                                    "reactivos",
                                    id,
                                    catalogoSustancias
                            ),
                            leerParticipantes(
                                    nodo,
                                    "productos",
                                    id,
                                    catalogoSustancias
                            )
                    )
            );
        }

        return reacciones;
    }

    private Map<Sustancia, Integer> leerParticipantes(
            JsonNode nodoReaccion,
            String campo,
            String idReaccion,
            CatalogoSustancias catalogoSustancias
    ) {
        JsonNode nodoParticipantes =
                nodoReaccion.get(campo);

        if (nodoParticipantes == null
                || !nodoParticipantes.isObject()) {

            throw new IllegalStateException(
                    "Los "
                            + campo
                            + " de la reacción "
                            + idReaccion
                            + " deben ser un objeto"
            );
        }

        Map<Sustancia, Integer> participantes =
                new HashMap<>();

        nodoParticipantes.fields().forEachRemaining(
                entrada -> {
                    String idSustancia = entrada.getKey();

                    Sustancia sustancia =
                            catalogoSustancias
                                    .buscarPorId(idSustancia)
                                    .orElseThrow(
                                            () ->
                                                    new IllegalStateException(
                                                            "La reacción "
                                                                    + idReaccion
                                                                    + " referencia "
                                                                    + "una sustancia inexistente: "
                                                                    + idSustancia
                                                    )
                                    );

                    participantes.put(
                            sustancia,
                            leerCoeficiente(
                                    entrada.getValue(),
                                    idReaccion,
                                    idSustancia
                            )
                    );
                }
        );

        return participantes;
    }

    private JsonNode obtenerArreglo(
            JsonNode raiz,
            String campo
    ) {
        JsonNode arreglo = raiz.get(campo);

        if (arreglo == null || !arreglo.isArray()) {
            throw new IllegalStateException(
                    "El campo "
                            + campo
                            + " debe ser un arreglo"
            );
        }

        return arreglo;
    }

    private String leerTexto(
            JsonNode nodo,
            String campo,
            String tipo
    ) {
        JsonNode valor = nodo.get(campo);

        if (valor == null
                || !valor.isTextual()
                || valor.asText().isBlank()) {

            throw new IllegalStateException(
                    "El campo "
                            + campo
                            + " de la "
                            + tipo
                            + " es obligatorio"
            );
        }

        return valor.asText();
    }

    private <T extends Enum<T>> T leerEnum(
            JsonNode nodo,
            String campo,
            String tipo,
            Class<T> claseEnum
    ) {
        String valor = leerTexto(
                nodo,
                campo,
                tipo
        );

        try {
            return Enum.valueOf(
                    claseEnum,
                    valor
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "El valor "
                            + valor
                            + " no es válido para "
                            + campo,
                    e
            );
        }
    }

    private double leerMasaMolar(
            JsonNode nodo
    ) {
        JsonNode valor = nodo.get("masaMolar");

        if (valor == null
                || !valor.isNumber()
                || !Double.isFinite(valor.asDouble())
                || valor.asDouble() <= 0) {

            throw new IllegalStateException(
                    "La masa molar debe ser un número positivo"
            );
        }

        return valor.asDouble();
    }

    private int leerCoeficiente(
            JsonNode nodo,
            String idReaccion,
            String idSustancia
    ) {
        if (!nodo.isInt() || nodo.asInt() <= 0) {
            throw new IllegalStateException(
                    "El coeficiente de "
                            + idSustancia
                            + " en la reacción "
                            + idReaccion
                            + " debe ser un entero positivo"
            );
        }

        return nodo.asInt();
    }
}