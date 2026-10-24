package com.chemlab.ui.workbench;

import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import javafx.scene.paint.Color;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Metadatos de presentación para dibujar reactivos, productos y cambios de fase. */
public final class VisualizadorReaccion {

    public enum Fenomeno {
        OXIDACION("Oxidación · capa de óxido"),
        REDUCCION("Reducción · recuperación del metal"),
        DISOLUCION("Disolución · soluto en fase acuosa"),
        PRECIPITACION("Precipitación · partículas sólidas en suspensión"),
        CRISTALIZACION("Cristalización · sólido recuperado"),
        CALCINACION("Calcinación · sólido y gas"),
        DEPOSICION("Deposición · crecimiento superficial"),
        COMBUSTION("Combustión · gases y condensado"),
        SINTESIS("Síntesis · nuevo compuesto");

        private final String etiqueta;

        Fenomeno(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String etiqueta() {
            return etiqueta;
        }
    }

    public record Especie(Sustancia sustancia, int coeficiente, Color color) {
        public Especie {
            Objects.requireNonNull(sustancia, "La sustancia visual es obligatoria");
            Objects.requireNonNull(color, "El color de la especie es obligatorio");
        }

        public String formulaEstequiometrica() {
            return (coeficiente > 1 ? Integer.toString(coeficiente) : "") + sustancia.getFormula();
        }

        public String fase() {
            return switch (sustancia.getEstadoMateria()) {
                case SOLIDO -> "SÓLIDO";
                case LIQUIDO -> "LÍQUIDO";
                case GASEOSO -> "GAS";
                case PLASMA -> "PLASMA";
            };
        }
    }

    public record Perfil(
            List<Especie> reactivos,
            List<Especie> productos,
            Fenomeno fenomeno,
            Color tonoReactivos,
            Color tonoProductos
    ) {
        public Perfil {
            reactivos = List.copyOf(reactivos);
            productos = List.copyOf(productos);
            Objects.requireNonNull(fenomeno, "El fenómeno visual es obligatorio");
            Objects.requireNonNull(tonoReactivos, "El tono de reactivos es obligatorio");
            Objects.requireNonNull(tonoProductos, "El tono de productos es obligatorio");
        }

        public boolean productoEnFase(EstadoMateria fase) {
            return productos.stream().anyMatch(especie -> especie.sustancia().getEstadoMateria() == fase);
        }

        public boolean esFenomeno(Fenomeno tipo) {
            return fenomeno == tipo;
        }

        public String resumenReactivos() {
            return reactivos.stream().map(Especie::formulaEstequiometrica)
                    .collect(Collectors.joining(" + "));
        }

        public String resumenProductos() {
            return productos.stream().map(Especie::formulaEstequiometrica)
                    .collect(Collectors.joining(" + "));
        }

        public String resumenFasesProducto() {
            return productos.stream().map(Especie::fase).distinct().collect(Collectors.joining(" + "));
        }
    }

    private VisualizadorReaccion() {}

    public static Perfil crear(Reaccion reaccion) {
        Objects.requireNonNull(reaccion, "La reacción es obligatoria");
        List<Especie> reactivos = especies(reaccion.getReactivos());
        List<Especie> productos = especies(reaccion.getProductos());
        return new Perfil(reactivos, productos, fenomeno(reaccion), tonoMedio(reactivos), tonoMedio(productos));
    }

    private static List<Especie> especies(Map<Sustancia, Integer> mapa) {
        return mapa.entrySet().stream()
                .sorted(Comparator.comparing(entrada -> entrada.getKey().getId()))
                .map(entrada -> new Especie(entrada.getKey(), entrada.getValue(), color(entrada.getKey())))
                .toList();
    }

    private static Fenomeno fenomeno(Reaccion reaccion) {
        String id = reaccion.getId().toUpperCase(Locale.ROOT);
        if (id.contains("CORROSION") || id.contains("FE2O3") || id.contains("CUO")
                || id.contains("OXIDO_METALICO")) return Fenomeno.OXIDACION;
        if (id.contains("REDOX")) return Fenomeno.REDUCCION;
        if (id.contains("DISOLUCION")) return Fenomeno.DISOLUCION;
        if (id.contains("CACO3")) return Fenomeno.PRECIPITACION;
        if (id.contains("SEPARACION")) return Fenomeno.CRISTALIZACION;
        if (id.contains("CALCINACION")) return Fenomeno.CALCINACION;
        if (id.contains("DIAMANTE_CVD")) return Fenomeno.DEPOSICION;
        if (id.contains("COMBUSTION") || id.equals("R_CO2") || id.equals("R_CH4")
                || id.equals("R_H2O") || id.equals("R_SO2")) return Fenomeno.COMBUSTION;
        return Fenomeno.SINTESIS;
    }

    private static Color tonoMedio(List<Especie> especies) {
        if (especies.isEmpty()) return Color.web("#b6c9c8");
        double rojo = 0;
        double verde = 0;
        double azul = 0;
        for (Especie especie : especies) {
            rojo += especie.color().getRed();
            verde += especie.color().getGreen();
            azul += especie.color().getBlue();
        }
        double cantidad = especies.size();
        return new Color(rojo / cantidad, verde / cantidad, azul / cantidad, 1);
    }

    private static Color color(Sustancia sustancia) {
        return switch (sustancia.getId()) {
            case "Fe2O3", "FeOH3" -> Color.web("#b85e3f");
            case "CuO" -> Color.web("#33373b");
            case "Cu" -> Color.web("#c47b4b");
            case "Fe", "Ni" -> Color.web("#929fa3");
            case "C", "GRAFITO" -> Color.web("#454b50");
            case "DIAMANTE", "Si" -> Color.web("#c8f2ff");
            case "O2" -> Color.web("#e76e68");
            case "H2", "N2" -> Color.web("#d9e8ee");
            case "Cl2" -> Color.web("#c8d64e");
            case "S" -> Color.web("#f1d344");
            case "H2O" -> Color.web("#50b8e5");
            case "NaCl", "CaCO3", "Ca3PO42" -> Color.web("#e8e3cb");
            case "NaCl_AC", "CaOH2" -> Color.web("#a9dce1");
            case "CaO", "MgO", "Al2O3", "SiO2" -> Color.web("#d8d2bd");
            case "CO2", "NH3", "HCl", "CH4", "H2S", "SO2" -> Color.web("#c5dfe4");
            default -> switch (sustancia.getEstadoMateria()) {
                case SOLIDO -> Color.web("#bbc4c0");
                case LIQUIDO -> Color.web("#59bfce");
                case GASEOSO -> Color.web("#b9dce3");
                case PLASMA -> Color.web("#b18aff");
            };
        };
    }
}
