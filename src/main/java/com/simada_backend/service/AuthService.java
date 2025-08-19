package com.simada_backend.service;

import com.simada_backend.dto.*;
import com.simada_backend.model.*;
import com.simada_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TreinadorRepository treinadorRepository;

    @Autowired
    private AtletaRepository atletaRepository;

    public void registerTrainer(RegisterTrainerRequest request) {
        // Cria usuário
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setSenha(request.getPassword());
        usuario.setTipoUsuario("treinador");
        usuarioRepository.save(usuario);

        // Cria treinador associado
        Treinador treinador = new Treinador();
        treinador.setFullName(request.getFullName());
        treinador.setModality(request.getModality());
        treinador.setGender(request.getGender());
        treinador.setUsuario(usuario);
        treinadorRepository.save(treinador);
    }

    public void registerAthlete(RegisterAthleteRequest request) {
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setSenha(request.getPassword());
        usuario.setTipoUsuario("atleta");
        usuarioRepository.save(usuario);

        Atleta atleta = new Atleta();
        atleta.setFullName(request.getFullName());
        atleta.setGender(request.getGender());
        atleta.setUsuario(usuario);
        atletaRepository.save(atleta);
    }

    public Usuario login(LoginRequest request) {
        return usuarioRepository.findByEmail(request.getEmail())
                .filter(u -> u.getSenha().equals(request.getPassword()))
                .orElse(null); // aqui você pode lançar exceção caso queira
    }
}
