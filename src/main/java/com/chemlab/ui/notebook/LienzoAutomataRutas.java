package com.chemlab.ui.notebook;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EstadoQuimico;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.factory.FabricaAutomata;
import com.chemlab.game.procedure.ProtocoloProcedimiento;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Diagrama ramificado construido desde los autómatas que ejecuta cada ruta. */
final class LienzoAutomataRutas extends Canvas {

    private static final double ANCHO = 1420;
    private static final double RADIO = 31;
    private static final double ESPACIO_FILA = 118;
    private static final double X_INICIO = 62;
    private static final double X_ESTADOS = 218;
    private static final double X_FIN = 1345;

    private record RutaVisual(
            ProtocoloProcedimiento.InfoRutaSintesis ruta,
            EstadoQuimico inicial,
            List<EstadoQuimico> estados,
            List<TransicionQuimica> transiciones,
            Set<String> finales
    ) {}

    LienzoAutomataRutas(List<ProtocoloProcedimiento.InfoRutaSintesis> rutas) {
        List<RutaVisual> visuales = rutas.stream().map(this::construirRuta).toList();
        double alto = Math.max(180, 112 + visuales.size() * ESPACIO_FILA);
        setWidth(ANCHO);
        setHeight(alto);
        dibujar(visuales, alto);
    }

