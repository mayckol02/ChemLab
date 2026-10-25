package com.chemlab.ui.grammar;

import com.chemlab.grammar.formula.AnalizadorFormulaQuimica;
import com.chemlab.grammar.formula.FormulaInvalidaException;
import com.chemlab.grammar.formula.NodoDerivacionFormula;
import com.chemlab.grammar.formula.ResultadoAnalisisFormula;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Lección de gramática libre de contexto con árbol y conteo de átomos. */
public final class VistaAnalizadorFormula extends VBox {

    private final AnalizadorFormulaQuimica analizador = new AnalizadorFormulaQuimica();
    private final TextField entradaFormula = new TextField();
    private final Label estado = new Label();
    private final Label formulaNormalizada = new Label();
    private final Label totalAtomos = new Label();
    private final TreeView<String> arbol = new TreeView<>();
    private final FlowPane resumenAtomos = new FlowPane(8, 8);
    private int numeroAnalisis;
    private String textoAnalizado = "";

    public VistaAnalizadorFormula() {
        getStyleClass().add("game-formula-analyzer");
        setSpacing(10);
        setPadding(new Insets(8));

        Label titulo = new Label("GRAMÁTICA LIBRE DE CONTEXTO · FÓRMULAS");
        titulo.getStyleClass().add("game-section-title");
        Label ayuda = new Label("Analiza elementos, subíndices y grupos anidados. Prueba Ca(OH)₂ o Ca₃(PO₄)₂.");
        ayuda.getStyleClass().add("game-menu-subtitle");
        ayuda.setWrapText(true);

        Label regla = new Label("F → Secuencia\nSecuencia → Término Secuencia | Término\n"
                + "Término → Elemento Subíndice? | ( Secuencia ) Subíndice?\n"
                + "Elemento → Mayúscula Minúscula?   ·   Subíndice → Dígito+");
        regla.getStyleClass().add("game-grammar-definition");
        regla.setWrapText(true);

        entradaFormula.setPromptText("Escribe una fórmula, por ejemplo: Al₂(SO₄)₃");
        entradaFormula.getStyleClass().add("game-experiment-search");
        entradaFormula.setOnAction(evento -> analizar());
        Button analizar = new Button("ANALIZAR FÓRMULA");
        analizar.getStyleClass().add("game-start-button");
        analizar.setAccessibleText("Analizar la fórmula y actualizar el árbol de derivación y el conteo de átomos");
        analizar.setOnAction(evento -> {
            analizar();
            entradaFormula.requestFocus();
        });
        HBox filaEntrada = new HBox(10, entradaFormula, analizar);
        filaEntrada.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(entradaFormula, Priority.ALWAYS);

        FlowPane ejemplos = new FlowPane(8, 6);
        ejemplos.getChildren().addAll(
                crearEjemplo("H₂O", "H₂O"),
                crearEjemplo("Ca(OH)₂", "Ca(OH)₂"),
                crearEjemplo("Ca₃(PO₄)₂", "Ca₃(PO₄)₂"),
                crearEjemplo("Al₂(SO₄)₃", "Al₂(SO₄)₃")
        );

        estado.getStyleClass().add("game-formula-status");
        estado.setWrapText(true);
        entradaFormula.textProperty().addListener((observable, anterior, actual) -> {
            if (actual != null && !actual.equals(textoAnalizado)) {
                estado.setText("Fórmula editada · pulsa ANALIZAR FÓRMULA para actualizar el árbol y el conteo.");
                estado.getStyleClass().setAll("game-formula-status", "game-grammar-hint");
            }
        });
        formulaNormalizada.getStyleClass().add("game-menu-subtitle");
        totalAtomos.getStyleClass().add("game-section-title");

        arbol.setShowRoot(true);
        arbol.getStyleClass().add("game-formula-tree");
        arbol.setPrefHeight(310);
        arbol.setMinWidth(300);
        VBox panelArbol = new VBox(7, new Label("ÁRBOL DE DERIVACIÓN"), arbol);
        panelArbol.getStyleClass().add("game-formula-result-panel");
        ((Label) panelArbol.getChildren().get(0)).getStyleClass().add("game-grammar-micro");
        VBox.setVgrow(arbol, Priority.ALWAYS);

        VBox panelAtomos = new VBox(9, new Label("ÁTOMOS POR ELEMENTO"), resumenAtomos, totalAtomos,
                formulaNormalizada);
        panelAtomos.getStyleClass().add("game-formula-result-panel");
        ((Label) panelAtomos.getChildren().get(0)).getStyleClass().add("game-grammar-micro");
        panelAtomos.setMinWidth(205);
        resumenAtomos.setAlignment(Pos.TOP_LEFT);

        HBox resultados = new HBox(10, panelArbol, panelAtomos);
        HBox.setHgrow(panelArbol, Priority.ALWAYS);
        VBox.setVgrow(resultados, Priority.ALWAYS);

        getChildren().addAll(titulo, ayuda, regla, filaEntrada, ejemplos, estado, resultados);
        VBox.setVgrow(resultados, Priority.ALWAYS);
        entradaFormula.setText("Ca(OH)₂");
        analizar();
    }

