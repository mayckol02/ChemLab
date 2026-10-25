package com.chemlab.ui.grammar;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.grammar.CompiladorGramaticaRegular;
import com.chemlab.grammar.GramaticaRegular;
import com.chemlab.grammar.ProduccionRegular;
import com.chemlab.grammar.SimboloNoTerminal;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Inventario;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Práctica guiada para derivar secuencias de acciones y seguir el autómata equivalente. */
public final class VistaTallerGramatica extends VBox {

    private final List<GramaticaRegular> gramaticas;
    private final ComboBox<GramaticaRegular> selectorGramatica;
    private final VBox opcionesProduccion = new VBox(6);
    private final VBox historialDerivacion = new VBox(6);
    private final Label simboloActual = new Label();
    private final Label estadoAutomata = new Label();
    private final Label palabraGenerada = new Label();
    private final Label feedback = new Label();
    private final Button reiniciar = new Button("REINICIAR PRÁCTICA");

    private GramaticaRegular gramatica;
    private AutomataQuimico automata;
    private SimboloNoTerminal noTerminalActual;
    private final List<AccionLaboratorio> accionesAplicadas = new ArrayList<>();
    private final List<ProduccionRegular> derivacion = new ArrayList<>();
    private boolean terminada;

    public VistaTallerGramatica(List<GramaticaRegular> gramaticas) {
        this.gramaticas = List.copyOf(Objects.requireNonNull(gramaticas, "Las gramáticas son obligatorias"));
        if (this.gramaticas.isEmpty()) throw new IllegalArgumentException("Se requiere al menos una gramática");

        getStyleClass().add("game-grammar-workshop");
        setSpacing(12);
        setPadding(new Insets(6));

        Label titulo = new Label("TALLER DE DERIVACIONES");
        titulo.getStyleClass().add("game-section-title");
        Label descripcion = new Label("Aplica producciones desde el símbolo actual. Cada terminal ejecuta la misma acción que mueve el autómata.");
        descripcion.getStyleClass().add("game-menu-subtitle");
        descripcion.setWrapText(true);

        selectorGramatica = new ComboBox<>(FXCollections.observableArrayList(this.gramaticas));
        selectorGramatica.setConverter(new StringConverter<>() {
            @Override
            public String toString(GramaticaRegular valor) {
                return valor == null ? "" : nombreGramatica(valor);
            }

            @Override
            public GramaticaRegular fromString(String texto) {
                return gramaticas.stream().filter(valor -> nombreGramatica(valor).equals(texto))
                        .findFirst().orElse(null);
            }
        });
        selectorGramatica.getStyleClass().add("game-grammar-selector");
        selectorGramatica.setPrefWidth(260);
        selectorGramatica.valueProperty().addListener((observable, anterior, actual) -> iniciar(actual));

        HBox selector = new HBox(12, new VBox(4, titulo, descripcion), selectorGramatica);
        selector.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(selector.getChildren().get(0), Priority.ALWAYS);

        VBox panelEstado = new VBox(6);
        panelEstado.getStyleClass().add("game-grammar-state-card");
        Label tituloEstado = new Label("SÍMBOLO NO TERMINAL ACTUAL");
        tituloEstado.getStyleClass().add("game-grammar-micro");
        simboloActual.getStyleClass().add("game-grammar-state-value");
        panelEstado.getChildren().addAll(tituloEstado, simboloActual);

        VBox panelAutomata = new VBox(6);
        panelAutomata.getStyleClass().add("game-grammar-state-card");
        Label tituloAutomata = new Label("ESTADO DEL AUTÓMATA");
        tituloAutomata.getStyleClass().add("game-grammar-micro");
        estadoAutomata.getStyleClass().add("game-grammar-state-value");
        panelAutomata.getChildren().addAll(tituloAutomata, estadoAutomata);

        HBox estados = new HBox(10, panelEstado, panelAutomata);
        HBox.setHgrow(panelEstado, Priority.ALWAYS);
        HBox.setHgrow(panelAutomata, Priority.ALWAYS);

        Label tituloProducciones = new Label("ELIGE UNA PRODUCCIÓN");
        tituloProducciones.getStyleClass().add("game-section-title");
        ScrollPane scrollProducciones = crearScroll(opcionesProduccion);
        VBox panelProducciones = new VBox(7, tituloProducciones, scrollProducciones);
        panelProducciones.getStyleClass().add("game-grammar-exercise-panel");
        VBox.setVgrow(scrollProducciones, Priority.ALWAYS);

        Label tituloDerivacion = new Label("DERIVACIÓN CONSTRUIDA");
        tituloDerivacion.getStyleClass().add("game-section-title");
        palabraGenerada.getStyleClass().add("game-grammar-word");
        palabraGenerada.setWrapText(true);
        ScrollPane scrollDerivacion = crearScroll(historialDerivacion);
        VBox panelDerivacion = new VBox(7, tituloDerivacion, scrollDerivacion, palabraGenerada);
        panelDerivacion.getStyleClass().add("game-grammar-exercise-panel");
        VBox.setVgrow(scrollDerivacion, Priority.ALWAYS);

        HBox columnas = new HBox(12, panelProducciones, panelDerivacion);
        HBox.setHgrow(panelProducciones, Priority.ALWAYS);
        HBox.setHgrow(panelDerivacion, Priority.ALWAYS);
        VBox.setVgrow(columnas, Priority.ALWAYS);

        feedback.getStyleClass().add("game-grammar-feedback");
        feedback.setWrapText(true);
        reiniciar.getStyleClass().add("game-secondary-button");
        reiniciar.setOnAction(evento -> iniciar(gramatica));
        HBox pie = new HBox(12, feedback, reiniciar);
        pie.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(feedback, Priority.ALWAYS);

        getChildren().addAll(selector, estados, columnas, pie);
        selectorGramatica.setValue(this.gramaticas.get(0));
    }

