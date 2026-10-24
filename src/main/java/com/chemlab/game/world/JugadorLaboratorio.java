package com.chemlab.game.world;

/** Estado de movimiento del personaje del laboratorio. */
public final class JugadorLaboratorio {

    private static final double VELOCIDAD = 220;

    private double x = 625;
    private double y = 600;
    private double tiempoCaminando;
    private boolean enMovimiento;
    private double orientacionX;
    private double orientacionY = -1;

    public void mover(double direccionX, double direccionY, double deltaSegundos, MapaLaboratorio mapa) {
        if (direccionX == 0 && direccionY == 0) {
            enMovimiento = false;
            return;
        }

        double longitud = Math.hypot(direccionX, direccionY);
        double avanceX = direccionX / longitud * VELOCIDAD * deltaSegundos;
        double avanceY = direccionY / longitud * VELOCIDAD * deltaSegundos;
        orientacionX = direccionX / longitud;
        orientacionY = direccionY / longitud;
        tiempoCaminando += deltaSegundos;
        double xAnterior = x;
        double yAnterior = y;

        if (!mapa.hayColision(x + avanceX, y)) {
            x += avanceX;
        }
        if (!mapa.hayColision(x, y + avanceY)) {
            y += avanceY;
        }
        enMovimiento = x != xAnterior || y != yAnterior;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isEnMovimiento() {
        return enMovimiento;
    }

    public boolean isPasoAlterno() {
        return enMovimiento && ((int) (tiempoCaminando * 8) % 2 == 0);
    }

    public double getOrientacionX() {
        return orientacionX;
    }

    public double getOrientacionY() {
        return orientacionY;
    }
}
