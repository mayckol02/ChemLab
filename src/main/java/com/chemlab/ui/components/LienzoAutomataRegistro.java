package com.chemlab.ui.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EstadoQuimico;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.game.ProgresoAutomata;
import com.chemlab.model.entity.RegistroExperimento;
import com.chemlab.model.enums.AccionLaboratorio;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/** Representación gráfica del autómata vinculado a un experimento guardado. */
public final class LienzoAutomataRegistro extends Canvas {

    private static final double ANCHO = 1000;
    private static final double RADIO = 38;
    private static final int ESTADOS_POR_FILA = 5;

    private final RegistroExperimento registro;
    private final List<Nodo> nodos;
    private final List<Arista> aristas;

    public LienzoAutomataRegistro(RegistroExperimento registro) {
        this(registro, crearAutomata(registro));
    }

    public LienzoAutomataRegistro(RegistroExperimento registro, AutomataQuimico automata) {
        this.registro = java.util.Objects.requireNonNull(registro, "El registro es obligatorio");
        java.util.Objects.requireNonNull(automata, "El autómata es obligatorio");
        nodos = construirNodos(automata);
        aristas = construirAristas(automata);
        int filas = Math.max(1, (int) Math.ceil((double) nodos.size() / ESTADOS_POR_FILA));
        setWidth(ANCHO);
        setHeight(145 + filas * 165);
        dibujar(automata);
    }

    private static AutomataQuimico crearAutomata(RegistroExperimento registro) {
        java.util.Objects.requireNonNull(registro, "El registro es obligatorio");
        return registro.getIdProceso() == null
                ? FabricaAutomata.crearAutomataEstandar()
                : FabricaAutomata.crearAutomataParaReaccion(registro.getIdProceso());
    }

    private List<Nodo> construirNodos(AutomataQuimico automata) {
        List<EstadoQuimico> estados = new ArrayList<>(automata.getEstados());
        Map<String, EstadoQuimico> porNombre = new LinkedHashMap<>();
        Map<String, EstadoQuimico> porId = new LinkedHashMap<>();
        estados.forEach(estado -> {
            porNombre.put(estado.getNombre(), estado);
            porId.put(estado.getId(), estado);
        });
        LinkedHashMap<String, EstadoQuimico> ordenados = new LinkedHashMap<>();
        agregar(ordenados, buscar(registro.getEstadoInicial(), porId, porNombre));
        for (RegistroExperimento.PasoTransicion paso : registro.getPasosTransicion()) {
            agregar(ordenados, buscar(paso.estadoOrigen(), porId, porNombre));
            agregar(ordenados, buscar(paso.estadoDestino(), porId, porNombre));
        }
        registro.getTodosLosEstados().forEach(nombre -> agregar(ordenados, buscar(nombre, porId, porNombre)));
        estados.stream().sorted(Comparator.comparing(EstadoQuimico::getId))
                .forEach(estado -> agregar(ordenados, estado));

        Set<String> finales = automata.getEstadosFinales().stream()
                .map(EstadoQuimico::getId).collect(Collectors.toSet());
        if (finales.isEmpty()) {
            registro.getEstadosFinales().stream()
                    .map(nombre -> buscar(nombre, porId, porNombre))
                    .filter(java.util.Objects::nonNull)
                    .map(EstadoQuimico::getId).forEach(finales::add);
        }
        String inicial = automata.getEstadoInicial().getId();
        Set<String> estadosRevelados = ProgresoAutomata.estadosAlcanzadosCorrectamente(
                registro.getEstadoInicial(), registro.getPasosTransicion());
        String estadoActual = registro.getPasosTransicion().isEmpty()
                ? registro.getEstadoInicial()
                : registro.getPasosTransicion().getLast().estadoDestino();
        return ordenados.values().stream()
                .map(estado -> new Nodo(estado.getId(), estado.getNombre(),
                        estado.getId().equals(inicial) || estado.getNombre().equals(registro.getEstadoInicial()),
                        finales.contains(estado.getId()) || registro.getEstadosFinales().contains(estado.getNombre()),
                        estado.getId().equals(estadoActual) || estado.getNombre().equals(estadoActual),
                        estadosRevelados.contains(estado.getId())
                                || estadosRevelados.contains(estado.getNombre())))
                .toList();
    }

    private EstadoQuimico buscar(String nombre, Map<String, EstadoQuimico> porId,
                                 Map<String, EstadoQuimico> porNombre) {
        if (nombre == null) return null;
        return porId.containsKey(nombre) ? porId.get(nombre) : porNombre.get(nombre);
    }

    private void agregar(Map<String, EstadoQuimico> destino, EstadoQuimico estado) {
        if (estado != null) destino.putIfAbsent(estado.getId(), estado);
    }

