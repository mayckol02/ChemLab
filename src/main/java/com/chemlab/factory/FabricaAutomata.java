package com.chemlab.factory;

import com.chemlab.automata.AutomataQuimico;
import com.chemlab.automata.EstadoQuimico;
import com.chemlab.automata.TransicionQuimica;
import com.chemlab.grammar.CompiladorGramaticaRegular;
import com.chemlab.grammar.GramaticaRegular;
import com.chemlab.grammar.GramaticasProtocolos;
import com.chemlab.model.entity.CondicionesRequeridas;
import com.chemlab.model.enums.AccionLaboratorio;

import java.util.List;

/**
 * Factory que construye el {@link AutomataQuimico} estándar del laboratorio.
 *
 * <p>Define los estados y transiciones del proceso experimental, siguiendo
 * la secuencia típica: preparar → reaccionar → obtener producto.</p>
 *
 * <p>Al estar separado del dominio, el autómata puede rediseñarse o
 * extenderse sin tocar la lógica química central.</p>
 */
public final class FabricaAutomata {

    private FabricaAutomata() {
        // utilidad estática
    }

    /**
     * Crea el autómata estándar con las siguientes etapas:
     * <ol>
     *   <li>Preparación: CARGAR_REACTIVO → Preparado</li>
     *   <li>Mezcla: MEZCLAR → Mezclado</li>
     *   <li>Reacción: CALENTAR → Reaccionando</li>
     *   <li>Finalización: FINALIZAR → Completado (estado final)</li>
     * </ol>
     */
    public static AutomataQuimico crearAutomataEstandar() {
        EstadoQuimico preparacion = new EstadoQuimico(
                "PREPARACION",
                "Preparación de reactivos"
        );

        EstadoQuimico preparado = new EstadoQuimico(
                "PREPARADO",
                "Reactivos cargados"
        );

        EstadoQuimico mezclado = new EstadoQuimico(
                "MEZCLADO",
                "Mezcla en progreso"
        );

        EstadoQuimico reaccionando = new EstadoQuimico(
                "REACCIONANDO",
                "Reacción en curso"
        );

        EstadoQuimico completado = new EstadoQuimico(
                "COMPLETADO",
                "Proceso completado"
        );

        List<EstadoQuimico> estados = List.of(
                preparacion,
                preparado,
                mezclado,
                reaccionando,
                completado
        );

        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(
                        preparacion,
                        AccionLaboratorio.CARGAR_REACTIVO,
                        CondicionesRequeridas.sinCondicionesEspeciales(),
                        preparado
                ),
                new TransicionQuimica(
                        preparado,
                        AccionLaboratorio.MEZCLAR,
                        CondicionesRequeridas.sinCondicionesEspeciales(),
                        mezclado
                ),
                new TransicionQuimica(
                        mezclado,
                        AccionLaboratorio.CALENTAR,
                        CondicionesRequeridas.sinCondicionesEspeciales(),
                        reaccionando
                ),
                new TransicionQuimica(
                        reaccionando,
                        AccionLaboratorio.FINALIZAR,
                        CondicionesRequeridas.sinCondicionesEspeciales(),
                        completado
                )
        );

