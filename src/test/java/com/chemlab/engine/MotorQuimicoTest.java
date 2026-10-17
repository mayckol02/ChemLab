package com.chemlab.engine;

import com.chemlab.model.entity.*;
import com.chemlab.model.enums.*;
import org.junit.jupiter.api.Test;
import com.chemlab.service.ConversorQuimico;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MotorQuimicoTest {

    @Test
    void debeDelegarLaEjecucionEnLaRegla() {

        Sustancia hidrogeno = new Sustancia(
                "H2",
                "Hidrógeno",
                "H₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );

        Sustancia oxigeno = new Sustancia(
                "O2",
                "Oxígeno",
                "O₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                32.00
        );

        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H₂O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        Reaccion reaccion = new Reaccion(
                "R001",
                "Formación de agua",
                Map.of(
                        hidrogeno, 2,
                        oxigeno, 1
                ),
                Map.of(
                        agua, 2
                )
        );

        Inventario inventario = new Inventario();

        inventario.agregar(
                hidrogeno,
                10,
                UnidadCantidad.GRAMOS
        );

        inventario.agregar(
                oxigeno,
                10,
                UnidadCantidad.GRAMOS
        );

        Equipo equipo = new Equipo(
                "E001",
                "Recipiente",
                TipoEquipo.RECIPIENTE,
                100
        );

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25,
                        1,
                        7,
                        Atmosfera.AIRE
                );

        Objetivo objetivo = new Objetivo(
                "O001",
                "Obtener agua",
                agua,
                1
        );

        Experimento experimento = new Experimento(
                "EXP001",
                "Formación de agua",
                objetivo,
                inventario,
                equipo,
                condiciones
        );

        MotorQuimico motor =
                new MotorQuimico(
                        new ReglaReaccionBasica(),
                        new CalculadorEstequiometrico(
                                new ConversorQuimico()
                        ),
                        new AplicadorReaccion()
                );

        ResultadoQuimico resultado =
                motor.ejecutar(
                        reaccion,
                        experimento
                );

        assertTrue(resultado.isExitoso());
        assertEquals(
                0.625,
                resultado
                        .getCantidad(agua)
                        .getValor(),
                0.0001
        );

        assertEquals(
                UnidadCantidad.MOLES,
                resultado
                        .getCantidad(agua)
                        .getUnidad()
        );
    }

    @Test
    void debeIndicarCuandoUnaReaccionNoPuedeEjecutarse() {

        Sustancia hidrogeno = new Sustancia(
                "H2",
                "Hidrógeno",
                "H₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );

        Sustancia oxigeno = new Sustancia(
                "O2",
                "Oxígeno",
                "O₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                32.00
        );

        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H₂O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        Reaccion reaccion = new Reaccion(
                "R001",
                "Formación de agua",
                Map.of(
                        hidrogeno, 2,
                        oxigeno, 1
                ),
                Map.of(
                        agua, 2
                )
        );

        Inventario inventario = new Inventario();

        inventario.agregar(
                hidrogeno,
                10,
                UnidadCantidad.GRAMOS
        );

        Equipo equipo = new Equipo(
                "E001",
                "Recipiente",
                TipoEquipo.RECIPIENTE,
                100
        );

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25,
                        1,
                        7,
                        Atmosfera.AIRE
                );

        Objetivo objetivo = new Objetivo(
                "O001",
                "Obtener agua",
                agua,
                1
        );

        Experimento experimento = new Experimento(
                "EXP001",
                "Formación de agua",
                objetivo,
                inventario,
                equipo,
                condiciones
        );

        MotorQuimico motor =
                new MotorQuimico(
                        new ReglaReaccionBasica()
                );

        assertFalse(
                motor.puedeEjecutarse(
                        reaccion,
                        experimento
                )
        );
    }

    @Test
    void debeModificarElInventarioAlEjecutarUnaReaccion() {

        Sustancia hidrogeno = new Sustancia(
                "H2",
                "Hidrógeno",
                "H2",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );

        Sustancia oxigeno = new Sustancia(
                "O2",
                "Oxígeno",
                "O2",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                32.00
        );

        Sustancia agua = new Sustancia(
                "H2O",
                "Agua",
                "H2O",
                TipoSustancia.COMPUESTO,
                EstadoMateria.LIQUIDO,
                18.015
        );

        Reaccion reaccion = new Reaccion(
                "combustion-hidrogeno",
                "Formación de agua",
                Map.of(
                        hidrogeno, 2,
                        oxigeno, 1
                ),
                Map.of(
                        agua, 2
                )
        );

        Inventario inventario = new Inventario();

        inventario.agregar(
                hidrogeno,
                10.0,
                UnidadCantidad.GRAMOS
        );

        inventario.agregar(
                oxigeno,
                10.0,
                UnidadCantidad.GRAMOS
        );

        Objetivo objetivo = new Objetivo(
                "objetivo-agua",
                "Producir agua",
                agua,
                0.5
        );

        Equipo equipo = new Equipo(
                "recipiente-01",
                "Recipiente",
                TipoEquipo.RECIPIENTE,
                100.0
        );

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25.0,
                        1.0,
                        7.0,
                        Atmosfera.AIRE
                );

        Experimento experimento =
                new Experimento(
                        "exp-01",
                        "Formación de agua",
                        objetivo,
                        inventario,
                        equipo,
                        condiciones
                );

        MotorQuimico motor =
                new MotorQuimico(
                        new ReglaReaccionBasica(),
                        new CalculadorEstequiometrico(
                                new ConversorQuimico()
                        ),
                        new AplicadorReaccion()
                );

        ResultadoQuimico resultado =
                motor.ejecutar(
                        reaccion,
                        experimento
                );

        assertTrue(resultado.isExitoso());

        assertEquals(
                0.0,
                inventario.obtenerCantidad(oxigeno),
                0.0001
        );

        assertEquals(
                0.625,
                inventario.obtenerCantidad(agua),
                0.0001
        );

        assertEquals(
                UnidadCantidad.MOLES,
                inventario.obtenerUnidad(agua)
        );
    }
}