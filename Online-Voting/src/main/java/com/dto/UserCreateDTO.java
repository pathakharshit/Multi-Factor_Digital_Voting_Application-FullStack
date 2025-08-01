package com.example.Online.Voting.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserCreateDTO {

    private String username;
    private String password;    // if you plan to store a hashed password
    private String mobileNumber;

    // Aadhaar photo file (for Aadhaar extraction)
    private MultipartFile aadhaarPhoto;

    // Face photo file (for face recognition)
    private MultipartFile facePhoto;
}
