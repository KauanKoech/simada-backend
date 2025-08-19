package com.simada_backend.service;

import com.simada_backend.model.Athlete;
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

    public void loginTrainer(Trainer trainer) {
        System.out.println("=== DADOS RECEBIDOS DO FRONT ===");
        System.out.println("Nome: " + trainer.getFullName());
        System.out.println("Email: " + trainer.getEmail());
        System.out.println("Senha: " + trainer.getPassword());
    }

    public void registerAthlete(Athlete athlete) {
        System.out.println("=== DADOS RECEBIDOS DO FRONT ===");
        System.out.println("Nome: " + athlete.getFullName());
        System.out.println("Email: " + athlete.getEmail());
        System.out.println("Senha: " + athlete.getPassword());
    }

    public void loginAthlete(Athlete athlete) {
        System.out.println("=== DADOS RECEBIDOS DO FRONT ===");
        System.out.println("Nome: " + athlete.getFullName());
        System.out.println("Email: " + athlete.getEmail());
        System.out.println("Senha: " + athlete.getPassword());
    }
}