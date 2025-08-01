package com.example.Online.Voting.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "party")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Party {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String partyName;
    private String description;

    // Photo of the party symbol, stored as a BLOB.
    // Alternatively, you can store a file path or a URL if you prefer.
    @Lob
    private byte[] partySymbol;
}
