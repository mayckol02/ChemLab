package com.chemlab;

import com.chemlab.ui.game.VistaJuego2D;
import com.chemlab.ui.IntroChemLab;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

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
        URL logoResource = Main.class.getResource("/images/chemlab-logo.png");
        if (logoResource == null) {
            throw new IllegalStateException("No se encontró el icono de ChemLab");
        }
        stage.getIcons().add(new Image(logoResource.toExternalForm()));

        stage.setTitle("ChemLab - Laboratorio 2D");
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        IntroChemLab intro = new IntroChemLab(() -> {
            VistaJuego2D vista = new VistaJuego2D();
            stage.getScene().setRoot(vista);
            vista.requestFocus();
        });
        Scene scene = new Scene(intro, ANCHO, ALTO);
        stage.setScene(scene);
        stage.show();
        intro.iniciar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
