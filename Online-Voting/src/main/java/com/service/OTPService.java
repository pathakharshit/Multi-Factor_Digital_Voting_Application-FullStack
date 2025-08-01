package com.example.Online.Voting.service;

import com.example.Online.Voting.config.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {

    private static final ConcurrentHashMap<String, OtpInfo> otpCache = new ConcurrentHashMap<>();
    private static final int OTP_VALIDITY_SECONDS = 120; // 2 minutes

    @Autowired
    private TwilioConfig twilioConfig;

    public String generateOTP(String phoneNumber) {
        // 1) Generate a 4-digit OTP
        String otp = String.valueOf(1000 + new Random().nextInt(9000));

        // 2) Compute expiry time (now + 2 minutes)
        Instant expiryTime = Instant.now().plusSeconds(OTP_VALIDITY_SECONDS);

        // 3) Store in the map
        otpCache.put(phoneNumber, new OtpInfo(otp, expiryTime));
        //System.out.println("My OTP : " + otp);

        // 4) Send OTP via Twilio
        sendOtpToUser(phoneNumber, otp);

        return otp;
    }

    public boolean verifyOTP(String phoneNumber, String userInputOtp) {
        OtpInfo info = otpCache.get(phoneNumber);
        if (info == null) {
            return false;
        }
        if (Instant.now().isAfter(info.expiryTime)) {
            otpCache.remove(phoneNumber);
            return false;
        }
        boolean isMatch = info.otp.equals(userInputOtp);
        if (isMatch) {
            otpCache.remove(phoneNumber);
        }
        return isMatch;
    }

    private void sendOtpToUser(String phoneNumber, String otp) {
        // Initialize Twilio with your Account SID and Auth Token
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());

        String messageBody = "Your OTP is " + otp + ". It will expire in 2 minutes.";

        // phoneNumber must be in E.164 format, e.g. +919876543210 for India
        Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioConfig.getFromNumber()),
                messageBody
        ).create();
    }

    private static class OtpInfo {
        String otp;
        Instant expiryTime;
        public OtpInfo(String otp, Instant expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
}

