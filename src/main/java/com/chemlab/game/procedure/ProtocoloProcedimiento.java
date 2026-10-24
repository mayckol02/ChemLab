package com.chemlab.game.procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Protocolo de procedimientos reales de laboratorio y manejo físico de reactivos.
 *
 * <p>Explica con exactitud científica cómo se maneja cada elemento y compuesto
 * en cada etapa del proceso (si están en compartimentos separados, en contacto físico,
 * disueltos, mezclados o activados en plasma), y detalla las rutas alternativas
 * de síntesis cuando un compuesto puede obtenerse por diferentes vías (como el diamante
 * por HPHT o CVD, o el CO₂ por combustión vs calcinación).</p>
 */
public final class ProtocoloProcedimiento {

    public enum EstadoManejo {
        EN_ALMACEN("En mesa de preparación / Almacén", "📦", "Sin introducir al reactor"),
        SEPARADO_EN_CAMARA("En compartimento separado en la cámara", "🔲", "En su soporte, sin contacto con otros"),
        EN_CONTACTO_SOLIDO("En contacto físico directo", "🤝", "Superficies en contacto en fase sólida"),
        MEZCLADO_HOMOGENEO("Mezcla fluida homogénea", "🌪", "Fase fluida homogénea y uniforme"),
        DISUELTO_EN_SOLVENTE("Disuelto a nivel molecular / atómico", "💧", "En solución acuosa o flujo metálico fundido"),
        ACTIVADO_EN_PLASMA("Ionizado en plasma de alta energía", "⚡", "Radicales libres activos a baja presión"),
        EN_REACCION_TERMICA("En reacción activa sostenida", "🔥", "Reacción sostenida bajo temperatura/presión"),
        PRODUCTO_FORMADO("Transformado en producto", "✨", "Red cristalina o molécula sintetizada");

        private final String etiqueta;
        private final String icono;
        private final String descripcionCorta;

        EstadoManejo(String etiqueta, String icono, String descripcionCorta) {
            this.etiqueta = etiqueta;
            this.icono = icono;
            this.descripcionCorta = descripcionCorta;
        }

        public String getEtiqueta() { return etiqueta; }
        public String getIcono() { return icono; }
        public String getDescripcionCorta() { return descripcionCorta; }
    }

    public record InfoReactivoFase(
            String formula,
            String nombre,
            String rol,
            EstadoManejo estadoManejo,
            String faseFisica,
            String descripcionUbicacion
    ) {}

    public record InfoPasoProcedimiento(
            String idReaccion,
            String estadoAutomataId,
            String nombreEstado,
            String equipoTecnico,
            double temperaturaActual,
            double temperaturaObjetivo,
            double presionActual,
            double presionObjetivo,
            String atmosferaRequerida,
            String catalizadorEstado,
            List<InfoReactivoFase> reactivosFases,
            String explicacionManejo,
            String accionSiguienteSugerida
    ) {}

    public record InfoRutaSintesis(
            String compuestoId,
            String nombreCompuesto,
            String idReaccion,
            String nombreRuta,
            String badge,
            String metodo,
            String equipo,
            String descripcionCientifica,
            String comparacionConOtrasRutas
    ) {}

    private ProtocoloProcedimiento() {}

