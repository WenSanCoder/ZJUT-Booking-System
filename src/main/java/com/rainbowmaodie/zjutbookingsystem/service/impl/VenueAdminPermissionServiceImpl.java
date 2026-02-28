package com.rainbowmaodie.zjutbookingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rainbowmaodie.zjutbookingsystem.entity.Building;
import com.rainbowmaodie.zjutbookingsystem.entity.Venue;
import com.rainbowmaodie.zjutbookingsystem.entity.VenueAdminPermission;
import com.rainbowmaodie.zjutbookingsystem.mapper.BuildingMapper;
import com.rainbowmaodie.zjutbookingsystem.mapper.VenueAdminPermissionMapper;
import com.rainbowmaodie.zjutbookingsystem.mapper.VenueMapper;
import com.rainbowmaodie.zjutbookingsystem.service.VenueAdminPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueAdminPermissionServiceImpl extends ServiceImpl<VenueAdminPermissionMapper, VenueAdminPermission> implements VenueAdminPermissionService {

    @Autowired
    private VenueMapper venueMapper;

    @Autowired
    private BuildingMapper buildingMapper;

    @Override
    public boolean hasPermission(Long userId, Long venueId) {
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) return false;

        Building building = buildingMapper.selectById(venue.getBuildingId());
        
        List<VenueAdminPermission> permissions = this.list(new LambdaQueryWrapper<VenueAdminPermission>()
                .eq(VenueAdminPermission::getUserId, userId));
        
        for (VenueAdminPermission p : permissions) {
            if ("VENUE".equals(p.getTargetType()) && p.getTargetId().equals(String.valueOf(venueId))) {
                return true;
            }
            if ("BUILDING".equals(p.getTargetType()) && p.getTargetId().equals(String.valueOf(venue.getBuildingId()))) {
                return true;
            }
            if ("CAMPUS".equals(p.getTargetType()) && building != null && p.getTargetId().equals(building.getLocation())) {
                return true;
            }
        }
        return false;
    }
}
