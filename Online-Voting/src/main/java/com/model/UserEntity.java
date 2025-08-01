package com.example.Online.Voting.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique username
    @Column(unique = true, nullable = false)
    private String username;

    // If you plan to store password for later use (optional)
    private String password;

    // e.g. "USER", "ADMIN"
    private String role;

    // Store the user's face photo encoding for face authentication
    @Lob
    private String faceEncoding;

    // One-to-one link with AadhaarDetails
    // Each user has exactly one AadhaarDetails record
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "aadhaar_id")  // foreign key in 'users' referencing 'aadhaar_details'(id)
    private AadhaarDetails aadhaarDetails;
}