    private RutaVisual construirRuta(ProtocoloProcedimiento.InfoRutaSintesis ruta) {
        AutomataQuimico automata = FabricaAutomata.crearAutomataParaReaccion(ruta.idReaccion());
        Map<String, EstadoQuimico> porId = new LinkedHashMap<>();
        ArrayDeque<EstadoQuimico> pendientes = new ArrayDeque<>();
        Set<String> visitados = new LinkedHashSet<>();
        List<TransicionQuimica> transiciones = new ArrayList<>();
        pendientes.add(automata.getEstadoInicial());
        while (!pendientes.isEmpty()) {
            EstadoQuimico estado = pendientes.removeFirst();
            if (!visitados.add(estado.getId())) continue;
            porId.put(estado.getId(), estado);
            List<TransicionQuimica> salientes = automata.getTransicionesDesdeEstado(estado).stream()
                    .sorted(Comparator.comparing(transicion -> transicion.getAccion().name()))
                    .toList();
            transiciones.addAll(salientes);
            salientes.stream().map(TransicionQuimica::getEstadoDestino)
                    .filter(destino -> !visitados.contains(destino.getId()))
                    .forEach(pendientes::addLast);
        }
        return new RutaVisual(ruta, automata.getEstadoInicial(), List.copyOf(porId.values()),
                List.copyOf(transiciones), automata.getEstadosFinales().stream()
                .map(EstadoQuimico::getId).collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }

    private void dibujar(List<RutaVisual> rutas, double alto) {
        GraphicsContext grafico = getGraphicsContext2D();
        grafico.setFill(Color.web("#071923"));
        grafico.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        grafico.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        double centro = alto / 2;
        dibujarNodo(grafico, X_INICIO, centro, "INICIO", false, true);
        dibujarNodo(grafico, X_FIN, centro, "PRODUCTO", true, true);

        int maximoEstados = rutas.stream().mapToInt(ruta -> ruta.estados().size()).max().orElse(1);
        double espacioX = maximoEstados <= 1
                ? 0 : Math.min(150, (X_FIN - X_ESTADOS - 100) / (maximoEstados - 1));
        for (int indice = 0; indice < rutas.size(); indice++) {
            RutaVisual ruta = rutas.get(indice);
            double y = 56 + indice * ESPACIO_FILA;
            grafico.setFill(Color.web("#9edbd4"));
            grafico.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            grafico.fillText(ruta.ruta().nombreRuta(), X_ESTADOS, y - 39, X_FIN - X_ESTADOS);

            Map<String, Integer> posicionPorEstado = new LinkedHashMap<>();
            for (int posicion = 0; posicion < ruta.estados().size(); posicion++) {
                EstadoQuimico estado = ruta.estados().get(posicion);
                posicionPorEstado.put(estado.getId(), posicion);
            }
            double inicioX = X_ESTADOS + posicionPorEstado.get(ruta.inicial().getId()) * espacioX;
            dibujarFlecha(grafico, X_INICIO + RADIO, centro, inicioX - RADIO, y, "elegir ruta");

            for (TransicionQuimica transicion : ruta.transiciones()) {
                double xOrigen = X_ESTADOS + posicionPorEstado.get(transicion.getEstadoOrigen().getId()) * espacioX;
                double xDestino = X_ESTADOS + posicionPorEstado.get(transicion.getEstadoDestino().getId()) * espacioX;
                dibujarFlecha(grafico, xOrigen + RADIO, y, xDestino - RADIO, y,
                        nombreAccion(transicion));
            }
            for (String estadoFinal : ruta.finales()) {
                double xFinal = X_ESTADOS + posicionPorEstado.get(estadoFinal) * espacioX;
                dibujarFlecha(grafico, xFinal + RADIO, y, X_FIN - RADIO, centro, "ruta completada");
            }
            for (int posicion = 0; posicion < ruta.estados().size(); posicion++) {
                EstadoQuimico estado = ruta.estados().get(posicion);
                double x = X_ESTADOS + posicion * espacioX;
                dibujarNodo(grafico, x, y, estado.getNombre(),
                        ruta.finales().contains(estado.getId()),
                        ruta.inicial().getId().equals(estado.getId()));
            }
        }
    }

    private void dibujarNodo(
            GraphicsContext grafico,
            double x,
            double y,
            String nombre,
            boolean finalizado,
            boolean inicial
    ) {
        grafico.setFill(Color.web(finalizado ? "#164c43" : "#123746"));
        grafico.setStroke(Color.web(inicial ? "#65e4cc" : finalizado ? "#7be1b5" : "#55b8bb"));
        grafico.setLineWidth(finalizado ? 3 : 1.5);
        grafico.fillOval(x - RADIO, y - RADIO, RADIO * 2, RADIO * 2);
        grafico.strokeOval(x - RADIO, y - RADIO, RADIO * 2, RADIO * 2);
        grafico.setFill(Color.web("#e4f6ef"));
        grafico.setFont(Font.font("Consolas", FontWeight.NORMAL, 9));
        List<String> lineas = dividir(nombre, 12);
        double altoTexto = lineas.size() * 11;
        for (int indice = 0; indice < lineas.size(); indice++) {
            grafico.fillText(lineas.get(indice), x - RADIO + 4,
                    y - altoTexto / 2 + 9 + indice * 11, RADIO * 2 - 8);
        }
        if (finalizado) {
            grafico.setLineWidth(1);
            grafico.strokeOval(x - RADIO + 5, y - RADIO + 5, (RADIO - 5) * 2, (RADIO - 5) * 2);
        }
    }

    private void dibujarFlecha(
            GraphicsContext grafico,
            double x1,
            double y1,
            double x2,
            double y2,
            String etiqueta
    ) {
        grafico.setStroke(Color.web("#6d9ea9"));
        grafico.setFill(Color.web("#6d9ea9"));
        grafico.setLineWidth(1.3);
        grafico.strokeLine(x1, y1, x2, y2);
        double angulo = Math.atan2(y2 - y1, x2 - x1);
        double punta = 8;
        grafico.fillPolygon(
                new double[]{x2, x2 - punta * Math.cos(angulo - 0.45), x2 - punta * Math.cos(angulo + 0.45)},
                new double[]{y2, y2 - punta * Math.sin(angulo - 0.45), y2 - punta * Math.sin(angulo + 0.45)},
                3
        );
        grafico.setFont(Font.font("Consolas", 8));
        grafico.setFill(Color.web("#b8d5d5"));
        double centroX = (x1 + x2) / 2;
        double centroY = (y1 + y2) / 2;
        grafico.fillText(etiqueta, centroX - 52, centroY - 7, 104);
    }

    private List<String> dividir(String texto, int maximo) {
        String limpio = texto == null ? "" : texto.trim();
        List<String> lineas = new ArrayList<>();
        while (limpio.length() > maximo) {
            int corte = limpio.lastIndexOf(' ', maximo);
            if (corte < 1) corte = maximo;
            lineas.add(limpio.substring(0, corte));
            limpio = limpio.substring(corte).trim();
        }
        if (!limpio.isEmpty()) lineas.add(limpio);
        return lineas;
    }

    private String nombreAccion(TransicionQuimica transicion) {
        return switch (transicion.getAccion()) {
            case CARGAR_REACTIVO -> "cargar reactivos";
            case MEZCLAR -> "mezclar";
            case INICIAR_PLASMA -> "activar plasma";
            case CALENTAR -> "calentar";
            case COMPRIMIR -> "comprimir";
            case AGREGAR_CATALIZADOR -> "añadir catalizador";
            case AGREGAR_AGUA -> "añadir agua";
            case ACTIVAR_IGNICION -> "activar ignición";
            case FINALIZAR -> "recuperar producto";
            default -> transicion.getAccion().name().replace('_', ' ').toLowerCase();
        };
    }
}
