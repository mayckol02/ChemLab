package com.chemlab.ui.workbench;

import com.chemlab.ui.components.IconoSustancia;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.model.value.Cantidad;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** Controles físicos para preparar y ejecutar un paso del autómata. */
public final class PanelInteraccionProcedimiento extends VBox {

    private final Experimento experimento;
    private final Reaccion reaccion;
    private final TipoEquipo tipoEquipo;
    private final AccionLaboratorio accion;
    private final Consumer<AccionLaboratorio> alIniciarOperacion;
    private final Consumer<AccionLaboratorio> alConfirmarCarga;
    private final boolean interaccionSobreMesa;
    private final Set<String> sustanciasDisponibles;
    private final Consumer<String> alCargarSustancia;
    private final Map<String, Sustancia> catalogoSustancias;
    private final Map<Sustancia, Cantidad> materialesEnProceso;
    private final Set<String> elementosRequeridos;
    private final boolean cargaParcialPermitida;
    private final Set<String> elementosColocados = new LinkedHashSet<>();
    private final Label estadoCarga = new Label();
    private final Label etiquetaProgreso = new Label();
    private final ProgressBar barraProgreso = new ProgressBar(0);
    private Button botonConfirmar;
    private Button botonIniciar;
    private FlowPane fichasColocadas;

    public PanelInteraccionProcedimiento(
            Reaccion reaccion,
            Experimento experimento,
            TipoEquipo tipoEquipo,
            AccionLaboratorio accion,
            Consumer<AccionLaboratorio> alIniciarOperacion,
            Consumer<AccionLaboratorio> alConfirmarCarga
    ) {
        this(reaccion, experimento, tipoEquipo, accion, alIniciarOperacion, alConfirmarCarga, false);
    }

    public PanelInteraccionProcedimiento(
            Reaccion reaccion,
            Experimento experimento,
            TipoEquipo tipoEquipo,
            AccionLaboratorio accion,
            Consumer<AccionLaboratorio> alIniciarOperacion,
            Consumer<AccionLaboratorio> alConfirmarCarga,
            boolean interaccionSobreMesa
    ) {
        this(reaccion, experimento, tipoEquipo, accion, alIniciarOperacion, alConfirmarCarga,
                interaccionSobreMesa, null, id -> {}, Map.of());
    }

    public PanelInteraccionProcedimiento(
            Reaccion reaccion,
            Experimento experimento,
            TipoEquipo tipoEquipo,
            AccionLaboratorio accion,
            Consumer<AccionLaboratorio> alIniciarOperacion,
            Consumer<AccionLaboratorio> alConfirmarCarga,
            boolean interaccionSobreMesa,
            Set<String> sustanciasDisponibles,
            Consumer<String> alCargarSustancia
    ) {
        this(reaccion, experimento, tipoEquipo, accion, alIniciarOperacion, alConfirmarCarga,
                interaccionSobreMesa, sustanciasDisponibles, alCargarSustancia, Map.of());
    }

    public PanelInteraccionProcedimiento(
            Reaccion reaccion,
            Experimento experimento,
            TipoEquipo tipoEquipo,
            AccionLaboratorio accion,
            Consumer<AccionLaboratorio> alIniciarOperacion,
            Consumer<AccionLaboratorio> alConfirmarCarga,
            boolean interaccionSobreMesa,
            Set<String> sustanciasDisponibles,
            Consumer<String> alCargarSustancia,
            Map<String, Sustancia> catalogoSustancias
    ) {
        this(reaccion, experimento, tipoEquipo, accion, alIniciarOperacion, alConfirmarCarga,
                interaccionSobreMesa, sustanciasDisponibles, alCargarSustancia, catalogoSustancias, false);
    }

