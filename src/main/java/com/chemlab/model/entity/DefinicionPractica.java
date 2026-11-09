package com.chemlab.model.entity;

import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.grammar.GramaticaRegular;
import com.chemlab.grammar.ProduccionRegular;
import com.chemlab.grammar.SimboloNoTerminal;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Contenido declarativo de una práctica, independiente de su código de ejecución. */
public record DefinicionPractica(
        String dificultad,
        String aprendizaje,
        ObjetivoPractica objetivo,
        EquipoPractica equipo,
        CondicionesIniciales condicionesIniciales,
        List<MaterialInicial> materiales,
        List<EtapaPractica> etapas,
        String estadoInicialId,
        List<ProduccionGramatical> producciones
) {
    public DefinicionPractica {
        dificultad = textoObligatorio(dificultad, "La dificultad");
        aprendizaje = textoObligatorio(aprendizaje, "El aprendizaje");
        Objects.requireNonNull(objetivo, "El objetivo es obligatorio");
        Objects.requireNonNull(equipo, "El equipo es obligatorio");
        Objects.requireNonNull(condicionesIniciales, "Las condiciones iniciales son obligatorias");
        materiales = List.copyOf(Objects.requireNonNull(materiales, "Los materiales son obligatorios"));
        etapas = List.copyOf(Objects.requireNonNull(etapas, "Las etapas son obligatorias"));
        estadoInicialId = textoObligatorio(estadoInicialId, "El símbolo inicial");
        producciones = List.copyOf(Objects.requireNonNull(producciones, "Las producciones son obligatorias"));

        if (materiales.isEmpty() || etapas.isEmpty() || producciones.isEmpty()) {
            throw new IllegalArgumentException("La práctica necesita materiales, etapas y producciones gramaticales");
        }
        validarUnicos(materiales.stream().map(MaterialInicial::sustanciaId).toList(), "sustancias");
        validarUnicos(etapas.stream().map(EtapaPractica::id).toList(), "etapas");
        validarGramatica(estadoInicialId, etapas, producciones);
    }

    public GramaticaRegular crearGramatica(String id) {
        Map<String, SimboloNoTerminal> simbolos = new java.util.LinkedHashMap<>();
        etapas.forEach(etapa -> simbolos.put(
                etapa.id(), new SimboloNoTerminal(etapa.id(), etapa.nombre())));
        List<ProduccionRegular> reglas = producciones.stream()
                .map(produccion -> produccion.esEpsilon()
                        ? ProduccionRegular.epsilon(simbolos.get(produccion.origenId()))
                        : produccion.siguienteId() == null
                                ? ProduccionRegular.terminar(
                                        simbolos.get(produccion.origenId()), produccion.terminal())
                                : ProduccionRegular.avanzar(
                                        simbolos.get(produccion.origenId()),
                                        produccion.terminal(),
                                        simbolos.get(produccion.siguienteId())))
                .toList();
        return new GramaticaRegular(
                id,
                simbolos.get(estadoInicialId),
                simbolos.values(),
                reglas);
    }

    private static void validarGramatica(
            String estadoInicialId,
            List<EtapaPractica> etapas,
            List<ProduccionGramatical> producciones
    ) {
        Set<String> idsEtapas = new HashSet<>(etapas.stream().map(EtapaPractica::id).toList());
        Map<String, SimboloNoTerminal> simbolos = new java.util.LinkedHashMap<>();
        etapas.forEach(etapa -> simbolos.put(
                etapa.id(), new SimboloNoTerminal(etapa.id(), etapa.nombre())));
        List<ProduccionRegular> reglas = producciones.stream()
                .map(produccion -> {
                    if (!idsEtapas.contains(produccion.origenId())
                            || (produccion.siguienteId() != null
                            && !idsEtapas.contains(produccion.siguienteId()))) {
                        throw new IllegalArgumentException(
                                "Cada producción debe usar símbolos definidos como etapas");
                    }
                    return produccion.esEpsilon()
                            ? ProduccionRegular.epsilon(simbolos.get(produccion.origenId()))
                            : produccion.siguienteId() == null
                                    ? ProduccionRegular.terminar(
                                            simbolos.get(produccion.origenId()), produccion.terminal())
                                    : ProduccionRegular.avanzar(
                                            simbolos.get(produccion.origenId()),
                                            produccion.terminal(),
                                            simbolos.get(produccion.siguienteId()));
                })
                .toList();
        GramaticaRegular gramatica = new GramaticaRegular(
                "VALIDACION",
                simbolos.get(estadoInicialId),
                simbolos.values(),
                reglas);

        Set<String> alcanzables = new HashSet<>();
        ArrayDeque<String> pendientes = new ArrayDeque<>();
        pendientes.add(estadoInicialId);
        while (!pendientes.isEmpty()) {
            String actual = pendientes.removeFirst();
            if (alcanzables.add(actual)) {
                gramatica.getProducciones().stream()
                        .filter(produccion -> actual.equals(produccion.origen().id()))
                        .map(ProduccionRegular::siguiente)
                        .filter(Objects::nonNull)
                        .map(SimboloNoTerminal::id)
                        .filter(destino -> !alcanzables.contains(destino))
                        .forEach(pendientes::addLast);
            }
        }
        if (!alcanzables.equals(idsEtapas)) {
            throw new IllegalArgumentException("Todas las etapas deben ser alcanzables desde el estado inicial");
        }

        Set<String> puedenFinalizar = gramatica.getProducciones().stream()
                .filter(produccion -> produccion.esEpsilon()
                        || produccion.siguiente() == null)
                .map(produccion -> produccion.origen().id())
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        boolean cambio;
        do {
            cambio = gramatica.getProducciones().stream()
                    .filter(produccion -> produccion.siguiente() != null
                            && puedenFinalizar.contains(produccion.siguiente().id()))
                    .map(produccion -> produccion.origen().id())
                    .filter(puedenFinalizar::add)
                    .findAny()
                    .isPresent();
        } while (cambio);
        if (!puedenFinalizar.containsAll(idsEtapas)) {
            throw new IllegalArgumentException("Cada etapa debe tener un recorrido hacia un estado final");
        }
    }

    private static void validarUnicos(List<String> valores, String etiqueta) {
        if (valores.stream().anyMatch(Objects::isNull)
                || new HashSet<>(valores).size() != valores.size()) {
            throw new IllegalArgumentException("La lista de " + etiqueta + " contiene valores duplicados o vacíos");
        }
    }

    private static String textoObligatorio(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombre + " es obligatorio");
        }
        return valor;
    }

    public record ObjetivoPractica(String descripcion, String productoId, double cantidadGramos) {
        public ObjetivoPractica {
            descripcion = textoObligatorio(descripcion, "La descripción del objetivo");
            productoId = textoObligatorio(productoId, "El producto objetivo");
            if (!Double.isFinite(cantidadGramos) || cantidadGramos <= 0) {
                throw new IllegalArgumentException("La cantidad objetivo debe ser positiva");
            }
        }
    }

    public record EquipoPractica(String nombre, TipoEquipo tipo, double capacidad) {
        public EquipoPractica {
            nombre = textoObligatorio(nombre, "El nombre del equipo");
            Objects.requireNonNull(tipo, "El tipo de equipo es obligatorio");
            if (!Double.isFinite(capacidad) || capacidad <= 0) {
                throw new IllegalArgumentException("La capacidad del equipo debe ser positiva");
            }
        }
    }

    public record CondicionesIniciales(
            double temperatura,
            double presion,
            double ph,
            Atmosfera atmosfera
    ) {
        public CondicionesIniciales {
            if (!Double.isFinite(temperatura) || !Double.isFinite(presion)
                    || !Double.isFinite(ph) || presion < 0 || ph < 0 || ph > 14) {
                throw new IllegalArgumentException("Las condiciones iniciales contienen valores inválidos");
            }
            Objects.requireNonNull(atmosfera, "La atmósfera inicial es obligatoria");
        }
    }

    public record MaterialInicial(String sustanciaId, double cantidadGramos) {
        public MaterialInicial {
            sustanciaId = textoObligatorio(sustanciaId, "El identificador del material");
            if (!Double.isFinite(cantidadGramos) || cantidadGramos <= 0) {
                throw new IllegalArgumentException("La cantidad de cada material debe ser positiva");
            }
        }
    }

    public record EtapaPractica(
            String id,
            String nombre,
            String equipoTecnico,
            double temperaturaActual,
            double temperaturaObjetivo,
            double presionActual,
            double presionObjetivo,
            String atmosferaRequerida,
            String catalizadorEstado,
            List<FaseReactivo> fasesReactivos,
            String explicacionManejo,
            String accionSiguienteSugerida
    ) {
        public EtapaPractica(
                String id,
                String nombre,
                String equipoTecnico,
                double temperaturaActual,
                double temperaturaObjetivo,
                double presionActual,
                double presionObjetivo,
                String atmosferaRequerida,
                String catalizadorEstado,
                String explicacionManejo,
                String accionSiguienteSugerida
        ) {
            this(id, nombre, equipoTecnico, temperaturaActual, temperaturaObjetivo,
                    presionActual, presionObjetivo, atmosferaRequerida, catalizadorEstado,
                    List.of(), explicacionManejo, accionSiguienteSugerida);
        }

        public EtapaPractica {
            id = textoObligatorio(id, "El identificador de etapa");
            nombre = textoObligatorio(nombre, "El nombre de etapa");
            equipoTecnico = textoObligatorio(equipoTecnico, "El equipo técnico de etapa");
            atmosferaRequerida = textoObligatorio(atmosferaRequerida, "La atmósfera requerida");
            catalizadorEstado = textoObligatorio(catalizadorEstado, "El estado del catalizador");
            fasesReactivos = List.copyOf(Objects.requireNonNull(fasesReactivos, "Las fases de reactivos son obligatorias"));
            explicacionManejo = textoObligatorio(explicacionManejo, "La explicación de manejo");
            accionSiguienteSugerida = textoObligatorio(accionSiguienteSugerida, "La acción siguiente");
            if (!Double.isFinite(temperaturaActual) || !Double.isFinite(temperaturaObjetivo)
                    || !Double.isFinite(presionActual) || !Double.isFinite(presionObjetivo)
                    || presionActual < 0 || presionObjetivo < 0) {
                throw new IllegalArgumentException("Las mediciones de etapa contienen valores inválidos");
            }
        }
    }

    public record FaseReactivo(
            String formula,
            String nombre,
            String rol,
            EstadoManejo estadoManejo,
            String faseFisica,
            String descripcionUbicacion
    ) {
        public FaseReactivo {
            formula = textoObligatorio(formula, "La fórmula del reactivo");
            nombre = textoObligatorio(nombre, "El nombre del reactivo");
            rol = textoObligatorio(rol, "El rol del reactivo");
            Objects.requireNonNull(estadoManejo, "El estado de manejo del reactivo es obligatorio");
            faseFisica = textoObligatorio(faseFisica, "La fase física del reactivo");
            descripcionUbicacion = textoObligatorio(descripcionUbicacion, "La ubicación del reactivo");
        }
    }

    public enum EstadoManejo {
        EN_ALMACEN,
        SEPARADO_EN_CAMARA,
        EN_CONTACTO_SOLIDO,
        MEZCLADO_HOMOGENEO,
        DISUELTO_EN_SOLVENTE,
        ACTIVADO_EN_PLASMA,
        EN_REACCION_TERMICA,
        PRODUCTO_FORMADO
    }

    public record ProduccionGramatical(
            String origenId,
            AccionLaboratorio terminal,
            String siguienteId
    ) {
        public ProduccionGramatical {
            origenId = textoObligatorio(origenId, "El símbolo izquierdo de la producción");
            if (terminal == null && siguienteId != null) {
                throw new IllegalArgumentException("Una producción epsilon no lleva símbolo siguiente");
            }
            if (siguienteId != null) {
                siguienteId = textoObligatorio(siguienteId, "El símbolo siguiente");
            }
        }

        public boolean esEpsilon() {
            return terminal == null;
        }
    }
}
