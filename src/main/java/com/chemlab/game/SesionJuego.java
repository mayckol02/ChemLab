package com.chemlab.game;

import com.chemlab.automata.EjecutorAutomataQuimico;
import com.chemlab.automata.ResultadoAccion;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.entity.CuadernoLaboratorio;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.RegistroExperimento;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.enums.AccionLaboratorio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Coordinador principal de la sesión de juego.
 *
 * <p>Orquesta la interacción entre {@link Experimento}, el
 * {@link com.chemlab.automata.AutomataQuimico} y el
 * {@link com.chemlab.engine.MotorQuimico} siguiendo
 * el patrón de casos de uso: un único punto de entrada para
 * cada acción del jugador.</p>
 *
 * <p>La UI sólo habla con esta clase; nunca modifica el dominio
 * directamente.</p>
 *
 * <p>Incorpora un {@link CuadernoLaboratorio} que persiste el progreso
 * del jugador a lo largo de múltiples experimentos dentro de la misma
 * sesión de juego.</p>
 */
public final class SesionJuego {

    private final CatalogoQuimico catalogo;
    private final EjecutorAutomataQuimico ejecutorAutomata;
    private final CuadernoLaboratorio cuaderno;

    private final List<ObservadorSesion> observadores = new ArrayList<>();

    private Experimento experimentoActual;
    private Reaccion reaccionActual;
    private boolean activa;

    /** Registro de las acciones ejecutadas en el experimento en curso. */
    private final Map<String, String> accionesDelExperimento = new LinkedHashMap<>();
    private final List<RegistroExperimento.PasoTransicion> pasosDelExperimento = new ArrayList<>();

    public SesionJuego(
            CatalogoQuimico catalogo,
            EjecutorAutomataQuimico ejecutorAutomata
    ) {
        this.catalogo = Objects.requireNonNull(
                catalogo,
                "El catálogo no puede ser null"
        );

        this.ejecutorAutomata = Objects.requireNonNull(
                ejecutorAutomata,
                "El ejecutor de autómata no puede ser null"
        );

        this.cuaderno = new CuadernoLaboratorio();
        this.activa = false;
    }

    // ---------------------------------------------------------------
    // Registro de observadores (patrón Observer)
    // ---------------------------------------------------------------

    public void agregarObservador(ObservadorSesion observador) {
        Objects.requireNonNull(observador, "El observador no puede ser null");
        observadores.add(observador);
    }

    public void quitarObservador(ObservadorSesion observador) {
        observadores.remove(observador);
    }

    public List<ObservadorSesion> getObservadores() {
        return Collections.unmodifiableList(observadores);
    }

    // ---------------------------------------------------------------
    // Caso de uso: iniciar experimento
    // ---------------------------------------------------------------

    /**
     * Inicia la sesión con el experimento y la reacción dados.
     *
     * @throws IllegalStateException si ya hay una sesión activa.
     */
    public void iniciar(
            Experimento experimento,
            Reaccion reaccion,
            com.chemlab.automata.AutomataQuimico automata
    ) {
        if (activa) {
            throw new IllegalStateException(
                    "Ya existe una sesión activa. "
                            + "Finalice la sesión actual antes de iniciar una nueva."
            );
        }

        this.experimentoActual = Objects.requireNonNull(
                experimento,
                "El experimento no puede ser null"
        );

        this.reaccionActual = Objects.requireNonNull(
                reaccion,
                "La reacción no puede ser null"
        );

        accionesDelExperimento.clear();
        pasosDelExperimento.clear();
        ejecutorAutomata.setAutomataQuimico(automata);
        automata.reiniciar();
        experimento.iniciar();
        this.activa = true;

        for (ObservadorSesion obs : observadores) {
            obs.onExperimentoIniciado(experimento);
        }
    }

    // ---------------------------------------------------------------
    // Caso de uso: ejecutar acción del jugador
    // ---------------------------------------------------------------

