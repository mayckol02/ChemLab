package com.chemlab.grammar;

import com.chemlab.model.enums.AccionLaboratorio;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Gramáticas fuente de los protocolos migrados al modelo formal. */
public final class GramaticasProtocolos {

    private static final Map<String, GramaticaRegular> POR_REACCION = Map.of(
            "R_H2O", crearSintesisAgua(),
            "R_DIAMANTE_CVD", crearDiamanteCvd()
    );

    private GramaticasProtocolos() {}

    public static Optional<GramaticaRegular> buscar(String idReaccion) {
        if (idReaccion == null) return Optional.empty();
        return Optional.ofNullable(POR_REACCION.get(idReaccion));
    }

    public static List<GramaticaRegular> obtenerTodas() {
        return POR_REACCION.values().stream()
                .sorted(java.util.Comparator.comparing(GramaticaRegular::getId))
                .toList();
    }

    private static GramaticaRegular crearSintesisAgua() {
        SimboloNoTerminal preparacion = new SimboloNoTerminal("PREPARACION", "Preparación de reactivos");
        SimboloNoTerminal cargado = new SimboloNoTerminal("PREPARADO", "Reactivos cargados");
        SimboloNoTerminal mezclado = new SimboloNoTerminal("MEZCLADO", "Mezcla en progreso");
        SimboloNoTerminal reaccionando = new SimboloNoTerminal("REACCIONANDO", "Reacción en curso");
        SimboloNoTerminal completado = new SimboloNoTerminal("COMPLETADO", "Proceso completado");

        return new GramaticaRegular("R_H2O", preparacion,
                List.of(preparacion, cargado, mezclado, reaccionando, completado),
                List.of(
                        ProduccionRegular.avanzar(preparacion, AccionLaboratorio.CARGAR_REACTIVO, cargado),
                        ProduccionRegular.avanzar(cargado, AccionLaboratorio.MEZCLAR, mezclado),
                        ProduccionRegular.avanzar(mezclado, AccionLaboratorio.CALENTAR, reaccionando),
                        ProduccionRegular.avanzar(reaccionando, AccionLaboratorio.FINALIZAR, completado),
                        ProduccionRegular.epsilon(completado)
                ));
    }

    private static GramaticaRegular crearDiamanteCvd() {
        SimboloNoTerminal preparacion = new SimboloNoTerminal("PREPARACION", "Preparación de cámara CVD");
        SimboloNoTerminal cargado = new SimboloNoTerminal("CARGADO", "Gases CH₄ y H₂ introducidos");
        SimboloNoTerminal plasma = new SimboloNoTerminal("PLASMA", "Plasma de microondas activado");
        SimboloNoTerminal deposicion = new SimboloNoTerminal("DEPOSICION", "Deposición epitaxial de carbono");
        SimboloNoTerminal completado = new SimboloNoTerminal("COMPLETADO", "Diamante CVD obtenido");

        return new GramaticaRegular("R_DIAMANTE_CVD", preparacion,
                List.of(preparacion, cargado, plasma, deposicion, completado),
                List.of(
                        ProduccionRegular.avanzar(preparacion, AccionLaboratorio.CARGAR_REACTIVO, cargado),
                        ProduccionRegular.avanzar(cargado, AccionLaboratorio.INICIAR_PLASMA, plasma),
                        ProduccionRegular.avanzar(plasma, AccionLaboratorio.CALENTAR, deposicion),
                        ProduccionRegular.avanzar(deposicion, AccionLaboratorio.FINALIZAR, completado),
                        ProduccionRegular.epsilon(completado)
                ));
    }
}