    private Button crearEjemplo(String texto, String formula) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("game-grammar-example");
        boton.setOnAction(evento -> {
            entradaFormula.setText(formula);
            analizar();
        });
        return boton;
    }

    private void analizar() {
        numeroAnalisis++;
        textoAnalizado = entradaFormula.getText() == null ? "" : entradaFormula.getText();
        try {
            ResultadoAnalisisFormula resultado = analizador.analizar(entradaFormula.getText());
            arbol.setRoot(crearRama(resultado.arbol()));
            expandir(arbol.getRoot());
            resumenAtomos.getChildren().clear();
            resultado.atomos().forEach((elemento, cantidad) ->
                    resumenAtomos.getChildren().add(crearFichaAtomo(elemento, cantidad)));
            int total = resultado.atomos().values().stream().mapToInt(Integer::intValue).sum();
            totalAtomos.setText("Total: " + total + (total == 1 ? " átomo" : " átomos"));
            formulaNormalizada.setText("Forma normalizada: " + resultado.formulaNormalizada());
            estado.setText("Análisis " + numeroAnalisis
                    + " · fórmula válida · los grupos y subíndices se derivaron correctamente.");
            estado.getStyleClass().setAll("game-formula-status", "game-grammar-good");
        } catch (FormulaInvalidaException error) {
            arbol.setRoot(null);
            resumenAtomos.getChildren().clear();
            totalAtomos.setText("");
            formulaNormalizada.setText("");
            estado.setText("Análisis " + numeroAnalisis + " · fórmula inválida · posición "
                    + (error.getPosicion() + 1) + ": " + error.getMessage());
            estado.getStyleClass().setAll("game-formula-status", "game-grammar-error");
        }
    }

    private TreeItem<String> crearRama(NodoDerivacionFormula nodo) {
        TreeItem<String> item = new TreeItem<>(nodo.etiqueta());
        nodo.hijos().stream().map(this::crearRama).forEach(item.getChildren()::add);
        return item;
    }

    private void expandir(TreeItem<String> nodo) {
        if (nodo == null) return;
        nodo.setExpanded(true);
        nodo.getChildren().forEach(this::expandir);
    }

    private VBox crearFichaAtomo(String elemento, int cantidad) {
        Label simbolo = new Label(elemento);
        simbolo.getStyleClass().add("game-formula-atom-symbol");
        Label conteo = new Label(Integer.toString(cantidad));
        conteo.getStyleClass().add("game-formula-atom-count");
        VBox ficha = new VBox(2, simbolo, conteo);
        ficha.getStyleClass().add("game-formula-atom-card");
        ficha.setAlignment(Pos.CENTER);
        return ficha;
    }
}