    /**
     * Aplica la acción del jugador al autómata y al experimento.
     *
     * @return resultado de la acción con el nuevo estado del autómata.
     * @throws IllegalStateException si no hay sesión activa.
     */
    public ResultadoAccion ejecutarAccion(AccionLaboratorio accion) {
        verificarSesionActiva();

        Objects.requireNonNull(
                accion,
                "La acción no puede ser null"
        );

        String estadoPrevio = ejecutorAutomata.getAutomataQuimico().getEstadoActual().getNombre();

        ResultadoAccion resultadoAccion = ejecutorAutomata.ejecutar(
                accion,
                reaccionActual,
                experimentoActual
        );

        String estadoPosterior = resultadoAccion.getEstadoActual().getNombre();

        // Registrar acción en el log de acciones del experimento actual
        accionesDelExperimento.put(
                accion.name(),
                estadoPosterior
        );
        pasosDelExperimento.add(new RegistroExperimento.PasoTransicion(
                estadoPrevio,
                accion.name(),
                estadoPosterior,
                resultadoAccion.isExito()
        ));

        for (ObservadorSesion obs : observadores) {
            obs.onAccionEjecutada(resultadoAccion);
        }

        if (resultadoAccion.isCompletado()) {
            ResultadoQuimico resultado = resultadoAccion
                    .getResultadoQuimicoOpcional()
                    .orElse(null);

            if (resultado != null) {
                experimentoActual.completar(resultado);

                // Registrar descubrimientos en el cuaderno
                resultado.getProductos().keySet().forEach(
                        sustancia -> cuaderno.registrarDescubrimiento(sustancia)
                );
            }

            activa = false;

            var automata = ejecutorAutomata.getAutomataQuimico();
            String estadoInicialNombre = automata.getEstadoInicial().getNombre();
            List<String> finalesNombres = automata.getEstadosFinales().stream()
                    .map(com.chemlab.automata.EstadoQuimico::getNombre)
                    .toList();
            List<String> todosNombres = automata.getEstados().stream()
                    .map(com.chemlab.automata.EstadoQuimico::getNombre)
                    .toList();

            // Guardar historial en el cuaderno
            cuaderno.agregarRegistro(new RegistroExperimento(
                    experimentoActual.getNombre(),
                    reaccionActual.getId(),
                    Map.copyOf(accionesDelExperimento),
                    resultado,
                    true,
                    estadoInicialNombre,
                    finalesNombres,
                    todosNombres,
                    List.copyOf(pasosDelExperimento)
            ));

            for (ObservadorSesion obs : observadores) {
                obs.onExperimentoCompletado(experimentoActual);
            }
        }

        return resultadoAccion;
    }

    // ---------------------------------------------------------------
    // Caso de uso: abandonar / reintentar
    // ---------------------------------------------------------------

    /**
     * El jugador abandona el experimento: marca como fallido y cierra sesión.
     *
     * @throws IllegalStateException si no hay sesión activa.
     */
    public void abandonar() {
        verificarSesionActiva();
        experimentoActual.fallar();
        activa = false;

        var automata = ejecutorAutomata.getAutomataQuimico();
        String estadoInicialNombre = automata != null && automata.getEstadoInicial() != null
                ? automata.getEstadoInicial().getNombre()
                : "q0";
        List<String> finalesNombres = automata != null
                ? automata.getEstadosFinales().stream().map(com.chemlab.automata.EstadoQuimico::getNombre).toList()
                : List.of();
        List<String> todosNombres = automata != null
                ? automata.getEstados().stream().map(com.chemlab.automata.EstadoQuimico::getNombre).toList()
                : List.of();

        // Guardar el intento fallido en el cuaderno
        cuaderno.agregarRegistro(new RegistroExperimento(
                experimentoActual.getNombre(),
                reaccionActual.getId(),
                Map.copyOf(accionesDelExperimento),
                null,
                false,
                estadoInicialNombre,
                finalesNombres,
                todosNombres,
                List.copyOf(pasosDelExperimento)
        ));

        for (ObservadorSesion obs : observadores) {
            obs.onExperimentoAbandonado(experimentoActual);
        }
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    public boolean isActiva() {
        return activa;
    }

    public Optional<Experimento> getExperimento() {
        return Optional.ofNullable(experimentoActual);
    }

    public Optional<Reaccion> getReaccion() {
        return Optional.ofNullable(reaccionActual);
    }

    public List<Reaccion> obtenerReacciones() {
        return catalogo.obtenerReacciones();
    }

    public com.chemlab.automata.AutomataQuimico getAutomataActual() {
        return ejecutorAutomata.getAutomataQuimico();
    }

    /**
     * Devuelve el cuaderno de laboratorio con el progreso del jugador.
     * El cuaderno persiste a lo largo de toda la sesión de juego.
     */
    public CuadernoLaboratorio getCuaderno() {
        return cuaderno;
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private void verificarSesionActiva() {
        if (!activa) {
            throw new IllegalStateException(
                    "No hay ninguna sesión activa"
            );
        }
    }
}
