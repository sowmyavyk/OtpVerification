package com.example.OTP.config;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;  // Changed from javax to jakarta

@Configuration
public class TwilioConfig {
    @Value("${twilio.account.sid}")
    private String accountSid;
    
    @Value("${twilio.auth.token}")
    private String authToken;
    
    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }
}