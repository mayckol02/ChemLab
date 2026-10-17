package com.chemlab.factory;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EjecutorAutomataQuimico;
import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.engine.MotorQuimico;
import com.chemlab.engine.ReglaReaccionBasica;
import com.chemlab.game.SesionJuego;

/**
 * Factory que ensambla una {@link SesionJuego} completamente configurada.
 *
 * <p>Centraliza la resolución de dependencias entre catálogo, motor
 * químico, autómata y caso de uso, de modo que ni la UI ni los tests
 * necesiten conocer el grafo de construcción.</p>
 */
public final class FabricaSesion {

    private FabricaSesion() {
        // utilidad estática
    }

    /**
     * Crea una {@link SesionJuego} con el autómata estándar (4 pasos)
     * y el catálogo predeterminado de sustancias y reacciones.
     */
    public static SesionJuego crearSesionEstandar() {
        CatalogoQuimico catalogo = CatalogoQuimico.predeterminado();
        return crearSesion(
                catalogo,
                FabricaAutomata.crearAutomataEstandar()
        );
    }

    /**
     * Crea una {@link SesionJuego} con el autómata simple (2 pasos).
     * Útil para el tutorial o tests de integración.
     */
    public static SesionJuego crearSesionSimple() {
        CatalogoQuimico catalogo = CatalogoQuimico.predeterminado();
        return crearSesion(
                catalogo,
                FabricaAutomata.crearAutomataSimple()
        );
    }

    /**
     * Crea una sesión con catálogo y autómata explícitos.
     * Permite usar catálogos personalizados (por ejemplo, cargados desde JSON).
     */
    public static SesionJuego crearSesion(
            CatalogoQuimico catalogo,
            AutomataQuimico automata
    ) {
        MotorQuimico motor = new MotorQuimico(
                new ReglaReaccionBasica()
        );

        EjecutorAutomataQuimico ejecutor =
                new EjecutorAutomataQuimico(motor, automata);

        return new SesionJuego(catalogo, ejecutor);
    }
}
