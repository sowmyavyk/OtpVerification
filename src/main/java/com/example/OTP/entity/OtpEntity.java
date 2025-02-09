package com.example.OTP.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class OtpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String phoneNumber;
    private String otp;
    private LocalDateTime expiryTime;
    private boolean verified;
}