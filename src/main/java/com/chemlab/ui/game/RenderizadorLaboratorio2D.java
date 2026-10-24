package com.chemlab.ui.game;

import com.chemlab.game.world.EstacionLaboratorio;
import com.chemlab.game.world.JugadorLaboratorio;
import com.chemlab.game.world.MapaLaboratorio;
import com.chemlab.game.ProductoAlmacenado;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.TipoEquipo;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Locale;

/** Dibuja el escenario y el personaje sin gestionar entradas ni estado de sesión. */
public final class RenderizadorLaboratorio2D extends Canvas {

    private final MapaLaboratorio mapa;
    private final JugadorLaboratorio jugador;

    public RenderizadorLaboratorio2D(MapaLaboratorio mapa, JugadorLaboratorio jugador) {
        super(MapaLaboratorio.ANCHO, MapaLaboratorio.ALTO);
        this.mapa = mapa;
        this.jugador = jugador;
    }

    public void renderizar(
            EstacionLaboratorio estacionCercana,
            String idReaccion,
            EstacionLaboratorio estacionActiva,
            AccionLaboratorio accionEnCurso,
            double progreso,
            java.util.List<ProductoAlmacenado> productosEnMesa,
            Sustancia productoEnMano,
            boolean resaltarAlmacen
    ) {
        double anchoCanvas = getWidth();
        double altoCanvas = getHeight();
        if (anchoCanvas <= 0 || altoCanvas <= 0) return;

        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.web("#08121a"));
        gc.fillRect(0, 0, anchoCanvas, altoCanvas);
        double escala = Math.min(anchoCanvas / MapaLaboratorio.ANCHO, altoCanvas / MapaLaboratorio.ALTO);
        double margenX = (anchoCanvas - MapaLaboratorio.ANCHO * escala) / 2;
        double margenY = (altoCanvas - MapaLaboratorio.ALTO * escala) / 2;
        gc.save();
        gc.translate(margenX, margenY);
        gc.scale(escala, escala);

