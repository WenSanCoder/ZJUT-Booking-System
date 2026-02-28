package com.rainbowmaodie.zjutbookingsystem.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("booking")
public class Booking {
    private Long id;
    private Long userId;
    private Long venueId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String remark;
    private String planUrl;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String activityName;
    private String organizer;
    private Integer expectedPeople;
    private String contactName;
    private String contactPhone;
    private String description;
    private String attachment;
}
