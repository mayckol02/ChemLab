package com.chemlab.grammar.formula;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Analizador descendente recursivo para la gramática libre de contexto de
 * fórmulas con elementos, subíndices y grupos anidados.
 */
public final class AnalizadorFormulaQuimica {

    private static final int LONGITUD_MAXIMA = 128;
    private static final int PROFUNDIDAD_MAXIMA = 16;
    private static final int MAX_ATOMOS = 1_000_000;
    private static final Set<String> ELEMENTOS = Set.of(
            "H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne",
            "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca",
            "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn",
            "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr",
            "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn",
            "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd",
            "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb",
            "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg",
            "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th",
            "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm",
            "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh", "Hs", "Mt", "Ds",
            "Rg", "Cn", "Nh", "Fl", "Mc", "Lv", "Ts", "Og"
    );

    public ResultadoAnalisisFormula analizar(String entrada) {
        if (entrada == null || entrada.isBlank()) {
            throw new FormulaInvalidaException("Escribe una fórmula química.", 0);
        }
        String original = entrada.trim();
        String normalizada = normalizarSubindices(original);
        if (normalizada.length() > LONGITUD_MAXIMA) {
            throw new FormulaInvalidaException("La fórmula supera el límite de " + LONGITUD_MAXIMA + " caracteres.",
                    LONGITUD_MAXIMA);
        }

        Analizador parser = new Analizador(normalizada);
        ResultadoSecuencia secuencia = parser.analizarSecuencia(0);
        if (!parser.termino()) {
            char inesperado = parser.actual();
            String mensaje = inesperado == ')'
                    ? "Hay un paréntesis de cierre sin su grupo correspondiente."
                    : "Carácter no esperado: '" + inesperado + "'.";
            throw new FormulaInvalidaException(mensaje, parser.posicion());
        }

        NodoDerivacionFormula raiz = new NodoDerivacionFormula(
                "Fórmula → Secuencia", original, List.of(secuencia.arbol()));
        return new ResultadoAnalisisFormula(original, normalizada, raiz, secuencia.atomos());
    }

