package com.chemlab.ui.game;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.ResultadoAccion;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.factory.FabricaSesion;
import com.chemlab.game.world.EstacionLaboratorio;
import com.chemlab.game.world.JugadorLaboratorio;
import com.chemlab.game.world.MapaLaboratorio;
import com.chemlab.game.ObservadorSesion;
import com.chemlab.game.ProductoAlmacenado;
import com.chemlab.game.GestorMisiones;
import com.chemlab.game.PerfilJugador;
import com.chemlab.game.ProgresoPerfil;
import com.chemlab.game.procedure.PlanificadorEstacionesProcedimiento;
import com.chemlab.game.procedure.ProtocoloProcedimiento;
import com.chemlab.game.SesionJuego;
import com.chemlab.game.ModoJuego;
import com.chemlab.model.entity.CuadernoLaboratorio;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.RegistroExperimento;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.repository.RepositorioPerfilesJson;
import com.chemlab.model.enums.EstadoRegistroExperimento;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import com.chemlab.ui.components.IconoSustancia;
import com.chemlab.ui.components.LienzoAutomataRegistro;
import com.chemlab.ui.notebook.VistaCuadernoQuimico;
import com.chemlab.ui.workbench.PanelInteraccionProcedimiento;
import com.chemlab.ui.workbench.VistaPuestoLaboratorio;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Primera vista jugable de ChemLab: mapa 2D, desplazamiento, estaciones y
 * menús de juego. El dibujo no ejecuta química; las acciones pasan por
 * {@link SesionJuego} y el dominio existente.
 */
public final class VistaJuego2D extends StackPane implements ObservadorSesion {

    private enum Menu { MODO, ALMACEN, CUADERNO, TUTORIAL, ANALISIS, MISIONES }
    private enum SeccionCuaderno { INVENTARIO, HISTORIAL, QUIMICA }

    private static final DateTimeFormatter FECHA_REGISTRO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final long DURACION_AVISO_NANOS = 3_500_000_000L;
    private static final long DURACION_FEEDBACK_NANOS = 9_000_000_000L;
    private static final Set<KeyCode> TECLAS_MOVIMIENTO = Set.of(
            KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D,
            KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT
    );

    private final CatalogoQuimico catalogo;
    private final FabricaExperimento fabricaExperimento;
    private final SesionJuego sesion;
    private final GestorMisiones gestorMisiones;
    private final RepositorioPerfilesJson repositorioPerfiles;
    private final Runnable alCambiarPerfil;
    private PerfilJugador perfilJugador;
    private final MapaLaboratorio mapa;
    private final PlanificadorEstacionesProcedimiento planificadorEstaciones =
            new PlanificadorEstacionesProcedimiento();
    private final JugadorLaboratorio jugador = new JugadorLaboratorio();
    private final RenderizadorLaboratorio2D renderizador;
    private final Set<KeyCode> teclasPresionadas = EnumSet.noneOf(KeyCode.class);
    private final Set<String> reactivosDisponibles = new HashSet<>();
    private final AnimationTimer reloj;
    private PresentadorModoJuego presentadorModo = new PresentadorModoTutorial();

    private final Label labelEstado = new Label("Explora el laboratorio");
    private final Label labelObjetivo = new Label("Selecciona una práctica con M");
    private final Label labelPuestoCercano = new Label("WASD mover · F interactuar");
    private final Label labelAviso = new Label();
    private final BorderPane capaHud = new BorderPane();
    private VistaPuestoLaboratorio vistaPuesto;
    private PanelInteraccionProcedimiento panelInteraccion;
    private ProtocoloProcedimiento.InfoPasoProcedimiento protocoloActivo;
    private AccionLaboratorio accionEnCurso;
    private double progresoInteraccion;
    private double duracionInteraccion;
    private boolean operacionEnMarcha;
    private String reaccionVisualId;
    private final List<ProductoAlmacenado> productosEnMesa = new ArrayList<>();
    private final List<ProductoAlmacenado> resultadosAlmacenados = new ArrayList<>();
    private ProductoAlmacenado productoEnMano;
    private EstacionLaboratorio ultimaEstacionDeTrabajo;
    private boolean resaltarAlmacen;
    private String muestraEnProceso = "";
    private boolean trasladoPendiente;

    private Experimento experimentoSeleccionado;
    private Experimento experimentoVisible;
    private Reaccion reaccionSeleccionada;
    private AutomataQuimico automataSeleccionado;
    private EstacionLaboratorio estacionActiva;
    private EstacionLaboratorio estacionCercana;
    private Menu menuAbierto;
    private SeccionCuaderno seccionCuaderno = SeccionCuaderno.INVENTARIO;
    private StackPane overlayMenu;
    private VBox listaExperimentos;
    private VBox listaReactivos;
    private VBox panelExperimentos;
    private VBox panelResultados;
    private FlowPane galeriaResultados;
    private Button botonTabExperimentos;
    private Button botonTabResultados;
    private Button botonCambiarModo;
    private Button botonMisiones;
    private TextField busquedaExperimentos;
    private String filtroExperimentos = "";
    private VBox contenidoCuaderno;
    private Button botonInventarioCuaderno;
    private Button botonHistorialCuaderno;
    private Button botonQuimicaCuaderno;
    private Label detalleExperimento;
    private Button botonIniciarExperimento;
    private Button botonSolicitarPista;
    private StackPane cabecera;
    private HBox barraControles;
    private long ultimoPulso;
    private long avisoHasta;
    private String avisoActual = "";
    private boolean modoSeleccionInicial = true;
    private boolean tutorialInicialMostrado;
    private boolean cambioPerfilEnCurso;
    private GeneradorRetroalimentacionPractica.Retroalimentacion retroalimentacionActual;

    public VistaJuego2D() {
        this(null, null, null);
    }

    public VistaJuego2D(PerfilJugador perfilJugador, RepositorioPerfilesJson repositorioPerfiles) {
        this(perfilJugador, repositorioPerfiles, null);
    }

    public VistaJuego2D(
            PerfilJugador perfilJugador,
            RepositorioPerfilesJson repositorioPerfiles,
            Runnable alCambiarPerfil
    ) {
        if ((perfilJugador == null) != (repositorioPerfiles == null)) {
            throw new IllegalArgumentException("El perfil y su repositorio deben proporcionarse juntos.");
        }
        this.perfilJugador = perfilJugador;
        this.repositorioPerfiles = repositorioPerfiles;
        this.alCambiarPerfil = alCambiarPerfil;
        catalogo = CatalogoQuimico.predeterminado();
        fabricaExperimento = new FabricaExperimento(catalogo);
        CuadernoLaboratorio cuaderno = new CuadernoLaboratorio();
        gestorMisiones = perfilJugador == null
                ? new GestorMisiones()
                : new GestorMisiones(
                        perfilJugador.progreso().misionesCompletadas(),
                        perfilJugador.progreso().rutaMisionesIniciada()
                );
        if (perfilJugador != null) {
            perfilJugador.progreso().restaurarCuaderno(cuaderno, catalogo);
        }
        sesion = FabricaSesion.crearSesion(
                catalogo,
                FabricaAutomata.crearAutomataEstandar(),
                ModoJuego.TUTORIAL,
                cuaderno
        );
        mapa = MapaLaboratorio.crearPredeterminado(catalogo, fabricaExperimento);
        renderizador = new RenderizadorLaboratorio2D(mapa, jugador);
        reactivosDisponibles.addAll(catalogo.obtenerSustancias().stream().map(Sustancia::getId).toList());
        sesion.agregarObservador(this);

        getStyleClass().add("game-root");
        getStylesheets().add(getClass().getResource("/styles/game-2d.css").toExternalForm());
        setFocusTraversable(true);
        renderizador.widthProperty().bind(widthProperty());
        renderizador.heightProperty().bind(heightProperty());
        renderizador.setMouseTransparent(false);
        renderizador.setOnMouseMoved(this::alMoverPunteroMundo);
        renderizador.setOnMousePressed(this::alPresionarPunteroMundo);
        renderizador.setOnMouseReleased(this::alSoltarPunteroMundo);
        renderizador.setOnMouseClicked(this::alHacerClicEnMundo);
        renderizador.setOnMouseExited(evento -> {
            resaltarAlmacen = false;
            renderizador.setCursor(Cursor.DEFAULT);
        });

        cabecera = construirCabecera();
        barraControles = construirBarraControles();
        actualizarVisibilidadMisiones();
        labelEstado.getStyleClass().add("game-status");
        VBox pieHud = new VBox(8, cabecera, barraControles);
        pieHud.setAlignment(Pos.CENTER);
        capaHud.setBottom(pieHud);
        capaHud.setMouseTransparent(false);
        capaHud.setPickOnBounds(false);
        labelAviso.getStyleClass().add("game-toast");
        labelAviso.setWrapText(true);
        labelAviso.setMaxWidth(960);
        labelAviso.setVisible(false);
        labelAviso.setManaged(false);
        labelAviso.setMouseTransparent(true);

        getChildren().addAll(renderizador, capaHud, labelAviso);
        StackPane.setAlignment(capaHud, Pos.CENTER);
        StackPane.setAlignment(labelAviso, Pos.TOP_CENTER);
        StackPane.setMargin(labelAviso, new Insets(82, 16, 0, 16));
        BorderPane.setMargin(pieHud, new Insets(0, 14, 12, 14));

        addEventFilter(KeyEvent.KEY_PRESSED, this::alPresionarTecla);
        addEventFilter(KeyEvent.KEY_RELEASED, this::alSoltarTecla);
        abrirSelectorModo();

        reloj = new AnimationTimer() {
            @Override
            public void handle(long ahora) {
                double delta = ultimoPulso == 0 ? 0 : Math.min((ahora - ultimoPulso) / 1_000_000_000.0, 0.05);
                ultimoPulso = ahora;
                actualizarMovimiento(delta);
                if (menuAbierto != Menu.MODO) actualizarOperacion(delta);
                actualizarPuestoCercano();
                actualizarAviso(ahora);
                dibujarMundo();
            }
        };
        reloj.start();
    }

    private StackPane construirCabecera() {
        HBox contenido = new HBox(18);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setMaxWidth(Double.MAX_VALUE);
        contenido.setMaxHeight(Double.MAX_VALUE);
        contenido.getStyleClass().add("game-hud-content");

        Label marca = new Label("⚗ CHEMLAB"
                + (perfilJugador == null ? "" : " · " + perfilJugador.nombre()));
        marca.getStyleClass().add("game-title");
        Button cambiarPerfil = null;
        if (alCambiarPerfil != null) {
            cambiarPerfil = new Button("Cambiar perfil");
            cambiarPerfil.getStyleClass().add("game-secondary-button");
            cambiarPerfil.setOnAction(evento -> solicitarCambioPerfil());
            cambiarPerfil.setAccessibleText("Guardar progreso y cambiar de perfil");
        }
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        labelObjetivo.getStyleClass().add("game-objective");
        contenido.getChildren().add(marca);
        if (cambiarPerfil != null) contenido.getChildren().add(cambiarPerfil);
        contenido.getChildren().addAll(espacio, labelObjetivo);

        StackPane barra = new StackPane(contenido, labelEstado);
        barra.getStyleClass().add("game-hud");
        barra.setMinHeight(58);
        barra.setPrefHeight(58);
        labelEstado.setAlignment(Pos.CENTER);
        labelEstado.setWrapText(true);
        labelEstado.setMaxWidth(280);
        labelEstado.setMaxHeight(46);
        StackPane.setAlignment(contenido, Pos.CENTER_LEFT);
        StackPane.setAlignment(labelEstado, Pos.CENTER);
        return barra;
    }

    private HBox construirBarraControles() {
        HBox barra = new HBox(18);
        barra.setAlignment(Pos.CENTER);
        barra.getStyleClass().add("game-controls");
        labelPuestoCercano.getStyleClass().add("game-station-hint");
        botonSolicitarPista = new Button("Solicitar pista · 0/2");
        botonSolicitarPista.getStyleClass().add("game-secondary-button");
        botonSolicitarPista.setVisible(false);
        botonSolicitarPista.setManaged(false);
        botonSolicitarPista.setOnAction(evento -> solicitarPista());
        barra.getChildren().addAll(
                new Label("WASD / flechas · mover"),
                new Label("F · usar puesto"),
                new Label("E · inventario y cuaderno"),
                new Label("M · almacén · V · modo"),
                botonMisiones = crearBotonMisiones(),
                botonCambiarModo = crearBotonCambiarModo(),
                botonSolicitarPista,
                labelPuestoCercano
        );
        return barra;
    }

    private Button crearBotonMisiones() {
        Button boton = new Button("Misiones · G");
        boton.getStyleClass().add("game-secondary-button");
        boton.setOnAction(evento -> alternarMenu(Menu.MISIONES));
        boton.setAccessibleText("Abrir misiones");
        boolean visible = sesion.getModoJuego().permiteMisiones();
        boton.setVisible(visible);
        boton.setManaged(visible);
        return boton;
    }

