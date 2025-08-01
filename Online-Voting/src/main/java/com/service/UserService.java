package com.example.Online.Voting.service;


import com.example.Online.Voting.dto.UserCreateDTO;
import com.example.Online.Voting.model.AadhaarDetails;
import com.example.Online.Voting.model.UserEntity;
import com.example.Online.Voting.repository.AadhaarRepository;
import com.example.Online.Voting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AadhaarService aadhaarService;     // for Aadhaar extraction
    private final FaceAuthService faceAuthService;   // for face encoding/comparison
    private final AadhaarRepository aadhaarDetailsRepository;

    public UserEntity createUser(String username,
                                 String password,
                                 AadhaarDetails aadhaarDetails,
                                 String faceEncoding) {

        if(userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username Already taken!!");
        }

        // Build a new UserEntity
        UserEntity user = UserEntity.builder()
                .username(username)
                .password(password)  // if you eventually store a hashed password
                .role("USER")
                .faceEncoding(faceEncoding)
                .aadhaarDetails(aadhaarDetails)
                .build();

        // Save to DB
        return userRepository.save(user);
    }

    /**
     * Checks if face encoding matches any existing user's faceEncoding
     * (Naive approach: loop over all users and call compareFaces).
     */
    public boolean isFaceAlreadyUsed(MultipartFile newFaceFile) throws Exception {
        // fetch all users that have a non-null faceEncoding
        List<UserEntity> allUsers = userRepository.findAll();
        for (UserEntity existingUser : allUsers) {
            if (existingUser.getFaceEncoding() != null) {
                boolean match = faceAuthService.compareFaces(newFaceFile, existingUser.getFaceEncoding());
                if (match) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    /**
     * Find a user by username, if needed.
     */
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Update an existing user (optional).
     */
    public UserEntity updateUser(UserEntity user) {
        return userRepository.save(user);
    }
}

