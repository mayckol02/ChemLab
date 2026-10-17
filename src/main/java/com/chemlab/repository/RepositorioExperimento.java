package com.chemlab.repository;

import com.chemlab.model.entity.Experimento;
import java.util.Optional;
import java.util.List;

public interface RepositorioExperimento {
    void guardar(Experimento experimento);
    Optional<Experimento> cargar(String id);
    List<Experimento> listarTodos();
}
