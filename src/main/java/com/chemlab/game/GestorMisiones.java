package com.chemlab.game;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controla el avance de la ruta introductoria durante la sesión actual.
 */
public final class GestorMisiones {

    public record Mision(
            String id,
            String titulo,
            String objetivo,
            String reaccionId,
            String aprendizaje
    ) {}

    private static final List<Mision> RUTA_INICIAL = List.of(
            new Mision(
                    "fundamentos-agua",
                    "Primer compuesto: agua",
                    "Completa la práctica de formación de agua.",
                    "R_H2O",
                    "Identifica cómo los reactivos se transforman en un compuesto y lee la ecuación química."
            ),
            new Mision(
                    "combustion-carbono",
                    "Transformación del carbono",
                    "Completa la combustión del carbono.",
                    "R_CO2",
                    "Reconoce la formación de dióxido de carbono al combinar carbono y oxígeno."
            ),
            new Mision(
                    "sintesis-sal",
                    "Formación de una sal",
                    "Completa la síntesis de cloruro de sodio.",
                    "R_NaCl",
                    "Distingue una reacción de síntesis de una mezcla física."
            ),
            new Mision(
                    "disolucion-sal",
                    "Preparar una disolución",
                    "Disuelve cloruro de sodio en agua.",
                    "R_DISOLUCION_NACL",
                    "Observa cómo una sustancia puede dispersarse en un disolvente sin formar un compuesto nuevo."
            ),
            new Mision(
                    "recuperacion-sal",
                    "Recuperar el soluto",
                    "Separa la sal de la disolución mediante evaporación.",
                    "R_SEPARACION_NACL",
                    "Relaciona la evaporación del disolvente con la recuperación del sólido disuelto."
            )
    );

    private int indiceActual;
    private boolean rutaIniciada;

    public GestorMisiones() {
        this(0, false);
    }

    public GestorMisiones(int misionesCompletadas, boolean rutaIniciada) {
        if (misionesCompletadas < 0 || misionesCompletadas > RUTA_INICIAL.size()) {
            throw new IllegalArgumentException("El progreso de misiones está fuera del rango permitido.");
        }
        if (misionesCompletadas > 0 && !rutaIniciada) {
            throw new IllegalArgumentException("Una ruta con misiones completadas debe estar iniciada.");
        }
        this.indiceActual = misionesCompletadas;
        this.rutaIniciada = rutaIniciada;
    }

    public List<Mision> obtenerMisiones() {
        return RUTA_INICIAL;
    }

    public boolean iniciarRuta() {
        if (estaCompletada()) return false;
        rutaIniciada = true;
        return true;
    }

    public boolean estaIniciada() {
        return rutaIniciada;
    }

    public boolean estaCompletada() {
        return indiceActual >= RUTA_INICIAL.size();
    }

    public int obtenerCantidadCompletadas() {
        return indiceActual;
    }

    public Optional<Mision> obtenerMisionActual() {
        if (!rutaIniciada || estaCompletada()) return Optional.empty();
        return Optional.of(RUTA_INICIAL.get(indiceActual));
    }

    public boolean estaCompletada(Mision mision) {
        int indice = RUTA_INICIAL.indexOf(Objects.requireNonNull(mision, "La misión no puede ser null"));
        return indice >= 0 && indice < indiceActual;
    }

    public boolean estaDesbloqueada(Mision mision) {
        int indice = RUTA_INICIAL.indexOf(Objects.requireNonNull(mision, "La misión no puede ser null"));
        return indice >= 0 && (indice < indiceActual
                || rutaIniciada && indice == indiceActual);
    }

    /**
     * Avanza únicamente si la práctica completada corresponde a la misión activa.
     */
    public boolean registrarPracticaCompletada(String reaccionId) {
        if (!rutaIniciada || estaCompletada() || reaccionId == null) return false;
        if (!RUTA_INICIAL.get(indiceActual).reaccionId().equals(reaccionId)) return false;
        indiceActual++;
        return true;
    }
}
