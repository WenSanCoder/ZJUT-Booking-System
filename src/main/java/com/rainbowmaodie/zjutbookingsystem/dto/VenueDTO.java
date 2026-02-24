package com.rainbowmaodie.zjutbookingsystem.dto;

import lombok.Data;

@Data
public class VenueDTO {
    private Long id;
    private String name;
    private String type;
    private String location;
    private Integer capacity;
    private String imageUrl;
    private String equipment;
    private Integer status;
}
