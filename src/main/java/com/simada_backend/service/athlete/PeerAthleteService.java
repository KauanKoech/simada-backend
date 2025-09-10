package com.simada_backend.service.athlete;

import com.simada_backend.dto.response.athlete.PeerAthleteDTO;
import com.simada_backend.repository.athlete.PeerAthleteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PeerAthleteService {

    private final PeerAthleteRepository repo;

    public List<PeerAthleteDTO> listPeers(Long athleteId, boolean includeSelf) {
        var rows = repo.findPeersByAthlete(athleteId);
        return rows.stream()
                .filter(p -> includeSelf || !p.getId().equals(athleteId))
                .map(p -> new PeerAthleteDTO(
                        p.getId(),
                        p.getName(),
                        p.getEmail(),
                        p.getPosition(),
                        p.getJersey(),
                        p.getNationality(),
                        p.getAvatar(),
                        p.getPoints()
                ))
                .toList();
    }
}