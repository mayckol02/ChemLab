package com.chemlab.game;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.entity.CuadernoLaboratorio;
import com.chemlab.model.entity.RegistroExperimento;
import com.chemlab.model.entity.ResultadoQuimico;
import com.chemlab.model.enums.EstadoRegistroExperimento;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import com.chemlab.repository.RepositorioPerfilesJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgresoPerfilTest {

    @TempDir
    Path directorioTemporal;

    @Test
    void capturaGuardaYRestauraElHistorialDelCuaderno() {
        CatalogoQuimico catalogo = CatalogoQuimico.predeterminado();
        CuadernoLaboratorio cuaderno = new CuadernoLaboratorio();
        var agua = catalogo.buscarSustancia("H2O").orElseThrow();
        LocalDateTime fecha = LocalDateTime.of(2025, 3, 12, 14, 30);
        RegistroExperimento registro = new RegistroExperimento(
                fecha,
                "Formación de agua",
                "R_H2O",
                Map.of("mezclar", "valido"),
                new ResultadoQuimico(
                        Map.of(agua, new Cantidad(18, UnidadCantidad.GRAMOS)),
                        true
                ),
                true,
                "q0",
                List.of("q4"),
                List.of("q0", "q4"),
                List.of(new RegistroExperimento.PasoTransicion("q0", "mezclar", "q4", true)),
                ModoJuego.TUTORIAL,
                EstadoRegistroExperimento.COMPLETADO,
                "",
                List.of("Producto formado"),
                List.of(),
                Map.of(agua, new Cantidad(18, UnidadCantidad.GRAMOS))
        );
        cuaderno.registrarDescubrimiento(agua);
        cuaderno.agregarRegistro(registro);
        GestorMisiones misiones = new GestorMisiones(1, true);
        ProgresoPerfil progreso = ProgresoPerfil.capturar(cuaderno, misiones);
        RepositorioPerfilesJson repositorio =
                new RepositorioPerfilesJson(directorioTemporal.resolve("profiles"));
        PerfilJugador perfil = repositorio.crear("Ada");
        repositorio.guardar(perfil.conProgreso(progreso));

        PerfilJugador cargado = repositorio.listar().getFirst();
        CuadernoLaboratorio cuadernoRestaurado = new CuadernoLaboratorio();
        cargado.progreso().restaurarCuaderno(cuadernoRestaurado, catalogo);
        RegistroExperimento registroRestaurado = cuadernoRestaurado.getHistorial().getFirst();
        GestorMisiones misionesRestauradas = new GestorMisiones(
                cargado.progreso().misionesCompletadas(),
                cargado.progreso().rutaMisionesIniciada()
        );

        assertEquals(fecha, registroRestaurado.getFecha());
        assertEquals("R_H2O", registroRestaurado.getIdProceso());
        assertEquals(18, registroRestaurado.getResultado().getProductos().get(agua).getValor(), 0.0001);
        assertEquals("combustion-carbono", misionesRestauradas.obtenerMisionActual().orElseThrow().id());
        assertEquals(Set.of("H2", "O2", "C", "Fe", "N2", "Ni", "H2O"),
                cuadernoRestaurado.getSustanciasDescubiertas());
    }
}
