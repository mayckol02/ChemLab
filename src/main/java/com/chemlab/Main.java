package com.chemlab;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/** Punto de entrada de ChemLab. */
public final class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("ChemLab");
        stage.setScene(new Scene(new Label("ChemLab - estructura inicial"), 900, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
