package com.chemlab.ui.components;

import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/** Cursores transparentes de guante abierto y cerrado para manipular muestras. */
public final class CursorGuante {

    private static final double TAMANIO = 40;
    private static Cursor abierto;
    private static Cursor cerrado;

    private CursorGuante() {}

    public static Cursor abierto() {
        if (abierto == null) abierto = crear(false);
        return abierto;
    }

    public static Cursor cerrado() {
        if (cerrado == null) cerrado = crear(true);
        return cerrado;
    }

    private static Cursor crear(boolean puño) {
        Canvas dibujo = new Canvas(TAMANIO, TAMANIO);
        GraphicsContext gc = dibujo.getGraphicsContext2D();
        // La transparencia explícita evita que el snapshot del Canvas genere un cuadro blanco.
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);

        gc.save();
        gc.translate(1.2, 1.8);
        dibujarSilueta(gc, puño, Color.web("#06141d", 0.75), false);
        gc.restore();
        dibujarSilueta(gc, puño, Color.web("#e8f3ed"), true);
        dibujarCosturas(gc, puño);

        WritableImage imagen = new WritableImage((int) TAMANIO, (int) TAMANIO);
        dibujo.snapshot(parametros, imagen);
        return new ImageCursor(imagen, 6, 4);
    }

    private static void dibujarSilueta(GraphicsContext gc, boolean puño, Color color, boolean contorno) {
        gc.beginPath();
        if (puño) {
            gc.moveTo(13, 36);
            gc.lineTo(9, 30);
            gc.lineTo(5.8, 25.4);
            gc.bezierCurveTo(3.4, 22, 4.7, 18.2, 8, 17.7);
            gc.bezierCurveTo(10, 17.3, 12, 18.6, 14.6, 21.2);
            gc.lineTo(14.6, 11.2);
            gc.bezierCurveTo(14.6, 6.2, 21, 5.9, 21, 10.6);
            gc.lineTo(21, 17.6);
            gc.lineTo(22, 8.7);
            gc.bezierCurveTo(22.4, 4.4, 28.4, 5.2, 28.1, 9.7);
            gc.lineTo(27.5, 17.6);
            gc.lineTo(29.7, 12.8);
            gc.bezierCurveTo(31.4, 9.3, 36.5, 11, 34.9, 15.1);
            gc.lineTo(31.2, 25.2);
            gc.bezierCurveTo(29.5, 29.8, 26.3, 32.7, 22.8, 34.2);
            gc.lineTo(22.8, 37.2);
            gc.lineTo(13, 37.2);
        } else {
            gc.moveTo(12.7, 36.5);
            gc.lineTo(8.1, 29.5);
            gc.lineTo(5.8, 25.4);
            gc.bezierCurveTo(3.8, 22, 5.2, 18.5, 8, 18);
            gc.bezierCurveTo(10.2, 17.6, 12.1, 19.3, 14.2, 22);
            gc.lineTo(14.2, 8.8);
            gc.bezierCurveTo(14.2, 3.8, 20.5, 3.7, 20.5, 8.6);
            gc.lineTo(20.5, 19.4);
            gc.lineTo(21.5, 6.5);
            gc.bezierCurveTo(21.8, 2.3, 28, 2.7, 27.7, 7);
            gc.lineTo(26.8, 19.2);
            gc.lineTo(29.2, 9.5);
            gc.bezierCurveTo(30.3, 5.3, 36.3, 6.6, 35.1, 11.1);
            gc.lineTo(31.7, 24.3);
            gc.bezierCurveTo(30.4, 29.1, 27.6, 32.6, 23.1, 34.3);
            gc.lineTo(23.1, 37.4);
            gc.lineTo(13.4, 37.4);
        }
        gc.closePath();
        if (contorno) {
            gc.setFill(color);
            gc.fill();
            gc.setStroke(Color.web("#102b35"));
            gc.setLineWidth(2.4);
            gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
            gc.stroke();
        } else {
            gc.setFill(color);
            gc.fill();
        }
    }

    private static void dibujarCosturas(GraphicsContext gc, boolean puño) {
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setStroke(Color.web("#4fb7b1"));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(12.5, 31.5, 12.5, 5.2, 2, 2);
        gc.strokeLine(13.2, 21.7, 14.5, 25.4);
        gc.strokeLine(20.8, puño ? 13.1 : 12.7, 20.8, 18.1);
        gc.strokeLine(27.1, 12.7, 26.3, 17.2);
        gc.setStroke(Color.web("#ffffff", 0.9));
        gc.setLineWidth(1.4);
        gc.strokeLine(16.1, puño ? 10.5 : 7.8, 16.1, puño ? 17 : 17.8);
        gc.strokeLine(23.5, 6.3, 23, 14.5);
    }
}
