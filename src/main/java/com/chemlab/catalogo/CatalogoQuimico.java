package com.chemlab.catalogo;

import com.chemlab.model.entity.Reaccion;
import com.chemlab.model.entity.Sustancia;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CatalogoQuimico {

    private final CatalogoSustancias catalogoSustancias;
    private final CatalogoReacciones catalogoReacciones;

    public CatalogoQuimico(
            CatalogoSustancias catalogoSustancias,
            CatalogoReacciones catalogoReacciones
    ) {
        this.catalogoSustancias =
                Objects.requireNonNull(
                        catalogoSustancias,
                        "El catálogo de sustancias no puede ser null"
                );

        this.catalogoReacciones =
                Objects.requireNonNull(
                        catalogoReacciones,
                        "El catálogo de reacciones no puede ser null"
                );

        validarSustanciasDeReacciones();
    }

    public static CatalogoQuimico predeterminado() {
        return new com.chemlab.repository.RepositorioCatalogoQuimicoJson().cargar();
    }

    public Optional<Sustancia> buscarSustancia(
            String id
    ) {
        return catalogoSustancias.buscarPorId(id);
    }

    public Optional<Reaccion> buscarReaccion(
            String id
    ) {
        return catalogoReacciones.buscarPorId(id);
    }

    public List<Sustancia> obtenerSustancias() {
        return catalogoSustancias.obtenerTodas();
    }

    public List<Reaccion> obtenerReacciones() {
        return catalogoReacciones.obtenerTodas();
    }

    private void validarSustanciasDeReacciones() {
        for (Reaccion reaccion
                : catalogoReacciones.obtenerTodas()) {

            validarSustancias(
                    reaccion.getReactivos().keySet(),
                    reaccion
            );

            validarSustancias(
                    reaccion.getProductos().keySet(),
                    reaccion
            );
        }
    }

    private void validarSustancias(
            Iterable<Sustancia> sustancias,
            Reaccion reaccion
    ) {
        for (Sustancia sustancia : sustancias) {
            if (!catalogoSustancias.contiene(
                    sustancia.getId()
            )) {
                throw new IllegalArgumentException(
                        "La reacción "
                                + reaccion.getId()
                                + " usa una sustancia no registrada: "
                                + sustancia.getId()
                );
            }
        }
    }
}