    private ScrollPane crearScroll(VBox contenido) {
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("game-grammar-scroll");
        return scroll;
    }

    private String nombreGramatica(GramaticaRegular valor) {
        return switch (valor.getId()) {
            case "R_H2O" -> "Síntesis de agua · R_H2O";
            case "R_DIAMANTE_CVD" -> "Diamante CVD · R_DIAMANTE_CVD";
            default -> valor.getId();
        };
    }

    private void iniciar(GramaticaRegular nueva) {
        if (nueva == null) return;
        gramatica = nueva;
        automata = CompiladorGramaticaRegular.compilar(gramatica);
        noTerminalActual = gramatica.getInicial();
        accionesAplicadas.clear();
        derivacion.clear();
        terminada = false;
        feedback.setText("Empieza en " + noTerminalActual.id() + " y busca una producción cuyo lado izquierdo coincida.");
        feedback.getStyleClass().setAll("game-grammar-feedback", "game-grammar-hint");
        actualizarVista();
    }

    private void aplicar(ProduccionRegular produccion) {
        if (terminada) {
            informarError("La derivación ya terminó. Reinicia para practicar de nuevo.");
            return;
        }
        if (noTerminalActual == null || !produccion.origen().id().equals(noTerminalActual.id())) {
            informarError("No se puede aplicar " + produccion.origen().id() + " aquí; el símbolo actual es "
                    + (noTerminalActual == null ? "ε" : noTerminalActual.id()) + ".");
            return;
        }

        if (produccion.esEpsilon()) {
            if (gramatica.buscarEpsilon(noTerminalActual.id()) == null || !automata.esEstadoFinalActual()) {
                informarError("ε solo cierra la derivación en un estado final del autómata.");
                return;
            }
            derivacion.add(produccion);
            noTerminalActual = null;
            terminada = true;
            feedback.setText("¡Derivación completa! La palabra de acciones fue aceptada por el autómata.");
            feedback.getStyleClass().setAll("game-grammar-feedback", "game-grammar-good");
            actualizarVista();
            return;
        }

        String estadoOrigen = automata.getEstadoActual().getId();
        try {
            automata.transicionar(produccion.terminal(),
                    new CondicionesExperimentales(25, 1, 7, Atmosfera.AIRE), new Inventario());
        } catch (IllegalStateException error) {
            informarError("El autómata no permite " + produccion.terminal() + " desde su estado actual.");
            return;
        }

        derivacion.add(produccion);
        accionesAplicadas.add(produccion.terminal());
        String estadoDestino = automata.getEstadoActual().getId();
        noTerminalActual = produccion.siguiente() == null
                ? null : gramatica.buscarNoTerminal(produccion.siguiente().id());
        if (noTerminalActual == null) {
            terminada = true;
            feedback.setText("¡Derivación completa! El autómata llegó a un estado de aceptación.");
            feedback.getStyleClass().setAll("game-grammar-feedback", "game-grammar-good");
        } else if (automata.esEstadoFinalActual()) {
            feedback.setText("Autómata: " + estadoOrigen + " —" + produccion.terminal() + "→ "
                    + estadoDestino + ". Aplica ε para cerrar la derivación.");
            feedback.getStyleClass().setAll("game-grammar-feedback", "game-grammar-hint");
        } else {
            feedback.setText("Autómata: " + estadoOrigen + " —" + produccion.terminal() + "→ "
                    + estadoDestino + ". Gramática: símbolo actual " + noTerminalActual.id() + ".");
            feedback.getStyleClass().setAll("game-grammar-feedback", "game-grammar-good");
        }
        actualizarVista();
    }

