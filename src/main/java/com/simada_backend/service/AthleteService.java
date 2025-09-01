package com.simada_backend.service;

import com.simada_backend.dto.request.athlete.UpdateAthleteRequest;
import com.simada_backend.dto.response.athlete.AthleteDetailDTO;
import com.simada_backend.dto.response.athlete.AthleteExtraDTO;
import com.simada_backend.model.Atleta;
import com.simada_backend.model.AtletaExtra;
import com.simada_backend.model.Usuario;
import com.simada_backend.repository.UsuarioRepository;
import com.simada_backend.repository.athlete.AtletaExtraRepository;
import com.simada_backend.repository.athlete.AtletaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AthleteService {

    private final AtletaRepository atletaRepo;
    private final AtletaExtraRepository extraRepo;
    private final UsuarioRepository usuarioRepo;

    @Transactional
    public AthleteDetailDTO getAthlete(Long trainerId, Long athleteId) {
        Atleta atleta = atletaRepo.findByIdAtletaAndTreinador_Id(athleteId, trainerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atleta não encontrado para este treinador"));

        Usuario u = atleta.getUsuario();
        AtletaExtra ex = atleta.getExtra();

        return new AthleteDetailDTO(
                atleta.getIdAtleta(),
                atleta.getFullName(),
                u != null ? u.getEmail() : null,
                u != null ? u.getTelefone() : null,
                u != null && u.getDataNascimento() != null ? u.getDataNascimento().toString() : null,
                u != null ? u.getFoto() : null,
                atleta.getShirtNumber() != null ? String.valueOf(atleta.getShirtNumber()) : null,
                mapDbPosToUi(atleta.getPosition()),
                ex == null ? null : new AthleteExtraDTO(
                        ex.getHeightCm(), ex.getWeightKg(), ex.getLeanMassKg(), ex.getFatMassKg(),
                        ex.getBodyFatPct(), ex.getDominantFoot(), ex.getNationality(), ex.getInjuryStatus()
                )
        );
    }

    @Transactional
    public AthleteDetailDTO updateAthlete(Long trainerId, Long athleteId, UpdateAthleteRequest req) {
        Atleta atleta = atletaRepo.findByIdAtletaAndTreinador_Id(athleteId, trainerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atleta não encontrado para este treinador"));

        // Atualiza campos de Atleta
        if (req.getName() != null) atleta.setFullName(req.getName());
        if (req.getPosition() != null) atleta.setPosition(mapUiPosToDb(req.getPosition()));

        // Atualiza dados do Usuario
        Usuario u = atleta.getUsuario();
        if (u != null) {
            if (req.getEmail() != null) u.setEmail(req.getEmail());
            if (req.getPhone() != null) u.setTelefone(req.getPhone());
            if (req.getBirth() != null && !req.getBirth().isBlank()) {
                try {
                    u.setDataNascimento(LocalDate.parse(req.getBirth()));
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de birth inválido (use YYYY-MM-DD)");
                }
            }
            usuarioRepo.save(u);
        }

        // Extra
        if (req.getExtra() != null) {
            AtletaExtra ex = extraRepo.findByAtleta_IdAtleta(athleteId).orElseGet(() -> {
                AtletaExtra novo = new AtletaExtra();
                novo.setAtleta(atleta);
                return novo;
            });

            var e = req.getExtra();
            ex.setHeightCm(e.height_cm());
            ex.setWeightKg(e.weight_kg());
            ex.setLeanMassKg(e.lean_mass_kg());
            ex.setFatMassKg(e.fat_mass_kg());
            ex.setBodyFatPct(e.body_fat_pct());
            ex.setDominantFoot(e.dominant_foot());
            ex.setNationality(e.nationality());
            ex.setInjuryStatus(e.injury_status());

            extraRepo.save(ex);
            atleta.setExtra(ex);
        }

        atletaRepo.save(atleta);
        return getAthlete(trainerId, athleteId);
    }

    private String mapUiPosToDb(String ui) {
        if (ui == null) return null;
        return switch (ui) {
            case "Goalkeeper" -> "Goleiro";
            case "Defender"   -> "Zagueiro";
            case "Midfielder" -> "Meio Campo";
            case "Forward"    -> "Atacante";
            default -> ui;
        };
    }

    private String mapDbPosToUi(String db) {
        if (db == null) return null;
        return switch (db) {
            case "Goleiro"    -> "Goalkeeper";
            case "Zagueiro"   -> "Defender";
            case "Meio Campo" -> "Midfielder";
            case "Atacante"   -> "Forward";
            default -> db; // mantém se já vier EN
        };
    }
}
