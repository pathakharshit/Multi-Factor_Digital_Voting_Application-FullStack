package com.example.Online.Voting.controller;

import com.example.Online.Voting.dto.PartyDTO;
import com.example.Online.Voting.model.Party;
import com.example.Online.Voting.service.AdminPartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// AdminPartyController.java
@RestController
@RequestMapping("/api/admin/parties")
@RequiredArgsConstructor
public class AdminPartyController {

    private final AdminPartyService adminPartyService;

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createParty(@RequestParam String partyName,
                                         @RequestParam(required=false) String description,
                                         @RequestParam(required=false) MultipartFile partySymbolFile) {
        try {
            byte[] symbolBytes = null;
            if (partySymbolFile != null && !partySymbolFile.isEmpty()) {
                symbolBytes = partySymbolFile.getBytes();
            }
            Party newParty = adminPartyService.createParty(partyName, description, symbolBytes);
            return ResponseEntity.ok(newParty);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PartyDTO> getAllParties() {
        return adminPartyService.getAllParties();
    }
}