    public PanelInteraccionProcedimiento(
            Reaccion reaccion,
            Experimento experimento,
            TipoEquipo tipoEquipo,
            AccionLaboratorio accion,
            Consumer<AccionLaboratorio> alIniciarOperacion,
            Consumer<AccionLaboratorio> alConfirmarCarga,
            boolean interaccionSobreMesa,
            Set<String> sustanciasDisponibles,
            Consumer<String> alCargarSustancia,
            Map<String, Sustancia> catalogoSustancias,
            boolean cargaParcialPermitida
    ) {
        this(reaccion, experimento, tipoEquipo, accion, alIniciarOperacion, alConfirmarCarga,
                interaccionSobreMesa, sustanciasDisponibles, alCargarSustancia,
                catalogoSustancias, cargaParcialPermitida,
                experimento.getInventario().obtenerCantidades());
    }

    public PanelInteraccionProcedimiento(
            Reaccion reaccion,
            Experimento experimento,
            TipoEquipo tipoEquipo,
            AccionLaboratorio accion,
            Consumer<AccionLaboratorio> alIniciarOperacion,
            Consumer<AccionLaboratorio> alConfirmarCarga,
            boolean interaccionSobreMesa,
            Set<String> sustanciasDisponibles,
            Consumer<String> alCargarSustancia,
            Map<String, Sustancia> catalogoSustancias,
            boolean cargaParcialPermitida,
            Map<Sustancia, Cantidad> materialesEnProceso
    ) {
        this.reaccion = reaccion;
        this.experimento = experimento;
        this.tipoEquipo = tipoEquipo;
        this.accion = accion;
        this.alIniciarOperacion = alIniciarOperacion;
        this.alConfirmarCarga = alConfirmarCarga;
        this.interaccionSobreMesa = interaccionSobreMesa;
        this.sustanciasDisponibles = sustanciasDisponibles == null
                ? null : Set.copyOf(sustanciasDisponibles);
        this.alCargarSustancia = alCargarSustancia == null ? id -> {} : alCargarSustancia;
        this.catalogoSustancias = Map.copyOf(catalogoSustancias);
        this.materialesEnProceso = Map.copyOf(materialesEnProceso);
        this.elementosRequeridos = calcularElementosRequeridos();
        this.cargaParcialPermitida = cargaParcialPermitida;

        setSpacing(8);
        setPadding(new Insets(10, 12, 10, 12));
        getStyleClass().add("game-procedure-panel");

        Label destino = new Label(textoDestino(accion));
        destino.getStyleClass().add("game-section-title");
        getChildren().addAll(destino, construirInventarioCompartido());

        if (requiereColocarElementos()) {
            construirInteraccionDeCarga();
        } else {
            construirInteraccionDeEquipo();
        }
    }

