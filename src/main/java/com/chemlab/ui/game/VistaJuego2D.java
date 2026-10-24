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
import com.chemlab.game.procedure.PlanificadorEstacionesProcedimiento;
import com.chemlab.game.procedure.ProtocoloProcedimiento;
import com.chemlab.game.SesionJuego;
import com.chemlab.model.entity.CuadernoLaboratorio;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.RegistroExperimento;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import com.chemlab.ui.components.CursorGuante;
import com.chemlab.ui.components.IconoSustancia;
import com.chemlab.ui.components.LienzoAutomataRegistro;
import com.chemlab.ui.workbench.PanelInteraccionProcedimiento;
import com.chemlab.ui.workbench.VistaPuestoLaboratorio;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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

    private enum Menu { ALMACEN, CUADERNO }
    private enum SeccionCuaderno { INVENTARIO, HISTORIAL }

    private static final DateTimeFormatter FECHA_REGISTRO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Set<KeyCode> TECLAS_MOVIMIENTO = Set.of(
            KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D,
            KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT
    );

    private final CatalogoQuimico catalogo;
    private final FabricaExperimento fabricaExperimento;
    private final SesionJuego sesion;
    private final MapaLaboratorio mapa;
    private final PlanificadorEstacionesProcedimiento planificadorEstaciones =
            new PlanificadorEstacionesProcedimiento();
    private final JugadorLaboratorio jugador = new JugadorLaboratorio();
    private final RenderizadorLaboratorio2D renderizador;
    private final Set<KeyCode> teclasPresionadas = EnumSet.noneOf(KeyCode.class);
    private final Set<String> reactivosDisponibles = new HashSet<>();
    private final AnimationTimer reloj;

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
    private TextField busquedaExperimentos;
    private String filtroExperimentos = "";
    private VBox contenidoCuaderno;
    private Button botonInventarioCuaderno;
    private Button botonHistorialCuaderno;
    private Label detalleExperimento;
    private Button botonIniciarExperimento;
    private StackPane cabecera;
    private HBox barraControles;
    private long ultimoPulso;
    private long avisoHasta;
    private String avisoActual = "";

    public VistaJuego2D() {
        catalogo = CatalogoQuimico.predeterminado();
        fabricaExperimento = new FabricaExperimento(catalogo);
        sesion = FabricaSesion.crearSesion(catalogo, FabricaAutomata.crearAutomataEstandar());
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
        labelEstado.getStyleClass().add("game-status");
        VBox pieHud = new VBox(8, cabecera, barraControles);
        pieHud.setAlignment(Pos.CENTER);
        capaHud.setBottom(pieHud);
        capaHud.setMouseTransparent(true);
        labelAviso.getStyleClass().add("game-toast");
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

        reloj = new AnimationTimer() {
            @Override
            public void handle(long ahora) {
                double delta = ultimoPulso == 0 ? 0 : Math.min((ahora - ultimoPulso) / 1_000_000_000.0, 0.05);
                ultimoPulso = ahora;
                actualizarMovimiento(delta);
                actualizarOperacion(delta);
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

        Label marca = new Label("⚗ CHEMLAB");
        marca.getStyleClass().add("game-title");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        labelObjetivo.getStyleClass().add("game-objective");
        contenido.getChildren().addAll(marca, espacio, labelObjetivo);

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
        barra.getChildren().addAll(
                new Label("WASD / flechas · mover"),
                new Label("F · usar puesto"),
                new Label("E · inventario y cuaderno"),
                new Label("M · almacén y experimentos"),
                labelPuestoCercano
        );
        return barra;
    }

    private void alPresionarTecla(KeyEvent evento) {
        KeyCode tecla = evento.getCode();
        boolean primeraPulsacion = teclasPresionadas.add(tecla);
        if (!primeraPulsacion) {
            evento.consume();
            return;
        }

        switch (tecla) {
            case E -> alternarMenu(Menu.CUADERNO);
            case M -> alternarMenu(Menu.ALMACEN);
            case F -> {
                if (menuAbierto == null) interactuarConPuesto();
            }
            case ESCAPE -> cerrarMenuOAcciones();
            default -> { }
        }
        if (tecla == KeyCode.E || tecla == KeyCode.M || tecla == KeyCode.F || tecla == KeyCode.ESCAPE) {
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
        avisoActual = mensaje;
        labelAviso.setText(avisoActual);
        labelAviso.setManaged(true);
        labelAviso.setVisible(true);
        avisoHasta = System.nanoTime() + 3_500_000_000L;
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
        if (requerida == null || estacionCercana != requerida) {
            mostrarAviso("El paso actual debe realizarse en: "
                    + (requerida == null ? "otro puesto" : requerida.nombre()));
            return;
        }

        teclasPresionadas.removeAll(TECLAS_MOVIMIENTO);
        estacionActiva = estacionCercana;
        ultimaEstacionDeTrabajo = estacionActiva;
        labelEstado.setText("PUESTO ACTIVO · " + estacionActiva.nombre());
        mostrarAccionesDisponibles();
    }

    private EstacionLaboratorio obtenerEstacionRequerida() {
        if (!sesion.isActiva() || reaccionSeleccionada == null || experimentoSeleccionado == null) {
            return null;
        }
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
        protocoloActivo =
                ProtocoloProcedimiento.obtenerProtocoloPaso(reaccionSeleccionada.getId(), estadoId);
        List<TransicionQuimica> transiciones = sesion.getAutomataActual().getTransicionesDisponibles();
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
                }
        );
        getChildren().add(2, vistaPuesto);
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
        requestFocus();
    }

    private void construirControlInteraccion(TransicionQuimica transicion) {
        panelInteraccion = new PanelInteraccionProcedimiento(
                reaccionSeleccionada,
                experimentoSeleccionado,
                estacionActiva.tipoEquipo(),
                transicion.getAccion(),
                this::iniciarOperacion,
                this::confirmarColocacion,
                true
        );
        vistaPuesto.mostrarInteraccion(panelInteraccion, transicion.getAccion());
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
        if (menuAbierto != null) {
            cerrarMenu();
        } else {
            cerrarPanelAcciones();
        }
    }

    private void alternarMenu(Menu menu) {
        if (operacionEnMarcha) {
            mostrarAviso("El equipo está en operación; espera a que termine antes de abrir otro menú.");
            return;
        }
        teclasPresionadas.removeAll(TECLAS_MOVIMIENTO);
        if (menuAbierto == menu) {
            cerrarMenu();
        } else {
            if (menuAbierto != null) cerrarMenu();
            cerrarPanelAcciones();
            if (menu == Menu.ALMACEN) {
                abrirMenuAlmacen();
            } else {
                abrirMenuCuaderno();
            }
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
        requestFocus();
    }

    private StackPane crearOverlay(String tituloTexto, String subtituloTexto) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("game-menu-overlay");
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

    private void abrirMenuAlmacen() {
        menuAbierto = Menu.ALMACEN;
        StackPane overlay = crearOverlay("Almacén y experimentos", "Elige los reactivos disponibles y la práctica que quieres realizar.");
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
        List<Reaccion> disponibles = catalogo.obtenerReacciones().stream()
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
            String mensaje = filtroExperimentos.isBlank()
                    ? "No hay prácticas con los reactivos marcados. Ajusta el almacén para continuar."
                    : "No se encontraron experimentos con ese nombre y los reactivos disponibles.";
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
        Label ecuacion = new Label(formatearEcuacion(reaccion));
        ecuacion.getStyleClass().add("game-menu-subtitle");
        detalle.getChildren().addAll(nombre, ecuacion);
        boton.setGraphic(detalle);
        boton.setAccessibleText(reaccion.getNombre() + ": " + formatearEcuacion(reaccion));
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
        experimentoSeleccionado = fabricaExperimento.crearExperimentoParaReaccion(reaccion);
        automataSeleccionado = FabricaAutomata.crearAutomataParaReaccion(reaccion.getId());
        actualizarDetalleExperimento();
    }

    private void actualizarDetalleExperimento() {
        if (detalleExperimento == null || botonIniciarExperimento == null) return;
        if (reaccionSeleccionada == null || experimentoSeleccionado == null) {
            detalleExperimento.setText("Selecciona una práctica para ver su estación y objetivo.");
            botonIniciarExperimento.setDisable(true);
            return;
        }
        EstacionLaboratorio estacion = primeraEstacionDe(
                reaccionSeleccionada, experimentoSeleccionado, automataSeleccionado);
        String nombreEstacion = estacion == null ? "sin puesto asignado" : estacion.nombre();
        boolean disponible = reaccionDisponible(reaccionSeleccionada);
        detalleExperimento.setText("Seleccionado: " + reaccionSeleccionada.getNombre()
                + "\nPrimera estación: " + nombreEstacion
                + "\nObjetivo: " + experimentoSeleccionado.getObjetivo().getDescripcion()
                + (disponible ? "" : "\nFaltan reactivos marcados en el almacén."));
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
        EstacionLaboratorio destino = obtenerEstacionRequerida();
        mostrarAviso("Experimento iniciado. Ve a " + (destino == null ? "la estación indicada" : destino.nombre()) + " y pulsa F.");
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
        botonInventario.getStyleClass().add("game-notebook-tab");
        botonHistorial.getStyleClass().add("game-notebook-tab");
        VBox contenido = new VBox(10);
        contenido.getStyleClass().add("game-notebook-screen");
        contenido.setPadding(new Insets(12));
        contenido.setMinHeight(360);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        contenidoCuaderno = contenido;
        botonInventarioCuaderno = botonInventario;
        botonHistorialCuaderno = botonHistorial;
        botonInventario.setOnAction(evento -> {
            seccionCuaderno = SeccionCuaderno.INVENTARIO;
            mostrarSeccionCuaderno(contenido, botonInventario, botonHistorial);
        });
        botonHistorial.setOnAction(evento -> {
            seccionCuaderno = SeccionCuaderno.HISTORIAL;
            mostrarSeccionCuaderno(contenido, botonInventario, botonHistorial);
        });
        tabs.getChildren().addAll(botonInventario, botonHistorial);
        Label pie = new Label("CHEMLAB OS  ·  ARCHIVO LOCAL CIFRADO  ·  SELECCIONA UN REGISTRO PARA ABRIR SU AUTÓMATA");
        pie.getStyleClass().add("game-notebook-micro");
        tarjeta.getChildren().addAll(metadatos, tabs, contenido, pie);
        mostrarSeccionCuaderno(contenido, botonInventario, botonHistorial);
        getChildren().add(overlay);
        overlayMenu = overlay;
    }

    private void mostrarSeccionCuaderno(VBox contenido, Button inventario, Button historial) {
        contenido.getChildren().clear();
        inventario.getStyleClass().setAll("game-notebook-tab");
        historial.getStyleClass().setAll("game-notebook-tab");
        if (seccionCuaderno == SeccionCuaderno.INVENTARIO) {
            inventario.getStyleClass().setAll("game-notebook-tab", "game-notebook-tab-selected");
            contenido.getChildren().add(construirVistaInventario());
        } else {
            historial.getStyleClass().setAll("game-notebook-tab", "game-notebook-tab-selected");
            contenido.getChildren().add(construirVistaHistorial());
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
                String estado = registro.isExitoso() ? "COMPLETADO" : "ABANDONADO";
                Label titulo = new Label((registro.isExitoso() ? "✓ " : "○ ") + registro.getNombreExperimento());
                titulo.setStyle("-fx-font-weight: bold;");
                Label fecha = new Label(registro.getFecha().format(FECHA_REGISTRO) + " · " + estado);
                fecha.setText(fecha.getText() + " / " + registro.getPasosTransicion().size() + " pasos");
                fecha.getStyleClass().add("game-menu-subtitle");
                Label pasos = new Label(registro.getPasosTransicion().size() + " pasos registrados");
                pasos.getStyleClass().add("game-menu-subtitle");
                pasos.setManaged(false);
                pasos.setVisible(false);
                String acciones = registro.getPasosTransicion().stream()
                        .map(RegistroExperimento.PasoTransicion::accion)
                        .reduce((anterior, actual) -> anterior + " → " + actual)
                        .orElse("Sin acciones registradas");
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
                abrirRegistro.setAccessibleText("Abrir autómata del experimento " + registro.getNombreExperimento());
                abrirRegistro.getStyleClass().add("game-notebook-entry-button");
                abrirRegistro.setOnAction(evento -> mostrarAutomataEnCuaderno(registro));
                vista.getChildren().add(abrirRegistro);
            }
        }
        return crearScroll(vista);
    }

    private void mostrarAutomataEnCuaderno(RegistroExperimento registro) {
        if (contenidoCuaderno == null) return;
        Button volver = new Button("‹  VOLVER A EXPERIMENTOS");
        volver.getStyleClass().add("game-notebook-back-button");
        volver.setOnAction(evento -> {
            seccionCuaderno = SeccionCuaderno.HISTORIAL;
            mostrarSeccionCuaderno(contenidoCuaderno, botonInventarioCuaderno, botonHistorialCuaderno);
        });

        Label titulo = new Label(registro.getNombreExperimento());
        titulo.getStyleClass().add("game-notebook-summary");
        Label descripcion = new Label("AUTÓMATA DE " + registro.getIdProceso()
                + "  ·  " + (registro.isExitoso() ? "PROCESO COMPLETADO" : "RUTA RECORRIDA HASTA EL ABANDONO"));
        descripcion.getStyleClass().add("game-notebook-micro");
        VBox encabezado = new VBox(4, volver, titulo, descripcion);

        LienzoAutomataRegistro lienzo = new LienzoAutomataRegistro(registro);
        ScrollPane desplazamiento = new ScrollPane(lienzo);
        desplazamiento.setFitToWidth(false);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        desplazamiento.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        desplazamiento.setPrefViewportHeight(390);
        desplazamiento.getStyleClass().add("game-automaton-scroll");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);
        VBox detalle = new VBox(10, encabezado, desplazamiento);
        detalle.getStyleClass().add("game-automaton-detail");
        contenidoCuaderno.getChildren().setAll(detalle);
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
        int indiceProducto = renderizador.productoBajoPuntero(
                evento.getX(), evento.getY(), productosEnMesa);
        boolean puedeRecoger = productoEnMano == null && indiceProducto >= 0
                && productosEnMesa.get(indiceProducto).estacionOrigen()
                .distanciaHasta(jugador.getX(), jugador.getY()) <= 82;
        if (resaltarAlmacen || puedeRecoger || productoEnMano != null) {
            renderizador.setCursor(CursorGuante.cerrado());
        }
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
        int indiceProducto = renderizador.productoBajoPuntero(
                evento.getX(), evento.getY(), productosEnMesa);
        boolean puedeRecoger = productoEnMano == null && indiceProducto >= 0
                && productosEnMesa.get(indiceProducto).estacionOrigen()
                .distanciaHasta(jugador.getX(), jugador.getY()) <= 82;
        boolean sobreAlmacen = renderizador.punteroSobreAlmacen(evento.getX(), evento.getY());
        resaltarAlmacen = productoEnMano != null && sobreAlmacen && renderizador.jugadorJuntoAlAlmacen();
        renderizador.setCursor(puedeRecoger || resaltarAlmacen || productoEnMano != null
                ? CursorGuante.abierto() : Cursor.DEFAULT);
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
            experimentoVisible = experimento;
            labelEstado.setText("EN PROGRESO");
            labelObjetivo.setText(experimento.getObjetivo().getDescripcion());
            mostrarAviso("Experimento iniciado. Interactúa con el puesto asignado.");
        });
    }

    @Override
    public void onAccionEjecutada(ResultadoAccion resultado) {
        Platform.runLater(() -> {
            labelEstado.setText("Estado: " + resultado.getEstadoActual().getNombre());
            mostrarAviso((resultado.isExitoso() ? "✓ " : "× ") + resultado.getMensaje());
            if (sesion.isActiva() && estacionActiva != null) {
                EstacionLaboratorio siguiente = obtenerEstacionRequerida();
                if (siguiente != estacionActiva) {
                    String nombreSiguiente = siguiente == null ? "el puesto indicado" : siguiente.nombre();
                    muestraEnProceso = describirMuestraTransferible(resultado);
                    trasladoPendiente = siguiente != null;
                    cerrarVistaPuesto(true);
                    estacionActiva = null;
                    labelEstado.setText("CAMBIA DE PUESTO · " + nombreSiguiente);
                    mostrarAviso("Traslada el procedimiento a " + nombreSiguiente + " y pulsa F.");
                } else {
                    mostrarAccionesDisponibles();
                }
            }
        });
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
            labelObjetivo.setText("Práctica registrada en tu cuaderno");
            cerrarVistaPuesto(true);
            estacionActiva = null;
            teclasPresionadas.clear();
            operacionEnMarcha = false;
            accionEnCurso = null;
            mostrarAviso("¡Experimento completado! El producto quedó en la mesa; recógelo con un clic.");
            experimentoSeleccionado = null;
            reaccionSeleccionada = null;
            automataSeleccionado = null;
            requestFocus();
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
            experimentoVisible = experimento;
            labelEstado.setText("EXPERIMENTO ABANDONADO");
            labelObjetivo.setText("El intento quedó registrado en el cuaderno");
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
}
