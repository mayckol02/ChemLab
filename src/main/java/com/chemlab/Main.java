package com.chemlab;

import com.chemlab.ui.game.VistaJuego2D;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de ChemLab.
 *
 * <p>La clase {@link Main} solo instancia la aplicación JavaFX;
 * toda la lógica de UI vive en {@link VistaJuego2D} y el
 * dominio en los paquetes correspondientes.</p>
 */
public final class Main extends Application {

    private static final double ANCHO = 1280;
    private static final double ALTO = 720;

    @Override
    public void start(Stage stage) {
        VistaJuego2D vista = new VistaJuego2D();

        Scene scene = new Scene(vista, ANCHO, ALTO);

        stage.setTitle("ChemLab - Laboratorio 2D");
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        stage.setScene(scene);
        stage.show();
        vista.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
