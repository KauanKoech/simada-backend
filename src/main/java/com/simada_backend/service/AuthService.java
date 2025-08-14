package com.simada_backend.service;

import com.simada_backend.model.Trainer;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public void registerTrainer(Trainer trainer) {
        System.out.println("=== DADOS RECEBIDOS DO FRONT ===");
        System.out.println("Nome: " + trainer.getFullName());
        System.out.println("Email: " + trainer.getEmail());
        System.out.println("Senha: " + trainer.getPassword());
    }
}