    /**
     * Retorna la información de rutas alternativas de síntesis para un compuesto o reacción.
     */
    public static List<InfoRutaSintesis> obtenerRutasDisponibles(String idReaccion) {
        if (idReaccion == null) return List.of();

        if (idReaccion.contains("DIAMANTE")) {
            return List.of(
                    new InfoRutaSintesis(
                            "DIAMANTE", "Diamante", "R_DIAMANTE_HPHT",
                            "Ruta 1: HPHT (Alta Presión y Alta Temperatura)",
                            "💎 RUTA HPHT [5.5 GPa | 1500°C]",
                            "Disolución y transporte en flujo metálico solvente (Fe/Ni)",
                            "Prensa Cúbica / Yunque de Diamante (5.5 GPa)",
                            "Utiliza grafito sólido y semilla monocristalina sometidos a 55,000 atm (5.5 GPa) y 1500 °C. Un flujo metálico de Fe-Ni se funde, disuelve el grafito metaestable y transporta el carbono por gradiente térmico para precipitarlo como diamante cúbico.",
                            "Diferencia clave con CVD: HPHT requiere presiones extremas de gigapascales y solvente metálico líquido para entrar en la zona termodinámica del diamante; CVD opera a baja presión/vacío usando plasma gaseoso."
                    ),
                    new InfoRutaSintesis(
                            "DIAMANTE", "Diamante", "R_DIAMANTE_CVD",
                            "Ruta 2: CVD (Deposición Química de Vapor Asistida por Plasma)",
                            "💎 RUTA CVD [Vacío | 1000°C | Plasma RF]",
                            "Disociación de hidrocarburo gaseoso por microondas",
                            "Reactor de Vacío con Plasma de Microondas (2.45 GHz)",
                            "Utiliza gas metano (CH₄) diluido al 1% en hidrógeno (H₂) en cámara a baja presión (0.05 atm). Un plasma de microondas genera radicales de H* que decapan selectivamente el grafito no deseado (sp²), permitiendo únicamente la deposición de diamante sp³ sobre el sustrato a 1000 °C.",
                            "Diferencia clave con HPHT: No requiere altas presiones ni catalizador metálico; permite crear láminas de diamante de pureza electrónica monocristalina sobre obleas amplias."
                    )
            );
        }

        if (idReaccion.equals("R_CO2") || idReaccion.equals("R_CH4") || idReaccion.equals("R_CALCINACION_CACO3")) {
            return List.of(
                    new InfoRutaSintesis(
                            "CO2", "Dióxido de carbono", "R_CO2",
                            "Ruta 1: Combustión Directa de Carbono",
                            "🔥 COMBUSTIÓN DIRECTA [500°C | O₂]",
                            "Oxidación térmica de sólido amorfo o grafito",
                            "Horno de combustión sellado",
                            "El carbono sólido reacciona directamente con oxígeno gaseoso: C + O₂ → CO₂ liberando 393.5 kJ/mol.",
                            "Produce únicamente CO₂ gaseoso sin vapor de agua."
                    ),
                    new InfoRutaSintesis(
                            "CO2", "Dióxido de carbono", "R_CH4",
                            "Ruta 2: Combustión de Hidrocarburo (Metano)",
                            "🔥 COMBUSTIÓN HIDROCARBURO [CH₄ + 2O₂]",
                            "Combustión completa de gas natural",
                            "Cámara de combustión con mezclador de gases",
                            "El metano reacciona con exceso de oxígeno: CH₄ + 2O₂ → CO₂ + 2H₂O. Ambos gases se generan simultáneamente.",
                            "Produce CO₂ y agua vaporizada como coproducto."
                    ),
                    new InfoRutaSintesis(
                            "CO2", "Dióxido de carbono", "R_CALCINACION_CACO3",
                            "Ruta 3: Calcinación Térmica de Piedra Caliza",
                            "🏛 CALCINACIÓN CALIZA [900°C | Sin O₂]",
                            "Descomposición térmica endotérmica",
                            "Horno rotatorio de calcinación",
                            "Descompone carbonato de calcio sólido por calor puro sin comburente: CaCO₃ → CaO + CO₂↑ a 900 °C.",
                            "No requiere oxígeno; libera CO₂ puro y cal viva (CaO)."
                    )
            );
        }

        if (idReaccion.equals("R_CaO")) {
            return List.of(
                    new InfoRutaSintesis(
                            "CaO", "Óxido de calcio (Cal viva)", "R_CaO",
                            "Ruta 1: Oxidación Directa de Calcio Metálico",
                            "⚡ OXIDACIÓN METÁLICA [400°C]",
                            "Oxidación de metal reactivo con oxígeno",
                            "Horno de atmósfera seca",
                            "El calcio elemental reacciona directamente con O₂: 2Ca + O₂ → 2CaO.",
                            "Ruta rápida de síntesis en laboratorio químico."
                    ),
                    new InfoRutaSintesis(
                            "CaO", "Óxido de calcio (Cal viva)", "R_CALCINACION_CACO3",
                            "Ruta 2: Calcinación de Caliza",
                            "🏛 CALCINACIÓN INDUSTRIAL [900°C]",
                            "Descarbonatación térmica de carbonato",
                            "Horno industrial de cal",
                            "Calentamiento de CaCO₃ a 900 °C: CaCO₃ → CaO + CO₂.",
                            "Método industrial milenario para producción masiva de cal viva."
                    )
            );
        }

        if (idReaccion.equals("R_H2O")) {
            return List.of(
                    new InfoRutaSintesis(
                            "H2O", "Agua", "R_H2O",
                            "Ruta 1: Síntesis Directa de Hidrógeno y Oxígeno",
                            "💧 SÍNTESIS ELEMENTAL [Ignición]",
                            "Reacción de formación pura 2H₂ + O₂ → 2H₂O",
                            "Matraz Erlenmeyer con chispero sellado",
                            "Mezcla estequiométrica 2:1 activada con chispa eléctrica; libera gran energía exotérmica y condensa agua pura.",
                            "Síntesis directa y limpia sin subproductos."
                    ),
                    new InfoRutaSintesis(
                            "H2O", "Agua", "R_CH4",
                            "Ruta 2: Coproducto de Combustión de Metano",
                            "🔥 COMBUSTIÓN DE CH₄",
                            "Oxidación de hidrógeno molecular enlazado",
                            "Cámara de combustión",
                            "El hidrógeno del metano se oxida produciendo 2 moléculas de agua por cada molécula de metano consumida.",
                            "Genera CO₂ junto al agua."
                    )
            );
        }

        return List.of();
    }

