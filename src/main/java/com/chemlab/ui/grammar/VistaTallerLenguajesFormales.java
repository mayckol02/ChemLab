package com.chemlab.ui.grammar;

import com.chemlab.grammar.GramaticaRegular;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/** Sección del cuaderno para protocolos regulares y fórmulas libres de contexto. */
public final class VistaTallerLenguajesFormales extends VBox {

    private final Button botonProtocolos = new Button("PROTOCOLOS · REGULAR");
    private final Button botonFormulas = new Button("FÓRMULAS · LIBRE DE CONTEXTO");
    private final VistaTallerGramatica tallerProtocolos;
    private final VistaAnalizadorFormula analizadorFormulas = new VistaAnalizadorFormula();
    private final ScrollPane desplazamientoFormulas;

    public VistaTallerLenguajesFormales(List<GramaticaRegular> gramaticas) {
        tallerProtocolos = new VistaTallerGramatica(gramaticas);
        desplazamientoFormulas = new ScrollPane(analizadorFormulas);
        desplazamientoFormulas.setFitToWidth(true);
        desplazamientoFormulas.setPannable(true);
        desplazamientoFormulas.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamientoFormulas.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        desplazamientoFormulas.setMinHeight(0);
        desplazamientoFormulas.getStyleClass().add("game-formula-scroll");
        getStyleClass().add("game-formal-languages-workshop");
        setSpacing(9);
        setPadding(new Insets(4));
        setMinHeight(0);

        botonProtocolos.getStyleClass().add("game-grammar-subtab");
        botonFormulas.getStyleClass().add("game-grammar-subtab");
        HBox pestañas = new HBox(8, botonProtocolos, botonFormulas);
        pestañas.setAlignment(Pos.CENTER_LEFT);

        StackPane contenido = new StackPane(tallerProtocolos, desplazamientoFormulas);
        contenido.setMinHeight(0);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        botonProtocolos.setOnAction(evento -> mostrarProtocolos());
        botonFormulas.setOnAction(evento -> mostrarFormulas());
        getChildren().addAll(pestañas, contenido);
        mostrarProtocolos();
    }

    private void mostrarProtocolos() {
        tallerProtocolos.setVisible(true);
        tallerProtocolos.setManaged(true);
        desplazamientoFormulas.setVisible(false);
        desplazamientoFormulas.setManaged(false);
        botonProtocolos.getStyleClass().setAll("game-grammar-subtab", "game-grammar-subtab-selected");
        botonFormulas.getStyleClass().setAll("game-grammar-subtab");
    }

    private void mostrarFormulas() {
        tallerProtocolos.setVisible(false);
        tallerProtocolos.setManaged(false);
        desplazamientoFormulas.setVisible(true);
        desplazamientoFormulas.setManaged(true);
        botonProtocolos.getStyleClass().setAll("game-grammar-subtab");
        botonFormulas.getStyleClass().setAll("game-grammar-subtab", "game-grammar-subtab-selected");
    }
}
