package com.simada_backend.service;

import com.simada_backend.dto.response.TopPerformerDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainerService {
    private final com.simada_backend.repository.RankingRepository repo;

    public TrainerService(com.simada_backend.repository.RankingRepository repo) {
        this.repo = repo;
    }

    public List<TopPerformerDTO> getTopPerformers(int limit /*, Integer trainerId */) {
        int safe = Math.max(1, Math.min(limit, 50)); // saneamento
        System.out.println("COMUNICAÇÃO FEITA");
//        return repo.findTopPerformers(safe /*, trainerId */).stream()
//                .map(r -> new TopPerformerDTO(
//                        r.getNome_atleta(),
//                        r.getFoto(),               // hoje está null; ajuste se buscar a foto
//                        r.getData_atualizacao(),
//                        r.getPontuacao(),
//                        r.getUltima_pontuacao()
//                ))
//                .toList();
        return null;
    }
}