package com.example.Online.Voting.service;

import com.example.Online.Voting.dto.EventCreateDTO;
import com.example.Online.Voting.model.Candidate;
import com.example.Online.Voting.enums.EventStatus;
import com.example.Online.Voting.model.Party;
import com.example.Online.Voting.model.VotingEvent;
import com.example.Online.Voting.repository.CandidateRepository;
import com.example.Online.Voting.repository.PartyRepository;
import com.example.Online.Voting.repository.VotingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final VotingEventRepository eventRepository;
    private final CandidateRepository candidateRepository;
    private final PartyRepository partyRepository;

    public VotingEvent createEvent(EventCreateDTO dto) {
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time must not be null.");
        }
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time cannot be after end time.");
        }

        VotingEvent event = VotingEvent.builder()
                .name(dto.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(EventStatus.UPCOMING)
                .description(dto.getDescription())
                .build();

        return eventRepository.save(event);
    }

    public Candidate addCandidate(Long eventId, String candidateName, String description,Long partyId) throws Exception {
        VotingEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new Exception("Event not found with ID: " + eventId));

        if (event.getStatus() != EventStatus.UPCOMING) {
            throw new IllegalStateException("Cannot add candidate to an event that is not UPCOMING.");
        }

        Party party = null;
        if (partyId != null) {
            party = partyRepository.findById(partyId)
                    .orElseThrow(() -> new Exception("Party not found with ID: " + partyId));
        }

        Candidate candidate = Candidate.builder()
                .candidateName(candidateName)
                .description(description)
                .voteCount(0)
                .votingEvent(event)
                .party(party)
                .build();

        return candidateRepository.save(candidate);
    }

    public VotingEvent updateEventStatus(Long eventId, EventStatus newStatus) throws Exception {
        VotingEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new Exception("Event not found with ID: " + eventId));
        event.setStatus(newStatus);
        return eventRepository.save(event);
    }

    public List<VotingEvent> getAllEvents() {
        return eventRepository.findAll();
    }

    // NEW: fetch upcoming events (status = UPCOMING)
    public List<VotingEvent> getUpcomingEvents() {
        return eventRepository.findByStatus(EventStatus.UPCOMING);
    }

    // NEW: fetch ongoing events (status = ONGOING)
    public List<VotingEvent> getOngoingEvents() {
        return eventRepository.findByStatus(EventStatus.ONGOING);
    }

    // NEW: fetch ended events (status = ENDED)
    public List<VotingEvent> getEndedEvents() {
        return eventRepository.findByStatus(EventStatus.ENDED);
    }

    @Scheduled(fixedRate = 60000)
    public void autoUpdateEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<VotingEvent> allEvents = eventRepository.findAll();

        for (VotingEvent event : allEvents) {
            if (event.getStatus() == EventStatus.UPCOMING && !event.getStartTime().isAfter(now)) {
                event.setStatus(EventStatus.ONGOING);
                eventRepository.save(event);

            } else if (event.getStatus() == EventStatus.ONGOING && !event.getEndTime().isAfter(now)) {
                event.setStatus(EventStatus.ENDED);
                eventRepository.save(event);
            }
        }
    }
}