    private List<Arista> construirAristas(AutomataQuimico automata) {
        Map<ParEstados, LinkedHashSet<String>> acciones = new LinkedHashMap<>();
        Map<ParEstados, Boolean> ejecutadas = new LinkedHashMap<>();
        for (EstadoQuimico origen : automata.getEstados()) {
            for (TransicionQuimica transicion : automata.getTransicionesDesdeEstado(origen)) {
                EstadoQuimico destino = transicion.getEstadoDestino();
                ParEstados par = new ParEstados(origen.getId(), destino.getId());
                acciones.computeIfAbsent(par, ignorado -> new LinkedHashSet<>())
                        .add(etiquetaAccion(transicion.getAccion()));
                boolean ejecutada = registro.getPasosTransicion().stream().anyMatch(paso ->
                        paso.estadoOrigen().equals(origen.getNombre())
                                && paso.estadoDestino().equals(destino.getNombre())
                                && paso.accion().equals(transicion.getAccion().name()));
                ejecutadas.merge(par, ejecutada, Boolean::logicalOr);
            }
        }
        List<Arista> resultado = new ArrayList<>();
        acciones.forEach((par, etiquetas) -> resultado.add(new Arista(
                par.origen(), par.destino(), String.join(" / ", etiquetas),
                ejecutadas.getOrDefault(par, false))));
        return List.copyOf(resultado);
    }

    private String etiquetaAccion(AccionLaboratorio accion) {
        return switch (accion) {
            case MONTAR_EQUIPO -> "Montar equipo";
            case CARGAR_REACTIVO -> "Cargar";
            case AGREGAR_CATALIZADOR -> "Catalizador";
            case AGREGAR_AGUA -> "Añadir agua";
            case CALENTAR -> "Calentar";
            case ENFRIAR -> "Enfriar";
            case COMPRIMIR -> "Comprimir";
            case DESCOMPRIMIR -> "Descomprimir";
            case MEZCLAR -> "Mezclar";
            case ACTIVAR_IGNICION -> "Ignición";
            case ESPERAR -> "Esperar";
            case FINALIZAR -> "Finalizar";
            case INICIAR_PLASMA -> "Plasma";
            case SELECCIONAR_METODO -> "Elegir método";
        };
    }

    private void dibujar(AutomataQuimico automata) {
        GraphicsContext gc = getGraphicsContext2D();
        double alto = getHeight();
        gc.setFill(Color.web("#071923"));
        gc.fillRect(0, 0, ANCHO, alto);
        gc.setStroke(Color.web("#164450", 0.28));
        gc.setLineWidth(1);
        for (int x = 0; x <= ANCHO; x += 32) gc.strokeLine(x, 0, x, alto);
        for (int y = 0; y <= alto; y += 32) gc.strokeLine(0, y, ANCHO, y);
        gc.setFill(Color.web("#a9fff0"));
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        gc.fillText("AUTÓMATA  /  " + registro.getIdProceso(), 28, 32);
        gc.setFill(Color.web("#81aeb6"));
        gc.setFont(Font.font("Segoe UI", 10));
        gc.fillText("Ruta ejecutada resaltada · " + registro.getNombreExperimento(), 28, 51);
        dibujarLeyenda(gc);
        dibujarAristas(gc);
        dibujarNodos(gc);
    }

    private void dibujarLeyenda(GraphicsContext gc) {
        double y = 78;
        gc.setFill(Color.web("#84f1d2"));
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        gc.fillText(">", 28, y + 4);
        gc.setFill(Color.web("#b2c6c8"));
        gc.setFont(Font.font("Segoe UI", 9));
        gc.fillText("Inicial", 43, y + 4);
        gc.setStroke(Color.web("#ffd277"));
        gc.setLineWidth(1.4);
        gc.strokeOval(122, y - 7, 18, 18);
        gc.strokeOval(126, y - 3, 10, 10);
        gc.setFill(Color.web("#b2c6c8"));
        gc.fillText("Final", 146, y + 4);
        gc.setStroke(Color.web("#50dfc2"));
        gc.setLineWidth(2.5);
        gc.strokeLine(205, y, 238, y);
        dibujarPunta(gc, 238, y, 0, Color.web("#50dfc2"));
        gc.setFill(Color.web("#b2c6c8"));
        gc.fillText("Ejecutada", 246, y + 4);
    }

    private void dibujarAristas(GraphicsContext gc) {
        Map<String, Punto> puntos = posiciones();
        for (Arista arista : aristas) {
            Punto origen = puntos.get(arista.origen());
            Punto destino = puntos.get(arista.destino());
            if (origen == null || destino == null) continue;
            Color color = arista.ejecutada() ? Color.web("#54e2c5") : Color.web("#597984", 0.72);
            double dx = destino.x() - origen.x();
            double dy = destino.y() - origen.y();
            double distancia = Math.max(1, Math.hypot(dx, dy));
            double ux = dx / distancia;
            double uy = dy / distancia;
            double x1 = origen.x() + ux * (RADIO + 3);
            double y1 = origen.y() + uy * (RADIO + 3);
            double x2 = destino.x() - ux * (RADIO + 6);
            double y2 = destino.y() - uy * (RADIO + 6);
            gc.setStroke(color);
            gc.setLineWidth(arista.ejecutada() ? 2.6 : 1.5);
            gc.strokeLine(x1, y1, x2, y2);
            dibujarPunta(gc, x2, y2, Math.atan2(dy, dx), color);
            double medioX = (x1 + x2) / 2;
            double medioY = (y1 + y2) / 2 - 7;
            double anchoEtiqueta = Math.min(190, arista.etiqueta().length() * 5.8 + 12);
            gc.setFill(Color.web("#06131a", 0.92));
            gc.fillRoundRect(medioX - anchoEtiqueta / 2, medioY - 11, anchoEtiqueta, 17, 5, 5);
            gc.setFill(arista.ejecutada() ? Color.web("#baffeb") : Color.web("#b2c6c8"));
            gc.setFont(Font.font("Segoe UI", arista.ejecutada() ? FontWeight.BOLD : FontWeight.NORMAL, 9));
            gc.fillText(arista.etiqueta(), medioX - anchoEtiqueta / 2 + 6, medioY + 1);
        }
    }

