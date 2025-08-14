package com.simada_backend.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String registrarTreinador(String nome, String email, String senha) throws Exception {
        // Configuração para criar um usuário no Firebase
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setDisplayName(nome)
                .setEmail(email)
                .setPassword(senha);

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

        // Retorna o UID gerado pelo Firebase
        return userRecord.getUid();
    }
}