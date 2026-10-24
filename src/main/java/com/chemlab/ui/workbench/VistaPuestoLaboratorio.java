package com.chemlab.ui.workbench;

import com.chemlab.game.procedure.ProtocoloProcedimiento;
import com.chemlab.game.world.EstacionLaboratorio;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.ui.components.CursorGuante;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.chemlab.automata.TransicionQuimica;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Puesto de trabajo inmersivo. El lienzo representa el aparato y acepta la
 * carga sobre su zona física; sus mandos dibujados inician las operaciones.
 */
public final class VistaPuestoLaboratorio extends BorderPane {

    private static final double LIENZO_ANCHO = 900;
    private static final double LIENZO_ALTO = 560;
    private static final String MARCADOR_MUESTRA = "CHEMLAB_MUESTRA_TRANSFERIBLE";

    private final EstacionLaboratorio estacion;
    private final Reaccion reaccion;
    private final ProtocoloProcedimiento.InfoPasoProcedimiento protocolo;
    private final VisualizadorReaccion.Perfil perfilVisual;
    private final LienzoPuesto lienzo = new LienzoPuesto();
    private final VBox panelLateral = new VBox(12);
    private final VBox contenidoInteraccion = new VBox(10);
    private final VBox panelTraslado = new VBox(6);
    private final ScrollPane scrollInteraccion = new ScrollPane();
    private final Label labelEstado = new Label("EQUIPO EN ESPERA");
    private final Label labelPaso = new Label();
    private final Label labelCondiciones = new Label();
    private final Label estadoTraslado = new Label();
    private final Button fichaMuestra = new Button();
    private PanelInteraccionProcedimiento interaccion;
    private String descripcionMuestra = "";
    private boolean transferenciaRequerida;
    private boolean transferenciaCompletada = true;
    private boolean muestraEnPuesto;
    private Runnable alCompletarTraslado = () -> {};

