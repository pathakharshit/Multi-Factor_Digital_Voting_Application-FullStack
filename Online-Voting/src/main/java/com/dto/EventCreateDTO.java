package com.example.Online.Voting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventCreateDTO {
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") // matches <input type="datetime-local">
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime endTime;
    private String description;
}