    private void actualizarVisibilidadMisiones() {
        if (botonMisiones == null) return;
        boolean visible = sesion.getModoJuego().permiteMisiones();
        botonMisiones.setVisible(visible);
        botonMisiones.setManaged(visible);
    }

    private Button crearBotonCambiarModo() {
        Button boton = new Button();
        boton.getStyleClass().add("game-secondary-button");
        boton.setOnAction(evento -> abrirSelectorModo());
        actualizarTextoBotonModo();
        return boton;
    }

    private void actualizarTextoBotonModo() {
        if (botonCambiarModo == null) return;
        botonCambiarModo.setText(sesion.getModoJuego() == ModoJuego.TUTORIAL
                ? "Modo: Tutorial · V"
                : "Modo: Difícil · V");
        botonCambiarModo.setAccessibleText("Cambiar modo de juego");
    }

    private void alPresionarTecla(KeyEvent evento) {
        KeyCode tecla = evento.getCode();
        if (menuAbierto == Menu.TUTORIAL || menuAbierto == Menu.ANALISIS) {
            if (tecla == KeyCode.ESCAPE) {
                cerrarMenu();
                evento.consume();
            } else if (TECLAS_MOVIMIENTO.contains(tecla)
                    || tecla == KeyCode.E || tecla == KeyCode.M
                    || tecla == KeyCode.V || tecla == KeyCode.F) {
                evento.consume();
            }
            return;
        }
        if ((tecla == KeyCode.E || tecla == KeyCode.M || tecla == KeyCode.V || tecla == KeyCode.G)
                && (evento.getTarget() instanceof TextInputControl
                || getScene() != null && getScene().getFocusOwner() instanceof TextInputControl)) {
            teclasPresionadas.remove(tecla);
            return;
        }
        boolean primeraPulsacion = teclasPresionadas.add(tecla);
        if (!primeraPulsacion) {
            evento.consume();
            return;
        }

        switch (tecla) {
            case E -> alternarMenu(Menu.CUADERNO);
            case M -> alternarMenu(Menu.ALMACEN);
            case V -> abrirSelectorModo();
            case G -> alternarMenu(Menu.MISIONES);
            case F -> {
                if (menuAbierto == null) interactuarConPuesto();
            }
            case ESCAPE -> cerrarMenuOAcciones();
            default -> { }
        }
        if (tecla == KeyCode.E || tecla == KeyCode.M || tecla == KeyCode.V
                || tecla == KeyCode.F || tecla == KeyCode.G || tecla == KeyCode.ESCAPE) {
            evento.consume();
        }
    }

    private void alSoltarTecla(KeyEvent evento) {
        teclasPresionadas.remove(evento.getCode());
    }

    private void actualizarMovimiento(double delta) {
        if (menuAbierto != null || vistaPuesto != null && vistaPuesto.isVisible()
                || operacionEnMarcha || delta <= 0) {
            return;
        }
        double dx = 0;
        double dy = 0;
        if (teclasPresionadas.contains(KeyCode.A) || teclasPresionadas.contains(KeyCode.LEFT)) dx--;
        if (teclasPresionadas.contains(KeyCode.D) || teclasPresionadas.contains(KeyCode.RIGHT)) dx++;
        if (teclasPresionadas.contains(KeyCode.W) || teclasPresionadas.contains(KeyCode.UP)) dy--;
        if (teclasPresionadas.contains(KeyCode.S) || teclasPresionadas.contains(KeyCode.DOWN)) dy++;
        jugador.mover(dx, dy, delta, mapa);
        if (dx != 0 || dy != 0) {
            cerrarVistaPuesto(true);
            estacionActiva = null;
            if (sesion.getModoJuego() == ModoJuego.DIFICIL && sesion.isActiva()) {
                sesion.salirPuesto();
                botonSolicitarPista.setVisible(true);
                botonSolicitarPista.setManaged(true);
            }
        }
    }

    private void actualizarPuestoCercano() {
        EstacionLaboratorio nuevo = mapa.estacionCercana(jugador.getX(), jugador.getY());
        estacionCercana = nuevo;
        boolean productoCercano = nuevo != null && productosEnMesa.stream().anyMatch(producto ->
                producto.estacionOrigen() == nuevo
                        && producto.estacionOrigen().distanciaHasta(jugador.getX(), jugador.getY()) <= 82);
        String pista = productoEnMano != null
                ? "Lleva el resultado al almacén · clic"
                : productoCercano ? "Producto en mesa · clic para recoger"
                : nuevo == null ? "Busca un puesto de trabajo" : "Cerca: " + nuevo.nombre() + " · F";
        if (!pista.equals(labelPuestoCercano.getText())) labelPuestoCercano.setText(pista);
    }

    private void actualizarAviso(long ahora) {
        if (labelAviso.isVisible() && ahora > avisoHasta) {
            labelAviso.setVisible(false);
            labelAviso.setManaged(false);
        }
    }

    private void mostrarAviso(String mensaje) {
        mostrarAviso(mensaje, DURACION_AVISO_NANOS);
    }

    private void mostrarAviso(String mensaje, long duracionNanos) {
        avisoActual = mensaje;
        labelAviso.setText(avisoActual);
        labelAviso.setManaged(true);
        labelAviso.setVisible(true);
        avisoHasta = System.nanoTime() + duracionNanos;
    }

    private void interactuarConPuesto() {
        if (operacionEnMarcha) {
            mostrarAviso("El equipo está trabajando. Espera a que termine el procedimiento.");
            return;
        }
        if (estacionCercana == null) {
            mostrarAviso("Acércate a una estación para interactuar.");
            return;
        }
        if (!sesion.isActiva() || reaccionSeleccionada == null || experimentoSeleccionado == null) {
            mostrarAviso("Selecciona un experimento desde el menú M.");
            return;
        }
        EstacionLaboratorio requerida = obtenerEstacionRequerida();
        if (!presentadorModo.permiteAccesoLibre()
                && (requerida == null || estacionCercana != requerida)) {
            mostrarAviso("El paso actual debe realizarse en: "
                    + (requerida == null ? "otro puesto" : requerida.nombre()));
            return;
        }

        teclasPresionadas.removeAll(TECLAS_MOVIMIENTO);
        estacionActiva = estacionCercana;
        ultimaEstacionDeTrabajo = estacionActiva;
        if (presentadorModo.permiteAccesoLibre()) {
            sesion.registrarEntradaPuesto(estacionActiva.nombre(), estacionActiva.tipoEquipo());
            botonSolicitarPista.setVisible(false);
            botonSolicitarPista.setManaged(false);
            avisoActual = "";
            labelAviso.setText("");
            labelAviso.setVisible(false);
            labelAviso.setManaged(false);
        }
        labelEstado.setText("PUESTO ACTIVO · " + estacionActiva.nombre());
        mostrarAccionesDisponibles();
    }

    private EstacionLaboratorio obtenerEstacionRequerida() {
        if (!sesion.isActiva() || reaccionSeleccionada == null || experimentoSeleccionado == null) {
            return null;
        }
        if (presentadorModo.permiteAccesoLibre()) return null;
        return primeraEstacionDe(reaccionSeleccionada, experimentoSeleccionado, sesion.getAutomataActual());
    }

    private EstacionLaboratorio primeraEstacionDe(Reaccion reaccion, Experimento experimento,
                                                   AutomataQuimico automata) {
        if (reaccion == null || experimento == null || automata == null) return null;
        List<TransicionQuimica> transiciones = automata.getTransicionesDisponibles();
        if (transiciones.isEmpty()) return null;
        TipoEquipo equipo = planificadorEstaciones.equipoRequerido(
                reaccion.getId(), automata.getEstadoActual().getId(),
                transiciones.get(0).getAccion(), experimento.getEquipo().getTipo());
        return mapa.buscarEstacionPorEquipo(equipo);
    }

    private void mostrarAccionesDisponibles() {
        if (vistaPuesto != null) {
            getChildren().remove(vistaPuesto);
            vistaPuesto = null;
        }
        String estadoId = sesion.getAutomataActual().getEstadoActual().getId();
        boolean modoDificil = presentadorModo.permiteAccesoLibre();
        protocoloActivo = presentadorModo.obtenerProtocolo(reaccionSeleccionada, estadoId);
        List<TransicionQuimica> transiciones = presentadorModo.obtenerTransiciones(
                sesion.getAutomataActual(), estacionActiva.tipoEquipo());
        vistaPuesto = new VistaPuestoLaboratorio(
                estacionActiva,
                reaccionSeleccionada,
                protocoloActivo,
                transiciones,
                this::construirControlInteraccion,
                this::cerrarPanelAcciones,
                () -> {
                    operacionEnMarcha = false;
                    accionEnCurso = null;
                    sesion.abandonar();
                },
                modoDificil,
                experimentoSeleccionado.getCondiciones(),
                this::abrirSelectorModo,
                estadoId
        );
        getChildren().add(2, vistaPuesto);
        vistaPuesto.toFront();
        if (!muestraEnProceso.isBlank()) {
            if (trasladoPendiente) {
                vistaPuesto.exigirTrasladoDeMuestra(muestraEnProceso, () -> {
                    trasladoPendiente = false;
                    mostrarAviso("Muestra colocada en " + estacionActiva.nombre() + ". Continúa el procedimiento.");
                });
            } else {
                vistaPuesto.mostrarMuestraEnPuesto(muestraEnProceso);
            }
        }
        if (transiciones.size() == 1) construirControlInteraccion(transiciones.get(0));
        if (retroalimentacionActual != null) {
            vistaPuesto.mostrarRetroalimentacion(
                    retroalimentacionActual.titulo(),
                    retroalimentacionActual.detalle(),
                    retroalimentacionActual.positiva()
            );
        }
        requestFocus();
    }

    private void construirControlInteraccion(TransicionQuimica transicion) {
        boolean modoDificil = sesion.getModoJuego() == ModoJuego.DIFICIL;
        Map<String, Sustancia> sustancias = catalogo.obtenerSustancias().stream()
                .collect(java.util.stream.Collectors.toMap(Sustancia::getId, sustancia -> sustancia));
        Set<String> materialesDisponibles = new LinkedHashSet<>(reactivosDisponibles);
        sesion.obtenerMaterialesDisponibles().keySet().stream()
                .map(Sustancia::getId)
                .forEach(materialesDisponibles::add);
        panelInteraccion = new PanelInteraccionProcedimiento(
                reaccionSeleccionada,
                experimentoSeleccionado,
                estacionActiva.tipoEquipo(),
                transicion.getAccion(),
                this::iniciarOperacion,
                this::confirmarColocacion,
                true,
                modoDificil ? materialesDisponibles : null,
                id -> {
                    if (modoDificil) {
                        boolean yaDisponible = sesion.obtenerMaterialesDisponibles().keySet().stream()
                                .anyMatch(sustancia -> sustancia.getId().equals(id));
                        if (!yaDisponible) {
                            catalogo.buscarSustancia(id).ifPresent(sustancia ->
                                    sesion.agregarSustancia(
                                            sustancia,
                                            new Cantidad(1, UnidadCantidad.GRAMOS)
                                    ));
                        }
                    }
                },
                sustancias,
                modoDificil,
                sesion.obtenerMaterialesDisponibles()
        );
        vistaPuesto.mostrarInteraccion(panelInteraccion, transicion.getAccion());
    }

    private void solicitarPista() {
        String puestoSiguiente = null;
        if (sesion.isActiva() && sesion.getModoJuego() == ModoJuego.DIFICIL
                && reaccionSeleccionada != null && experimentoSeleccionado != null) {
            EstacionLaboratorio estacion = primeraEstacionDe(
                    reaccionSeleccionada,
                    experimentoSeleccionado,
                    sesion.getAutomataActual()
            );
            if (estacion != null) puestoSiguiente = estacion.nombre();
        }
        sesion.solicitarPista(puestoSiguiente).ifPresentOrElse(
                this::mostrarAviso,
                () -> {
                    botonSolicitarPista.setDisable(true);
                    mostrarAviso("No quedan pistas disponibles o debes salir del puesto para solicitarlas.");
                }
        );
        botonSolicitarPista.setText("Solicitar pista · " + sesion.getCantidadPistasSolicitadas() + "/2");
        if (sesion.getCantidadPistasSolicitadas() >= 2) botonSolicitarPista.setDisable(true);
    }

    private void confirmarColocacion(AccionLaboratorio accion) {
        if (vistaPuesto != null && !vistaPuesto.transferenciaLista()) {
            mostrarAviso("Abre el equipo y coloca la muestra transportada antes de confirmar.");
            return;
        }
        if (panelInteraccion == null || !panelInteraccion.puedeConfirmarCarga()) {
            mostrarAviso("Carga incompleta: arrastra todos los materiales al recipiente antes de confirmar.");
            return;
        }
        ejecutarAccion(accion);
    }

