package com.rainbowmaodie.zjutbookingsystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rainbowmaodie.zjutbookingsystem.entity.Venue;

import java.time.LocalDateTime;

public interface VenueService extends IService<Venue> {
    void lockVenue(Long venueId, LocalDateTime startTime, LocalDateTime endTime, String reason);

    Page<Venue> getVenuePage(Page<Venue> page, String name, Long buildingId, Long adminId);
}
