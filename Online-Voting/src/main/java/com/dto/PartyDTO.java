package com.example.Online.Voting.dto;

import lombok.Data;

@Data
public class PartyDTO {
    private Long id;
    private String partyName;
    private String description;
    private String symbolBase64; // holds the base64 version of partySymbol
}
