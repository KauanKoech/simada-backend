package com.simada_backend.service.session;

import com.simada_backend.dto.response.sessionGraphs.AthleteListRow;
import com.simada_backend.repository.session.SessionMetricsQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionChartsService {

    private final SessionMetricsQueryRepository repo;

    public List<SessionMetricsQueryRepository.AthleteListRow> listAthletes(Long sessionId) {
        return repo.listAthletesInSession(sessionId);
    }

    public List<SessionMetricsQueryRepository.MetricsRowView> listMetricsForTeam(Long sessionId) {
        return repo.listMetricsForTeam(sessionId);
    }

    public List<SessionMetricsQueryRepository.MetricsRowView> listMetricsForAthlete(Long sessionId, Long athleteId) {
        return repo.listMetricsForAthlete(sessionId, athleteId);
    }
}