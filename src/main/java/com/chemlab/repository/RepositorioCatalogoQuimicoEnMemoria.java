package com.chemlab.repository;

import com.chemlab.catalogo.CatalogoQuimico;

import java.util.Objects;

public final class RepositorioCatalogoQuimicoEnMemoria
        implements RepositorioCatalogoQuimico {

    private final CatalogoQuimico catalogoQuimico;

    public RepositorioCatalogoQuimicoEnMemoria() {
        this(CatalogoQuimico.predeterminado());
    }

    public RepositorioCatalogoQuimicoEnMemoria(
            CatalogoQuimico catalogoQuimico
    ) {
        this.catalogoQuimico = Objects.requireNonNull(
                catalogoQuimico,
                "El catálogo químico no puede ser null"
        );
    }

    @Override
    public CatalogoQuimico cargar() {
        return catalogoQuimico;
    }
}