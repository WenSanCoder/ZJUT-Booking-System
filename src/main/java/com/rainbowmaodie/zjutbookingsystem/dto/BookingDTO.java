package com.rainbowmaodie.zjutbookingsystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Long id;
    private Long userId;
    private Long venueId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}
