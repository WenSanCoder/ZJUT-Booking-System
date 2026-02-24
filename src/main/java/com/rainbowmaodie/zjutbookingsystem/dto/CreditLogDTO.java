package com.rainbowmaodie.zjutbookingsystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreditLogDTO {
    private Long id;
    private Long userId;
    private Integer changeValue;
    private String reason;
    private LocalDateTime createdAt;
}
