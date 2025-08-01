package com.example.Online.Voting.repository;

import com.example.Online.Voting.model.AadhaarDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AadhaarRepository extends JpaRepository<AadhaarDetails,Long> {
    boolean existsByAadhaarNumber(String aadhaarNumber);

    boolean existsByMobileNumber(String mobileNumber);
}
