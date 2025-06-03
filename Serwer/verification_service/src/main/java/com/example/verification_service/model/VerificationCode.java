package com.example.verification_service.model;

import java.time.LocalDateTime;

import com.example.verification_service.enums.VerificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VerificationCodes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    private String userId; 
    
    private String code;

    private LocalDateTime expiration;
    
    private boolean used = false;

    private VerificationType type;

}
