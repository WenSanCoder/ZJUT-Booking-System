package com.rainbowmaodie.zjutbookingsystem.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingVO {
    private Long id;
    private Long userId;
    private String applicantName;
    private String applicantUsername;
    private String phone;
    private Long venueId;
    private String venueName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String remark;
    private String planUrl;
    private String rejectReason;
    private LocalDateTime createdAt;
    
    // 活动详情 
    private String activityName;
    private String organizer;
    private Integer expectedPeople;
    private String contactName;
    private String contactPhone;
    private String description;
    private String attachment;
}