    public VistaPuestoLaboratorio(
            EstacionLaboratorio estacion,
            Reaccion reaccion,
            ProtocoloProcedimiento.InfoPasoProcedimiento protocolo,
            List<TransicionQuimica> transiciones,
            Consumer<TransicionQuimica> alSeleccionarTransicion,
            Runnable alCerrar,
            Runnable alAbandonar
    ) {
        this.estacion = estacion;
        this.reaccion = reaccion;
        this.protocolo = protocolo;
        this.perfilVisual = VisualizadorReaccion.crear(reaccion);

        getStyleClass().add("workbench-overlay");
        setCursor(CursorGuante.abierto());
        setPadding(new Insets(15, 18, 15, 18));
        setTop(construirCabecera(alCerrar, alAbandonar));
        setCenter(construirAreaTrabajo(transiciones, alSeleccionarTransicion));
        setBottom(construirPie());
        setMinSize(0, 0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    private HBox construirCabecera(Runnable alCerrar, Runnable alAbandonar) {
        Label indicador = new Label("ESTACIÓN ACTIVA");
        indicador.getStyleClass().add("workbench-eyebrow");
        Label titulo = new Label(estacion.nombre());
        titulo.getStyleClass().add("workbench-title");
        Label reaccionLabel = new Label(reaccion.getNombre());
        reaccionLabel.getStyleClass().add("workbench-reaction");
        VBox identidad = new VBox(2, indicador, titulo, reaccionLabel);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Label telemetria = new Label("●  SISTEMA EN LÍNEA");
        telemetria.getStyleClass().add("workbench-online");
        Button abandonar = new Button("Abandonar práctica");
        abandonar.getStyleClass().add("workbench-quiet-button");
        abandonar.setCursor(CursorGuante.abierto());
        abandonar.setOnAction(evento -> alAbandonar.run());
        Button cerrar = new Button("Volver al laboratorio  ·  Esc");
        cerrar.getStyleClass().add("workbench-close-button");
        cerrar.setCursor(CursorGuante.abierto());
        cerrar.setOnAction(evento -> alCerrar.run());
        HBox cabecera = new HBox(12, identidad, espacio, telemetria, abandonar, cerrar);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.getStyleClass().add("workbench-header");
        return cabecera;
    }

    private HBox construirAreaTrabajo(
            List<TransicionQuimica> transiciones,
            Consumer<TransicionQuimica> alSeleccionarTransicion
    ) {
        StackPane escena = new StackPane(lienzo);
        escena.getStyleClass().add("workbench-stage");
        escena.setMinSize(350, 300);
        lienzo.widthProperty().bind(escena.widthProperty());
        lienzo.heightProperty().bind(escena.heightProperty());

        panelLateral.getStyleClass().add("workbench-rack");
        panelLateral.setPadding(new Insets(16));
        panelLateral.setPrefWidth(344);
        panelLateral.setMinWidth(285);
        panelLateral.setMaxWidth(390);

        Label cabeceraRack = new Label("CONSOLA DEL EQUIPO");
        cabeceraRack.getStyleClass().add("workbench-rack-title");
        labelPaso.setText(protocolo.nombreEstado().toUpperCase(Locale.ROOT));
        labelPaso.getStyleClass().add("workbench-step");
        labelPaso.setWrapText(true);
        labelCondiciones.setText(String.format(Locale.ROOT,
                "OBJETIVO  %.0f °C     ·     %.3f atm\nATMÓSFERA  %s",
                protocolo.temperaturaObjetivo(), protocolo.presionObjetivo(), protocolo.atmosferaRequerida()));
        labelCondiciones.getStyleClass().add("workbench-telemetry");
        labelCondiciones.setWrapText(true);
        Label explicacion = new Label(protocolo.explicacionManejo());
        explicacion.getStyleClass().add("workbench-instruction");
        explicacion.setWrapText(true);
        Label fenomeno = new Label("CAMBIO OBSERVABLE  ·  " + perfilVisual.fenomeno().etiqueta());
        fenomeno.getStyleClass().add("workbench-phenomenon");
        fenomeno.setWrapText(true);

        panelLateral.getChildren().addAll(cabeceraRack, labelPaso, labelCondiciones, fenomeno, explicacion);
        if (transiciones.size() > 1) {
            Label elegir = new Label("OPERACIONES DISPONIBLES");
            elegir.getStyleClass().add("workbench-rack-title");
            FlowPane selector = new FlowPane(7, 7);
            for (TransicionQuimica transicion : transiciones) {
                Button opcion = new Button(nombreAccion(transicion.getAccion()));
                opcion.getStyleClass().add("workbench-action-choice");
                opcion.setCursor(CursorGuante.abierto());
                opcion.setOnAction(evento -> alSeleccionarTransicion.accept(transicion));
                selector.getChildren().add(opcion);
            }
            panelLateral.getChildren().addAll(elegir, selector);
        }
        labelEstado.getStyleClass().add("workbench-state");
        labelEstado.setWrapText(true);
        labelEstado.setMinHeight(38);
        labelEstado.setPrefHeight(38);
        labelEstado.setMaxHeight(38);
        panelLateral.getChildren().add(labelEstado);
        panelTraslado.getStyleClass().add("workbench-transfer-panel");
        Label tituloTraslado = new Label("MUESTRA DEL PROCESO");
        tituloTraslado.getStyleClass().add("workbench-rack-title");
        fichaMuestra.getStyleClass().add("workbench-transfer-item");
        fichaMuestra.setWrapText(true);
        fichaMuestra.setMaxWidth(Double.MAX_VALUE);
        fichaMuestra.setCursor(CursorGuante.abierto());
        fichaMuestra.setOnDragDetected(evento -> iniciarArrastreDeMuestra(evento));
        fichaMuestra.setOnDragDone(evento -> {
            if (getScene() != null) getScene().setCursor(CursorGuante.abierto());
            evento.consume();
        });
        estadoTraslado.getStyleClass().add("workbench-transfer-status");
        estadoTraslado.setWrapText(true);
        panelTraslado.getChildren().addAll(tituloTraslado, fichaMuestra, estadoTraslado);
        panelTraslado.setVisible(false);
        panelTraslado.setManaged(false);
        scrollInteraccion.setFitToWidth(true);
        scrollInteraccion.setPrefViewportHeight(320);
        scrollInteraccion.setMinHeight(150);
        scrollInteraccion.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollInteraccion.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollInteraccion.getStyleClass().add("workbench-interaction-scroll");
        VBox.setVgrow(scrollInteraccion, Priority.ALWAYS);
        panelLateral.getChildren().add(scrollInteraccion);
        scrollInteraccion.setContent(contenidoInteraccion);

        HBox area = new HBox(14, escena, panelLateral);
        HBox.setHgrow(escena, Priority.ALWAYS);
        area.setAlignment(Pos.CENTER);
        area.setPadding(new Insets(14, 0, 12, 0));
        return area;
    }

    private HBox construirPie() {
        Label objetivo = new Label("SIGUIENTE  " + protocolo.accionSiguienteSugerida());
        objetivo.getStyleClass().add("workbench-next");
        objetivo.setWrapText(true);
        Label interaccion = new Label("Arrastra los materiales al recipiente resaltado o haz clic en el mando del aparato.");
        interaccion.getStyleClass().add("workbench-hint");
        interaccion.setWrapText(true);
        HBox pie = new HBox(18, objetivo, interaccion);
        pie.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(objetivo, Priority.ALWAYS);
        HBox.setHgrow(interaccion, Priority.ALWAYS);
        pie.getStyleClass().add("workbench-footer");
        pie.setPadding(new Insets(9, 13, 9, 13));
        return pie;
    }

    public void mostrarInteraccion(PanelInteraccionProcedimiento nueva, AccionLaboratorio accion) {
        interaccion = nueva;
        interaccion.setMaxWidth(Double.MAX_VALUE);
        reconstruirContenidoInteraccion();
        lienzo.setAccionSeleccionada(accion);
        labelEstado.setText(accion == AccionLaboratorio.CARGAR_REACTIVO
                ? "ESPERANDO CARGA EN EL RECIPIENTE" : "LISTO · ACCIONA EL MANDO DEL EQUIPO");
        lienzo.renderizar();
    }

    public void actualizarProceso(AccionLaboratorio accion, double progreso, String descripcion) {
        lienzo.setProceso(accion, progreso);
        labelEstado.setText(descripcion.toUpperCase(Locale.ROOT));
    }

    public void redibujar() {
        lienzo.renderizar();
    }

    public void exigirTrasladoDeMuestra(String descripcion, Runnable alCompletar) {
        descripcionMuestra = descripcion;
        transferenciaRequerida = true;
        transferenciaCompletada = false;
        muestraEnPuesto = false;
        alCompletarTraslado = alCompletar == null ? () -> {} : alCompletar;
        actualizarPanelTraslado();
        lienzo.reiniciarAcceso();
        actualizarEstadoAcceso();
        reconstruirContenidoInteraccion();
        lienzo.renderizar();
    }

    public void mostrarMuestraEnPuesto(String descripcion) {
        descripcionMuestra = descripcion;
        transferenciaRequerida = false;
        transferenciaCompletada = true;
        muestraEnPuesto = true;
        actualizarPanelTraslado();
        lienzo.cerrarAcceso();
        reconstruirContenidoInteraccion();
        lienzo.renderizar();
    }

    public boolean transferenciaLista() {
        return !transferenciaRequerida || transferenciaCompletada;
    }

    private void iniciarArrastreDeMuestra(MouseEvent evento) {
        if (!transferenciaRequerida || transferenciaCompletada) return;
        Dragboard dragboard = fichaMuestra.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent contenido = new ClipboardContent();
        contenido.putString("CHEMLAB_MUESTRA_TRANSFERIBLE");
        dragboard.setContent(contenido);
        if (getScene() != null) getScene().setCursor(CursorGuante.cerrado());
        evento.consume();
    }

    private void actualizarPanelTraslado() {
        boolean visible = transferenciaRequerida || muestraEnPuesto;
        panelTraslado.setManaged(visible);
        panelTraslado.setVisible(visible);
        fichaMuestra.setText(descripcionMuestra);
        fichaMuestra.setDisable(!transferenciaRequerida || transferenciaCompletada);
        estadoTraslado.setText(transferenciaRequerida
                ? "Abre el acceso del equipo y arrastra la muestra hasta su cámara o recipiente."
                : "Muestra recibida y montada en este puesto.");
    }

    private void reconstruirContenidoInteraccion() {
        contenidoInteraccion.getChildren().clear();
        if (transferenciaRequerida || muestraEnPuesto) contenidoInteraccion.getChildren().add(panelTraslado);
        if (interaccion != null) contenidoInteraccion.getChildren().add(interaccion);
    }

    private void actualizarEstadoAcceso() {
        estadoTraslado.setText(lienzo.estaAbierto()
                ? "Acceso abierto · suelta la muestra dentro del equipo."
                : "Haz clic en el seguro o la tapa para abrir el equipo.");
    }

    private void recibirMuestra(String idTransferencia) {
        transferenciaCompletada = true;
        transferenciaRequerida = false;
        muestraEnPuesto = true;
        lienzo.cerrarAcceso();
        lienzo.materialReciente = idTransferencia;
        panelMuestraRecibida();
        alCompletarTraslado.run();
        lienzo.renderizar();
    }

    private void panelMuestraRecibida() {
        actualizarPanelTraslado();
        estadoTraslado.setText("Muestra dentro del equipo · lista para el siguiente paso.");
    }

    private String nombreAccion(AccionLaboratorio accion) {
        return switch (accion) {
            case CARGAR_REACTIVO -> "Cargar materiales";
            case AGREGAR_CATALIZADOR -> "Montar catalizador";
            case AGREGAR_AGUA -> "Añadir agua";
            case CALENTAR -> "Calentar";
            case ENFRIAR -> "Enfriar";
            case MEZCLAR -> "Mezclar";
            case INICIAR_PLASMA -> "Encender plasma";
            case COMPRIMIR -> "Comprimir";
            case DESCOMPRIMIR -> "Liberar presión";
            case ACTIVAR_IGNICION -> "Encender";
            case ESPERAR -> "Mantener";
            case FINALIZAR -> "Finalizar";
            case SELECCIONAR_METODO -> "Preparar método";
        };
    }

    private final class LienzoPuesto extends Canvas {
        private AccionLaboratorio accionSeleccionada;
        private AccionLaboratorio accionEnCurso;
        private double progreso;
        private boolean destinoActivo;
        private long animacionCargaHasta;
        private String materialReciente;
        private double origenCargaX;
        private double origenCargaY;
        private boolean accesoAbierto;

        private LienzoPuesto() {
            setOnMouseClicked(this::alHacerClic);
            setOnMouseMoved(this::alMoverMouse);
            setOnDragOver(this::alArrastrarSobreEquipo);
            setOnDragExited(evento -> {
                destinoActivo = false;
                renderizar();
            });
            setOnDragDropped(this::alSoltarEnEquipo);
            widthProperty().addListener((observable, anterior, nuevo) -> renderizar());
            heightProperty().addListener((observable, anterior, nuevo) -> renderizar());
        }

        private void setAccionSeleccionada(AccionLaboratorio accion) {
            accionSeleccionada = accion;
            accionEnCurso = null;
            progreso = 0;
        }

        private void setProceso(AccionLaboratorio accion, double valorProgreso) {
            accionEnCurso = accion;
            progreso = valorProgreso;
            renderizar();
        }

        private void reiniciarAcceso() {
            accesoAbierto = false;
        }

        private void cerrarAcceso() {
            accesoAbierto = false;
        }

        private boolean estaAbierto() {
            return accesoAbierto;
        }

        private void alHacerClic(MouseEvent evento) {
            if (transferenciaRequerida && !transferenciaCompletada) {
                if (enPestilloAcceso(evento.getX(), evento.getY())) {
                    accesoAbierto = !accesoAbierto;
                    actualizarEstadoAcceso();
                    renderizar();
                    evento.consume();
                    return;
                }
                if (enRectanguloControl(evento.getX(), evento.getY())
                        || enHerramientaActiva(evento.getX(), evento.getY())) {
                    labelEstado.setText("PRIMERO ABRE EL EQUIPO Y COLOCA LA MUESTRA");
                    evento.consume();
                }
                return;
            }
            if ((!enRectanguloControl(evento.getX(), evento.getY())
                    && !enHerramientaActiva(evento.getX(), evento.getY()))
                    || interaccion == null || !interaccion.permiteAccionarHerramienta()
                    || !transferenciaLista()) return;
            interaccion.accionarHerramienta();
            labelEstado.setText("EQUIPO EN OPERACIÓN · " + nombreAccion(accionSeleccionada).toUpperCase(Locale.ROOT));
            renderizar();
            evento.consume();
        }

        private void alMoverMouse(MouseEvent evento) {
            setCursor(CursorGuante.abierto());
        }

        private void alArrastrarSobreEquipo(DragEvent evento) {
            String id = evento.getDragboard().getString();
            boolean arrastraMuestra = MARCADOR_MUESTRA.equals(id);
            boolean destino = arrastraMuestra
                    ? transferenciaRequerida && !transferenciaCompletada && accesoAbierto
                    && enZonaTransferencia(evento.getX(), evento.getY())
                    : enZonaCarga(evento.getX(), evento.getY()) && interaccion != null
                    && interaccion.aceptaCargaDesdeMesa(id);
            if (destino) {
                evento.acceptTransferModes(arrastraMuestra ? TransferMode.MOVE : TransferMode.COPY);
            }
            if (destinoActivo != destino) {
                destinoActivo = destino;
                renderizar();
            }
            evento.consume();
        }

        private void alSoltarEnEquipo(DragEvent evento) {
            boolean colocado = false;
            if (evento.getDragboard().hasString()
                    && MARCADOR_MUESTRA.equals(evento.getDragboard().getString())) {
                if (transferenciaRequerida && !transferenciaCompletada && accesoAbierto
                        && enZonaTransferencia(evento.getX(), evento.getY())) {
                    colocado = true;
                    double[] punto = aCoordenadasLogicas(evento.getX(), evento.getY());
                    materialReciente = "MUESTRA";
                    origenCargaX = punto[0];
                    origenCargaY = punto[1];
                    animacionCargaHasta = System.nanoTime() + 950_000_000L;
                    recibirMuestra(MARCADOR_MUESTRA);
                } else if (transferenciaRequerida && !transferenciaCompletada) {
                    estadoTraslado.setText(accesoAbierto
                            ? "Suelta la muestra dentro del recipiente marcado."
                            : "Haz clic en el seguro o la tapa antes de introducir la muestra.");
                }
            } else if (evento.getDragboard().hasString() && enZonaCarga(evento.getX(), evento.getY())
                    && interaccion != null) {
                String id = evento.getDragboard().getString();
                colocado = interaccion.colocarElementoDesdeMesa(id);
                if (colocado) {
                    double[] punto = aCoordenadasLogicas(evento.getX(), evento.getY());
                    materialReciente = id;
                    origenCargaX = punto[0];
                    origenCargaY = punto[1];
                    animacionCargaHasta = System.nanoTime() + 850_000_000L;
                }
            }
            evento.setDropCompleted(colocado);
            destinoActivo = false;
            renderizar();
            evento.consume();
        }

        private boolean enPestilloAcceso(double x, double y) {
            double[] punto = aCoordenadasLogicas(x, y);
            double lx = punto[0];
            double ly = punto[1];
            return switch (estacion.tipoEquipo()) {
                case MATRAZ -> dentro(lx, ly, 390, 215, 420, 246);
                case HORNO -> dentro(lx, ly, 278, 270, 312, 322);
                case REACTOR -> dentro(lx, ly, 342, 216, 379, 258);
                case CAMARA_SELLADA -> dentro(lx, ly, 539, 276, 578, 321);
                case BANDEJA -> dentro(lx, ly, 269, 269, 311, 309);
                default -> dentro(lx, ly, 282, 235, 318, 281);
            };
        }

        private boolean enZonaTransferencia(double x, double y) {
            double[] punto = aCoordenadasLogicas(x, y);
            double[] zona = switch (estacion.tipoEquipo()) {
                case MATRAZ -> new double[]{360, 231, 449, 362};
                case HORNO -> new double[]{309, 235, 493, 354};
                case REACTOR -> new double[]{375, 245, 505, 358};
                case CAMARA_SELLADA -> new double[]{298, 235, 550, 354};
                case BANDEJA -> new double[]{270, 274, 568, 345};
                default -> new double[]{301, 242, 533, 347};
            };
            return accesoAbierto && punto[0] >= zona[0] && punto[0] <= zona[2]
                    && punto[1] >= zona[1] && punto[1] <= zona[3];
        }

        private boolean enRectanguloControl(double x, double y) {
            double[] punto = aCoordenadasLogicas(x, y);
            return punto[0] >= 640 && punto[0] <= 822 && punto[1] >= 280 && punto[1] <= 370;
        }

        private boolean enHerramientaActiva(double x, double y) {
            if (accionSeleccionada == null) return false;
            double[] punto = aCoordenadasLogicas(x, y);
            double lx = punto[0];
            double ly = punto[1];
            return switch (estacion.tipoEquipo()) {
                case MATRAZ -> switch (accionSeleccionada) {
                    case CALENTAR -> dentro(lx, ly, 382, 302, 425, 360); // válvula y tubo del mechero
                    case MEZCLAR -> dentro(lx, ly, 307, 291, 520, 348); // placa agitadora
                    case ACTIVAR_IGNICION -> dentro(lx, ly, 365, 224, 448, 296); // chispero junto al matraz
                    case ENFRIAR, FINALIZAR -> dentro(lx, ly, 362, 238, 450, 360); // cuello y pared del matraz
                    default -> false;
                };
                case HORNO -> switch (accionSeleccionada) {
                    case CALENTAR, ENFRIAR, ESPERAR -> dentro(lx, ly, 510, 222, 564, 310); // panel térmico y perilla
                    case FINALIZAR -> dentro(lx, ly, 296, 225, 506, 358); // puerta de la mufla
                    default -> false;
                };
                case REACTOR -> switch (accionSeleccionada) {
                    case COMPRIMIR -> dentro(lx, ly, 588, 325, 650, 380); // bomba hidráulica
                    case DESCOMPRIMIR -> dentro(lx, ly, 603, 230, 673, 303); // manómetro y descarga
                    case CALENTAR, ENFRIAR, FINALIZAR -> dentro(lx, ly, 375, 246, 504, 355); // cápsula HPHT
                    default -> false;
                };
                case CAMARA_SELLADA -> switch (accionSeleccionada) {
                    case INICIAR_PLASMA -> dentro(lx, ly, 345, 246, 503, 350); // plasma y antena
                    case CALENTAR -> dentro(lx, ly, 382, 300, 466, 346); // portamuestras
                    case DESCOMPRIMIR -> dentro(lx, ly, 593, 239, 663, 304); // válvula de vacío
                    case FINALIZAR -> dentro(lx, ly, 294, 237, 551, 355); // puerta de la cámara
                    default -> false;
                };
                case BANDEJA -> dentro(lx, ly, 255, 263, 585, 367); // cubeta y soportes
                default -> false;
            };
        }

        private double[] aCoordenadasLogicas(double x, double y) {
            double escala = escala();
            double desplazamientoX = (getWidth() - LIENZO_ANCHO * escala) / 2;
            double desplazamientoY = (getHeight() - LIENZO_ALTO * escala) / 2;
            double logicoX = (x - desplazamientoX) / escala;
            double logicoY = (y - desplazamientoY) / escala;
            return new double[]{logicoX, logicoY};
        }

        private boolean dentro(double x, double y, double izq, double arriba, double der, double abajo) {
            return x >= izq && x <= der && y >= arriba && y <= abajo;
        }

        private boolean enZonaCarga(double x, double y) {
            double escala = escala();
            double desplazamientoX = (getWidth() - LIENZO_ANCHO * escala) / 2;
            double desplazamientoY = (getHeight() - LIENZO_ALTO * escala) / 2;
            double logicoX = (x - desplazamientoX) / escala;
            double logicoY = (y - desplazamientoY) / escala;
            double[] zona = switch (estacion.tipoEquipo()) {
                case MATRAZ -> new double[]{360, 231, 449, 362};
                case HORNO -> new double[]{309, 235, 493, 354};
                case REACTOR -> new double[]{375, 245, 505, 358};
                case CAMARA_SELLADA -> new double[]{298, 235, 550, 354};
                case BANDEJA -> new double[]{270, 274, 568, 345};
                default -> new double[]{301, 242, 533, 347};
            };
            return logicoX >= zona[0] && logicoX <= zona[2]
                    && logicoY >= zona[1] && logicoY <= zona[3];
        }

        private double escala() {
            if (getWidth() <= 0 || getHeight() <= 0) return 1;
            return Math.min(getWidth() / LIENZO_ANCHO, getHeight() / LIENZO_ALTO);
        }

        private void renderizar() {
            if (getWidth() <= 0 || getHeight() <= 0) return;
            GraphicsContext gc = getGraphicsContext2D();
            double ancho = getWidth();
            double alto = getHeight();
            gc.setFill(Color.web("#0a1921"));
            gc.fillRect(0, 0, ancho, alto);
            double escala = escala();
            double margenX = (ancho - LIENZO_ANCHO * escala) / 2;
            double margenY = (alto - LIENZO_ALTO * escala) / 2;
            gc.save();
            try {
                gc.translate(margenX, margenY);
                gc.scale(escala, escala);

                dibujarHabitacion(gc);
                dibujarMesa(gc);
                dibujarCadenaQuimica(gc);
                dibujarAparato(gc);
                dibujarMuestraEnPuesto(gc);
                dibujarAccesoTraslado(gc);
                dibujarDestinoCarga(gc);
                dibujarMaterialesCargados(gc);
                dibujarAnimacionCarga(gc);
                dibujarTransformacion(gc);
                dibujarHerramientaInteractiva(gc);
                dibujarMando(gc);
            } finally {
                gc.restore();
            }
        }

        private void dibujarHabitacion(GraphicsContext gc) {
            gc.setFill(Color.web("#11232c"));
            gc.fillRect(0, 0, LIENZO_ANCHO, LIENZO_ALTO);
            gc.setFill(Color.web("#172f38"));
            gc.fillRect(0, 0, LIENZO_ANCHO, 132);
            gc.setStroke(Color.web("#45616a", 0.4));
            gc.setLineWidth(2);
            gc.strokeLine(0, 131, LIENZO_ANCHO, 131);
            for (int i = 0; i < 4; i++) {
                double x = 30 + i * 210;
                gc.setFill(Color.web("#1e3e49"));
                gc.fillRoundRect(x, 24, 178, 82, 10, 10);
                gc.setStroke(Color.web("#76a8ad", 0.55));
                gc.setLineWidth(2);
                gc.strokeRoundRect(x + 7, 31, 164, 68, 7, 7);
                gc.setFill(Color.web("#72b9c7", 0.16));
                gc.fillRoundRect(x + 9, 33, 160, 64, 5, 5);
            }
            gc.setFill(Color.web("#d7ece7"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.fillText("MESA DE OPERACIONES  /  " + estacion.nombre().toUpperCase(Locale.ROOT), 30, 160);
            gc.setFill(Color.web("#7f9ba2"));
            gc.setFont(Font.font("Consolas", 10));
            gc.fillText("PROTOCOLO  " + protocolo.nombreEstado().toUpperCase(Locale.ROOT), 30, 178);
        }

        private void dibujarMesa(GraphicsContext gc) {
            gc.setFill(Color.web("#050c10", 0.45));
            gc.fillRoundRect(37, 455, 830, 52, 16, 16);
            gc.setFill(Color.web("#344b50"));
            gc.fillRoundRect(24, 402, 852, 68, 12, 12);
            gc.setFill(Color.web("#738b89"));
            gc.fillRoundRect(24, 389, 852, 27, 9, 9);
            gc.setFill(Color.web("#95a9a0"));
            gc.fillRoundRect(31, 391, 838, 7, 5, 5);
            gc.setFill(Color.web("#263a40"));
            gc.fillRoundRect(54, 469, 34, 58, 6, 6);
            gc.fillRoundRect(810, 469, 34, 58, 6, 6);
            gc.setFill(Color.web("#557176", 0.46));
            gc.fillRoundRect(92, 185, 500, 195, 16, 16);
            gc.setStroke(Color.web("#789399", 0.22));
            gc.setLineWidth(1);
            for (int i = 0; i < 8; i++) gc.strokeLine(110 + i * 58, 202, 110 + i * 58, 360);
            for (int i = 0; i < 4; i++) gc.strokeLine(110, 215 + i * 43, 574, 215 + i * 43);
        }

        private void dibujarCadenaQuimica(GraphicsContext gc) {
            double transformacion = progresoQuimico();
            gc.setFill(Color.web("#0a181e", 0.96));
            gc.fillRoundRect(103, 418, 505, 48, 8, 8);
            gc.setStroke(Color.web("#4b7375", 0.8));
            gc.setLineWidth(1.2);
            gc.strokeRoundRect(103, 418, 505, 48, 8, 8);

            gc.setFill(Color.web("#87a7a4"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 8));
            gc.fillText("REACTIVOS", 115, 432);
            gc.fillText("PRODUCTOS  ·  " + perfilVisual.resumenFasesProducto(), 365, 432);
            gc.setFill(Color.web("#e1efea"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 9));
            gc.fillText(textoCorto(perfilVisual.resumenReactivos(), 24), 115, 449);
            gc.fillText(textoCorto(perfilVisual.resumenProductos(), 23), 365, 449);

            dibujarParticulas(gc, perfilVisual.reactivos(), 265, 443, 1 - transformacion * 0.72);
            gc.setStroke(Color.web("#9bbab3", 0.75));
            gc.setLineWidth(2);
            gc.strokeLine(314, 443, 342, 443);
            gc.strokeLine(335, 437, 342, 443);
            gc.strokeLine(335, 449, 342, 443);
            dibujarIconosProducto(gc, 558, 442, transformacion);

            gc.setFill(Color.web("#1d3338"));
            gc.fillRoundRect(115, 457, 425, 4, 2, 2);
            gc.setFill(Color.web("#55d6b8").interpolate(perfilVisual.tonoProductos(), transformacion));
            gc.fillRoundRect(115, 457, 425 * transformacion, 4, 2, 2);
        }

        private String textoCorto(String texto, int maximo) {
            return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
        }

        private void dibujarParticulas(GraphicsContext gc, List<VisualizadorReaccion.Especie> especies,
                                       double x, double y, double opacidad) {
            int indice = 0;
            for (VisualizadorReaccion.Especie especie : especies) {
                if (indice == 3) break;
                Color tono = especie.color();
                gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), Math.max(0.15, opacidad)));
                double cx = x + indice * 15;
                gc.fillOval(cx - 4, y - 4, 8, 8);
                gc.fillOval(cx + 3, y - 2, 5, 5);
                gc.strokeLine(cx, y, cx + 4, y);
                indice++;
            }
        }

        private void dibujarIconosProducto(GraphicsContext gc, double x, double y, double opacidad) {
            int indice = 0;
            for (VisualizadorReaccion.Especie especie : perfilVisual.productos()) {
                if (indice == 2) break;
                Color tono = especie.color();
                gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), Math.max(0.12, opacidad)));
                double cx = x + indice * 19;
                switch (especie.sustancia().getEstadoMateria()) {
                    case GASEOSO -> {
                        gc.fillOval(cx - 5, y - 5, 7, 7);
                        gc.fillOval(cx + 2, y - 11, 6, 6);
                        gc.fillOval(cx + 4, y + 1, 5, 5);
                    }
                    case LIQUIDO -> {
                        gc.fillOval(cx - 4, y - 4, 9, 9);
                        gc.fillPolygon(new double[]{cx, cx - 4, cx + 4},
                                new double[]{y - 10, y - 2, y - 2}, 3);
                    }
                    case SOLIDO -> {
                        if (especie.sustancia().getId().equals("DIAMANTE")) {
                            gc.fillPolygon(new double[]{cx, cx + 7, cx + 3, cx - 5},
                                    new double[]{y - 9, y - 2, y + 8, y + 1}, 4);
                        } else {
                            gc.fillPolygon(new double[]{cx, cx + 5, cx + 8, cx + 1, cx - 4},
                                    new double[]{y - 7, y - 2, y + 5, y + 8, y + 1}, 5);
                        }
                    }
                    case PLASMA -> {
                        gc.fillOval(cx - 6, y - 7, 13, 14);
                    }
                }
                indice++;
            }
        }

        private void dibujarAparato(GraphicsContext gc) {
            switch (estacion.tipoEquipo()) {
                case MATRAZ -> dibujarMesaSintesis(gc);
                case HORNO -> dibujarHorno(gc);
                case REACTOR -> dibujarReactor(gc);
                case CAMARA_SELLADA -> dibujarCamaraCvd(gc);
                case BANDEJA -> dibujarBandeja(gc);
                default -> dibujarEquipoGenerico(gc);
            }
        }

        private void dibujarMesaSintesis(GraphicsContext gc) {
            gc.setFill(Color.web("#263c43"));
            gc.fillRoundRect(284, 235, 260, 135, 18, 18);
            gc.setFill(Color.web("#111c20"));
            gc.fillRoundRect(311, 294, 207, 50, 9, 9);
            gc.setFill(Color.web("#758b8e"));
            gc.fillRoundRect(330, 306, 168, 12, 5, 5);
            gc.setFill(Color.web("#b5c9c5"));
            gc.fillRoundRect(379, 238, 54, 7, 3, 3);
            gc.setFill(Color.web("#53686c"));
            if (transferenciaRequerida && !transferenciaCompletada && accesoAbierto) {
                gc.fillRoundRect(461, 324, 18, 9, 3, 3);
                gc.setStroke(Color.web("#c6d7d1"));
                gc.setLineWidth(2);
                gc.strokeLine(467, 326, 467, 318);
            } else {
                gc.fillRoundRect(396, 224, 18, 14, 4, 4);
                gc.setFill(Color.web("#d9e8e3"));
                gc.fillRoundRect(399, 226, 12, 4, 2, 2);
            }
            gc.setFill(Color.web("#d7e5e0", 0.18));
            gc.fillOval(366, 237, 82, 124);
            if (perfilVisual.productoEnFase(EstadoMateria.LIQUIDO)
                    || perfilVisual.reactivos().stream().anyMatch(especie ->
                    especie.sustancia().getEstadoMateria() == EstadoMateria.LIQUIDO)) {
                dibujarLiquido(gc, 377, 318, 58, 23, colorProceso());
            }
            double avance = progresoQuimico();
            if (perfilVisual.productoEnFase(EstadoMateria.GASEOSO) && avance > 0.2) {
                dibujarGasProducto(gc, 395, 270, avance);
            }
            if (perfilVisual.productoEnFase(EstadoMateria.SOLIDO) && avance > 0.55) {
                dibujarCristales(gc, 393, 328, avance);
            }
            if (perfilVisual.esFenomeno(VisualizadorReaccion.Fenomeno.OXIDACION) && avance > 0.25) {
                dibujarCapaOxido(gc, 374, 328, 62, 10, avance);
            }
            gc.setStroke(Color.web("#c6e7e5", 0.92));
            gc.setLineWidth(4);
            gc.strokeLine(383, 246, 383, 280);
            gc.strokeLine(429, 246, 429, 280);
            gc.strokeLine(383, 280, 366, 328);
            gc.strokeLine(429, 280, 446, 328);
            gc.strokeArc(366, 300, 80, 57, 180, 180, javafx.scene.shape.ArcType.OPEN);
            gc.setStroke(Color.web("#b9d9d4", 0.7));
            gc.setLineWidth(2);
            gc.strokeLine(403, 223, 403, 192);
            gc.setFill(Color.web("#e7c668"));
            gc.fillOval(396, 188, 14, 9);
            dibujarMechero(gc, 404, 350);
            dibujarBotellaReactivo(gc, 206, 326, "REACTIVO");
            dibujarVidrio(gc, 550, 326);
            dibujarEtiquetaInstrumento(gc, "MATRAZ / AGITADOR", 310, 365);
        }

        private void dibujarHorno(GraphicsContext gc) {
            gc.setFill(Color.web("#46585d"));
            gc.fillRoundRect(273, 208, 310, 176, 14, 14);
            gc.setFill(Color.web("#172329"));
            gc.fillRoundRect(298, 226, 206, 131, 7, 7);
            gc.setFill(Color.web("#412e22"));
            gc.fillRoundRect(310, 237, 182, 111, 5, 5);
            boolean caliente = accionEnCurso == AccionLaboratorio.CALENTAR;
            gc.setFill(Color.web(caliente ? "#ff7138" : "#8d5b38", caliente ? 0.76 : 0.35));
            gc.fillRoundRect(322, 248, 158, 89, 4, 4);
            gc.setFill(Color.web("#dfad72"));
            gc.fillRoundRect(325, 325, 152, 8, 3, 3);
            gc.setFill(colorProceso());
            gc.fillRoundRect(358, 294, 89, 16, 3, 3);
            if (perfilVisual.esFenomeno(VisualizadorReaccion.Fenomeno.OXIDACION)
                    && progresoQuimico() > 0.18) {
                dibujarCapaOxido(gc, 360, 294, 85, 16, progresoQuimico());
            }
            if (perfilVisual.productoEnFase(EstadoMateria.GASEOSO)
                    && progresoQuimico() > 0.32) {
                dibujarGasProducto(gc, 398, 277, progresoQuimico());
            }
            for (int i = 0; i < 6; i++) {
                double opacidad = caliente
                        ? 0.4 + 0.45 * Math.abs(Math.sin(i + progreso * 8))
                        : 0.4;
                gc.setFill(Color.web(caliente ? "#ffcb7a" : "#72888a", Math.min(0.9, Math.max(0.1, opacidad))));
                gc.fillOval(332 + i * 24, 254 + (i % 2) * 22, 8, 8);
            }
            gc.setFill(Color.web("#819397"));
            gc.fillRoundRect(512, 224, 48, 142, 7, 7);
            gc.setFill(Color.web("#17282d"));
            gc.fillRoundRect(519, 235, 34, 35, 4, 4);
            gc.setFill(Color.web("#98ffe3"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
            gc.fillText(String.format(Locale.ROOT, "%.0f°", temperaturaVisual()), 523, 257);
            dibujarPerilla(gc, 536, 292);
            gc.setFill(Color.web("#c9d3ce"));
            gc.fillRoundRect(286, 284, 8, 28, 3, 3);
            gc.setFill(Color.web("#3c4b4e"));
            gc.fillOval(282, 281, 14, 9);
            if (transferenciaRequerida || muestraEnPuesto) {
                if (accesoAbierto) {
                    gc.save();
                    gc.translate(301, 238);
                    gc.rotate(-24);
                    gc.setFill(Color.web("#75878a"));
                    gc.fillRoundRect(-43, 0, 43, 105, 7, 7);
                    gc.setStroke(Color.web("#c3d1ce"));
                    gc.setLineWidth(2);
                    gc.strokeRoundRect(-43, 0, 43, 105, 7, 7);
                    gc.restore();
                } else {
                    gc.setFill(Color.web("#afc4c1", 0.17));
                    gc.fillRoundRect(307, 239, 183, 104, 7, 7);
                    gc.setStroke(Color.web("#b5c9c4", 0.62));
                    gc.setLineWidth(2);
                    gc.strokeRoundRect(307, 239, 183, 104, 7, 7);
                    gc.setFill(Color.web("#263a3e", 0.64));
                    gc.fillRoundRect(286, 282, 8, 32, 3, 3);
                }
            }
            dibujarHerramienta(gc, 203, 334, "PINZAS");
            dibujarEtiquetaInstrumento(gc, "HORNO MUFLA · CÁMARA REFRACTARIA", 300, 375);
        }

        private void dibujarReactor(GraphicsContext gc) {
            gc.setFill(Color.web("#56676b"));
            gc.fillRoundRect(326, 205, 224, 178, 15, 15);
            gc.setFill(Color.web("#35484d"));
            double alturaPlatina = transferenciaRequerida && !transferenciaCompletada && accesoAbierto ? 202 : 220;
            gc.fillRoundRect(353, alturaPlatina, 170, 34, 5, 5);
            gc.setFill(Color.web("#17262c"));
            gc.fillRoundRect(377, 249, 124, 108, 8, 8);
            gc.setFill(Color.web("#b8c6c2"));
            gc.fillRoundRect(395, 265, 88, 14, 3, 3);
            gc.fillRoundRect(395, 329, 88, 14, 3, 3);
            gc.setFill(colorProceso());
            gc.fillRoundRect(421, 294, 36, 25, 4, 4);
            gc.setFill(colorProceso().brighter());
            gc.fillOval(428, 298, 22, 17);
            if (perfilVisual.productos().stream().anyMatch(especie -> especie.sustancia().getId().equals("DIAMANTE"))
                    && progresoQuimico() > 0.32) {
                dibujarDiamanteCreciente(gc, 439, 301, progresoQuimico());
            } else if (perfilVisual.productoEnFase(EstadoMateria.SOLIDO) && progresoQuimico() > 0.58) {
                dibujarCristales(gc, 439, 313, progresoQuimico());
            }
            if (perfilVisual.esFenomeno(VisualizadorReaccion.Fenomeno.OXIDACION)
                    && progresoQuimico() > 0.2) {
                dibujarCapaOxido(gc, 421, 294, 36, 24, progresoQuimico());
            }
            gc.setStroke(Color.web("#c4d4cd"));
            gc.setLineWidth(8);
            gc.strokeLine(366, 233, 366, 351);
            gc.strokeLine(512, 233, 512, 351);
            gc.setFill(Color.web("#c8d4d0"));
            gc.fillRoundRect(347, 220, 11, 28, 4, 4);
            gc.setFill(Color.web("#334b50"));
            gc.fillOval(344, 217, 17, 9);
            dibujarManometro(gc, 610, 239, presionVisual());
            dibujarHerramienta(gc, 597, 334, "BOMBA");
            dibujarEtiquetaInstrumento(gc, "PRENSA HIDRÁULICA · CÁPSULA HPHT", 300, 377);
        }

        private void dibujarCamaraCvd(GraphicsContext gc) {
            gc.setFill(Color.web("#273e47"));
            gc.fillRoundRect(268, 221, 330, 159, 18, 18);
            gc.setFill(Color.web("#102229"));
            gc.fillRoundRect(298, 238, 252, 116, 40, 40);
            gc.setFill(Color.web("#afd0d1", 0.2));
            gc.fillRoundRect(303, 242, 242, 107, 34, 34);
            gc.setStroke(Color.web("#a0c2c3", 0.82));
            gc.setLineWidth(4);
            gc.strokeRoundRect(299, 238, 250, 116, 38, 38);
            gc.setFill(perfilVisual.esFenomeno(VisualizadorReaccion.Fenomeno.DEPOSICION)
                    ? Color.web("#d2b66e") : colorProceso());
            gc.fillRoundRect(384, 323, 78, 9, 3, 3);
            gc.setFill(colorProceso().brighter());
            gc.fillRoundRect(397, 310, 52, 8, 3, 3);
            if (perfilVisual.esFenomeno(VisualizadorReaccion.Fenomeno.DEPOSICION)
                    && progresoQuimico() > 0.38) {
                dibujarDiamanteCreciente(gc, 423, 307, progresoQuimico());
            }
            if (perfilVisual.productoEnFase(EstadoMateria.GASEOSO)
                    && progresoQuimico() > 0.34) {
                dibujarGasProducto(gc, 491, 284, progresoQuimico());
            }
            gc.setFill(Color.web("#7e80ff", accionEnCurso == AccionLaboratorio.INICIAR_PLASMA ? 0.24 : 0.08));
            gc.fillOval(347, 254, 150, 90);
            if (accionEnCurso == AccionLaboratorio.INICIAR_PLASMA) {
                double pulso = 0.55 + 0.28 * Math.sin(System.nanoTime() / 90_000_000.0);
                gc.setFill(Color.web("#9d75ff", pulso));
                gc.fillOval(371, 261, 104, 76);
                gc.setFill(Color.web("#eabfff", pulso * 0.86));
                gc.fillOval(393, 272, 61, 54);
                for (int i = 0; i < 9; i++) {
                    double a = i * Math.PI * 2 / 9 + progreso * 5;
                    gc.setFill(Color.web("#d8c7ff", 0.8));
                    gc.fillOval(421 + Math.cos(a) * 42, 299 + Math.sin(a) * 27, 5, 5);
                }
            }
            gc.setFill(Color.web("#bbc9c6"));
            gc.fillRoundRect(543, 282, 8, 29, 3, 3);
            gc.setFill(Color.web("#273b40"));
            gc.fillOval(539, 278, 16, 10);
            if ((transferenciaRequerida || muestraEnPuesto) && accesoAbierto) {
                gc.save();
                gc.translate(548, 249);
                gc.rotate(34);
                gc.setFill(Color.web("#80969a"));
                gc.fillRoundRect(0, 0, 43, 103, 9, 9);
                gc.setStroke(Color.web("#d3e4de"));
                gc.setLineWidth(2);
                gc.strokeRoundRect(0, 0, 43, 103, 9, 9);
                gc.restore();
            }
            dibujarCilindro(gc, 177, 285, "H₂");
            dibujarCilindro(gc, 220, 285, "CH₄");
            dibujarManometro(gc, 603, 247, presionVisual());
            dibujarEtiquetaInstrumento(gc, "CÁMARA DE VACÍO · MICROONDAS", 303, 374);
        }

        private void dibujarBandeja(GraphicsContext gc) {
            gc.setFill(Color.web("#455d60"));
            gc.fillRoundRect(256, 265, 326, 99, 15, 15);
            gc.setFill(Color.web("#abc6bf", 0.35));
            gc.fillRoundRect(274, 278, 290, 64, 10, 10);
            gc.setStroke(Color.web("#d7e9df", 0.8));
            gc.setLineWidth(3);
            gc.strokeRoundRect(273, 277, 292, 65, 9, 9);
            gc.setFill(Color.web("#54bfc4", 0.46));
            gc.fillRoundRect(286, 308, 266, 23, 7, 7);
            gc.setFill(Color.web("#aebbb7"));
            gc.fillOval(389, 291, 44, 15);
            if (perfilVisual.esFenomeno(VisualizadorReaccion.Fenomeno.OXIDACION)) {
                dibujarCapaOxido(gc, 389, 291, 44, 15, progresoQuimico());
            } else {
                gc.setFill(colorProceso());
                gc.fillOval(394, 294, 34, 8);
            }
            gc.setFill(Color.web("#b9c9c5"));
            gc.fillRoundRect(272, 281, 9, 20, 3, 3);
            gc.setFill(Color.web("#354d53"));
            gc.fillOval(269, 277, 15, 9);
            if (transferenciaRequerida && !transferenciaCompletada && accesoAbierto) {
                gc.save();
                gc.translate(274, 278);
                gc.rotate(-16);
                gc.setFill(Color.web("#b9d2d0", 0.35));
                gc.fillRoundRect(0, -55, 290, 61, 12, 12);
                gc.setStroke(Color.web("#d7e9df", 0.8));
                gc.setLineWidth(3);
                gc.strokeRoundRect(0, -55, 290, 61, 12, 12);
                gc.restore();
            }
            dibujarHerramienta(gc, 203, 350, "PINZAS");
            dibujarEtiquetaInstrumento(gc, "CUBETA DE EXPOSICIÓN · SOPORTE DE MUESTRA", 270, 373);
        }

        private void dibujarEquipoGenerico(GraphicsContext gc) {
            gc.setFill(Color.web("#55686b"));
            gc.fillRoundRect(282, 224, 270, 144, 14, 14);
            gc.setFill(Color.web("#15252a"));
            gc.fillRoundRect(301, 244, 232, 103, 7, 7);
            dibujarManometro(gc, 574, 252, presionVisual());
            dibujarEtiquetaInstrumento(gc, "EQUIPO EXPERIMENTAL", 320, 359);
        }

        private double progresoQuimico() {
            String estado = protocolo.estadoAutomataId().toUpperCase(Locale.ROOT);
            double base = switch (estado) {
                case "QF", "COMPLETADO" -> 0.98;
                default -> {
                    if (estado.contains("DEPOSICION") || estado.contains("OBTENIDO")
                            || estado.contains("PRODUCTO")) yield 0.88;
                    if (estado.contains("PLASMA") || estado.contains("REACCION")) yield 0.64;
                    if (estado.contains("MEZCL") || estado.contains("COMPRIM")
                            || estado.contains("AGREG") || estado.contains("CATALIZ")) yield 0.34;
                    if (estado.contains("PREPARADO") || estado.contains("CARGADO")) yield 0.1;
                    if (estado.contains("PREPARACION")) yield 0.02;
                    yield 0.22;
                }
            };
            if (accionEnCurso != null) {
                if (accionEnCurso == AccionLaboratorio.FINALIZAR) return Math.min(1, 0.84 + progreso * 0.16);
                return Math.min(0.96, base + progreso * 0.29);
            }
            if (interaccion != null && interaccion.esCargaDeMateriales()) {
                return Math.min(0.14, base + interaccion.getFraccionCarga() * 0.06);
            }
            return base;
        }

        private Color colorProceso() {
            return perfilVisual.tonoReactivos().interpolate(perfilVisual.tonoProductos(), progresoQuimico());
        }

        private void dibujarLiquido(GraphicsContext gc, double x, double y, double ancho, double alto, Color tono) {
            gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.72));
            gc.fillOval(x, y, ancho, alto);
            if (accionEnCurso == AccionLaboratorio.MEZCLAR) {
                for (int i = 0; i < 5; i++) {
                    double fase = (System.nanoTime() / 100_000_000.0 + i * 0.62) % 1;
                    gc.setFill(Color.web("#c9ffff", 0.75 - fase * 0.42));
                    gc.fillOval(x + 8 + i * 10, y + alto * (1 - fase), 4 + i % 3, 4 + i % 3);
                }
                gc.setStroke(Color.web("#e7ffff", 0.75));
                gc.setLineWidth(2);
                gc.strokeArc(x + 10, y - 3, ancho - 20, 15, progreso * 260, 210, javafx.scene.shape.ArcType.OPEN);
            }
            if (perfilVisual.productoEnFase(EstadoMateria.GASEOSO) && progresoQuimico() > 0.52) {
                for (int i = 0; i < 4; i++) {
                    double fase = (System.nanoTime() / 95_000_000.0 + i * 0.26) % 1;
                    gc.setFill(Color.web("#e2fbff", 0.72 - fase * 0.5));
                    gc.fillOval(x + 10 + i * 12, y - 3 - fase * 24, 4, 4);
                }
            }
        }

        private void dibujarGasProducto(GraphicsContext gc, double x, double y, double avance) {
            Color tono = perfilVisual.tonoProductos();
            for (int i = 0; i < 8; i++) {
                double fase = (System.nanoTime() / 120_000_000.0 + i * 0.19) % 1;
                double desplazamientoX = Math.sin(i * 2.1 + fase * 4) * 20;
                double burbujaY = y - fase * (32 + avance * 35);
                double tamano = 5 + (i % 3) * 2;
                gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.1 + avance * 0.28));
                gc.fillOval(x + desplazamientoX - tamano, burbujaY - tamano, tamano * 2, tamano * 2);
                gc.setStroke(new Color(tono.brighter().getRed(), tono.brighter().getGreen(),
                        tono.brighter().getBlue(), 0.22 + avance * 0.48));
                gc.setLineWidth(1.1);
                gc.strokeOval(x + desplazamientoX - tamano, burbujaY - tamano, tamano * 2, tamano * 2);
            }
        }

        private void dibujarCristales(GraphicsContext gc, double x, double y, double avance) {
            Color tono = perfilVisual.tonoProductos();
            double tamano = 5 + avance * 7;
            for (int i = 0; i < 5; i++) {
                double cx = x - 18 + i * 9;
                double altura = tamano * (0.65 + (i % 3) * 0.22);
                gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.32 + avance * 0.62));
                gc.fillPolygon(new double[]{cx, cx + 4, cx + 2, cx - 3},
                        new double[]{y, y - altura, y + 2, y + 3}, 4);
                gc.setStroke(Color.web("#f5fff4", 0.54));
                gc.setLineWidth(0.8);
                gc.strokeLine(cx, y, cx + 4, y - altura);
            }
        }

        private void dibujarCapaOxido(GraphicsContext gc, double x, double y, double ancho, double alto,
                                      double avance) {
            Color oxido = perfilVisual.tonoProductos();
            String productoId = perfilVisual.productos().isEmpty() ? ""
                    : perfilVisual.productos().get(0).sustancia().getId();
            Color textura = productoId.equals("CuO") ? Color.web("#595852") : Color.web("#d78855");
            if (!productoId.equals("Fe2O3") && !productoId.equals("FeOH3") && !productoId.equals("CuO")) {
                textura = oxido.brighter();
            }
            gc.setFill(new Color(oxido.getRed(), oxido.getGreen(), oxido.getBlue(), 0.18 + avance * 0.72));
            gc.fillRoundRect(x, y, ancho, alto, Math.min(8, alto), Math.min(8, alto));
            for (int i = 0; i < 8; i++) {
                double px = x + (i * 17 % Math.max(1, (int) ancho));
                double py = y + (i * 7 % Math.max(1, (int) alto));
                Color mote = i % 2 == 0 ? textura : textura.darker();
                gc.setFill(new Color(mote.getRed(), mote.getGreen(), mote.getBlue(), 0.25 + avance * 0.58));
                gc.fillOval(px, py, 3 + i % 3, 2 + i % 2);
            }
        }

        private void dibujarDiamanteCreciente(GraphicsContext gc, double x, double y, double avance) {
            double radio = 3 + avance * 7;
            Color cristal = perfilVisual.tonoProductos();
            gc.setFill(new Color(cristal.getRed(), cristal.getGreen(), cristal.getBlue(), 0.25 + avance * 0.7));
            gc.fillPolygon(new double[]{x, x + radio, x + radio * 0.55, x - radio * 0.75},
                    new double[]{y - radio, y - 2, y + radio, y + radio * 0.24}, 4);
            gc.setStroke(Color.web("#f4ffff", 0.8));
            gc.setLineWidth(1.2);
            gc.strokeLine(x, y - radio, x, y + radio * 0.35);
            gc.strokeLine(x, y, x + radio, y - 2);
        }

        private void dibujarMechero(GraphicsContext gc, double x, double y) {
            gc.setFill(Color.web("#809397"));
            gc.fillRoundRect(x - 20, y - 5, 40, 12, 4, 4);
            gc.setFill(Color.web("#4c646b"));
            gc.fillRoundRect(x - 8, y - 39, 16, 36, 4, 4);
            gc.setFill(Color.web("#303f43"));
            gc.fillOval(x - 12, y - 46, 24, 8);
            if (accionEnCurso == AccionLaboratorio.CALENTAR) {
                double llama = 1 + 0.12 * Math.sin(System.nanoTime() / 60_000_000.0);
                gc.setFill(Color.web("#ff592c", 0.45));
                gc.fillOval(x - 18 * llama, y - 72 * llama, 36 * llama, 39 * llama);
                gc.setFill(Color.web("#ffb34d"));
                gc.fillOval(x - 9, y - 60, 18, 27);
                gc.setFill(Color.web("#fff0a5"));
                gc.fillOval(x - 4, y - 53, 8, 17);
            }
            gc.setStroke(Color.web("#d8e1dc"));
            gc.setLineWidth(2);
            gc.strokeLine(x + 22, y - 1, x + 36, y - 16);
            gc.strokeLine(x + 36, y - 16, x + 43, y - 16);
        }

        private void dibujarManometro(GraphicsContext gc, double x, double y, double valor) {
            gc.setFill(Color.web("#d5e0dc"));
            gc.fillOval(x, y, 56, 56);
            gc.setFill(Color.web("#19282d"));
            gc.fillOval(x + 4, y + 4, 48, 48);
            gc.setStroke(Color.web("#a6c1bd"));
            gc.setLineWidth(1);
            for (int i = 0; i <= 6; i++) {
                double angulo = Math.toRadians(140 + i * 260 / 6.0);
                gc.strokeLine(x + 28 + Math.cos(angulo) * 17, y + 28 + Math.sin(angulo) * 17,
                        x + 28 + Math.cos(angulo) * 21, y + 28 + Math.sin(angulo) * 21);
            }
            double factor = Math.min(1, Math.max(0.02, valor / 200.0));
            double aguja = Math.toRadians(140 + factor * 260);
            gc.setStroke(Color.web("#ff765b"));
            gc.setLineWidth(2);
            gc.strokeLine(x + 28, y + 28, x + 28 + Math.cos(aguja) * 19, y + 28 + Math.sin(aguja) * 19);
            gc.setFill(Color.web("#f3f7ee"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 8));
            gc.fillText(String.format(Locale.ROOT, "%.0f", valor), x + 21, y + 47);
        }

        private void dibujarCilindro(GraphicsContext gc, double x, double y, String texto) {
            gc.setFill(Color.web(texto.equals("H₂") ? "#93a9b2" : "#73936f"));
            gc.fillRoundRect(x, y, 31, 87, 10, 10);
            gc.setFill(Color.web("#c6d5d1"));
            gc.fillRoundRect(x + 8, y - 9, 15, 12, 3, 3);
            gc.setFill(Color.web("#263a40"));
            gc.fillRoundRect(x + 4, y + 30, 23, 18, 4, 4);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 8));
            gc.fillText(texto, x + 8, y + 42);
            gc.setStroke(Color.web("#adbfbb"));
            gc.setLineWidth(3);
            gc.strokeLine(x + 15, y - 1, x + 15, y - 14);
            gc.strokeLine(x + 15, y - 14, x + 45, y - 14);
        }

        private void dibujarBotellaReactivo(GraphicsContext gc, double x, double y, String texto) {
            gc.setFill(Color.web("#adc6c2", 0.78));
            gc.fillRoundRect(x, y, 32, 48, 5, 5);
            gc.setFill(Color.web("#d2e0d7"));
            gc.fillRoundRect(x + 10, y - 9, 12, 11, 2, 2);
            gc.setFill(Color.web("#5ca7a1"));
            gc.fillRect(x + 4, y + 23, 24, 13);
            gc.setFill(Color.web("#e3f1e9"));
            gc.setFont(Font.font("Segoe UI", 7));
            gc.fillText(texto, x - 12, y + 61);
        }

        private void dibujarVidrio(GraphicsContext gc, double x, double y) {
            gc.setFill(Color.web("#d0dedb", 0.23));
            gc.fillRoundRect(x, y, 35, 46, 5, 5);
            gc.setStroke(Color.web("#b9d5d2", 0.8));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, 35, 46, 5, 5);
            gc.setFill(Color.web("#d4e2dc"));
            gc.setFont(Font.font("Consolas", 8));
            gc.fillText("50", x + 7, y + 21);
            gc.fillText("mL", x + 7, y + 31);
        }

        private void dibujarHerramienta(GraphicsContext gc, double x, double y, String texto) {
            gc.setStroke(Color.web("#c0cbc5"));
            gc.setLineWidth(5);
            gc.strokeLine(x, y, x + 28, y - 18);
            gc.strokeLine(x + 8, y + 4, x + 37, y - 14);
            gc.setFill(Color.web("#d8e5dd"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 8));
            gc.fillText(texto, x - 4, y + 19);
        }

        private void dibujarPerilla(GraphicsContext gc, double x, double y) {
            gc.setFill(Color.web("#17262c"));
            gc.fillOval(x - 12, y - 12, 24, 24);
            gc.setStroke(Color.web("#d5e1dd"));
            gc.setLineWidth(3);
            gc.strokeLine(x, y, x + 8, y - 5);
        }

        private void dibujarEtiquetaInstrumento(GraphicsContext gc, String texto, double x, double y) {
            gc.setFill(Color.web("#b5c9c3"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 9));
            gc.fillText(texto, x, y);
        }

        private void dibujarMaterialesCargados(GraphicsContext gc) {
            if (interaccion == null) return;
            int cantidad = interaccion.getCantidadColocada();
            for (int i = 0; i < cantidad; i++) {
                double baseX = estacion.tipoEquipo() == TipoEquipo.REACTOR ? 424
                        : estacion.tipoEquipo() == TipoEquipo.CAMARA_SELLADA ? 404
                        : estacion.tipoEquipo() == TipoEquipo.HORNO ? 358 : 365;
                double baseY = estacion.tipoEquipo() == TipoEquipo.REACTOR ? 303
                        : estacion.tipoEquipo() == TipoEquipo.CAMARA_SELLADA ? 316
                        : estacion.tipoEquipo() == TipoEquipo.HORNO ? 310 : 332;
                double x = baseX + (i % 4) * 17;
                double y = baseY - (i / 4) * 10;
                gc.setFill(i % 2 == 0 ? Color.web("#f0cf79") : Color.web("#79d7d0"));
                gc.fillOval(x, y, 13, 7);
            }
            if (interaccion.esCargaDeMateriales() && cantidad > 0) {
                gc.setFill(Color.web("#73e6cb"));
                gc.setFont(Font.font("Consolas", FontWeight.BOLD, 9));
                gc.fillText("MATERIALES EN EL RECIPIENTE  ·  " + cantidad, 340, 352);
            }
        }

        private void dibujarDestinoCarga(GraphicsContext gc) {
            if (interaccion == null || !interaccion.esCargaDeMateriales()) return;
            double[] zona = switch (estacion.tipoEquipo()) {
                case MATRAZ -> new double[]{362, 233, 84, 127};
                case HORNO -> new double[]{311, 237, 180, 107};
                case REACTOR -> new double[]{377, 249, 124, 107};
                case CAMARA_SELLADA -> new double[]{301, 241, 246, 105};
                case BANDEJA -> new double[]{275, 279, 289, 61};
                default -> new double[]{301, 242, 232, 103};
            };
            Color acento = destinoActivo ? Color.web("#baffdb", 0.94) : Color.web("#62dfc7", 0.86);
            gc.setFill(Color.web("#4de0bd", destinoActivo ? 0.18 : 0.07));
            gc.fillRoundRect(zona[0], zona[1], zona[2], zona[3], 16, 16);
            gc.setStroke(acento);
            gc.setLineWidth(destinoActivo ? 4 : 2);
            gc.setLineDashes(9, 6);
            gc.strokeRoundRect(zona[0], zona[1], zona[2], zona[3], 16, 16);
            gc.setLineDashes();
            gc.setFill(Color.web("#caffef"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 9));
            gc.fillText(destinoActivo ? "SUELTA AQUÍ" : "CARGA DE MUESTRA", zona[0] + 8, zona[1] + 15);
        }

        private void dibujarMuestraEnPuesto(GraphicsContext gc) {
            if (!muestraEnPuesto) return;
            Color tono = perfilVisual.tonoReactivos().interpolate(perfilVisual.tonoProductos(),
                    Math.max(0.14, progresoQuimico()));
            gc.setFill(Color.web("#b9fff0", 0.18));
            gc.fillOval(397, 282, 76, 58);
            switch (estacion.tipoEquipo()) {
                case MATRAZ -> {
                    gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.82));
                    gc.fillOval(389, 318, 30, 14);
                    gc.fillOval(416, 321, 19, 11);
                }
                case HORNO -> {
                    gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.96));
                    gc.fillRoundRect(376, 297, 56, 13, 4, 4);
                    gc.setStroke(Color.web("#f2f4de", 0.84));
                    gc.setLineWidth(1.4);
                    gc.strokeRoundRect(376, 297, 56, 13, 4, 4);
                }
                case REACTOR -> {
                    gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.96));
                    gc.fillRoundRect(421, 294, 36, 25, 4, 4);
                    gc.setStroke(Color.web("#fff3c2", 0.8));
                    gc.strokeOval(427, 297, 23, 18);
                }
                case CAMARA_SELLADA -> {
                    gc.setFill(Color.web("#cbdedb"));
                    gc.fillRoundRect(394, 307, 56, 10, 2, 2);
                    gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.94));
                    gc.fillRoundRect(400, 304, 44, 5, 2, 2);
                }
                case BANDEJA -> {
                    gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.94));
                    gc.fillOval(389, 291, 44, 15);
                    gc.setStroke(Color.web("#f7fff1", 0.86));
                    gc.strokeOval(389, 291, 44, 15);
                }
                default -> gc.fillOval(412, 296, 34, 26);
            }
            gc.setFill(Color.web("#d4fff2"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 8));
            gc.fillText("MUESTRA RECIBIDA", 348, 381);
        }

        private void dibujarAccesoTraslado(GraphicsContext gc) {
            if (!transferenciaRequerida || transferenciaCompletada) return;
            double[] puntoAcceso = zonaPestillo();
            gc.setFill(Color.web("#61e8c7", accesoAbierto ? 0.12 : 0.18));
            gc.fillRoundRect(puntoAcceso[0], puntoAcceso[1], puntoAcceso[2], puntoAcceso[3], 10, 10);
            gc.setStroke(Color.web(accesoAbierto ? "#a7ffe4" : "#68e6c7", 0.94));
            gc.setLineWidth(accesoAbierto ? 2.2 : 3);
            gc.setLineDashes(6, 4);
            gc.strokeRoundRect(puntoAcceso[0], puntoAcceso[1], puntoAcceso[2], puntoAcceso[3], 10, 10);
            gc.setLineDashes();

            if (accesoAbierto) {
                double[] zona = zonaLogicaTransferencia();
                gc.setFill(Color.web("#61e8c7", destinoActivo ? 0.21 : 0.08));
                gc.fillRoundRect(zona[0], zona[1], zona[2] - zona[0], zona[3] - zona[1], 16, 16);
                gc.setStroke(Color.web(destinoActivo ? "#d5fff0" : "#5ce4c1", 0.95));
                gc.setLineWidth(destinoActivo ? 4 : 2);
                gc.setLineDashes(8, 5);
                gc.strokeRoundRect(zona[0], zona[1], zona[2] - zona[0], zona[3] - zona[1], 16, 16);
                gc.setLineDashes();
                gc.setFill(Color.web("#dcfff5"));
                gc.setFont(Font.font("Consolas", FontWeight.BOLD, 9));
                gc.fillText(destinoActivo ? "SUELTA LA MUESTRA" : "PUERTO ABIERTO · SUELTA AQUÍ", zona[0] + 8, zona[1] + 16);
            } else {
                gc.setFill(Color.web("#e2fff5"));
                gc.setFont(Font.font("Consolas", FontWeight.BOLD, 8));
                gc.fillText("CLIC PARA ABRIR " + nombreAcceso().toUpperCase(Locale.ROOT),
                        puntoAcceso[0] - 38, puntoAcceso[1] - 7);
            }
        }

        private double[] zonaLogicaTransferencia() {
            return switch (estacion.tipoEquipo()) {
                case MATRAZ -> new double[]{360, 231, 449, 362};
                case HORNO -> new double[]{309, 235, 493, 354};
                case REACTOR -> new double[]{375, 245, 505, 358};
                case CAMARA_SELLADA -> new double[]{298, 235, 550, 354};
                case BANDEJA -> new double[]{270, 274, 568, 345};
                default -> new double[]{301, 242, 533, 347};
            };
        }

        private double[] zonaPestillo() {
            return switch (estacion.tipoEquipo()) {
                case MATRAZ -> new double[]{390, 215, 30, 31};
                case HORNO -> new double[]{278, 270, 34, 52};
                case REACTOR -> new double[]{342, 216, 37, 42};
                case CAMARA_SELLADA -> new double[]{539, 276, 39, 45};
                case BANDEJA -> new double[]{269, 269, 42, 40};
                default -> new double[]{282, 235, 36, 46};
            };
        }

        private String nombreAcceso() {
            return switch (estacion.tipoEquipo()) {
                case MATRAZ -> "tapón del matraz";
                case HORNO -> "puerta del horno";
                case REACTOR -> "seguro de la prensa";
                case CAMARA_SELLADA -> "cierre de la cámara";
                case BANDEJA -> "tapa de la bandeja";
                default -> "acceso del equipo";
            };
        }

        private void dibujarAnimacionCarga(GraphicsContext gc) {
            long restante = animacionCargaHasta - System.nanoTime();
            if (restante <= 0 || materialReciente == null) return;
            double progresoCaida = 1 - restante / 850_000_000.0;
            double destinoY = estacion.tipoEquipo() == TipoEquipo.REACTOR ? 303
                    : estacion.tipoEquipo() == TipoEquipo.CAMARA_SELLADA ? 315
                    : estacion.tipoEquipo() == TipoEquipo.HORNO ? 307 : 331;
            double puntoY = origenCargaY + (destinoY - origenCargaY) * progresoCaida;
            Color color = materialReciente.equals("SUSTRATO_CVD") || materialReciente.equals("SEMILLA_DIAMANTE")
                    ? Color.web("#e3c16e")
                    : materialReciente.equals("H2O") ? Color.web("#64d9ef")
                    : materialReciente.equals("H2") || materialReciente.equals("O2")
                    || materialReciente.equals("N2") || materialReciente.equals("CH4")
                    ? Color.web("#a5dfff") : Color.web("#f0e3b5");
            gc.setFill(Color.web("#b9fff0", 0.25));
            gc.fillOval(origenCargaX - 12, puntoY - 10, 24, 24);
            for (int i = 0; i < 5; i++) {
                double offset = (i - 2) * 7;
                double x = origenCargaX + offset * (1 - progresoCaida * 0.4);
                double y = puntoY - Math.abs(i - 2) * 4;
                gc.setFill(color);
                if (materialReciente.equals("SUSTRATO_CVD") || materialReciente.equals("SEMILLA_DIAMANTE")) {
                    gc.fillRoundRect(x - 11, y - 4, 22, 8, 2, 2);
                } else if (materialReciente.equals("H2O")) {
                    gc.fillOval(x - 3, y - 4, 6, 9);
                } else {
                    gc.fillOval(x - 3, y - 3, 6, 6);
                }
            }
            gc.setFill(Color.web("#e0fff5"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 8));
            gc.fillText("TRANSFIRIENDO " + materialReciente, origenCargaX + 12, puntoY - 13);
        }

        private void dibujarTransformacion(GraphicsContext gc) {
            if (accionEnCurso == null) return;
            double pulso = 0.55 + Math.sin(System.nanoTime() / 95_000_000.0) * 0.2;
            switch (accionEnCurso) {
                case CALENTAR -> {
                    gc.setFill(Color.web("#ff8147", 0.09 + 0.14 * pulso));
                    gc.fillOval(300, 205, 285, 188);
                    if (estacion.tipoEquipo() != TipoEquipo.MATRAZ) {
                        for (int i = 0; i < 7; i++) {
                            gc.setFill(Color.web("#ffb361", 0.7 * pulso));
                            gc.fillOval(338 + i * 25, 295 + (i % 2) * 11, 8, 5);
                        }
                    }
                }
                case ENFRIAR, FINALIZAR -> {
                    gc.setFill(Color.web("#8beaff", 0.12 + pulso * 0.11));
                    gc.fillOval(310, 215, 270, 170);
                    for (int i = 0; i < 8; i++) {
                        double x = 340 + i * 29;
                        double y = 241 + ((i * 13 + System.nanoTime() / 15_000_000) % 85);
                        gc.setStroke(Color.web("#b7f4ff", 0.72));
                        gc.setLineWidth(2);
                        gc.strokeLine(x - 4, y, x + 4, y);
                        gc.strokeLine(x, y - 4, x, y + 4);
                    }
                }
                case MEZCLAR -> {
                    gc.setStroke(Color.web("#88fff0", 0.82));
                    gc.setLineWidth(3);
                    gc.strokeArc(358, 250, 120, 104, progreso * -300, 230, javafx.scene.shape.ArcType.OPEN);
                    gc.strokeArc(365, 258, 104, 90, 180 + progreso * -300, -210, javafx.scene.shape.ArcType.OPEN);
                }
                case COMPRIMIR, DESCOMPRIMIR -> {
                    double desplazamiento = accionEnCurso == AccionLaboratorio.COMPRIMIR ? progreso * 16 : (1 - progreso) * 16;
                    gc.setFill(Color.web("#ffc46e", 0.52));
                    gc.fillRoundRect(389, 282 + desplazamiento, 98, 9, 4, 4);
                    gc.setFill(Color.web("#83d6ef", 0.4));
                    gc.fillOval(595, 230, 74, 74);
                }
                case ACTIVAR_IGNICION -> {
                    gc.setStroke(Color.web("#fff28b", pulso));
                    gc.setLineWidth(5);
                    gc.strokeLine(400, 245, 411, 269);
                    gc.strokeLine(411, 269, 399, 288);
                    gc.strokeLine(422, 244, 414, 265);
                }
                case ESPERAR -> {
                    gc.setStroke(Color.web("#a7e9d9", 0.86));
                    gc.setLineWidth(3);
                    gc.strokeArc(375, 245, 100, 100, 90, progreso * 360, javafx.scene.shape.ArcType.OPEN);
                    gc.setFill(Color.web("#eafff2"));
                    gc.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
                    gc.fillText(String.format(Locale.ROOT, "%02d s", Math.round(progreso * 10)), 410, 299);
                }
                case INICIAR_PLASMA -> {
                    gc.setFill(Color.web("#bf83ff", 0.22 * pulso));
                    gc.fillOval(317, 220, 246, 166);
                }
                default -> { }
            }
        }

        private void dibujarMando(GraphicsContext gc) {
            double x = 642;
            double y = 280;
            gc.setFill(Color.web("#263c43"));
            gc.fillRoundRect(x - 8, y - 54, 190, 130, 12, 12);
            gc.setStroke(Color.web("#789394", 0.74));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x - 8, y - 54, 190, 130, 12, 12);
            gc.setFill(Color.web("#11252b"));
            gc.fillRoundRect(x + 8, y - 38, 158, 33, 5, 5);
            gc.setFill(Color.web("#91f5d5"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 10));
            gc.fillText("CONTROL DE PROCESO", x + 21, y - 17);
            boolean trasladoPendiente = transferenciaRequerida && !transferenciaCompletada;
            boolean activo = trasladoPendiente || accionEnCurso != null
                    || (interaccion != null && !interaccion.permiteAccionarHerramienta());
            gc.setFill(Color.web(activo ? "#769083" : "#37c89f"));
            gc.fillOval(x + 17, y + 8, 34, 34);
            gc.setFill(Color.web("#c4ffeb", activo ? 0.5 : 0.95));
            gc.fillOval(x + 25, y + 16, 18, 18);
            gc.setFill(Color.web("#d2e2dc"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
            gc.fillText(accionSeleccionada == null ? "SELECCIONA PASO" : etiquetaMando(accionSeleccionada), x + 60, y + 24);
            gc.setFill(Color.web("#8aa5a5"));
            gc.setFont(Font.font("Segoe UI", 8));
            String instruccion = trasladoPendiente ? "MUESTRA NO COLOCADA"
                    : activo ? "EQUIPO ACTIVO" : "PULSA EL MANDO PARA INICIAR";
            gc.fillText(instruccion, x + 60, y + 41);
            gc.setStroke(Color.web("#93b0aa"));
            gc.setLineWidth(3);
            gc.strokeLine(x + 145, y + 19, x + 157, y + 19);
            gc.strokeLine(x + 151, y + 13, x + 151, y + 25);
            if (accionSeleccionada != null && accionSeleccionada == AccionLaboratorio.CALENTAR) {
                gc.setStroke(Color.web("#ffc274", 0.86));
                gc.setLineWidth(2);
                gc.strokeArc(x + 130, y - 29, 24, 24, 20, 135, javafx.scene.shape.ArcType.OPEN);
            }
        }

        private void dibujarHerramientaInteractiva(GraphicsContext gc) {
            if (accionSeleccionada == null || interaccion == null
                    || !interaccion.permiteAccionarHerramienta() || accionEnCurso != null) return;
            double[] zona = switch (estacion.tipoEquipo()) {
                case MATRAZ -> switch (accionSeleccionada) {
                    case CALENTAR -> new double[]{380, 300, 49, 61};
                    case MEZCLAR -> new double[]{304, 289, 217, 61};
                    case ACTIVAR_IGNICION -> new double[]{362, 222, 90, 77};
                    case ENFRIAR, FINALIZAR -> new double[]{359, 233, 94, 130};
                    default -> null;
                };
                case HORNO -> switch (accionSeleccionada) {
                    case CALENTAR, ENFRIAR, ESPERAR -> new double[]{508, 220, 61, 94};
                    case FINALIZAR -> new double[]{293, 222, 217, 139};
                    default -> null;
                };
                case REACTOR -> switch (accionSeleccionada) {
                    case COMPRIMIR -> new double[]{584, 321, 70, 61};
                    case DESCOMPRIMIR -> new double[]{600, 226, 74, 82};
                    case CALENTAR, ENFRIAR, FINALIZAR -> new double[]{372, 242, 138, 120};
                    default -> null;
                };
                case CAMARA_SELLADA -> switch (accionSeleccionada) {
                    case INICIAR_PLASMA -> new double[]{292, 232, 265, 129};
                    case CALENTAR -> new double[]{377, 296, 96, 53};
                    case DESCOMPRIMIR -> new double[]{590, 233, 78, 78};
                    case FINALIZAR -> new double[]{292, 232, 264, 128};
                    default -> null;
                };
                case BANDEJA -> new double[]{248, 256, 342, 118};
                default -> null;
            };
            if (zona == null) return;
            double pulso = 0.56 + 0.34 * Math.sin(System.nanoTime() / 180_000_000.0);
            gc.setFill(Color.web("#6ce9c6", 0.06 + pulso * 0.06));
            gc.fillRoundRect(zona[0], zona[1], zona[2], zona[3], 15, 15);
            gc.setStroke(Color.web("#9affd8", 0.58 + pulso * 0.34));
            gc.setLineWidth(3);
            gc.setLineDashes(7, 5);
            gc.strokeRoundRect(zona[0], zona[1], zona[2], zona[3], 15, 15);
            gc.setLineDashes();
            gc.setFill(Color.web("#c7ffe9"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 8));
            gc.fillText("CLIC  ·  " + etiquetaMando(accionSeleccionada), zona[0] + 5, zona[1] - 4);
        }

        private double temperaturaVisual() {
            return protocolo.temperaturaActual()
                    + (protocolo.temperaturaObjetivo() - protocolo.temperaturaActual())
                    * (accionEnCurso == AccionLaboratorio.CALENTAR ? progreso : 0);
        }

        private double presionVisual() {
            return protocolo.presionActual()
                    + (protocolo.presionObjetivo() - protocolo.presionActual())
                    * (accionEnCurso == AccionLaboratorio.COMPRIMIR || accionEnCurso == AccionLaboratorio.DESCOMPRIMIR
                    ? progreso : 0);
        }

        private String etiquetaMando(AccionLaboratorio accion) {
            return switch (accion) {
                case CALENTAR -> estacion.tipoEquipo() == TipoEquipo.MATRAZ ? "VÁLVULA DE GAS" : "REGULADOR TÉRMICO";
                case ENFRIAR, FINALIZAR -> "CIRCUITO DE ENFRIAMIENTO";
                case MEZCLAR -> "AGITADOR MAGNÉTICO";
                case INICIAR_PLASMA -> "GENERADOR DE MICROONDAS";
                case COMPRIMIR -> "BOMBA HIDRÁULICA";
                case DESCOMPRIMIR -> "VÁLVULA DE DESCARGA";
                case ACTIVAR_IGNICION -> "CHISPERO";
                case ESPERAR -> "TEMPORIZADOR";
                default -> nombreAccion(accion).toUpperCase(Locale.ROOT);
            };
        }
    }
}
