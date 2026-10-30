package com.chemlab.ui.notebook;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EstadoQuimico;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.game.SesionJuego;
import com.chemlab.game.procedure.ProtocoloProcedimiento;
import com.chemlab.grammar.formula.AnalizadorFormulaQuimica;
import com.chemlab.grammar.formula.FormulaInvalidaException;
import com.chemlab.grammar.formula.NomenclaturaQuimica;
import com.chemlab.grammar.formula.ResultadoAnalisisFormula;
import com.chemlab.grammar.formula.ResultadoNombreQuimico;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.RegistroExperimento;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.AccionLaboratorio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Cuaderno de síntesis, análisis de muestras y comparación de rutas químicas. */
public final class VistaCuadernoQuimico extends VBox {

    private enum Seccion { PROTOCOLOS, MUESTRAS, RUTAS }

    private record GrupoRutas(Sustancia compuesto, List<ProtocoloProcedimiento.InfoRutaSintesis> rutas) {
        @Override
        public String toString() {
            return compuesto.getNombre();
        }
    }

    private final CatalogoQuimico catalogo;
    private final SesionJuego sesion;
    private final List<Experimento> experimentos;
    private final List<Sustancia> productosAlmacenados;
    private final AnalizadorFormulaQuimica analizador = new AnalizadorFormulaQuimica();
    private final NomenclaturaQuimica nomenclatura = new NomenclaturaQuimica();
    private final VBox contenido = new VBox(10);
    private final Button botonProtocolos = crearPestana("PROTOCOLOS", Seccion.PROTOCOLOS);
    private final Button botonMuestras = crearPestana("ANÁLISIS DE MUESTRAS", Seccion.MUESTRAS);
    private final Button botonRutas = crearPestana("COMPARAR RUTAS", Seccion.RUTAS);
    private Seccion seccion = Seccion.PROTOCOLOS;

    public VistaCuadernoQuimico(
            CatalogoQuimico catalogo,
            SesionJuego sesion,
            List<Experimento> experimentos,
            List<Sustancia> productosAlmacenados
    ) {
        this.catalogo = Objects.requireNonNull(catalogo, "El catálogo es obligatorio");
        this.sesion = Objects.requireNonNull(sesion, "La sesión es obligatoria");
        this.experimentos = List.copyOf(experimentos);
        this.productosAlmacenados = List.copyOf(productosAlmacenados);

        getStyleClass().add("game-chemistry-notebook");
        setSpacing(9);
        setMinHeight(0);
        contenido.setMinHeight(0);
        VBox.setVgrow(contenido, Priority.ALWAYS);

        HBox pestañas = new HBox(8, botonProtocolos, botonMuestras, botonRutas);
        pestañas.getStyleClass().add("game-chemistry-tabs");
        pestañas.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(pestañas, contenido);
        mostrar(Seccion.PROTOCOLOS);
    }

