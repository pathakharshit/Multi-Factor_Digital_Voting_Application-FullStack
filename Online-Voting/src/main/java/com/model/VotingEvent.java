package com.example.Online.Voting.model;

import com.example.Online.Voting.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voting_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VotingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private EventStatus status;  // e.g. UPCOMING, ONGOING, ENDED

    private String description;  // optional
}