    private void iniciarOperacion(AccionLaboratorio accion) {
        if (operacionEnMarcha || !sesion.isActiva()) return;
        if (vistaPuesto != null) vistaPuesto.anticiparOperacion(accion);
        accionEnCurso = accion;
        progresoInteraccion = 0;
        duracionInteraccion = duracionDeOperacion(accion);
        operacionEnMarcha = true;
    }

    private double duracionDeOperacion(AccionLaboratorio accion) {
        return switch (accion) {
            case CALENTAR, ENFRIAR, ESPERAR -> 4.0;
            case MEZCLAR, COMPRIMIR, DESCOMPRIMIR, INICIAR_PLASMA -> 3.0;
            case ACTIVAR_IGNICION, FINALIZAR -> 1.8;
            case MONTAR_EQUIPO -> 3.0;
            default -> 2.2;
        };
    }

    private void actualizarOperacion(double delta) {
        if (!operacionEnMarcha || delta <= 0) return;
        progresoInteraccion = Math.min(1, progresoInteraccion + delta / duracionInteraccion);
        String descripcion = descripcionProgreso(accionEnCurso, progresoInteraccion);
        if (panelInteraccion != null) {
            panelInteraccion.actualizarProgreso(progresoInteraccion, descripcion);
        }
        if (vistaPuesto != null) vistaPuesto.actualizarProceso(accionEnCurso, progresoInteraccion, descripcion);
        if (progresoInteraccion >= 1) {
            AccionLaboratorio accionTerminada = accionEnCurso;
            operacionEnMarcha = false;
            accionEnCurso = null;
            ejecutarAccion(accionTerminada);
        }
    }

    private String descripcionProgreso(AccionLaboratorio accion, double progreso) {
        if (protocoloActivo == null || accion == null) return "Operación: " + Math.round(progreso * 100) + "%";
        if (accion == AccionLaboratorio.CALENTAR) {
            double actual = interpolar(protocoloActivo.temperaturaActual(), protocoloActivo.temperaturaObjetivo(), progreso);
            return String.format(Locale.ROOT, "Calentando · %.0f / %.0f °C", actual, protocoloActivo.temperaturaObjetivo());
        }
        if (accion == AccionLaboratorio.ENFRIAR) {
            double actual = interpolar(protocoloActivo.temperaturaActual(), protocoloActivo.temperaturaObjetivo(), progreso);
            return String.format(Locale.ROOT, "Enfriando · %.0f / %.0f °C", actual, protocoloActivo.temperaturaObjetivo());
        }
        if (accion == AccionLaboratorio.COMPRIMIR || accion == AccionLaboratorio.DESCOMPRIMIR) {
            double actual = interpolar(protocoloActivo.presionActual(), protocoloActivo.presionObjetivo(), progreso);
            return String.format(Locale.ROOT, "Presión · %.3f / %.3f atm", actual, protocoloActivo.presionObjetivo());
        }
        String verbo = switch (accion) {
            case MONTAR_EQUIPO -> "Montando equipo";
            case MEZCLAR -> "Mezclando";
            case INICIAR_PLASMA -> "Estabilizando plasma";
            case ACTIVAR_IGNICION -> "Encendiendo";
            case ESPERAR -> "Tiempo de proceso";
            case FINALIZAR -> "Enfriando y extrayendo";
            default -> "Operación";
        };
        return verbo + " · " + Math.round(progreso * 100) + "%";
    }

    private double interpolar(double inicio, double fin, double progreso) {
        return inicio + (fin - inicio) * progreso;
    }

    private void ejecutarAccion(AccionLaboratorio accion) {
        try {
            sesion.ejecutarAccion(accion);
        } catch (IllegalStateException | IllegalArgumentException error) {
            mostrarAviso("No se pudo aplicar la acción: " + error.getMessage());
            if (sesion.isActiva() && estacionActiva != null) mostrarAccionesDisponibles();
        } finally {
            requestFocus();
        }
    }

    private void cerrarPanelAcciones() {
        if (operacionEnMarcha) return;
        cerrarVistaPuesto(true);
        estacionActiva = null;
        sesion.salirPuesto();
        if (sesion.getModoJuego() == ModoJuego.DIFICIL && sesion.isActiva()) {
            botonSolicitarPista.setVisible(true);
            botonSolicitarPista.setManaged(true);
        }
        requestFocus();
    }

    private void cerrarVistaPuesto(boolean limpiarInteraccion) {
        if (vistaPuesto != null) {
            getChildren().remove(vistaPuesto);
            vistaPuesto = null;
        }
        if (limpiarInteraccion) panelInteraccion = null;
    }

    private void cerrarMenuOAcciones() {
        if (menuAbierto == Menu.MODO && modoSeleccionInicial) return;
        if (menuAbierto != null) {
            cerrarMenu();
        } else {
            cerrarPanelAcciones();
        }
    }

    private void alternarMenu(Menu menu) {
        if (menu == Menu.MISIONES && !sesion.getModoJuego().permiteMisiones()) {
            mostrarAviso("Las misiones están disponibles únicamente en el Modo Difícil.");
            return;
        }
        if (menuAbierto == Menu.MODO && modoSeleccionInicial) return;
        if (operacionEnMarcha) {
            mostrarAviso("El equipo está en operación; espera a que termine antes de abrir otro menú.");
            return;
        }
        teclasPresionadas.removeAll(TECLAS_MOVIMIENTO);
        if (menuAbierto == menu) return;
        if (menuAbierto != null) cerrarMenu();
        cerrarPanelAcciones();
        if (menu == Menu.ALMACEN) {
            abrirMenuAlmacen();
        } else if (menu == Menu.CUADERNO) {
            abrirMenuCuaderno();
        } else if (menu == Menu.MISIONES) {
            abrirMenuMisiones();
        }
    }

    private void cerrarMenu() {
        if (overlayMenu != null) {
            getChildren().remove(overlayMenu);
            overlayMenu = null;
        }
        menuAbierto = null;
        contenidoCuaderno = null;
        botonInventarioCuaderno = null;
        botonHistorialCuaderno = null;
        botonQuimicaCuaderno = null;
        requestFocus();
    }

    private void abrirMenuMisiones() {
        menuAbierto = Menu.MISIONES;
        String subtitulo = gestorMisiones.estaIniciada()
                ? "Ruta de aprendizaje · el siguiente objetivo se desbloquea al completar la práctica actual."
                : "Elige una ruta guiada opcional. Al activarla, las prácticas se desbloquean en orden.";
        StackPane overlay = crearOverlay("Misiones y progreso", subtitulo);
        VBox tarjeta = (VBox) overlay.getUserData();
        tarjeta.setPrefSize(840, 560);
        tarjeta.setMaxWidth(940);
        tarjeta.setMaxHeight(610);

        VBox lista = new VBox(10);
        for (int indice = 0; indice < gestorMisiones.obtenerMisiones().size(); indice++) {
            GestorMisiones.Mision mision = gestorMisiones.obtenerMisiones().get(indice);
            String estado = gestorMisiones.estaCompletada(mision) ? "COMPLETADA"
                    : gestorMisiones.estaDesbloqueada(mision) ? "DISPONIBLE"
                    : gestorMisiones.estaIniciada() ? "BLOQUEADA" : "INICIA LA RUTA";
            Label titulo = new Label((indice + 1) + ". " + mision.titulo() + "  ·  " + estado);
            titulo.getStyleClass().add("game-section-title");
            Label objetivo = new Label(mision.objetivo());
            objetivo.getStyleClass().add("game-debrief-text");
            Label aprendizaje = new Label("Aprenderás: " + mision.aprendizaje());
            aprendizaje.getStyleClass().add("game-menu-subtitle");
            aprendizaje.setWrapText(true);
            VBox bloque = new VBox(4, titulo, objetivo, aprendizaje);
            bloque.getStyleClass().add("game-debrief-section");
            lista.getChildren().add(bloque);
        }

        ScrollPane scroll = crearScroll(lista);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button iniciar = new Button(gestorMisiones.estaIniciada()
                ? gestorMisiones.estaCompletada() ? "Ruta completada" : "Continuar misión"
                : "Iniciar ruta");
        iniciar.getStyleClass().add("game-start-button");
        iniciar.setDisable(gestorMisiones.estaCompletada());
        iniciar.setOnAction(evento -> {
            gestorMisiones.iniciarRuta();
            guardarProgreso();
            cerrarMenu();
            actualizarObjetivoHud("Completa la práctica desbloqueada para avanzar.");
            mostrarAviso(gestorMisiones.estaCompletada()
                    ? "¡Ruta de aprendizaje completada!"
                    : "Ruta iniciada. Abre el almacén para comenzar la práctica disponible.");
            alternarMenu(Menu.ALMACEN);
        });

        Button cerrar = new Button("Cerrar");
        cerrar.getStyleClass().add("game-secondary-button");
        cerrar.setOnAction(evento -> cerrarMenu());
        HBox acciones = new HBox(10, cerrar, iniciar);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        tarjeta.getChildren().addAll(scroll, acciones);
        getChildren().add(overlay);
        overlayMenu = overlay;
        overlay.toFront();
        iniciar.requestFocus();
    }

    private void actualizarObjetivoHud(String objetivo) {
        if (!sesion.getModoJuego().permiteMisiones() || !gestorMisiones.estaIniciada()) {
            labelObjetivo.setText(objetivo);
            return;
        }

        if (gestorMisiones.estaCompletada()) {
            labelObjetivo.setText("RUTA COMPLETADA · " + objetivo);
            return;
        }
        GestorMisiones.Mision mision = gestorMisiones.obtenerMisionActual().orElseThrow();
        labelObjetivo.setText("MISIÓN " + (gestorMisiones.obtenerCantidadCompletadas() + 1)
                + "/" + gestorMisiones.obtenerMisiones().size() + " · "
                + mision.titulo() + " — " + objetivo);
    }

    private boolean guardarProgreso() {
        if (perfilJugador == null) {
            return true;
        }
        perfilJugador = perfilJugador.conProgreso(
                ProgresoPerfil.capturar(sesion.getCuaderno(), gestorMisiones));
        try {
            repositorioPerfiles.guardar(perfilJugador);
            return true;
        } catch (IllegalStateException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("No se pudo guardar el perfil");
            alerta.setHeaderText("El progreso de " + perfilJugador.nombre() + " no quedó guardado.");
            alerta.setContentText(e.getMessage());
            alerta.show();
            return false;
        }
    }

    private void solicitarCambioPerfil() {
        if (alCambiarPerfil == null) {
            return;
        }
        boolean practicaActiva = sesion.isActiva();
        if (practicaActiva) {
            Alert confirmacion = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "La práctica actual se registrará como abandonada antes de cambiar de perfil.",
                    ButtonType.YES,
                    ButtonType.NO
            );
            confirmacion.setTitle("Cambiar de perfil");
            confirmacion.setHeaderText("¿Guardar y salir de este perfil?");
            if (confirmacion.showAndWait().filter(ButtonType.YES::equals).isEmpty()) {
                return;
            }
        }

