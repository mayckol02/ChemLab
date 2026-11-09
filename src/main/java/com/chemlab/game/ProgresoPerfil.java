package com.chemlab.game;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.entity.CuadernoLaboratorio;
import com.chemlab.model.entity.RegistroExperimento;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoRegistroExperimento;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Datos del cuaderno y de la ruta de misiones guardados por perfil.
 */
public record ProgresoPerfil(
        int misionesCompletadas,
        boolean rutaMisionesIniciada,
        Set<String> sustanciasDescubiertas,
        Set<String> procesosCompletados,
        List<RegistroGuardado> historial
) {

    public record CantidadGuardada(double valor, String unidad) {
        public CantidadGuardada {
            if (!Double.isFinite(valor) || valor < 0) {
                throw new IllegalArgumentException("La cantidad guardada no es válida.");
            }
            UnidadCantidad.valueOf(Objects.requireNonNull(unidad, "La unidad no puede ser null"));
        }
    }

    public record RegistroGuardado(
            String fecha,
            String nombreExperimento,
            String idProceso,
            Map<String, String> accionesEjecutadas,
            boolean exitoso,
            String estadoInicial,
            List<String> estadosFinales,
            List<String> todosLosEstados,
            List<RegistroExperimento.PasoTransicion> pasosTransicion,
            String modoJuego,
            String estadoFinal,
            String motivoFinalizacion,
            List<String> eventos,
            List<String> pistasSolicitadas,
            Map<String, CantidadGuardada> resultadoProductos,
            boolean tieneResultado,
            Map<String, CantidadGuardada> inventarioFinal
    ) {
        public RegistroGuardado {
            Objects.requireNonNull(fecha, "La fecha del registro no puede ser null");
            Objects.requireNonNull(nombreExperimento, "El nombre del experimento no puede ser null");
            Objects.requireNonNull(modoJuego, "El modo del registro no puede ser null");
            Objects.requireNonNull(estadoFinal, "El estado final no puede ser null");
            accionesEjecutadas = Map.copyOf(Objects.requireNonNull(accionesEjecutadas));
            estadosFinales = List.copyOf(Objects.requireNonNull(estadosFinales));
            todosLosEstados = List.copyOf(Objects.requireNonNull(todosLosEstados));
            pasosTransicion = List.copyOf(Objects.requireNonNull(pasosTransicion));
            eventos = List.copyOf(Objects.requireNonNull(eventos));
            pistasSolicitadas = List.copyOf(Objects.requireNonNull(pistasSolicitadas));
            resultadoProductos = Map.copyOf(Objects.requireNonNull(resultadoProductos));
            inventarioFinal = Map.copyOf(Objects.requireNonNull(inventarioFinal));
        }
    }

    public ProgresoPerfil {
        if (misionesCompletadas < 0
                || misionesCompletadas > new GestorMisiones().obtenerMisiones().size()) {
            throw new IllegalArgumentException("El progreso de misiones está fuera del rango permitido.");
        }
        if (misionesCompletadas > 0 && !rutaMisionesIniciada) {
            throw new IllegalArgumentException("Las misiones completadas requieren una ruta iniciada.");
        }
        sustanciasDescubiertas = Set.copyOf(Objects.requireNonNull(sustanciasDescubiertas));
        procesosCompletados = Set.copyOf(Objects.requireNonNull(procesosCompletados));
        historial = List.copyOf(Objects.requireNonNull(historial));
        historial.forEach(ProgresoPerfil::validarRegistro);
    }

    public static ProgresoPerfil inicial() {
        return new ProgresoPerfil(0, false,
                Set.of("H2", "O2", "C", "Fe", "N2", "Ni"),
                Set.of(), List.of());
    }

    public static ProgresoPerfil capturar(CuadernoLaboratorio cuaderno, GestorMisiones misiones) {
        Objects.requireNonNull(cuaderno, "El cuaderno no puede ser null");
        Objects.requireNonNull(misiones, "El gestor de misiones no puede ser null");
        List<RegistroGuardado> registros = cuaderno.getHistorial().stream()
                .map(ProgresoPerfil::capturarRegistro)
                .toList();
        return new ProgresoPerfil(
                misiones.obtenerCantidadCompletadas(),
                misiones.estaIniciada(),
                cuaderno.getSustanciasDescubiertas(),
                cuaderno.getProcesosCompletados(),
                registros
        );
    }

    public void restaurarCuaderno(CuadernoLaboratorio cuaderno, CatalogoQuimico catalogo) {
        Objects.requireNonNull(cuaderno, "El cuaderno no puede ser null");
        Objects.requireNonNull(catalogo, "El catálogo no puede ser null");
        sustanciasDescubiertas.forEach(id -> catalogo.buscarSustancia(id)
                .orElseThrow(() -> new IllegalStateException(
                        "El perfil contiene una sustancia que no existe en el catálogo: " + id)));
        cuaderno.restaurar(
                sustanciasDescubiertas,
                procesosCompletados,
                historial.stream().map(registro -> restaurarRegistro(registro, catalogo)).toList()
        );
    }

    private static RegistroGuardado capturarRegistro(RegistroExperimento registro) {
        return new RegistroGuardado(
                registro.getFecha().toString(),
                registro.getNombreExperimento(),
                registro.getIdProceso(),
                registro.getAccionesEjecutadas(),
                registro.isExitoso(),
                registro.getEstadoInicial(),
                registro.getEstadosFinales(),
                registro.getTodosLosEstados(),
                registro.getPasosTransicion(),
                registro.getModoJuego().name(),
                registro.getEstadoFinal().name(),
                registro.getMotivoFinalizacion(),
                registro.getEventos(),
                registro.getPistasSolicitadas(),
                cantidadesPorId(registro.getResultado() == null
                        ? Map.of() : registro.getResultado().getProductos()),
                registro.getResultado() != null,
                cantidadesPorId(registro.getInventarioFinal())
        );
    }

    private static void validarRegistro(RegistroGuardado registro) {
        Objects.requireNonNull(registro, "El historial no puede contener registros null");
        LocalDateTime.parse(registro.fecha());
        ModoJuego.valueOf(registro.modoJuego());
        EstadoRegistroExperimento.valueOf(registro.estadoFinal());
    }

    private static RegistroExperimento restaurarRegistro(
            RegistroGuardado registro,
            CatalogoQuimico catalogo
    ) {
        Map<Sustancia, Cantidad> productos = sustanciasPorId(registro.resultadoProductos(), catalogo);
        ResultadoQuimico resultado = registro.tieneResultado()
                ? new ResultadoQuimico(productos, registro.exitoso())
                : null;
        return new RegistroExperimento(
                LocalDateTime.parse(registro.fecha()),
                registro.nombreExperimento(),
                registro.idProceso(),
                registro.accionesEjecutadas(),
                resultado,
                registro.exitoso(),
                registro.estadoInicial(),
                registro.estadosFinales(),
                registro.todosLosEstados(),
                registro.pasosTransicion(),
                ModoJuego.valueOf(registro.modoJuego()),
                EstadoRegistroExperimento.valueOf(registro.estadoFinal()),
                registro.motivoFinalizacion(),
                registro.eventos(),
                registro.pistasSolicitadas(),
                sustanciasPorId(registro.inventarioFinal(), catalogo)
        );
    }

    private static Map<String, CantidadGuardada> cantidadesPorId(Map<Sustancia, Cantidad> cantidades) {
        Map<String, CantidadGuardada> guardadas = new LinkedHashMap<>();
        cantidades.forEach((sustancia, cantidad) -> guardadas.put(
                sustancia.getId(),
                new CantidadGuardada(cantidad.getValor(), cantidad.getUnidad().name())
        ));
        return guardadas;
    }

    private static Map<Sustancia, Cantidad> sustanciasPorId(
            Map<String, CantidadGuardada> cantidades,
            CatalogoQuimico catalogo
    ) {
        Map<Sustancia, Cantidad> restauradas = new LinkedHashMap<>();
        cantidades.forEach((id, cantidad) -> {
            Sustancia sustancia = catalogo.buscarSustancia(id)
                    .orElseThrow(() -> new IllegalStateException(
                            "El perfil contiene una sustancia que no existe en el catálogo: " + id));
            UnidadCantidad unidad = UnidadCantidad.valueOf(cantidad.unidad());
            restauradas.put(sustancia, new Cantidad(cantidad.valor(), unidad));
        });
        return restauradas;
    }
}
