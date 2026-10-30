package com.chemlab.engine;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Equipo;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.TipoEquipo;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Aplica efectos físicos explícitos y ejecuta únicamente reacciones registradas
 * que coincidan con el equipo, las condiciones y el inventario actuales.
 */
public final class MotorExperimentacionLibre {

    public record ReaccionAplicada(Reaccion reaccion, ResultadoQuimico resultado) {}

    private final CatalogoQuimico catalogo;
    private final FabricaExperimento fabricaExperimento;
    private final MotorQuimico motorQuimico;

    public MotorExperimentacionLibre(CatalogoQuimico catalogo) {
        this.catalogo = Objects.requireNonNull(catalogo, "El catálogo no puede ser null");
        this.fabricaExperimento = new FabricaExperimento(catalogo);
        this.motorQuimico = new MotorQuimico(new ReglaReaccionBasica());
    }

    public String aplicarOperacion(AccionLaboratorio accion, TipoEquipo equipo, Experimento experimento) {
        Objects.requireNonNull(accion, "La acción no puede ser null");
        Objects.requireNonNull(equipo, "El equipo no puede ser null");
        Objects.requireNonNull(experimento, "El experimento no puede ser null");

        CondicionesExperimentales condiciones = experimento.getCondiciones();
        return switch (accion) {
            case CALENTAR -> {
                double incremento = equipo == TipoEquipo.REACTOR || equipo == TipoEquipo.HORNO ? 100 : 25;
                condiciones.cambiarTemperatura(condiciones.getTemperatura() + incremento);
                yield "Temperatura actual: " + condiciones.getTemperatura() + " °C.";
            }
            case ENFRIAR -> {
                double decremento = equipo == TipoEquipo.REACTOR || equipo == TipoEquipo.HORNO ? 100 : 25;
                condiciones.cambiarTemperatura(Math.max(25, condiciones.getTemperatura() - decremento));
                yield "Temperatura actual: " + condiciones.getTemperatura() + " °C.";
            }
            case COMPRIMIR -> {
                if (equipo != TipoEquipo.REACTOR && equipo != TipoEquipo.CAMARA_SELLADA) {
                    yield "Este equipo no permite aplicar presión. No se modificaron las condiciones.";
                }
                condiciones.cambiarPresion(Math.max(1, condiciones.getPresion() * 2));
                yield "Presión actual: " + condiciones.getPresion() + " atm.";
            }
            case DESCOMPRIMIR -> {
                if (equipo != TipoEquipo.REACTOR && equipo != TipoEquipo.CAMARA_SELLADA) {
                    yield "Este equipo no permite regular presión. No se modificaron las condiciones.";
                }
                condiciones.cambiarPresion(Math.max(0.01, condiciones.getPresion() / 2));
                yield "Presión actual: " + condiciones.getPresion() + " atm.";
            }
            case INICIAR_PLASMA -> {
                condiciones.cambiarAtmosfera(Atmosfera.VACIO);
                yield "Plasma iniciado en atmósfera de vacío.";
            }
            default -> "Operación aplicada en " + equipo + ".";
        };
    }

    public Optional<ReaccionAplicada> evaluarReaccion(
            AccionLaboratorio accion,
            TipoEquipo equipo,
            Experimento experimento
    ) {
        return evaluarReaccion(accion, equipo, experimento, null);
    }

    public Optional<ReaccionAplicada> evaluarReaccion(
            AccionLaboratorio accion,
            TipoEquipo equipo,
            Experimento experimento,
            Reaccion reaccionPreferida
    ) {
        if (!esAccionDeProceso(accion)) {
            return Optional.empty();
        }

        List<Reaccion> candidatas = new ArrayList<>();
        if (reaccionPreferida != null) {
            candidatas.add(reaccionPreferida);
        }
        catalogo.obtenerReacciones().stream()
                .filter(candidata -> reaccionPreferida == null
                        || !candidata.getId().equals(reaccionPreferida.getId()))
                .forEach(candidatas::add);

        for (Reaccion candidata : candidatas) {
            Experimento configuracion = fabricaExperimento.crearExperimentoParaReaccion(candidata);
            if (configuracion.getEquipo().getTipo() != equipo
                    || !candidata.getCondicionesRequeridas().cumple(
                    experimento.getCondiciones(), experimento.getInventario())) {
                continue;
            }

            Experimento ensayo = new Experimento(
                    experimento.getId(),
                    experimento.getNombre(),
                    configuracion.getObjetivo(),
                    experimento.getInventario(),
                    new Equipo(
                            configuracion.getEquipo().getId(),
                            configuracion.getEquipo().getNombre(),
                            equipo,
                            configuracion.getEquipo().getCapacidad()
                    ),
                    experimento.getCondiciones()
            );
            ResultadoQuimico resultado = motorQuimico.ejecutar(candidata, ensayo);
            if (resultado.isExitoso() && !resultado.getProductos().isEmpty()) {
                return Optional.of(new ReaccionAplicada(candidata, resultado));
            }
        }
        return Optional.empty();
    }

    private boolean esAccionDeProceso(AccionLaboratorio accion) {
        return accion == AccionLaboratorio.CALENTAR
                || accion == AccionLaboratorio.ENFRIAR
                || accion == AccionLaboratorio.COMPRIMIR
                || accion == AccionLaboratorio.DESCOMPRIMIR
                || accion == AccionLaboratorio.MEZCLAR
                || accion == AccionLaboratorio.ACTIVAR_IGNICION
                || accion == AccionLaboratorio.ESPERAR
                || accion == AccionLaboratorio.INICIAR_PLASMA
                || accion == AccionLaboratorio.FINALIZAR;
    }
}
