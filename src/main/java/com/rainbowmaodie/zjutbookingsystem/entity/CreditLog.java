package com.rainbowmaodie.zjutbookingsystem.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("credit_log")
public class CreditLog {
    private Long id;
    private Long userId;
    private Integer changeValue;
    private String reason;
    private LocalDateTime createdAt;
}
