package com.chemlab.ui;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EstadoQuimico;
import com.chemlab.automata.ResultadoAccion;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.factory.FabricaSesion;
import com.chemlab.game.ObservadorSesion;
import com.chemlab.game.procedure.ProtocoloProcedimiento;
import com.chemlab.ui.components.IconoSustancia;
import com.chemlab.game.SesionJuego;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.RegistroExperimento;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.value.Cantidad;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Vista principal del laboratorio químico.
 *
 * <p>Implementa {@link ObservadorSesion} para recibir notificaciones
 * del dominio sin acoplamiento inverso. La UI nunca modifica el dominio;
 * sólo invoca métodos de {@link SesionJuego}.</p>
 *
 * <p>Panel izquierdo: inventario + cuaderno (historial de experimentos).<br>
 * Panel central: selección de experimento, controles y acciones del autómata.<br>
 * Panel inferior: registro de laboratorio (consola de eventos).</p>
 */
public final class VistaLaboratorio extends BorderPane
        implements ObservadorSesion {

    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    // --- Estado de juego ---
    private final SesionJuego sesion;
    private final FabricaExperimento fabricaExperimento;

    // --- Widgets superiores ---
    private Label labelEstadoExperimento;
    private Label labelEstadoAutomata;
    private Label labelNombreExperimento;
    private Label labelContadorExitos;

    // --- Panel lateral: inventario + cuaderno ---
    private VBox panelInventario;
    private VBox panelHistorial;

    // --- Panel central: acciones ---
    private FlowPane panelAcciones;
    private Button botonIniciar;
    private Button botonAbandonar;

    // --- Panel inferior: log ---
    private TextArea areaLog;

    // --- Panel de progreso del autómata ---
    private HBox panelProgresoAutomata;
    private VBox panelGuiaProceso;

    /** Experimento y reacción que el usuario ha seleccionado pero aún no iniciado. */
    private Experimento experimentoSeleccionado;
    private Reaccion reaccionSeleccionada;
    private AutomataQuimico automataSeleccionado;

    // --- Control de reactivos seleccionados ---
    private final java.util.Set<String> elementosSeleccionados = new java.util.HashSet<>();
    private FlowPane selectorExp;

    // --- Monitor de procedimiento real, reactivos y rutas ---
    private VBox panelRutasSintesis;
    private VBox panelMonitorProcedimiento;
    private Label labelEquipoMonitor;
    private Label labelTempMonitor;
    private Label labelPresionMonitor;
    private Label labelAtmosferaMonitor;
    private Label labelCatalizadorMonitor;
    private FlowPane gridTarjetasReactivos;
    private Label labelNarrativaProcedimiento;
    private Label labelProximaAccion;

    public VistaLaboratorio() {
        CatalogoQuimico catalogo = CatalogoQuimico.predeterminado();
        this.sesion = FabricaSesion.crearSesion(
                catalogo,
                FabricaAutomata.crearAutomataEstandar()
        );
        this.fabricaExperimento = new FabricaExperimento(catalogo);

        sesion.agregarObservador(this);
        construirUI();
    }

    // ---------------------------------------------------------------
    // Construcción de la UI
    // ---------------------------------------------------------------

    private void construirUI() {
        getStylesheets().add(
                getClass().getResource("/styles/main.css")
                        .toExternalForm()
        );
        getStyleClass().add("root");

        setTop(construirBarraSuperior());
        setLeft(construirPanelLateral());
        setCenter(construirPanelCentral());
        setBottom(construirPanelLog());
    }

    private HBox construirBarraSuperior() {
        HBox barra = new HBox(16);
        barra.getStyleClass().add("barra-superior");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(10, 16, 10, 16));

        Label titulo = new Label("⚗ ChemLab");
        titulo.getStyleClass().add("titulo-app");

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        labelContadorExitos = new Label("✓ 0 completados");
        labelContadorExitos.getStyleClass().add("contador-exitos");

        labelEstadoExperimento = new Label("Sin experimento");
        labelEstadoExperimento.getStyleClass().add("badge-no-iniciado");

        barra.getChildren().addAll(
                titulo,
                espaciador,
                labelContadorExitos,
                labelEstadoExperimento
        );

        return barra;
    }

    private TabPane construirPanelLateral() {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("panel-lateral-tabs");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMinWidth(240);
        tabPane.setPrefWidth(270);

        // --- Tab Almacén ---
        Tab tabAlmacen = new Tab("🗄️ Almacén");
        VBox contenidoAlmacen = new VBox(10);
        contenidoAlmacen.setPadding(new Insets(12));
        Label tituloAlmacen = new Label("REACTIVOS DISPONIBLES");
        tituloAlmacen.getStyleClass().add("titulo-seccion");

        Label subtituloAlmacen = new Label("Selecciona los reactivos con que cuentas:");
        subtituloAlmacen.getStyleClass().add("texto-secundario");
        subtituloAlmacen.setWrapText(true);

        List<javafx.scene.control.CheckBox> checkBoxesAlmacen = new ArrayList<>();
        FlowPane gridAlmacen = new FlowPane(6, 6);
        gridAlmacen.getStyleClass().add("grid-almacen");

        HBox barraAccionesAlmacen = new HBox(8);
        barraAccionesAlmacen.setAlignment(Pos.CENTER_LEFT);
        Button btnTodos = new Button("✓ Seleccionar Todos");
        btnTodos.getStyleClass().add("boton-secundario");
        btnTodos.setStyle("-fx-font-size: 10px; -fx-padding: 3 8;");
        btnTodos.setOnAction(e -> {
            for (var cb : checkBoxesAlmacen) {
                cb.setSelected(true);
            }
            elementosSeleccionados.clear();
            for (Sustancia s : CatalogoQuimico.predeterminado().obtenerSustancias()) {
                elementosSeleccionados.add(s.getId());
            }
            actualizarSelectorExperimentos();
        });

        Button btnNinguno = new Button("✕ Desmarcar");
        btnNinguno.getStyleClass().add("boton-secundario");
        btnNinguno.setStyle("-fx-font-size: 10px; -fx-padding: 3 8;");
        btnNinguno.setOnAction(e -> {
            for (var cb : checkBoxesAlmacen) {
                cb.setSelected(false);
            }
            elementosSeleccionados.clear();
            actualizarSelectorExperimentos();
        });
        barraAccionesAlmacen.getChildren().addAll(btnTodos, btnNinguno);

        for (Sustancia s : CatalogoQuimico.predeterminado().obtenerSustancias()) {
            javafx.scene.control.CheckBox cb = new javafx.scene.control.CheckBox(
                    s.getFormula() + "  " + s.getNombre()
            );
            cb.getStyleClass().add("checkbox-reactivo");
            cb.setSelected(true);
            elementosSeleccionados.add(s.getId());
            checkBoxesAlmacen.add(cb);
            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    elementosSeleccionados.add(s.getId());
                } else {
                    elementosSeleccionados.remove(s.getId());
                }
                actualizarSelectorExperimentos();
            });
            gridAlmacen.getChildren().add(cb);
        }

        ScrollPane scrollAlmacen = new ScrollPane(gridAlmacen);
        scrollAlmacen.getStyleClass().add("scroll-almacen");
        scrollAlmacen.setFitToWidth(true);
        VBox.setVgrow(scrollAlmacen, Priority.ALWAYS);

        contenidoAlmacen.getChildren().addAll(tituloAlmacen, subtituloAlmacen, barraAccionesAlmacen, scrollAlmacen);
        tabAlmacen.setContent(contenidoAlmacen);

        // --- Tab Inventario ---
        Tab tabInventario = new Tab("📦 Inventario");
        VBox contenidoInventario = new VBox(10);
        contenidoInventario.setPadding(new Insets(12));

        Label tituloInv = new Label("INVENTARIO ACTUAL");
        tituloInv.getStyleClass().add("titulo-seccion");

        panelInventario = new VBox(6);
        Label sinInventario = new Label(
                "Selecciona e inicia un experimento para ver el inventario."
        );
        sinInventario.getStyleClass().add("texto-secundario");
        sinInventario.setWrapText(true);
        panelInventario.getChildren().add(sinInventario);

        contenidoInventario.getChildren().addAll(tituloInv, panelInventario);
        tabInventario.setContent(contenidoInventario);

        // --- Tab Cuaderno / Historial ---
        Tab tabCuaderno = new Tab("📓 Cuaderno");
        VBox contenidoCuaderno = new VBox(10);
        contenidoCuaderno.setPadding(new Insets(12));

        Label tituloCuad = new Label("HISTORIAL DE EXPERIMENTOS");
        tituloCuad.getStyleClass().add("titulo-seccion");

        panelHistorial = new VBox(6);
        Label sinHistorial = new Label("Aún no has realizado ningún experimento.");
        sinHistorial.getStyleClass().add("texto-secundario");
        sinHistorial.setWrapText(true);
        panelHistorial.getChildren().add(sinHistorial);

        ScrollPane scrollHistorial = new ScrollPane(panelHistorial);
        scrollHistorial.setFitToWidth(true);
        scrollHistorial.getStyleClass().add("scroll-historial");
        VBox.setVgrow(scrollHistorial, Priority.ALWAYS);

        contenidoCuaderno.getChildren().addAll(tituloCuad, scrollHistorial);
        VBox.setVgrow(contenidoCuaderno, Priority.ALWAYS);
        tabCuaderno.setContent(contenidoCuaderno);

        tabPane.getTabs().addAll(tabAlmacen, tabInventario, tabCuaderno);
        return tabPane;
    }

    private ScrollPane construirPanelCentral() {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("panel-principal");
        panel.setPadding(new Insets(20));

        // Nombre del experimento
        labelNombreExperimento = new Label("Selecciona un experimento");
        labelNombreExperimento.getStyleClass().add("titulo-experimento");

        // Estado del autómata
        labelEstadoAutomata = new Label("Esperando inicio…");
        labelEstadoAutomata.getStyleClass().add("texto-info");

        // Rutas alternativas de síntesis (diamante HPHT vs CVD, CO2 combustión vs calcinación, etc.)
        panelRutasSintesis = construirPanelRutasSintesis();

        // Monitor de procedimiento físico y disposición de reactivos
        panelMonitorProcedimiento = construirPanelMonitorProcedimiento();

        // Panel progreso del autómata (barra de estados)
        Label labelProgreso = new Label("PROGRESO DEL PROCESO");
        labelProgreso.getStyleClass().add("titulo-seccion");

        panelProgresoAutomata = new HBox(4);
        panelProgresoAutomata.setAlignment(Pos.CENTER_LEFT);
        panelProgresoAutomata.setPadding(new Insets(4, 0, 8, 0));
        Label sinProceso = new Label("—  Sin proceso activo");
        sinProceso.getStyleClass().add("texto-secundario");
        panelProgresoAutomata.getChildren().add(sinProceso);

        // Guía de acciones del proceso
        Label labelGuia = new Label("GUÍA DEL PROCESO");
        labelGuia.getStyleClass().add("titulo-seccion");

        panelGuiaProceso = new VBox(4);
        Label sinGuia = new Label("Selecciona un experimento para ver la secuencia de acciones.");
        sinGuia.getStyleClass().add("texto-secundario");
        sinGuia.setWrapText(true);
        panelGuiaProceso.getChildren().add(sinGuia);

        // Separador visual
        Region sep = new Region();
        sep.getStyleClass().add("separador");

        // Selector de experimentos
        Label labelSelector = new Label("EXPERIMENTOS DISPONIBLES");
        labelSelector.getStyleClass().add("titulo-seccion");

        selectorExp = new FlowPane(8, 8);
        selectorExp.setAlignment(Pos.CENTER_LEFT);
        actualizarSelectorExperimentos();

        // Botones de control
        botonIniciar = new Button("▶ Iniciar Experimento");
        botonIniciar.getStyleClass().add("boton-primario");
        botonIniciar.setDisable(true);
        botonIniciar.setOnAction(e -> accionIniciar());

        botonAbandonar = new Button("✕ Abandonar");
        botonAbandonar.getStyleClass().add("boton-peligro");
        botonAbandonar.setDisable(true);
        botonAbandonar.setOnAction(e -> accionAbandonar());

        HBox controles = new HBox(10, botonIniciar, botonAbandonar);
        controles.setAlignment(Pos.CENTER_LEFT);

        // Panel de acciones del autómata
        Label labelAcciones = new Label("ACCIONES DE LABORATORIO");
        labelAcciones.getStyleClass().add("titulo-seccion");

        panelAcciones = new FlowPane(8, 8);
        panelAcciones.setDisable(true);
        construirBotonesAcciones();

        panel.getChildren().addAll(
                labelNombreExperimento,
                labelEstadoAutomata,
                panelRutasSintesis,
                panelMonitorProcedimiento,
                labelProgreso,
                panelProgresoAutomata,
                labelGuia,
                panelGuiaProceso,
                sep,
                labelSelector,
                selectorExp,
                controles,
                new Region(),
                labelAcciones,
                panelAcciones
        );

        ScrollPane scrollCentro = new ScrollPane(panel);
        scrollCentro.setFitToWidth(true);
        scrollCentro.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scrollCentro;
    }

    private VBox construirPanelRutasSintesis() {
        VBox box = new VBox(6);
        box.getStyleClass().add("box-rutas-sintesis");
        box.setVisible(false);
        box.setManaged(false);
        return box;
    }

    private VBox construirPanelMonitorProcedimiento() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel-monitor-procedimiento");
        panel.setPadding(new Insets(12));

        HBox cabecera = new HBox(8);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        Label titulo = new Label("🔬 MONITOR DE PROCEDIMIENTO EXPERIMENTAL Y DISPOSICIÓN FÍSICA");
        titulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #58a6ff; -fx-font-size: 13px;");
        cabecera.getChildren().add(titulo);

        FlowPane barraParametros = new FlowPane(8, 8);
        barraParametros.getStyleClass().add("barra-parametros-equipo");

        labelEquipoMonitor = new Label("Equipo: —");
        labelEquipoMonitor.getStyleClass().add("pill-parametro");

        labelTempMonitor = new Label("Temp: —");
        labelTempMonitor.getStyleClass().add("pill-parametro");

        labelPresionMonitor = new Label("Presión: —");
        labelPresionMonitor.getStyleClass().add("pill-parametro");

        labelAtmosferaMonitor = new Label("Atmósfera: —");
        labelAtmosferaMonitor.getStyleClass().add("pill-parametro");

        labelCatalizadorMonitor = new Label("Catalizador: —");
        labelCatalizadorMonitor.getStyleClass().add("pill-parametro");

        barraParametros.getChildren().addAll(
                labelEquipoMonitor,
                labelTempMonitor,
                labelPresionMonitor,
                labelAtmosferaMonitor,
                labelCatalizadorMonitor
        );

        Label lblReactivos = new Label("ESTADO INDIVIDUAL DE ELEMENTOS / REACTIVOS (MANEJO FÍSICO Y ESPACIAL):");
        lblReactivos.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8b949e;");

        gridTarjetasReactivos = new FlowPane(10, 10);
        Label sinReactivos = new Label("Selecciona un experimento para ver cómo se maneja cada elemento.");
        sinReactivos.getStyleClass().add("texto-secundario");
        gridTarjetasReactivos.getChildren().add(sinReactivos);

        labelNarrativaProcedimiento = new Label("Elige una síntesis para cargar el protocolo experimental.");
        labelNarrativaProcedimiento.getStyleClass().add("box-narrativa-procedimiento");
        labelNarrativaProcedimiento.setWrapText(true);

        labelProximaAccion = new Label("Próxima acción: —");
        labelProximaAccion.setStyle("-fx-font-size: 11px; -fx-text-fill: #7ee787; -fx-font-weight: bold;");

        panel.getChildren().addAll(
                cabecera,
                barraParametros,
                lblReactivos,
                gridTarjetasReactivos,
                labelNarrativaProcedimiento,
                labelProximaAccion
        );

        return panel;
    }

    private void actualizarPanelRutasSintesis(String idReaccion) {
        panelRutasSintesis.getChildren().clear();
        List<ProtocoloProcedimiento.InfoRutaSintesis> rutas = ProtocoloProcedimiento.obtenerRutasDisponibles(idReaccion);
        if (rutas.size() <= 1) {
            panelRutasSintesis.setVisible(false);
            panelRutasSintesis.setManaged(false);
            return;
        }

        panelRutasSintesis.setVisible(true);
        panelRutasSintesis.setManaged(true);

        HBox cabecera = new HBox(8);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        Label titulo = new Label("🔄 RUTAS ALTERNATIVAS DE SÍNTESIS DISPONIBLES");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #e3b341;");
        cabecera.getChildren().add(titulo);

        Label desc = new Label("Este compuesto puede sintetizarse por diferentes métodos químicos/físicos:");
        desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");

        FlowPane botonesRuta = new FlowPane(8, 8);
        CatalogoQuimico cat = CatalogoQuimico.predeterminado();

        for (var ruta : rutas) {
            boolean esActual = ruta.idReaccion().equalsIgnoreCase(idReaccion);
            Button btnRuta = new Button((esActual ? "▶ (Ruta actual) " : "⚡ Cambiar a: ") + ruta.nombreRuta());
            btnRuta.getStyleClass().add("boton-cambiar-ruta");
            if (esActual) {
                btnRuta.setStyle("-fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                btnRuta.setStyle("-fx-background-color: #21262d; -fx-text-fill: #58a6ff; -fx-border-color: #388bfd;");
                btnRuta.setOnAction(e -> {
                    if (sesion.isActiva()) {
                        log("⚠ Finaliza el experimento en curso antes de cambiar de ruta.");
                        return;
                    }
                    cat.buscarReaccion(ruta.idReaccion()).ifPresent(this::seleccionarExperimentoPorReaccion);
                });
            }
            btnRuta.setTooltip(new Tooltip(ruta.metodo() + "\n" + ruta.equipo() + "\n" + ruta.descripcionCientifica()));
            botonesRuta.getChildren().add(btnRuta);
        }

        panelRutasSintesis.getChildren().addAll(cabecera, desc, botonesRuta);
    }

    private void actualizarMonitorProcedimiento(String idReaccion, String estadoAutomataId) {
        if (idReaccion == null || idReaccion.isBlank()) {
            limpiarMonitorProcedimiento();
            return;
        }

        ProtocoloProcedimiento.InfoPasoProcedimiento info =
                ProtocoloProcedimiento.obtenerProtocoloPaso(idReaccion, estadoAutomataId);

        labelEquipoMonitor.setText("🏭 Equipo: " + info.equipoTecnico());
        labelTempMonitor.setText(String.format("🌡 Temp: %.0f / %.0f °C", info.temperaturaActual(), info.temperaturaObjetivo()));
        labelPresionMonitor.setText(String.format("⚙ Presión: %.1f / %.1f atm", info.presionActual(), info.presionObjetivo()));
        labelAtmosferaMonitor.setText("💨 Atmósfera: " + info.atmosferaRequerida());
        labelCatalizadorMonitor.setText("⚗ Cat/Medio: " + info.catalizadorEstado());

        labelNarrativaProcedimiento.setText(info.explicacionManejo());
        labelProximaAccion.setText("👉 Próxima acción requerida: " + info.accionSiguienteSugerida());

        gridTarjetasReactivos.getChildren().clear();
        for (ProtocoloProcedimiento.InfoReactivoFase rf : info.reactivosFases()) {
            VBox card = new VBox(3);
            card.getStyleClass().add("card-reactivo-fase");
            card.setPadding(new Insets(6, 8, 6, 8));

            HBox fila1 = new HBox(6);
            fila1.setAlignment(Pos.CENTER_LEFT);
            Label formula = new Label(rf.formula());
            formula.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 13px;");

            Label badgeManejo = new Label(rf.estadoManejo().name().replace("_", " "));
            badgeManejo.getStyleClass().addAll("badge-manejo", "badge-manejo-" + rf.estadoManejo().name().toLowerCase());
            fila1.getChildren().addAll(formula, badgeManejo);

            Label lblRol = new Label("Rol: " + rf.rol() + " | Fase: " + rf.faseFisica());
            lblRol.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b949e;");

            Label lblDisp = new Label("Disposición: " + rf.descripcionUbicacion());
            lblDisp.setStyle("-fx-font-size: 10px; -fx-text-fill: #c9d1d9; -fx-font-style: italic;");
            lblDisp.setWrapText(true);
            lblDisp.setMaxWidth(220);

            card.getChildren().addAll(fila1, lblRol, lblDisp);
            gridTarjetasReactivos.getChildren().add(card);
        }

        actualizarPanelRutasSintesis(idReaccion);
    }

    private void limpiarMonitorProcedimiento() {
        if (labelEquipoMonitor == null) return;
        labelEquipoMonitor.setText("Equipo: —");
        labelTempMonitor.setText("Temp: —");
        labelPresionMonitor.setText("Presión: —");
        labelAtmosferaMonitor.setText("Atmósfera: —");
        labelCatalizadorMonitor.setText("Catalizador: —");
        labelNarrativaProcedimiento.setText("Selecciona un experimento para visualizar el protocolo experimental detallado.");
        labelProximaAccion.setText("Próxima acción: —");
        gridTarjetasReactivos.getChildren().clear();
        Label sinReactivos = new Label("Selecciona un experimento para ver cómo se maneja cada elemento.");
        sinReactivos.getStyleClass().add("texto-secundario");
        gridTarjetasReactivos.getChildren().add(sinReactivos);
        if (panelRutasSintesis != null) {
            panelRutasSintesis.setVisible(false);
            panelRutasSintesis.setManaged(false);
        }
    }

    private Button crearBotonExperimento(
            String etiqueta,
            String ecuacion,
            String tipo
    ) {
        Button btn = new Button(etiqueta);
        btn.getStyleClass().add("boton-secundario");
        btn.setTooltip(new Tooltip(ecuacion));
        btn.setOnAction(e -> seleccionarExperimento(tipo));
        return btn;
    }

    private void actualizarSelectorExperimentos() {
        selectorExp.getChildren().clear();
        CatalogoQuimico cat = CatalogoQuimico.predeterminado();

        for (Reaccion r : cat.obtenerReacciones()) {
            if (puedeHacer(r.getId(), cat)) {
                String ecuacion = formatearEcuacion(r);
                String etiqueta = obtenerEtiquetaBoton(r);
                Button btn = new Button(etiqueta);
                btn.getStyleClass().add("boton-secundario");
                btn.setTooltip(new Tooltip(r.getNombre() + "\n" + ecuacion));
                btn.setOnAction(e -> seleccionarExperimentoPorReaccion(r));
                selectorExp.getChildren().add(btn);
            }
        }

        if (selectorExp.getChildren().isEmpty()) {
            Label vacio = new Label("Selecciona reactivos en el Almacén para ver experimentos posibles.");
            vacio.getStyleClass().add("texto-secundario");
            selectorExp.getChildren().add(vacio);
        }
    }

    private String obtenerEtiquetaBoton(Reaccion r) {
        String icono = obtenerIconoReaccion(r.getId());
        String id = r.getId();
        if ("R_DIAMANTE_HPHT".equalsIgnoreCase(id)) {
            return "💎 Diamante [Ruta HPHT: 5.5 GPa / Fundente]";
        }
        if ("R_DIAMANTE_CVD".equalsIgnoreCase(id)) {
            return "💎 Diamante [Ruta CVD: Plasma Microondas]";
        }
        if ("R_CO2".equalsIgnoreCase(id)) {
            return "💨 CO₂ [Combustión Directa C]";
        }
        if ("R_COMBUSTION_CH4".equalsIgnoreCase(id)) {
            return "🔥 CO₂ + H₂O [Combustión Metano]";
        }
        if ("R_CALCINACION_CACO3".equalsIgnoreCase(id)) {
            return "🏛 CaO + CO₂ [Calcinación CaCO₃]";
        }
        if ("R_OXIDACION_CA".equalsIgnoreCase(id)) {
            return "🏛 CaO [Oxidación Directa Ca]";
        }
        if ("R_DISOLUCION_NACL".equalsIgnoreCase(id)) {
            return "🧂 NaCl(ac) [Disolución e Hidratación]";
        }
        if ("R_SEPARACION_NACL".equalsIgnoreCase(id)) {
            return "⚡ NaCl Cristalino [Evaporación/Crecimiento]";
        }
        return icono + " " + r.getNombre();
    }

    private void seleccionarExperimentoPorReaccion(Reaccion r) {
        if (sesion.isActiva()) {
            log("⚠ Finaliza el experimento actual antes de cambiar.");
            return;
        }

        experimentoSeleccionado = fabricaExperimento.crearExperimentoParaReaccion(r);
        reaccionSeleccionada = r;
        automataSeleccionado = FabricaAutomata.crearAutomataParaReaccion(r.getId());
        log("→ Experimento seleccionado: " + r.getNombre() + " (" + formatearEcuacion(r) + ")");

        labelNombreExperimento.setText(
                "Experimento: " + experimentoSeleccionado.getNombre()
        );
        botonIniciar.setDisable(false);
        actualizarInventario(experimentoSeleccionado);
        actualizarGuiaProceso(automataSeleccionado);
        actualizarProgresoAutomata(automataSeleccionado, false);
        actualizarDisponibilidadAcciones();
        actualizarMonitorProcedimiento(r.getId(), automataSeleccionado != null ? automataSeleccionado.getEstadoActual().getId() : "q0");
    }

    private String formatearEcuacion(Reaccion r) {
        StringBuilder sb = new StringBuilder();
        boolean primero = true;
        for (var entry : r.getReactivos().entrySet()) {
            if (!primero) sb.append(" + ");
            int c = entry.getValue();
            if (c > 1) sb.append(c);
            sb.append(entry.getKey().getFormula());
            primero = false;
        }
        sb.append(" → ");
        primero = true;
        for (var entry : r.getProductos().entrySet()) {
            if (!primero) sb.append(" + ");
            int c = entry.getValue();
            if (c > 1) sb.append(c);
            sb.append(entry.getKey().getFormula());
            primero = false;
        }
        return sb.toString();
    }

    private String obtenerIconoReaccion(String id) {
        if (id.contains("H2O")) return "💧";
        if (id.contains("CO2") || id.contains("CH4") || id.contains("SO2")) return "🔥";
        if (id.contains("NH3")) return "🧪";
        if (id.contains("NaCl")) return "🧂";
        if (id.contains("HCl")) return "🟡";
        if (id.contains("Fe2O3") || id.contains("CuO")) return "🔩";
        if (id.contains("DIAMANTE")) return "💎";
        if (id.contains("CaO") || id.contains("CaOH2") || id.contains("CaCO3")) return "🏛";
        if (id.contains("SiO2") || id.contains("Al2O3")) return "🧱";
        if (id.contains("MgO")) return "⚡";
        return "⚗";
    }

    private boolean puedeHacer(String reaccionId, CatalogoQuimico cat) {
        return cat.buscarReaccion(reaccionId).map(r -> {
            for (Sustancia s : r.getReactivos().keySet()) {
                if (!elementosSeleccionados.contains(s.getId())) return false;
            }
            return true;
        }).orElse(false);
    }

    private void construirBotonesAcciones() {
        for (AccionLaboratorio accion : AccionLaboratorio.values()) {
            Button btn = new Button(formatearAccion(accion));
            btn.getStyleClass().add("boton-accion");
            btn.setId("btn-accion-" + accion.name());
            btn.setUserData(accion);
            btn.setOnAction(e -> accionLaboratorio(accion));
            panelAcciones.getChildren().add(btn);
        }
    }

    private VBox construirPanelLog() {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(8, 16, 12, 16));
        panel.setMaxHeight(180);

        Label titulo = new Label("REGISTRO DE LABORATORIO");
        titulo.getStyleClass().add("titulo-seccion");

        areaLog = new TextArea();
        areaLog.getStyleClass().add("area-log");
        areaLog.setEditable(false);
        areaLog.setWrapText(true);
        areaLog.setPrefHeight(140);
        VBox.setVgrow(areaLog, Priority.ALWAYS);

        panel.getChildren().addAll(titulo, areaLog);
        return panel;
    }

    // ---------------------------------------------------------------
    // Acciones del usuario
    // ---------------------------------------------------------------

    private void seleccionarExperimento(String tipo) {
        if (sesion.isActiva()) {
            log("⚠ Finaliza el experimento actual antes de cambiar.");
            return;
        }

        CatalogoQuimico catalogo = CatalogoQuimico.predeterminado();

        switch (tipo) {
            case "agua" -> {
                experimentoSeleccionado = fabricaExperimento.crearExperimentoSintesisAgua();
                reaccionSeleccionada = catalogo.buscarReaccion("R_H2O").orElseThrow();
                automataSeleccionado = FabricaAutomata.crearAutomataEstandar();
                log("→ Experimento seleccionado: Síntesis de Agua (2H₂ + O₂ → 2H₂O)");
            }
            case "carbono" -> {
                experimentoSeleccionado = fabricaExperimento.crearExperimentoCombustionCarbono();
                reaccionSeleccionada = catalogo.buscarReaccion("R_CO2").orElseThrow();
                automataSeleccionado = FabricaAutomata.crearAutomataSimple();
                log("→ Experimento seleccionado: Combustión de Carbono (C + O₂ → CO₂)");
            }
            case "amoniaco" -> {
                experimentoSeleccionado = fabricaExperimento.crearExperimentoSintesisAmoniaco();
                reaccionSeleccionada = catalogo.buscarReaccion("R_NH3").orElseThrow();
                automataSeleccionado = FabricaAutomata.crearAutomataHaberBosch();
                log("→ Experimento seleccionado: Síntesis de Amoníaco — Haber-Bosch");
            }
            case "nacl" -> {
                experimentoSeleccionado = fabricaExperimento.crearExperimentoSintesisNaCl();
                reaccionSeleccionada = catalogo.buscarReaccion("R_NaCl").orElseThrow();
                automataSeleccionado = FabricaAutomata.crearAutomataSimple();
                log("→ Experimento seleccionado: Síntesis de Cloruro de Sodio");
            }
            case "hcl" -> {
                experimentoSeleccionado = fabricaExperimento.crearExperimentoSintesisHCl();
                reaccionSeleccionada = catalogo.buscarReaccion("R_HCl").orElseThrow();
                automataSeleccionado = FabricaAutomata.crearAutomataSintesisHCl();
                log("→ Experimento seleccionado: Síntesis de Ácido Clorhídrico (H₂ + Cl₂ → 2HCl)");
            }
            case "hierro" -> {
                experimentoSeleccionado = fabricaExperimento.crearExperimentoOxidacionHierro();
                reaccionSeleccionada = catalogo.buscarReaccion("R_Fe2O3").orElseThrow();
                automataSeleccionado = FabricaAutomata.crearAutomataOxidacionHierro();
                log("→ Experimento seleccionado: Oxidación del Hierro");
            }
            default -> {
                log("⚠ Tipo de experimento desconocido: " + tipo);
                return;
            }
        }

        labelNombreExperimento.setText(
                "Experimento: " + experimentoSeleccionado.getNombre()
        );
        botonIniciar.setDisable(false);
        actualizarInventario(experimentoSeleccionado);
        actualizarGuiaProceso(automataSeleccionado);
        actualizarProgresoAutomata(automataSeleccionado, false);
        actualizarDisponibilidadAcciones();
        actualizarMonitorProcedimiento(reaccionSeleccionada.getId(), automataSeleccionado != null ? automataSeleccionado.getEstadoActual().getId() : "q0");
    }

    private void accionIniciar() {
        if (experimentoSeleccionado == null || reaccionSeleccionada == null) {
            log("⚠ Selecciona un experimento primero.");
            return;
        }

        sesion.iniciar(experimentoSeleccionado, reaccionSeleccionada, automataSeleccionado);
        // Los callbacks de ObservadorSesion actualizarán la UI
    }

    private void accionAbandonar() {
        sesion.abandonar();
    }

    private void accionLaboratorio(AccionLaboratorio accion) {
        try {
            sesion.ejecutarAccion(accion);
            // Actualizar resaltado de botones según el nuevo estado
            actualizarDisponibilidadAcciones();
        } catch (IllegalStateException ex) {
            log("✗ " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // ObservadorSesion (llamado desde el dominio)
    // ---------------------------------------------------------------

    @Override
    public void onExperimentoIniciado(Experimento experimento) {
        Platform.runLater(() -> {
            labelEstadoExperimento.setText("EN PROGRESO");
            labelEstadoExperimento.getStyleClass()
                    .setAll("badge-en-progreso");
            labelEstadoAutomata.setText(
                    "Estado: " + sesion.getExperimento()
                            .map(e -> e.getEstado().name())
                            .orElse("?")
            );
            botonIniciar.setDisable(true);
            botonAbandonar.setDisable(false);
            panelAcciones.setDisable(false);
            log("▶ Experimento iniciado: " + experimento.getNombre());
            log("  Objetivo: " + experimento.getObjetivo().getDescripcion());
            log("  Reacción: " + reaccionSeleccionada.getNombre());
            actualizarProgresoAutomata(sesion.getAutomataActual(), true);
            actualizarDisponibilidadAcciones();
            String reaccionId = reaccionSeleccionada != null ? reaccionSeleccionada.getId() : (experimentoSeleccionado != null ? experimentoSeleccionado.getId() : "");
            String estadoId = sesion.getAutomataActual() != null ? sesion.getAutomataActual().getEstadoActual().getId() : "q0";
            actualizarMonitorProcedimiento(reaccionId, estadoId);
        });
    }

    @Override
    public void onAccionEjecutada(ResultadoAccion resultado) {
        Platform.runLater(() -> {
            String icono = resultado.isExitoso() ? "✔" : "✗";
            log(icono + " [" + resultado.getEstadoActual().getNombre() + "] "
                    + resultado.getMensaje());
            labelEstadoAutomata.setText(
                    "Estado autómata: " + resultado.getEstadoActual().getNombre()
            );
            // Actualizar barra de progreso del autómata
            actualizarProgresoAutomata(sesion.getAutomataActual(), true);
            actualizarDisponibilidadAcciones();
            String reaccionId = reaccionSeleccionada != null ? reaccionSeleccionada.getId() : "";
            String estadoId = resultado.getEstadoActual() != null ? resultado.getEstadoActual().getId() : "";
            actualizarMonitorProcedimiento(reaccionId, estadoId);
        });
    }

    @Override
    public void onExperimentoCompletado(Experimento experimento) {
        Platform.runLater(() -> {
            labelEstadoExperimento.setText("COMPLETADO");
            labelEstadoExperimento.getStyleClass()
                    .setAll("badge-completado");
            botonAbandonar.setDisable(true);
            panelAcciones.setDisable(true);
            labelEstadoAutomata.setText("¡Experimento completado exitosamente!");
            log("🏆 ¡Experimento completado!");

            if (experimento.getResultado() != null) {
                log("   Productos obtenidos:");
                for (Map.Entry<Sustancia, Cantidad> entry
                        : experimento.getResultado().getProductos().entrySet()) {
                    log("   • " + entry.getKey().getNombre()
                            + ": " + String.format("%.2f", entry.getValue().getValor())
                            + " " + entry.getValue().getUnidad());
                }
            }

            // Actualizar cuaderno y contador
            actualizarHistorial();
            actualizarContadorExitos();

            String reaccionId = reaccionSeleccionada != null ? reaccionSeleccionada.getId() : "";
            actualizarMonitorProcedimiento(reaccionId, "qF");

            // Habilitar nuevo experimento
            botonIniciar.setDisable(false);
            experimentoSeleccionado = null;
            reaccionSeleccionada = null;
            limpiarProgresoAutomata();
        });
    }

    @Override
    public void onExperimentoAbandonado(Experimento experimento) {
        Platform.runLater(() -> {
            labelEstadoExperimento.setText("FALLIDO");
            labelEstadoExperimento.getStyleClass()
                    .setAll("badge-fallido");
            botonAbandonar.setDisable(true);
            panelAcciones.setDisable(true);
            botonIniciar.setDisable(true);
            labelEstadoAutomata.setText("Experimento abandonado.");
            log("✕ Experimento abandonado por el jugador.");

            // Actualizar historial con el intento fallido
            actualizarHistorial();

            experimentoSeleccionado = null;
            reaccionSeleccionada = null;
            limpiarProgresoAutomata();
        });
    }

    // ---------------------------------------------------------------
    // Helpers de UI
    // ---------------------------------------------------------------

    private void actualizarInventario(Experimento experimento) {
        panelInventario.getChildren().clear();

        if (experimento.getInventario().estaVacio()) {
            Label sinItems = new Label("Inventario vacío");
            sinItems.getStyleClass().add("texto-secundario");
            panelInventario.getChildren().add(sinItems);
            return;
        }

        for (Map.Entry<Sustancia, Cantidad> entry
                : experimento.getInventario().obtenerCantidades().entrySet()) {

            HBox fila = new HBox(8);
            fila.getStyleClass().add("fila-inventario");
            fila.setAlignment(Pos.CENTER_LEFT);

            Label formula = new Label(entry.getKey().getFormula());
            formula.getStyleClass().add("texto-formula");
            formula.setMinWidth(44);

            Label nombreSust = new Label(entry.getKey().getNombre());
            nombreSust.getStyleClass().add("texto-secundario");
            HBox.setHgrow(nombreSust, Priority.ALWAYS);

            Label cantidad = new Label(
                    String.format("%.2f %s",
                            entry.getValue().getValor(),
                            entry.getValue().getUnidad()
                    )
            );
            cantidad.getStyleClass().add("texto-cantidad");

            fila.getChildren().addAll(formula, nombreSust, cantidad);
            panelInventario.getChildren().add(fila);
        }
    }

    /**
     * Actualiza el panel de progreso del autómata mostrando cada estado
     * con un indicador: completado (✔ verde), actual (● cian parpadeante),
     * o pendiente (○ gris).
     */
    private void actualizarProgresoAutomata(AutomataQuimico automata, boolean activo) {
        panelProgresoAutomata.getChildren().clear();

        if (automata == null) {
            Label sinProceso = new Label("—  Sin proceso activo");
            sinProceso.getStyleClass().add("texto-secundario");
            panelProgresoAutomata.getChildren().add(sinProceso);
            return;
        }

        // Reconstruir la secuencia de estados a partir de las transiciones
        List<EstadoQuimico> secuencia = obtenerSecuenciaEstados(automata);
        EstadoQuimico estadoActual = automata.getEstadoActual();
        boolean encontrado = false;

        for (int i = 0; i < secuencia.size(); i++) {
            EstadoQuimico estado = secuencia.get(i);

            Label badge = new Label();
            if (!activo) {
                badge.setText("○ " + estado.getNombre());
                badge.getStyleClass().add("badge-estado-pendiente");
            } else if (!encontrado && !estado.getId().equals(estadoActual.getId())) {
                // Ya pasamos este estado
                badge.setText("✔ " + estado.getNombre());
                badge.getStyleClass().add("badge-estado-completado");
            } else if (estado.getId().equals(estadoActual.getId())) {
                // Estado actual
                badge.setText("● " + estado.getNombre());
                badge.getStyleClass().add("badge-estado-actual");
                encontrado = true;
            } else {
                // Pendiente
                badge.setText("○ " + estado.getNombre());
                badge.getStyleClass().add("badge-estado-pendiente");
            }

            panelProgresoAutomata.getChildren().add(badge);

            // Flecha separadora entre estados (no al final)
            if (i < secuencia.size() - 1) {
                Label flecha = new Label("→");
                flecha.getStyleClass().add("texto-secundario");
                panelProgresoAutomata.getChildren().add(flecha);
            }
        }
    }

    /**
     * Construye la guía de proceso: muestra la secuencia de acciones requeridas
     * en orden, tal como lo define el autómata seleccionado.
     */
    private void actualizarGuiaProceso(AutomataQuimico automata) {
        panelGuiaProceso.getChildren().clear();

        if (automata == null) {
            Label sinGuia = new Label("Selecciona un experimento para ver la secuencia de acciones.");
            sinGuia.getStyleClass().add("texto-secundario");
            sinGuia.setWrapText(true);
            panelGuiaProceso.getChildren().add(sinGuia);
            return;
        }

        List<EstadoQuimico> secuencia = obtenerSecuenciaEstados(automata);
        // La guía muestra el par (estado_origen → acción → estado_destino)
        for (int i = 0; i < secuencia.size() - 1; i++) {
            EstadoQuimico origen = secuencia.get(i);

            // Buscar la transición saliente de este estado
            List<TransicionQuimica> trans = automata.getTransicionesDesdeEstado(origen);
            if (trans.isEmpty()) break;

            AccionLaboratorio accion = trans.get(0).getAccion();

            HBox paso = new HBox(6);
            paso.setAlignment(Pos.CENTER_LEFT);

            Label numLabel = new Label((i + 1) + ".");
            numLabel.getStyleClass().add("texto-secundario");
            numLabel.setMinWidth(18);

            Label accionLabel = new Label(formatearAccion(accion));
            accionLabel.getStyleClass().add("badge-accion-guia");

            paso.getChildren().addAll(numLabel, accionLabel);
            panelGuiaProceso.getChildren().add(paso);
        }
    }

    private void limpiarProgresoAutomata() {
        panelProgresoAutomata.getChildren().clear();
        Label sinProceso = new Label("—  Sin proceso activo");
        sinProceso.getStyleClass().add("texto-secundario");
        panelProgresoAutomata.getChildren().add(sinProceso);

        panelGuiaProceso.getChildren().clear();
        Label sinGuia = new Label("Selecciona un experimento para ver la secuencia.");
        sinGuia.getStyleClass().add("texto-secundario");
        sinGuia.setWrapText(true);
        panelGuiaProceso.getChildren().add(sinGuia);

        limpiarMonitorProcedimiento();
    }

    /**
     * Resalta los botones de acción según disponibilidad en el estado actual del autómata.
     * Acciones válidas se muestran en azul brillante; las inválidas se oscurecen.
     */
    private void actualizarDisponibilidadAcciones() {
        if (!sesion.isActiva()) return;

        AutomataQuimico automata = sesion.getAutomataActual();
        List<TransicionQuimica> disponibles = automata.getTransicionesDisponibles();
        List<AccionLaboratorio> accionesDisponibles = new ArrayList<>();
        for (TransicionQuimica t : disponibles) {
            accionesDisponibles.add(t.getAccion());
        }

        for (javafx.scene.Node node : panelAcciones.getChildren()) {
            if (node instanceof Button btn && btn.getUserData() instanceof AccionLaboratorio accion) {
                if (accionesDisponibles.contains(accion)) {
                    btn.getStyleClass().setAll("boton-accion", "boton-accion-disponible");
                } else {
                    btn.getStyleClass().setAll("boton-accion", "boton-accion-inactivo");
                }
            }
        }
    }

    /**
     * Obtiene la secuencia lineal de estados del autómata recorriendo las transiciones
     * desde el estado inicial hasta el estado final.
     */
    private List<EstadoQuimico> obtenerSecuenciaEstados(AutomataQuimico automata) {
        List<EstadoQuimico> secuencia = new ArrayList<>();
        // Usamos un enfoque: partimos del estado actual y también necesitamos los anteriores.
        // Por simplicidad, pedimos al autómata los estados y hacemos BFS desde el estado inicial.
        // El autómata expone getEstados() y getTransicionesDisponibles() sólo desde el estado actual,
        // así que usaremos getEstados() y ordenamos por el recorrido de transiciones.

        // Recorrer desde el estado actual usando getTransicionesDisponibles()
        // Para la guía/progreso necesitamos recorrer TODOS los estados en orden.
        // El AutomataQuimico ahora expone getTransicionesDesdeEstado(), así que usamos eso.
        EstadoQuimico actual = null;
        // Encontramos el estado inicial (sin transiciones entrantes) entre todos los estados
        java.util.Set<String> destinos = new java.util.HashSet<>();
        for (EstadoQuimico e : automata.getEstados()) {
            List<TransicionQuimica> trans = automata.getTransicionesDesdeEstado(e);
            for (TransicionQuimica t : trans) {
                destinos.add(t.getEstadoDestino().getId());
            }
        }
        for (EstadoQuimico e : automata.getEstados()) {
            if (!destinos.contains(e.getId())) {
                actual = e;
                break;
            }
        }
        if (actual == null && !automata.getEstados().isEmpty()) {
            actual = automata.getEstados().get(0);
        }

        // Recorrer en cadena
        java.util.Set<String> visitados = new java.util.HashSet<>();
        while (actual != null && !visitados.contains(actual.getId())) {
            secuencia.add(actual);
            visitados.add(actual.getId());
            List<TransicionQuimica> trans = automata.getTransicionesDesdeEstado(actual);
            if (trans.isEmpty()) break;
            actual = trans.get(0).getEstadoDestino();
        }

        return secuencia;
    }

    private void actualizarHistorial() {
        panelHistorial.getChildren().clear();

        var historial = sesion.getCuaderno().getHistorial();

        if (historial.isEmpty()) {
            Label vacio = new Label("Sin registros aún.");
            vacio.getStyleClass().add("texto-secundario");
            panelHistorial.getChildren().add(vacio);
            return;
        }

        // Mostrar del más reciente al más antiguo
        for (int i = historial.size() - 1; i >= 0; i--) {
            RegistroExperimento reg = historial.get(i);
            panelHistorial.getChildren().add(
                    crearTarjetaHistorial(reg)
            );
        }
    }

    private VBox crearTarjetaHistorial(RegistroExperimento reg) {
        VBox tarjeta = new VBox(4);
        tarjeta.getStyleClass().add(
                reg.isExitoso()
                        ? "tarjeta-historial-exito"
                        : "tarjeta-historial-fallo"
        );

        String icono = reg.isExitoso() ? "✔" : "✕";
        Label titulo = new Label(icono + " " + reg.getNombreExperimento());
        titulo.getStyleClass().add("texto-normal");

        Label fecha = new Label(
                reg.getFecha().toLocalDate()
                + " " + reg.getFecha().toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        fecha.getStyleClass().add("texto-secundario");

        tarjeta.getChildren().addAll(titulo, fecha);
        tarjeta.setOnMouseClicked(e -> mostrarModalAutomataHistorial(reg));
        tarjeta.setStyle("-fx-cursor: hand;");

        return tarjeta;
    }

    private void mostrarModalAutomataHistorial(RegistroExperimento reg) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Autómata del Proceso - " + reg.getNombreExperimento());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().addAll(getStylesheets());
        dialogPane.getStyleClass().add("dialogo-automata");
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        // Cabecera del modal
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(14, 18, 12, 18));
        headerBox.setStyle("-fx-background-color: #0d1117; -fx-border-color: #21262d; -fx-border-width: 0 0 1 0;");

        HBox filaTitulo = new HBox(12);
        filaTitulo.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label("⚗ " + reg.getNombreExperimento());
        lblTitulo.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #58a6ff;");

        Label badgeResultado = new Label(reg.isExitoso() ? "✔ COMPLETADO CON ÉXITO" : "✕ ABANDONADO / NO COMPLETADO");
        if (reg.isExitoso()) {
            badgeResultado.getStyleClass().add("badge-estado-completado");
        } else {
            badgeResultado.setStyle("-fx-background-color: #3b1219; -fx-text-fill: #f85149; -fx-border-color: #da3633; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 3 8; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
        filaTitulo.getChildren().addAll(lblTitulo, badgeResultado);

        Label lblSubtitulo = new Label("Fecha y hora: "
                + reg.getFecha().toLocalDate() + " "
                + reg.getFecha().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                + "  |  Proceso ID: " + (reg.getIdProceso() != null ? reg.getIdProceso() : "N/A"));
        lblSubtitulo.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");

        headerBox.getChildren().addAll(filaTitulo, lblSubtitulo);

        // Contenido
        VBox contenido = new VBox(14);
        contenido.setPadding(new Insets(16, 18, 16, 18));
        contenido.setPrefWidth(780);

        // 1. Leyenda de estados del autómata
        HBox leyenda = new HBox(12);
        leyenda.setAlignment(Pos.CENTER_LEFT);
        leyenda.setStyle("-fx-background-color: #0d1117; -fx-padding: 8 14; -fx-border-color: #30363d; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label titLeyenda = new Label("NOTACIÓN:");
        titLeyenda.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #8b949e;");

        Label legIni = new Label("▶ Estado Inicial (q0)");
        legIni.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #79c0ff;");

        Label legInter = new Label("● Estado Intermedio");
        legInter.setStyle("-fx-font-size: 11px; -fx-text-fill: #c9d1d9;");

        Label legFin = new Label("◎ Estado Final (qF)");
        legFin.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #3fb950;");

        Label legAcc = new Label("──[Acción]──▶ Transición");
        legAcc.setStyle("-fx-font-size: 11px; -fx-text-fill: #d29922;");

        leyenda.getChildren().addAll(titLeyenda, legIni, legInter, legFin, legAcc);

        // 2. Título de sección de autómata
        Label lblSecuencia = new Label("DIAGRAMA DEL AUTÓMATA QUÍMICO (ESTADOS Y TRANSICIONES)");
        lblSecuencia.getStyleClass().add("titulo-seccion");

        // 3. Diagrama gráfico del autómata
        VBox diagramaBox = construirDiagramaAutomata(reg);

        // 4. Panel de pasos y resultados
        VBox panelPasos = new VBox(8);
        panelPasos.setStyle("-fx-background-color: #0d1117; -fx-padding: 12; -fx-border-color: #21262d; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label lblDetalle = new Label("HISTORIAL CRONOLÓGICO DE TRANSICIONES");
        lblDetalle.getStyleClass().add("titulo-seccion");

        VBox listaItems = new VBox(4);
        if (reg.getPasosTransicion() != null && !reg.getPasosTransicion().isEmpty()) {
            int num = 1;
            for (var paso : reg.getPasosTransicion()) {
                Label pasoLbl = new Label("  Paso " + (num++) + ": (" + paso.estadoOrigen() + ") ──[" + paso.accion() + "]──▶ (" + paso.estadoDestino() + ")");
                pasoLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #e6edf3; -fx-font-family: 'Consolas', monospace;");
                listaItems.getChildren().add(pasoLbl);
            }
        } else if (!reg.getAccionesEjecutadas().isEmpty()) {
            int num = 1;
            for (var entry : reg.getAccionesEjecutadas().entrySet()) {
                Label pasoLbl = new Label("  Paso " + (num++) + ": Acción [" + entry.getKey() + "] ──▶ Estado: " + entry.getValue());
                pasoLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #e6edf3; -fx-font-family: 'Consolas', monospace;");
                listaItems.getChildren().add(pasoLbl);
            }
        } else {
            Label sinPasos = new Label("  No se registraron acciones.");
            sinPasos.getStyleClass().add("texto-secundario");
            listaItems.getChildren().add(sinPasos);
        }

        if (reg.getResultado() != null && !reg.getResultado().getProductos().isEmpty()) {
            Label lblSust = new Label("Sustancias sintetizadas en este intento:");
            lblSust.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #3fb950; -fx-padding: 8 0 2 0;");
            listaItems.getChildren().add(lblSust);

            reg.getResultado().getProductos().forEach((s, cant) -> {
                Label prod = new Label("  ✔ " + s.getFormula() + " (" + s.getNombre() + "): " + String.format("%.2f", cant.getValor()) + " " + cant.getUnidad());
                prod.setStyle("-fx-font-size: 12px; -fx-text-fill: #79c0ff; -fx-font-weight: bold;");
                listaItems.getChildren().add(prod);
            });
        }

        panelPasos.getChildren().addAll(lblDetalle, listaItems);

        contenido.getChildren().addAll(leyenda, lblSecuencia, diagramaBox, panelPasos);

        ScrollPane scrollModal = new ScrollPane(contenido);
        scrollModal.setFitToWidth(true);
        scrollModal.setMaxHeight(520);
        scrollModal.setStyle("-fx-background-color: #161b22; -fx-border-color: transparent;");

        VBox layoutDialog = new VBox(headerBox, scrollModal);
        dialogPane.setContent(layoutDialog);
        dialog.showAndWait();
    }

    private VBox construirDiagramaAutomata(RegistroExperimento reg) {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(12));
        contenedor.setStyle("-fx-background-color: #0d1117; -fx-border-color: #30363d; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        AutomataQuimico automataRef = null;
        if (reg.getIdProceso() != null) {
            try {
                automataRef = FabricaAutomata.crearAutomataParaReaccion(reg.getIdProceso());
            } catch (Exception ignored) {}
        }

        List<EstadoQuimico> estados = (automataRef != null)
                ? obtenerSecuenciaEstados(automataRef)
                : List.of();

        EstadoQuimico estInicial = (automataRef != null) ? automataRef.getEstadoInicial() : null;
        Set<EstadoQuimico> estFinales = (automataRef != null) ? automataRef.getEstadosFinales() : Set.of();

        HBox flujoNodos = new HBox(10);
        flujoNodos.setAlignment(Pos.CENTER_LEFT);
        flujoNodos.setPadding(new Insets(14, 10, 14, 10));

        if (!estados.isEmpty()) {
            Set<String> estadosAlcanzados = new HashSet<>();
            if (reg.getPasosTransicion() != null && !reg.getPasosTransicion().isEmpty()) {
                for (var paso : reg.getPasosTransicion()) {
                    estadosAlcanzados.add(paso.estadoOrigen());
                    estadosAlcanzados.add(paso.estadoDestino());
                }
            } else {
                estadosAlcanzados.addAll(reg.getAccionesEjecutadas().values());
            }
            if (estInicial != null) {
                estadosAlcanzados.add(estInicial.getId());
                estadosAlcanzados.add(estInicial.getNombre());
            }

            for (int i = 0; i < estados.size(); i++) {
                EstadoQuimico est = estados.get(i);
                boolean esIni = (estInicial != null && est.getId().equals(estInicial.getId()));
                boolean esFin = estFinales.stream().anyMatch(f -> f.getId().equals(est.getId()));
                boolean alcanzado = estadosAlcanzados.contains(est.getId()) || estadosAlcanzados.contains(est.getNombre());

                VBox tarjetaNodo = crearTarjetaNodoAutomata(est, esIni, esFin, alcanzado, i);
                flujoNodos.getChildren().add(tarjetaNodo);

                if (i < estados.size() - 1) {
                    EstadoQuimico siguiente = estados.get(i + 1);
                    String nombreAccion = "TRANSICIÓN";
                    if (automataRef != null) {
                        for (var t : automataRef.getTransicionesDesdeEstado(est)) {
                            if (t.getEstadoDestino().getId().equals(siguiente.getId())) {
                                nombreAccion = formatearAccion(t.getAccion());
                                break;
                            }
                        }
                    }
                    VBox conector = crearConectorTransicion(nombreAccion, alcanzado);
                    flujoNodos.getChildren().add(conector);
                }
            }
        } else {
            Label info = new Label("Estado Inicial: " + reg.getEstadoInicial() + "  ──▶  Estados Finales: " + reg.getEstadosFinales());
            info.getStyleClass().add("texto-info");
            flujoNodos.getChildren().add(info);
        }

        ScrollPane scrollFlujo = new ScrollPane(flujoNodos);
        scrollFlujo.setFitToHeight(true);
        scrollFlujo.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollFlujo.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        contenedor.getChildren().add(scrollFlujo);
        return contenedor;
    }

    private VBox crearTarjetaNodoAutomata(EstadoQuimico estado, boolean esInicial, boolean esFinal, boolean alcanzado, int indice) {
        VBox nodo = new VBox(4);
        nodo.setAlignment(Pos.CENTER);
        nodo.setMinWidth(140);
        nodo.setMaxWidth(170);

        Label etiquetaTipo = new Label();
        if (esInicial) {
            etiquetaTipo.setText("▶ ESTADO INICIAL (q0)");
            etiquetaTipo.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #79c0ff;");
            nodo.getStyleClass().add("nodo-automata-inicial");
        } else if (esFinal) {
            etiquetaTipo.setText("◎ ESTADO FINAL (qF)");
            etiquetaTipo.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #3fb950;");
            nodo.getStyleClass().add("nodo-automata-final");
        } else {
            etiquetaTipo.setText("● ESTADO q" + indice);
            etiquetaTipo.setStyle("-fx-font-size: 9px; -fx-text-fill: #8b949e;");
            nodo.getStyleClass().add("nodo-automata-intermedio");
        }

        Label lblNombre = new Label(estado.getNombre());
        lblNombre.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-text-alignment: center;");
        lblNombre.setWrapText(true);

        Label lblEstadoAlcanzado = new Label(alcanzado ? "✔ Visitado" : "○ No alcanzado");
        lblEstadoAlcanzado.setStyle("-fx-font-size: 9px; -fx-text-fill: " + (alcanzado ? "#3fb950;" : "#6e7681;"));

        nodo.getChildren().addAll(etiquetaTipo, lblNombre, lblEstadoAlcanzado);
        return nodo;
    }

    private VBox crearConectorTransicion(String nombreAccion, boolean activa) {
        VBox conector = new VBox(2);
        conector.setAlignment(Pos.CENTER);
        conector.getStyleClass().add("conector-transicion");

        Label badgeAccion = new Label(nombreAccion);
        badgeAccion.getStyleClass().add("badge-transicion-accion");
        if (!activa) {
            badgeAccion.setStyle("-fx-opacity: 0.55; -fx-background-color: #161b22; -fx-text-fill: #8b949e; -fx-border-color: #30363d;");
        }

        Label flecha = new Label("──────▶");
        flecha.setStyle("-fx-font-family: monospace; -fx-font-size: 12px; -fx-text-fill: " + (activa ? "#58a6ff;" : "#484f58;"));

        conector.getChildren().addAll(badgeAccion, flecha);
        return conector;
    }

    private void actualizarContadorExitos() {
        long exitos = sesion.getCuaderno().getHistorial()
                .stream()
                .filter(RegistroExperimento::isExitoso)
                .count();
        labelContadorExitos.setText("✓ " + exitos + " completado" + (exitos != 1 ? "s" : ""));
    }

    private void log(String mensaje) {
        Platform.runLater(() -> {
            String hora = LocalTime.now().format(HORA);
            areaLog.appendText("[" + hora + "] " + mensaje + "\n");
        });
    }

    private static String formatearAccion(AccionLaboratorio accion) {
        return switch (accion) {
            case CARGAR_REACTIVO    -> "📦 Cargar Reactivo";
            case AGREGAR_CATALIZADOR -> "⚗ Catalizador";
            case CALENTAR           -> "🔥 Calentar";
            case ENFRIAR            -> "❄ Enfriar";
            case COMPRIMIR          -> "⬇ Comprimir";
            case DESCOMPRIMIR       -> "⬆ Descomprimir";
            case MEZCLAR            -> "🔄 Mezclar";
            case ACTIVAR_IGNICION   -> "⚡ Ignición";
            case ESPERAR            -> "⏳ Esperar";
            case FINALIZAR          -> "✔ Finalizar";
            case AGREGAR_AGUA       -> "💧 Agregar Agua";
            case INICIAR_PLASMA     -> "🌟 Plasma";
            case SELECCIONAR_METODO -> "📋 Método";
        };
    }
}
