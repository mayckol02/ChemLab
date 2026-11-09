package com.chemlab.ui;

import com.chemlab.game.PerfilJugador;
import com.chemlab.repository.RepositorioPerfilesJson;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Pantalla inicial para crear o elegir el perfil de juego.
 */
public final class SelectorPerfil extends StackPane {

    private final RepositorioPerfilesJson repositorio;
    private final Consumer<PerfilJugador> alSeleccionar;
    private final ListView<PerfilJugador> listaPerfiles = new ListView<>();
    private final TextField campoNombre = new TextField();
    private final Label mensaje = new Label();
    private final Button botonComenzar = new Button("Continuar");

    public SelectorPerfil(
            RepositorioPerfilesJson repositorio,
            Consumer<PerfilJugador> alSeleccionar
    ) {
        this.repositorio = Objects.requireNonNull(repositorio, "El repositorio no puede ser null");
        this.alSeleccionar = Objects.requireNonNull(alSeleccionar, "La acción de selección no puede ser null");
        getStyleClass().add("game-root");
        getStylesheets().add(getClass().getResource("/styles/game-2d.css").toExternalForm());

        Label titulo = new Label("¿Quién entra al laboratorio?");
        titulo.getStyleClass().add("game-menu-title");
        Label subtitulo = new Label("Selecciona un perfil o crea uno nuevo. Cada perfil guarda su propio progreso.");
        subtitulo.getStyleClass().add("game-menu-subtitle");
        subtitulo.setWrapText(true);

        listaPerfiles.setPlaceholder(new Label("Todavía no hay perfiles. Crea uno para empezar."));
        listaPerfiles.getStyleClass().add("game-profile-list");
        listaPerfiles.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(PerfilJugador perfil, boolean vacio) {
                super.updateItem(perfil, vacio);
                if (vacio || perfil == null) {
                    setText(null);
                } else {
                    setText(perfil.nombre() + "   ·   Misiones: "
                            + perfil.progreso().misionesCompletadas() + "/5   ·   Descubrimientos: "
                            + perfil.progreso().sustanciasDescubiertas().size());
                }
            }
        });
        VBox.setVgrow(listaPerfiles, Priority.ALWAYS);
        listaPerfiles.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) -> botonComenzar.setDisable(seleccionado == null));
        listaPerfiles.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2 && listaPerfiles.getSelectionModel().getSelectedItem() != null) {
                comenzar();
            }
        });

        campoNombre.setPromptText("Nombre del perfil (máximo 24 caracteres)");
        campoNombre.getStyleClass().add("game-profile-name-input");
        Button botonCrear = new Button("Crear perfil");
        botonCrear.getStyleClass().add("game-secondary-button");
        botonCrear.setOnAction(evento -> crearPerfil());
        campoNombre.setOnAction(evento -> crearPerfil());
        HBox filaCrear = new HBox(10, campoNombre, botonCrear);
        HBox.setHgrow(campoNombre, Priority.ALWAYS);

        mensaje.getStyleClass().add("game-empty-state");
        mensaje.setWrapText(true);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        botonComenzar.getStyleClass().add("game-start-button");
        botonComenzar.setDisable(true);
        botonComenzar.setOnAction(evento -> comenzar());
        HBox acciones = new HBox(10, espacio, botonComenzar);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        VBox tarjeta = new VBox(16, titulo, subtitulo, listaPerfiles, filaCrear, mensaje, acciones);
        tarjeta.getStyleClass().add("game-menu-card");
        tarjeta.setMaxWidth(720);
        tarjeta.setPrefHeight(540);
        tarjeta.setPadding(new Insets(28));
        getChildren().add(tarjeta);
        setPadding(new Insets(24));
        recargarPerfiles();
    }

    private void crearPerfil() {
        try {
            PerfilJugador perfil = repositorio.crear(campoNombre.getText());
            listaPerfiles.getItems().add(perfil);
            listaPerfiles.getSelectionModel().select(perfil);
            campoNombre.clear();
            mensaje.setText("Perfil creado. Pulsa Continuar para entrar.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mensaje.setText(e.getMessage());
        }
    }

    private void recargarPerfiles() {
        try {
            listaPerfiles.getItems().setAll(repositorio.listar());
            if (!listaPerfiles.getItems().isEmpty()) {
                listaPerfiles.getSelectionModel().selectFirst();
            }
        } catch (IllegalStateException e) {
            mensaje.setText("No se pudieron cargar los perfiles: " + e.getMessage());
        }
    }

    private void comenzar() {
        PerfilJugador seleccionado = listaPerfiles.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            alSeleccionar.accept(seleccionado);
        }
    }
}