        return new AutomataQuimico(
                preparacion,
                estados,
                transiciones,
                List.of(completado)
        );
    }

    /**
     * Autómata simplificado para experimentos de 2 pasos:
     * CARGAR_REACTIVO → FINALIZAR (estado final).
     *
     * <p>Útil para tutoriales o reacciones directas sin mezcla previa.</p>
     */
    public static AutomataQuimico crearAutomataSimple() {
        EstadoQuimico inicial = new EstadoQuimico(
                "INICIAL",
                "Estado inicial"
        );

        EstadoQuimico listo = new EstadoQuimico(
                "LISTO",
                "Listo para reaccionar"
        );

        List<EstadoQuimico> estados = List.of(inicial, listo);

        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(
                        inicial,
                        AccionLaboratorio.CARGAR_REACTIVO,
                        CondicionesRequeridas.sinCondicionesEspeciales(),
                        listo
                )
        );

        return new AutomataQuimico(
                inicial,
                estados,
                transiciones,
                List.of(listo)
        );
    }

    public static AutomataQuimico crearAutomataHaberBosch() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico preparado = new EstadoQuimico("PREPARADO", "Reactivos cargados");
        EstadoQuimico mezclado = new EstadoQuimico("MEZCLADO", "Mezclado");
        EstadoQuimico comprimido = new EstadoQuimico("COMPRIMIDO", "Alta presión");
        EstadoQuimico reaccionando = new EstadoQuimico("REACCIONANDO", "Reaccionando (calor)");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Completado");

        List<EstadoQuimico> estados = List.of(preparacion, preparado, mezclado, comprimido, reaccionando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), preparado),
                new TransicionQuimica(preparado, AccionLaboratorio.MEZCLAR, CondicionesRequeridas.sinCondicionesEspeciales(), mezclado),
                new TransicionQuimica(mezclado, AccionLaboratorio.COMPRIMIR, CondicionesRequeridas.sinCondicionesEspeciales(), comprimido),
                new TransicionQuimica(comprimido, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), reaccionando),
                new TransicionQuimica(reaccionando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );

        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataOxidacionHierro() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico preparado = new EstadoQuimico("PREPARADO", "Hierro cargado");
        EstadoQuimico humedecido = new EstadoQuimico("HUMEDECIDO", "Humedecido");
        EstadoQuimico oxidandose = new EstadoQuimico("OXIDANDOSE", "Oxidándose");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Completado");

        List<EstadoQuimico> estados = List.of(preparacion, preparado, humedecido, oxidandose, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), preparado),
                new TransicionQuimica(preparado, AccionLaboratorio.AGREGAR_AGUA, CondicionesRequeridas.sinCondicionesEspeciales(), humedecido),
                new TransicionQuimica(humedecido, AccionLaboratorio.ESPERAR, CondicionesRequeridas.sinCondicionesEspeciales(), oxidandose),
                new TransicionQuimica(oxidandose, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    /**
     * Autómata para síntesis de ácido clorhídrico (H₂ + Cl₂ → 2HCl).
     * Secuencia: CARGAR_REACTIVO → ACTIVAR_IGNICION → FINALIZAR.
     * La reacción entre H₂ y Cl₂ es exotérmica y puede iniciarse con luz/calor.
     */
    public static AutomataQuimico crearAutomataSintesisHCl() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de gases");
        EstadoQuimico preparado  = new EstadoQuimico("PREPARADO",   "Gases cargados en la cámara");
        EstadoQuimico activado   = new EstadoQuimico("ACTIVADO",    "Ignición activada – reacción en curso");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO",  "HCl sintetizado");

        List<EstadoQuimico> estados = List.of(preparacion, preparado, activado, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO,
                        CondicionesRequeridas.sinCondicionesEspeciales(), preparado),
                new TransicionQuimica(preparado, AccionLaboratorio.ACTIVAR_IGNICION,
                        CondicionesRequeridas.sinCondicionesEspeciales(), activado),
                new TransicionQuimica(activado, AccionLaboratorio.FINALIZAR,
                        CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );

        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataCombustionMetano() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de reactivos");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "CH₄ y O₂ cargados");
        EstadoQuimico ignicion = new EstadoQuimico("IGNICION", "Ignición activada");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Combustión finalizada (CO₂ + H₂O)");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, ignicion, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.ACTIVAR_IGNICION, CondicionesRequeridas.sinCondicionesEspeciales(), ignicion),
                new TransicionQuimica(ignicion, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataSulfuroHidrogeno() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "H₂ y S cargados");
        EstadoQuimico calentando = new EstadoQuimico("CALENTANDO", "Calentando a reacción");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "H₂S obtenido");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, calentando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), calentando),
                new TransicionQuimica(calentando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataOxidacionCobre() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Cobre y O₂ cargados");
        EstadoQuimico oxidando = new EstadoQuimico("OXIDANDO", "Calentamiento y oxidación");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "CuO obtenido");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, oxidando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), oxidando),
                new TransicionQuimica(oxidando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataOxidoCalcio() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Calcio y O₂ listos");
        EstadoQuimico calcinando = new EstadoQuimico("CALCINANDO", "Calcinación térmica");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "CaO obtenido");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, calcinando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), calcinando),
                new TransicionQuimica(calcinando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataHidratacionCalcio() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Cal viva cargada");
        EstadoQuimico hidratado = new EstadoQuimico("HIDRATANDO", "Adición de agua exotérmica");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Ca(OH)₂ (cal apagada) obtenido");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, hidratado, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.AGREGAR_AGUA, CondicionesRequeridas.sinCondicionesEspeciales(), hidratado),
                new TransicionQuimica(hidratado, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataPrecipitacionCarbonato() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico reactivos = new EstadoQuimico("REACTIVOS", "Ca(OH)₂ y CO₂ listos");
        EstadoQuimico mezclado = new EstadoQuimico("MEZCLADO", "Gaseado y agitación");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "CaCO₃ precipitado");

        List<EstadoQuimico> estados = List.of(preparacion, reactivos, mezclado, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), reactivos),
                new TransicionQuimica(reactivos, AccionLaboratorio.MEZCLAR, CondicionesRequeridas.sinCondicionesEspeciales(), mezclado),
                new TransicionQuimica(mezclado, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataOxidoSilicio() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Silicio y O₂ listos");
        EstadoQuimico oxidando = new EstadoQuimico("OXIDANDO", "Oxidación térmica a alta T");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "SiO₂ (Sílice) obtenido");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, oxidando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), oxidando),
                new TransicionQuimica(oxidando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataOxidoMagnesio() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Cinta de Mg y O₂");
        EstadoQuimico ignicion = new EstadoQuimico("IGNICION", "Ignición brillante");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "MgO obtenido");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, ignicion, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.ACTIVAR_IGNICION, CondicionesRequeridas.sinCondicionesEspeciales(), ignicion),
                new TransicionQuimica(ignicion, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataOxidoAluminio() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Aluminio y O₂");
        EstadoQuimico oxidando = new EstadoQuimico("OXIDANDO", "Oxidación superficial activada");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Al₂O₃ (Alúmina) obtenido");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, oxidando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), oxidando),
                new TransicionQuimica(oxidando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataFosfatoCalcio() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Precursores de Ca, P y O₂");
        EstadoQuimico calcinando = new EstadoQuimico("CALCINANDO", "Sinterización a alta T");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Ca₃(PO₄)₂ obtenido");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, calcinando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), calcinando),
                new TransicionQuimica(calcinando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataDiamanteHPHT() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de celda HPHT");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Grafito y semilla cargados");
        EstadoQuimico catalizador = new EstadoQuimico("CATALIZADOR", "Flujo metálico Fe/Ni añadido");
        EstadoQuimico presurizado = new EstadoQuimico("PRESURIZADO", "Cámara a 5-6 GPa");
        EstadoQuimico crecimiento = new EstadoQuimico("CRECIMIENTO", "Crecimiento cristalino a 1500°C");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Diamante HPHT sintetizado");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, catalizador, presurizado, crecimiento, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.AGREGAR_CATALIZADOR, CondicionesRequeridas.sinCondicionesEspeciales(), catalizador),
                new TransicionQuimica(catalizador, AccionLaboratorio.COMPRIMIR, CondicionesRequeridas.sinCondicionesEspeciales(), presurizado),
                new TransicionQuimica(presurizado, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), crecimiento),
                new TransicionQuimica(crecimiento, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataDiamanteCVD() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de cámara CVD");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Gases CH₄ y H₂ introducidos");
        EstadoQuimico plasma = new EstadoQuimico("PLASMA", "Plasma de microondas activado");
        EstadoQuimico deposicion = new EstadoQuimico("DEPOSICION", "Deposición epitaxial de carbono");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Diamante CVD obtenido");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, plasma, deposicion, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.INICIAR_PLASMA, CondicionesRequeridas.sinCondicionesEspeciales(), plasma),
                new TransicionQuimica(plasma, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), deposicion),
                new TransicionQuimica(deposicion, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataOxidacionAzufre() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Azufre y O₂ listos");
        EstadoQuimico ignicion = new EstadoQuimico("IGNICION", "Ignición de llama azul");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "SO₂ sintetizado");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, ignicion, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.ACTIVAR_IGNICION, CondicionesRequeridas.sinCondicionesEspeciales(), ignicion),
                new TransicionQuimica(ignicion, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataCorrosionHierro() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de placa de hierro");
        EstadoQuimico preparado = new EstadoQuimico("PREPARADO", "Hierro en bandeja de exposición");
        EstadoQuimico humedecido = new EstadoQuimico("HUMEDECIDO", "Película de agua y electrolito añadida");
        EstadoQuimico oxidandose = new EstadoQuimico("OXIDANDOSE", "Corrosión electroquímica en medio húmedo");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Herrumbre Fe(OH)₃ formada");

        List<EstadoQuimico> estados = List.of(preparacion, preparado, humedecido, oxidandose, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), preparado),
                new TransicionQuimica(preparado, AccionLaboratorio.AGREGAR_AGUA, CondicionesRequeridas.sinCondicionesEspeciales(), humedecido),
                new TransicionQuimica(humedecido, AccionLaboratorio.ESPERAR, CondicionesRequeridas.sinCondicionesEspeciales(), oxidandose),
                new TransicionQuimica(oxidandose, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataCalcinacionCaliza() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de piedra caliza");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "CaCO₃ en crisol térmico");
        EstadoQuimico calcinando = new EstadoQuimico("CALCINANDO", "Descomposición térmica a 900°C");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Cal viva (CaO) y CO₂ liberados");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, calcinando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), calcinando),
                new TransicionQuimica(calcinando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataDisolucionNaCl() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de sal y solvente");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Cristales de NaCl en fondo de vaso");
        EstadoQuimico humedecido = new EstadoQuimico("HUMEDECIDO", "Agua agregada (sistema bifásico)");
        EstadoQuimico disolviendo = new EstadoQuimico("DISOLVIENDO", "Agitación y solvatación iónica");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Solución salina homogénea formada");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, humedecido, disolviendo, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.AGREGAR_AGUA, CondicionesRequeridas.sinCondicionesEspeciales(), humedecido),
                new TransicionQuimica(humedecido, AccionLaboratorio.MEZCLAR, CondicionesRequeridas.sinCondicionesEspeciales(), disolviendo),
                new TransicionQuimica(disolviendo, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataSeparacionSolucion() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Solución salina en matraz");
        EstadoQuimico sistemaMontado = new EstadoQuimico("SISTEMA_MONTADO", "Sistema de evaporación instalado");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Solución salina en el sistema de evaporación");
        EstadoQuimico metodoElegido = new EstadoQuimico("METODO_ELEGIDO", "Método de evaporación/cristalización seleccionado");
        EstadoQuimico evaporando = new EstadoQuimico("EVAPORANDO", "Ebullición a 100°C y escape de vapor");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Cristales de sal recuperados y agua separada");

        List<EstadoQuimico> estados = List.of(preparacion, sistemaMontado, cargado, metodoElegido, evaporando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.MONTAR_EQUIPO, CondicionesRequeridas.sinCondicionesEspeciales(), sistemaMontado),
                new TransicionQuimica(sistemaMontado, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.SELECCIONAR_METODO, CondicionesRequeridas.sinCondicionesEspeciales(), metodoElegido),
                new TransicionQuimica(metodoElegido, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), evaporando),
                new TransicionQuimica(evaporando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataRedox() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de CuO y H₂");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Reactivos en tubo de reducción");
        EstadoQuimico mezclado = new EstadoQuimico("MEZCLADO", "Flujo de hidrógeno purgado");
        EstadoQuimico reaccionando = new EstadoQuimico("REACCIONANDO", "Reducción térmica a 300°C (transferencia e⁻)");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Cobre metálico rojizo brillante recuperado");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, mezclado, reaccionando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.MEZCLAR, CondicionesRequeridas.sinCondicionesEspeciales(), mezclado),
                new TransicionQuimica(mezclado, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), reaccionando),
                new TransicionQuimica(reaccionando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataOxidoMetalico() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Metal preparado");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Metal y O₂ listos en cámara");
        EstadoQuimico oxidando = new EstadoQuimico("OXIDANDO", "Oxidación térmica activada");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Óxido metálico formado");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, oxidando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.CALENTAR, CondicionesRequeridas.sinCondicionesEspeciales(), oxidando),
                new TransicionQuimica(oxidando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataCombustionCarbono() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de carbono");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Carbono en cápsula de combustión");
        EstadoQuimico mezclado = new EstadoQuimico("MEZCLADO", "Atmósfera de O₂ puro inyectada");
        EstadoQuimico ignicion = new EstadoQuimico("IGNICION", "Combustión viva a 500°C");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Dióxido de carbono (CO₂) obtenido");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, mezclado, ignicion, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.MEZCLAR, CondicionesRequeridas.sinCondicionesEspeciales(), mezclado),
                new TransicionQuimica(mezclado, AccionLaboratorio.ACTIVAR_IGNICION, CondicionesRequeridas.sinCondicionesEspeciales(), ignicion),
                new TransicionQuimica(ignicion, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataSintesisNaCl() {
        EstadoQuimico preparacion = new EstadoQuimico("PREPARACION", "Preparación de Na y Cl₂");
        EstadoQuimico cargado = new EstadoQuimico("CARGADO", "Sodio metálico en cámara sellada");
        EstadoQuimico contacto = new EstadoQuimico("CONTACTO", "Gas cloro introducido");
        EstadoQuimico reaccionando = new EstadoQuimico("REACCIONANDO", "Reacción vigorosa de halogenación");
        EstadoQuimico completado = new EstadoQuimico("COMPLETADO", "Cristales de NaCl sintetizados");

        List<EstadoQuimico> estados = List.of(preparacion, cargado, contacto, reaccionando, completado);
        List<TransicionQuimica> transiciones = List.of(
                new TransicionQuimica(preparacion, AccionLaboratorio.CARGAR_REACTIVO, CondicionesRequeridas.sinCondicionesEspeciales(), cargado),
                new TransicionQuimica(cargado, AccionLaboratorio.MEZCLAR, CondicionesRequeridas.sinCondicionesEspeciales(), contacto),
                new TransicionQuimica(contacto, AccionLaboratorio.ACTIVAR_IGNICION, CondicionesRequeridas.sinCondicionesEspeciales(), reaccionando),
                new TransicionQuimica(reaccionando, AccionLaboratorio.FINALIZAR, CondicionesRequeridas.sinCondicionesEspeciales(), completado)
        );
        return new AutomataQuimico(preparacion, estados, transiciones, List.of(completado));
    }

    public static AutomataQuimico crearAutomataParaReaccion(String idReaccion) {
        GramaticaRegular gramatica = GramaticasProtocolos.buscar(idReaccion).orElse(null);
        if (gramatica != null) return CompiladorGramaticaRegular.compilar(gramatica);
        return switch (idReaccion) {
            case "R_CO2" -> crearAutomataCombustionCarbono();
            case "R_NH3" -> crearAutomataHaberBosch();
            case "R_NaCl" -> crearAutomataSintesisNaCl();
            case "R_HCl" -> crearAutomataSintesisHCl();
            case "R_Fe2O3" -> crearAutomataOxidacionHierro();
            case "R_CORROSION_FE" -> crearAutomataCorrosionHierro();
            case "R_CH4" -> crearAutomataCombustionMetano();
            case "R_H2S" -> crearAutomataSulfuroHidrogeno();
            case "R_CuO" -> crearAutomataOxidacionCobre();
            case "R_CaO" -> crearAutomataOxidoCalcio();
            case "R_CaOH2" -> crearAutomataHidratacionCalcio();
            case "R_CaCO3" -> crearAutomataPrecipitacionCarbonato();
            case "R_CALCINACION_CACO3" -> crearAutomataCalcinacionCaliza();
            case "R_SiO2" -> crearAutomataOxidoSilicio();
            case "R_MgO" -> crearAutomataOxidoMagnesio();
            case "R_Al2O3" -> crearAutomataOxidoAluminio();
            case "R_Ca3PO42" -> crearAutomataFosfatoCalcio();
            case "R_DIAMANTE_HPHT" -> crearAutomataDiamanteHPHT();
            case "R_SO2" -> crearAutomataOxidacionAzufre();
            case "R_DISOLUCION_NACL" -> crearAutomataDisolucionNaCl();
            case "R_SEPARACION_NACL" -> crearAutomataSeparacionSolucion();
            case "R_REDOX_CU" -> crearAutomataRedox();
            case "R_OXIDO_METALICO" -> crearAutomataOxidoMetalico();
            default -> crearAutomataEstandar();
        };
    }
}