        cambioPerfilEnCurso = true;
        if (practicaActiva) {
            operacionEnMarcha = false;
            accionEnCurso = null;
            sesion.abandonar();
        }
        if (!guardarProgreso()) {
            cambioPerfilEnCurso = false;
            return;
        }
        reloj.stop();
        sesion.quitarObservador(this);
        alCambiarPerfil.run();
    }

    private StackPane crearOverlay(String tituloTexto, String subtituloTexto) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("game-menu-overlay");
        overlay.setPickOnBounds(true);
        overlay.setMinSize(0, 0);
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox tarjeta = new VBox(14);
        tarjeta.getStyleClass().add("game-menu-card");
        tarjeta.setPrefSize(1040, 590);
        tarjeta.setMaxWidth(1120);
        tarjeta.setMaxHeight(640);
        VBox.setVgrow(tarjeta, Priority.ALWAYS);

        Label titulo = new Label(tituloTexto);
        titulo.getStyleClass().add("game-menu-title");
        Label subtitulo = new Label(subtituloTexto);
        subtitulo.getStyleClass().add("game-menu-subtitle");
        Button cerrar = new Button("Cerrar · Esc");
        cerrar.getStyleClass().add("game-close-button");
        cerrar.setOnAction(evento -> cerrarMenu());
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox encabezado = new HBox(14, new VBox(3, titulo, subtitulo), espacio, cerrar);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        Region divisor = new Region();
        divisor.setMinHeight(1);
        divisor.setPrefHeight(1);
        divisor.setMaxHeight(1);
        divisor.setMaxWidth(Double.MAX_VALUE);
        divisor.getStyleClass().add("game-menu-divider");
        tarjeta.getChildren().addAll(encabezado, divisor);
        overlay.getChildren().add(tarjeta);
        StackPane.setAlignment(tarjeta, Pos.CENTER);
        overlay.setUserData(tarjeta);
        return overlay;
    }

    private void abrirSelectorModo() {
        if (menuAbierto == Menu.MODO) return;
        if (menuAbierto != null) cerrarMenu();
        menuAbierto = Menu.MODO;
        StackPane overlay = crearOverlay(
                "Cambiar modo de juego",
                "El cambio aplica de inmediato. Si hay un experimento activo, se registrará como abandonado."
        );
        VBox tarjeta = (VBox) overlay.getUserData();
        if (modoSeleccionInicial) {
            HBox encabezado = (HBox) tarjeta.getChildren().getFirst();
            Button cerrar = (Button) encabezado.getChildren().getLast();
            cerrar.setVisible(false);
            cerrar.setManaged(false);
        }
        Button tutorial = new Button(mensajeOpcionModo(
                ModoJuego.TUTORIAL,
                "Guía paso a paso, procedimientos y pistas del laboratorio."
        ));
        tutorial.getStyleClass().add("game-start-button");
        tutorial.setWrapText(true);
        tutorial.setMaxWidth(Double.MAX_VALUE);
        tutorial.setMinHeight(96);
        tutorial.setOnAction(evento -> seleccionarModo(ModoJuego.TUTORIAL));

        Button dificil = new Button(mensajeOpcionModo(
                ModoJuego.DIFICIL,
                "Experimentación libre, sin pistas iniciales y con resultados según tus decisiones."
        ));
        dificil.getStyleClass().add("game-secondary-button");
        dificil.setWrapText(true);
        dificil.setMaxWidth(Double.MAX_VALUE);
        dificil.setMinHeight(96);
        dificil.setOnAction(evento -> seleccionarModo(ModoJuego.DIFICIL));
        tarjeta.getChildren().addAll(tutorial, dificil);
        getChildren().add(overlay);
        overlayMenu = overlay;
        overlay.toFront();
    }

    private String mensajeOpcionModo(ModoJuego modo, String descripcion) {
        String estado = modo == sesion.getModoJuego() ? " · ACTUAL" : "";
        String nombre = modo == ModoJuego.TUTORIAL ? "MODO TUTORIAL" : "MODO DIFÍCIL";
        return nombre + estado + "\n" + descripcion;
    }

    private void seleccionarModo(ModoJuego modo) {
        boolean mostrarRecorridoInicial = modo == ModoJuego.TUTORIAL && !tutorialInicialMostrado;
        ModoJuego modoAnterior = sesion.getModoJuego();
        if (modo == modoAnterior) {
            modoSeleccionInicial = false;
            cerrarMenu();
            if (mostrarRecorridoInicial) {
                mostrarTutorialInicial();
            } else {
                mostrarAviso(modo == ModoJuego.TUTORIAL
                        ? "Continúas en Modo Tutorial."
                        : "Continúas en Modo Difícil.");
            }
            return;
        }
        operacionEnMarcha = false;
        accionEnCurso = null;
        progresoInteraccion = 0;
        sesion.salirPuesto();
        sesion.seleccionarModo(modo);
        presentadorModo = PresentadorModoJuego.para(modo);
        modoSeleccionInicial = false;
        cerrarVistaPuesto(true);
        estacionActiva = null;
        experimentoSeleccionado = null;
        reaccionSeleccionada = null;
        automataSeleccionado = null;
        reaccionVisualId = null;
        botonSolicitarPista.setVisible(false);
        botonSolicitarPista.setManaged(false);
        actualizarTextoBotonModo();
        actualizarVisibilidadMisiones();
        actualizarObjetivoHud(modo == ModoJuego.TUTORIAL
                ? "Modo Tutorial · selecciona una práctica con M"
                : "Modo Difícil · selecciona una práctica con M");
        cerrarMenu();
        if (mostrarRecorridoInicial) {
            mostrarTutorialInicial();
        } else {
            mostrarAviso(modoAnterior == ModoJuego.TUTORIAL
                    ? "Experimento interrumpido y registrado. Modo Difícil seleccionado."
                    : "Experimento interrumpido y registrado. Modo Tutorial seleccionado.");
            requestFocus();
        }
    }

    private void mostrarTutorialInicial() {
        tutorialInicialMostrado = true;
        menuAbierto = Menu.TUTORIAL;
        StackPane overlay = crearOverlay(
                "Recorrido inicial",
                "Conoce el flujo básico del laboratorio. Puedes avanzar a tu ritmo u omitirlo."
        );
        VBox tarjeta = (VBox) overlay.getUserData();
        tarjeta.setPrefSize(760, 420);
        tarjeta.setMaxWidth(820);
        tarjeta.setMaxHeight(460);

        HBox encabezado = (HBox) tarjeta.getChildren().getFirst();
        Button omitirEncabezado = (Button) encabezado.getChildren().getLast();
        omitirEncabezado.setText("Omitir recorrido · Esc");

        String[] pasos = {
                "1. Muévete por el laboratorio con WASD o las flechas. La indicación superior te ayuda a ubicar estaciones y objetivos.",
                "2. Pulsa M para abrir el almacén, elegir una práctica, revisar su objetivo y comprobar los reactivos antes de comenzar.",
                "3. Acércate a la estación indicada y pulsa F. Sigue la operación que aparece en la mesa y arrastra los materiales al recipiente cuando se soliciten.",
                "4. Lee las indicaciones del procedimiento. Algunos procesos requieren trasladar la muestra a otra estación antes de continuar.",
                "5. Pulsa E para consultar el cuaderno. Al terminar una práctica verás el resultado y un análisis; los productos también quedan en la mesa."
        };
        Label contador = new Label();
        contador.getStyleClass().add("game-section-title");
        Label textoPaso = new Label();
        textoPaso.getStyleClass().add("game-tutorial-step");
        textoPaso.setWrapText(true);
        textoPaso.setMaxWidth(Double.MAX_VALUE);
        VBox contenido = new VBox(18, contador, textoPaso);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setMinHeight(150);
        VBox.setVgrow(contenido, Priority.ALWAYS);

        Button anterior = new Button("Anterior");
        anterior.getStyleClass().add("game-secondary-button");
        Button siguiente = new Button();
        siguiente.getStyleClass().add("game-start-button");
        Button omitir = new Button("Omitir recorrido");
        omitir.getStyleClass().add("game-secondary-button");
        HBox acciones = new HBox(10, omitir, anterior, siguiente);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        int[] indice = {0};
        Runnable actualizarPaso = () -> {
            contador.setText("PASO " + (indice[0] + 1) + " DE " + pasos.length);
            textoPaso.setText(pasos[indice[0]]);
            anterior.setDisable(indice[0] == 0);
            siguiente.setText(indice[0] == pasos.length - 1 ? "Comenzar" : "Siguiente");
        };
        anterior.setOnAction(evento -> {
            if (indice[0] > 0) indice[0]--;
            actualizarPaso.run();
        });
        siguiente.setOnAction(evento -> {
            if (indice[0] == pasos.length - 1) {
                cerrarMenu();
                mostrarAviso("Recorrido terminado. Pulsa M para elegir tu primera práctica.");
            } else {
                indice[0]++;
                actualizarPaso.run();
            }
        });
        omitir.setOnAction(evento -> cerrarMenu());
        actualizarPaso.run();
        tarjeta.getChildren().addAll(contenido, acciones);
        getChildren().add(overlay);
        overlayMenu = overlay;
        overlay.toFront();
        siguiente.requestFocus();
    }

    private void abrirMenuAlmacen() {
        menuAbierto = Menu.ALMACEN;
        String descripcion = presentadorModo.descripcionAlmacen();
        StackPane overlay = crearOverlay("Almacén y experimentos", descripcion);
        VBox tarjeta = (VBox) overlay.getUserData();

        HBox contenido = new HBox(16);
        VBox almacen = new VBox(8);
        almacen.setPrefWidth(300);
        almacen.setMinWidth(260);
        Label tituloAlmacen = new Label("REACTIVOS EN ALMACÉN");
        tituloAlmacen.getStyleClass().add("game-section-title");
        Label pistaAlmacen = new Label("Desmarca los materiales que no están disponibles.");
        pistaAlmacen.getStyleClass().add("game-menu-subtitle");
        listaReactivos = new VBox(4);
        ScrollPane scrollReactivos = crearScroll(listaReactivos);
        almacen.getChildren().addAll(tituloAlmacen, pistaAlmacen, scrollReactivos);
        VBox.setVgrow(scrollReactivos, Priority.ALWAYS);

        VBox panelDerecho = new VBox(10);
        HBox.setHgrow(panelDerecho, Priority.ALWAYS);
        HBox pestanas = new HBox(8);
        botonTabExperimentos = new Button("EXPERIMENTOS");
        botonTabExperimentos.getStyleClass().add("game-secondary-button");
        botonTabResultados = new Button();
        botonTabResultados.getStyleClass().add("game-secondary-button");
        botonTabExperimentos.setOnAction(evento -> seleccionarPestanaAlmacen(false));
        botonTabResultados.setOnAction(evento -> seleccionarPestanaAlmacen(true));
        pestanas.getChildren().addAll(botonTabExperimentos, botonTabResultados);

        panelExperimentos = new VBox(8);
        Label tituloExperimentos = new Label("PRÁCTICAS DISPONIBLES");
        tituloExperimentos.getStyleClass().add("game-section-title");
        busquedaExperimentos = new TextField();
        busquedaExperimentos.setPromptText("Buscar experimento por nombre...");
        busquedaExperimentos.getStyleClass().add("game-experiment-search");
        busquedaExperimentos.setText(filtroExperimentos);
        busquedaExperimentos.textProperty().addListener((observable, anterior, actual) -> {
            filtroExperimentos = actual == null ? "" : actual.trim();
            actualizarListaExperimentos();
        });
        listaExperimentos = new VBox(7);
        ScrollPane scrollExperimentos = crearScroll(listaExperimentos);
        VBox.setVgrow(scrollExperimentos, Priority.ALWAYS);
        detalleExperimento = new Label("Selecciona una práctica para ver su estación y objetivo.");
        detalleExperimento.getStyleClass().add("game-menu-subtitle");
        detalleExperimento.setWrapText(true);
        botonIniciarExperimento = new Button("Iniciar experimento");
        botonIniciarExperimento.getStyleClass().add("game-start-button");
        botonIniciarExperimento.setDisable(true);
        botonIniciarExperimento.setOnAction(evento -> iniciarExperimentoSeleccionado());
        panelExperimentos.getChildren().addAll(
                tituloExperimentos, busquedaExperimentos, scrollExperimentos,
                detalleExperimento, botonIniciarExperimento);
        VBox.setVgrow(scrollExperimentos, Priority.ALWAYS);

        panelResultados = new VBox(8);
        Label tituloResultados = new Label("RESULTADOS OBTENIDOS");
        tituloResultados.getStyleClass().add("game-section-title");
        Label ayudaResultados = new Label("Recoge los productos de las mesas y deposítalos en el almacén para archivarlos.");
        ayudaResultados.getStyleClass().add("game-menu-subtitle");
        ayudaResultados.setWrapText(true);
        galeriaResultados = new FlowPane(12, 12);
        galeriaResultados.setAlignment(Pos.TOP_LEFT);
        galeriaResultados.setPrefWrapLength(640);
        galeriaResultados.setMaxWidth(Double.MAX_VALUE);
        ScrollPane scrollResultados = crearScroll(galeriaResultados);
        VBox.setVgrow(scrollResultados, Priority.ALWAYS);
        panelResultados.getChildren().addAll(tituloResultados, ayudaResultados, scrollResultados);

        StackPane contenidoPestanas = new StackPane(panelExperimentos, panelResultados);
        VBox.setVgrow(contenidoPestanas, Priority.ALWAYS);
        panelDerecho.getChildren().addAll(pestanas, contenidoPestanas);
        contenido.getChildren().addAll(almacen, panelDerecho);
        VBox.setVgrow(contenido, Priority.ALWAYS);

        tarjeta.getChildren().add(contenido);
        popularListaReactivos();
        actualizarListaExperimentos();
        actualizarGaleriaResultados();
        seleccionarPestanaAlmacen(false);
        getChildren().add(overlay);
        overlayMenu = overlay;
    }

    private void seleccionarPestanaAlmacen(boolean mostrarResultados) {
        if (panelExperimentos == null || panelResultados == null) return;
        panelExperimentos.setVisible(!mostrarResultados);
        panelExperimentos.setManaged(!mostrarResultados);
        panelResultados.setVisible(mostrarResultados);
        panelResultados.setManaged(mostrarResultados);
        botonTabExperimentos.getStyleClass().remove("game-tab-selected");
        botonTabResultados.getStyleClass().remove("game-tab-selected");
        (mostrarResultados ? botonTabResultados : botonTabExperimentos)
                .getStyleClass().add("game-tab-selected");
    }

    private ScrollPane crearScroll(Node contenido) {
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("game-menu-scroll");
        return scroll;
    }

    private void popularListaReactivos() {
        if (listaReactivos == null) return;
        listaReactivos.getChildren().clear();
        for (Sustancia sustancia : catalogo.obtenerSustancias()) {
            CheckBox check = new CheckBox(sustancia.getFormula() + " · " + sustancia.getNombre());
            check.setSelected(reactivosDisponibles.contains(sustancia.getId()));
            check.setOnAction(evento -> {
                if (check.isSelected()) reactivosDisponibles.add(sustancia.getId());
                else reactivosDisponibles.remove(sustancia.getId());
                actualizarListaExperimentos();
            });
            HBox reactivo = new HBox(8, new IconoSustancia(sustancia, 30), check);
            reactivo.setAlignment(Pos.CENTER_LEFT);
            listaReactivos.getChildren().add(reactivo);
        }
    }

    private boolean reaccionDisponible(Reaccion reaccion) {
        return reaccion.getReactivos().keySet().stream()
                .allMatch(sustancia -> reactivosDisponibles.contains(sustancia.getId()));
    }

    private void actualizarListaExperimentos() {
        if (listaExperimentos == null) return;
        listaExperimentos.getChildren().clear();
        GestorMisiones.Mision misionActual = sesion.getModoJuego().permiteMisiones()
                ? gestorMisiones.obtenerMisionActual().orElse(null)
                : null;
        List<Reaccion> disponibles = catalogo.obtenerReacciones().stream()
                .filter(reaccion -> misionActual == null
                        || reaccion.getId().equals(misionActual.reaccionId()))
                .filter(this::reaccionDisponible)
                .filter(reaccion -> {
                    String consulta = filtroExperimentos.toLowerCase(Locale.ROOT);
                    if (consulta.isBlank()) return true;
                    String nombreReaccion = reaccion.getNombre().toLowerCase(Locale.ROOT);
                    String nombreExperimento = fabricaExperimento.crearExperimentoParaReaccion(reaccion)
                            .getNombre().toLowerCase(Locale.ROOT);
                    return nombreReaccion.contains(consulta) || nombreExperimento.contains(consulta);
                })
                .sorted(Comparator.comparing(Reaccion::getNombre))
                .toList();

        if (disponibles.isEmpty()) {
            String mensaje;
            if (misionActual != null) {
                mensaje = filtroExperimentos.isBlank()
                        ? "Misión activa: " + misionActual.titulo()
                                + ". Activa los reactivos requeridos en el almacén para ver la práctica."
                        : "La ruta activa solo permite la práctica de "
                                + misionActual.titulo() + ". Limpia la búsqueda para verla.";
            } else {
                mensaje = filtroExperimentos.isBlank()
                        ? "No hay prácticas con los reactivos marcados. Ajusta el almacén para continuar."
                        : "No se encontraron experimentos con ese nombre y los reactivos disponibles.";
            }
            Label vacio = new Label(mensaje);
            vacio.getStyleClass().add("game-empty-state");
            listaExperimentos.getChildren().add(vacio);
        } else {
            for (Reaccion reaccion : disponibles) {
                Button tarjeta = crearTarjetaReaccion(reaccion);
                tarjeta.setDisable(sesion.isActiva());
                listaExperimentos.getChildren().add(tarjeta);
            }
        }
        actualizarDetalleExperimento();
    }

    private void actualizarGaleriaResultados() {
        if (galeriaResultados == null) return;
        galeriaResultados.getChildren().clear();
        if (botonTabResultados != null) {
            botonTabResultados.setText("RESULTADOS OBTENIDOS (" + resultadosAlmacenados.size() + ")");
        }
        if (resultadosAlmacenados.isEmpty()) {
            Label vacio = new Label("Todavía no hay resultados archivados. Completa una práctica, recoge su producto y déjalo en el almacén.");
            vacio.getStyleClass().add("game-empty-state");
            vacio.setWrapText(true);
            vacio.setPrefWidth(520);
            galeriaResultados.getChildren().add(vacio);
            return;
        }
        resultadosAlmacenados.stream()
                .sorted(Comparator.comparing(ProductoAlmacenado::fechaObtencion).reversed())
                .map(this::crearTarjetaResultado)
                .forEach(galeriaResultados.getChildren()::add);
    }

    private Node crearTarjetaResultado(ProductoAlmacenado resultado) {
        VBox tarjeta = new VBox(7);
        tarjeta.getStyleClass().add("game-result-card");
        tarjeta.setAlignment(Pos.TOP_CENTER);
        tarjeta.setPrefWidth(225);
        tarjeta.setMinWidth(205);
        tarjeta.setMinHeight(290);

        IconoSustancia imagen = new IconoSustancia(resultado.sustancia(), 104);
        Label nombre = new Label(resultado.sustancia().getNombre());
        nombre.getStyleClass().add("game-result-name");
        nombre.setWrapText(true);
        nombre.setMaxWidth(195);
        Label formula = new Label(resultado.sustancia().getFormula());
        formula.getStyleClass().add("game-result-formula");
        VBox caracteristicas = new VBox(3);
        caracteristicas.getStyleClass().add("game-result-details");
        caracteristicas.getChildren().addAll(
                new Label("Cantidad: " + formatearCantidad(resultado.cantidad())),
                new Label("Estado: " + resultado.sustancia().getEstadoMateria().name().toLowerCase(Locale.ROOT)),
                new Label("Tipo: " + resultado.sustancia().getTipo().name().toLowerCase(Locale.ROOT)),
                new Label(String.format(Locale.ROOT, "Masa molar: %.3f g/mol", resultado.sustancia().getMasaMolar())),
                new Label("Experimento: " + resultado.experimentoOrigen()),
                new Label("Obtenido: " + FECHA_REGISTRO.format(resultado.fechaObtencion()))
        );
        caracteristicas.getChildren().forEach(nodo -> ((Label) nodo).setWrapText(true));
        tarjeta.getChildren().addAll(imagen, nombre, formula, caracteristicas);
        return tarjeta;
    }

    private String formatearCantidad(Cantidad cantidad) {
        return String.format(Locale.ROOT, "%.3f %s", cantidad.getValor(),
                cantidad.getUnidad().name().toLowerCase(Locale.ROOT));
    }

    private Button crearTarjetaReaccion(Reaccion reaccion) {
        Button boton = new Button();
        boton.getStyleClass().add("game-reaction-card");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setAlignment(Pos.CENTER_LEFT);
        VBox detalle = new VBox(3);
        Label nombre = new Label(reaccion.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        if (!presentadorModo.mostrarEcuacionReaccion()) {
            detalle.getChildren().add(nombre);
        } else {
            Label ecuacion = new Label(formatearEcuacion(reaccion));
            ecuacion.getStyleClass().add("game-menu-subtitle");
            detalle.getChildren().addAll(nombre, ecuacion);
        }
        boton.setGraphic(detalle);
        boton.setAccessibleText(presentadorModo.mostrarEcuacionReaccion()
                ? reaccion.getNombre() + ": " + formatearEcuacion(reaccion)
                : reaccion.getNombre());
        boton.setOnAction(evento -> seleccionarExperimento(reaccion));
        return boton;
    }

    private void seleccionarExperimento(Reaccion reaccion) {
        if (sesion.isActiva()) {
            mostrarAviso("Termina el experimento actual antes de elegir otro.");
            return;
        }
        reaccionSeleccionada = reaccion;
        muestraEnProceso = "";
        trasladoPendiente = false;
        reaccionVisualId = reaccion.getId();
        ultimaEstacionDeTrabajo = null;
        operacionEnMarcha = false;
        accionEnCurso = null;
        experimentoSeleccionado = presentadorModo.crearExperimento(fabricaExperimento, reaccion);
        automataSeleccionado = FabricaAutomata.crearAutomataParaReaccion(reaccion);
        actualizarDetalleExperimento();
    }

    private void actualizarDetalleExperimento() {
        if (detalleExperimento == null || botonIniciarExperimento == null) return;
        if (reaccionSeleccionada == null || experimentoSeleccionado == null) {
            detalleExperimento.setText("Selecciona una práctica para ver su estación y objetivo.");
            botonIniciarExperimento.setDisable(true);
            return;
        }
        boolean disponible = reaccionDisponible(reaccionSeleccionada);
        EstacionLaboratorio estacion = presentadorModo.permiteAccesoLibre() ? null
                : primeraEstacionDe(reaccionSeleccionada, experimentoSeleccionado, automataSeleccionado);
        detalleExperimento.setText(presentadorModo.textoExperimento(
                reaccionSeleccionada, experimentoSeleccionado, estacion, disponible));
        botonIniciarExperimento.setDisable(sesion.isActiva() || !disponible);
    }

    private void iniciarExperimentoSeleccionado() {
        if (sesion.isActiva() || experimentoSeleccionado == null
                || reaccionSeleccionada == null || automataSeleccionado == null) {
            return;
        }
        experimentoVisible = experimentoSeleccionado;
        muestraEnProceso = "";
        trasladoPendiente = false;
        sesion.iniciar(experimentoSeleccionado, reaccionSeleccionada, automataSeleccionado);
        cerrarMenu();
        if (sesion.getModoJuego() == ModoJuego.DIFICIL) {
            botonSolicitarPista.setText("Solicitar pista · 0/2");
            botonSolicitarPista.setDisable(false);
            botonSolicitarPista.setVisible(true);
            botonSolicitarPista.setManaged(true);
        }
        EstacionLaboratorio destino = obtenerEstacionRequerida();
        mostrarAviso(presentadorModo.textoInicio(destino));
    }

    private void abrirMenuCuaderno() {
        menuAbierto = Menu.CUADERNO;
        StackPane overlay = crearOverlay("Cuaderno de laboratorio", "CHEMLAB OS · archivo digital de materiales y procesos.");
        overlay.getStyleClass().add("game-notebook-overlay");
        VBox tarjeta = (VBox) overlay.getUserData();
        tarjeta.getStyleClass().add("game-notebook-card");
        tarjeta.setPrefSize(1080, 620);

        HBox metadatos = new HBox(12);
        metadatos.setAlignment(Pos.CENTER_LEFT);
        metadatos.getStyleClass().add("game-notebook-meta");
        Label sistema = new Label("▣ CHEMLAB TABLET  /  BITÁCORA EXPERIMENTAL");
        sistema.getStyleClass().add("game-notebook-micro");
        Region espacioMeta = new Region();
        HBox.setHgrow(espacioMeta, Priority.ALWAYS);
        Label conexion = new Label("● SINCRONIZADO   ·   BATERÍA 98%");
        conexion.getStyleClass().add("game-notebook-online");
        metadatos.getChildren().addAll(sistema, espacioMeta, conexion);

        HBox tabs = new HBox(8);
        tabs.getStyleClass().add("game-notebook-tabs");
        Button botonInventario = new Button("▤  MATERIALES");
        Button botonHistorial = new Button("⌁  EXPERIMENTOS");
        Button botonQuimica = new Button("⚗  CUADERNO QUÍMICO");
        for (Button boton : List.of(botonInventario, botonHistorial, botonQuimica)) {
            boton.setMinHeight(42);
            boton.setFocusTraversable(true);
            HBox.setHgrow(boton, Priority.ALWAYS);
            boton.setMaxWidth(Double.MAX_VALUE);
        }
        botonInventario.getStyleClass().add("game-notebook-tab");
        botonHistorial.getStyleClass().add("game-notebook-tab");
        botonQuimica.getStyleClass().add("game-notebook-tab");
        VBox contenido = new VBox(10);
        contenido.getStyleClass().add("game-notebook-screen");
        contenido.setPadding(new Insets(12));
        contenido.setMinHeight(360);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        contenidoCuaderno = contenido;
        botonInventarioCuaderno = botonInventario;
        botonHistorialCuaderno = botonHistorial;
        botonQuimicaCuaderno = botonQuimica;
        botonInventario.setOnAction(evento -> {
            seccionCuaderno = SeccionCuaderno.INVENTARIO;
            mostrarSeccionCuaderno(contenido, botonInventario, botonHistorial, botonQuimica);
        });
        botonHistorial.setOnAction(evento -> {
            seccionCuaderno = SeccionCuaderno.HISTORIAL;
            mostrarSeccionCuaderno(contenido, botonInventario, botonHistorial, botonQuimica);
        });
        botonQuimica.setOnAction(evento -> {
            seccionCuaderno = SeccionCuaderno.QUIMICA;
            mostrarSeccionCuaderno(contenido, botonInventario, botonHistorial, botonQuimica);
        });
        tabs.getChildren().addAll(botonInventario, botonHistorial, botonQuimica);
        Label pie = new Label("CHEMLAB OS  ·  CUADERNO DE SÍNTESIS Y ANÁLISIS");
        pie.getStyleClass().add("game-notebook-micro");
        tarjeta.getChildren().addAll(metadatos, tabs, contenido, pie);
        mostrarSeccionCuaderno(contenido, botonInventario, botonHistorial, botonQuimica);
        getChildren().add(overlay);
        overlayMenu = overlay;
        StackPane.setAlignment(overlay, Pos.CENTER);
        overlay.toFront();
    }

    private void mostrarSeccionCuaderno(
            VBox contenido,
            Button inventario,
            Button historial,
            Button quimica
    ) {
        contenido.getChildren().clear();
        inventario.getStyleClass().setAll("game-notebook-tab");
        historial.getStyleClass().setAll("game-notebook-tab");
        quimica.getStyleClass().setAll("game-notebook-tab");
        if (seccionCuaderno == SeccionCuaderno.INVENTARIO) {
            inventario.getStyleClass().setAll("game-notebook-tab", "game-notebook-tab-selected");
            contenido.getChildren().add(construirVistaInventario());
        } else if (seccionCuaderno == SeccionCuaderno.HISTORIAL) {
            historial.getStyleClass().setAll("game-notebook-tab", "game-notebook-tab-selected");
            contenido.getChildren().add(construirVistaHistorial());
        } else {
            quimica.getStyleClass().setAll("game-notebook-tab", "game-notebook-tab-selected");
            List<Experimento> experimentos = new ArrayList<>();
            if (experimentoSeleccionado != null) experimentos.add(experimentoSeleccionado);
            if (experimentoVisible != null && experimentoVisible != experimentoSeleccionado) {
                experimentos.add(experimentoVisible);
            }
            List<Sustancia> productos = resultadosAlmacenados.stream()
                    .map(ProductoAlmacenado::sustancia)
                    .toList();
            VistaCuadernoQuimico cuadernoQuimico =
                    new VistaCuadernoQuimico(catalogo, sesion, experimentos, productos);
            VBox.setVgrow(cuadernoQuimico, Priority.ALWAYS);
            contenido.getChildren().add(cuadernoQuimico);
        }
    }

    private Node construirVistaInventario() {
        Experimento experimento = experimentoSeleccionado != null ? experimentoSeleccionado : experimentoVisible;
        if (experimento == null) {
            Label vacio = new Label("Aún no hay un experimento seleccionado. Abre M para elegir una práctica.");
            vacio.getStyleClass().add("game-empty-state");
            return vacio;
        }

        VBox vista = new VBox(8);
        Label nombre = new Label("Materiales de: " + experimento.getNombre());
        nombre.getStyleClass().add("game-section-title");
        vista.getChildren().add(nombre);
        List<Map.Entry<Sustancia, Cantidad>> cantidades = new ArrayList<>(experimento.getInventario().obtenerCantidades().entrySet());
        cantidades.sort(Comparator.comparing(
                (Map.Entry<Sustancia, Cantidad> entrada) -> entrada.getKey().getNombre()));
        if (cantidades.isEmpty()) {
            vista.getChildren().add(new Label("El inventario está vacío."));
        } else {
            for (Map.Entry<Sustancia, Cantidad> entrada : cantidades) {
                HBox fila = new HBox(12);
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.getStyleClass().add("game-notebook-row");
                fila.getChildren().add(new IconoSustancia(entrada.getKey(), 32));
                Label formula = new Label(entrada.getKey().getFormula());
                formula.setMinWidth(92);
                Label sustancia = new Label(entrada.getKey().getNombre());
                HBox.setHgrow(sustancia, Priority.ALWAYS);
                Label cantidad = new Label(String.format(Locale.ROOT, "%.2f %s",
                        entrada.getValue().getValor(), entrada.getValue().getUnidad()));
                cantidad.getStyleClass().add("game-menu-subtitle");
                fila.getChildren().addAll(formula, sustancia, cantidad);
                vista.getChildren().add(fila);
            }
        }
        return crearScroll(vista);
    }

    private Node construirVistaHistorial() {
        CuadernoLaboratorio cuaderno = sesion.getCuaderno();
        VBox vista = new VBox(10);
        Label resumen = new Label("Sustancias descubiertas: " + cuaderno.getSustanciasDescubiertas().size()
                + " · Procesos registrados: " + cuaderno.getHistorial().size());
        resumen.getStyleClass().add("game-notebook-summary");
        vista.getChildren().add(resumen);
        Label tituloDescubrimientos = new Label("DESCUBRIMIENTOS");
        tituloDescubrimientos.getStyleClass().add("game-section-title");
        vista.getChildren().add(tituloDescubrimientos);
        FlowPane descubrimientos = new FlowPane(8, 8);
        for (String idSustancia : cuaderno.getSustanciasDescubiertas()) {
            catalogo.buscarSustancia(idSustancia).ifPresent(sustancia -> {
                HBox ficha = new HBox(6, new IconoSustancia(sustancia, 26),
                        new Label(sustancia.getFormula() + " · " + sustancia.getNombre()));
                ficha.setAlignment(Pos.CENTER_LEFT);
                ficha.getStyleClass().add("game-notebook-chip");
                descubrimientos.getChildren().add(ficha);
            });
        }
        if (descubrimientos.getChildren().isEmpty()) {
            Label sinDescubrimientos = new Label("Aún no has sintetizado ninguna sustancia.");
            sinDescubrimientos.getStyleClass().add("game-menu-subtitle");
            descubrimientos.getChildren().add(sinDescubrimientos);
        }
        vista.getChildren().add(descubrimientos);
        if (cuaderno.getHistorial().isEmpty()) {
            Label vacio = new Label("Aún no hay registros. Completa o abandona una práctica para guardarla en el cuaderno.");
            vacio.getStyleClass().add("game-empty-state");
            vista.getChildren().add(vacio);
        } else {
            List<RegistroExperimento> registros = new ArrayList<>(cuaderno.getHistorial());
            registros.sort(Comparator.comparing(RegistroExperimento::getFecha).reversed());
            for (RegistroExperimento registro : registros) {
                VBox tarjeta = new VBox(4);
                tarjeta.getStyleClass().add("game-notebook-entry");
                tarjeta.setSpacing(2);
                tarjeta.setMinHeight(88);
                tarjeta.setPrefHeight(88);
                tarjeta.setMaxHeight(88);
                String estado = registro.getEstadoFinal().name();
                Label titulo = new Label((registro.isExitoso() ? "✓ " : "○ ") + registro.getNombreExperimento());
                titulo.setStyle("-fx-font-weight: bold;");
                Label fecha = new Label(registro.getFecha().format(FECHA_REGISTRO)
                        + " · " + estado + " · " + registro.getModoJuego());
                fecha.setText(fecha.getText() + " / " + registro.getPasosTransicion().size() + " pasos");
                fecha.getStyleClass().add("game-menu-subtitle");
                Label pasos = new Label(registro.getPasosTransicion().size() + " pasos registrados");
                pasos.getStyleClass().add("game-menu-subtitle");
                pasos.setManaged(false);
                pasos.setVisible(false);
                String acciones = registro.getPasosTransicion().stream()
                        .map(RegistroExperimento.PasoTransicion::accion)
                        .map(this::nombreAccionQuimica)
                        .reduce((anterior, actual) -> anterior + " → " + actual)
                        .orElse("Sin etapas registradas");
                Label detallePasos = new Label(acciones);
                detallePasos.setWrapText(false);
                detallePasos.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
                detallePasos.getStyleClass().add("game-menu-subtitle");
                tarjeta.getChildren().addAll(titulo, fecha, pasos, detallePasos);
                if (registro.getResultado() != null && !registro.getResultado().getProductos().isEmpty()) {
                    String productos = registro.getResultado().getProductos().entrySet().stream()
                            .map(producto -> producto.getKey().getFormula() + " · "
                                    + String.format(Locale.ROOT, "%.2f %s",
                                    producto.getValue().getValor(), producto.getValue().getUnidad()))
                            .reduce((anterior, actual) -> anterior + " | " + actual)
                            .orElse("");
                    Label resultado = new Label("Productos: " + productos);
                    resultado.setWrapText(false);
                    resultado.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
                    resultado.getStyleClass().add("game-section-title");
                    tarjeta.getChildren().add(resultado);
                }
                Button abrirRegistro = new Button();
                abrirRegistro.setMaxWidth(Double.MAX_VALUE);
                abrirRegistro.setMinHeight(108);
                abrirRegistro.setPrefHeight(108);
                abrirRegistro.setMaxHeight(108);
                abrirRegistro.setAlignment(Pos.CENTER_LEFT);
                abrirRegistro.setGraphic(tarjeta);
                abrirRegistro.setAccessibleText("Ver etapas del experimento " + registro.getNombreExperimento());
                abrirRegistro.getStyleClass().add("game-notebook-entry-button");
                abrirRegistro.setOnAction(evento -> mostrarDetalleExperimentoEnCuaderno(registro));
                vista.getChildren().add(abrirRegistro);
            }
        }
        return crearScroll(vista);
    }

    private void mostrarDetalleExperimentoEnCuaderno(RegistroExperimento registro) {
        if (contenidoCuaderno == null) return;
        Button volver = new Button("‹  VOLVER A EXPERIMENTOS");
        volver.getStyleClass().add("game-notebook-back-button");
        volver.setOnAction(evento -> {
            seccionCuaderno = SeccionCuaderno.HISTORIAL;
            mostrarSeccionCuaderno(contenidoCuaderno, botonInventarioCuaderno,
                    botonHistorialCuaderno, botonQuimicaCuaderno);
        });

        Label titulo = new Label(registro.getNombreExperimento());
        titulo.getStyleClass().add("game-notebook-summary");
        Label descripcion = new Label(registro.getEstadoFinal().name()
                + "  ·  " + registro.getModoJuego()
                + "  ·  " + registro.getPasosTransicion().size() + " acciones evaluadas");
        descripcion.getStyleClass().add("game-notebook-micro");

        AutomataQuimico automata = registro.getIdProceso() == null
                ? FabricaAutomata.crearAutomataEstandar()
                : catalogo.buscarReaccion(registro.getIdProceso())
                        .map(FabricaAutomata::crearAutomataParaReaccion)
                        .orElseThrow(() -> new IllegalStateException(
                                "La reacción del registro ya no existe en el catálogo: "
                                        + registro.getIdProceso()));
        LienzoAutomataRegistro lienzoRuta = new LienzoAutomataRegistro(registro, automata);
        ScrollPane desplazamientoRuta = new ScrollPane(lienzoRuta);
        desplazamientoRuta.setFitToWidth(false);
        desplazamientoRuta.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        desplazamientoRuta.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        desplazamientoRuta.setPrefViewportHeight(390);
        desplazamientoRuta.getStyleClass().add("game-automaton-scroll");

        VBox etapas = new VBox(8);
        etapas.getChildren().add(new Label("RECORRIDO DEL EXPERIMENTO"));
        if (registro.getPasosTransicion().isEmpty()) {
            etapas.getChildren().add(new Label("No se alcanzó a completar ninguna etapa."));
        } else {
            for (int indice = 0; indice < registro.getPasosTransicion().size(); indice++) {
                RegistroExperimento.PasoTransicion paso = registro.getPasosTransicion().get(indice);
                Label etapa = new Label((paso.valida() ? "✓  " : "○  ")
                        + (indice + 1) + ". " + nombreEstadoQuimico(paso.estadoDestino())
                        + " · " + nombreAccionQuimica(paso.accion()));
                etapa.setWrapText(true);
                etapa.getStyleClass().add("game-chemistry-stage-title");
                etapas.getChildren().add(etapa);
            }
        }
        VBox detalleEtapas = new VBox(10, etapas);
        if (registro.getResultado() != null && !registro.getResultado().getProductos().isEmpty()) {
            detalleEtapas.getChildren().add(new Label("Productos obtenidos: "
                    + registro.getResultado().getProductos().keySet().stream()
                    .map(producto -> producto.getFormula() + " · " + producto.getNombre())
                    .reduce((a, b) -> a + "  |  " + b).orElse("")));
        }
        if (!registro.getMotivoFinalizacion().isBlank()) {
            Label motivo = new Label("Causa de finalización: " + registro.getMotivoFinalizacion());
            motivo.setWrapText(true);
            detalleEtapas.getChildren().add(motivo);
        }
        if (!registro.getEventos().isEmpty()) {
            detalleEtapas.getChildren().add(new Label("BITÁCORA DE LA SESIÓN"));
            registro.getEventos().forEach(evento -> {
                Label linea = new Label("• " + evento);
                linea.setWrapText(true);
                detalleEtapas.getChildren().add(linea);
            });
        }
        if (!registro.getPistasSolicitadas().isEmpty()) {
            detalleEtapas.getChildren().add(new Label("PISTAS SOLICITADAS"));
            registro.getPistasSolicitadas().forEach(pista -> {
                Label linea = new Label("• " + pista);
                linea.setWrapText(true);
                detalleEtapas.getChildren().add(linea);
            });
        }
        if (!registro.getInventarioFinal().isEmpty()) {
            detalleEtapas.getChildren().add(new Label("INVENTARIO AL CIERRE"));
            registro.getInventarioFinal().forEach((sustancia, cantidad) -> {
                Label linea = new Label(sustancia.getFormula() + " · " + sustancia.getNombre()
                        + " · " + String.format(Locale.ROOT, "%.3f %s",
                        cantidad.getValor(), cantidad.getUnidad()));
                linea.setWrapText(true);
                detalleEtapas.getChildren().add(linea);
            });
        }

        Button botonRuta = new Button("RUTA");
        Button botonEtapas = new Button("ETAPAS");
        botonRuta.getStyleClass().add("game-notebook-tab");
        botonEtapas.getStyleClass().add("game-notebook-tab");
        HBox pestañas = new HBox(8, botonRuta, botonEtapas);
        ScrollPane desplazamientoEtapas = crearScroll(detalleEtapas);
        StackPane paneles = new StackPane(desplazamientoRuta, desplazamientoEtapas);
        VBox.setVgrow(paneles, Priority.ALWAYS);
        Runnable mostrarRuta = () -> {
            desplazamientoRuta.setVisible(true);
            desplazamientoRuta.setManaged(true);
            desplazamientoEtapas.setVisible(false);
            desplazamientoEtapas.setManaged(false);
            botonRuta.getStyleClass().setAll("game-notebook-tab", "game-notebook-tab-selected");
            botonEtapas.getStyleClass().setAll("game-notebook-tab");
        };
        Runnable mostrarEtapas = () -> {
            desplazamientoRuta.setVisible(false);
            desplazamientoRuta.setManaged(false);
            desplazamientoEtapas.setVisible(true);
            desplazamientoEtapas.setManaged(true);
            botonRuta.getStyleClass().setAll("game-notebook-tab");
            botonEtapas.getStyleClass().setAll("game-notebook-tab", "game-notebook-tab-selected");
        };
        botonRuta.setOnAction(evento -> mostrarRuta.run());
        botonEtapas.setOnAction(evento -> mostrarEtapas.run());
        VBox encabezado = new VBox(5, volver, titulo, descripcion, pestañas);
        VBox detalle = new VBox(10, encabezado, paneles);
        detalle.setMinHeight(0);
        mostrarRuta.run();
        detalle.getStyleClass().add("game-chemistry-record-detail");
        contenidoCuaderno.getChildren().setAll(detalle);
    }

    private String nombreAccionQuimica(String accion) {
        try {
            return switch (AccionLaboratorio.valueOf(accion)) {
                case CARGAR_REACTIVO -> "preparar y cargar reactivos";
                case MEZCLAR -> "mezclar reactivos";
                case INICIAR_PLASMA -> "activar plasma";
                case CALENTAR -> "aplicar calor";
                case COMPRIMIR -> "comprimir el sistema";
                case AGREGAR_CATALIZADOR -> "añadir catalizador";
                case AGREGAR_AGUA -> "añadir agua";
                case ACTIVAR_IGNICION -> "activar ignición";
                case FINALIZAR -> "recuperar el producto";
                default -> accion.replace('_', ' ').toLowerCase(Locale.ROOT);
            };
        } catch (IllegalArgumentException | NullPointerException error) {
            return "acción de laboratorio";
        }
    }

    private String nombreEstadoQuimico(String estado) {
        if (estado == null || estado.isBlank()) return "etapa del proceso";
        return switch (estado) {
            case "PREPARACION", "Preparación" -> "Reactivos en preparación";
            case "PREPARADO" -> "Reactivos preparados";
            case "CARGADO" -> "Equipo cargado";
            case "MEZCLADO" -> "Reactivos mezclados";
            case "PLASMA", "EN_PLASMA" -> "Plasma activo";
            case "DEPOSICION" -> "Deposición en curso";
            case "REACCIONANDO" -> "Reacción en curso";
            case "COMPLETADO" -> "Producto sintetizado";
            default -> estado.replace('_', ' ');
        };
    }

    private void dibujarMundo() {
        renderizador.renderizar(
                estacionCercana,
                reaccionSeleccionada == null ? reaccionVisualId : reaccionSeleccionada.getId(),
                estacionActiva,
                accionEnCurso,
                progresoInteraccion,
                productosEnMesa,
                productoEnMano == null ? null : productoEnMano.sustancia(),
                resaltarAlmacen
        );
        if (vistaPuesto != null) vistaPuesto.redibujar();
    }

    private void alMoverPunteroMundo(MouseEvent evento) {
        actualizarCursorMundo(evento);
    }

    private void alPresionarPunteroMundo(MouseEvent evento) {
        if (!evento.isPrimaryButtonDown() || !puedeInteractuarConMundo()) return;
        actualizarCursorMundo(evento);
    }

    private void alSoltarPunteroMundo(MouseEvent evento) {
        actualizarCursorMundo(evento);
    }

    private boolean puedeInteractuarConMundo() {
        return menuAbierto == null && (vistaPuesto == null || !vistaPuesto.isVisible()) && !operacionEnMarcha;
    }

    private void actualizarCursorMundo(MouseEvent evento) {
        if (!puedeInteractuarConMundo()) {
            resaltarAlmacen = false;
            renderizador.setCursor(Cursor.DEFAULT);
            return;
        }
        boolean sobreAlmacen = renderizador.punteroSobreAlmacen(evento.getX(), evento.getY());
        resaltarAlmacen = productoEnMano != null && sobreAlmacen && renderizador.jugadorJuntoAlAlmacen();
        renderizador.setCursor(Cursor.DEFAULT);
    }

    private void alHacerClicEnMundo(MouseEvent evento) {
        if (evento.getButton() != javafx.scene.input.MouseButton.PRIMARY
                || !puedeInteractuarConMundo()) return;
        boolean sobreAlmacen = renderizador.punteroSobreAlmacen(evento.getX(), evento.getY());
        if (productoEnMano != null) {
            if (!sobreAlmacen) {
                mostrarAviso("Lleva el producto hasta el almacén y haz clic sobre el mostrador para guardarlo.");
                return;
            }
            if (!renderizador.jugadorJuntoAlAlmacen()) {
                mostrarAviso("Acércate al almacén para dejar el producto.");
                return;
            }
            resultadosAlmacenados.add(productoEnMano);
            String nombre = productoEnMano.sustancia().getNombre();
            productoEnMano = null;
            actualizarGaleriaResultados();
            mostrarAviso(nombre + " guardado en Resultados obtenidos. Pulsa M para consultarlo.");
            actualizarCursorMundo(evento);
            return;
        }

        int indiceProducto = renderizador.productoBajoPuntero(
                evento.getX(), evento.getY(), productosEnMesa);
        if (indiceProducto < 0) return;
        ProductoAlmacenado producto = productosEnMesa.get(indiceProducto);
        if (producto.estacionOrigen().distanciaHasta(jugador.getX(), jugador.getY()) > 82) {
            mostrarAviso("Acércate a " + producto.estacionOrigen().nombre() + " para recoger el producto.");
            return;
        }
        productoEnMano = productosEnMesa.remove(indiceProducto);
        mostrarAviso("Recogiste " + productoEnMano.sustancia().getNombre()
                + ". Llévalo al almacén para archivarlo.");
        actualizarCursorMundo(evento);
    }

    private String formatearEcuacion(Reaccion reaccion) {
        StringBuilder ecuacion = new StringBuilder();
        boolean primero = true;
        for (Map.Entry<Sustancia, Integer> entrada : reaccion.getReactivos().entrySet()) {
            if (!primero) ecuacion.append(" + ");
            agregarTermino(ecuacion, entrada.getKey(), entrada.getValue());
            primero = false;
        }
        ecuacion.append(" → ");
        primero = true;
        for (Map.Entry<Sustancia, Integer> entrada : reaccion.getProductos().entrySet()) {
            if (!primero) ecuacion.append(" + ");
            agregarTermino(ecuacion, entrada.getKey(), entrada.getValue());
            primero = false;
        }
        return ecuacion.toString();
    }

    private void agregarTermino(StringBuilder texto, Sustancia sustancia, int coeficiente) {
        if (coeficiente > 1) texto.append(coeficiente);
        texto.append(sustancia.getFormula());
    }

    private String formatearAccion(AccionLaboratorio accion) {
        return switch (accion) {
            case MONTAR_EQUIPO -> "Montar equipo";
            case CARGAR_REACTIVO -> "Cargar reactivos";
            case AGREGAR_CATALIZADOR -> "Añadir catalizador";
            case CALENTAR -> "Calentar";
            case ENFRIAR -> "Enfriar";
            case COMPRIMIR -> "Comprimir";
            case DESCOMPRIMIR -> "Descomprimir";
            case MEZCLAR -> "Mezclar";
            case ACTIVAR_IGNICION -> "Activar ignición";
            case ESPERAR -> "Esperar";
            case FINALIZAR -> "Finalizar proceso";
            case AGREGAR_AGUA -> "Añadir agua";
            case INICIAR_PLASMA -> "Iniciar plasma";
            case SELECCIONAR_METODO -> "Seleccionar método";
        };
    }

    @Override
    public void onExperimentoIniciado(Experimento experimento) {
        Platform.runLater(() -> {
            retroalimentacionActual = null;
            experimentoVisible = experimento;
            labelEstado.setText("EN PROGRESO");
            actualizarObjetivoHud(experimento.getObjetivo().getDescripcion());
            mostrarAviso(sesion.getModoJuego() == ModoJuego.DIFICIL
                    ? "Experimento iniciado. Elige un puesto para explorar."
                    : "Experimento iniciado. Interactúa con el puesto asignado.");
        });
    }

    @Override
    public void onAccionEjecutada(ResultadoAccion resultado) {
        Platform.runLater(() -> {
            labelEstado.setText("Estado: " + resultado.getEstadoActual().getNombre());
            ProtocoloProcedimiento.InfoPasoProcedimiento protocoloSiguiente = null;
            if (sesion.isActiva() && sesion.getModoJuego() == ModoJuego.TUTORIAL
                    && reaccionSeleccionada != null) {
                protocoloSiguiente = presentadorModo.obtenerProtocolo(
                        reaccionSeleccionada,
                        sesion.getAutomataActual().getEstadoActual().getId()
                );
            }
            retroalimentacionActual = GeneradorRetroalimentacionPractica.generar(
                    resultado.isExitoso(),
                    sesion.isActiva(),
                    sesion.getModoJuego() == ModoJuego.DIFICIL,
                    resultado.getMensaje(),
                    resultado.getEstadoActual().getNombre(),
                    protocoloSiguiente
            );
            mostrarAviso(retroalimentacionActual.detalle(), DURACION_FEEDBACK_NANOS);
            if (sesion.isActiva() && estacionActiva != null) {
                if (sesion.getModoJuego() == ModoJuego.DIFICIL) {
                    mostrarAccionesDisponibles();
                    mostrarRetroalimentacionActual();
                    return;
                }
                EstacionLaboratorio siguiente = obtenerEstacionRequerida();
                if (siguiente != estacionActiva) {
                    String nombreSiguiente = siguiente == null ? "el puesto indicado" : siguiente.nombre();
                    muestraEnProceso = describirMuestraTransferible(resultado);
                    trasladoPendiente = siguiente != null;
                    cerrarVistaPuesto(true);
                    estacionActiva = null;
                    labelEstado.setText("CAMBIA DE PUESTO · " + nombreSiguiente);
                    mostrarAviso("Traslada el procedimiento a " + nombreSiguiente
                            + " y pulsa F.\n" + retroalimentacionActual.detalle(), DURACION_FEEDBACK_NANOS);
                } else {
                    mostrarAccionesDisponibles();
                    mostrarRetroalimentacionActual();
                }
            }
        });
    }

    private void mostrarRetroalimentacionActual() {
        if (vistaPuesto != null && retroalimentacionActual != null) {
            vistaPuesto.mostrarRetroalimentacion(
                    retroalimentacionActual.titulo(),
                    retroalimentacionActual.detalle(),
                    retroalimentacionActual.positiva()
            );
        }
    }

    private String describirMuestraTransferible(ResultadoAccion resultado) {
        String componentes = reaccionSeleccionada == null ? "muestra del procedimiento"
                : reaccionSeleccionada.getReactivos().keySet().stream()
                .map(Sustancia::getFormula).sorted()
                .reduce((anterior, actual) -> anterior + " + " + actual)
                .orElse("muestra del procedimiento");
        String estado = resultado == null || resultado.getEstadoActual() == null
                ? "mezcla en proceso" : resultado.getEstadoActual().getNombre();
        return estado + "  ·  " + componentes;
    }

    @Override
    public void onExperimentoCompletado(Experimento experimento) {
        Reaccion reaccionCompletada = reaccionSeleccionada;
        EstacionLaboratorio estacionResultado = ultimaEstacionDeTrabajo != null
                ? ultimaEstacionDeTrabajo : estacionActiva;
        if (estacionResultado == null && reaccionSeleccionada != null) {
            estacionResultado = mapa.buscarEstacionParaReaccion(reaccionSeleccionada.getId());
        }
        final EstacionLaboratorio estacionProducto = estacionResultado;
        Platform.runLater(() -> {
            experimentoVisible = experimento;
            registrarResultadoEnMesa(experimento, estacionProducto);
            muestraEnProceso = "";
            trasladoPendiente = false;
            labelEstado.setText("COMPLETADO");
            boolean misionAvanzada = reaccionCompletada != null
                    && sesion.getModoJuego().permiteMisiones()
                    && gestorMisiones.registrarPracticaCompletada(reaccionCompletada.getId());
            guardarProgreso();
            actualizarObjetivoHud("Práctica registrada en tu cuaderno");
            cerrarVistaPuesto(true);
            estacionActiva = null;
            teclasPresionadas.clear();
            operacionEnMarcha = false;
            accionEnCurso = null;
            if (misionAvanzada) {
                mostrarAviso(textoAvanceMision(), DURACION_FEEDBACK_NANOS);
            } else {
                mostrarAviso("¡Experimento completado! El producto quedó en la mesa; recógelo con un clic.");
            }
            mostrarAnalisisFinal(experimento, "", true, reaccionCompletada);
            experimentoSeleccionado = null;
            reaccionSeleccionada = null;
            automataSeleccionado = null;
            if (menuAbierto != Menu.ANALISIS) requestFocus();
        });
    }

    private void registrarResultadoEnMesa(Experimento experimento, EstacionLaboratorio estacion) {
        if (estacion == null) {
            mostrarAviso("Experimento completado, pero no se encontró una mesa para mostrar el resultado.");
            return;
        }
        Map<Sustancia, Cantidad> productos = experimento.getResultado() == null
                ? Map.of() : experimento.getResultado().getProductos();
        if (productos.isEmpty()) {
            productosEnMesa.add(new ProductoAlmacenado(
                    experimento.getNombre(), experimento.getObjetivo().getSustanciaEsperada(),
                    new Cantidad(experimento.getObjetivo().getCantidadMinima(), UnidadCantidad.GRAMOS),
                    LocalDateTime.now(), estacion));
            return;
        }
        LocalDateTime ahora = LocalDateTime.now();
        productos.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Sustancia::getId)))
                .map(entrada -> new ProductoAlmacenado(experimento.getNombre(),
                        entrada.getKey(), entrada.getValue(), ahora, estacion))
                .forEach(productosEnMesa::add);
    }

    @Override
    public void onExperimentoAbandonado(Experimento experimento) {
        Platform.runLater(() -> {
            if (cambioPerfilEnCurso) return;
            guardarProgreso();
            experimentoVisible = experimento;
            if (sesion.getModoJuego() == ModoJuego.DIFICIL) {
                mostrarResumenFinalDificil(experimento);
            } else {
                labelEstado.setText("EXPERIMENTO ABANDONADO");
                actualizarObjetivoHud("El intento quedó registrado en el cuaderno");
            }
            cerrarVistaPuesto(true);
            estacionActiva = null;
            teclasPresionadas.clear();
            operacionEnMarcha = false;
            accionEnCurso = null;
            muestraEnProceso = "";
            trasladoPendiente = false;
            reaccionVisualId = null;
            mostrarAviso("Práctica abandonada. El registro está en el cuaderno.");
            experimentoSeleccionado = null;
            reaccionSeleccionada = null;
            automataSeleccionado = null;
            requestFocus();
        });
    }

    @Override
    public void onExperimentoFallido(Experimento experimento, String motivo) {
        Platform.runLater(() -> {
            guardarProgreso();
            experimentoVisible = experimento;
            if (sesion.getModoJuego() == ModoJuego.DIFICIL) {
                mostrarResumenFinalDificil(experimento);
            } else {
                labelEstado.setText("EXPERIMENTO FALLIDO");
                actualizarObjetivoHud("El procedimiento llegó al final, pero la reacción no se produjo.");
            }
            cerrarVistaPuesto(true);
            estacionActiva = null;
            teclasPresionadas.clear();
            operacionEnMarcha = false;
            accionEnCurso = null;
            muestraEnProceso = "";
            trasladoPendiente = false;
            reaccionVisualId = null;
            botonSolicitarPista.setVisible(false);
            botonSolicitarPista.setManaged(false);
            mostrarAviso(motivo);
            mostrarAnalisisFinal(experimento, motivo, false, reaccionSeleccionada);
            experimentoSeleccionado = null;
            reaccionSeleccionada = null;
            automataSeleccionado = null;
            if (menuAbierto != Menu.ANALISIS) requestFocus();
        });
    }

    private void mostrarResumenFinalDificil(Experimento experimento) {
        RegistroExperimento registro = sesion.getCuaderno().getHistorial().stream()
                .filter(item -> item.getNombreExperimento().equals(experimento.getNombre()))
                .max(Comparator.comparing(RegistroExperimento::getFecha))
                .orElse(null);
        if (registro == null) {
            labelEstado.setText("EXPERIMENTO INTERRUMPIDO");
            actualizarObjetivoHud("El intento quedó registrado en el cuaderno");
            return;
        }

        labelEstado.setText("EXPERIMENTO " + registro.getEstadoFinal().name());
        Map<Sustancia, Cantidad> productos = registro.getResultado() == null
                ? Map.of() : registro.getResultado().getProductos();
        if (productos.isEmpty()) {
            actualizarObjetivoHud("No se formaron productos. El inventario final está en el cuaderno.");
            return;
        }
        String resumenProductos = productos.entrySet().stream()
                .map(entrada -> entrada.getKey().getFormula() + " · "
                        + String.format(Locale.ROOT, "%.3f %s",
                        entrada.getValue().getValor(), entrada.getValue().getUnidad()))
                .reduce((anterior, actual) -> anterior + "  |  " + actual)
                .orElse("");
        actualizarObjetivoHud("Productos obtenidos: " + resumenProductos);
    }

    private void mostrarAnalisisFinal(
            Experimento experimento,
            String motivo,
            boolean completado,
            Reaccion reaccion
    ) {
        RegistroExperimento registro = sesion.getCuaderno().getHistorial().stream()
                .filter(item -> item.getNombreExperimento().equals(experimento.getNombre()))
                .filter(item -> reaccion == null || reaccion.getId().equals(item.getIdProceso()))
                .max(Comparator.comparing(RegistroExperimento::getFecha))
                .orElse(null);

        if (menuAbierto != null) cerrarMenu();
        menuAbierto = Menu.ANALISIS;
        StackPane overlay = crearOverlay(
                completado ? "Análisis de práctica · completada" : "Análisis de práctica · intento no completado",
                "Revisa el objetivo, los productos y la secuencia registrada."
        );
        VBox tarjeta = (VBox) overlay.getUserData();
        tarjeta.setPrefSize(900, 570);
        tarjeta.setMaxWidth(1000);
        tarjeta.setMaxHeight(620);

        VBox contenido = new VBox(10);
        contenido.setPadding(new Insets(4, 2, 4, 2));
        agregarBloqueResumen(contenido, "OBJETIVO",
                experimento.getObjetivo().getDescripcion());
        if (reaccion != null) {
            agregarBloqueResumen(contenido, "REACCIÓN",
                    reaccion.getNombre() + "\n" + formatearEcuacion(reaccion));
        }

        if (registro == null) {
            agregarBloqueResumen(contenido, "REGISTRO",
                    "No se encontró el registro de esta práctica en el cuaderno.");
        } else {
            agregarBloqueResumen(contenido, "PROGRESO DE MISIÓN", textoProgresoMision());
            Map<Sustancia, Cantidad> productos = registro.getResultado() == null
                    ? Map.of() : registro.getResultado().getProductos();
            String productosTexto = productos.isEmpty()
                    ? "No se registraron productos."
                    : productos.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(Sustancia::getFormula)))
                    .map(entrada -> entrada.getKey().getFormula() + " · "
                            + String.format(Locale.ROOT, "%.3f %s",
                            entrada.getValue().getValor(), entrada.getValue().getUnidad()))
                    .reduce((anterior, actual) -> anterior + "\n" + actual)
                    .orElse("No se registraron productos.");
            agregarBloqueResumen(contenido, "RESULTADO OBSERVADO", productosTexto);

            String detalleAprendizaje = completado
                    ? "Compara los materiales iniciales con los productos obtenidos. La ecuación muestra la transformación y las proporciones representadas por sus coeficientes."
                    : construirDiagnosticoFallo(registro, motivo);
            agregarBloqueResumen(contenido, completado ? "QUÉ APRENDER" : "QUÉ REVISAR",
                    detalleAprendizaje);

            String pasos = registro.getPasosTransicion().isEmpty()
                    ? "No hay pasos de operación registrados."
                    : registro.getPasosTransicion().stream()
                    .map(paso -> (paso.valida() ? "✓ " : "× ")
                            + paso.accion().replace('_', ' ').toLowerCase(Locale.ROOT)
                            + " · " + paso.estadoOrigen() + " → " + paso.estadoDestino())
                    .reduce((anterior, actual) -> anterior + "\n" + actual)
                    .orElse("No hay pasos de operación registrados.");
            agregarBloqueResumen(contenido, "SECUENCIA REGISTRADA", pasos);
        }

        ScrollPane scroll = crearScroll(contenido);
        scroll.setPrefViewportHeight(330);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button botonCuaderno = new Button("Ver cuaderno");
        botonCuaderno.getStyleClass().add("game-secondary-button");
        botonCuaderno.setOnAction(evento -> {
            cerrarMenu();
            alternarMenu(Menu.CUADERNO);
        });
        Button botonVolver = new Button("Volver al laboratorio");
        botonVolver.getStyleClass().add("game-start-button");
        botonVolver.setOnAction(evento -> cerrarMenu());
        HBox acciones = new HBox(10, botonCuaderno, botonVolver);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        tarjeta.getChildren().addAll(scroll, acciones);
        getChildren().add(overlay);
        overlayMenu = overlay;
        overlay.toFront();
        botonVolver.requestFocus();
    }

    private String textoAvanceMision() {
        if (gestorMisiones.estaCompletada()) {
            return "¡Ruta de aprendizaje completada! Has superado todas las misiones.";
        }
        GestorMisiones.Mision siguiente = gestorMisiones.obtenerMisionActual().orElseThrow();
        return "¡Misión completada! Nueva práctica desbloqueada: " + siguiente.titulo() + ".";
    }

    private String textoProgresoMision() {
        if (!gestorMisiones.estaIniciada()) {
            return "La ruta opcional no está iniciada. Puedes activarla desde Misiones · G.";
        }
        if (gestorMisiones.estaCompletada()) {
            return "Ruta de aprendizaje completada: " + gestorMisiones.obtenerCantidadCompletadas()
                    + "/" + gestorMisiones.obtenerMisiones().size() + " misiones.";
        }
        GestorMisiones.Mision actual = gestorMisiones.obtenerMisionActual().orElseThrow();
        return "Progreso: " + gestorMisiones.obtenerCantidadCompletadas() + "/"
                + gestorMisiones.obtenerMisiones().size() + " · Siguiente: "
                + actual.titulo() + ". " + actual.objetivo();
    }

    private String construirDiagnosticoFallo(RegistroExperimento registro, String motivo) {
        String causa = motivo == null || motivo.isBlank()
                ? registro.getMotivoFinalizacion() : motivo;
        StringBuilder diagnostico = new StringBuilder(causa == null || causa.isBlank()
                ? "No se alcanzó el objetivo de la práctica." : causa);
        registro.getPasosTransicion().stream()
                .filter(paso -> !paso.valida())
                .findFirst()
                .ifPresentOrElse(
                        paso -> diagnostico.append("\nEmpieza revisando la operación ")
                                .append(paso.accion().replace('_', ' ').toLowerCase(Locale.ROOT))
                                .append(" en el estado ")
                                .append(paso.estadoOrigen()).append('.'),
                        () -> diagnostico.append("\nNo hay operaciones marcadas como inválidas; revisa los reactivos y las condiciones registradas.")
                );
        return diagnostico.toString();
    }

    private void agregarBloqueResumen(VBox contenido, String titulo, String detalle) {
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("game-section-title");
        Label texto = new Label(detalle);
        texto.getStyleClass().add("game-debrief-text");
        texto.setWrapText(true);
        texto.setMaxWidth(Double.MAX_VALUE);
        VBox bloque = new VBox(4, encabezado, texto);
        bloque.getStyleClass().add("game-debrief-section");
        contenido.getChildren().add(bloque);
    }
}
