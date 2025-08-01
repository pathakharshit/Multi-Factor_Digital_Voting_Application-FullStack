package com.example.Online.Voting.controller;

import com.example.Online.Voting.model.AadhaarDetails;
import com.example.Online.Voting.model.UserEntity;
import com.example.Online.Voting.service.FaceAuthService;
import com.example.Online.Voting.service.OTPService;
import com.example.Online.Voting.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OTPService otpService;
    private final FaceAuthService faceAuthService; // if you need to re-encode face, optional
    private final UserService userService;

    @PostMapping("/complete")
    public ResponseEntity<?> completeUserCreation(@RequestParam("otp") String otp,
                                                  HttpSession session) {
        try {
            // 1) Fetch phoneNumber from session
            String phoneNumber = (String) session.getAttribute("phoneNumber");
            System.out.println("When otp verify : " + phoneNumber);
            if (phoneNumber == null) {
                return ResponseEntity.badRequest().body("No phone number found in session or session expired.");
            }

            // 2) Verify OTP
            boolean isValid = otpService.verifyOTP(phoneNumber, otp);
            if (!isValid) {
                return ResponseEntity.badRequest().body("OTP invalid or expired.");
            }

            // 3) OTP is correct => fetch data from session
            AadhaarDetails details = (AadhaarDetails) session.getAttribute("aadhaarDetails");
            String faceEncoding = (String) session.getAttribute("faceEncoding");
            String username = (String) session.getAttribute("username");
            String password = (String) session.getAttribute("password");

            if (details == null || username == null) {
                return ResponseEntity.badRequest().body("Session data missing or expired. Start over.");
            }

            // 4) Create user in DB now
            UserEntity newUser = userService.createUser(username, password, details, faceEncoding);

            // 5) Clear session
            session.removeAttribute("phoneNumber");
            session.removeAttribute("aadhaarDetails");
            session.removeAttribute("faceEncoding");
            session.removeAttribute("username");
            session.removeAttribute("password");

            // 6) Return success
            Map<String, Object> result = new HashMap<>();
            result.put("id", newUser.getId());
            result.put("message", "User created successfully!");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}