    private String normalizarSubindices(String texto) {
        StringBuilder normalizada = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char caracter = texto.charAt(i);
            normalizada.append(switch (caracter) {
                case '₀' -> '0';
                case '₁' -> '1';
                case '₂' -> '2';
                case '₃' -> '3';
                case '₄' -> '4';
                case '₅' -> '5';
                case '₆' -> '6';
                case '₇' -> '7';
                case '₈' -> '8';
                case '₉' -> '9';
                default -> caracter;
            });
        }
        return normalizada.toString();
    }

    private record ResultadoSecuencia(NodoDerivacionFormula arbol, Map<String, Integer> atomos) {}

    private record ResultadoTermino(NodoDerivacionFormula arbol, Map<String, Integer> atomos) {}

    private static final class Analizador {

        private final String entrada;
        private int posicion;

        private Analizador(String entrada) {
            this.entrada = entrada;
        }

        private ResultadoSecuencia analizarSecuencia(int profundidad) {
            if (profundidad > PROFUNDIDAD_MAXIMA) {
                throw error("Los grupos anidados superan el límite de " + PROFUNDIDAD_MAXIMA + ".");
            }
            List<NodoDerivacionFormula> terminos = new ArrayList<>();
            Map<String, Integer> atomos = new LinkedHashMap<>();
            while (!termino() && actual() != ')') {
                ResultadoTermino termino = analizarTermino(profundidad);
                terminos.add(termino.arbol());
                sumarAtomos(atomos, termino.atomos());
            }
            if (terminos.isEmpty()) throw error("Se esperaba un elemento o un grupo químico.");
            NodoDerivacionFormula nodo = new NodoDerivacionFormula(
                    "Secuencia → Término Secuencia | Término", "", terminos);
            return new ResultadoSecuencia(nodo, atomos);
        }

        private ResultadoTermino analizarTermino(int profundidad) {
            if (actual() == '(') return analizarGrupo(profundidad);
            if (!esMayuscula(actual())) {
                throw error("Se esperaba el símbolo de un elemento (por ejemplo, H, Fe o Ca).");
            }
            return analizarElemento();
        }

        private ResultadoTermino analizarGrupo(int profundidad) {
            posicion++;
            ResultadoSecuencia interior = analizarSecuencia(profundidad + 1);
            if (termino() || actual() != ')') throw error("Falta ')' para cerrar el grupo químico.");
            posicion++;
            ResultadoSubindice subindice = analizarSubindice();
            Map<String, Integer> atomos = multiplicarAtomos(interior.atomos(), subindice.valor());

            List<NodoDerivacionFormula> hijos = new ArrayList<>();
            hijos.add(new NodoDerivacionFormula("Terminal", "(", List.of()));
            hijos.add(interior.arbol());
            hijos.add(new NodoDerivacionFormula("Terminal", ")", List.of()));
            if (subindice.arbol() != null) hijos.add(subindice.arbol());
            String regla = subindice.arbol() == null
                    ? "Término → ( Secuencia )"
                    : "Término → ( Secuencia ) Subíndice";
            return new ResultadoTermino(new NodoDerivacionFormula(regla, "grupo × " + subindice.valor(), hijos), atomos);
        }

        private ResultadoTermino analizarElemento() {
            int inicio = posicion;
            posicion++;
            if (!termino() && esMinuscula(actual())) posicion++;
            String simbolo = entrada.substring(inicio, posicion);
            if (!ELEMENTOS.contains(simbolo)) {
                throw new FormulaInvalidaException("'" + simbolo + "' no es un símbolo de elemento reconocido.", inicio);
            }
            ResultadoSubindice subindice = analizarSubindice();

            List<NodoDerivacionFormula> simboloHijos = new ArrayList<>();
            simboloHijos.add(new NodoDerivacionFormula("Mayúscula", simbolo.substring(0, 1), List.of()));
            if (simbolo.length() == 2) {
                simboloHijos.add(new NodoDerivacionFormula("Minúscula", simbolo.substring(1), List.of()));
            }
            NodoDerivacionFormula nodoElemento = new NodoDerivacionFormula(
                    "Elemento → Mayúscula Minúscula?", simbolo, simboloHijos);
            List<NodoDerivacionFormula> hijosTermino = new ArrayList<>();
            hijosTermino.add(nodoElemento);
            if (subindice.arbol() != null) hijosTermino.add(subindice.arbol());
            String regla = subindice.arbol() == null
                    ? "Término → Elemento"
                    : "Término → Elemento Subíndice";
            return new ResultadoTermino(new NodoDerivacionFormula(regla, simbolo + " × " + subindice.valor(), hijosTermino),
                    Map.of(simbolo, subindice.valor()));
        }

        private ResultadoSubindice analizarSubindice() {
            if (termino() || !esDigito(actual())) return new ResultadoSubindice(1, null);
            int inicio = posicion;
            while (!termino() && esDigito(actual())) posicion++;
            String digitos = entrada.substring(inicio, posicion);
            final int valor;
            try {
                valor = Integer.parseInt(digitos);
            } catch (NumberFormatException error) {
                throw new FormulaInvalidaException("El subíndice es demasiado grande.", inicio);
            }
            if (valor < 1) throw new FormulaInvalidaException("El subíndice debe ser un entero positivo.", inicio);
            if (valor > MAX_ATOMOS) throw new FormulaInvalidaException("El subíndice supera el límite permitido.", inicio);
            List<NodoDerivacionFormula> digitosNodo = digitos.chars()
                    .mapToObj(caracter -> new NodoDerivacionFormula("Dígito", Character.toString(caracter), List.of()))
                    .toList();
            return new ResultadoSubindice(valor,
                    new NodoDerivacionFormula("Subíndice → Dígito+", digitos, digitosNodo));
        }

        private Map<String, Integer> multiplicarAtomos(Map<String, Integer> atomos, int factor) {
            Map<String, Integer> resultado = new LinkedHashMap<>();
            atomos.forEach((simbolo, cantidad) -> {
                try {
                    int producto = Math.multiplyExact(cantidad, factor);
                    if (producto > MAX_ATOMOS) throw error("La cantidad de átomos supera el límite permitido.");
                    resultado.put(simbolo, producto);
                } catch (ArithmeticException desbordamiento) {
                    throw error("La cantidad de átomos supera el límite permitido.");
                }
            });
            return resultado;
        }

        private void sumarAtomos(Map<String, Integer> destino, Map<String, Integer> sumandos) {
            sumandos.forEach((simbolo, cantidad) -> {
                try {
                    int suma = Math.addExact(destino.getOrDefault(simbolo, 0), cantidad);
                    if (suma > MAX_ATOMOS) throw error("La cantidad de átomos supera el límite permitido.");
                    destino.put(simbolo, suma);
                } catch (ArithmeticException desbordamiento) {
                    throw error("La cantidad de átomos supera el límite permitido.");
                }
            });
        }

        private boolean termino() {
            return posicion >= entrada.length();
        }

        private char actual() {
            return termino() ? '\0' : entrada.charAt(posicion);
        }

        private int posicion() {
            return posicion;
        }

        private FormulaInvalidaException error(String mensaje) {
            return new FormulaInvalidaException(mensaje, posicion);
        }

        private boolean esMayuscula(char caracter) {
            return caracter >= 'A' && caracter <= 'Z';
        }

        private boolean esMinuscula(char caracter) {
            return caracter >= 'a' && caracter <= 'z';
        }

        private boolean esDigito(char caracter) {
            return caracter >= '0' && caracter <= '9';
        }
    }

    private record ResultadoSubindice(int valor, NodoDerivacionFormula arbol) {}
}
