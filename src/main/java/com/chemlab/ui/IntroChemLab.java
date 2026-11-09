package com.chemlab.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.util.Duration;

import java.net.URL;
import java.util.Objects;

public final class IntroChemLab extends StackPane {

    private static final String LOGO_RESOURCE = "/images/chemlab-logo.png";
    private static final String AUDIO_RESOURCE = "/audio/intro.mp3";

    private final Runnable alTerminar;
    private final ImageView logo;
    private final Pane tablaPeriodica;
    private final Pane fichasAnimadas;
    private final StackPane fichaCarbono;
    private final StackPane fichaLantano;
    private final Label subtitulo;
    private final Label indicacion;
    private final Label audioInfo;
    private final Timeline animacion;
    private final EventHandler<KeyEvent> manejadorTeclado = this::alPresionarTecla;
    private Scene escena;
    private MediaPlayer reproductor;
    private boolean finalizada;

    public IntroChemLab(Runnable alTerminar) {
        this.alTerminar = Objects.requireNonNull(alTerminar, "La acción final es obligatoria");
        setFocusTraversable(true);
        setStyle("-fx-background-color: #050b10;");

        Circle halo = new Circle(310, Color.rgb(0, 164, 87, 0.045));
        Circle orbital = new Circle(270, Color.TRANSPARENT);
        orbital.setStroke(Color.rgb(85, 226, 161, 0.2));
        orbital.setStrokeWidth(1.2);
        Circle orbitDot = new Circle(4, Color.web("#63e6a3"));
        StackPane orbit = new StackPane(orbital, orbitDot);
        orbit.setRotate(-28);
        StackPane.setAlignment(orbitDot, Pos.TOP_CENTER);

        logo = new ImageView(cargarLogo());
        logo.setPreserveRatio(true);
        logo.setFitWidth(700);
        logo.setFitHeight(465);
        logo.setOpacity(0);
        logo.setSmooth(true);
        StackPane.setMargin(logo, new Insets(0, 24, 84, 24));

        tablaPeriodica = crearTablaPeriodica();
        tablaPeriodica.setOpacity(1);
        StackPane.setMargin(tablaPeriodica, new Insets(0, 24, 84, 24));

        fichasAnimadas = new Pane();
        fichasAnimadas.setPrefSize(700, 465);
        fichasAnimadas.setMinSize(700, 465);
        fichasAnimadas.setMaxSize(700, 465);
        StackPane.setMargin(fichasAnimadas, new Insets(0, 24, 84, 24));

        fichaCarbono = crearFicha("6", "C", 193);
        fichaLantano = crearFicha("57", "La", 193);
        fichaCarbono.setLayoutX(310);
        fichaCarbono.setLayoutY(48);
        fichaCarbono.setScaleX(0.32);
        fichaCarbono.setScaleY(0.32);
        fichaLantano.setLayoutX(434);
        fichaLantano.setLayoutY(234);
        fichaLantano.setScaleX(0.32);
        fichaLantano.setScaleY(0.32);
        fichasAnimadas.getChildren().addAll(fichaCarbono, fichaLantano);

        Label marca = new Label("CIENCIA EN MOVIMIENTO");
        marca.setTextFill(Color.web("#91a9a8"));
        marca.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        marca.setStyle("-fx-letter-spacing: 3px;");
        StackPane.setAlignment(marca, Pos.TOP_CENTER);
        StackPane.setMargin(marca, new Insets(64, 0, 0, 0));
        marca.setOpacity(0);

        subtitulo = new Label("EXPLORA · EXPERIMENTA · DESCUBRE");
        subtitulo.setTextFill(Color.web("#d8e8e1"));
        subtitulo.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        subtitulo.setStyle("-fx-letter-spacing: 2px;");
        StackPane.setAlignment(subtitulo, Pos.BOTTOM_CENTER);
        StackPane.setMargin(subtitulo, new Insets(0, 16, 102, 16));
        subtitulo.setOpacity(0);

        indicacion = new Label("Presiona ESPACIO o ENTER para continuar");
        indicacion.setTextFill(Color.web("#819493"));
        indicacion.setFont(Font.font("Segoe UI", 11));
        indicacion.setOpacity(1);

        audioInfo = new Label();
        audioInfo.setTextFill(Color.web("#617472"));
        audioInfo.setFont(Font.font("Segoe UI", 9));
        audioInfo.setOpacity(0);
        javafx.scene.layout.VBox pie = new javafx.scene.layout.VBox(6, indicacion, audioInfo);
        pie.setAlignment(Pos.CENTER);
        StackPane.setAlignment(pie, Pos.BOTTOM_CENTER);
        StackPane.setMargin(pie, new Insets(0, 16, 36, 16));

        getChildren().addAll(halo, orbit, tablaPeriodica, logo, fichasAnimadas,
                marca, subtitulo, pie);

        animacion = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(logo.opacityProperty(), 0),
                        new KeyValue(tablaPeriodica.opacityProperty(), 1),
                        new KeyValue(fichaCarbono.translateXProperty(), 0),
                        new KeyValue(fichaCarbono.translateYProperty(), 0),
                        new KeyValue(fichaCarbono.scaleXProperty(), 0.32),
                        new KeyValue(fichaCarbono.scaleYProperty(), 0.32),
                        new KeyValue(fichaLantano.translateXProperty(), 0),
                        new KeyValue(fichaLantano.translateYProperty(), 0),
                        new KeyValue(fichaLantano.scaleXProperty(), 0.32),
                        new KeyValue(fichaLantano.scaleYProperty(), 0.32),
                        new KeyValue(marca.opacityProperty(), 0),
                        new KeyValue(subtitulo.opacityProperty(), 0),
                        new KeyValue(audioInfo.opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(1.1),
                        new KeyValue(fichaCarbono.translateXProperty(), -280, Interpolator.EASE_BOTH),
                        new KeyValue(fichaCarbono.translateYProperty(), -6, Interpolator.EASE_BOTH),
                        new KeyValue(fichaCarbono.scaleXProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(fichaCarbono.scaleYProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(fichaLantano.translateXProperty(), -214, Interpolator.EASE_BOTH),
                        new KeyValue(fichaLantano.translateYProperty(), -2, Interpolator.EASE_BOTH),
                        new KeyValue(fichaLantano.scaleXProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(fichaLantano.scaleYProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(marca.opacityProperty(), 1, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(2.2),
                        new KeyValue(subtitulo.opacityProperty(), 1, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(2.5),
                        new KeyValue(tablaPeriodica.opacityProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(fichaCarbono.opacityProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(fichaLantano.opacityProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(logo.opacityProperty(), 1, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(3.2),
                        new KeyValue(audioInfo.opacityProperty(), 1, Interpolator.EASE_OUT)));
    }

    private static Pane crearTablaPeriodica() {
        Pane tabla = new Pane();
        tabla.setPrefSize(700, 465);
        tabla.setMinSize(700, 465);
        tabla.setMaxSize(700, 465);

        Label titulo = new Label("TABLA PERIÓDICA");
        titulo.setTextFill(Color.rgb(203, 221, 214, 0.62));
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        titulo.setLayoutX(270);
        titulo.setLayoutY(2);
        tabla.getChildren().add(titulo);

        int[] numerosAtomicos = {1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20, 26, 28, 29, 30, 47};
        String[] simbolos = {
                "H", "He", "Li", "Be", "B", "N", "O", "F",
                "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl",
                "Ar", "K", "Ca", "Fe", "Ni", "Cu", "Zn", "Ag"
        };
        for (int i = 0; i < simbolos.length; i++) {
            StackPane celda = crearFicha(Integer.toString(numerosAtomicos[i]), simbolos[i], 58);
            celda.setLayoutX(70 + (i % 8) * 70);
            celda.setLayoutY(38 + (i / 8) * 72);
            celda.setOpacity(0.55);
            tabla.getChildren().add(celda);
        }
        return tabla;
    }

    private static StackPane crearFicha(String numeroAtomico, String simbolo, double tamanio) {
        StackPane ficha = new StackPane();
        ficha.setPrefSize(tamanio, tamanio);
        ficha.setMinSize(tamanio, tamanio);
        ficha.setMaxSize(tamanio, tamanio);
        ficha.setStyle("-fx-background-color: linear-gradient(to bottom right, #075235, #08752e);"
                + "-fx-border-color: #f3fff7; -fx-border-width: 2;");

        Label numero = new Label(numeroAtomico);
        numero.setTextFill(Color.WHITE);
        numero.setFont(Font.font("Segoe UI", FontWeight.BOLD, tamanio * 0.14));
        StackPane.setAlignment(numero, Pos.TOP_RIGHT);
        StackPane.setMargin(numero, new Insets(tamanio * 0.04, tamanio * 0.07, 0, 0));

        Label textoSimbolo = new Label(simbolo);
        textoSimbolo.setTextFill(Color.WHITE);
        textoSimbolo.setFont(Font.font("Georgia", FontWeight.BOLD, tamanio * 0.57));
        textoSimbolo.setAlignment(Pos.CENTER);
        ficha.getChildren().addAll(textoSimbolo, numero);
        return ficha;
    }

    public void iniciar() {
        escena = Objects.requireNonNull(getScene(), "La intro debe estar asociada a una escena");
        escena.addEventFilter(KeyEvent.KEY_PRESSED, manejadorTeclado);
        iniciarAudio();
        animacion.playFromStart();
        requestFocus();
    }

    public void terminar() {
        if (finalizada) {
            return;
        }
        finalizada = true;
        animacion.stop();
        if (escena != null) {
            escena.removeEventFilter(KeyEvent.KEY_PRESSED, manejadorTeclado);
        }
        if (reproductor != null) {
            reproductor.stop();
            reproductor.dispose();
            reproductor = null;
        }
        alTerminar.run();
    }

    private void alPresionarTecla(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER) {
            terminar();
            event.consume();
        }
    }

    private void iniciarAudio() {
        URL recurso = getClass().getResource(AUDIO_RESOURCE);
        if (recurso == null) {
            audioInfo.setText("Pista propia opcional: src/main/resources/audio/intro.mp3");
            return;
        }

        Media media = new Media(recurso.toExternalForm());
        reproductor = new MediaPlayer(media);
        reproductor.setVolume(0.38);
        reproductor.setOnError(() -> {
            Throwable error = reproductor == null ? null : reproductor.getError();
            System.err.println("No se pudo reproducir el audio de la intro: "
                    + (error == null ? "error multimedia desconocido" : error.getMessage()));
        });
        reproductor.play();
    }

    private static Image cargarLogo() {
        URL recurso = IntroChemLab.class.getResource(LOGO_RESOURCE);
        if (recurso == null) {
            throw new IllegalStateException("No se encontró el recurso del logo: " + LOGO_RESOURCE);
        }
        Image imagen = new Image(recurso.toExternalForm());
        if (imagen.isError()) {
            throw new IllegalStateException("No se pudo cargar el logo de ChemLab", imagen.getException());
        }
        return imagen;
    }
}
