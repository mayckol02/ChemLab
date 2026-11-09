package com.chemlab.game.procedure;

import com.chemlab.model.entity.DefinicionPractica;
import com.chemlab.model.entity.Reaccion;

import java.util.List;
import java.util.Objects;

/** Presenta las etapas declaradas por cada práctica y rutas de síntesis relacionadas. */
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

        if (idReaccion.equals("R_CO2") || idReaccion.equals("R_CH4")
                || idReaccion.equals("R_CALCINACION_CACO3")) {
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

    public static InfoPasoProcedimiento obtenerProtocoloPaso(
            Reaccion reaccion,
            String estadoAutomataId
    ) {
        Objects.requireNonNull(reaccion, "La reacción es obligatoria");
        DefinicionPractica definicion = reaccion.getDefinicionPractica()
                .orElseThrow(() -> new IllegalStateException(
                        "La reacción " + reaccion.getId() + " no tiene una práctica declarativa"));
        String estadoId = estadoAutomataId == null ? definicion.estadoInicialId() : estadoAutomataId;
        DefinicionPractica.EtapaPractica etapa = definicion.etapas().stream()
                .filter(candidata -> candidata.id().equals(estadoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La práctica " + reaccion.getId() + " no define la etapa " + estadoId));
        List<InfoReactivoFase> fases = etapa.fasesReactivos().stream()
                .map(fase -> new InfoReactivoFase(
                        fase.formula(),
                        fase.nombre(),
                        fase.rol(),
                        EstadoManejo.valueOf(fase.estadoManejo().name()),
                        fase.faseFisica(),
                        fase.descripcionUbicacion()))
                .toList();
        return new InfoPasoProcedimiento(
                reaccion.getId(),
                etapa.id(),
                etapa.nombre(),
                etapa.equipoTecnico(),
                etapa.temperaturaActual(),
                etapa.temperaturaObjetivo(),
                etapa.presionActual(),
                etapa.presionObjetivo(),
                etapa.atmosferaRequerida(),
                etapa.catalizadorEstado(),
                fases,
                etapa.explicacionManejo(),
                etapa.accionSiguienteSugerida());
    }
}
