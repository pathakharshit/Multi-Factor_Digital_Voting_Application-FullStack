package com.example.Online.Voting.repository;

import com.example.Online.Voting.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    // Example: find all candidates for a specific event
    List<Candidate> findByVotingEventId(Long eventId);
}