    /**
     * Devuelve el protocolo y estado de cada reactivo en la etapa actual del autómata.
     */
    public static InfoPasoProcedimiento obtenerProtocoloPaso(
            String idReaccion,
            String estadoAutomataId
    ) {
        if (idReaccion == null) idReaccion = "R_H2O";
        if (estadoAutomataId == null) estadoAutomataId = "PREPARACION";

        // DIAMANTE HPHT
        if (idReaccion.equals("R_DIAMANTE_HPHT")) {
            return switch (estadoAutomataId) {
                case "PREPARACION" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Preparación de la Celda HPHT",
                        "Prensa Cúbica HPHT de Carburo de Tungsteno (5.5 GPa)",
                        25.0, 1500.0, 1.0, 55000.0, "Vacío / Gas Inerte", "Flujo Fe-Ni en lámina",
                        List.of(
                                new InfoReactivoFase("C", "Grafito", "Fuente de Carbono", EstadoManejo.EN_ALMACEN, "Sólido (pastilla prensada)", "En mesa de preparación"),
                                new InfoReactivoFase("Fe/Ni", "Aleación Fe-Ni", "Flujo Metálico Solvente", EstadoManejo.EN_ALMACEN, "Sólido (lámina metálica)", "En contenedor sellado"),
                                new InfoReactivoFase("C (semilla)", "Semilla de Diamante", "Núcleo de Cristalización", EstadoManejo.EN_ALMACEN, "Sólido (microcristal natural)", "En cápsula de molibdeno")
                        ),
                        "Los reactivos se encuentran TOTALMENTE SEPARADOS en la mesa de preparación. El grafito purificado se troquela en cilindros densos, mientras la semilla de diamante monocristalino se inspecciona para orientar su cara cristalina [100]. Aún no se introducen a la celda.",
                        "Ejecuta 'CARGAR_REACTIVO' para introducir el grafito y la semilla en la celda cúbica."
                );
                case "CARGADO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Carga en Compartimentos Aislados",
                        "Prensa Cúbica HPHT (Cámara cargada)",
                        25.0, 1500.0, 1.0, 55000.0, "Cámara sellada", "Pendiente de inserción",
                        List.of(
                                new InfoReactivoFase("C", "Grafito", "Fuente de Carbono", EstadoManejo.SEPARADO_EN_CAMARA, "Sólido (cilindro de alta pureza)", "Zona superior de la cápsula térmica"),
                                new InfoReactivoFase("C (semilla)", "Semilla de Diamante", "Núcleo de Cristalización", EstadoManejo.SEPARADO_EN_CAMARA, "Sólido (orientación [100])", "Base inferior más fría de la celda"),
                                new InfoReactivoFase("Fe/Ni", "Aleación Fe-Ni", "Flujo Metálico Solvente", EstadoManejo.EN_ALMACEN, "Sólido", "Listo para inserción intermedia")
                        ),
                        "El grafito y la semilla de diamante están CARGADOS EN COMPARTIMENTOS SEPARADOS en los extremos de la celda de alta presión. Permanecen aislados para que no exista grafitización desordenada sin el solvente metálico regulador.",
                        "Ejecuta 'AGREGAR_CATALIZADOR' para intercalar la lámina de flujo solvente Fe-Ni entre el grafito y la semilla."
                );
                case "CATALIZADOR" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Flujo Metálico en Contacto Sólido",
                        "Prensa Cúbica HPHT (Ensamble completo)",
                        25.0, 1500.0, 1.0, 55000.0, "Atmósfera protegida", "Fe-Ni intercalado en contacto",
                        List.of(
                                new InfoReactivoFase("C", "Grafito", "Fuente de Carbono", EstadoManejo.EN_CONTACTO_SOLIDO, "Sólido", "En contacto con cara superior del flujo"),
                                new InfoReactivoFase("Fe/Ni", "Aleación Fe-Ni", "Flujo Metálico Solvente", EstadoManejo.EN_CONTACTO_SOLIDO, "Sólido (intercalado)", "En contacto directo con grafito y semilla"),
                                new InfoReactivoFase("C (semilla)", "Semilla de Diamante", "Núcleo de Cristalización", EstadoManejo.EN_CONTACTO_SOLIDO, "Sólido", "En contacto con cara inferior del flujo")
                        ),
                        "Los reactivos se encuentran ahora EN CONTACTO FÍSICO DIRECTO en fase sólida (Grafito ↔ Flujo Fe/Ni ↔ Semilla). El metal aún no está fundido, por lo que no hay difusión de carbono todavía.",
                        "Ejecuta 'COMPRIMIR' para accionar los 6 pistones hidráulicos y alcanzar 5.5 GPa (55,000 atm)."
                );
                case "PRESURIZADO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Presurización Extrema a 5.5 GPa",
                        "Prensa Cúbica HPHT (Presión hiperbárica activa)",
                        25.0, 1500.0, 55000.0, 55000.0, "Cámara a 5.5 GPa", "Fe-Ni bajo 55,000 atm",
                        List.of(
                                new InfoReactivoFase("C", "Grafito", "Fuente de Carbono", EstadoManejo.EN_CONTACTO_SOLIDO, "Sólido bajo 5.5 GPa", "Comprimido en zona caliente"),
                                new InfoReactivoFase("Fe/Ni", "Aleación Fe-Ni", "Flujo Metálico Solvente", EstadoManejo.EN_CONTACTO_SOLIDO, "Sólido bajo 5.5 GPa", "Soporte hiperbárico"),
                                new InfoReactivoFase("C (semilla)", "Semilla de Diamante", "Núcleo de Cristalización", EstadoManejo.EN_CONTACTO_SOLIDO, "Sólido bajo 5.5 GPa", "Zona termodinámica estable")
                        ),
                        "El sistema completo ha alcanzado 5.5 GPa (55,000 atmósferas). A esta presión colosal, el diamante es la fase termodinámicamente más estable del carbono (curva Berman-Simon), pero los átomos aún no tienen energía cinética para reorganizarse.",
                        "Ejecuta 'CALENTAR' para encender el calefactor resistivo de grafito y alcanzar 1500 °C."
                );
                case "CRECIMIENTO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Disolución en Flujo Líquido y Crecimiento Epitaxial",
                        "Prensa Cúbica HPHT (1500°C / 5.5 GPa)",
                        1500.0, 1500.0, 55000.0, 55000.0, "Condiciones de síntesis sostenidas", "Fe-Ni fundido disolviendo carbono",
                        List.of(
                                new InfoReactivoFase("C (disuelto)", "Carbono atómico", "En transporte", EstadoManejo.DISUELTO_EN_SOLVENTE, "Disolución atómica en metal fundido", "Difundiéndose por gradiente térmico"),
                                new InfoReactivoFase("Fe/Ni", "Aleación Fe-Ni", "Solvente Metálico Líquido", EstadoManejo.DISUELTO_EN_SOLVENTE, "Líquido fundido a 1500°C", "Medio de transporte activo"),
                                new InfoReactivoFase("C (diamante)", "Diamante en crecimiento", "Cristalización activa", EstadoManejo.PRODUCTO_FORMADO, "Sólido cristalino sp³", "Precipitándose sobre la semilla")
                        ),
                        "A 1500 °C el flujo Fe-Ni se FUNDE POR COMPLETO. El grafito metaestable se disuelve continuamente en el metal fundido. Por gradiente térmico (~30°C/mm), el carbono disuelto migra hacia la semilla más fría donde precipita en red tetraédrica pura sp³ como DIAMANTE.",
                        "Ejecuta 'FINALIZAR' para enfriar la celda, liberar la presión hidráulica y extraer el diamante."
                );
                default -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Diamante HPHT Sintetizado",
                        "Prensa Cúbica HPHT (Despresurizada y fría)",
                        25.0, 25.0, 1.0, 1.0, "Ambiente", "Recuperado y reciclable",
                        List.of(
                                new InfoReactivoFase("DIAMANTE", "Diamante Sintético HPHT", "Producto Monocristalino", EstadoManejo.PRODUCTO_FORMADO, "Sólido cristalino (dureza 10 Mohs)", "Monocristal octaédrico recuperado")
                        ),
                        "El proceso HPHT ha concluido exitosamente. Tras disolver la envoltura metálica residual en baño ácido, se recupera el diamante monocristalino sintético de máxima dureza.",
                        "Experimento completado. Registrado en el cuaderno de laboratorio."
                );
            };
        }

        // DIAMANTE CVD
        if (idReaccion.equals("R_DIAMANTE_CVD")) {
            return switch (estadoAutomataId) {
                case "PREPARACION" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Preparación del Reactor de Plasma CVD",
                        "Cámara de Deposición Química de Vapor (CVD Microondas)",
                        25.0, 1000.0, 1.0, 0.05, "Cámara abierta", "Plasma inactivo",
                        List.of(
                                new InfoReactivoFase("CH4", "Metano", "Gas Precursor (Carbono)", EstadoManejo.EN_ALMACEN, "Gas puro en cilindro", "Línea de alimentación de gas"),
                                new InfoReactivoFase("H2", "Hidrógeno", "Gas Portador y Decapante", EstadoManejo.EN_ALMACEN, "Gas puro en cilindro", "Línea de alimentación de gas"),
                                new InfoReactivoFase("Sustrato", "Oblea de Silicio/Diamante", "Sustrato de Crecimiento", EstadoManejo.EN_ALMACEN, "Sólido pulido", "En portamuestras limpio")
                        ),
                        "Los gases precursores (Metano e Hidrógeno) se encuentran aislados en cilindros externos de alta pureza. El sustrato de silicio está preparado y pulido con polvo de diamante en la mesa de montaje.",
                        "Ejecuta 'CARGAR_REACTIVO' para montar el sustrato en el portamuestras del reactor."
                );
                case "CARGADO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Evacuación a Vacío e Inyección de Mezcla Gaseosa",
                        "Cámara de Vacío CVD (0.05 atm)",
                        25.0, 1000.0, 0.05, 0.05, "Vacío tenue CH₄/H₂", "Microondas apagadas",
                        List.of(
                                new InfoReactivoFase("CH4/H2", "Mezcla CH₄ (1%) + H₂ (99%)", "Gases Precursores", EstadoManejo.MEZCLADO_HOMOGENEO, "Gas a baja presión (0.05 atm)", "Mezcla uniforme en la cámara"),
                                new InfoReactivoFase("Sustrato", "Oblea de Crecimiento", "Superficie de Deposición", EstadoManejo.SEPARADO_EN_CAMARA, "Sólido en portamuestras", "Fijado bajo la antena de microondas")
                        ),
                        "La cámara se ha evacuado a baja presión (0.05 atm). Los gases se encuentran MEZCLADOS DE FORMA HOMOGÉNEA en flujo continuo (99% H₂ y 1% CH₄). El flujo de H₂ es masivo porque decapará cualquier enlace no diamante que se intente formar.",
                        "Ejecuta 'INICIAR_PLASMA' para encender el generador de microondas (2.45 GHz) y crear el plasma ionizado."
                );
                case "PLASMA", "EN_PLASMA" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Plasma de Microondas Ionizado",
                        "Reactor CVD (Plasma violeta de hidrógeno activo)",
                        25.0, 1000.0, 0.05, 0.05, "Plasma de hidrógeno/metano", "Radicales H* y CH₃* activos",
                        List.of(
                                new InfoReactivoFase("H*", "Radicales de Hidrógeno", "Agente Decapante sp²", EstadoManejo.ACTIVADO_EN_PLASMA, "Plasma ionizado", "Bola de plasma sobre el sustrato"),
                                new InfoReactivoFase("CH3*", "Radicales Metilo", "Precursor de Carbono sp³", EstadoManejo.ACTIVADO_EN_PLASMA, "Plasma ionizado", "Radicales activados químicamente"),
                                new InfoReactivoFase("Sustrato", "Sustrato de Si", "Soporte Térmico", EstadoManejo.SEPARADO_EN_CAMARA, "Sólido a temperatura ambiente", "Listo para calentamiento inductivo")
                        ),
                        "El plasma de microondas ha IONIZADO la mezcla gaseosa. El hidrógeno se disocia en radicales atómicos H* que destruyen selectivamente cualquier grafito (sp²). Los radicales metilo (CH₃*) están listos para anclarse en red sp³.",
                        "Ejecuta 'CALENTAR' para calentar el portamuestras del sustrato a 1000 °C."
                );
                case "DEPOSICION" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Deposición Epitaxial Capa por Capa",
                        "Reactor CVD (1000°C / Plasma activo)",
                        1000.0, 1000.0, 0.05, 0.05, "Plasma activo a 1000°C", "Radicales en deposición continua",
                        List.of(
                                new InfoReactivoFase("C (sp³)", "Carbono diamantino", "Película en Crecimiento", EstadoManejo.PRODUCTO_FORMADO, "Sólido monocristalino", "Creciendo epitaxialmente sobre el sustrato"),
                                new InfoReactivoFase("H2", "Gas Hidrógeno", "Gas Efluente", EstadoManejo.ACTIVADO_EN_PLASMA, "Gas caliente efluente", "Evacuado por la bomba de vacío")
                        ),
                        "A 1000 °C en la superficie del sustrato, los radicales de carbono se DEPOSITAN ÁTOMO POR ÁTOMO en orientación tetraédrica diamantina. El gas efluente H₂ se evacúa continuamente.",
                        "Ejecuta 'FINALIZAR' para cortar el plasma, enfriar bajo vacío y retirar el diamante CVD puro."
                );
                default -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Diamante CVD Monocristalino Obtenido",
                        "Cámara CVD (Enfriada y purgada)",
                        25.0, 25.0, 1.0, 1.0, "Ambiente purgado con N₂", "Finalizado",
                        List.of(
                                new InfoReactivoFase("DIAMANTE", "Diamante CVD Puro", "Película Monocristalina Óptica", EstadoManejo.PRODUCTO_FORMADO, "Sólido de pureza óptica 100%", "Película lista sobre el sustrato")
                        ),
                        "Se ha obtenido una lámina monocristalina de Diamante CVD de alta pureza óptica y electrónica sin impurezas metálicas.",
                        "Experimento completado y registrado."
                );
            };
        }

        // HABER-BOSCH
        if (idReaccion.equals("R_NH3")) {
            return switch (estadoAutomataId) {
                case "PREPARACION" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Preparación de Gases Reactivos",
                        "Reactor de Alta Presión Haber-Bosch",
                        25.0, 450.0, 1.0, 200.0, "Líneas de alimentación", "Lecho catalítico en espera",
                        List.of(
                                new InfoReactivoFase("N2", "Nitrógeno", "Reactivo (1 mol)", EstadoManejo.EN_ALMACEN, "Gas inerte", "Cilindro presurizado"),
                                new InfoReactivoFase("H2", "Hidrógeno", "Reactivo (3 mol)", EstadoManejo.EN_ALMACEN, "Gas combustible", "Cilindro presurizado")
                        ),
                        "El Nitrógeno y el Hidrógeno se encuentran en sus respectivos cilindros de almacenamiento por separado. Deben dosificarse en la estequiometría exacta 1:3 antes de comprimir.",
                        "Ejecuta 'CARGAR_REACTIVO' para dosificar y cargar los gases en el reactor."
                );
                case "PREPARADO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Gases Cargados en Proporción 1:3",
                        "Reactor Haber-Bosch (Cámara de mezcla)",
                        25.0, 450.0, 1.0, 200.0, "Mezcla gaseosa N₂/H₂", "Sin catalizador",
                        List.of(
                                new InfoReactivoFase("N2 + 3H2", "Mezcla estequiométrica", "Reactivos cargados", EstadoManejo.MEZCLADO_HOMOGENEO, "Gas homogéneo", "Mezcla en el bucle de circulación")
                        ),
                        "Los gases N₂ y H₂ se han MEZCLADO HOMOGÉNEAMENTE en la cámara en proporción exacta 1:3. Sin catalizador ni compresión, la reacción no puede ocurrir por la enorme fuerza del triple enlace N≡N.",
                        "Ejecuta 'AGREGAR_CATALIZADOR' para activar el lecho de óxido de hierro promovido con K₂O/Al₂O₃."
                );
                case "COMPRIMIDO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Compresión Hiperbárica a 200 atm",
                        "Reactor Haber-Bosch (Compresor multietapa a 200 atm)",
                        25.0, 450.0, 200.0, 200.0, "N₂ + 3H₂ a 200 atm", "Catalizador Fe listo",
                        List.of(
                                new InfoReactivoFase("N2 + 3H2", "Gases a 200 atm", "Reactivos comprimidos", EstadoManejo.MEZCLADO_HOMOGENEO, "Gas denso hiperbárico", "En contacto con el lecho de Fe")
                        ),
                        "La mezcla de gases se ha COMPRIMIDO a 200 atmósferas. Según el principio de Le Chatelier, la alta presión favorece la formación de amoníaco porque 4 moles de gas se convierten en 2 moles de producto.",
                        "Ejecuta 'CALENTAR' para calentar el reactor catalítico a 450 °C."
                );
                case "REACCIONANDO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Síntesis Catalítica Heterogénea a 450°C",
                        "Reactor Catalítico Haber-Bosch (450°C / 200 atm)",
                        450.0, 450.0, 200.0, 200.0, "Mezcla en reacción", "Catalizador de Fe activo",
                        List.of(
                                new InfoReactivoFase("N2 + 3H2", "Moléculas quimisorbidas", "En disociación superficial", EstadoManejo.EN_REACCION_TERMICA, "Gas en superficie de Fe", "Adsorbidas en la red del catalizador"),
                                new InfoReactivoFase("NH3", "Amoníaco gas", "Producto sintetizado", EstadoManejo.PRODUCTO_FORMADO, "Gas caliente a 200 atm", "Fluyendo hacia el condensador")
                        ),
                        "A 450 °C y 200 atm sobre el hierro poroso, las moléculas de N₂ y H₂ se quimisorben en la superficie del catalizador rompiendo el triple enlace N≡N y recombinándose en amoníaco (NH₃).",
                        "Ejecuta 'FINALIZAR' para condensar el amoníaco a líquido (-33 °C) y recolectarlo."
                );
                default -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Amoníaco NH₃ Sintetizado",
                        "Condensador de Amoníaco",
                        -33.0, -33.0, 1.0, 1.0, "Gas inerte recuperado", "Fe regenerado",
                        List.of(
                                new InfoReactivoFase("NH3", "Amoníaco líquido", "Producto final", EstadoManejo.PRODUCTO_FORMADO, "Líquido licuado", "Tanque criogénico de almacenamiento")
                        ),
                        "El amoníaco se ha sintetizado y licuado por condensación criogénica.",
                        "Experimento completado exitosamente."
                );
            };
        }

        // AGUA H2O
        if (idReaccion.equals("R_H2O")) {
            return switch (estadoAutomataId) {
                case "PREPARACION" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Preparación de Reactivos Gaseosos",
                        "Matraz Erlenmeyer con tapón sellado y chispero",
                        25.0, 25.0, 1.0, 1.0, "Aire ambiente", "Sin catalizador",
                        List.of(
                                new InfoReactivoFase("H2", "Gas Hidrógeno", "Combustible (2 mol)", EstadoManejo.EN_ALMACEN, "Gas diatómico", "Cilindro de H₂"),
                                new InfoReactivoFase("O2", "Gas Oxígeno", "Comburente (1 mol)", EstadoManejo.EN_ALMACEN, "Gas diatómico", "Cilindro de O₂")
                        ),
                        "Los gases H₂ y O₂ se encuentran en contenedores SEPARADOS. El hidrógeno es altamente inflamable y debe manejarse con cuidado antes de conectarlo a la cámara de mezcla.",
                        "Ejecuta 'CARGAR_REACTIVO' para introducir ambos gases en el matraz de síntesis sellado."
                );
                case "PREPARADO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Gases Introducidos en el Matraz",
                        "Matraz de Síntesis Sellado",
                        25.0, 25.0, 1.0, 1.0, "Cámara sellada", "Chispero montado",
                        List.of(
                                new InfoReactivoFase("H2", "Hidrógeno", "Capa superior", EstadoManejo.SEPARADO_EN_CAMARA, "Gas ligero", "Estratificado en parte alta"),
                                new InfoReactivoFase("O2", "Oxígeno", "Capa inferior", EstadoManejo.SEPARADO_EN_CAMARA, "Gas denso", "Estratificado en parte baja")
                        ),
                        "Los gases están dentro del matraz pero se encuentran ESTRATIFICADOS POR DENSIDAD (el H₂ es 16 veces más liviano que el O₂). Aún no forman una mezcla homogénea reactiva.",
                        "Ejecuta 'MEZCLAR' para agitar los gases y formar una mezcla estequiométrica uniforme 2:1."
                );
                case "MEZCLADO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Mezcla Estequiométrica Homogénea (Gas detonante)",
                        "Matraz de Síntesis Sellado",
                        25.0, 25.0, 1.0, 1.0, "Mezcla uniforme H₂/O₂", "Chispero listo",
                        List.of(
                                new InfoReactivoFase("2H2 + O2", "Mezcla de gas detonante", "Reactivos homogéneos", EstadoManejo.MEZCLADO_HOMOGENEO, "Gas homogéneo a 25°C", "Distribuido uniformemente")
                        ),
                        "Los gases están MEZCLADOS HOMOGÉNEAMENTE a nivel molecular. La mezcla es termodinámicamente reactiva pero cinéticamente estable a 25 °C por la barrera de activación.",
                        "Ejecuta 'CALENTAR' o 'ACTIVAR_IGNICION' para proporcionar la chispa de energía de activación."
                );
                case "REACCIONANDO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Combustión Rápida Exotérmica",
                        "Matraz de Síntesis (Llama invisible de hidrógeno)",
                        600.0, 600.0, 1.0, 1.0, "Vapor de agua generado", "Reacción en cadena",
                        List.of(
                                new InfoReactivoFase("H2O (vapor)", "Vapor de agua sobrecalentado", "Producto gaseoso", EstadoManejo.EN_REACCION_TERMICA, "Gas caliente", "Llenando el volumen del matraz")
                        ),
                        "La ignición desencadenó la reacción en cadena: 2H₂ + O₂ → 2H₂O + 572 kJ. La temperatura se eleva momentáneamente y se forma vapor de agua que empieza a empañar el cristal.",
                        "Ejecuta 'FINALIZAR' para enfriar y condensar el agua líquida pura."
                );
                default -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Agua H₂O Líquida Obtenida",
                        "Matraz Erlenmeyer con agua pura condensada",
                        25.0, 25.0, 1.0, 1.0, "Condensación a temperatura ambiente", "Finalizado",
                        List.of(
                                new InfoReactivoFase("H2O", "Agua pura", "Producto condensado", EstadoManejo.PRODUCTO_FORMADO, "Líquido transparente (1.00 g/mL)", "Fondo del matraz")
                        ),
                        "El vapor se ha condensado en agua líquida pura en el fondo del matraz.",
                        "Síntesis completada con éxito."
                );
            };
        }

        // DISOLUCIÓN Y SEPARACIÓN DE NACL
        if (idReaccion.equals("R_DISOLUCION_NACL")) {
            return switch (estadoAutomataId) {
                case "PREPARACION" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Preparación de Sal y Agua",
                        "Vaso de Precipitados con Agitador Magnético",
                        25.0, 25.0, 1.0, 1.0, "Aire ambiente", "Sin catalizador",
                        List.of(
                                new InfoReactivoFase("NaCl", "Cloruro de sodio", "Soluto sólido", EstadoManejo.EN_ALMACEN, "Sólido cristalino cúbico", "En cápsula de pesaje"),
                                new InfoReactivoFase("H2O", "Agua destilada", "Solvente polar", EstadoManejo.EN_ALMACEN, "Líquido polar", "En probeta graduada")
                        ),
                        "El cloruro de sodio sólido (cristales blancos) y el agua destilada se encuentran SEPARADOS en sus recipientes de pesaje.",
                        "Ejecuta 'CARGAR_REACTIVO' para verter los cristales de sal en el fondo del vaso de precipitados."
                );
                case "CARGADO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Sal Cargada en el Vaso",
                        "Vaso de Precipitados (Fondo con cristales)",
                        25.0, 25.0, 1.0, 1.0, "Aire ambiente", "Agitador listo",
                        List.of(
                                new InfoReactivoFase("NaCl", "Cristales de sal", "Fase sólida", EstadoManejo.SEPARADO_EN_CAMARA, "Sólido en el fondo", "Descansando en el fondo del vaso"),
                                new InfoReactivoFase("H2O", "Agua destilada", "Solvente", EstadoManejo.EN_ALMACEN, "Líquido en probeta", "Lista para verter")
                        ),
                        "La sal sólida está en el fondo del vaso en fase seca. El agua aún no se ha incorporado.",
                        "Ejecuta 'AGREGAR_AGUA' para verter el agua sobre los cristales."
                );
                case "AGREGADO", "HUMEDECIDO" -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Agua Agregada (Fases heterogéneas separadas)",
                        "Vaso de Precipitados (Sistema bifásico)",
                        25.0, 25.0, 1.0, 1.0, "Aire ambiente", "Agitador magnético apagado",
                        List.of(
                                new InfoReactivoFase("NaCl", "Sal en fondo", "Fase sólida heterogénea", EstadoManejo.EN_CONTACTO_SOLIDO, "Sólido sedimentado", "En el fondo bajo la columna de agua"),
                                new InfoReactivoFase("H2O", "Columna de agua", "Fase líquida superior", EstadoManejo.EN_CONTACTO_SOLIDO, "Líquido sobrenadante", "Capa líquida sin mezclar")
                        ),
                        "El sistema tiene DOS FASES SEPARADAS: los cristales pesados están en el fondo y el agua pura está arriba. Aún no se ha producido la disolución completa porque falta agitación mecánica.",
                        "Ejecuta 'MEZCLAR' para activar el agitador magnético y forzar la solvatación iónica."
                );
                default -> new InfoPasoProcedimiento(
                        idReaccion, estadoAutomataId, "Solución Salina Homogénea Formada",
                        "Vaso de Precipitados (Solución acuosa)",
                        25.0, 25.0, 1.0, 1.0, "Aire ambiente", "Disolución completada",
                        List.of(
                                new InfoReactivoFase("NaCl(ac)", "Iones Na⁺ y Cl⁻ solvatados", "Solución homogénea", EstadoManejo.DISUELTO_EN_SOLVENTE, "Fase líquida homogénea", "Completamente disuelto a nivel iónico")
                        ),
                        "Los dipolos del agua han rodeado y separado los cationes Na⁺ y aniones Cl⁻, rompiendo la red cristalina y formando una SOLUCIÓN HOMOGÉNEA TRANSPARENTE.",
                        "Disolución completada con éxito."
                );
            };
        }

        // PROTOCOLO GENÉRICO REALISTA PARA LAS DEMÁS REACCIONES
        return new InfoPasoProcedimiento(
                idReaccion, estadoAutomataId, estadoAutomataId,
                "Equipo experimental de laboratorio",
                25.0, 100.0, 1.0, 1.0, "Controlada", "Estándar",
                List.of(
                        new InfoReactivoFase("Reactivos", "Reactivos químicos", "Fase del proceso",
                                estadoAutomataId.contains("COMPLETADO") ? EstadoManejo.PRODUCTO_FORMADO :
                                estadoAutomataId.contains("PREP") ? EstadoManejo.EN_ALMACEN :
                                estadoAutomataId.contains("CARG") ? EstadoManejo.SEPARADO_EN_CAMARA :
                                estadoAutomataId.contains("MEZC") ? EstadoManejo.MEZCLADO_HOMOGENEO :
                                EstadoManejo.EN_REACCION_TERMICA,
                                "Fase física adecuada", "En reactor de proceso")
                ),
                "En esta etapa [" + estadoAutomataId + "], los componentes químicos se manipulan siguiendo el protocolo experimental estándar, asegurando que las condiciones de temperatura, presión y atmósfera permitan la transición adecuada de los reactivos.",
                "Sigue la secuencia de acciones indicada en la Guía del Proceso."
        );
    }
}
