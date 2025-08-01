package com.example.Online.Voting.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String candidateName;
    private String description;
    private int voteCount;        // increment when a user votes

    // Link to the VotingEvent this candidate belongs to
    @ManyToOne
    @JoinColumn(name = "event_id")
    private VotingEvent votingEvent;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;
}
