package com.example.Online.Voting.controller;

import com.example.Online.Voting.dto.EventCreateDTO;
import com.example.Online.Voting.model.Candidate;
import com.example.Online.Voting.enums.EventStatus;
import com.example.Online.Voting.model.VotingEvent;
import com.example.Online.Voting.service.AdminEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventService adminEventService;

    /**
     * POST /api/admin/events
     * Create a new voting event (UPCOMING by default).
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventCreateDTO dto) {
        try {
            VotingEvent event = adminEventService.createEvent(dto);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/admin/events/{eventId}/candidates
     * Add a candidate to a given event (only if event is UPCOMING).
     */
    @PostMapping("/{eventId}/candidates")
    public ResponseEntity<?> addCandidate(@PathVariable Long eventId,
                                          @RequestParam String candidateName,
                                          @RequestParam(required=false) String description,
                                          @RequestParam(required=false) Long partyId) {
        try {
            Candidate candidate = adminEventService.addCandidate(eventId, candidateName, description,partyId);
            return ResponseEntity.ok(candidate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PATCH /api/admin/events/{eventId}/status
     * Manually update the status of an event (e.g., UPCOMING -> ONGOING -> ENDED).
     */
    @PatchMapping("/{eventId}/status")
    public ResponseEntity<?> updateEventStatus(@PathVariable Long eventId,
                                               @RequestParam EventStatus newStatus) {
        try {
            VotingEvent updated = adminEventService.updateEventStatus(eventId, newStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/admin/events
     * List all events.
     */
    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        return ResponseEntity.ok(adminEventService.getAllEvents());
    }

    /**
     * GET /api/admin/events/upcoming
     * List upcoming events (status=UPCOMING).
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingEvents() {
        return ResponseEntity.ok(adminEventService.getUpcomingEvents());
    }

    /**
     * GET /api/admin/events/ongoing
     * List ongoing events (status=ONGOING).
     */
    @GetMapping("/ongoing")
    public ResponseEntity<?> getOngoingEvents() {
        return ResponseEntity.ok(adminEventService.getOngoingEvents());
    }

    /**
     * GET /api/admin/events/ended
     * List ended events (status=ENDED).
     */
    @GetMapping("/ended")
    public ResponseEntity<?> getEndedEvents() {
        return ResponseEntity.ok(adminEventService.getEndedEvents());
    }
}

