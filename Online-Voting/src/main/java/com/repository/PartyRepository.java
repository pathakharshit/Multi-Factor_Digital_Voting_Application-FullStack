package com.example.Online.Voting.repository;

import com.example.Online.Voting.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party,Long> {
    boolean existsByPartyName(String partyName);
}
