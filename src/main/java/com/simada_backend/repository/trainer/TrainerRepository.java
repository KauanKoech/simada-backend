package com.simada_backend.repository.trainer;

import com.simada_backend.model.Treinador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Treinador, Long> {
}
