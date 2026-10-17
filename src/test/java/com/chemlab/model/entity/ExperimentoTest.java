package com.chemlab.model.entity;

import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.EstadoExperimento;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.value.Cantidad;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentoTest {

    private Sustancia crearAgua() {
        return new Sustancia(
                "H2O",
                "Agua",
                "H₂O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );
    }

    private Objetivo crearObjetivo(Sustancia agua) {
        return new Objetivo(
                "OBJ_001",
                "Obtener agua",
                agua,
                1
        );
    }

    private Inventario crearInventario(Sustancia agua) {

        Inventario inventario = new Inventario();

        inventario.agregar(
                agua,
                5,
                UnidadCantidad.MOLES
        );

        return inventario;
    }

    private Equipo crearEquipo() {
        return new Equipo(
                "VASO_001",
                "Vaso de precipitados",
                TipoEquipo.RECIPIENTE,
                500
        );
    }

    private CondicionesExperimentales crearCondiciones() {
        return new CondicionesExperimentales(
                25,
                1,
                7,
                Atmosfera.AIRE
        );
    }

    private Experimento crearExperimento() {

        Sustancia agua = crearAgua();

        return new Experimento(
                "EXP_001",
                "Experimento de prueba",
                crearObjetivo(agua),
                crearInventario(agua),
                crearEquipo(),
                crearCondiciones()
        );
    }

    @Test
    void debeCrearUnExperimentoNoIniciado() {

        Experimento experimento = crearExperimento();

        assertEquals(
                "EXP_001",
                experimento.getId()
        );

        assertEquals(
                "Experimento de prueba",
                experimento.getNombre()
        );

        assertEquals(
                EstadoExperimento.NO_INICIADO,
                experimento.getEstado()
        );

        assertNull(
                experimento.getResultado()
        );
    }

    @Test
    void debeIniciarElExperimento() {

        Experimento experimento = crearExperimento();

        experimento.iniciar();

        assertEquals(
                EstadoExperimento.EN_PROGRESO,
                experimento.getEstado()
        );
    }

    @Test
    void noDebePermitirIniciarDosVeces() {

        Experimento experimento = crearExperimento();

        experimento.iniciar();

        assertThrows(
                IllegalStateException.class,
                experimento::iniciar
        );
    }

    @Test
    void debeCompletarUnExperimentoExitoso() {

        Experimento experimento = crearExperimento();

        experimento.iniciar();

        Sustancia agua = crearAgua();

        ResultadoQuimico resultado =
                new ResultadoQuimico(
                        Map.of(
                                agua,
                                new Cantidad(
                                        2.0,
                                        UnidadCantidad.MOLES
                                )
                        ),
                        true
                );

        experimento.completar(resultado);

        assertEquals(
                EstadoExperimento.COMPLETADO,
                experimento.getEstado()
        );

        assertEquals(
                resultado,
                experimento.getResultado()
        );
    }

    @Test
    void debeMarcarComoFallidoUnResultadoNoExitoso() {

        Experimento experimento = crearExperimento();

        experimento.iniciar();

        Sustancia agua = crearAgua();

        ResultadoQuimico resultado =
                new ResultadoQuimico(
                        Map.of(
                                agua,
                                new Cantidad(
                                        0.0,
                                        UnidadCantidad.MOLES
                                )
                        ),
                        false
                );

        experimento.completar(resultado);

        assertEquals(
                EstadoExperimento.FALLIDO,
                experimento.getEstado()
        );
    }

    @Test
    void noDebeCompletarUnExperimentoNoIniciado() {

        Experimento experimento = crearExperimento();

        Sustancia agua = crearAgua();

        ResultadoQuimico resultado =
                new ResultadoQuimico(
                        Map.of(
                                agua,
                                new Cantidad(
                                        1.0,
                                        UnidadCantidad.MOLES
                                )
                        ),
                        true
                );

        assertThrows(
                IllegalStateException.class,
                () -> experimento.completar(resultado)
        );
    }
}