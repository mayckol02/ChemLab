package com.chemlab.model.entity;

import com.chemlab.model.enums.Atmosfera;

import java.util.Objects;

public class CondicionesExperimentales {

    private double temperatura;
    private double presion;
    private double ph;
    private Atmosfera atmosfera;

    public CondicionesExperimentales(
            double temperatura,
            double presion,
            double ph,
            Atmosfera atmosfera
    ) {
        validarPresion(presion);
        validarPh(ph);

        this.temperatura = temperatura;
        this.presion = presion;
        this.ph = ph;
        this.atmosfera = Objects.requireNonNull(
                atmosfera,
                "La atmósfera no puede ser null"
        );
    }

    public double getTemperatura() {
        return temperatura;
    }

    public double getPresion() {
        return presion;
    }

    public double getPh() {
        return ph;
    }

    public Atmosfera getAtmosfera() {
        return atmosfera;
    }

    public void cambiarTemperatura(double nuevaTemperatura) {
        this.temperatura = nuevaTemperatura;
    }

    public void cambiarPresion(double nuevaPresion) {
        validarPresion(nuevaPresion);
        this.presion = nuevaPresion;
    }

    public void cambiarPh(double nuevoPh) {
        validarPh(nuevoPh);
        this.ph = nuevoPh;
    }

    public void cambiarAtmosfera(Atmosfera nuevaAtmosfera) {
        this.atmosfera = Objects.requireNonNull(
                nuevaAtmosfera,
                "La atmósfera no puede ser null"
        );
    }

    private void validarPresion(double presion) {
        if (presion < 0) {
            throw new IllegalArgumentException(
                    "La presión no puede ser negativa"
            );
        }
    }

    private void validarPh(double ph) {
        if (ph < 0 || ph > 14) {
            throw new IllegalArgumentException(
                    "El pH debe estar entre 0 y 14"
            );
        }
    }
}