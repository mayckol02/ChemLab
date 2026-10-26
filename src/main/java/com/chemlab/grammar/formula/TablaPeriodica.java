package com.chemlab.grammar.formula;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Registro inmutable de los elementos y los datos usados por el análisis químico. */
public final class TablaPeriodica {

    public record ElementoQuimico(
            String simbolo,
            String nombre,
            boolean noMetal,
            Set<Integer> numerosOxidacion
    ) {
        public ElementoQuimico {
            Objects.requireNonNull(simbolo, "El símbolo del elemento es obligatorio");
            Objects.requireNonNull(nombre, "El nombre del elemento es obligatorio");
            numerosOxidacion = Set.copyOf(
                    Objects.requireNonNull(numerosOxidacion, "Los números de oxidación son obligatorios"));
            if (simbolo.isBlank() || nombre.isBlank()) {
                throw new IllegalArgumentException("El símbolo y el nombre no pueden estar vacíos");
            }
            if (numerosOxidacion.stream().anyMatch(numero -> numero == 0)) {
                throw new IllegalArgumentException("Los estados de oxidación no pueden ser cero");
            }
        }
    }

    private static final String[] DATOS = {
            "H|hidrógeno|N|", "He|helio||", "Li|litio||1", "Be|berilio||2",
            "B|boro||", "C|carbono|N|", "N|nitrógeno|N|", "O|oxígeno|N|",
            "F|flúor|N|", "Ne|neón||", "Na|sodio||1", "Mg|magnesio||2",
            "Al|aluminio||3", "Si|silicio||", "P|fósforo|N|", "S|azufre|N|",
            "Cl|cloro|N|", "Ar|argón||", "K|potasio||1", "Ca|calcio||2",
            "Sc|escandio||", "Ti|titanio||", "V|vanadio||", "Cr|cromo||2,3,6",
            "Mn|manganeso||2,4,7", "Fe|hierro||2,3", "Co|cobalto||2,3", "Ni|níquel||2,3",
            "Cu|cobre||1,2", "Zn|zinc||2", "Ga|galio||", "Ge|germanio||",
            "As|arsénico||", "Se|selenio|N|", "Br|bromo|N|", "Kr|criptón||",
            "Rb|rubidio||1", "Sr|estroncio||2", "Y|itrio||", "Zr|circonio||",
            "Nb|niobio||", "Mo|molibdeno||", "Tc|tecnecio||", "Ru|rutenio||",
            "Rh|rodio||", "Pd|paladio||", "Ag|plata||1", "Cd|cadmio||2",
            "In|indio||", "Sn|estaño||2,4", "Sb|antimonio||", "Te|telurio||",
            "I|yodo|N|", "Xe|xenón||", "Cs|cesio||1", "Ba|bario||2",
            "La|lantano||", "Ce|cerio||", "Pr|praseodimio||", "Nd|neodimio||",
            "Pm|prometio||", "Sm|samario||", "Eu|europio||", "Gd|gadolinio||",
            "Tb|terbio||", "Dy|disprosio||", "Ho|holmio||", "Er|erbio||",
            "Tm|tulio||", "Yb|iterbio||", "Lu|lutecio||", "Hf|hafnio||",
            "Ta|tántalo||", "W|wolframio||", "Re|renio||", "Os|osmio||",
            "Ir|iridio||", "Pt|platino||2,4", "Au|oro||1,3", "Hg|mercurio||1,2",
            "Tl|talio||", "Pb|plomo||2,4", "Bi|bismuto||3,5", "Po|polonio||",
            "At|astato||", "Rn|radón||", "Fr|francio||", "Ra|radio||",
            "Ac|actinio||", "Th|torio||", "Pa|protactinio||", "U|uranio||",
            "Np|neptunio||", "Pu|plutonio||", "Am|americio||", "Cm|curio||",
            "Bk|berkelio||", "Cf|californio||", "Es|einstenio||", "Fm|fermio||",
            "Md|mendelevio||", "No|nobelio||", "Lr|lawrencio||", "Rf|rutherfordio||",
            "Db|dubnio||", "Sg|seaborgio||", "Bh|bohrio||", "Hs|hasio||",
            "Mt|meitnerio||", "Ds|darmstatio||", "Rg|roentgenio||", "Cn|copernicio||",
            "Nh|nihonio||", "Fl|flerovio||", "Mc|moscovio||", "Lv|livermorio||",
            "Ts|teneso||", "Og|oganesón||"
    };

    private static final Map<String, ElementoQuimico> ELEMENTOS = cargarElementos();

    private TablaPeriodica() {}

    public static Optional<ElementoQuimico> buscar(String simbolo) {
        return Optional.ofNullable(ELEMENTOS.get(simbolo));
    }

    public static boolean contiene(String simbolo) {
        return ELEMENTOS.containsKey(simbolo);
    }

    public static List<ElementoQuimico> todos() {
        return List.copyOf(ELEMENTOS.values());
    }

    private static Map<String, ElementoQuimico> cargarElementos() {
        Map<String, ElementoQuimico> elementos = new LinkedHashMap<>();
        for (String dato : DATOS) {
            String[] campos = dato.split("\\|", -1);
            if (campos.length != 4) {
                throw new IllegalStateException("Registro de elemento inválido: " + dato);
            }
            ElementoQuimico elemento = new ElementoQuimico(
                    campos[0],
                    campos[1],
                    campos[2].equals("N"),
                    parsearNumerosOxidacion(campos[3])
            );
            if (elementos.putIfAbsent(elemento.simbolo(), elemento) != null) {
                throw new IllegalStateException("Símbolo de elemento duplicado: " + elemento.simbolo());
            }
        }
        return Collections.unmodifiableMap(elementos);
    }

    private static Set<Integer> parsearNumerosOxidacion(String campo) {
        if (campo.isBlank()) return Set.of();
        Set<Integer> numeros = new LinkedHashSet<>();
        for (String numero : campo.split(",")) {
            try {
                if (!numeros.add(Integer.parseInt(numero))) {
                    throw new IllegalStateException("Número de oxidación duplicado: " + numero);
                }
            } catch (NumberFormatException error) {
                throw new IllegalStateException("Número de oxidación inválido: " + numero, error);
            }
        }
        return Collections.unmodifiableSet(numeros);
    }
}
