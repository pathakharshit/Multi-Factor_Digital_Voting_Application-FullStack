package com.example.Online.Voting.repository;

import com.example.Online.Voting.model.UserEventVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEventVoteRepository extends JpaRepository<UserEventVote, Long> {

    // If you want to check if a user has already voted in an event
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
