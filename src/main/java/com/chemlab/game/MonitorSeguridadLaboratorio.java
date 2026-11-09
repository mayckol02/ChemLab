package com.chemlab.game;

import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.CondicionesRequeridas;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Genera observaciones educativas; sus umbrales no son límites de seguridad reales. */
public final class MonitorSeguridadLaboratorio {

    private static final double TEMPERATURA_ELEVADA_C = 60.0;
    private static final double PRESION_ELEVADA_ATM = 2.0;

    public record Informe(String estadoParametros, List<String> advertencias) {
        public Informe {
            Objects.requireNonNull(estadoParametros, "El estado de parámetros es obligatorio");
            advertencias = List.copyOf(advertencias);
        }
    }

    private MonitorSeguridadLaboratorio() {}

    public static Informe analizar(
            CondicionesExperimentales condiciones,
            CondicionesRequeridas requeridas,
            AccionLaboratorio accion
    ) {
        Objects.requireNonNull(condiciones, "Las condiciones actuales son obligatorias");
        Objects.requireNonNull(requeridas, "Las condiciones requeridas son obligatorias");

        List<String> desviaciones = new ArrayList<>();
        if (condiciones.getTemperatura() < requeridas.getTempMin()
                || condiciones.getTemperatura() > requeridas.getTempMax()) {
            desviaciones.add(String.format(Locale.ROOT,
                    "temperatura fuera del rango de reacción (%.0f–%.0f °C)",
                    requeridas.getTempMin(), requeridas.getTempMax()));
        }
        if (condiciones.getPresion() < requeridas.getPresionMin()
                || condiciones.getPresion() > requeridas.getPresionMax()) {
            desviaciones.add(String.format(Locale.ROOT,
                    "presión fuera del rango de reacción (%.3f–%.3f atm)",
                    requeridas.getPresionMin(), requeridas.getPresionMax()));
        }
        if (requeridas.getAtmosferaRequerida() != null
                && condiciones.getAtmosfera() != requeridas.getAtmosferaRequerida()) {
            desviaciones.add("atmósfera distinta de la requerida ("
                    + requeridas.getAtmosferaRequerida() + ")");
        }

        String estadoParametros = desviaciones.isEmpty()
                ? "Parámetros dentro del rango registrado para la reacción."
                : "Revisar parámetros: " + String.join("; ", desviaciones) + ".";

        List<String> advertencias = new ArrayList<>();
        if (condiciones.getTemperatura() >= TEMPERATURA_ELEVADA_C) {
            advertencias.add("Temperatura elevada en la simulación; evita manipular la muestra mientras el equipo está activo.");
        }
        if (condiciones.getPresion() > PRESION_ELEVADA_ATM) {
            advertencias.add("Presión elevada en la simulación; verifica el estado del equipo antes de continuar.");
        }
        if (condiciones.getAtmosfera() == Atmosfera.OXIGENO) {
            advertencias.add("Atmósfera rica en oxígeno: evita fuentes de ignición no previstas por el protocolo.");
        } else if (condiciones.getAtmosfera() == Atmosfera.CLORO) {
            advertencias.add("Atmósfera de cloro en la simulación: mantén la muestra dentro del equipo.");
        }
        if (accion == AccionLaboratorio.ACTIVAR_IGNICION
                && (condiciones.getAtmosfera() == Atmosfera.OXIGENO
                || condiciones.getAtmosfera() == Atmosfera.CLORO)) {
            advertencias.add("La ignición con esta atmósfera activa una alerta educativa de riesgo; revisa el protocolo antes de seguir.");
        }

        return new Informe(estadoParametros, advertencias);
    }
}
