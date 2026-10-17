package com.chemlab.engine;

import com.chemlab.model.entity.*;
import com.chemlab.model.enums.*;
import org.junit.jupiter.api.Test;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.entity.Reaccion;
import java.util.Map;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReglaReaccionBasicaTest {
    private Sustancia crearHidrogeno() {
        return new Sustancia(
                "H2",
                "Hidrógeno",
                "H₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );
    }

    private Sustancia crearOxigeno() {
        return new Sustancia(
                "O2",
                "Oxígeno",
                "O₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                32.00
        );
    }

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

    private Reaccion crearReaccion(
            Sustancia hidrogeno,
            Sustancia oxigeno,
            Sustancia agua
    ) {
        return new Reaccion(
                "R1",
                "Formación de agua",
                Map.of(
                        hidrogeno, 2,
                        oxigeno, 1
                ),
                Map.of(
                        agua, 2
                )
        );
    }
    @Test
    void debeDetectarQueUnaReaccionPuedeEjecutarse() {

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

        Equipo recipiente = new Equipo(
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
                recipiente,
                condiciones
        );

        ReglaReaccion regla =
                new ReglaReaccionBasica();

        assertTrue(
                regla.puedeEjecutarse(
                        reaccion,
                        experimento
                )
        );
    }

    @Test
    void noDebeEjecutarseSiFaltaUnReactivo() {

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

        Equipo recipiente = new Equipo(
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
                recipiente,
                condiciones
        );

        ReglaReaccion regla =
                new ReglaReaccionBasica();

        assertFalse(
                regla.puedeEjecutarse(
                        reaccion,
                        experimento
                )
        );
    }
    @Test
    void debeGenerarProductosConCantidadEstequiometrica() {

        Sustancia hidrogeno = crearHidrogeno();
        Sustancia oxigeno = crearOxigeno();
        Sustancia agua = crearAgua();

        Reaccion reaccion =
                crearReaccion(
                        hidrogeno,
                        oxigeno,
                        agua
                );

        Inventario inventario =
                new Inventario();

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

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25,
                        1,
                        7,
                        Atmosfera.AIRE
                );

        Objetivo objetivo =
                new Objetivo(
                        "OBJ1",
                        "Producir agua",
                        agua,
                        1
                );

        Equipo equipo =
                new Equipo(
                        "E1",
                        "Recipiente",
                        TipoEquipo.RECIPIENTE,
                        100
                );

        Experimento experimento =
                new Experimento(
                        "EXP1",
                        "Formación de agua",
                        objetivo,
                        inventario,
                        equipo,
                        condiciones
                );

        ReglaReaccionBasica regla =
                new ReglaReaccionBasica();

        ResultadoQuimico resultado =
                regla.ejecutar(
                        reaccion,
                        experimento
                );

        assertTrue(
                resultado.isExitoso()
        );

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
}