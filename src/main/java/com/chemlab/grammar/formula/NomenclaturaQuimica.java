package com.chemlab.grammar.formula;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Aplica reglas explícitas de nomenclatura Stock para óxidos binarios comunes. */
public final class NomenclaturaQuimica {

    public Optional<String> nombreElemento(String simbolo) {
        return TablaPeriodica.buscar(simbolo).map(TablaPeriodica.ElementoQuimico::nombre);
    }

    public Map<String, String> nombresElementos(ResultadoAnalisisFormula analisis) {
        Objects.requireNonNull(analisis, "El análisis de fórmula es obligatorio");
        Map<String, String> nombres = new LinkedHashMap<>();
        analisis.atomos().keySet().forEach(simbolo ->
                nombres.put(simbolo, nombreElemento(simbolo).orElse("elemento " + simbolo)));
        return Map.copyOf(nombres);
    }

    public ResultadoNombreQuimico nombrar(ResultadoAnalisisFormula analisis, String nombreCatalogo) {
        Objects.requireNonNull(analisis, "El análisis de fórmula es obligatorio");
        Map<String, Integer> atomos = analisis.atomos();

        if (atomos.keySet().equals(java.util.Set.of("H", "O"))
                && nombreCatalogo != null && !nombreCatalogo.isBlank()) {
            return nombreDeCatalogo(nombreCatalogo, atomos);
        }

        ResultadoNombreQuimico hidroxido = nombrarHidroxido(analisis, atomos);
        if (hidroxido != null) return hidroxido;

        ResultadoNombreQuimico oxido = nombrarOxido(analisis, atomos);
        if (oxido != null) return oxido;

        if (nombreCatalogo != null && !nombreCatalogo.isBlank()) {
            return nombreDeCatalogo(nombreCatalogo, atomos);
        }

        String elementos = describirElementos(atomos);
        List<String> pasos = List.of(
                "1. La gramática reconoce la fórmula y cuenta sus átomos.",
                "2. Los elementos presentes son " + elementos + ".",
                "3. No hay una regla de nomenclatura configurada para esta familia; se evita inventar un nombre."
        );
        return new ResultadoNombreQuimico("Compuesto sin nombre registrado", "Fórmula reconocida; nomenclatura no disponible",
                pasos);
    }

    private ResultadoNombreQuimico nombrarHidroxido(
            ResultadoAnalisisFormula analisis,
            Map<String, Integer> atomos
    ) {
        if (!analisis.formulaNormalizada().matches("[A-Z][a-z]?\\(OH\\)[0-9]*")
                || !atomos.containsKey("H") || !atomos.containsKey("O")
                || atomos.size() != 3 || !atomos.get("H").equals(atomos.get("O"))) {
            return null;
        }
        String metal = atomos.keySet().stream()
                .filter(simbolo -> !simbolo.equals("H") && !simbolo.equals("O"))
                .findFirst().orElseThrow();
        Set<Integer> posibles = TablaPeriodica.buscar(metal)
                .map(TablaPeriodica.ElementoQuimico::numerosOxidacion).orElse(Set.of());
        int cantidadMetal = atomos.get(metal);
        int cantidadGrupos = atomos.get("H");
        if (cantidadGrupos % cantidadMetal != 0) return null;
        int numeroOxidacion = cantidadGrupos / cantidadMetal;
        if (!posibles.contains(numeroOxidacion)) return null;

        String nombreMetal = nombreElemento(metal).orElse(null);
        String sufijoStock = posibles.size() > 1 ? " (" + romano(numeroOxidacion) + ")" : "";
        String nombre = "Hidróxido de " + nombreMetal + sufijoStock;
        List<String> pasos = List.of(
                "1. La gramática reconoce el grupo hidróxido (OH) y el metal "
                        + nombreMetal + " (" + metal + ").",
                "2. Cada grupo hidróxido tiene carga −1; hay " + cantidadGrupos
                        + " grupo(s), con carga total −" + cantidadGrupos + ".",
                "3. La neutralidad del compuesto exige que " + cantidadMetal + " átomo(s) de "
                        + nombreMetal + " aporten en conjunto +" + cantidadGrupos + ".",
                "4. Cada átomo de " + nombreMetal + " tiene número de oxidación +"
                        + numeroOxidacion + ".",
                "5. En nomenclatura Stock, el nombre es «" + nombre + "»."
        );
        return new ResultadoNombreQuimico(nombre,
                "Gramática de hidróxidos y nomenclatura Stock", pasos);
    }

