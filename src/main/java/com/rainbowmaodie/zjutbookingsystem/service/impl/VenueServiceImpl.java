package com.rainbowmaodie.zjutbookingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rainbowmaodie.zjutbookingsystem.entity.Booking;
import com.rainbowmaodie.zjutbookingsystem.entity.User;
import com.rainbowmaodie.zjutbookingsystem.entity.Venue;
import com.rainbowmaodie.zjutbookingsystem.entity.VenueAdminBuilding;
import com.rainbowmaodie.zjutbookingsystem.mapper.VenueAdminBuildingMapper;
import com.rainbowmaodie.zjutbookingsystem.mapper.VenueMapper;
import com.rainbowmaodie.zjutbookingsystem.service.BookingService;
import com.rainbowmaodie.zjutbookingsystem.service.NotificationService;
import com.rainbowmaodie.zjutbookingsystem.service.UserService;
import com.rainbowmaodie.zjutbookingsystem.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VenueServiceImpl extends ServiceImpl<VenueMapper, Venue> implements VenueService {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VenueAdminBuildingMapper venueAdminBuildingMapper;

    @Override
    public Page<Venue> getVenuePage(Page<Venue> page, String name, Long adminId) {
        LambdaQueryWrapper<Venue> queryWrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(Venue::getName, name);
        }

        User user = userService.getById(adminId);
        if (user != null && "VENUE_ADMIN".equals(user.getRole())) {
            // 只查询管辖范围内的楼宇
            List<Long> buildingIds = venueAdminBuildingMapper.selectList(
                    new LambdaQueryWrapper<VenueAdminBuilding>().eq(VenueAdminBuilding::getUserId, adminId)
            ).stream().map(VenueAdminBuilding::getBuildingId).collect(Collectors.toList());
            
            if (buildingIds.isEmpty()) {
                // 如果没有分配楼宇，返回空分页
                return page;
            }
            queryWrapper.in(Venue::getBuildingId, buildingIds);
        }

        queryWrapper.orderByDesc(Venue::getCreatedAt);
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public void lockVenue(Long venueId, LocalDateTime startTime, LocalDateTime endTime, String reason) {
        Venue venue = this.getById(venueId);
        if (venue == null) throw new RuntimeException("场地不存在");

        // 查找该时间段内所有受影响的活跃预约
        List<Booking> affectedBookings = bookingService.list(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getVenueId, venueId)
                .in(Booking::getStatus, "PENDING", "APPROVED")
                .and(w -> w.lt(Booking::getStartTime, endTime).gt(Booking::getEndTime, startTime))
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String timeRangeStr = startTime.format(formatter) + " 至 " + endTime.format(formatter);

        // 取消预约并异步通知
        for (Booking booking : affectedBookings) {
            booking.setStatus("CANCELLED");
            booking.setRejectReason("管理员设置场地锁定: " + reason);
            bookingService.updateById(booking);

            User user = userService.getById(booking.getUserId());
            if (user != null) {
                notificationService.sendBookingCancellation(
                        user.getId(), user.getUsername(), venue.getName(), timeRangeStr, reason);
            }
        }
    }
}
