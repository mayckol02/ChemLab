package com.chemlab.factory;

import com.chemlab.catalogo.CatalogoQuimico;
import com.chemlab.model.entity.CondicionesExperimentales;
import com.chemlab.model.entity.Equipo;
import com.chemlab.model.entity.Experimento;
import com.chemlab.model.entity.Inventario;
import com.chemlab.model.entity.Objetivo;
import com.chemlab.model.entity.Sustancia;
import com.chemlab.model.enums.Atmosfera;
import com.chemlab.model.enums.TipoEquipo;
import com.chemlab.model.enums.UnidadCantidad;

import java.util.Objects;
import java.util.UUID;

/**
 * Factory Method para crear {@link Experimento}s bien configurados.
 *
 * <p>Separa la lógica de construcción del resto del dominio,
 * evitando que los casos de uso conozcan los detalles de inicialización.</p>
 */
public final class FabricaExperimento {

    private final CatalogoQuimico catalogo;

    public FabricaExperimento(CatalogoQuimico catalogo) {
        this.catalogo = Objects.requireNonNull(
                catalogo,
                "El catálogo no puede ser null"
        );
    }

    /**
     * Crea un experimento básico de síntesis de agua (H₂ + O₂ → H₂O).
     * Precarga el inventario con reactivos suficientes para la reacción.
     */
    public Experimento crearExperimentoSintesisAgua() {
        Sustancia h2 = catalogo.buscarSustancia("H2")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene H2"
                ));

        Sustancia o2 = catalogo.buscarSustancia("O2")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene O2"
                ));

        Sustancia h2o = catalogo.buscarSustancia("H2O")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene H2O"
                ));

        Inventario inventario = new Inventario();
        inventario.agregar(h2, 4.032, UnidadCantidad.GRAMOS);   // 2 mol H₂
        inventario.agregar(o2, 32.0, UnidadCantidad.GRAMOS);    // 1 mol O₂

        Equipo equipo = new Equipo(
                "EQ-MATRAZ-01",
                "Matraz Erlenmeyer 500 mL",
                TipoEquipo.MATRAZ,
                500.0
        );

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25.0,   // temperatura °C
                        1.0,    // presión atm
                        7.0,    // pH neutro
                        Atmosfera.AIRE
                );

        Objetivo objetivo = new Objetivo(
                "OBJ-AGUA-01",
                "Sintetizar al menos 18 g de agua",
                h2o,
                18.0
        );

        return new Experimento(
                generarId(),
                "Síntesis de Agua",
                objetivo,
                inventario,
                equipo,
                condiciones
        );
    }

    /**
     * Crea un experimento de combustión de carbono (C + O₂ → CO₂).
     */
    public Experimento crearExperimentoCombustionCarbono() {
        Sustancia c = catalogo.buscarSustancia("C")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene C"
                ));

        Sustancia o2 = catalogo.buscarSustancia("O2")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene O2"
                ));

        Sustancia co2 = catalogo.buscarSustancia("CO2")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene CO2"
                ));

        Inventario inventario = new Inventario();
        inventario.agregar(c, 12.011, UnidadCantidad.GRAMOS);   // 1 mol C
        inventario.agregar(o2, 32.0, UnidadCantidad.GRAMOS);    // 1 mol O₂

        Equipo equipo = new Equipo(
                "EQ-HORNO-01",
                "Horno de combustión",
                TipoEquipo.HORNO,
                1000.0
        );

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        500.0,  // temperatura alta para combustión
                        1.0,
                        7.0,
                        Atmosfera.OXIGENO
                );

        Objetivo objetivo = new Objetivo(
                "OBJ-CO2-01",
                "Sintetizar al menos 44 g de CO₂",
                co2,
                44.0
        );

        return new Experimento(
                generarId(),
                "Combustión de Carbono",
                objetivo,
                inventario,
                equipo,
                condiciones
        );
    }

    /**
     * Crea un experimento de síntesis de amoníaco mediante el proceso Haber-Bosch
     * (N₂ + 3H₂ → 2NH₃). Requiere alta presión y temperatura con catalizador de hierro.
     */
    public Experimento crearExperimentoSintesisAmoniaco() {
        Sustancia n2 = catalogo.buscarSustancia("N2")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene N2"
                ));

        Sustancia h2 = catalogo.buscarSustancia("H2")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene H2"
                ));

        Sustancia nh3 = catalogo.buscarSustancia("NH3")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene NH3"
                ));

        Inventario inventario = new Inventario();
        inventario.agregar(n2, 28.014, UnidadCantidad.GRAMOS);  // 1 mol N₂
        inventario.agregar(h2, 6.048, UnidadCantidad.GRAMOS);   // 3 mol H₂

        Equipo equipo = new Equipo(
                "EQ-REACTOR-01",
                "Reactor de alta presión",
                TipoEquipo.REACTOR,
                5000.0
        );

        // Haber-Bosch: 400-500 °C, 150-300 atm, catalizador Fe
        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        450.0,  // temperatura °C
                        200.0,  // presión atm
                        7.0,
                        Atmosfera.NITROGENO
                );

        Objetivo objetivo = new Objetivo(
                "OBJ-NH3-01",
                "Sintetizar al menos 17 g de amoníaco",
                nh3,
                17.0
        );

        return new Experimento(
                generarId(),
                "Síntesis de Amoníaco (Haber-Bosch)",
                objetivo,
                inventario,
                equipo,
                condiciones
        );
    }

    /**
     * Crea un experimento de síntesis de cloruro de sodio (sal común).
     * (2Na + Cl₂ → 2NaCl)
     */
    public Experimento crearExperimentoSintesisNaCl() {
        Sustancia na = catalogo.buscarSustancia("Na")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene Na"
                ));

        Sustancia cl2 = catalogo.buscarSustancia("Cl2")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene Cl2"
                ));

        Sustancia nacl = catalogo.buscarSustancia("NaCl")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene NaCl"
                ));

        Inventario inventario = new Inventario();
        inventario.agregar(na, 45.98, UnidadCantidad.GRAMOS);   // 2 mol Na
        inventario.agregar(cl2, 70.906, UnidadCantidad.GRAMOS); // 1 mol Cl₂

        Equipo equipo = new Equipo(
                "EQ-CAMARA-01",
                "Cámara de reacción sellada",
                TipoEquipo.CAMARA_SELLADA,
                300.0
        );

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25.0,
                        1.0,
                        7.0,
                        Atmosfera.CLORO
                );

        Objetivo objetivo = new Objetivo(
                "OBJ-NACL-01",
                "Sintetizar al menos 58 g de cloruro de sodio",
                nacl,
                58.0
        );

        return new Experimento(
                generarId(),
                "Síntesis de Cloruro de Sodio",
                objetivo,
                inventario,
                equipo,
                condiciones
        );
    }

    /**
     * Crea un experimento de oxidación del hierro (formación de herrumbre).
     * (4Fe + 3O₂ → 2Fe₂O₃)
     */
    public Experimento crearExperimentoOxidacionHierro() {
        Sustancia fe = catalogo.buscarSustancia("Fe")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene Fe"
                ));

        Sustancia o2 = catalogo.buscarSustancia("O2")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene O2"
                ));

        Sustancia fe2o3 = catalogo.buscarSustancia("Fe2O3")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene Fe2O3"
                ));

        Inventario inventario = new Inventario();
        inventario.agregar(fe, 223.38, UnidadCantidad.GRAMOS);  // 4 mol Fe
        inventario.agregar(o2, 96.0, UnidadCantidad.GRAMOS);    // 3 mol O₂

        Equipo equipo = new Equipo(
                "EQ-BANDEJA-01",
                "Bandeja de oxidación",
                TipoEquipo.BANDEJA,
                2000.0
        );

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        60.0,   // temperatura ambiente elevada
                        1.0,
                        7.0,
                        Atmosfera.AIRE_HUMEDO
                );

        Objetivo objetivo = new Objetivo(
                "OBJ-FE2O3-01",
                "Sintetizar al menos 159 g de óxido de hierro(III)",
                fe2o3,
                159.0
        );

        return new Experimento(
                generarId(),
                "Oxidación del Hierro",
                objetivo,
                inventario,
                equipo,
                condiciones
        );
    }

    /**
     * Crea un experimento de síntesis de ácido clorhídrico.
     * (H₂ + Cl₂ → 2HCl)
     */
    public Experimento crearExperimentoSintesisHCl() {
        Sustancia h2 = catalogo.buscarSustancia("H2")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene H2"
                ));

        Sustancia cl2 = catalogo.buscarSustancia("Cl2")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene Cl2"
                ));

        Sustancia hcl = catalogo.buscarSustancia("HCl")
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo no contiene HCl"
                ));

        Inventario inventario = new Inventario();
        inventario.agregar(h2, 2.016, UnidadCantidad.GRAMOS);   // 1 mol H₂
        inventario.agregar(cl2, 70.906, UnidadCantidad.GRAMOS); // 1 mol Cl₂

        Equipo equipo = new Equipo(
                "EQ-MATRAZ-02",
                "Matraz de síntesis 250 mL",
                TipoEquipo.MATRAZ,
                250.0
        );

        CondicionesExperimentales condiciones =
                new CondicionesExperimentales(
                        25.0,
                        1.0,
                        1.0,    // ácido fuerte: pH muy bajo
                        Atmosfera.CLORO
                );

        Objetivo objetivo = new Objetivo(
                "OBJ-HCL-01",
                "Sintetizar al menos 36 g de ácido clorhídrico",
                hcl,
                36.0
        );

        return new Experimento(
                generarId(),
                "Síntesis de Ácido Clorhídrico",
                objetivo,
                inventario,
                equipo,
                condiciones
        );
    }

    /**
     * Construye un experimento totalmente configurado para cualquier reacción
     * del catálogo químico y universo experimental.
     */
    public Experimento crearExperimentoParaReaccion(com.chemlab.model.entity.Reaccion reaccion) {
        Objects.requireNonNull(reaccion, "La reacción no puede ser null");

        // Casos conocidos con configuraciones históricas precisas:
        switch (reaccion.getId()) {
            case "R_H2O" -> { return crearExperimentoSintesisAgua(); }
            case "R_CO2" -> { return crearExperimentoCombustionCarbono(); }
            case "R_NH3" -> { return crearExperimentoSintesisAmoniaco(); }
            case "R_NaCl" -> { return crearExperimentoSintesisNaCl(); }
            case "R_HCl" -> { return crearExperimentoSintesisHCl(); }
            case "R_Fe2O3" -> { return crearExperimentoOxidacionHierro(); }
            default -> {}
        }

        // Configuración genérica para todas las demás reacciones del universo químico
        Inventario inventario = new Inventario();
        for (var entry : reaccion.getReactivos().entrySet()) {
            Sustancia sust = entry.getKey();
            int coef = entry.getValue();
            // Asignar al menos 2 moles de reactivo para que alcance la estequiometría
            double gramos = Math.max(10.0, coef * sust.getMasaMolar());
            inventario.agregar(sust, gramos, UnidadCantidad.GRAMOS);
        }

        Sustancia productoPrincipal = reaccion.getProductos().keySet().iterator().next();
        int coefProd = reaccion.getProductos().get(productoPrincipal);
        double metaGramos = Math.max(5.0, coefProd * productoPrincipal.getMasaMolar() * 0.8);

        Objetivo objetivo = new Objetivo(
                "OBJ-" + reaccion.getId(),
                "Sintetizar " + productoPrincipal.getFormula() + " (" + productoPrincipal.getNombre() + ")",
                productoPrincipal,
                metaGramos
        );

        // Elegir equipo adecuado
        TipoEquipo tipoEq = TipoEquipo.MATRAZ;
        String nombreEquipo = "Matraz de síntesis";
        double temp = 25.0;
        double pres = 1.0;
        Atmosfera atm = Atmosfera.AIRE;

        if (reaccion.getId().contains("DIAMANTE_HPHT")) {
            tipoEq = TipoEquipo.REACTOR;
            nombreEquipo = "Prensa Cúbica HPHT (5.5 GPa / 1500°C)";
            temp = 1500.0;
            pres = 55000.0;
            atm = Atmosfera.VACIO;
            // Precargar catalizador Fe y Ni para el flujo solvente
            catalogo.buscarSustancia("Ni").ifPresent(ni -> inventario.agregar(ni, 20.0, UnidadCantidad.GRAMOS));
            catalogo.buscarSustancia("Fe").ifPresent(fe -> inventario.agregar(fe, 20.0, UnidadCantidad.GRAMOS));
        } else if (reaccion.getId().contains("DIAMANTE_CVD")) {
            tipoEq = TipoEquipo.CAMARA_SELLADA;
            nombreEquipo = "Reactor CVD con Plasma de Microondas (2.45 GHz)";
            temp = 1000.0;
            pres = 0.05;
            atm = Atmosfera.VACIO;
            // Asegurar H2 presente para el plasma decapante
            catalogo.buscarSustancia("H2").ifPresent(h2 -> inventario.agregar(h2, 20.0, UnidadCantidad.GRAMOS));
        } else if (reaccion.getId().contains("CORROSION_FE")) {
            tipoEq = TipoEquipo.BANDEJA;
            nombreEquipo = "Bandeja de Corrosión Ambiental";
            temp = 25.0;
            pres = 1.0;
            atm = Atmosfera.AIRE_HUMEDO;
        } else if (reaccion.getId().contains("DISOLUCION_NACL")) {
            tipoEq = TipoEquipo.MATRAZ;
            nombreEquipo = "Vaso de Precipitados con Agitador Magnético";
            temp = 25.0;
            pres = 1.0;
            atm = Atmosfera.AIRE;
        } else if (reaccion.getId().contains("SEPARACION_NACL")) {
            tipoEq = TipoEquipo.MATRAZ;
            nombreEquipo = "Aparato de Evaporación y Cristalización";
            temp = 100.0;
            pres = 1.0;
            atm = Atmosfera.AIRE;
        } else if (reaccion.getId().contains("CALCINACION_CACO3")) {
            tipoEq = TipoEquipo.HORNO;
            nombreEquipo = "Horno Mufla de Calcinación (900°C)";
            temp = 900.0;
            pres = 1.0;
            atm = Atmosfera.AIRE;
        } else if (reaccion.getId().contains("REDOX_CU")) {
            tipoEq = TipoEquipo.CAMARA_SELLADA;
            nombreEquipo = "Tubo de Reducción Tubular (300°C)";
            temp = 300.0;
            pres = 1.0;
            atm = Atmosfera.AIRE;
        } else if (reaccion.getId().contains("CH4") || reaccion.getId().contains("SO2") || reaccion.getId().contains("MgO")) {
            tipoEq = TipoEquipo.HORNO;
            nombreEquipo = "Cámara de Combustión Sellada";
            temp = 400.0;
            pres = 1.0;
            atm = Atmosfera.OXIGENO;
        } else if (reaccion.getId().contains("SiO2") || reaccion.getId().contains("Al2O3") || reaccion.getId().contains("Ca3PO42")) {
            tipoEq = TipoEquipo.HORNO;
            nombreEquipo = "Horno de Oxidación Térmica (800°C)";
            temp = 800.0;
            pres = 1.0;
            atm = Atmosfera.OXIGENO;
        }

        Equipo equipo = new Equipo(
                "EQ-" + reaccion.getId(),
                nombreEquipo,
                tipoEq,
                1000.0
        );

        CondicionesExperimentales condiciones = new CondicionesExperimentales(
                temp,
                pres,
                7.0,
                atm
        );

        return new Experimento(
                generarId(),
                reaccion.getNombre(),
                objetivo,
                inventario,
                equipo,
                condiciones
        );
    }

    private static String generarId() {
        return "EXP-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
