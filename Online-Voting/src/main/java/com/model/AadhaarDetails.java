package com.example.Online.Voting.model;

import jakarta.persistence.*;
import lombok.*;

// JPA annotation to mark this class as a database entity.
@Entity
// Specifies the database table name (optional).
@Table(name = "aadhaar_details")
// Lombok annotations to generate getters, setters, constructors, equals, hashCode, and toString methods.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AadhaarDetails {

    // Primary key for the entity.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 12-digit Aadhaar number extracted from the Aadhaar card.
    @Column(name = "aadhaar_number", nullable = false, unique = true)
    private String aadhaarNumber;

    // Name of the user as extracted from the Aadhaar card.
    @Column(name = "name", nullable = false)
    private String name;

    // Date of birth of the user (format: YYYY-MM-DD).
    @Column(name = "dob", nullable = false)
    private String dob;

    @Column(name = "age")
    private Integer age;

    // Mobile number extracted from the Aadhaar card.
    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    // One-time password (OTP) generated for verification.
//    @Column(name = "otp")
//    private String otp;
}