    private Button crearPestana(String texto, Seccion destino) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("game-chemistry-tab");
        boton.setOnAction(evento -> mostrar(destino));
        return boton;
    }

    private void mostrar(Seccion nueva) {
        seccion = nueva;
        contenido.getChildren().clear();
        Node vista = switch (seccion) {
            case PROTOCOLOS -> construirProtocolos();
            case MUESTRAS -> construirAnalisisMuestras();
            case RUTAS -> construirComparadorRutas();
        };
        contenido.getChildren().add(vista);
        VBox.setVgrow(vista, Priority.ALWAYS);
        marcarPestana(botonProtocolos, seccion == Seccion.PROTOCOLOS);
        marcarPestana(botonMuestras, seccion == Seccion.MUESTRAS);
        marcarPestana(botonRutas, seccion == Seccion.RUTAS);
    }

    private void marcarPestana(Button boton, boolean seleccionada) {
        boton.getStyleClass().setAll("game-chemistry-tab");
        if (seleccionada) boton.getStyleClass().add("game-chemistry-tab-selected");
    }

    private Node construirProtocolos() {
        List<Reaccion> reacciones = catalogo.obtenerReacciones().stream()
                .sorted(Comparator.comparing(Reaccion::getNombre))
                .toList();
        ComboBox<Reaccion> selector = new ComboBox<>();
        selector.getItems().setAll(reacciones);
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(Reaccion reaccion) {
                return reaccion == null ? "" : reaccion.getNombre();
            }

            @Override
            public Reaccion fromString(String texto) {
                return reacciones.stream()
                        .filter(reaccion -> reaccion.getNombre().equals(texto))
                        .findFirst().orElse(null);
            }
        });
        if (reacciones.isEmpty()) {
            return crearScroll(new VBox(9,
                    titulo("Guía de síntesis"),
                    textoSecundario("El catálogo no contiene procesos químicos para mostrar.")));
        }
        Reaccion activo = sesion.getReaccion().flatMap(actual ->
                reacciones.stream().filter(reaccion -> reaccion.getId().equals(actual.getId())).findFirst()
        ).orElse(reacciones.get(0));
        selector.setValue(activo);

        VBox detalle = new VBox(9);
        detalle.setPadding(new Insets(4, 2, 8, 2));
        selector.valueProperty().addListener((observable, anterior, actual) -> {
            if (actual != null) {
                detalle.getChildren().setAll(construirDetalleProtocolo(actual));
            }
        });
        detalle.getChildren().setAll(construirDetalleProtocolo(activo));

        Label encabezado = titulo("Protocolos de todos los procesos");
        Label ayuda = textoSecundario("Selecciona cualquier proceso del catálogo para consultar sus etapas, "
                + "acciones y equipo. La secuencia se basa en el autómata que valida la práctica.");
        VBox selectorPanel = new VBox(4, encabezado, ayuda, selector);
        selectorPanel.getStyleClass().add("game-chemistry-card");
        VBox cuerpo = new VBox(8, selectorPanel, detalle);
        ScrollPane scroll = crearScroll(cuerpo);
        return scroll;
    }

    private Node construirDetalleProtocolo(Reaccion reaccion) {
        VBox vista = new VBox(9);
        Label producto = titulo(reaccion.getNombre());
        Node ecuacion = etiquetaEcuacion(reaccion);
        VBox resumen = new VBox(5, producto, ecuacion);
        resumen.getStyleClass().add("game-chemistry-card");
        vista.getChildren().add(resumen);

        boolean esActivo = sesion.isActiva()
                && sesion.getReaccion().map(actual -> actual.getId().equals(reaccion.getId())).orElse(false);
        RegistroExperimento ultimoIntento = sesion.getCuaderno().getHistorial().stream()
                .filter(registro -> reaccion.getId().equals(registro.getIdProceso()))
                .max(Comparator.comparing(RegistroExperimento::getFecha)).orElse(null);
        AutomataQuimico automata = FabricaAutomata.crearAutomataParaReaccion(reaccion.getId());
        List<TransicionQuimica> transiciones = obtenerTransicionesAlcanzables(automata);

        Label estado = new Label(esActivo
                ? "Práctica en curso · etapa actual: " + nombreEstadoActual(sesion.getAutomataActual())
                : ultimoIntento == null ? "Sin intentos registrados · sigue las etapas en orden."
                : ultimoIntento.isExitoso() ? "Último intento completado."
                : "Último intento: " + ultimoIntento.getPasosTransicion().size() + " etapas registradas.");
        estado.getStyleClass().add("game-chemistry-status");
        vista.getChildren().add(estado);

        Label tituloEtapas = titulo("Etapas del procedimiento");
        vista.getChildren().add(tituloEtapas);
        int indiceEtapa = 0;
        for (TransicionQuimica transicion : transiciones) {
            ProtocoloProcedimiento.InfoPasoProcedimiento info =
                    ProtocoloProcedimiento.obtenerProtocoloPaso(reaccion.getId(),
                            transicion.getEstadoOrigen().getId());
            boolean completada = ultimoIntento != null
                    && ultimoIntento.getPasosTransicion().stream().anyMatch(paso ->
                    paso.valida()
                            && paso.estadoOrigen().equals(transicion.getEstadoOrigen().getId())
                            && paso.estadoDestino().equals(transicion.getEstadoDestino().getId())
                            && paso.accion().equals(transicion.getAccion().name()));
            boolean actual = esActivo && transicion.getEstadoOrigen().getId()
                    .equals(sesion.getAutomataActual().getEstadoActual().getId());
            vista.getChildren().add(crearTarjetaEtapa(
                    indiceEtapa + 1,
                    transicion.getEstadoOrigen().getNombre(),
                    textoAccion(transicion.getAccion()),
                    info.equipoTecnico(),
                    completada,
                    actual
            ));
            indiceEtapa++;
        }
        return vista;
    }

    private VBox crearTarjetaEtapa(
            int numero,
            String etapa,
            String accion,
            String equipo,
            boolean completada,
            boolean actual
    ) {
        String estadoEtapa = completada ? "COMPLETADA" : actual ? "AHORA" : "PENDIENTE";
        Label encabezado = new Label((completada ? "✓  " : actual ? "▶  " : numero + ".  ")
                + etapa + " · " + estadoEtapa);
        encabezado.getStyleClass().add("game-chemistry-stage-title");
        Label instruccion = new Label(accion);
        instruccion.setWrapText(true);
        Label equipoRequerido = new Label("Equipo: " + equipo);
        equipoRequerido.getStyleClass().add("game-menu-subtitle");
        VBox tarjeta = new VBox(4, encabezado, instruccion, equipoRequerido);
        tarjeta.getStyleClass().add("game-chemistry-card");
        if (actual) tarjeta.getStyleClass().add("game-chemistry-card-current");
        return tarjeta;
    }

    private Node construirAnalisisMuestras() {
        Map<String, Sustancia> disponibles = new LinkedHashMap<>();
        for (String id : sesion.getCuaderno().getSustanciasDescubiertas()) {
            catalogo.buscarSustancia(id).ifPresent(sustancia -> disponibles.put(id, sustancia));
        }
        experimentos.stream()
                .flatMap(experimento -> experimento.getInventario().obtenerCantidades().keySet().stream())
                .forEach(sustancia -> disponibles.putIfAbsent(sustancia.getId(), sustancia));
        productosAlmacenados.forEach(sustancia -> disponibles.putIfAbsent(sustancia.getId(), sustancia));
        List<Sustancia> muestras = disponibles.values().stream()
                .sorted(Comparator.comparing(Sustancia::getNombre))
                .toList();

        ComboBox<Sustancia> selector = new ComboBox<>();
        selector.getItems().setAll(muestras);
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(Sustancia muestra) {
                return muestra == null ? "" : muestra.getNombre() + " · " + muestra.getFormula();
            }

            @Override
            public Sustancia fromString(String texto) {
                return muestras.stream()
                        .filter(muestra -> (muestra.getNombre() + " · " + muestra.getFormula()).equals(texto))
                        .findFirst().orElse(null);
            }
        });

        TextField formula = new TextField();
        formula.setPromptText("Fórmula de la muestra, por ejemplo Ca₃(PO₄)₂");
        boolean[] subindiceActivo = {false};
        formula.setTextFormatter(new TextFormatter<String>(cambio -> {
            if (subindiceActivo[0] && cambio.isContentChange()) {
                cambio.setText(convertirDigitosSubindice(cambio.getControlText(),
                        cambio.getRangeStart(), cambio.getText()));
            }
            return cambio;
        }));
        Button botonSubindice = new Button("SUBÍNDICE ₂");
        botonSubindice.getStyleClass().add("game-secondary-button");
        botonSubindice.setTooltip(new Tooltip(
                "Selecciona dígitos para convertirlos en subíndices, o activa el modo antes de escribirlos."));
        botonSubindice.setOnAction(evento -> {
            String seleccionado = formula.getSelectedText();
            if (!seleccionado.isEmpty()) {
                formula.replaceSelection(convertirDigitosSubindice(formula.getText(),
                        formula.getSelection().getStart(), seleccionado));
                formula.requestFocus();
            } else {
                subindiceActivo[0] = !subindiceActivo[0];
                botonSubindice.setText(subindiceActivo[0] ? "SUBÍNDICE ₂ · ACTIVO" : "SUBÍNDICE ₂");
                if (subindiceActivo[0]) formula.requestFocus();
            }
        });
        Label estado = new Label("Elige una sustancia descubierta o introduce una fórmula para examinar su composición.");
        estado.setWrapText(true);
        Label total = new Label();
        FlowPane atomos = new FlowPane(8, 8);
        VBox resultado = new VBox(8);
        resultado.getStyleClass().add("game-chemistry-card");

        Runnable analizar = () -> mostrarResultadoAnalisis(formula.getText(), estado, total, atomos, resultado);
        selector.valueProperty().addListener((observable, anterior, actual) -> {
            if (actual != null) {
                formula.setText(actual.getFormula());
                analizar.run();
            }
        });
        formula.setOnAction(evento -> analizar.run());
        Button botonAnalizar = new Button("EXAMINAR MUESTRA");
        botonAnalizar.getStyleClass().add("game-start-button");
        botonAnalizar.setOnAction(evento -> analizar.run());
        HBox entrada = new HBox(8, selector, formula, botonSubindice, botonAnalizar);
        entrada.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(formula, Priority.ALWAYS);

        VBox panel = new VBox(9,
                titulo("Análisis de muestras"),
                textoSecundario("Selecciona una sustancia del cuaderno o escribe una fórmula. "
                        + "Verás los nombres de los elementos, el nombre del compuesto y cómo se deriva paso a paso."),
                entrada, estado, resultado);
        panel.getStyleClass().add("game-chemistry-card");
        if (!muestras.isEmpty()) {
            selector.setValue(muestras.get(0));
        }
        return crearScroll(panel);
    }

    private void mostrarResultadoAnalisis(
            String formula,
            Label estado,
            Label total,
            FlowPane atomos,
            VBox panelResultado
    ) {
        panelResultado.getChildren().clear();
        atomos.getChildren().clear();
        try {
            ResultadoAnalisisFormula analisis = analizador.analizar(formula);
            String nombreRegistrado = buscarNombreCompuesto(analisis);
            ResultadoNombreQuimico nombre = nomenclatura.nombrar(analisis, nombreRegistrado);
            analisis.atomos().forEach((elemento, cantidad) -> {
                String nombreElemento = nomenclatura.nombreElemento(elemento)
                        .orElse("elemento " + elemento);
                Label ficha = new Label(nombreElemento + " (" + elemento + ")  ×  " + cantidad
                        + " por unidad");
                ficha.getStyleClass().add("game-chemistry-atom");
                atomos.getChildren().add(ficha);
            });
            int cantidadTotal = analisis.atomos().values().stream().mapToInt(Integer::intValue).sum();
            String cantidadCoeficiente = analisis.coeficiente() == 1 ? ""
                    : " · Coeficiente " + analisis.coeficiente() + " = "
                    + ((long) cantidadTotal * analisis.coeficiente()) + " átomos en total";
            String fase = analisis.fase().isBlank() ? ""
                    : " · Fase: " + nombreFase(analisis.fase()) + " " + analisis.fase();
            total.setText("Total por unidad de fórmula: " + cantidadTotal + cantidadCoeficiente + fase);
            total.getStyleClass().add("game-chemistry-stage-title");
            TreeView<String> arbol = new TreeView<>(crearArbolAnalisis(analisis, nombre));
            arbol.setShowRoot(true);
            arbol.setPrefHeight(360);
            arbol.setMinHeight(250);
            arbol.getStyleClass().add("game-chemistry-analysis-tree");
            expandir(arbol.getRoot());
            panelResultado.getChildren().addAll(
                    titulo("Nombre químico: " + nombre.nombre()),
                    etiquetaCorta("Regla aplicada: " + nombre.reglaAplicada()),
                    titulo("Composición · " + analisis.formulaNormalizada()),
                    atomos,
                    total,
                    titulo("Árbol de análisis y formación del nombre"),
                    textoSecundario("Despliega cada rama para seguir la fórmula, reconocer los elementos y aplicar la regla de nomenclatura."),
                    arbol
            );
            estado.setText("Análisis completado. Los grupos y subíndices de la fórmula se contabilizaron.");
            estado.getStyleClass().setAll("game-chemistry-status", "game-chemistry-status-good");
        } catch (FormulaInvalidaException error) {
            estado.setText("No se pudo identificar la muestra · posición " + (error.getPosicion() + 1)
                    + ": " + error.getMessage());
            estado.getStyleClass().setAll("game-chemistry-status", "game-chemistry-status-error");
        }
    }

    private String buscarNombreCompuesto(ResultadoAnalisisFormula analisis) {
        String nombreCoincidencia = null;
        for (Sustancia sustancia : catalogo.obtenerSustancias()) {
            try {
                ResultadoAnalisisFormula analisisCatalogo = analizador.analizar(sustancia.getFormula());
                if (analisisCatalogo.formulaNormalizada().equals(analisis.formulaNormalizada())) {
                    if (analisisCatalogo.fase().equals(analisis.fase())) return sustancia.getNombre();
                    if (nombreCoincidencia == null) nombreCoincidencia = sustancia.getNombre();
                }
            } catch (FormulaInvalidaException ignored) {
                // Algunas entradas del catálogo son formas físicas o metadatos, no fórmulas analizables.
            }
        }
        return nombreCoincidencia;
    }

    private String convertirDigitosSubindice(String contenidoActual, int inicio, String insertado) {
        StringBuilder convertido = new StringBuilder(insertado.length());
        String contexto = contenidoActual.substring(0, inicio);
        for (int indice = 0; indice < insertado.length(); indice++) {
            char caracter = insertado.charAt(indice);
            if (esDigito(caracter) && contextoContieneElemento(contexto)) {
                caracter = aSubindice(caracter);
            }
            convertido.append(caracter);
            contexto += caracter;
        }
        return convertido.toString();
    }

    private boolean contextoContieneElemento(String texto) {
        for (int indice = texto.length() - 1; indice >= 0; indice--) {
            char caracter = texto.charAt(indice);
            if (Character.isWhitespace(caracter)) continue;
            return Character.isLetter(caracter) || caracter == ')' || esSubindice(caracter);
        }
        return false;
    }

    private boolean esDigito(char caracter) {
        return caracter >= '0' && caracter <= '9';
    }

    private boolean esSubindice(char caracter) {
        return caracter >= '₀' && caracter <= '₉';
    }

    private char aSubindice(char digito) {
        return switch (digito) {
            case '0' -> '₀';
            case '1' -> '₁';
            case '2' -> '₂';
            case '3' -> '₃';
            case '4' -> '₄';
            case '5' -> '₅';
            case '6' -> '₆';
            case '7' -> '₇';
            case '8' -> '₈';
            case '9' -> '₉';
            default -> digito;
        };
    }

    private String nombreFase(String fase) {
        return switch (fase) {
            case "(ac)", "(aq)" -> "acuosa";
            case "(s)" -> "sólida";
            case "(l)" -> "líquida";
            case "(g)" -> "gaseosa";
            default -> "desconocida";
        };
    }

    private TreeItem<String> crearArbolAnalisis(
            ResultadoAnalisisFormula analisis,
            ResultadoNombreQuimico nombre
    ) {
        TreeItem<String> raiz = new TreeItem<>("Análisis químico · " + analisis.formulaOriginal());
        if (analisis.coeficiente() != 1) {
            raiz.getChildren().add(new TreeItem<>("Coeficiente estequiométrico: "
                    + analisis.coeficiente() + " · multiplica unidades de fórmula; no es un subíndice."));
        }
        if (!analisis.fase().isBlank()) {
            raiz.getChildren().add(new TreeItem<>("Estado físico: "
                    + nombreFase(analisis.fase()) + " " + analisis.fase()));
        }
        TreeItem<String> formula = new TreeItem<>("Estructura de la fórmula reconocida");
        formula.getChildren().add(crearRamaFormula(analisis.arbol()));
        TreeItem<String> elementos = new TreeItem<>("Elementos identificados");
        nomenclatura.nombresElementos(analisis).forEach((simbolo, nombreElemento) ->
                elementos.getChildren().add(new TreeItem<>(simbolo + " → " + nombreElemento
                        + " · " + analisis.atomos().get(simbolo) + " átomo(s)")));
        TreeItem<String> derivacion = new TreeItem<>("Construcción del nombre químico");
        for (String paso : nombre.pasos()) {
            derivacion.getChildren().add(new TreeItem<>(paso));
        }
        derivacion.getChildren().add(new TreeItem<>("Nombre obtenido: " + nombre.nombre()));
        raiz.getChildren().addAll(formula, elementos, derivacion);
        return raiz;
    }

    private TreeItem<String> crearRamaFormula(
            com.chemlab.grammar.formula.NodoDerivacionFormula nodo
    ) {
        TreeItem<String> rama = new TreeItem<>(nodo.etiqueta());
        nodo.hijos().stream().map(this::crearRamaFormula).forEach(rama.getChildren()::add);
        return rama;
    }

    private void expandir(TreeItem<String> nodo) {
        if (nodo == null) return;
        nodo.setExpanded(true);
        nodo.getChildren().forEach(this::expandir);
    }

    private Node construirComparadorRutas() {
        List<GrupoRutas> grupos = construirGruposRutas();
        if (grupos.isEmpty()) {
            return crearScroll(new VBox(9,
                    titulo("Comparar rutas de síntesis"),
                    textoSecundario("El catálogo no contiene productos para comparar.")));
        }
        ComboBox<GrupoRutas> selector = new ComboBox<>();
        selector.getItems().setAll(grupos);
        selector.setValue(grupos.get(0));
        VBox rutas = new VBox(9);
        selector.valueProperty().addListener((observable, anterior, actual) -> {
            if (actual != null) rutas.getChildren().setAll(construirTarjetasRutas(actual));
        });
        rutas.getChildren().setAll(construirTarjetasRutas(selector.getValue()));
        VBox cuerpo = new VBox(9,
                titulo("Comparar rutas de síntesis"),
                textoSecundario("Elige cualquier compuesto producido por el catálogo para ver todas sus rutas; "
                        + "los compuestos con una sola ruta también se incluyen."),
                selector, rutas);
        cuerpo.getStyleClass().add("game-chemistry-card");
        return crearScroll(cuerpo);
    }

    private List<GrupoRutas> construirGruposRutas() {
        List<Reaccion> reacciones = catalogo.obtenerReacciones();
        Map<String, List<Reaccion>> reaccionesPorProducto = new LinkedHashMap<>();
        Map<String, Sustancia> productos = new LinkedHashMap<>();
        for (Reaccion reaccion : reacciones) {
            for (Sustancia producto : reaccion.getProductos().keySet()) {
                productos.putIfAbsent(producto.getId(), producto);
                reaccionesPorProducto.computeIfAbsent(producto.getId(), ignorado -> new ArrayList<>())
                        .add(reaccion);
            }
        }

        Map<String, ProtocoloProcedimiento.InfoRutaSintesis> rutasCuradas = new LinkedHashMap<>();
        for (Reaccion reaccion : reacciones) {
            for (ProtocoloProcedimiento.InfoRutaSintesis ruta
                    : ProtocoloProcedimiento.obtenerRutasDisponibles(reaccion.getId())) {
                String clave = ruta.compuestoId() + "|" + ruta.idReaccion();
                rutasCuradas.putIfAbsent(clave, ruta);
            }
        }

        return productos.entrySet().stream()
                .map(entrada -> {
                    String productoId = entrada.getKey();
                    Sustancia producto = entrada.getValue();
                    Map<String, ProtocoloProcedimiento.InfoRutaSintesis> rutasProducto = new LinkedHashMap<>();
                    rutasCuradas.values().stream()
                            .filter(ruta -> ruta.compuestoId().equals(productoId))
                            .forEach(ruta -> rutasProducto.put(ruta.idReaccion(), ruta));
                    reaccionesPorProducto.getOrDefault(productoId, List.of()).forEach(reaccion ->
                            rutasProducto.putIfAbsent(reaccion.getId(),
                                    rutaParaProducto(reaccion, producto, rutasCuradas)));
                    List<ProtocoloProcedimiento.InfoRutaSintesis> rutas = List.copyOf(rutasProducto.values());
                    return new GrupoRutas(producto, rutas);
                })
                .sorted(Comparator.comparing(grupo -> grupo.compuesto().getNombre()))
                .toList();
    }

    private ProtocoloProcedimiento.InfoRutaSintesis rutaParaProducto(
            Reaccion reaccion,
            Sustancia producto,
            Map<String, ProtocoloProcedimiento.InfoRutaSintesis> rutasCuradas
    ) {
        ProtocoloProcedimiento.InfoRutaSintesis curada =
                rutasCuradas.get(producto.getId() + "|" + reaccion.getId());
        if (curada != null) return curada;

        return new ProtocoloProcedimiento.InfoRutaSintesis(
                producto.getId(),
                producto.getNombre(),
                reaccion.getId(),
                "Ruta: " + reaccion.getNombre(),
                "PROCESO DEL CATÁLOGO",
                "Síntesis mediante la reacción registrada",
                "Equipo experimental indicado por las etapas del proceso",
                "Esta ruta corresponde a la reacción «" + reaccion.getNombre()
                        + "» y produce " + producto.getNombre() + ".",
                "Ruta disponible en el catálogo de prácticas."
        );
    }

    private List<Node> construirTarjetasRutas(GrupoRutas grupo) {
        List<ProtocoloProcedimiento.InfoRutaSintesis> rutas = grupo.rutas();
        List<Node> tarjetas = new ArrayList<>();
        tarjetas.add(titulo("Rutas para " + grupo.compuesto().getNombre()
                + " · " + grupo.compuesto().getFormula()));
        tarjetas.add(titulo("Autómata general · " + rutas.size()
                + (rutas.size() == 1 ? " ruta disponible" : " rutas disponibles")));
        tarjetas.add(textoSecundario("El inicio se ramifica por cada método disponible; cada rama muestra los estados y acciones "
                + "del autómata real de esa ruta y converge en el producto objetivo."));
        if (!rutas.isEmpty()) {
            ScrollPane diagrama = crearScroll(new LienzoAutomataRutas(rutas, sesion));
            diagrama.setFitToWidth(false);
            diagrama.setPrefViewportHeight(Math.min(410, 120 + rutas.size() * 108));
            tarjetas.add(diagrama);
        }
        for (ProtocoloProcedimiento.InfoRutaSintesis ruta : rutas) {
            VBox tarjeta = new VBox(6);
            tarjeta.getStyleClass().add("game-chemistry-card");
            tarjeta.getChildren().addAll(
                    titulo(ruta.nombreRuta()),
                    etiquetaCorta(ruta.badge()),
                    etiquetaEcuacion(catalogo.buscarReaccion(ruta.idReaccion()).orElse(null)),
                    filaDato("Método", ruta.metodo()),
                    filaDato("Equipo", ruta.equipo()),
                    textoSecundario(ruta.descripcionCientifica()),
                    textoSecundario(ruta.comparacionConOtrasRutas())
            );
            boolean disponible = catalogo.buscarReaccion(ruta.idReaccion()).isPresent();
            Label disponibilidad = textoSecundario(disponible
                    ? "Esta ruta está disponible como reacción en el catálogo del juego."
                    : "Ficha comparativa: esta ruta aún no está habilitada como práctica en el catálogo.");
            tarjeta.getChildren().add(disponibilidad);
            tarjetas.add(tarjeta);
        }
        return tarjetas;
    }

    private Node etiquetaEcuacion(Reaccion reaccion) {
        return etiquetaCorta(reaccion == null ? "Ecuación no disponible en el catálogo de prácticas."
                : construirEcuacion(reaccion));
    }

    private String construirEcuacion(Reaccion reaccion) {
        return reaccion.getReactivos().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Sustancia::getId)))
                .map(entrada -> terminoEcuacion(entrada.getValue(), entrada.getKey()))
                .collect(Collectors.joining(" + "))
                + "  →  "
                + reaccion.getProductos().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Sustancia::getId)))
                .map(entrada -> terminoEcuacion(entrada.getValue(), entrada.getKey()))
                .collect(Collectors.joining(" + "));
    }

    private String terminoEcuacion(int coeficiente, Sustancia sustancia) {
        return (coeficiente == 1 ? "" : coeficiente + " ") + sustancia.getFormula();
    }

    private Node filaDato(String etiqueta, String valor) {
        Label nombre = new Label(etiqueta + ": ");
        nombre.getStyleClass().add("game-chemistry-stage-title");
        Label detalle = new Label(valor);
        detalle.setWrapText(true);
        HBox fila = new HBox(4, nombre, detalle);
        fila.setAlignment(Pos.TOP_LEFT);
        return fila;
    }

    private List<TransicionQuimica> obtenerTransicionesAlcanzables(AutomataQuimico automata) {
        List<TransicionQuimica> transiciones = new ArrayList<>();
        ArrayDeque<EstadoQuimico> pendientes = new ArrayDeque<>();
        Set<String> visitados = new HashSet<>();
        pendientes.add(automata.getEstadoInicial());
        while (!pendientes.isEmpty()) {
            EstadoQuimico estado = pendientes.removeFirst();
            if (!visitados.add(estado.getId())) continue;
            List<TransicionQuimica> salientes = automata.getTransicionesDesdeEstado(estado).stream()
                    .sorted(Comparator.comparing(transicion -> transicion.getAccion().name()))
                    .toList();
            transiciones.addAll(salientes);
            salientes.stream().map(TransicionQuimica::getEstadoDestino)
                    .filter(destino -> !visitados.contains(destino.getId()))
                    .forEach(pendientes::addLast);
        }
        return List.copyOf(transiciones);
    }

    private String nombreEstadoActual(AutomataQuimico automata) {
        return automata.getEstadoActual().getNombre();
    }

    private String textoAccion(AccionLaboratorio accion) {
        return switch (accion) {
            case MONTAR_EQUIPO -> "Monta y configura el equipo antes de procesar la muestra.";
            case CARGAR_REACTIVO -> "Prepara y carga los reactivos en el equipo indicado.";
            case MEZCLAR -> "Mezcla los reactivos para homogeneizar el sistema.";
            case INICIAR_PLASMA -> "Activa el plasma en la cámara de reacción.";
            case CALENTAR -> "Aplica calor para iniciar o sostener la transformación.";
            case COMPRIMIR -> "Aumenta la presión del sistema con el equipo indicado.";
            case AGREGAR_CATALIZADOR -> "Incorpora el catalizador requerido.";
            case AGREGAR_AGUA -> "Añade agua a la preparación.";
            case ACTIVAR_IGNICION -> "Activa la ignición para iniciar la reacción.";
            case FINALIZAR -> "Finaliza el proceso y recupera el producto.";
            default -> "Realiza la operación en el equipo indicado.";
        };
    }

    private AccionLaboratorio accionDesdeRegistro(String nombre) {
        try {
            return AccionLaboratorio.valueOf(nombre);
        } catch (IllegalArgumentException | NullPointerException error) {
            return null;
        }
    }

    private ScrollPane crearScroll(Node contenido) {
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("game-chemistry-scroll");
        return scroll;
    }

    private Label titulo(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("game-chemistry-title");
        etiqueta.setWrapText(true);
        return etiqueta;
    }

    private Label textoSecundario(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("game-menu-subtitle");
        etiqueta.setWrapText(true);
        return etiqueta;
    }

    private Label etiquetaCorta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("game-chemistry-equation");
        etiqueta.setWrapText(true);
        return etiqueta;
    }
}
