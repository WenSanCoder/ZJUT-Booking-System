package com.rainbowmaodie.zjutbookingsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rainbowmaodie.zjutbookingsystem.entity.VenueAdminPermission;

public interface VenueAdminPermissionService extends IService<VenueAdminPermission> {
    boolean hasPermission(Long userId, Long venueId);
}
