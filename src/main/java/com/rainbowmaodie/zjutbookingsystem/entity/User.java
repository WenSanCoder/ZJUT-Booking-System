package com.rainbowmaodie.zjutbookingsystem.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    private Long id;
    private String username;
    private String realName;
    private String password;
    private String role;
    private String phone;
    private String email;
    private Integer status;
    private Integer passwordChanged;
    private Integer creditScore;
    private String signatureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
