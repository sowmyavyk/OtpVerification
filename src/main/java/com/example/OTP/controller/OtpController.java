package com.example.OTP.controller;

import com.example.OTP.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/otp")
public class OtpController {
    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(@RequestParam String phoneNumber) {
        try {
            otpService.sendOtp(phoneNumber);
            return ResponseEntity.ok("OTP sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(
            @RequestParam String phoneNumber,
            @RequestParam String otp) {
        try {
            boolean isValid = otpService.verifyOtp(phoneNumber, otp);
            if (isValid) {
                return ResponseEntity.ok("OTP verified successfully");
            }
            return ResponseEntity.badRequest().body("Invalid OTP");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Verification failed: " + e.getMessage());
        }
    }
}