        dibujarPiso(gc);
        dibujarDecoracion(gc);
        for (EstacionLaboratorio estacion : mapa.getEstaciones()) {
            boolean cerca = estacion == estacionCercana;
            boolean objetivo = idReaccion != null && estacion.admite(idReaccion);
            boolean activa = estacion == estacionActiva && accionEnCurso != null;
            dibujarEstacion(gc, estacion, cerca, objetivo, activa, accionEnCurso, progreso);
            List<ProductoAlmacenado> productosEstacion = productosEnMesa == null ? List.of()
                    : productosEnMesa.stream().filter(producto ->
                    producto.estacionOrigen().id().equals(estacion.id())).toList();
            if (!productosEstacion.isEmpty()) {
                for (int indice = 0; indice < productosEstacion.size(); indice++) {
                    dibujarProducto(gc, estacion, productosEstacion.get(indice).sustancia(),
                            indice, productosEstacion.size());
                }
            }
        }
        dibujarAlmacen(gc, resaltarAlmacen);
        dibujarJugador(gc);
        if (productoEnMano != null) dibujarProductoEnMano(gc, productoEnMano);
        gc.restore();
    }

    public int productoBajoPuntero(double pantallaX, double pantallaY, List<ProductoAlmacenado> productos) {
        if (productos == null || productos.isEmpty()) return -1;
        Punto mundo = convertirAPuntoMundo(pantallaX, pantallaY);
        for (int indice = 0; indice < productos.size(); indice++) {
            ProductoAlmacenado producto = productos.get(indice);
            EstacionLaboratorio estacion = producto.estacionOrigen();
            List<ProductoAlmacenado> grupo = productos.stream().filter(item ->
                    item.estacionOrigen().id().equals(estacion.id())).toList();
            int indiceGrupo = grupo.indexOf(producto);
            double x = estacion.x() + estacion.ancho() - 38
                    + (indiceGrupo - (grupo.size() - 1) / 2.0) * 38;
            double y = estacion.y() + 35;
            if (mundo.x() >= x - 18 && mundo.x() <= x + 38
                    && mundo.y() >= y - 28 && mundo.y() <= y + 33) return indice;
        }
        return -1;
    }

    public boolean punteroSobreAlmacen(double pantallaX, double pantallaY) {
        Punto mundo = convertirAPuntoMundo(pantallaX, pantallaY);
        return mundo.x() >= 35 && mundo.x() <= 205 && mundo.y() >= 385 && mundo.y() <= 525;
    }

    public boolean jugadorJuntoAlAlmacen() {
        double x = jugador.getX();
        double y = jugador.getY();
        double puntoX = Math.max(42, Math.min(x, 192));
        double puntoY = Math.max(395, Math.min(y, 507));
        return Math.hypot(x - puntoX, y - puntoY) <= 82;
    }

    private Punto convertirAPuntoMundo(double pantallaX, double pantallaY) {
        double escala = Math.min(getWidth() / MapaLaboratorio.ANCHO, getHeight() / MapaLaboratorio.ALTO);
        double margenX = (getWidth() - MapaLaboratorio.ANCHO * escala) / 2;
        double margenY = (getHeight() - MapaLaboratorio.ALTO * escala) / 2;
        return new Punto((pantallaX - margenX) / escala, (pantallaY - margenY) / escala);
    }

    private record Punto(double x, double y) {}

    private void dibujarPiso(GraphicsContext gc) {
        gc.setFill(Color.web("#172832"));
        gc.fillRect(0, 0, MapaLaboratorio.ANCHO, MapaLaboratorio.ALTO);
        gc.setFill(Color.web("#243944"));
        gc.fillRect(0, 0, MapaLaboratorio.ANCHO, 108);
        gc.setFill(Color.web("#314c56"));
        gc.fillRect(0, 104, MapaLaboratorio.ANCHO, 7);

        for (int fila = 0; fila < 13; fila++) {
            for (int columna = 0; columna < 22; columna++) {
                if ((fila + columna) % 2 == 0) {
                    gc.setFill(Color.web("#1a2c35", 0.42));
                    gc.fillRect(columna * 60, 116 + fila * 48, 60, 48);
                }
            }
        }
        gc.setStroke(Color.web("#61747a", 0.2));
        gc.setLineWidth(1);
        for (int x = 0; x <= MapaLaboratorio.ANCHO; x += 60) gc.strokeLine(x, 112, x, 720);
        for (int y = 112; y <= 720; y += 48) gc.strokeLine(0, y, 1280, y);

        gc.setStroke(Color.web("#e0b75d", 0.7));
        gc.setLineWidth(3);
        gc.setLineDashes(14, 12);
        gc.strokeLine(60, 565, 1210, 565);
        gc.setLineDashes();
    }

    private void dibujarDecoracion(GraphicsContext gc) {
        gc.setFill(Color.web("#d7e5e4"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        gc.fillText("LABORATORIO PRINCIPAL", 38, 47);
        gc.setFill(Color.web("#91b5ba"));
        gc.setFont(Font.font("Segoe UI", 10));
        gc.fillText("SÍNTESIS · TERMODINÁMICA · MATERIALES", 40, 68);

        gc.setFill(Color.web("#b7dce0"));
        gc.fillRoundRect(890, 18, 185, 58, 8, 8);
        gc.setFill(Color.web("#567681"));
        gc.fillRoundRect(900, 26, 165, 43, 4, 4);
        gc.setStroke(Color.web("#e6f2ee", 0.8));
        gc.setLineWidth(2);
        gc.strokeLine(911, 43, 1052, 43);
        gc.setFill(Color.web("#caf3e9"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.fillText("PROTOCOLO DE SEGURIDAD", 914, 61);

        dibujarVentana(gc, 470, 20);
        dibujarVentana(gc, 650, 20);
        gc.setFill(Color.web("#b6423c"));
        gc.fillRoundRect(1155, 28, 80, 48, 7, 7);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.fillText("SALIDA", 1174, 58);

        gc.setStroke(Color.web("#9aadb2", 0.4));
        gc.setLineWidth(4);
        gc.strokeLine(22, 110, 1258, 110);
    }

    private void dibujarVentana(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#79a5b5"));
        gc.fillRoundRect(x, y, 138, 56, 7, 7);
        gc.setFill(Color.web("#8fc4d3"));
        gc.fillRoundRect(x + 5, y + 5, 128, 46, 4, 4);
        gc.setStroke(Color.web("#4d7180"));
        gc.setLineWidth(3);
        gc.strokeLine(x + 69, y + 5, x + 69, y + 51);
        gc.strokeLine(x + 5, y + 28, x + 133, y + 28);
    }

    private void dibujarEstacion(
            GraphicsContext gc,
            EstacionLaboratorio estacion,
            boolean cerca,
            boolean objetivo,
            boolean activa,
            AccionLaboratorio accionEnCurso,
            double progreso
    ) {
        double x = estacion.x();
        double y = estacion.y();
        double w = estacion.ancho();
        double h = estacion.alto();
        Color acento = objetivo ? Color.web("#40c4aa") : Color.web("#83a6ad");

        gc.setFill(Color.web("#000000", 0.3));
        gc.fillRoundRect(x + 7, y + 10, w, h, 12, 12);
        gc.setFill(Color.web("#435c63"));
        gc.fillRoundRect(x, y + 12, w, h - 12, 10, 10);
        gc.setFill(Color.web("#789096"));
        gc.fillRoundRect(x, y, w, 30, 10, 10);
        gc.setFill(Color.web("#90a7aa"));
        gc.fillRect(x + 7, y + 23, w - 14, 8);
        gc.setStroke(activa ? Color.web("#ffbd55") : cerca ? Color.web("#f3cd72") : acento);
        gc.setLineWidth(activa ? 4.5 : cerca ? 3.5 : objetivo ? 2.5 : 1.2);
        gc.strokeRoundRect(x - 2, y - 2, w + 4, h + 4, 12, 12);

        switch (estacion.tipoEquipo()) {
            case MATRAZ -> dibujarMaterialDeVidrio(gc, x + w / 2, y + 15);
            case HORNO -> dibujarHorno(gc, x + w / 2 - 42, y + 14);
            case REACTOR -> dibujarPrensa(gc, x + w / 2 - 47, y + 10);
            case CAMARA_SELLADA -> dibujarCamaraCvd(gc, x + w / 2 - 57, y + 12);
            case BANDEJA -> dibujarBandeja(gc, x + w / 2 - 50, y + 13);
            default -> dibujarEquipoGenerico(gc, x + w / 2, y + 15);
        }
        if (activa) dibujarEfectoAccion(gc, estacion, accionEnCurso, progreso);

        gc.setFill(Color.web("#e6f1ee"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.fillText(estacion.nombre().toUpperCase(Locale.ROOT), x + 8, y + h - 11);
        gc.setFill(Color.web("#bdd0d1"));
        gc.setFont(Font.font("Segoe UI", 9));
        gc.fillText(estacion.descripcion(), x + 8, y + h + 7);
    }

    private void dibujarEfectoAccion(
            GraphicsContext gc,
            EstacionLaboratorio estacion,
            AccionLaboratorio accion,
            double progreso
    ) {
        double centroX = estacion.x() + estacion.ancho() / 2;
        double parteSuperior = estacion.y() + 5;
        double pulso = 0.4 + 0.45 * Math.sin(progreso * Math.PI * 8);
        gc.save();
        switch (accion) {
            case CALENTAR -> {
                if (estacion.tipoEquipo() == TipoEquipo.MATRAZ) {
                    double baseFlama = estacion.y() + 82;
                    gc.setFill(Color.web("#ff742e", 0.3 + pulso * 0.35));
                    gc.fillOval(centroX - 23, baseFlama - 19, 46, 27);
                    gc.setFill(Color.web("#ffc45c", 0.92));
                    gc.fillPolygon(new double[]{centroX - 13, centroX - 6, centroX - 4,
                                    centroX + 1, centroX + 9, centroX + 6, centroX + 14,
                                    centroX + 5, centroX - 13},
                            new double[]{baseFlama, baseFlama - 13, baseFlama - 24,
                                    baseFlama - 17, baseFlama - 28, baseFlama - 11,
                                    baseFlama, baseFlama + 2, baseFlama + 2}, 9);
                } else if (estacion.tipoEquipo() == TipoEquipo.HORNO) {
                    gc.setFill(Color.web("#f16a35", 0.45 + pulso * 0.4));
                    gc.fillRoundRect(centroX - 31, estacion.y() + 40, 62, 30, 6, 6);
                    gc.setFill(Color.web("#ffd57d", 0.84));
                    gc.fillRoundRect(centroX - 22, estacion.y() + 47, 44, 15, 4, 4);
                    gc.setStroke(Color.web("#ffba72", 0.85));
                    gc.setLineWidth(2);
                    gc.strokeLine(centroX - 28, estacion.y() + 30, centroX - 31, estacion.y() + 19 - progreso * 7);
                    gc.strokeLine(centroX + 27, estacion.y() + 30, centroX + 30, estacion.y() + 17 - progreso * 7);
                } else if (estacion.tipoEquipo() == TipoEquipo.REACTOR) {
                    gc.setFill(Color.web("#ff803e", 0.3 + pulso * 0.35));
                    gc.fillOval(centroX - 31, estacion.y() + 28, 62, 48);
                    gc.setFill(Color.web("#ffc96e", 0.9));
                    gc.fillOval(centroX - 12, estacion.y() + 43, 24, 18);
                } else if (estacion.tipoEquipo() == TipoEquipo.CAMARA_SELLADA) {
                    gc.setFill(Color.web("#ee7bff", 0.22 + pulso * 0.3));
                    gc.fillOval(centroX - 46, estacion.y() + 18, 92, 70);
                    gc.setFill(Color.web("#ffc17c", 0.78));
                    gc.fillOval(centroX - 13, estacion.y() + 40, 26, 25);
                } else {
                    gc.setFill(Color.web("#ff742e", 0.3 + pulso * 0.35));
                    gc.fillOval(centroX - 34, parteSuperior - 3, 68, 42);
                    gc.setFill(Color.web("#ffc45c", 0.85));
                    gc.fillPolygon(new double[]{centroX - 13, centroX - 4, centroX - 8, centroX,
                                    centroX + 10, centroX + 6, centroX + 17, centroX + 7, centroX - 13},
                            new double[]{parteSuperior + 31, parteSuperior + 18, parteSuperior + 8,
                                    parteSuperior + 17, parteSuperior + 2, parteSuperior + 20,
                                    parteSuperior + 31, parteSuperior + 37, parteSuperior + 37}, 9);
                    gc.setStroke(Color.web("#ffe3a1", 0.8));
                    gc.setLineWidth(2);
                    gc.strokeLine(centroX - 28, parteSuperior - 5, centroX - 31, parteSuperior - 16 - progreso * 8);
                    gc.strokeLine(centroX + 25, parteSuperior - 5, centroX + 28, parteSuperior - 18 - progreso * 7);
                }
            }
            case ENFRIAR -> {
                gc.setStroke(Color.web("#79d8ff", 0.9));
                gc.setLineWidth(3);
                for (int i = 0; i < 4; i++) {
                    double x = centroX - 24 + i * 16;
                    double y = parteSuperior + 5 + ((i * 11 + progreso * 25) % 34);
                    gc.strokeLine(x, y - 5, x, y + 5);
                    gc.strokeLine(x - 4, y - 3, x + 4, y + 3);
                    gc.strokeLine(x - 4, y + 3, x + 4, y - 3);
                }
            }
            case MEZCLAR -> {
                gc.setStroke(Color.web("#f5f2cf", 0.95));
                gc.setLineWidth(4);
                gc.translate(centroX, parteSuperior + 32);
                gc.rotate(progreso * 1440);
                gc.strokeLine(-30, 0, 30, 0);
                gc.strokeLine(0, -10, 0, 10);
            }
            case INICIAR_PLASMA -> {
                gc.setFill(Color.web("#bd7cff", 0.22 + pulso * 0.35));
                gc.fillOval(centroX - 48, parteSuperior - 8, 96, 76);
                gc.setStroke(Color.web("#e1bbff", 0.85));
                gc.setLineWidth(2);
                gc.strokeOval(centroX - 43, parteSuperior - 4, 86, 68);
                gc.setFill(Color.web("#dcafff", 0.9));
                gc.fillOval(centroX - 6, parteSuperior + 25, 12, 12);
            }
            case COMPRIMIR, DESCOMPRIMIR -> {
                double separacion = 12 + (1 - progreso) * 15;
                gc.setFill(Color.web("#d7e6e7"));
                gc.fillRoundRect(centroX - 40, parteSuperior + 6, 80, 8, 4, 4);
                gc.fillRoundRect(centroX - 40, parteSuperior + 42 + separacion, 80, 8, 4, 4);
                gc.setFill(Color.web("#ffbb66", 0.9));
                gc.fillOval(centroX - 8, parteSuperior + 24 + separacion / 2, 16, 14);
            }
            case ACTIVAR_IGNICION -> {
                gc.setFill(Color.web("#ffad42", 0.35 + pulso * 0.4));
                gc.fillOval(centroX - 28, parteSuperior + 4, 56, 50);
                gc.setFill(Color.web("#ffe27d"));
                gc.fillPolygon(new double[]{centroX - 14, centroX - 6, centroX - 4,
                                centroX + 5, centroX + 11, centroX + 7, centroX + 17,
                                centroX + 3, centroX - 12},
                        new double[]{parteSuperior + 43, parteSuperior + 28, parteSuperior + 14,
                                parteSuperior + 25, parteSuperior + 12, parteSuperior + 32,
                                parteSuperior + 43, parteSuperior + 49, parteSuperior + 49}, 9);
            }
            case ESPERAR -> {
                gc.setStroke(Color.web("#ffd36e", 0.8));
                gc.setLineWidth(4);
                gc.strokeOval(centroX - 21, parteSuperior + 8, 42, 42);
                gc.setStroke(Color.web("#fff1c6"));
                gc.strokeLine(centroX, parteSuperior + 29, centroX, parteSuperior + 15 + (1 - progreso) * 12);
                gc.strokeLine(centroX, parteSuperior + 29, centroX + progreso * 12, parteSuperior + 34);
            }
            case FINALIZAR -> {
                gc.setStroke(Color.web("#9be8f2", 0.85));
                gc.setLineWidth(3);
                gc.strokeOval(centroX - 34, parteSuperior + 5, 68, 52);
                gc.setFill(Color.web("#d5fff4", 0.85));
                for (int i = 0; i < 4; i++) {
                    double chispaX = centroX - 22 + i * 14;
                    double chispaY = parteSuperior + 13 + ((i * 9 + progreso * 22) % 34);
                    gc.fillOval(chispaX, chispaY, 4, 4);
                }
            }
            default -> {
                gc.setStroke(Color.web("#91f3dc", 0.8));
                gc.setLineWidth(3);
                gc.strokeOval(centroX - 33, parteSuperior + 5, 66, 54);
            }
        }
        gc.restore();
    }

    private void dibujarProducto(GraphicsContext gc, EstacionLaboratorio estacion,
                                 Sustancia producto, int indice, int cantidadProductos) {
        double x = estacion.x() + estacion.ancho() - 38
                + (indice - (cantidadProductos - 1) / 2.0) * 38;
        double y = estacion.y() + 35;
        Color tono = colorProducto(producto.getId());
        gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.23));
        gc.fillOval(x - 16, y - 13, 44, 40);
        String id = producto.getId();
        if (id.equals("DIAMANTE") || id.equals("GRAFITO")) {
            gc.setFill(id.equals("DIAMANTE") ? Color.web("#d6fbff") : Color.web("#4d5357"));
            gc.fillPolygon(new double[]{x + 5, x + 17, x + 24, x + 17, x + 5, x - 2},
                    new double[]{y - 12, y - 5, y + 6, y + 17, y + 13, y + 1}, 6);
            gc.setFill(Color.web("#ffffff", 0.52));
            gc.fillPolygon(new double[]{x + 5, x + 17, x + 5, x - 2},
                    new double[]{y - 12, y - 5, y + 13, y + 1}, 4);
            gc.setStroke(Color.web(id.equals("DIAMANTE") ? "#64c6d3" : "#24282b"));
            gc.setLineWidth(1.5);
            gc.strokePolygon(new double[]{x + 5, x + 17, x + 24, x + 17, x + 5, x - 2},
                    new double[]{y - 12, y - 5, y + 6, y + 17, y + 13, y + 1}, 6);
        } else if (id.equals("Fe2O3") || id.equals("FeOH3")) {
            gc.setFill(Color.web("#655145"));
            gc.fillRoundRect(x + 1, y - 3, 30, 21, 6, 6);
            for (int i = 0; i < 8; i++) {
                gc.setFill(Color.web(i % 2 == 0 ? "#c66b42" : "#8d4933", 0.9));
                gc.fillOval(x + (i * 7 % 27), y - 1 + (i % 3) * 5, 5 + i % 3, 4 + i % 2);
            }
            gc.setStroke(Color.web("#e7a276", 0.9));
            gc.strokeRoundRect(x + 1, y - 3, 30, 21, 6, 6);
        } else if (id.equals("CuO")) {
            gc.setFill(Color.web("#282d31"));
            gc.fillOval(x + 1, y - 5, 30, 27);
            gc.setFill(Color.web("#4a3d34"));
            gc.fillOval(x + 7, y, 17, 15);
            gc.setStroke(Color.web("#bd7750", 0.7));
            gc.strokeOval(x + 1, y - 5, 30, 27);
        } else {
            switch (producto.getEstadoMateria()) {
                case GASEOSO -> {
                    gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.28));
                    gc.fillOval(x - 2, y - 13, 38, 38);
                    gc.setFill(tono);
                    gc.fillOval(x + 1, y + 5, 8, 8);
                    gc.fillOval(x + 12, y - 5, 13, 13);
                    gc.fillOval(x + 23, y + 10, 7, 7);
                    gc.setStroke(Color.web("#e4fbff", 0.65));
                    gc.setLineWidth(1);
                    gc.strokeOval(x - 2, y - 13, 38, 38);
                }
                case LIQUIDO -> {
                    gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.28));
                    gc.fillRoundRect(x, y - 7, 32, 29, 7, 7);
                    gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.86));
                    gc.fillRoundRect(x + 3, y + 4, 26, 14, 4, 4);
                    gc.setStroke(Color.web("#e2ffff", 0.82));
                    gc.strokeRoundRect(x, y - 7, 32, 29, 7, 7);
                }
                case SOLIDO -> {
                    gc.setFill(Color.web("#59666a"));
                    gc.fillOval(x, y + 9, 32, 13);
                    gc.setFill(tono);
                    for (int i = 0; i < 4; i++) {
                        double cx = x + 3 + i * 7;
                        double altura = 12 + (i % 2) * 7;
                        gc.fillPolygon(new double[]{cx, cx + 6, cx + 3},
                                new double[]{y + 13, y + 13, y + 13 - altura}, 3);
                    }
                    gc.setStroke(Color.web("#ffffff", 0.75));
                    gc.setLineWidth(0.9);
                    gc.strokeLine(x + 5, y + 12, x + 8, y + 1);
                }
                case PLASMA -> {
                    gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.35));
                    gc.fillOval(x - 2, y - 12, 36, 36);
                    gc.setFill(tono.brighter());
                    gc.fillOval(x + 5, y - 4, 21, 20);
                }
            }
        }
        gc.setFill(Color.web("#eafff9"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 8));
        gc.fillText(producto.getFormula(), x - 7, y + 30);
    }

    private Color colorProducto(String id) {
        return switch (id) {
            case "Fe2O3", "FeOH3" -> Color.web("#b85e3f");
            case "CuO" -> Color.web("#34383b");
            case "Cu" -> Color.web("#c47b4b");
            case "DIAMANTE", "SiO2" -> Color.web("#c8f2ff");
            case "CO2", "NH3", "HCl", "CH4", "SO2", "H2S" -> Color.web("#b8dce3");
            case "H2O", "NaCl_AC" -> Color.web("#55b9e0");
            case "NaCl", "CaCO3", "Ca3PO42" -> Color.web("#e8e3cb");
            default -> Color.web("#c9dbd7");
        };
    }

    private void dibujarMaterialDeVidrio(GraphicsContext gc, double centroX, double y) {
        gc.setFill(Color.web("#c5eef1", 0.85));
        gc.fillRoundRect(centroX - 6, y, 12, 20, 3, 3);
        gc.fillOval(centroX - 27, y + 14, 54, 44);
        gc.setFill(Color.web("#45b5a0", 0.82));
        gc.fillOval(centroX - 22, y + 31, 44, 22);
        gc.setStroke(Color.web("#ebffff"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(centroX - 6, y, 12, 20, 3, 3);
        gc.strokeOval(centroX - 27, y + 14, 54, 44);
        gc.setFill(Color.web("#e1edf0"));
        gc.fillRoundRect(centroX + 38, y + 24, 44, 35, 4, 4);
        gc.setFill(Color.web("#315c67"));
        gc.fillRoundRect(centroX + 44, y + 29, 32, 19, 3, 3);
        gc.setFill(Color.web("#92f0dc"));
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        gc.fillText("25°", centroX + 47, y + 43);

        // Base, tubo y línea de gas del mechero Bunsen bajo el matraz.
        gc.setFill(Color.web("#c8d6d4"));
        gc.fillRoundRect(centroX - 15, y + 65, 30, 7, 3, 3);
        gc.fillRoundRect(centroX - 4, y + 49, 8, 18, 3, 3);
        gc.setStroke(Color.web("#9db2b2"));
        gc.setLineWidth(2);
        gc.strokeLine(centroX + 14, y + 68, centroX + 34, y + 68);
        gc.setFill(Color.web("#567782"));
        gc.fillOval(centroX + 31, y + 64, 8, 8);
    }

    private void dibujarHorno(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#29353b"));
        gc.fillRoundRect(x, y, 84, 68, 8, 8);
        gc.setStroke(Color.web("#b7c4c4"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(x + 7, y + 8, 70, 47, 6, 6);
        gc.setFill(Color.web("#e28045"));
        gc.fillRoundRect(x + 16, y + 18, 52, 28, 4, 4);
        gc.setFill(Color.web("#ffd078", 0.8));
        gc.fillRoundRect(x + 23, y + 23, 38, 18, 3, 3);
        gc.setFill(Color.web("#96f1d9"));
        gc.fillRoundRect(x + 28, y + 57, 28, 8, 3, 3);
    }

    private void dibujarPrensa(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#3b555e"));
        gc.fillRoundRect(x, y + 8, 94, 60, 8, 8);
        gc.setFill(Color.web("#b3c3c5"));
        gc.fillRoundRect(x + 8, y + 3, 12, 62, 4, 4);
        gc.fillRoundRect(x + 74, y + 3, 12, 62, 4, 4);
        gc.fillRoundRect(x + 8, y + 2, 78, 14, 4, 4);
        gc.setFill(Color.web("#d49858"));
        gc.fillRoundRect(x + 34, y + 28, 26, 24, 4, 4);
        gc.setFill(Color.web("#56c5b1"));
        gc.fillOval(x + 41, y + 34, 12, 12);
    }

    private void dibujarCamaraCvd(GraphicsContext gc, double x, double y) {
        dibujarCilindroGas(gc, x - 24, y + 3, Color.web("#4aa8c3"));
        dibujarCilindroGas(gc, x + 120, y + 3, Color.web("#a6b8c1"));
        gc.setFill(Color.web("#344c57"));
        gc.fillRoundRect(x, y + 9, 114, 56, 13, 13);
        gc.setStroke(Color.web("#b7d1d4"));
        gc.setLineWidth(4);
        gc.strokeRoundRect(x + 4, y + 13, 106, 48, 11, 11);
        gc.setFill(Color.web("#492c70"));
        gc.fillOval(x + 40, y + 21, 36, 36);
        gc.setFill(Color.web("#bd7cff", 0.75));
        gc.fillOval(x + 48, y + 29, 20, 20);
        gc.setFill(Color.web("#8fe0d4"));
        gc.fillRoundRect(x + 8, y + 1, 18, 14, 4, 4);
        gc.fillRoundRect(x + 88, y + 1, 18, 14, 4, 4);
    }

    private void dibujarCilindroGas(GraphicsContext gc, double x, double y, Color color) {
        gc.setFill(Color.web("#182a33"));
        gc.fillRoundRect(x, y + 11, 28, 55, 8, 8);
        gc.setFill(color);
        gc.fillRoundRect(x + 4, y + 16, 20, 43, 6, 6);
        gc.setFill(Color.web("#c7d7d8"));
        gc.fillRoundRect(x + 9, y + 5, 10, 12, 2, 2);
        gc.setStroke(Color.web("#e7f3f1"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y + 11, 28, 55, 8, 8);
        gc.strokeLine(x + 14, y + 5, x + 14, y + 1);
    }

    private void dibujarBandeja(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#34464b"));
        gc.fillRoundRect(x, y + 5, 100, 57, 8, 8);
        gc.setFill(Color.web("#6e817d"));
        gc.fillRoundRect(x + 8, y + 12, 84, 39, 5, 5);
        gc.setFill(Color.web("#b6774b"));
        gc.fillOval(x + 21, y + 23, 19, 16);
        gc.fillOval(x + 54, y + 28, 24, 14);
        gc.setStroke(Color.web("#c9d5d0"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x + 8, y + 12, 84, 39, 5, 5);
    }

    private void dibujarEquipoGenerico(GraphicsContext gc, double centroX, double y) {
        gc.setFill(Color.web("#b9d0d2"));
        gc.fillRoundRect(centroX - 30, y + 4, 60, 52, 8, 8);
        gc.setFill(Color.web("#3ea99a"));
        gc.fillOval(centroX - 14, y + 18, 28, 28);
    }

    private void dibujarAlmacen(GraphicsContext gc, boolean resaltar) {
        gc.setFill(Color.web("#000000", 0.28));
        gc.fillRoundRect(48, 405, 150, 112, 12, 12);
        gc.setFill(Color.web("#4d6871"));
        gc.fillRoundRect(42, 395, 150, 112, 10, 10);
        gc.setFill(Color.web("#9bb4b5"));
        gc.fillRect(50, 404, 134, 8);
        gc.setFill(Color.web("#355d69"));
        gc.fillRoundRect(56, 421, 55, 76, 6, 6);
        gc.fillRoundRect(121, 421, 55, 76, 6, 6);
        gc.setFill(Color.web("#94dfcf"));
        gc.fillOval(70, 433, 24, 24);
        gc.setFill(Color.web("#dcaf56"));
        gc.fillOval(134, 433, 24, 24);
        gc.setFill(Color.web("#ecf5f0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        gc.fillText(resaltar ? "ALMACÉN · DEJAR" : "ALMACÉN · M", 62, 491);
        if (resaltar) {
            gc.setStroke(Color.web("#79f3d3", 0.96));
            gc.setLineWidth(3);
            gc.strokeRoundRect(38, 391, 158, 121, 13, 13);
            gc.setFill(Color.web("#7df3d2", 0.14));
            gc.fillOval(126, 428, 39, 35);
        }
    }

    private void dibujarProductoEnMano(GraphicsContext gc, Sustancia producto) {
        double x = jugador.getX() + (jugador.getOrientacionX() < 0 ? -27 : 20);
        double y = jugador.getY() - 11;
        Color tono = colorProducto(producto.getId());
        gc.setFill(Color.web("#07151e", 0.7));
        gc.fillRoundRect(x - 2, y - 2, 24, 25, 5, 5);
        gc.setFill(Color.web("#e7f1ea"));
        gc.fillRoundRect(x, y, 20, 21, 4, 4);
        switch (producto.getEstadoMateria()) {
            case SOLIDO -> {
                if (producto.getId().equals("DIAMANTE")) {
                    gc.setFill(Color.web("#bdf7ff"));
                    gc.fillPolygon(new double[]{x + 10, x + 18, x + 14, x + 3},
                            new double[]{y + 2, y + 8, y + 18, y + 12}, 4);
                    gc.setStroke(Color.web("#58b8c7"));
                    gc.strokePolygon(new double[]{x + 10, x + 18, x + 14, x + 3},
                            new double[]{y + 2, y + 8, y + 18, y + 12}, 4);
                } else {
                    gc.setFill(tono);
                    gc.fillOval(x + 3, y + 5, 14, 11);
                    gc.setFill(tono.brighter());
                    gc.fillOval(x + 5, y + 5, 6, 4);
                }
            }
            case LIQUIDO -> {
                gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.8));
                gc.fillRoundRect(x + 3, y + 7, 14, 10, 3, 3);
                gc.setStroke(Color.web("#efffff"));
                gc.strokeRoundRect(x + 3, y + 4, 14, 14, 3, 3);
            }
            case GASEOSO, PLASMA -> {
                gc.setFill(new Color(tono.getRed(), tono.getGreen(), tono.getBlue(), 0.3));
                gc.fillOval(x + 1, y + 1, 18, 18);
                gc.setStroke(tono.brighter());
                gc.strokeOval(x + 1, y + 1, 18, 18);
                gc.setFill(tono);
                gc.fillOval(x + 7, y + 7, 6, 6);
            }
        }
    }

    private void dibujarJugador(GraphicsContext gc) {
        double x = jugador.getX();
        double y = jugador.getY();
        boolean perfil = Math.abs(jugador.getOrientacionX()) > Math.abs(jugador.getOrientacionY());
        boolean frente = jugador.getOrientacionY() > 0;
        boolean perfilDerecho = jugador.getOrientacionX() > 0;
        double paso = jugador.isEnMovimiento() ? (jugador.isPasoAlterno() ? 1 : -1) : 0;
        double balanceo = paso == 0 ? 0 : paso * 3.5;
        double rebote = paso == 0 ? 0 : (jugador.isPasoAlterno() ? -1.5 : 1.5);

        gc.save();
        gc.translate(x, y - rebote);
        gc.setFill(Color.web("#000000", 0.3));
        gc.fillOval(-20, 10 + rebote, 40, 12);
        dibujarPiernasJugador(gc, paso, perfil);
        dibujarBrazosJugador(gc, balanceo);
        if (perfil) {
            gc.save();
            if (!perfilDerecho) gc.scale(-1, 1);
            dibujarPerfilJugador(gc);
            gc.restore();
        } else if (frente) {
            dibujarFrenteJugador(gc);
        } else {
            dibujarEspaldaJugador(gc);
        }
        gc.restore();
    }

    private void dibujarPiernasJugador(GraphicsContext gc, double paso, boolean perfil) {
        gc.setFill(Color.web("#263946"));
        if (perfil) {
            gc.fillRoundRect(-9 + paso * 2, -3, 9, 24, 4, 4);
            gc.fillRoundRect(0 - paso * 2, -1, 9, 22, 4, 4);
            gc.setFill(Color.web("#172832"));
            gc.fillRoundRect(-12 + paso * 2, 16, 14, 7, 4, 4);
            gc.fillRoundRect(-1 - paso * 2, 17, 14, 7, 4, 4);
        } else {
            double zancadaIzquierda = Math.max(0, paso) * 4;
            double zancadaDerecha = Math.max(0, -paso) * 4;
            gc.fillRoundRect(-13 - paso * 1.5, -3, 11, 23 + zancadaIzquierda, 5, 5);
            gc.fillRoundRect(2 + paso * 1.5, -3, 11, 23 + zancadaDerecha, 5, 5);
            gc.setFill(Color.web("#172832"));
            gc.fillRoundRect(-17 - paso * 1.5, 16 + zancadaIzquierda, 17, 7, 4, 4);
            gc.fillRoundRect(0 + paso * 1.5, 16 + zancadaDerecha, 17, 7, 4, 4);
        }
    }

    private void dibujarBrazosJugador(GraphicsContext gc, double balanceo) {
        gc.setStroke(Color.web("#eef5f0"));
        gc.setLineWidth(9);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.strokeLine(-16, -29, -22 - balanceo, -8);
        gc.strokeLine(16, -29, 22 + balanceo, -8);
        gc.setFill(Color.web("#b9e4dc"));
        gc.fillRoundRect(-27 - balanceo, -11, 10, 11, 5, 5);
        gc.fillRoundRect(17 + balanceo, -11, 10, 11, 5, 5);
    }

    private void dibujarFrenteJugador(GraphicsContext gc) {
        dibujarCaraJugador(gc, true);
        gc.setFill(Color.web("#f4f7f2"));
        gc.fillPolygon(new double[]{-22, -15, -8, 0, 8, 15, 22, 18, 14, -14, -18},
                new double[]{-32, -36, -31, -33, -31, -36, -32, 10, 13, 13, 10}, 11);
        gc.setStroke(Color.web("#c2d6d1"));
        gc.setLineWidth(1.2);
        gc.strokeLine(-18, 9, 18, 9);
        gc.setFill(Color.web("#53b9aa"));
        gc.fillPolygon(new double[]{-7, 0, 7, 3, 0, -3},
                new double[]{-30, -22, -30, -8, 7, -8}, 6);
        gc.setStroke(Color.web("#c3d8d2"));
        gc.setLineWidth(1);
        gc.strokeLine(0, -8, 0, 7);
        gc.setFill(Color.web("#ffffff"));
        gc.fillRoundRect(-16, -1, 10, 7, 2, 2);
        gc.fillRoundRect(6, -1, 10, 7, 2, 2);
        gc.setFill(Color.web("#e5bf64"));
        gc.fillRoundRect(-14, -46, 10, 7, 2, 2);
        gc.setFill(Color.web("#56bbc1"));
        gc.fillRoundRect(3, -46, 10, 7, 2, 2);
    }

    private void dibujarEspaldaJugador(GraphicsContext gc) {
        gc.setFill(Color.web("#f1f5ef"));
        gc.fillPolygon(new double[]{-22, -15, -8, 0, 8, 15, 22, 18, 14, -14, -18},
                new double[]{-32, -36, -31, -33, -31, -36, -32, 10, 13, 13, 10}, 11);
        gc.setStroke(Color.web("#b4c9c5"));
        gc.setLineWidth(1.4);
        gc.strokeLine(0, -31, 0, 12);
        gc.strokeLine(-16, 2, 16, 2);
        gc.setFill(Color.web("#d8e8e3"));
        gc.fillPolygon(new double[]{-8, 0, 8, 4, -4},
                new double[]{-33, -25, -33, -18, -18}, 5);
        gc.setFill(Color.web("#50b7a9"));
        gc.fillRoundRect(-7, -4, 14, 9, 3, 3);
        gc.setFill(Color.web("#dcebed"));
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 6));
        gc.fillText("CL", -4.5, 2.5);
        dibujarCaraJugador(gc, false);
    }

    private void dibujarPerfilJugador(GraphicsContext gc) {
        gc.setFill(Color.web("#f2f6f1"));
        gc.fillRoundRect(-13, -34, 27, 47, 8, 8);
        gc.setFill(Color.web("#d9e8e3"));
        gc.fillPolygon(new double[]{-5, 0, 7, 2}, new double[]{-32, -24, -32, -14}, 4);
        gc.setStroke(Color.web("#c0d3ce"));
        gc.setLineWidth(1.2);
        gc.strokeLine(1, -13, 1, 11);
        gc.setFill(Color.web("#ffffff"));
        gc.fillRoundRect(-6, -1, 11, 8, 2, 2);
        gc.setFill(Color.web("#52b8a7"));
        gc.fillRoundRect(2, -5, 4, 12, 2, 2);

        gc.setFill(Color.web("#d7ad86"));
        gc.fillOval(-8, -58, 23, 28);
        gc.setFill(Color.web("#40322c"));
        gc.fillOval(-9, -62, 24, 13);
        gc.fillRoundRect(8, -56, 7, 12, 3, 3);
        gc.setStroke(Color.web("#306c7a"));
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(6, -50, 10, 8, 3, 3);
        gc.setFill(Color.web("#80d7df", 0.6));
        gc.fillRoundRect(7, -49, 8, 6, 2, 2);
        gc.setFill(Color.web("#d7f0f1"));
        gc.fillPolygon(new double[]{8, 17, 8}, new double[]{-39, -36, -32}, 3);
        gc.setFill(Color.web("#b9e4dc"));
        gc.fillRoundRect(5, -12, 10, 10, 5, 5);
    }

    private void dibujarCaraJugador(GraphicsContext gc, boolean mostrarRostro) {
        gc.setFill(Color.web("#d8ad86"));
        gc.fillOval(-14, -59, 28, 30);
        gc.setFill(Color.web("#43352e"));
        gc.fillOval(-15, -63, 30, 17);
        gc.fillRoundRect(-14, -57, 6, 10, 3, 3);
        gc.fillRoundRect(8, -57, 6, 10, 3, 3);
        if (mostrarRostro) {
            gc.setFill(Color.web("#edf9f7", 0.76));
            gc.fillRoundRect(-13, -50, 26, 10, 4, 4);
            gc.setStroke(Color.web("#246879"));
            gc.setLineWidth(2.2);
            gc.strokeRoundRect(-12, -51, 11, 8, 3, 3);
            gc.strokeRoundRect(1, -51, 11, 8, 3, 3);
            gc.strokeLine(-1, -47, 1, -47);
            gc.setFill(Color.web("#d5f0ee"));
            gc.fillRoundRect(-10, -39, 20, 8, 4, 4);
            gc.setStroke(Color.web("#47919a"));
            gc.setLineWidth(1);
            gc.strokeRoundRect(-10, -39, 20, 8, 4, 4);
        } else {
            gc.setStroke(Color.web("#2e6671"));
            gc.setLineWidth(2.5);
            gc.strokeLine(-14, -47, 14, -47);
            gc.setFill(Color.web("#d5f0ee"));
            gc.fillRoundRect(-9, -38, 18, 6, 3, 3);
        }
    }

}
