package com.chemlab.repository;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.catalogo.CatalogoReacciones;
import com.chemlab.catalogo.CatalogoSustancias;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.entity.DefinicionPractica;
import com.chemlab.model.entity.CondicionesRequeridas;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoEquipo;
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
import java.util.Set;

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
            return construirCatalogo(raiz, leerPracticas(raiz));

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
            JsonNode raiz,
            Map<String, JsonNode> practicas
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
                        catalogoSustancias,
                        practicas
                );
        Set<String> idsReacciones = reacciones.stream()
                .map(Reaccion::getId)
                .collect(java.util.stream.Collectors.toSet());
        practicas.keySet().stream()
                .filter(id -> !idsReacciones.contains(id))
                .findFirst()
                .ifPresent(id -> {
                    throw new IllegalStateException(
                            "La práctica externa referencia una reacción inexistente: " + id);
                });

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
            CatalogoSustancias catalogoSustancias,
            Map<String, JsonNode> practicas
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

            Map<Sustancia, Integer> reactivos = leerParticipantes(
                    nodo, "reactivos", id, catalogoSustancias);
            Map<Sustancia, Integer> productos = leerParticipantes(
                    nodo, "productos", id, catalogoSustancias);
            JsonNode practicaExterna = practicas.get(id);
            if (nodo.has("practica")) {
                throw new IllegalStateException(
                        "La práctica de " + id + " debe estar en un archivo externo, no embebida en el catálogo");
            }
            if (practicaExterna == null) {
                throw new IllegalStateException("Falta registrar un archivo de práctica para la reacción " + id);
            }
            DefinicionPractica definicion = leerDefinicionPractica(
                    practicaExterna, id, reactivos, productos, catalogoSustancias);

            reacciones.add(
                    new Reaccion(
                            id,
                            leerTexto(
                                    nodo,
                                    "nombre",
                                    "reacción"
                            ),
                            com.chemlab.model.enums.TipoReaccion.SINTESIS,
                            reactivos,
                            productos,
                            CondicionesRequeridas.sinCondicionesEspeciales(),
                            definicion
                    )
            );
        }

        return reacciones;
    }

    private Map<String, JsonNode> leerPracticas(JsonNode raiz) throws IOException {
        JsonNode manifiesto = raiz.get("practicas");
        if (manifiesto == null) return Map.of();
        if (!manifiesto.isArray()) {
            throw new IllegalStateException("El campo practicas del catálogo debe ser un arreglo de rutas");
        }

        Map<String, JsonNode> definiciones = new HashMap<>();
        Set<String> rutas = new java.util.HashSet<>();
        for (JsonNode rutaNodo : manifiesto) {
            if (!rutaNodo.isTextual() || rutaNodo.asText().isBlank()) {
                throw new IllegalStateException("Cada ruta de práctica debe ser texto no vacío");
            }
            String ruta = rutaNodo.asText();
            validarRutaPractica(ruta);
            if (!rutas.add(ruta)) {
                throw new IllegalStateException("La ruta de práctica está repetida: " + ruta);
            }
            JsonNode archivo;
            try (InputStream entrada = obtenerRecursoRelacionado(ruta)) {
                archivo = objectMapper.readTree(entrada);
            }
            if (archivo == null || !archivo.isObject()) {
                throw new IllegalStateException("El archivo de práctica debe contener un objeto: " + ruta);
            }
            String idReaccion = leerTexto(archivo, "idReaccion", "archivo de práctica " + ruta);
            JsonNode practica = archivo.get("practica");
            if (practica == null || !practica.isObject()) {
                throw new IllegalStateException("Falta el objeto practica en " + ruta);
            }
            if (definiciones.putIfAbsent(idReaccion, practica) != null) {
                throw new IllegalStateException("Hay más de un archivo para la práctica " + idReaccion);
            }
        }
        return Map.copyOf(definiciones);
    }

    private void validarRutaPractica(String ruta) {
        if (ruta.startsWith("/") || ruta.startsWith("\\")
                || ruta.contains("\\") || ruta.contains(":")
                || java.util.Arrays.asList(ruta.split("/")).contains("..")) {
            throw new IllegalStateException("La ruta de práctica no es relativa al catálogo: " + ruta);
        }
    }

    private InputStream obtenerRecursoRelacionado(String ruta) {
        int separador = recurso.lastIndexOf('/');
        String base = separador < 0 ? "" : recurso.substring(0, separador + 1);
        String rutaCompleta = base + ruta;
        InputStream entrada = getClass().getClassLoader().getResourceAsStream(rutaCompleta);
        if (entrada == null) {
            throw new IllegalStateException("No se encontró el archivo de práctica: " + rutaCompleta);
        }
        return entrada;
    }

    private DefinicionPractica leerDefinicionPractica(
            JsonNode nodo,
            String idReaccion,
            Map<Sustancia, Integer> reactivos,
            Map<Sustancia, Integer> productos,
            CatalogoSustancias catalogoSustancias
    ) {
        if (nodo == null) return null;
        if (!nodo.isObject()) {
            throw new IllegalStateException("La definición de práctica de " + idReaccion + " debe ser un objeto");
        }

        try {
            JsonNode objetivoNodo = objetoObligatorio(nodo, "objetivo", idReaccion);
            JsonNode equipoNodo = objetoObligatorio(nodo, "equipo", idReaccion);
            JsonNode condicionesNodo = objetoObligatorio(nodo, "condicionesIniciales", idReaccion);
            String productoId = leerTexto(objetivoNodo, "producto", "objetivo de " + idReaccion);
            Sustancia producto = productos.keySet().stream()
                    .filter(sustancia -> sustancia.getId().equals(productoId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "El producto objetivo " + productoId + " no es producto de " + idReaccion));

            List<DefinicionPractica.MaterialInicial> materiales = new ArrayList<>();
            JsonNode materialesNodo = arregloObligatorio(nodo, "materiales", idReaccion);
            for (JsonNode materialNodo : materialesNodo) {
                String sustanciaId = leerTexto(materialNodo, "sustancia", "material de " + idReaccion);
                catalogoSustancias.buscarPorId(sustanciaId).orElseThrow(
                        () -> new IllegalStateException("El material " + sustanciaId
                                + " de " + idReaccion + " no existe en el catálogo"));
                materiales.add(new DefinicionPractica.MaterialInicial(
                        sustanciaId, leerNumero(materialNodo, "gramos", "material de " + idReaccion)));
            }
            Set<String> idsMateriales = materiales.stream()
                    .map(DefinicionPractica.MaterialInicial::sustanciaId)
                    .collect(java.util.stream.Collectors.toSet());
            for (Sustancia reactivo : reactivos.keySet()) {
                if (!idsMateriales.contains(reactivo.getId())) {
                    throw new IllegalStateException("Falta definir la cantidad inicial del reactivo "
                            + reactivo.getId());
                }
            }

            List<DefinicionPractica.EtapaPractica> etapas = new ArrayList<>();
            for (JsonNode etapaNodo : arregloObligatorio(nodo, "etapas", idReaccion)) {
                List<DefinicionPractica.FaseReactivo> fasesReactivos = new ArrayList<>();
                JsonNode fasesNodo = etapaNodo.get("reactivosFases");
                if (fasesNodo != null) {
                    if (!fasesNodo.isArray()) {
                        throw new IllegalStateException("El campo reactivosFases de la etapa debe ser un arreglo");
                    }
                    for (JsonNode faseNodo : fasesNodo) {
                        fasesReactivos.add(new DefinicionPractica.FaseReactivo(
                                leerTexto(faseNodo, "formula", "fase de reactivo de " + idReaccion),
                                leerTexto(faseNodo, "nombre", "fase de reactivo de " + idReaccion),
                                leerTexto(faseNodo, "rol", "fase de reactivo de " + idReaccion),
                                leerEnum(faseNodo, "estadoManejo", "fase de reactivo de " + idReaccion,
                                        DefinicionPractica.EstadoManejo.class),
                                leerTexto(faseNodo, "faseFisica", "fase de reactivo de " + idReaccion),
                                leerTexto(faseNodo, "descripcionUbicacion", "fase de reactivo de " + idReaccion)));
                    }
                }
                etapas.add(new DefinicionPractica.EtapaPractica(
                        leerTexto(etapaNodo, "id", "etapa de " + idReaccion),
                        leerTexto(etapaNodo, "nombre", "etapa de " + idReaccion),
                        leerTexto(etapaNodo, "equipoTecnico", "etapa de " + idReaccion),
                        leerNumero(etapaNodo, "temperaturaActual", "etapa de " + idReaccion),
                        leerNumero(etapaNodo, "temperaturaObjetivo", "etapa de " + idReaccion),
                        leerNumero(etapaNodo, "presionActual", "etapa de " + idReaccion),
                        leerNumero(etapaNodo, "presionObjetivo", "etapa de " + idReaccion),
                        leerTexto(etapaNodo, "atmosferaRequerida", "etapa de " + idReaccion),
                        leerTexto(etapaNodo, "catalizadorEstado", "etapa de " + idReaccion),
                        fasesReactivos,
                        leerTexto(etapaNodo, "explicacion", "etapa de " + idReaccion),
                        leerTexto(etapaNodo, "siguienteAccion", "etapa de " + idReaccion)
                ));
            }

            JsonNode gramaticaNodo = objetoObligatorio(nodo, "gramatica", idReaccion);
            List<DefinicionPractica.ProduccionGramatical> producciones = new ArrayList<>();
            for (JsonNode produccionNodo : arregloObligatorio(gramaticaNodo, "producciones", idReaccion)) {
                JsonNode terminalNodo = produccionNodo.get("terminal");
                AccionLaboratorio terminal = terminalNodo == null || terminalNodo.isNull()
                        ? null
                        : leerEnum(produccionNodo, "terminal", "producción de " + idReaccion,
                                AccionLaboratorio.class);
                JsonNode siguienteNodo = produccionNodo.get("siguiente");
                String siguiente = siguienteNodo == null || siguienteNodo.isNull()
                        ? null
                        : leerTexto(produccionNodo, "siguiente", "producción de " + idReaccion);
                JsonNode epsilonNodo = produccionNodo.get("epsilon");
                boolean epsilon = epsilonNodo != null && epsilonNodo.isBoolean() && epsilonNodo.asBoolean();
                if (terminal == null && (!epsilon || siguiente != null)) {
                    throw new IllegalStateException("Una producción sin terminal debe declarar epsilon: true"
                            + " y no puede tener símbolo siguiente (" + idReaccion + ")");
                }
                if (terminal != null && (epsilon || siguiente == null)) {
                    throw new IllegalStateException("Una producción con terminal debe declarar un símbolo siguiente"
                            + " y no puede ser epsilon (" + idReaccion + ")");
                }
                producciones.add(new DefinicionPractica.ProduccionGramatical(
                        leerTexto(produccionNodo, "origen", "producción de " + idReaccion),
                        terminal,
                        siguiente));
            }

            return new DefinicionPractica(
                    leerTexto(nodo, "dificultad", "práctica " + idReaccion),
                    leerTexto(nodo, "aprendizaje", "práctica " + idReaccion),
                    new DefinicionPractica.ObjetivoPractica(
                            leerTexto(objetivoNodo, "descripcion", "objetivo de " + idReaccion),
                            productoId,
                            leerNumero(objetivoNodo, "gramos", "objetivo de " + idReaccion)),
                    new DefinicionPractica.EquipoPractica(
                            leerTexto(equipoNodo, "nombre", "equipo de " + idReaccion),
                            leerEnum(equipoNodo, "tipo", "equipo de " + idReaccion, TipoEquipo.class),
                            leerNumero(equipoNodo, "capacidad", "equipo de " + idReaccion)),
                    new DefinicionPractica.CondicionesIniciales(
                            leerNumero(condicionesNodo, "temperatura", "condiciones de " + idReaccion),
                            leerNumero(condicionesNodo, "presion", "condiciones de " + idReaccion),
                            leerNumero(condicionesNodo, "ph", "condiciones de " + idReaccion),
                            leerEnum(condicionesNodo, "atmosfera", "condiciones de " + idReaccion,
                                    Atmosfera.class)                                    ),
                                    materiales,
                                    etapas,
                                    leerTexto(gramaticaNodo, "inicial", "gramática de " + idReaccion),
                                    producciones
            );
        } catch (IllegalArgumentException error) {
            throw new IllegalStateException(
                    "Definición de práctica inválida para " + idReaccion + ": " + error.getMessage(), error);
        }
    }

    private JsonNode objetoObligatorio(JsonNode nodo, String campo, String idReaccion) {
        JsonNode valor = nodo.get(campo);
        if (valor == null || !valor.isObject()) {
            throw new IllegalStateException("El campo " + campo + " de " + idReaccion + " debe ser un objeto");
        }
        return valor;
    }

    private JsonNode arregloObligatorio(JsonNode nodo, String campo, String idReaccion) {
        JsonNode valor = nodo.get(campo);
        if (valor == null || !valor.isArray()) {
            throw new IllegalStateException("El campo " + campo + " de " + idReaccion + " debe ser un arreglo");
        }
        return valor;
    }

    private double leerNumero(JsonNode nodo, String campo, String tipo) {
        JsonNode valor = nodo.get(campo);
        if (valor == null || !valor.isNumber() || !Double.isFinite(valor.asDouble())) {
            throw new IllegalStateException("El campo " + campo + " de " + tipo + " debe ser un número finito");
        }
        return valor.asDouble();
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