    private void dibujarNodos(GraphicsContext gc) {
        Map<String, Punto> puntos = posiciones();
        for (int i = 0; i < nodos.size(); i++) {
            Nodo nodo = nodos.get(i);
            Punto punto = puntos.get(nodo.id());
            if (punto == null) continue;
            Color borde = nodo.actual() ? Color.web("#64f0cd")
                    : nodo.inicial() ? Color.web("#5ae2c1")
                    : nodo.finalEstado() ? Color.web("#ffd277") : Color.web("#78a8b1");
            gc.setFill(Color.web(nodo.actual() ? "#145344"
                    : nodo.inicial() ? "#103d3d" : nodo.finalEstado() ? "#3c3420" : "#102a35"));
            gc.fillOval(punto.x() - RADIO, punto.y() - RADIO, RADIO * 2, RADIO * 2);
            gc.setStroke(borde);
            gc.setLineWidth(nodo.actual() || nodo.inicial() || nodo.finalEstado() ? 2.8 : 1.8);
            gc.strokeOval(punto.x() - RADIO, punto.y() - RADIO, RADIO * 2, RADIO * 2);
            if (nodo.finalEstado()) {
                gc.setLineWidth(1.5);
                gc.strokeOval(punto.x() - RADIO + 6, punto.y() - RADIO + 6,
                        RADIO * 2 - 12, RADIO * 2 - 12);
            }
            gc.setFill(Color.web("#e6faf4"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
            String etiqueta = "q" + i;
            gc.fillText(etiqueta, punto.x() - etiqueta.length() * 4, punto.y() + 5);
            if (nodo.inicial()) {
                gc.setFill(Color.web("#71f1d2"));
                gc.setFont(Font.font("Consolas", FontWeight.BOLD, 21));
                gc.fillText(">", punto.x() - RADIO - 19, punto.y() + 7);
            }
            dibujarNombreEstado(gc, nodo.revelado() ? nodo.nombre() : "?", punto.x(), punto.y() + RADIO + 16);
        }
    }

    private void dibujarNombreEstado(GraphicsContext gc, String nombre, double centroX, double y) {
        gc.setFill(Color.web("#adc5c8"));
        gc.setFont(Font.font("Segoe UI", 9));
        String limpio = nombre.replace('_', ' ');
        if (limpio.length() <= 18) {
            gc.fillText(limpio, centroX - limpio.length() * 2.5, y);
            return;
        }
        int corte = limpio.lastIndexOf(' ', 18);
        if (corte < 1) corte = 18;
        String primera = limpio.substring(0, corte);
        String segunda = limpio.substring(corte).trim();
        gc.fillText(primera, centroX - primera.length() * 2.5, y);
        gc.fillText(segunda, centroX - segunda.length() * 2.5, y + 12);
    }

    private Map<String, Punto> posiciones() {
        Map<String, Punto> puntos = new LinkedHashMap<>();
        int porFila = Math.min(ESTADOS_POR_FILA, Math.max(1, nodos.size()));
        double margen = 84;
        double separacion = (ANCHO - margen * 2) / Math.max(1, porFila - 1);
        for (int indice = 0; indice < nodos.size(); indice++) {
            int fila = indice / porFila;
            int columna = indice % porFila;
            if (fila % 2 == 1) columna = porFila - 1 - columna;
            double x = porFila == 1 ? ANCHO / 2 : margen + columna * separacion;
            double y = 139 + fila * 165;
            puntos.put(nodos.get(indice).id(), new Punto(x, y));
        }
        return puntos;
    }

    private void dibujarPunta(GraphicsContext gc, double x, double y, double angulo, Color color) {
        double longitud = 10;
        double apertura = Math.PI / 7;
        gc.setFill(color);
        gc.fillPolygon(new double[]{x, x - longitud * Math.cos(angulo - apertura), x - longitud * Math.cos(angulo + apertura)},
                new double[]{y, y - longitud * Math.sin(angulo - apertura), y - longitud * Math.sin(angulo + apertura)}, 3);
    }

    private record Nodo(String id, String nombre, boolean inicial, boolean finalEstado, boolean actual, boolean revelado) {}
    private record ParEstados(String origen, String destino) {}
    private record Arista(String origen, String destino, String etiqueta, boolean ejecutada) {}
    private record Punto(double x, double y) {}
}
