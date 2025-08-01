package com.example.Online.Voting.service;

import com.example.Online.Voting.dto.PartyDTO;
import com.example.Online.Voting.model.Party;
import com.example.Online.Voting.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

// AdminPartyService.java
@Service
@RequiredArgsConstructor
public class AdminPartyService {
    private final PartyRepository partyRepository;

    public Party createParty(String name, String description, byte[] partySymbol) throws Exception {
        // 1) Check if party name already exists
        if (partyRepository.existsByPartyName(name)) {
            throw new Exception("Party name already taken: " + name);
        }
        Party party = Party.builder()
                .partyName(name)
                .description(description)
                .partySymbol(partySymbol)
                .build();
        return partyRepository.save(party);
    }

    public List<PartyDTO> getAllParties() {
        List<Party> parties = partyRepository.findAll();
        return parties.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PartyDTO convertToDTO(Party party) {
        PartyDTO dto = new PartyDTO();
        dto.setId(party.getId());
        dto.setPartyName(party.getPartyName());
        dto.setDescription(party.getDescription());

        // Convert partySymbol (byte[]) to Base64 if not null
        if (party.getPartySymbol() != null && party.getPartySymbol().length > 0) {
            String base64 = Base64.getEncoder().encodeToString(party.getPartySymbol());
            // prepend data URI header (assuming PNG, adjust if JPEG, etc.)
            dto.setSymbolBase64("data:image/png;base64," + base64);
        }
        return dto;
    }

    // etc. for updating or deleting parties
}
