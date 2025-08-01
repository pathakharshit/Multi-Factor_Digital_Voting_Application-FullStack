package com.example.Online.Voting.repository;


import com.example.Online.Voting.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // A simple query method to fetch a user by username
    Optional<UserEntity> findByUsername(String username);

    //boolean existsByMobileNumber(String mobileNumber);
}
