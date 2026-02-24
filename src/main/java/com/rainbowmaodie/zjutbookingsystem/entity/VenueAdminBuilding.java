package com.rainbowmaodie.zjutbookingsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("venue_admin_building")
public class VenueAdminBuilding {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long buildingId;
}
