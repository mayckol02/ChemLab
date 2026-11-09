package com.chemlab.game;

public enum ModoJuego {
    TUTORIAL,
    DIFICIL;

    public boolean permiteMisiones() {
        return this == DIFICIL;
    }
}
