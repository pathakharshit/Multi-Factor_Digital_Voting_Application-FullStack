package com.example.Online.Voting.repository;

import com.example.Online.Voting.enums.EventStatus;
import com.example.Online.Voting.model.VotingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VotingEventRepository extends JpaRepository<VotingEvent, Long> {
    List<VotingEvent> findByStatus(EventStatus status);
}
