package com.example.OTP.repository;

import com.example.OTP.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
    Optional<OtpEntity> findByPhoneNumberAndVerifiedFalse(String phoneNumber);
}