    private ResultadoNombreQuimico nombreDeCatalogo(
            String nombreCatalogo,
            Map<String, Integer> atomos
    ) {
        List<String> pasos = List.of(
                "1. La gramática de fórmula identifica los símbolos y sus subíndices.",
                "2. Se buscan los elementos: " + describirElementos(atomos) + ".",
                "3. La fórmula coincide con una sustancia registrada; se conserva su nombre químico de catálogo: "
                        + nombreCatalogo + "."
        );
        return new ResultadoNombreQuimico(nombreCatalogo,
                "Identificación exacta en el catálogo químico", pasos);
    }

    private ResultadoNombreQuimico nombrarOxido(
            ResultadoAnalisisFormula analisis,
            Map<String, Integer> atomos
    ) {
        if (atomos.size() != 2 || !atomos.containsKey("O")) return null;
        String otro = atomos.keySet().stream().filter(simbolo -> !simbolo.equals("O")).findFirst().orElseThrow();
        int oxigenos = atomos.get("O");
        int cantidadOtro = atomos.get(otro);
        int numeroOxidacionNumerador = 2 * oxigenos;
        String nombreOtro = nombreElemento(otro).orElse(null);
        if (nombreOtro == null) return null;

        Set<Integer> posibles = TablaPeriodica.buscar(otro)
                .map(TablaPeriodica.ElementoQuimico::numerosOxidacion).orElse(Set.of());
        if (numeroOxidacionNumerador % cantidadOtro == 0) {
            int numeroOxidacion = numeroOxidacionNumerador / cantidadOtro;
            if (posibles.contains(numeroOxidacion)) {
                String sufijoStock = posibles.size() > 1
                        ? " (" + romano(numeroOxidacion) + ")" : "";
                String nombre = "Óxido de " + nombreOtro + sufijoStock;
                List<String> pasos = List.of(
                        "1. La gramática separa la fórmula en los elementos "
                                + describirElementos(atomos) + ".",
                        "2. Como el oxígeno está combinado con un solo elemento, se clasifica como óxido binario.",
                        "3. Cada oxígeno aporta −2: " + oxigenos + " × (−2) = "
                                + (-numeroOxidacionNumerador) + ".",
                        "4. La neutralidad eléctrica exige que " + cantidadOtro + " átomos de "
                                + nombreOtro + " compensen esa carga; cada uno tiene número de oxidación +"
                                + numeroOxidacion + ".",
                        "5. Se aplica la nomenclatura Stock: «óxido de " + nombreOtro
                                + (sufijoStock.isBlank() ? "»." : " (" + romano(numeroOxidacion) + ")».")
                );
                return new ResultadoNombreQuimico(nombre, "Gramática de óxidos binarios y nomenclatura Stock", pasos);
            }
        }

        if (TablaPeriodica.buscar(otro)
                .filter(elemento -> elemento.numerosOxidacion().isEmpty() && elemento.noMetal())
                .isPresent()) {
            String prefijoOxigeno = prefijo(oxigenos);
            String prefijoElemento = cantidadOtro == 1 ? "" : prefijo(cantidadOtro);
            String nombre = prefijoOxigeno + "óxido de " + prefijoElemento + nombreOtro;
            List<String> pasos = List.of(
                    "1. La gramática identifica " + describirElementos(atomos) + ".",
                    "2. El oxígeno y el no metal forman un óxido molecular.",
                    "3. Los subíndices indican " + oxigenos + " oxígeno(s) y "
                            + cantidadOtro + " átomo(s) de " + nombreOtro + ".",
                    "4. Se expresan las cantidades con prefijos: el nombre resultante es «" + nombre + "»."
            );
            return new ResultadoNombreQuimico(nombre, "Gramática de óxidos moleculares con prefijos", pasos);
        }
        return null;
    }

    private String describirElementos(Map<String, Integer> atomos) {
        return atomos.entrySet().stream()
                .map(entrada -> nombreElemento(entrada.getKey()).orElse(entrada.getKey())
                        + " (" + entrada.getKey() + "): " + entrada.getValue())
                .collect(Collectors.joining(", "));
    }

    private String prefijo(int cantidad) {
        return switch (cantidad) {
            case 1 -> "mon";
            case 2 -> "di";
            case 3 -> "tri";
            case 4 -> "tetra";
            case 5 -> "penta";
            case 6 -> "hexa";
            case 7 -> "hepta";
            case 8 -> "octa";
            case 9 -> "nona";
            case 10 -> "deca";
            default -> cantidad + "-";
        };
    }

    private String romano(int numero) {
        return switch (numero) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            default -> Integer.toString(numero);
        };
    }
}
