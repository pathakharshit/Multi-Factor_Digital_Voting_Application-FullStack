package com.example.Online.Voting.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_event_vote")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to user who voted
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // Link to the event in which they voted
    @ManyToOne
    @JoinColumn(name = "event_id")
    private VotingEvent event;

    // Optional: record when they voted
    private LocalDateTime votedAt;
}
