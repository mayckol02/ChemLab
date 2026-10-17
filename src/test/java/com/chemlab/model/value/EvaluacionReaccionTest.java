package com.chemlab.model.value;

import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.EstadoMateria;
import com.chemlab.model.enums.TipoFalloReaccion;
import com.chemlab.model.enums.TipoSustancia;
import com.chemlab.model.enums.UnidadCantidad;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluacionReaccionTest {

    @Test
    void debeCrearUnaEvaluacionExitosaSinFallo() {
        ResultadoEstequiometrico resultado =
                crearResultadoEstequiometrico();

        EvaluacionReaccion evaluacion =
                EvaluacionReaccion.exitosa(resultado);

        assertTrue(evaluacion.esPosible());
        assertSame(
                resultado,
                evaluacion.getResultadoEstequiometrico()
        );
        assertNull(evaluacion.getMotivo());
        assertEquals(
                TipoFalloReaccion.NINGUNO,
                evaluacion.getTipoFallo()
        );
    }

    @Test
    void debeConservarElTipoYMotivoDeUnFallo() {
        EvaluacionReaccion evaluacion =
                EvaluacionReaccion.fallida(
                        TipoFalloReaccion.CANTIDAD_INSUFICIENTE,
                        "No hay oxígeno suficiente"
                );

        assertFalse(evaluacion.esPosible());
        assertNull(evaluacion.getResultadoEstequiometrico());
        assertEquals(
                "No hay oxígeno suficiente",
                evaluacion.getMotivo()
        );
        assertEquals(
                TipoFalloReaccion.CANTIDAD_INSUFICIENTE,
                evaluacion.getTipoFallo()
        );
    }

    @Test
    void noDebePermitirFalloConTipoNinguno() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EvaluacionReaccion.fallida(
                        TipoFalloReaccion.NINGUNO,
                        "Motivo inválido"
                )
        );
    }

    @Test
    void noDebePermitirFalloConMotivoVacio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EvaluacionReaccion.fallida(
                        TipoFalloReaccion.REACTIVO_INEXISTENTE,
                        "   "
                )
        );
    }

    @Test
    void noDebePermitirEvaluacionExitosaSinResultado() {
        assertThrows(
                NullPointerException.class,
                () -> EvaluacionReaccion.exitosa(null)
        );
    }

    private ResultadoEstequiometrico
    crearResultadoEstequiometrico() {

        Sustancia hidrogeno = new Sustancia(
                "H2",
                "Hidrógeno",
                "H₂",
                TipoSustancia.ELEMENTO,
                EstadoMateria.GASEOSO,
                2.016
        );

        return new ResultadoEstequiometrico(
                hidrogeno,
                0.5,
                Map.of(
                        hidrogeno,
                        new Cantidad(
                                1.0,
                                UnidadCantidad.GRAMOS
                        )
                )
        );
    }
}