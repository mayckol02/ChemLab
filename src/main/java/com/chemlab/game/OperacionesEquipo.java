package com.chemlab.game;

import com.chemlab.model.enums.AccionLaboratorio;
import com.chemlab.model.enums.TipoEquipo;

import java.util.EnumSet;
import java.util.Set;

public final class OperacionesEquipo {

    private OperacionesEquipo() {}

    public static Set<AccionLaboratorio> disponibles(TipoEquipo equipo) {
        return switch (equipo) {
            case MATRAZ -> EnumSet.of(AccionLaboratorio.CARGAR_REACTIVO, AccionLaboratorio.AGREGAR_AGUA,
                    AccionLaboratorio.MEZCLAR, AccionLaboratorio.CALENTAR, AccionLaboratorio.ENFRIAR,
                    AccionLaboratorio.AGREGAR_CATALIZADOR, AccionLaboratorio.ESPERAR,
                    AccionLaboratorio.ACTIVAR_IGNICION, AccionLaboratorio.FINALIZAR,
                    AccionLaboratorio.SELECCIONAR_METODO, AccionLaboratorio.MONTAR_EQUIPO);
            case MECHERO, CALENTADOR, HORNO -> EnumSet.of(AccionLaboratorio.CARGAR_REACTIVO,
                    AccionLaboratorio.CALENTAR, AccionLaboratorio.ENFRIAR, AccionLaboratorio.ESPERAR,
                    AccionLaboratorio.ACTIVAR_IGNICION, AccionLaboratorio.FINALIZAR);
            case ENFRIADOR -> EnumSet.of(AccionLaboratorio.CARGAR_REACTIVO, AccionLaboratorio.ENFRIAR,
                    AccionLaboratorio.ESPERAR, AccionLaboratorio.FINALIZAR);
            case MEZCLADOR -> EnumSet.of(AccionLaboratorio.CARGAR_REACTIVO, AccionLaboratorio.MEZCLAR,
                    AccionLaboratorio.ESPERAR, AccionLaboratorio.FINALIZAR);
            case REACTOR -> EnumSet.of(AccionLaboratorio.CARGAR_REACTIVO,
                    AccionLaboratorio.AGREGAR_CATALIZADOR, AccionLaboratorio.CALENTAR,
                    AccionLaboratorio.ENFRIAR, AccionLaboratorio.COMPRIMIR,
                    AccionLaboratorio.DESCOMPRIMIR, AccionLaboratorio.MEZCLAR,
                    AccionLaboratorio.ESPERAR, AccionLaboratorio.FINALIZAR);
            case CAMARA_SELLADA -> EnumSet.of(AccionLaboratorio.CARGAR_REACTIVO,
                    AccionLaboratorio.AGREGAR_CATALIZADOR, AccionLaboratorio.AGREGAR_AGUA,
                    AccionLaboratorio.MEZCLAR, AccionLaboratorio.CALENTAR,
                    AccionLaboratorio.ENFRIAR, AccionLaboratorio.COMPRIMIR,
                    AccionLaboratorio.DESCOMPRIMIR, AccionLaboratorio.ACTIVAR_IGNICION,
                    AccionLaboratorio.ESPERAR, AccionLaboratorio.FINALIZAR,
                    AccionLaboratorio.INICIAR_PLASMA);
            case BANDEJA -> EnumSet.of(AccionLaboratorio.CARGAR_REACTIVO, AccionLaboratorio.AGREGAR_AGUA,
                    AccionLaboratorio.ESPERAR, AccionLaboratorio.FINALIZAR);
            case RECIPIENTE, BALANZA, TERMOMETRO, MEDIDOR_PH ->
                    EnumSet.of(AccionLaboratorio.CARGAR_REACTIVO, AccionLaboratorio.AGREGAR_AGUA,
                            AccionLaboratorio.MEZCLAR, AccionLaboratorio.ESPERAR,
                            AccionLaboratorio.FINALIZAR);
        };
    }
}
