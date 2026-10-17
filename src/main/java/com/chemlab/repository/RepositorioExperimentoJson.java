package com.chemlab.repository;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Equipo;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Objetivo;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.EstadoExperimento;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

public class RepositorioExperimentoJson implements RepositorioExperimento {

    private final File directorioGuardado;
    private final ObjectMapper objectMapper;
    private final CatalogoQuimico catalogo;

    public RepositorioExperimentoJson(String rutaDirectorio, CatalogoQuimico catalogo) {
        this.directorioGuardado = new File(rutaDirectorio);
        if (!this.directorioGuardado.exists()) {
            this.directorioGuardado.mkdirs();
        }
        this.objectMapper = new ObjectMapper();
        this.catalogo = Objects.requireNonNull(catalogo, "El catálogo no puede ser null");
    }

    @Override
    public void guardar(Experimento experimento) {
        ObjectNode raiz = objectMapper.createObjectNode();
        raiz.put("id", experimento.getId());
        raiz.put("nombre", experimento.getNombre());
        raiz.put("estado", experimento.getEstado().name());

        // Objetivo
        ObjectNode objNode = raiz.putObject("objetivo");
        objNode.put("id", experimento.getObjetivo().getId());
        objNode.put("descripcion", experimento.getObjetivo().getDescripcion());
        objNode.put("sustanciaEsperadaId", experimento.getObjetivo().getSustanciaEsperada().getId());
        objNode.put("cantidadRequerida", experimento.getObjetivo().getCantidadMinima());

        // Condiciones
        ObjectNode condNode = raiz.putObject("condiciones");
        condNode.put("temperatura", experimento.getCondiciones().getTemperatura());
        condNode.put("presion", experimento.getCondiciones().getPresion());
        condNode.put("ph", experimento.getCondiciones().getPh());
        condNode.put("atmosfera", experimento.getCondiciones().getAtmosfera().name());

        // Equipo
        ObjectNode equipoNode = raiz.putObject("equipo");
        equipoNode.put("id", experimento.getEquipo().getId());
        equipoNode.put("nombre", experimento.getEquipo().getNombre());
        equipoNode.put("tipo", experimento.getEquipo().getTipo().name());
        equipoNode.put("capacidad", experimento.getEquipo().getCapacidad());

        // Inventario
        ArrayNode invNode = raiz.putArray("inventario");
        for (Map.Entry<Sustancia, Cantidad> entry : experimento.getInventario().obtenerCantidades().entrySet()) {
            ObjectNode item = invNode.addObject();
            item.put("sustanciaId", entry.getKey().getId());
            item.put("valor", entry.getValue().getValor());
            item.put("unidad", entry.getValue().getUnidad().name());
        }

        File archivo = new File(directorioGuardado, experimento.getId() + ".json");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, raiz);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el experimento", e);
        }
    }

    @Override
    public Optional<Experimento> cargar(String id) {
        File archivo = new File(directorioGuardado, id + ".json");
        if (!archivo.exists()) {
            return Optional.empty();
        }

        try {
            JsonNode raiz = objectMapper.readTree(archivo);
            String nombre = raiz.get("nombre").asText();
            EstadoExperimento estado = EstadoExperimento.valueOf(raiz.get("estado").asText());

            // Objetivo
            JsonNode objNode = raiz.get("objetivo");
            Sustancia sustObj = catalogo.buscarSustancia(objNode.get("sustanciaEsperadaId").asText())
                    .orElseThrow(() -> new IllegalStateException("Sustancia objetivo no encontrada"));
            Objetivo objetivo = new Objetivo(
                    objNode.get("id").asText(),
                    objNode.get("descripcion").asText(),
                    sustObj,
                    objNode.get("cantidadRequerida").asDouble()
            );

            // Condiciones
            JsonNode condNode = raiz.get("condiciones");
            CondicionesExperimentales condiciones = new CondicionesExperimentales(
                    condNode.get("temperatura").asDouble(),
                    condNode.get("presion").asDouble(),
                    condNode.get("ph").asDouble(),
                    Atmosfera.valueOf(condNode.get("atmosfera").asText())
            );

            // Equipo
            JsonNode eqNode = raiz.get("equipo");
            Equipo equipo = new Equipo(
                    eqNode.get("id").asText(),
                    eqNode.get("nombre").asText(),
                    TipoEquipo.valueOf(eqNode.get("tipo").asText()),
                    eqNode.get("capacidad").asDouble()
            );

            // Inventario
            Inventario inventario = new Inventario();
            JsonNode invNode = raiz.get("inventario");
            for (JsonNode item : invNode) {
                Sustancia sust = catalogo.buscarSustancia(item.get("sustanciaId").asText())
                        .orElseThrow(() -> new IllegalStateException("Sustancia en inventario no encontrada"));
                inventario.agregar(sust, item.get("valor").asDouble(), UnidadCantidad.valueOf(item.get("unidad").asText()));
            }

            Experimento experimento = new Experimento(id, nombre, objetivo, inventario, equipo, condiciones);
            
            // Si el estado no es NO_INICIADO, podríamos forzarlo, pero Experimento encapsula su estado.
            // Para propósitos de este ejercicio, si estaba EN_PROGRESO o COMPLETADO, necesitaríamos setearlo.
            // Por simplicidad, asuminos que se puede cargar y continuar, llamando a iniciar si estaba en progreso.
            if (estado == EstadoExperimento.EN_PROGRESO) {
                experimento.iniciar();
            } else if (estado == EstadoExperimento.COMPLETADO) {
                experimento.iniciar();
                // experimento.completar(...) -> Omitido por simplicidad de carga
            } else if (estado == EstadoExperimento.FALLIDO) {
                experimento.iniciar();
                experimento.fallar();
            }

            return Optional.of(experimento);

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar el experimento", e);
        }
    }

    @Override
    public List<Experimento> listarTodos() {
        List<Experimento> experimentos = new ArrayList<>();
        File[] archivos = directorioGuardado.listFiles((d, name) -> name.endsWith(".json"));
        if (archivos != null) {
            for (File archivo : archivos) {
                String id = archivo.getName().replace(".json", "");
                cargar(id).ifPresent(experimentos::add);
            }
        }
        return experimentos;
    }
}
