package com.rainbowmaodie.zjutbookingsystem.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private Integer passwordChanged;
    private Integer creditScore;
    private String signatureUrl;
}
