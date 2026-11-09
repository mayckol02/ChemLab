package com.chemlab.repository;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.factory.FabricaExperimento;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.enums.EstadoExperimento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para {@link RepositorioExperimentoJson}.
 * Usa un directorio temporal provisto por JUnit 5 (@TempDir) para
 * no dejar artefactos en el disco ni requerir limpieza manual.
 */
class RepositorioExperimentoJsonTest {

    @TempDir
    Path directorioTemporal;

    private RepositorioExperimentoJson repositorio;
    private FabricaExperimento fabrica;
    private CatalogoQuimico catalogo;

    @BeforeEach
    void setUp() {
        catalogo = CatalogoQuimico.predeterminado();
        repositorio = new RepositorioExperimentoJson(
                directorioTemporal.toString(),
                catalogo
        );
        fabrica = new FabricaExperimento(catalogo);
    }

    // -------------------------------------------------------------------
    // guardar() + cargar()
    // -------------------------------------------------------------------

    @Test
    void debeGuardarYCargarUnExperimento() {
        Experimento original = crearExperimento("R_H2O");

        repositorio.guardar(original);
        Optional<Experimento> cargado = repositorio.cargar(original.getId());

        assertTrue(cargado.isPresent(), "Debe encontrar el experimento guardado");
        assertEquals(original.getId(),     cargado.get().getId());
        assertEquals(original.getNombre(), cargado.get().getNombre());
    }

    @Test
    void cargarConIdInexistentDebeRetornarVacio() {
        Optional<Experimento> resultado = repositorio.cargar("ID-QUE-NO-EXISTE");
        assertTrue(resultado.isEmpty(), "No debe encontrar experimento con ID inexistente");
    }

    @Test
    void debePreservarNombreDelObjetivo() {
        Experimento exp = crearExperimento("R_NH3");
        repositorio.guardar(exp);

        Experimento cargado = repositorio.cargar(exp.getId()).orElseThrow();
        assertEquals(
                exp.getObjetivo().getDescripcion(),
                cargado.getObjetivo().getDescripcion()
        );
    }

    @Test
    void debePreservarSustanciaObjetivo() {
        Experimento exp = crearExperimento("R_NaCl");
        repositorio.guardar(exp);

        Experimento cargado = repositorio.cargar(exp.getId()).orElseThrow();
        assertEquals(
                "NaCl",
                cargado.getObjetivo().getSustanciaEsperada().getId()
        );
    }

    @Test
    void debePreservarCondicionesTemperatura() {
        Experimento exp = crearExperimento("R_NH3");
        repositorio.guardar(exp);

        Experimento cargado = repositorio.cargar(exp.getId()).orElseThrow();
        assertEquals(
                exp.getCondiciones().getTemperatura(),
                cargado.getCondiciones().getTemperatura(),
                1e-9
        );
    }

    @Test
    void debePreservarEquipo() {
        Experimento exp = crearExperimento("R_CO2");
        repositorio.guardar(exp);

        Experimento cargado = repositorio.cargar(exp.getId()).orElseThrow();
        assertEquals(exp.getEquipo().getId(), cargado.getEquipo().getId());
        assertEquals(exp.getEquipo().getNombre(), cargado.getEquipo().getNombre());
    }

    @Test
    void debePreservarInventario() {
        Experimento exp = crearExperimento("R_H2O");
        repositorio.guardar(exp);

        Experimento cargado = repositorio.cargar(exp.getId()).orElseThrow();
        assertEquals(
                exp.getInventario().obtenerCantidades().size(),
                cargado.getInventario().obtenerCantidades().size(),
                "El inventario cargado debe tener el mismo número de sustancias"
        );
    }

    // -------------------------------------------------------------------
    // Estado del experimento tras carga
    // -------------------------------------------------------------------

    @Test
    void experimentoGuardadoNoIniciadoDebeCargarseSinIniciar() {
        Experimento exp = crearExperimento("R_H2O");
        assertEquals(EstadoExperimento.NO_INICIADO, exp.getEstado());

        repositorio.guardar(exp);
        Experimento cargado = repositorio.cargar(exp.getId()).orElseThrow();

        assertEquals(EstadoExperimento.NO_INICIADO, cargado.getEstado());
    }

    @Test
    void experimentoEnProgresoDebeCargarseCorrecto() {
        Experimento exp = crearExperimento("R_H2O");
        exp.iniciar();
        assertEquals(EstadoExperimento.EN_PROGRESO, exp.getEstado());

        repositorio.guardar(exp);
        Experimento cargado = repositorio.cargar(exp.getId()).orElseThrow();

        assertEquals(EstadoExperimento.EN_PROGRESO, cargado.getEstado());
    }

    @Test
    void experimentoFallidoDebeCargarseCorrecto() {
        Experimento exp = crearExperimento("R_H2O");
        exp.iniciar();
        exp.fallar();
        assertEquals(EstadoExperimento.FALLIDO, exp.getEstado());

        repositorio.guardar(exp);
        Experimento cargado = repositorio.cargar(exp.getId()).orElseThrow();

        assertEquals(EstadoExperimento.FALLIDO, cargado.getEstado());
    }

    // -------------------------------------------------------------------
    // listarTodos()
    // -------------------------------------------------------------------

    @Test
    void listarTodosEnDirectorioVacioDebeRetornarListaVacia() {
        List<Experimento> lista = repositorio.listarTodos();
        assertTrue(lista.isEmpty(), "Debe retornar lista vacía cuando no hay experimentos");
    }

    @Test
    void listarTodosDebeRetornarTodosLosGuardados() {
        repositorio.guardar(crearExperimento("R_H2O"));
        repositorio.guardar(crearExperimento("R_CO2"));
        repositorio.guardar(crearExperimento("R_NH3"));

        List<Experimento> lista = repositorio.listarTodos();
        assertEquals(3, lista.size(), "Debe haber exactamente 3 experimentos guardados");
    }

    @Test
    void sobreescribirUnExperimentoDebeActualizarloAlCargar() {
        Experimento exp = crearExperimento("R_H2O");
        repositorio.guardar(exp);   // estado NO_INICIADO
        exp.iniciar();               // cambiamos estado
        repositorio.guardar(exp);   // guardamos de nuevo (misma ID)

        // No debe haber duplicados
        List<Experimento> lista = repositorio.listarTodos();
        assertEquals(1, lista.size());

        Experimento cargado = repositorio.cargar(exp.getId()).orElseThrow();
        assertEquals(EstadoExperimento.EN_PROGRESO, cargado.getEstado());
    }

    // -------------------------------------------------------------------
    // Validación de argumentos
    // -------------------------------------------------------------------

    @Test
    void constructorConCatalogoNullDebeLanzarNPE() {
        assertThrows(
                NullPointerException.class,
                () -> new RepositorioExperimentoJson(directorioTemporal.toString(), null)
        );
    }

    private Experimento crearExperimento(String idReaccion) {
        return fabrica.crearExperimentoParaReaccion(
                catalogo.buscarReaccion(idReaccion).orElseThrow());
    }
}
