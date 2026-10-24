package com.chemlab.ui.components;

import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Objects;

/** Ilustración vectorial pequeña del recipiente y estado físico de una sustancia. */
public final class IconoSustancia extends Canvas {

    public IconoSustancia(Sustancia sustancia, double tamanio) {
        super(tamanio, tamanio);
        Objects.requireNonNull(sustancia, "La sustancia no puede ser null");
        setMouseTransparent(true);
        dibujar(sustancia, tamanio);
    }

    private void dibujar(Sustancia sustancia, double tamanio) {
        GraphicsContext gc = getGraphicsContext2D();
        double escala = tamanio / 40.0;
        gc.save();
        gc.scale(escala, escala);
        Color color = colorDeSustancia(sustancia);

        gc.setFill(Color.web("#08131b", 0.9));
        gc.fillRoundRect(1, 1, 38, 38, 10, 10);
        gc.setStroke(Color.web("#49636e", 0.8));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(1, 1, 38, 38, 10, 10);

        switch (sustancia.getEstadoMateria()) {
            case GASEOSO -> dibujarCilindro(gc, color);
            case LIQUIDO -> dibujarVaso(gc, color);
            case PLASMA -> dibujarAmpolla(gc, color);
            case SOLIDO -> dibujarFrascoSolido(gc, color);
        }
        gc.restore();
    }

    private Color colorDeSustancia(Sustancia sustancia) {
        Color colorQuimico = switch (sustancia.getId()) {
            case "Cl2" -> Color.web("#c8d84b");
            case "H2S" -> Color.web("#d6c66d");
            case "S" -> Color.web("#f1d339");
            case "Cu" -> Color.web("#c67b4c");
            case "Fe", "Ni", "Na", "Ca", "Mg", "Al" -> Color.web("#b9c7c9");
            case "Fe2O3", "FeOH3" -> Color.web("#ad5d49");
            case "CuO" -> Color.web("#3e4548");
            case "C", "GRAFITO" -> Color.web("#51565c");
            case "DIAMANTE", "Si", "SiO2" -> Color.web("#d4f1ff");
            case "NaCl", "CaO", "CaOH2", "CaCO3", "MgO", "Al2O3", "Ca3PO42" -> Color.web("#e0ddc8");
            case "H2O", "NaCl_AC" -> Color.web("#48aee0");
            case "H2", "O2", "N2", "CO2", "NH3", "HCl", "CH4", "SO2" -> Color.web("#b8dce3");
            default -> null;
        };
        if (colorQuimico != null) return colorQuimico;
        return switch (sustancia.getTipo()) {
            case ELEMENTO -> Color.web("#64c9df");
            case COMPUESTO -> Color.web("#58d0ad");
            case MEZCLA -> Color.web("#f0bb62");
        };
    }

    private void dibujarCilindro(GraphicsContext gc, Color color) {
        gc.setFill(Color.web("#a9c1c6"));
        gc.fillRoundRect(11, 10, 18, 25, 5, 5);
        gc.fillRoundRect(16, 6, 8, 6, 2, 2);
        gc.setFill(color);
        gc.fillRoundRect(13, 16, 14, 14, 3, 3);
        gc.setStroke(Color.web("#eaf7f5"));
        gc.setLineWidth(1.4);
        gc.strokeRoundRect(11, 10, 18, 25, 5, 5);
        gc.setStroke(Color.web("#d8e7e6"));
        gc.strokeLine(18, 5, 18, 2);
        gc.strokeLine(22, 5, 22, 2);
        gc.strokeLine(18, 3.5, 25, 3.5);
    }

    private void dibujarVaso(GraphicsContext gc, Color color) {
        gc.setFill(Color.web("#dff3f3", 0.2));
        gc.fillPolygon(new double[]{11, 29, 26, 14}, new double[]{9, 9, 34, 34}, 4);
        gc.setFill(Color.web("#88dafa", 0.8));
        gc.fillPolygon(new double[]{14, 26, 25, 15}, new double[]{23, 23, 32, 32}, 4);
        gc.setFill(color);
        gc.fillOval(15, 21, 10, 3);
        gc.setStroke(Color.web("#e8ffff"));
        gc.setLineWidth(1.7);
        gc.strokeLine(10, 8, 30, 8);
        gc.strokeLine(12, 9, 15, 34);
        gc.strokeLine(28, 9, 25, 34);
        gc.strokeLine(15, 34, 25, 34);
    }

    private void dibujarAmpolla(GraphicsContext gc, Color color) {
        gc.setFill(Color.web("#e1eef0", 0.28));
        gc.fillRoundRect(16, 5, 8, 29, 4, 4);
        gc.setFill(Color.web("#b875ff", 0.75));
        gc.fillOval(13, 14, 14, 16);
        gc.setFill(color);
        gc.fillOval(17, 18, 6, 8);
        gc.setStroke(Color.web("#eddbff"));
        gc.setLineWidth(1.4);
        gc.strokeRoundRect(16, 5, 8, 29, 4, 4);
    }

    private void dibujarFrascoSolido(GraphicsContext gc, Color color) {
        gc.setFill(Color.web("#dff3f3", 0.22));
        gc.fillRoundRect(12, 11, 16, 23, 4, 4);
        gc.fillRoundRect(16, 7, 8, 6, 2, 2);
        gc.setFill(color);
        gc.fillPolygon(new double[]{14, 19, 22, 26, 25, 15},
                new double[]{29, 21, 27, 22, 32, 32}, 6);
        gc.setStroke(Color.web("#e8ffff"));
        gc.setLineWidth(1.4);
        gc.strokeRoundRect(12, 11, 16, 23, 4, 4);
    }
}
