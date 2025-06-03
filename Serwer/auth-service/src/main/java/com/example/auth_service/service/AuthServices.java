package com.example.auth_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServices {

	@Autowired
	    private VerificationClient verificationClient;
	
	
	public boolean verifyUser2fa(String username, String code) {
        boolean valid = verificationClient.verify2FACode(username, code);
        if (!valid) {
            throw new IllegalArgumentException("Niepoprawny lub wygasły kod 2FA");
        }

        return valid;
    }
	
	public void generateUser2fa(String username, String email) {
        verificationClient.generate2FACode(username, email);

    }
}
