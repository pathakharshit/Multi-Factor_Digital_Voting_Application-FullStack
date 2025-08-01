package com.example.Online.Voting.controller;

import com.example.Online.Voting.model.AadhaarDetails;
import com.example.Online.Voting.service.AadhaarService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * This controller exposes the endpoint for Aadhaar verification.
 * It accepts an Aadhaar card image and a mobile number, then returns the extracted details along with a generated OTP.
 */
@RestController
@RequestMapping("/api/aadhaar")
public class AadhaarController {

    @Autowired
    private AadhaarService aadhaarService;

    /**
     * POST /api/aadhaar/verify
     * Accepts an Aadhaar photo and a mobile number provided by the user.
     *
     * @param aadhaarPhoto the uploaded Aadhaar card image
     * @param mobileNumber the mobile number provided manually by the user
     * @return AadhaarDetails object with extracted data and generated OTP, or a 400 error if verification fails.
     */
    @PostMapping("/verify")
    public ResponseEntity<AadhaarDetails> verifyAadhaar(
            @RequestParam("aadhaarPhoto") MultipartFile aadhaarPhoto,
            @RequestParam("mobileNumber") String mobileNumber,
            HttpSession session) {
        try {
            AadhaarDetails details = aadhaarService.verifyAadhaar(aadhaarPhoto, mobileNumber);

            session.setAttribute("phoneNumber", mobileNumber);

            return ResponseEntity.ok(details);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

