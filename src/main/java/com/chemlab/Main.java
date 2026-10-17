package com.chemlab;

import com.chemlab.ui.VistaLaboratorio;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de ChemLab.
 *
 * <p>La clase {@link Main} solo instancia la aplicación JavaFX;
 * toda la lógica de UI vive en {@link VistaLaboratorio} y el
 * dominio en los paquetes correspondientes.</p>
 */
public final class Main extends Application {

    private static final double ANCHO = 1100;
    private static final double ALTO = 700;

    @Override
    public void start(Stage stage) {
        VistaLaboratorio vista = new VistaLaboratorio();

        Scene scene = new Scene(vista, ANCHO, ALTO);

        stage.setTitle("ChemLab - Laboratorio Químico Educativo");
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
