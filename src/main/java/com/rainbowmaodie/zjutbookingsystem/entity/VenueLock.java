package com.rainbowmaodie.zjutbookingsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("venue_lock")
public class VenueLock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long venueId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private LocalDateTime createdAt;
}
