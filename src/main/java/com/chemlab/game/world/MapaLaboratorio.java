package com.chemlab.game.world;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.enums.TipoEquipo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Mapa de colisiones y puestos del laboratorio, independiente del renderer. */
public final class MapaLaboratorio {

    public static final double ANCHO = 1280;
    public static final double ALTO = 720;

    private static final double RADIO_COLISION_JUGADOR = 13;
    private static final double RADIO_INTERACCION = 82;

    private final List<EstacionLaboratorio> estaciones;

    private MapaLaboratorio(List<EstacionLaboratorio> estaciones) {
        this.estaciones = List.copyOf(estaciones);
    }

    /**
     * Distribuye todas las reacciones del catálogo según el equipo que
     * construye {@link FabricaExperimento}; añadir una reacción nueva la
     * asigna al puesto correspondiente sin tocar la vista.
     */
    public static MapaLaboratorio crearPredeterminado(
            CatalogoQuimico catalogo,
            FabricaExperimento fabricaExperimento
    ) {
        Objects.requireNonNull(catalogo, "El catálogo no puede ser null");
        Objects.requireNonNull(fabricaExperimento, "La fábrica no puede ser null");

        Map<TipoEquipo, Set<String>> reaccionesPorEquipo = new EnumMap<>(TipoEquipo.class);
        for (Reaccion reaccion : catalogo.obtenerReacciones()) {
            TipoEquipo tipo = fabricaExperimento
                    .crearExperimentoParaReaccion(reaccion)
                    .getEquipo()
                    .getTipo();
            reaccionesPorEquipo.computeIfAbsent(tipo, clave -> new LinkedHashSet<>())
                    .add(reaccion.getId());
        }

        List<EstacionLaboratorio> estaciones = new ArrayList<>();
        agregarSiTieneReacciones(estaciones, reaccionesPorEquipo, TipoEquipo.MATRAZ,
                "Mesa de síntesis", "Mechero Bunsen, matraz, balanza y agitador", 70, 185, 235, 118);
        agregarSiTieneReacciones(estaciones, reaccionesPorEquipo, TipoEquipo.HORNO,
                "Zona térmica", "Horno mufla y cámara de combustión", 370, 185, 220, 118);
        agregarSiTieneReacciones(estaciones, reaccionesPorEquipo, TipoEquipo.REACTOR,
                "Prensa HPHT", "Reactor de alta presión", 650, 185, 225, 118);
        agregarSiTieneReacciones(estaciones, reaccionesPorEquipo, TipoEquipo.CAMARA_SELLADA,
                "Cámara CVD / redox", "Vacío, gases y plasma", 930, 185, 260, 118);
        agregarSiTieneReacciones(estaciones, reaccionesPorEquipo, TipoEquipo.BANDEJA,
                "Mesa de corrosión", "Bandeja de exposición ambiental", 250, 420, 245, 108);
        return new MapaLaboratorio(estaciones);
    }

    private static void agregarSiTieneReacciones(
            List<EstacionLaboratorio> estaciones,
            Map<TipoEquipo, Set<String>> reaccionesPorEquipo,
            TipoEquipo tipo,
            String nombre,
            String descripcion,
            double x,
            double y,
            double ancho,
            double alto
    ) {
        Set<String> reacciones = reaccionesPorEquipo.get(tipo);
        if (reacciones != null && !reacciones.isEmpty()) {
            estaciones.add(new EstacionLaboratorio(
                    "ESTACION_" + tipo.name(), nombre, descripcion,
                    tipo, x, y, ancho, alto, reacciones
            ));
        }
    }

    public List<EstacionLaboratorio> getEstaciones() {
        return estaciones;
    }

    public EstacionLaboratorio buscarEstacionParaReaccion(String idReaccion) {
        return estaciones.stream().filter(estacion -> estacion.admite(idReaccion))
                .findFirst().orElse(null);
    }

    public EstacionLaboratorio buscarEstacionPorEquipo(TipoEquipo tipoEquipo) {
        Objects.requireNonNull(tipoEquipo, "El tipo de equipo no puede ser null");
        return estaciones.stream()
                .filter(estacion -> estacion.tipoEquipo() == tipoEquipo)
                .findFirst()
                .orElse(null);
    }

    public EstacionLaboratorio estacionCercana(double x, double y) {
        return estaciones.stream()
                .filter(estacion -> estacion.distanciaHasta(x, y) <= RADIO_INTERACCION)
                .min((a, b) -> Double.compare(
                        a.distanciaHasta(x, y), b.distanciaHasta(x, y)
                ))
                .orElse(null);
    }

    public boolean hayColision(double x, double y) {
        if (x < 34 || x > ANCHO - 34 || y < 116 || y > ALTO - 28) {
            return true;
        }
        return estaciones.stream()
                .anyMatch(estacion -> estacion.contiene(x, y, RADIO_COLISION_JUGADOR));
    }
}