    private void actualizarVista() {
        simboloActual.setText(noTerminalActual == null
                ? "ε · derivación terminada"
                : noTerminalActual.id() + " · " + noTerminalActual.nombre());
        estadoAutomata.setText(automata.getEstadoActual().getId()
                + " · " + automata.getEstadoActual().getNombre()
                + (automata.esEstadoFinalActual() ? " · FINAL" : ""));

        opcionesProduccion.getChildren().clear();
        for (ProduccionRegular produccion : gramatica.getProducciones()) {
            Button opcion = new Button(formatear(produccion));
            opcion.getStyleClass().add("game-grammar-production-option");
            opcion.setMaxWidth(Double.MAX_VALUE);
            opcion.setAlignment(Pos.CENTER_LEFT);
            opcion.setDisable(terminada);
            opcion.setOnAction(evento -> aplicar(produccion));
            opcionesProduccion.getChildren().add(opcion);
        }

        historialDerivacion.getChildren().clear();
        if (derivacion.isEmpty()) {
            Label vacia = new Label("Aún no has aplicado producciones.");
            vacia.getStyleClass().add("game-menu-subtitle");
            historialDerivacion.getChildren().add(vacia);
        } else {
            for (int indice = 0; indice < derivacion.size(); indice++) {
                Label paso = new Label((indice + 1) + ".  " + formatear(derivacion.get(indice)));
                paso.getStyleClass().add("game-grammar-derivation-step");
                paso.setWrapText(true);
                historialDerivacion.getChildren().add(paso);
            }
        }
        String palabra = accionesAplicadas.stream().map(AccionLaboratorio::name)
                .collect(Collectors.joining(" · "));
        palabraGenerada.setText("Palabra: " + (palabra.isBlank() ? "ε" : palabra));
    }

    private String formatear(ProduccionRegular produccion) {
        if (produccion.esEpsilon()) return produccion.origen().id() + " → ε";
        return produccion.origen().id() + " → " + produccion.terminal().name()
                + (produccion.siguiente() == null ? "" : " " + produccion.siguiente().id());
    }

    private void informarError(String texto) {
        feedback.setText(texto);
        feedback.getStyleClass().setAll("game-grammar-feedback", "game-grammar-error");
    }
}
