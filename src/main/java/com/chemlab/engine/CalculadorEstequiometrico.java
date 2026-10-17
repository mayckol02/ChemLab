package com.chemlab.engine;

import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import com.chemlab.model.value.EvaluacionReaccion;
import com.chemlab.model.value.ResultadoEstequiometrico;
import com.chemlab.service.ConversorQuimico;
import com.chemlab.model.enums.TipoFalloReaccion;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CalculadorEstequiometrico {

    private final ConversorQuimico conversorQuimico;

    public CalculadorEstequiometrico(
            ConversorQuimico conversorQuimico
    ) {
        this.conversorQuimico =
                Objects.requireNonNull(
                        conversorQuimico,
                        "El conversor químico no puede ser null"
                );
    }

    public EvaluacionReaccion evaluar(
            Reaccion reaccion,
            Inventario inventario,
            CondicionesExperimentales condiciones
    ) {
        Objects.requireNonNull(
                reaccion,
                "La reacción no puede ser null"
        );

        Objects.requireNonNull(
                inventario,
                "El inventario no puede ser null"
        );

        Objects.requireNonNull(
                condiciones,
                "Las condiciones no pueden ser null"
        );

        EvaluacionReaccion evalCondiciones = evaluarCondiciones(reaccion, inventario, condiciones);
        if (evalCondiciones != null) {
            return evalCondiciones;
        }

        Sustancia reactivoAusente =
                obtenerReactivoAusente(
                        reaccion,
                        inventario
                );

        if (reactivoAusente != null) {
            return EvaluacionReaccion.fallida(
                    TipoFalloReaccion.REACTIVO_INEXISTENTE,
                    "El inventario no tiene el reactivo requerido: "
                            + reactivoAusente.getNombre()
            );
        }

        double avanceMaximo = calcularAvanceMaximo(
                reaccion,
                inventario
        );

        if (avanceMaximo <= 0) {
            return EvaluacionReaccion.fallida(
                    TipoFalloReaccion.CANTIDAD_INSUFICIENTE,
                    "No hay cantidad suficiente para ejecutar la reacción"
            );
        }

        Sustancia reactivoLimitante =
                obtenerReactivoLimitante(
                        reaccion,
                        inventario
                );

        return crearEvaluacionExitosa(
                reaccion,
                inventario,
                reactivoLimitante,
                avanceMaximo
        );
    }

    public EvaluacionReaccion evaluar(
            Reaccion reaccion,
            Inventario inventario,
            CondicionesExperimentales condiciones,
            double avanceSolicitado
    ) {
        Objects.requireNonNull(
                reaccion,
                "La reacción no puede ser null"
        );

        Objects.requireNonNull(
                inventario,
                "El inventario no puede ser null"
        );

        Objects.requireNonNull(
                condiciones,
                "Las condiciones no pueden ser null"
        );

        EvaluacionReaccion evalCondiciones = evaluarCondiciones(reaccion, inventario, condiciones);
        if (evalCondiciones != null) {
            return evalCondiciones;
        }

        if (avanceSolicitado <= 0
                || Double.isNaN(avanceSolicitado)
                || Double.isInfinite(avanceSolicitado)) {

            throw new IllegalArgumentException(
                    "El avance solicitado debe ser un número positivo y finito"
            );
        }

        Sustancia reactivoAusente =
                obtenerReactivoAusente(
                        reaccion,
                        inventario
                );

        if (reactivoAusente != null) {
            return EvaluacionReaccion.fallida(
                    TipoFalloReaccion.REACTIVO_INEXISTENTE,
                    "El inventario no tiene el reactivo requerido: "
                            + reactivoAusente.getNombre()
            );
        }

        double avanceMaximo = calcularAvanceMaximo(
                reaccion,
                inventario
        );

        Sustancia reactivoLimitante =
                obtenerReactivoLimitante(
                        reaccion,
                        inventario
                );

        if (avanceSolicitado > avanceMaximo) {
            return EvaluacionReaccion.fallida(
                    TipoFalloReaccion.CANTIDAD_INSUFICIENTE,
                    "No hay cantidad suficiente de "
                            + reactivoLimitante.getNombre()
                            + " para el avance solicitado"
            );
        }

        return crearEvaluacionExitosa(
                reaccion,
                inventario,
                reactivoLimitante,
                avanceSolicitado
        );
    }

    public double calcularAvanceMaximo(
            Reaccion reaccion,
            Inventario inventario
    ) {
        Objects.requireNonNull(
                reaccion,
                "La reacción no puede ser null"
        );

        Objects.requireNonNull(
                inventario,
                "El inventario no puede ser null"
        );

        double avanceMaximo =
                Double.POSITIVE_INFINITY;

        for (Map.Entry<Sustancia, Integer> entrada
                : reaccion.getReactivos().entrySet()) {

            Sustancia reactivo =
                    entrada.getKey();

            int coeficiente =
                    entrada.getValue();

            if (!inventario.contiene(reactivo)) {
                return 0;
            }

            Cantidad cantidadDisponible =
                    inventario.obtenerCantidadCompleta(
                            reactivo
                    );

            Cantidad molesDisponibles =
                    conversorQuimico.convertirAMoles(
                            cantidadDisponible,
                            reactivo
                    );

            double avance =
                    molesDisponibles.getValor()
                            / coeficiente;

            avanceMaximo =
                    Math.min(
                            avanceMaximo,
                            avance
                    );
        }

        return avanceMaximo;
    }

    public Sustancia obtenerReactivoLimitante(
            Reaccion reaccion,
            Inventario inventario
    ) {
        Objects.requireNonNull(
                reaccion,
                "La reacción no puede ser null"
        );

        Objects.requireNonNull(
                inventario,
                "El inventario no puede ser null"
        );

        Sustancia reactivoLimitante = null;

        double avanceMinimo =
                Double.POSITIVE_INFINITY;

        for (Map.Entry<Sustancia, Integer> entrada
                : reaccion.getReactivos().entrySet()) {

            Sustancia reactivo =
                    entrada.getKey();

            int coeficiente =
                    entrada.getValue();

            if (!inventario.contiene(reactivo)) {
                return null;
            }

            Cantidad cantidadDisponible =
                    inventario.obtenerCantidadCompleta(
                            reactivo
                    );

            Cantidad molesDisponibles =
                    conversorQuimico.convertirAMoles(
                            cantidadDisponible,
                            reactivo
                    );

            double avance =
                    molesDisponibles.getValor()
                            / coeficiente;

            if (avance < avanceMinimo) {
                avanceMinimo = avance;
                reactivoLimitante = reactivo;
            }
        }

        return reactivoLimitante;
    }

    private EvaluacionReaccion evaluarCondiciones(
            Reaccion reaccion,
            Inventario inventario,
            CondicionesExperimentales condiciones
    ) {
        var req = reaccion.getCondicionesRequeridas();
        if (req == null) {
            return null;
        }

        if (condiciones.getTemperatura() < req.getTempMin() || condiciones.getTemperatura() > req.getTempMax()) {
            return EvaluacionReaccion.fallida(TipoFalloReaccion.CONDICION_TEMPERATURA, "Temperatura fuera de rango para la reacción.");
        }

        if (condiciones.getPresion() < req.getPresionMin() || condiciones.getPresion() > req.getPresionMax()) {
            return EvaluacionReaccion.fallida(TipoFalloReaccion.CONDICION_PRESION, "Presión fuera de rango para la reacción.");
        }

        if (req.getAtmosferaRequerida() != null && condiciones.getAtmosfera() != req.getAtmosferaRequerida()) {
            return EvaluacionReaccion.fallida(TipoFalloReaccion.CONDICION_ATMOSFERA, "Atmósfera incorrecta para la reacción.");
        }

        for (String catId : req.getCatalizadoresRequeridos()) {
            boolean encontrado = false;
            for (Sustancia sust : inventario.obtenerCantidades().keySet()) {
                if (sust.getId().equals(catId)) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                return EvaluacionReaccion.fallida(TipoFalloReaccion.CONDICION_CATALIZADOR, "Falta catalizador requerido: " + catId);
            }
        }

        return null;
    }

    private Sustancia obtenerReactivoAusente(
            Reaccion reaccion,
            Inventario inventario
    ) {
        for (Sustancia reactivo
                : reaccion.getReactivos().keySet()) {

            if (!inventario.contiene(reactivo)) {
                return reactivo;
            }
        }

        return null;
    }

    private EvaluacionReaccion crearEvaluacionExitosa(
            Reaccion reaccion,
            Inventario inventario,
            Sustancia reactivoLimitante,
            double avance
    ) {
        Map<Sustancia, Cantidad> consumidos =
                calcularReactivosConsumidos(
                        reaccion,
                        inventario,
                        avance
                );

        ResultadoEstequiometrico resultado =
                new ResultadoEstequiometrico(
                        reactivoLimitante,
                        avance,
                        consumidos
                );

        return EvaluacionReaccion.exitosa(resultado);
    }

    private Map<Sustancia, Cantidad> calcularReactivosConsumidos(
            Reaccion reaccion,
            Inventario inventario,
            double avance
    ) {
        Map<Sustancia, Cantidad> consumidos =
                new HashMap<>();

        for (Map.Entry<Sustancia, Integer> entrada
                : reaccion.getReactivos().entrySet()) {

            Sustancia reactivo =
                    entrada.getKey();

            int coeficiente =
                    entrada.getValue();

            double molesConsumidos =
                    coeficiente * avance;

            Cantidad cantidadEnMoles =
                    new Cantidad(
                            molesConsumidos,
                            UnidadCantidad.MOLES
                    );

            UnidadCantidad unidadOriginal =
                    inventario.obtenerUnidad(
                            reactivo
                    );

            Cantidad cantidadConsumida =
                    conversorQuimico.convertirDesdeMoles(
                            cantidadEnMoles,
                            reactivo,
                            unidadOriginal
                    );

            consumidos.put(
                    reactivo,
                    cantidadConsumida
            );
        }

        return consumidos;
    }

    public ResultadoEstequiometrico calcular(
            Reaccion reaccion,
            Inventario inventario,
            CondicionesExperimentales condiciones
    ) {
        EvaluacionReaccion evaluacion =
                evaluar(
                        reaccion,
                        inventario,
                        condiciones
                );

        if (!evaluacion.esPosible()) {
            return null;
        }

        return evaluacion.getResultadoEstequiometrico();
    }
}