package com.example.OTP.service;

import com.example.OTP.entity.OtpEntity;
import com.example.OTP.repository.OtpRepository;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final OtpRepository otpRepository;
    
    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;
    
    public OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }
    
    @Transactional
    public void sendOtp(String phoneNumber) {
        // Preprocess the phone number
        String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
        
        logger.info("Processing OTP for formatted number: {}", formattedPhoneNumber);
        
        // Validate formatted number
        if (!isValidPhoneNumber(formattedPhoneNumber)) {
            logger.error("Invalid formatted number: {}", formattedPhoneNumber);
            throw new IllegalArgumentException("Invalid phone number");
        }
        
        try {
            // Check for existing unverified OTP
            otpRepository.findByPhoneNumberAndVerifiedFalse(formattedPhoneNumber)
                .ifPresent(existing -> {
                    logger.info("Removing existing unverified OTP for phone number: {}", formattedPhoneNumber);
                    otpRepository.delete(existing);
                });
            
            String otp = generateOtp();
            
            OtpEntity otpEntity = new OtpEntity();
            otpEntity.setPhoneNumber(formattedPhoneNumber);
            otpEntity.setOtp(otp);
            otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
            otpEntity.setVerified(false);
            
            otpRepository.save(otpEntity);
            logger.info("OTP entity saved successfully for phone number: {}", formattedPhoneNumber);
            
            // Send SMS via Twilio
            try {
                Message message = Message.creator(
                    new PhoneNumber(formattedPhoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    "Your OTP is: " + otp
                ).create();
                
                logger.info("Twilio message sent successfully. SID: {}", message.getSid());
            } catch (ApiException e) {
                logger.error("Twilio API error: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to send OTP via Twilio", e);
            }
            
        } catch (Exception e) {
            logger.error("Error in sendOtp process: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process OTP request", e);
        }
    }
    
    @Transactional
    public boolean verifyOtp(String phoneNumber, String otp) {
        String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
        logger.info("Initiating OTP verification for phone number: {}", formattedPhoneNumber);
        
        try {
            OtpEntity otpEntity = otpRepository.findByPhoneNumberAndVerifiedFalse(formattedPhoneNumber)
                .orElseThrow(() -> {
                    logger.error("No OTP found for verification for phone number: {}", formattedPhoneNumber);
                    return new RuntimeException("No OTP found for verification");
                });
                
            if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
                logger.error("OTP expired for phone number: {}", formattedPhoneNumber);
                throw new RuntimeException("OTP has expired");
            }
            
            if (otpEntity.getOtp().equals(otp)) {
                otpEntity.setVerified(true);
                otpRepository.save(otpEntity);
                logger.info("OTP verified successfully for phone number: {}", formattedPhoneNumber);
                return true;
            }
            
            logger.warn("Invalid OTP attempt for phone number: {}", formattedPhoneNumber);
            return false;
            
        } catch (Exception e) {
            logger.error("Error in verifyOtp process: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to verify OTP", e);
        }
    }
    
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    private String formatPhoneNumber(String phoneNumber) {
        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        if (digitsOnly.length() == 10) {
            return "+91" + digitsOnly; // Prepend +91 for 10-digit numbers
        }
        return phoneNumber; // Return original if not 10 digits
    }
    
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("^\\+91[6-9]\\d{9}$");
    }
}