    private VBox construirInventarioCompartido() {
        VBox inventario = new VBox(5);
        inventario.getStyleClass().add("game-shared-inventory");
        Label titulo = new Label("MATERIALES DISPONIBLES EN EL EXPERIMENTO");
        titulo.getStyleClass().add("game-section-title");
        inventario.getChildren().add(titulo);
        if (materialesEnProceso.isEmpty()) {
            Label vacio = new Label("No hay materiales cargados en el inventario compartido.");
            vacio.getStyleClass().add("game-menu-subtitle");
            inventario.getChildren().add(vacio);
            return inventario;
        }

        FlowPane materiales = new FlowPane(8, 6);
        materiales.setAlignment(Pos.CENTER_LEFT);
        materiales.getStyleClass().add("game-shared-inventory-items");
        materialesEnProceso.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(java.util.Comparator.comparing(Sustancia::getId)))
                .forEach(entrada -> {
                    Sustancia sustancia = entrada.getKey();
                    Cantidad cantidad = entrada.getValue();
                    HBox ficha = new HBox(5,
                            new IconoSustancia(sustancia, 22),
                            new Label(sustancia.getFormula() + " · "
                                    + String.format(java.util.Locale.ROOT, "%.3f %s",
                                    cantidad.getValor(), cantidad.getUnidad())));
                    ficha.setAlignment(Pos.CENTER_LEFT);
                    ficha.getStyleClass().add("game-item-placed");
                    materiales.getChildren().add(ficha);
                });
        inventario.getChildren().add(materiales);
        return inventario;
    }

    private Set<String> calcularElementosRequeridos() {
        Set<String> ids = new LinkedHashSet<>();
        if (accion == AccionLaboratorio.AGREGAR_CATALIZADOR) {
            if (!reaccion.getCondicionesRequeridas().getCatalizadoresRequeridos().isEmpty()) {
                ids.addAll(reaccion.getCondicionesRequeridas().getCatalizadoresRequeridos());
            } else if ("R_DIAMANTE_HPHT".equals(reaccion.getId())) {
                ids.add("Fe");
                ids.add("Ni");
            } else {
                Set<Sustancia> reactivos = reaccion.getReactivos().keySet();
                Set<Sustancia> productos = reaccion.getProductos().keySet();
                experimento.getInventario().obtenerCantidades().keySet().stream()
                        .filter(sustancia -> !reactivos.contains(sustancia) && !productos.contains(sustancia))
                        .forEach(sustancia -> ids.add(sustancia.getId()));
            }
            if (sustanciasDisponibles != null) {
                ids.retainAll(sustanciasDisponibles);
            }
            return ids;
        }
        if (sustanciasDisponibles != null
                && (accion == AccionLaboratorio.CARGAR_REACTIVO
                || accion == AccionLaboratorio.AGREGAR_AGUA)) {
            if (accion == AccionLaboratorio.AGREGAR_AGUA) {
                if (sustanciasDisponibles.contains("H2O")) ids.add("H2O");
                return ids;
            }
            ids.addAll(sustanciasDisponibles);
            return ids;
        }
        if (accion == AccionLaboratorio.CARGAR_REACTIVO) {
            reaccion.getReactivos().keySet().forEach(sustancia -> ids.add(sustancia.getId()));
            if ("R_DIAMANTE_CVD".equals(reaccion.getId())
                    && experimento.getInventario().obtenerCantidades().keySet().stream()
                    .anyMatch(sustancia -> "H2".equals(sustancia.getId()))) {
                ids.add("H2");
            }
            if ("R_DIAMANTE_CVD".equals(reaccion.getId())) ids.add("SUSTRATO_CVD");
            if ("R_DIAMANTE_HPHT".equals(reaccion.getId())) ids.add("SEMILLA_DIAMANTE");
        } else if (accion == AccionLaboratorio.AGREGAR_AGUA) {
            experimento.getInventario().obtenerCantidades().keySet().stream()
                    .filter(sustancia -> "H2O".equals(sustancia.getId()))
                    .forEach(sustancia -> ids.add(sustancia.getId()));
        }
        return ids;
    }

    private boolean requiereColocarElementos() {
        return accion == AccionLaboratorio.CARGAR_REACTIVO
                || accion == AccionLaboratorio.AGREGAR_CATALIZADOR
                || accion == AccionLaboratorio.AGREGAR_AGUA;
    }

    private void construirInteraccionDeCarga() {
        FlowPane fichas = new FlowPane(8, 8);
        fichas.setAlignment(Pos.CENTER_LEFT);
        for (String id : elementosRequeridos) {
            Sustancia sustancia = buscarSustancia(id);
            Button ficha = new Button();
            ficha.getStyleClass().add("game-item-chip");
            ficha.setCursor(Cursor.DEFAULT);
            HBox contenidoFicha = new HBox(7);
            contenidoFicha.setAlignment(Pos.CENTER_LEFT);
            if (sustancia != null) {
                contenidoFicha.getChildren().add(new IconoSustancia(sustancia, 32));
            } else {
                Label objeto = new Label("◈");
                objeto.setStyle("-fx-text-fill: #8ce5d4; -fx-font-size: 19px;");
                contenidoFicha.getChildren().add(objeto);
            }
            contenidoFicha.getChildren().add(new Label(textoElemento(id, sustancia)));
            ficha.setGraphic(contenidoFicha);
            ficha.setAccessibleText(textoElemento(id, sustancia));
            ficha.setOnDragDetected(evento -> {
                if (elementosColocados.contains(id)) return;
                Dragboard dragboard = ficha.startDragAndDrop(TransferMode.COPY);
                ClipboardContent contenido = new ClipboardContent();
                contenido.putString(id);
                dragboard.setContent(contenido);
                if (getScene() != null) getScene().setCursor(Cursor.DEFAULT);
                evento.consume();
            });
            ficha.setOnDragDone(evento -> {
                if (getScene() != null) getScene().setCursor(Cursor.DEFAULT);
                evento.consume();
            });
            fichas.getChildren().add(ficha);
        }

        VBox zonaDestino = new VBox(6);
        zonaDestino.setAlignment(Pos.CENTER);
        zonaDestino.setMinHeight(64);
        zonaDestino.setMaxWidth(Double.MAX_VALUE);
        zonaDestino.setPickOnBounds(true);
        zonaDestino.getStyleClass().add("game-drop-zone");
        Label tituloZona = new Label(textoZonaDeCarga());
        tituloZona.getStyleClass().add("game-menu-subtitle");
        fichasColocadas = new FlowPane(6, 6);
        fichasColocadas.setAlignment(Pos.CENTER);
        estadoCarga.getStyleClass().add("game-menu-subtitle");
        if (!interaccionSobreMesa) {
            zonaDestino.getChildren().addAll(tituloZona, fichasColocadas, estadoCarga);
        }

        zonaDestino.setOnDragOver(evento -> {
            Dragboard dragboard = evento.getDragboard();
            if (dragboard.hasString()
                    && elementosRequeridos.contains(dragboard.getString())
                    && !elementosColocados.contains(dragboard.getString())) {
                evento.acceptTransferModes(TransferMode.COPY);
            }
            evento.consume();
        });
        zonaDestino.setOnDragEntered(evento -> {
            Dragboard dragboard = evento.getDragboard();
            if (dragboard.hasString() && elementosRequeridos.contains(dragboard.getString())) {
                if (!zonaDestino.getStyleClass().contains("game-drop-zone-active")) {
                    zonaDestino.getStyleClass().add("game-drop-zone-active");
                }
            }
        });
        zonaDestino.setOnDragExited(evento -> {
            zonaDestino.getStyleClass().remove("game-drop-zone-active");
        });
        zonaDestino.setOnDragDropped(evento -> {
            Dragboard dragboard = evento.getDragboard();
            boolean correcto = false;
            if (dragboard.hasString()) {
                String id = dragboard.getString();
                if (elementosRequeridos.contains(id)) {
                    colocarElemento(id);
                    correcto = true;
                } else {
                    estadoCarga.setText("Ese material no corresponde a este paso.");
                }
            }
            evento.setDropCompleted(correcto);
            evento.consume();
        });

        botonConfirmar = new Button(textoConfirmacion(accion));
        botonConfirmar.getStyleClass().add("game-start-button");
        botonConfirmar.setCursor(Cursor.DEFAULT);
        botonConfirmar.setDisable(!puedeConfirmarCarga());
        botonConfirmar.setOnAction(evento -> confirmarCarga());

        if (elementosRequeridos.isEmpty()) {
            estadoCarga.setText("No hay materiales disponibles para este paso; no se puede continuar.");
            botonConfirmar.setDisable(true);
        } else {
            estadoCarga.setText("Colocados: 0 de " + elementosRequeridos.size());
        }

        getChildren().addAll(new Label("Suministros · arrástralos hasta el recipiente del puesto:"), fichas);
        if (interaccionSobreMesa) {
            getChildren().addAll(fichasColocadas, estadoCarga, botonConfirmar);
        } else {
            getChildren().addAll(zonaDestino, botonConfirmar);
        }
    }

    private void colocarElemento(String id) {
        if (!elementosColocados.add(id)) return;
        Sustancia sustancia = buscarSustancia(id);
        if (sustancia != null) {
            HBox ficha = new HBox(5, new IconoSustancia(sustancia, 24),
                    new Label("✓ " + sustancia.getFormula()));
            ficha.setAlignment(Pos.CENTER_LEFT);
            ficha.getStyleClass().add("game-item-placed");
            fichasColocadas.getChildren().add(ficha);
        } else {
            Label ficha = new Label("✓ " + textoElemento(id, null));
            ficha.getStyleClass().add("game-item-placed");
            fichasColocadas.getChildren().add(ficha);
        }
        if (sustanciasDisponibles != null
                && (accion == AccionLaboratorio.CARGAR_REACTIVO
                || accion == AccionLaboratorio.AGREGAR_CATALIZADOR
                || accion == AccionLaboratorio.AGREGAR_AGUA)) {
            alCargarSustancia.accept(id);
        }
        estadoCarga.setText("Colocados: " + elementosColocados.size()
                + " de " + elementosRequeridos.size());
        botonConfirmar.setDisable(!puedeConfirmarCarga());
    }

    public boolean aceptaCargaDesdeMesa(String id) {
        return interaccionSobreMesa && requiereColocarElementos()
                && elementosRequeridos.contains(id) && !elementosColocados.contains(id);
    }

    public boolean colocarElementoDesdeMesa(String id) {
        if (!aceptaCargaDesdeMesa(id)) {
            if (estadoCarga != null) estadoCarga.setText("Ese material no corresponde a este recipiente.");
            return false;
        }
        colocarElemento(id);
        return true;
    }

    public boolean puedeConfirmarCarga() {
        if (!requiereColocarElementos()) return false;
        return cargaCompleta(cargaParcialPermitida, accion, elementosColocados, elementosRequeridos);
    }

    static boolean cargaCompleta(
            boolean permitirParcial,
            AccionLaboratorio accion,
            Set<String> colocados,
            Set<String> requeridos
    ) {
        if (requeridos.isEmpty()) return false;
        if (permitirParcial && accion == AccionLaboratorio.CARGAR_REACTIVO) {
            return !colocados.isEmpty();
        }
        return !colocados.isEmpty() && colocados.containsAll(requeridos);
    }

    private void confirmarCarga() {
        if (puedeConfirmarCarga()) {
            alConfirmarCarga.accept(accion);
        } else {
            estadoCarga.setText(cargaParcialPermitida && accion == AccionLaboratorio.CARGAR_REACTIVO
                    ? "Coloca al menos un reactivo para confirmar la carga."
                    : "Carga incompleta: coloca todos los materiales antes de continuar.");
        }
    }

    public double getFraccionCarga() {
        return elementosRequeridos.isEmpty() ? 0
                : (double) elementosColocados.size() / elementosRequeridos.size();
    }

    public boolean permiteAccionarHerramienta() {
        return interaccionSobreMesa && !requiereColocarElementos()
                && botonIniciar != null && !botonIniciar.isDisabled();
    }

    public void accionarHerramienta() {
        if (!permiteAccionarHerramienta()) return;
        botonIniciar.setDisable(true);
        etiquetaProgreso.setText("Preparando el equipo...");
        barraProgreso.setProgress(0);
        barraProgreso.setVisible(true);
        etiquetaProgreso.setVisible(true);
        alIniciarOperacion.accept(accion);
    }

    private void construirInteraccionDeEquipo() {
        Label instruccion = new Label(textoOperacion(accion));
        instruccion.setWrapText(true);
        instruccion.getStyleClass().add("game-menu-subtitle");
        botonIniciar = new Button(textoBoton(accion));
        botonIniciar.getStyleClass().add("game-action-button");
        botonIniciar.setCursor(Cursor.DEFAULT);
        botonIniciar.setOnAction(evento -> accionarHerramientaInterna());
        barraProgreso.setMaxWidth(Double.MAX_VALUE);
        barraProgreso.setMinHeight(11);
        barraProgreso.setPrefHeight(11);
        barraProgreso.setMaxHeight(11);
        barraProgreso.setVisible(false);
        barraProgreso.setManaged(true);
        etiquetaProgreso.setText(" ");
        etiquetaProgreso.setWrapText(true);
        etiquetaProgreso.setMinHeight(26);
        etiquetaProgreso.setPrefHeight(26);
        etiquetaProgreso.setMaxHeight(26);
        etiquetaProgreso.setVisible(false);
        etiquetaProgreso.setManaged(true);
        etiquetaProgreso.getStyleClass().add("game-menu-subtitle");
        getChildren().add(instruccion);
        if (interaccionSobreMesa) {
            Label ayuda = new Label("Haz clic sobre el mando físico del aparato para iniciar esta operación.");
            ayuda.setWrapText(true);
            ayuda.getStyleClass().add("game-menu-subtitle");
            getChildren().add(ayuda);
        } else {
            getChildren().add(botonIniciar);
        }
        getChildren().addAll(barraProgreso, etiquetaProgreso);
    }

    private void accionarHerramientaInterna() {
        if (botonIniciar == null || botonIniciar.isDisabled()) return;
        botonIniciar.setDisable(true);
        etiquetaProgreso.setText("Preparando el equipo...");
        barraProgreso.setProgress(0);
        barraProgreso.setVisible(true);
        etiquetaProgreso.setVisible(true);
        alIniciarOperacion.accept(accion);
    }

    public void actualizarProgreso(double progreso, String descripcion) {
        if (barraProgreso != null) {
            barraProgreso.setProgress(Math.max(0, Math.min(1, progreso)));
        }
        if (etiquetaProgreso != null) {
            etiquetaProgreso.setText(descripcion);
        }
    }

    public int getCantidadColocada() {
        return elementosColocados.size();
    }

    public boolean esCargaDeMateriales() {
        return requiereColocarElementos();
    }

    private Sustancia buscarSustancia(String id) {
        Sustancia delInventario = experimento.getInventario().obtenerCantidades().keySet().stream()
                .filter(sustancia -> sustancia.getId().equals(id))
                .findFirst()
                .orElse(null);
        return delInventario != null ? delInventario : catalogoSustancias.get(id);
    }

    private String textoElemento(String id, Sustancia sustancia) {
        if (sustancia != null) return sustancia.getFormula() + " · " + sustancia.getNombre();
        return switch (id) {
            case "SUSTRATO_CVD" -> "Sustrato de silicio";
            case "SEMILLA_DIAMANTE" -> "Semilla de diamante";
            default -> id;
        };
    }

    private String textoDestino(AccionLaboratorio accion) {
        String destino = nombreHerramienta();
        return switch (accion) {
            case MONTAR_EQUIPO -> reaccion.getId().equals("R_SEPARACION_NACL")
                    ? "MONTAJE · " + destino + " · instala el sistema de evaporación"
                    : "MONTAJE · " + destino + " · configura el equipo";
            case SELECCIONAR_METODO -> reaccion.getId().equals("R_SEPARACION_NACL")
                    ? "MÉTODO · " + destino + " · selecciona evaporación"
                    : "MÉTODO · " + destino;
            case CARGAR_REACTIVO -> "PREPARACIÓN · " + destino + " · carga los reactivos";
            case AGREGAR_CATALIZADOR -> "CATALIZADOR · " + destino;
            case AGREGAR_AGUA -> "DISOLUCIÓN · " + destino;
            default -> "OPERACIÓN · " + destino;
        };
    }

    private String textoZonaDeCarga() {
        String equipo = nombreHerramienta();
        return switch (accion) {
            case CARGAR_REACTIVO -> "Arrastra los reactivos dentro del " + equipo;
            case AGREGAR_CATALIZADOR -> "Monta el catalizador en el soporte del " + equipo;
            case AGREGAR_AGUA -> "Vierte el agua en el " + equipo;
            default -> "Coloca los elementos en el equipo indicado";
        };
    }

    private String textoConfirmacion(AccionLaboratorio accion) {
        return switch (accion) {
            case CARGAR_REACTIVO -> "Confirmar carga en el equipo";
            case AGREGAR_CATALIZADOR -> "Confirmar adición del catalizador";
            case AGREGAR_AGUA -> "Confirmar adición de agua";
            default -> "Confirmar operación";
        };
    }

    private String textoOperacion(AccionLaboratorio accion) {
        return switch (accion) {
            case MONTAR_EQUIPO -> reaccion.getId().equals("R_SEPARACION_NACL")
                    ? "Instala el matraz de evaporación, conecta el condensador y coloca el recipiente colector."
                    : "Monta y configura el equipo antes de iniciar el proceso.";
            case CALENTAR -> "La fuente térmica del " + nombreHerramienta()
                    + " elevará la temperatura hasta el valor del protocolo.";
            case ENFRIAR -> "El circuito de enfriamiento del " + nombreHerramienta()
                    + " bajará la temperatura de forma controlada.";
            case MEZCLAR -> "El agitador del " + nombreHerramienta()
                    + " homogeneizará la mezcla.";
            case INICIAR_PLASMA -> "El generador de microondas de la " + nombreHerramienta()
                    + " ionizará la mezcla gaseosa.";
            case COMPRIMIR -> "La prensa del " + nombreHerramienta()
                    + " elevará la presión de forma gradual.";
            case DESCOMPRIMIR -> "La válvula del " + nombreHerramienta()
                    + " liberará presión de forma controlada.";
            case ACTIVAR_IGNICION -> "El chispero del " + nombreHerramienta()
                    + " iniciará la reacción.";
            case ESPERAR -> "Mantén el " + nombreHerramienta()
                    + " en operación durante el tiempo de residencia.";
            case FINALIZAR -> "Detén el " + nombreHerramienta()
                    + ", estabiliza el producto y retira la muestra.";
            case SELECCIONAR_METODO -> reaccion.getId().equals("R_SEPARACION_NACL")
                    ? "Selecciona evaporación para retirar el agua de la solución y recuperarla en el condensador."
                    : "Selecciona y prepara la ruta experimental.";
            default -> "Prepara el equipo según la guía y realiza la operación.";
        };
    }

    private String textoBoton(AccionLaboratorio accion) {
        return switch (accion) {
            case MONTAR_EQUIPO -> reaccion.getId().equals("R_SEPARACION_NACL")
                    ? "Montar sistema de evaporación"
                    : "Montar equipo";
            case CALENTAR -> switch (tipoEquipo) {
                case MATRAZ -> "Encender mechero Bunsen";
                case HORNO -> "Encender horno mufla";
                case REACTOR -> "Activar calentamiento de prensa";
                case CAMARA_SELLADA -> "Activar calefactor de cámara";
                default -> "Encender fuente de calor";
            };
            case ENFRIAR -> "Encender circuito de enfriamiento";
            case MEZCLAR -> "Encender agitador magnético";
            case INICIAR_PLASMA -> "Activar generador de plasma";
            case COMPRIMIR -> "Bombear prensa hidráulica";
            case DESCOMPRIMIR -> "Abrir válvula de descarga";
            case ACTIVAR_IGNICION -> "Pulsar chispero";
            case ESPERAR -> "Iniciar temporizador";
            case FINALIZAR -> "Detener equipo y extraer muestra";
            case SELECCIONAR_METODO -> reaccion.getId().equals("R_SEPARACION_NACL")
                    ? "Seleccionar evaporación"
                    : "Confirmar método";
            default -> "Iniciar operación";
        };
    }

    private String nombreHerramienta() {
        return switch (tipoEquipo) {
            case MATRAZ -> reaccion.getId().equals("R_SEPARACION_NACL")
                    ? "sistema de evaporación"
                    : "matraz con mechero Bunsen";
            case HORNO -> "horno mufla";
            case REACTOR -> "reactor de alta presión";
            case CAMARA_SELLADA -> "cámara de vacío CVD";
            case BANDEJA -> "bandeja de exposición";
            default -> tipoEquipo.name().replace('_', ' ').toLowerCase();
        };
    }
}
