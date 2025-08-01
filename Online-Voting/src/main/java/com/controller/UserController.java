package com.example.Online.Voting.controller;



import com.example.Online.Voting.dto.UserCreateDTO;
import com.example.Online.Voting.model.AadhaarDetails;
import com.example.Online.Voting.service.AadhaarService;
import com.example.Online.Voting.service.FaceAuthService;
import com.example.Online.Voting.service.OTPService;
import com.example.Online.Voting.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final AadhaarService aadhaarService;
    private final FaceAuthService faceAuthService;
    private final UserService userService;
    // Note: We do NOT inject UserService here for step 1

    /**
     * STEP 1: /api/user/create
     *  - Extract Aadhaar details from aadhaarPhoto
     *  - Optionally encode face if facePhoto present
     *  - Generate OTP (already in AadhaarService)
     *  - Store everything in session, but DO NOT create user in DB yet
     */
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@ModelAttribute UserCreateDTO request, HttpSession session) {
        try {
            if(request.getUsername() == null) {
                throw new RuntimeException("Username can't be null");
            }

            if(userService.isUsernameExists(request.getUsername())) {
                throw new RuntimeException("Username is already taken " + request.getUsername());
            }
            if(request.getFacePhoto() == null || request.getFacePhoto().isEmpty()) {
                throw new RuntimeException("Face authentication is important!!");
            }

            // 2) Face encoding (if provided)
            String faceEncoding = null;
            if (request.getFacePhoto() != null && !request.getFacePhoto().isEmpty()) {
                if(userService.isFaceAlreadyUsed(request.getFacePhoto())) {
                    throw new RuntimeException("This face is already registered!!");
                }
                faceEncoding = faceAuthService.encodeFace(request.getFacePhoto());
            }

            if(request.getAadhaarPhoto() == null || request.getAadhaarPhoto().isEmpty()) {
                throw new RuntimeException("Aadhar authentication is important!!");
            }

            // 1) Aadhaar extraction
            MultipartFile aadhaarPhoto = request.getAadhaarPhoto();
            AadhaarDetails details = aadhaarService.verifyAadhaar(aadhaarPhoto, request.getMobileNumber());


            // 3) Store data in session (AadhaarDetails, faceEncoding, username, password)
            session.setAttribute("aadhaarDetails", details);
            session.setAttribute("faceEncoding", faceEncoding);
            session.setAttribute("username", request.getUsername());
            session.setAttribute("password", request.getPassword());
            session.setAttribute("phoneNumber",request.getMobileNumber());

            String phoneNumber = (String) session.getAttribute("phoneNumber");

            System.out.println("When user is created" + phoneNumber);

            // The OTP is already generated by AadhaarService, so user must verify next
            // Return success with a message
            return ResponseEntity.ok("Aadhaar verified, OTP sent. Please verify OTP at /api/otp/complete");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}


