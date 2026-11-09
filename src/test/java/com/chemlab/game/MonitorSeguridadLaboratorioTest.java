package com.chemlab.game;

import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.CondicionesRequeridas;
import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.Atmosfera;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitorSeguridadLaboratorioTest {

    @Test
    void condicionesNormalesNoGeneranAlertasSimuladas() {
        var informe = MonitorSeguridadLaboratorio.analizar(
                condiciones(25, 1, Atmosfera.AIRE),
                CondicionesRequeridas.sinCondicionesEspeciales(),
                null);

        assertTrue(informe.advertencias().isEmpty());
        assertTrue(informe.estadoParametros().contains("dentro del rango"));
    }

    @Test
    void parametrosFueraDelRangoSeReportanSeparadosDeLaSeguridad() {
        var requeridas = new CondicionesRequeridas(
                100, 200, 1, 2, List.of(), Atmosfera.NITROGENO, false);

        var informe = MonitorSeguridadLaboratorio.analizar(
                condiciones(25, 1, Atmosfera.AIRE), requeridas, null);

        assertTrue(informe.estadoParametros().contains("temperatura fuera del rango"));
        assertTrue(informe.estadoParametros().contains("atmósfera distinta"));
        assertTrue(informe.advertencias().isEmpty());
    }

    @Test
    void temperaturaYPresionElevadasGeneranAlertasEducativas() {
        var informe = MonitorSeguridadLaboratorio.analizar(
                condiciones(100, 5, Atmosfera.AIRE),
                CondicionesRequeridas.sinCondicionesEspeciales(),
                null);

        assertEquals(2, informe.advertencias().size());
        assertTrue(informe.advertencias().get(0).contains("Temperatura elevada"));
        assertTrue(informe.advertencias().get(1).contains("Presión elevada"));
    }

    @Test
    void ignicionEnOxigenoGeneraAlertaAdicional() {
        var informe = MonitorSeguridadLaboratorio.analizar(
                condiciones(25, 1, Atmosfera.OXIGENO),
                CondicionesRequeridas.sinCondicionesEspeciales(),
                AccionLaboratorio.ACTIVAR_IGNICION);

        assertEquals(2, informe.advertencias().size());
        assertTrue(informe.advertencias().get(0).contains("fuentes de ignición"));
        assertTrue(informe.advertencias().get(1).contains("ignición con esta atmósfera"));
    }

    @Test
    void cloroGeneraAlertaDeContencionSinBloquearElProceso() {
        var informe = MonitorSeguridadLaboratorio.analizar(
                condiciones(25, 1, Atmosfera.CLORO),
                CondicionesRequeridas.sinCondicionesEspeciales(),
                AccionLaboratorio.ESPERAR);

        assertFalse(informe.advertencias().isEmpty());
        assertTrue(informe.advertencias().get(0).contains("mantén la muestra dentro del equipo"));
    }

    private static CondicionesExperimentales condiciones(
            double temperatura, double presion, Atmosfera atmosfera) {
        return new CondicionesExperimentales(temperatura, presion, 7, atmosfera);
    }
}
