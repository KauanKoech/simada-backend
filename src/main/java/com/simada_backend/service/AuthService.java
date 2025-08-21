package com.simada_backend.service;

import com.simada_backend.dto.request.LoginRequest;
import com.simada_backend.dto.request.RegisterAthleteRequest;
import com.simada_backend.dto.request.RegisterTrainerRequest;
import com.simada_backend.model.Atleta;
import com.simada_backend.model.Treinador;
import com.simada_backend.model.Usuario;
import com.simada_backend.repository.AtletaRepository;
import com.simada_backend.repository.TreinadorRepository;
import com.simada_backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TreinadorRepository treinadorRepository;
    private final AtletaRepository atletaRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
                       TreinadorRepository treinadorRepository,
                       AtletaRepository atletaRepository,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
        this.treinadorRepository = Objects.requireNonNull(treinadorRepository);
        this.atletaRepository = Objects.requireNonNull(atletaRepository);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    @Transactional
    public void registerTrainer(RegisterTrainerRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setSenha(passwordEncoder.encode(request.getPassword()));
        usuario.setTipoUsuario("treinador");
        usuarioRepository.save(usuario);

        Treinador treinador = new Treinador();
        treinador.setFullName(request.getFullName());
        treinador.setModality(request.getModality());
        treinador.setGender(request.getGender());
        treinador.setUsuario(usuario);
        treinadorRepository.save(treinador);
    }

    @Transactional
    public void registerAthlete(RegisterAthleteRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setSenha(passwordEncoder.encode(request.getPassword())); // BCrypt
        usuario.setTipoUsuario("atleta");
        usuarioRepository.save(usuario);

        Atleta atleta = new Atleta();
        atleta.setFullName(request.getFullName());
        atleta.setGender(request.getGender());
        atleta.setUsuario(usuario);
        atletaRepository.save(atleta);
    }

    public Usuario login(LoginRequest request) {
        Usuario user = usuarioRepository
                .findFirstByEmailOrderByIdDesc(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        return user;
    }
}
