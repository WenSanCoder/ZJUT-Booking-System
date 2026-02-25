package com.rainbowmaodie.zjutbookingsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("venue_admin_permission")
public class VenueAdminPermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String targetType; // CAMPUS, BUILDING, VENUE
    private String targetId;
    private LocalDateTime createdAt;
}
