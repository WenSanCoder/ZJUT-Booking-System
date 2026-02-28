package com.rainbowmaodie.zjutbookingsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("venue")
public class Venue {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long buildingId;
    private String type;
    private String address;
    private Integer capacity;
    private String imageUrl;
    private String equipment;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
