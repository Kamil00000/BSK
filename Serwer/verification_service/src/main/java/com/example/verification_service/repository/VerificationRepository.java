package com.example.verification_service.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.verification_service.model.VerificationCode;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationCode, Long> {
	@Query("SELECT v FROM VerificationCode v WHERE v.userId = :userId AND v.code = :code AND v.used = false AND v.expiration > :now")
	Optional<VerificationCode> findValidCode(@Param("userId") String userId, @Param("code") String code, @Param("now") LocalDateTime now);


    default void invalidate(VerificationCode code) {
        code.setUsed(true);
        save(code);
    }
}