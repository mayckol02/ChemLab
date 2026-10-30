package com.chemlab.game;

import com.chemlab.automata.EjecutorAutomataQuimico;
import com.chemlab.automata.ResultadoAccion;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.engine.MotorExperimentacionLibre;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.model.entity.CuadernoLaboratorio;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.RegistroExperimento;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.EstadoRegistroExperimento;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import com.chemlab.game.procedure.PlanificadorEstacionesProcedimiento;
import com.chemlab.game.procedure.ProtocoloProcedimiento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
    private final MotorExperimentacionLibre motorLibre;
    private final PlanificadorEstacionesProcedimiento planificadorEstaciones =
            new PlanificadorEstacionesProcedimiento();

    private final List<ObservadorSesion> observadores = new ArrayList<>();
    private final List<String> eventosDelExperimento = new ArrayList<>();
    private final List<String> pistasSolicitadas = new ArrayList<>();
    private final Map<Sustancia, Cantidad> productosProducidos = new LinkedHashMap<>();
    private final Set<String> reaccionesProducidas = new LinkedHashSet<>();

    private Experimento experimentoActual;
    private Reaccion reaccionActual;
    private boolean activa;
    private ModoJuego modoJuego = ModoJuego.TUTORIAL;
    private TipoEquipo equipoActual;
    private int cantidadPistasSolicitadas;

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
        this.motorLibre = new MotorExperimentacionLibre(catalogo);
        this.activa = false;
    }

    public void seleccionarModo(ModoJuego modo) {
        Objects.requireNonNull(modo, "El modo no puede ser null");
        if (modo == modoJuego) {
            return;
        }
        if (activa) {
            abandonar();
        }
        this.modoJuego = modo;
    }

    public ModoJuego getModoJuego() {
        return modoJuego;
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
        eventosDelExperimento.clear();
        pistasSolicitadas.clear();
        productosProducidos.clear();
        reaccionesProducidas.clear();
        cantidadPistasSolicitadas = 0;
        equipoActual = null;
        ejecutorAutomata.setAutomataQuimico(automata);
        automata.reiniciar();
        experimento.iniciar();
        this.activa = true;
        if (modoJuego == ModoJuego.DIFICIL) {
            eventosDelExperimento.add("Inicio · " + experimento.getCondiciones().getTemperatura()
                    + " °C · " + experimento.getCondiciones().getPresion() + " atm · "
                    + experimento.getCondiciones().getAtmosfera());
        }

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

        if (modoJuego == ModoJuego.DIFICIL) {
            return ejecutarAccionLibre(accion);
        }

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
                resultadoAccion.isExitoso()
        ));

        for (ObservadorSesion obs : observadores) {
            obs.onAccionEjecutada(resultadoAccion);
        }

        if (!resultadoAccion.isCompletado()
                && ejecutorAutomata.getAutomataQuimico().esEstadoFinalActual()) {
            finalizarTutorialFallido(resultadoAccion.getMensaje());
            return resultadoAccion;
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

    private void finalizarTutorialFallido(String motivo) {
        experimentoActual.fallar();
        activa = false;
        var automata = ejecutorAutomata.getAutomataQuimico();
        cuaderno.agregarRegistro(new RegistroExperimento(
                experimentoActual.getNombre(),
                reaccionActual.getId(),
                Map.copyOf(accionesDelExperimento),
                null,
                false,
                automata.getEstadoInicial().getNombre(),
                automata.getEstadosFinales().stream()
                        .map(com.chemlab.automata.EstadoQuimico::getNombre)
                        .toList(),
                automata.getEstados().stream()
                        .map(com.chemlab.automata.EstadoQuimico::getNombre)
                        .toList(),
                List.copyOf(pasosDelExperimento),
                ModoJuego.TUTORIAL,
                EstadoRegistroExperimento.FALLIDO,
                motivo,
                List.of("Finalización fallida · " + motivo),
                List.of(),
                experimentoActual.getInventario().obtenerCantidades()
        ));
        for (ObservadorSesion obs : observadores) {
            obs.onExperimentoFallido(experimentoActual, motivo);
        }
    }

    private ResultadoAccion ejecutarAccionLibre(AccionLaboratorio accion) {
        if (equipoActual == null) {
            throw new IllegalStateException("Entra a un puesto de trabajo antes de ejecutar una operación.");
        }

        var automata = ejecutorAutomata.getAutomataQuimico();
        var estado = automata.getEstadoActual();
        String estadoPrevio = estado.getNombre();
        boolean equipoPermiteAccion = OperacionesEquipo.disponibles(equipoActual).contains(accion);
        String efectoFisico = equipoPermiteAccion
                ? motorLibre.aplicarOperacion(accion, equipoActual, experimentoActual)
                : "El equipo no dispone de esa operación.";
        boolean transicionPermitida = equipoPermiteAccion
                && automata.puedeTransicionar(
                accion,
                experimentoActual.getCondiciones(),
                experimentoActual.getInventario()
        );
        if (transicionPermitida) {
            TipoEquipo equipoEsperado = planificadorEstaciones.equipoRequerido(
                    reaccionActual.getId(),
                    estado.getId(),
                    accion,
                    experimentoActual.getEquipo().getTipo()
            );
            transicionPermitida = equipoEsperado == equipoActual;
        }

        String estadoPosterior = estadoPrevio;
        if (transicionPermitida) {
            estadoPosterior = automata.transicionar(
                    accion,
                    experimentoActual.getCondiciones(),
                    experimentoActual.getInventario()
            ).getNombre();
        }

        Optional<MotorExperimentacionLibre.ReaccionAplicada> reaccionAplicada =
                equipoPermiteAccion
                        ? motorLibre.evaluarReaccion(
                                accion, equipoActual, experimentoActual, reaccionActual)
                        : Optional.empty();
        reaccionAplicada.ifPresent(aplicada -> {
            aplicada.resultado().getProductos().forEach((sustancia, cantidad) ->
                    productosProducidos.merge(sustancia, cantidad, SesionJuego::sumarCantidades));
            reaccionesProducidas.add(aplicada.reaccion().getId());
            aplicada.resultado().getProductos().keySet().forEach(cuaderno::registrarDescubrimiento);
            eventosDelExperimento.add("Transformación · " + aplicada.reaccion().getId()
                    + " · productos: " + aplicada.resultado().getProductos());
        });

        accionesDelExperimento.put(accion.name(), estadoPosterior);
        pasosDelExperimento.add(new RegistroExperimento.PasoTransicion(
                estadoPrevio,
                accion.name(),
                estadoPosterior,
                transicionPermitida
        ));
        eventosDelExperimento.add("Operación · " + accion + " · " + equipoActual
                + " · " + efectoFisico + " · " + describirCondicionesActuales());

        String mensajeQuimico = reaccionAplicada
                .map(aplicada -> " Reacción definida: " + aplicada.reaccion().getNombre()
                        + "; se formaron " + aplicada.resultado().getProductos().keySet() + ".")
                .orElse(" No hay una transformación definida para esta combinación de sustancias, equipo y condiciones; no se generaron productos.");

        if (accion == AccionLaboratorio.FINALIZAR && !automata.esEstadoFinalActual()) {
            ResultadoAccion resultado = ResultadoAccion.fallo(
                    automata.getEstadoActual(),
                    "Experimento finalizado antes de validar el procedimiento." + mensajeQuimico
            );
            registrarAccion(resultado);
            finalizarDificil(estadoFinalParcial(), "El jugador finalizó el experimento antes de alcanzar un estado válido.");
            return resultado;
        }

        if (!equipoPermiteAccion || !transicionPermitida) {
            ResultadoAccion resultado = ResultadoAccion.fallo(
                    automata.getEstadoActual(),
                    (equipoPermiteAccion
                            ? "El autómata no valida esta acción, sus condiciones o el puesto utilizado."
                            : "Esta operación no está disponible en el equipo.")
                            + " La interacción quedó registrada y el experimento puede continuar."
                            + mensajeQuimico
            );
            registrarAccion(resultado);
            return resultado;
        }

        if (automata.esEstadoFinalActual()) {
            if (!reaccionesProducidas.contains(reaccionActual.getId())) {
                ResultadoAccion resultado = ResultadoAccion.fallo(
                        automata.getEstadoActual(),
                        "El procedimiento alcanzó el final, pero las condiciones y reactivos no produjeron "
                                + "el producto objetivo." + mensajeQuimico
                );
                registrarAccion(resultado);
                finalizarDificil(estadoFinalParcial(),
                        "El autómata llegó al final sin producir el objetivo químico del experimento.");
                return resultado;
            }

            ResultadoQuimico resultadoQuimico = resultadoAcumulado(true);
            experimentoActual.completar(resultadoQuimico);
            activa = false;
            equipoActual = null;
            registrarRegistroDificil(EstadoRegistroExperimento.COMPLETADO, "", resultadoQuimico);
            ResultadoAccion resultado = ResultadoAccion.completado(
                    automata.getEstadoActual(),
                    resultadoQuimico,
                    "Procedimiento válido y producto objetivo formado." + mensajeQuimico
            );
            registrarAccion(resultado);
            for (ObservadorSesion obs : observadores) {
                obs.onExperimentoCompletado(experimentoActual);
            }
            return resultado;
        }

        ResultadoAccion resultado = ResultadoAccion.exito(
                automata.getEstadoActual(),
                "Operación registrada. " + efectoFisico + mensajeQuimico
        );
        registrarAccion(resultado);
        return resultado;
    }

    private void registrarAccion(ResultadoAccion resultado) {
        for (ObservadorSesion obs : observadores) {
            obs.onAccionEjecutada(resultado);
        }
    }

    private void finalizarDificil(EstadoRegistroExperimento estadoFinal, String motivo) {
        experimentoActual.fallar();
        activa = false;
        equipoActual = null;
        eventosDelExperimento.add("Finalización · " + estadoFinal + " · " + motivo);
        registrarRegistroDificil(estadoFinal, motivo, resultadoAcumulado(!productosProducidos.isEmpty()));
        for (ObservadorSesion obs : observadores) {
            obs.onExperimentoFallido(experimentoActual, motivo);
        }
    }

    private EstadoRegistroExperimento estadoFinalParcial() {
        return productosProducidos.isEmpty()
                ? EstadoRegistroExperimento.FALLIDO
                : EstadoRegistroExperimento.PARCIAL;
    }

    private void registrarRegistroDificil(
            EstadoRegistroExperimento estadoFinal,
            String motivo,
            ResultadoQuimico resultado
    ) {
        var automata = ejecutorAutomata.getAutomataQuimico();
        List<String> finalesNombres = automata.getEstadosFinales().stream()
                .map(com.chemlab.automata.EstadoQuimico::getNombre)
                .toList();
        List<String> todosNombres = automata.getEstados().stream()
                .map(com.chemlab.automata.EstadoQuimico::getNombre)
                .toList();
        cuaderno.agregarRegistro(new RegistroExperimento(
                experimentoActual.getNombre(),
                reaccionActual.getId(),
                Map.copyOf(accionesDelExperimento),
                resultado,
                estadoFinal == EstadoRegistroExperimento.COMPLETADO,
                automata.getEstadoInicial().getNombre(),
                finalesNombres,
                todosNombres,
                List.copyOf(pasosDelExperimento),
                modoJuego,
                estadoFinal,
                motivo,
                List.copyOf(eventosDelExperimento),
                List.copyOf(pistasSolicitadas),
                experimentoActual.getInventario().obtenerCantidades()
        ));
    }

    private ResultadoQuimico resultadoAcumulado(boolean exitoso) {
        return new ResultadoQuimico(new LinkedHashMap<>(productosProducidos), exitoso);
    }

    private static Cantidad sumarCantidades(Cantidad anterior, Cantidad nueva) {
        if (anterior.getUnidad() != nueva.getUnidad()) {
            return nueva;
        }
        return new Cantidad(anterior.getValor() + nueva.getValor(), anterior.getUnidad());
    }

    private String describirCondicionesActuales() {
        return String.format(
                java.util.Locale.ROOT,
                "%.1f °C · %.3f atm · pH %.2f · %s",
                experimentoActual.getCondiciones().getTemperatura(),
                experimentoActual.getCondiciones().getPresion(),
                experimentoActual.getCondiciones().getPh(),
                experimentoActual.getCondiciones().getAtmosfera()
        );
    }

    public void registrarEntradaPuesto(String nombre, TipoEquipo equipo) {
        verificarSesionActiva();
        if (modoJuego != ModoJuego.DIFICIL) return;
        Objects.requireNonNull(nombre, "El nombre del puesto no puede ser null");
        equipoActual = Objects.requireNonNull(equipo, "El equipo no puede ser null");
        eventosDelExperimento.add("Puesto visitado · " + nombre + " · " + equipo);
    }

    public void salirPuesto() {
        if (modoJuego == ModoJuego.DIFICIL) {
            equipoActual = null;
        }
    }

    public void agregarSustancia(Sustancia sustancia, Cantidad cantidad) {
        verificarSesionActiva();
        if (modoJuego != ModoJuego.DIFICIL) {
            throw new IllegalStateException("La carga libre de sustancias solo está disponible en Modo Difícil.");
        }
        Objects.requireNonNull(sustancia, "La sustancia no puede ser null");
        Objects.requireNonNull(cantidad, "La cantidad no puede ser null");
        Sustancia delCatalogo = catalogo.buscarSustancia(sustancia.getId())
                .orElseThrow(() -> new IllegalArgumentException("La sustancia no pertenece al catálogo activo."));
        experimentoActual.getInventario().agregar(delCatalogo, cantidad);
        eventosDelExperimento.add("Sustancia cargada · " + delCatalogo.getFormula()
                + " · " + cantidad);
    }

    public Optional<String> solicitarPista() {
        return solicitarPista(null);
    }

    public Optional<String> solicitarPista(String nombrePuestoSiguiente) {
        if (modoJuego != ModoJuego.DIFICIL || !activa || equipoActual != null
                || cantidadPistasSolicitadas >= 2) {
            return Optional.empty();
        }
        var automata = ejecutorAutomata.getAutomataQuimico();
        var siguiente = automata.getTransicionesDisponibles().stream().findFirst();
        String pista;
        if (siguiente.isEmpty()) {
            pista = "En el punto actual no hay otro paso validado; revisa el estado de la muestra antes de decidir cómo continuar.";
        } else {
            String accion = switch (siguiente.get().getAccion()) {
                case MONTAR_EQUIPO -> "monta y configura el sistema de evaporación";
                case CARGAR_REACTIVO -> "prepara los reactivos necesarios para este proceso";
                case AGREGAR_CATALIZADOR -> "revisa si el proceso necesita un material auxiliar";
                case CALENTAR -> "ajusta la temperatura de la muestra";
                case ENFRIAR -> "reduce la temperatura antes de continuar";
                case COMPRIMIR -> "aumenta la presión del sistema";
                case DESCOMPRIMIR -> "libera presión de forma controlada";
                case MEZCLAR -> "pon en contacto los materiales de la muestra";
                case ACTIVAR_IGNICION -> "activa la fuente de ignición del equipo";
                case INICIAR_PLASMA -> "inicia el plasma bajo las condiciones del proceso";
                case ESPERAR -> "mantén las condiciones actuales durante el proceso";
                case AGREGAR_AGUA -> "incorpora el medio acuoso requerido";
                case SELECCIONAR_METODO -> "elige el método de síntesis adecuado";
                case FINALIZAR -> "comprueba si ya alcanzaste las condiciones para finalizar";
            };
            pista = nombrePuestoSiguiente == null || nombrePuestoSiguiente.isBlank()
                    ? "Desde el punto actual, el siguiente paso es " + accion + "."
                    : "Desde el punto actual, ve a " + nombrePuestoSiguiente
                            + " y " + accion + ".";
        }
        cantidadPistasSolicitadas++;
        pistasSolicitadas.add(pista);
        eventosDelExperimento.add("Pista solicitada · " + cantidadPistasSolicitadas);
        return Optional.of(pista);
    }

    public int getCantidadPistasSolicitadas() {
        return cantidadPistasSolicitadas;
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
        if (modoJuego == ModoJuego.DIFICIL) {
            eventosDelExperimento.add("Interrupción solicitada por el jugador.");
            experimentoActual.fallar();
            activa = false;
            equipoActual = null;
            registrarRegistroDificil(
                    productosProducidos.isEmpty()
                            ? EstadoRegistroExperimento.ABANDONADO
                            : EstadoRegistroExperimento.PARCIAL,
                    "El jugador abandonó el experimento.",
                    resultadoAcumulado(!productosProducidos.isEmpty())
            );
            for (ObservadorSesion obs : observadores) {
                obs.onExperimentoAbandonado(experimentoActual);
            }
            return;
        }
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

    public Map<Sustancia, Cantidad> obtenerMaterialesDisponibles() {
        if (!activa) {
            return Map.of();
        }
        return Map.copyOf(experimentoActual.getInventario().obtenerCantidades());
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

    public List<RegistroExperimento.PasoTransicion> getPasosTransicionActual() {
        return List.copyOf(pasosDelExperimento);
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
