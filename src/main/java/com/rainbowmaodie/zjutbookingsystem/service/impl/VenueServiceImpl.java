package com.rainbowmaodie.zjutbookingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rainbowmaodie.zjutbookingsystem.entity.Booking;
import com.rainbowmaodie.zjutbookingsystem.entity.Building;
import com.rainbowmaodie.zjutbookingsystem.entity.User;
import com.rainbowmaodie.zjutbookingsystem.entity.Venue;
import com.rainbowmaodie.zjutbookingsystem.entity.VenueAdminPermission;
import com.rainbowmaodie.zjutbookingsystem.entity.VenueLock;
import com.rainbowmaodie.zjutbookingsystem.mapper.BuildingMapper;
import com.rainbowmaodie.zjutbookingsystem.mapper.VenueAdminPermissionMapper;
import com.rainbowmaodie.zjutbookingsystem.mapper.VenueMapper;
import com.rainbowmaodie.zjutbookingsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private VenueAdminPermissionMapper venueAdminPermissionMapper;

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private VenueLockService venueLockService;

    @Override
    public Page<Venue> getVenuePage(Page<Venue> page, String name, Long buildingId, Long adminId) {
        LambdaQueryWrapper<Venue> queryWrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(Venue::getName, name);
        }
        if (buildingId != null) {
            queryWrapper.eq(Venue::getBuildingId, buildingId);
        }

        User user = userService.getById(adminId);
        if (user != null && "VENUE_ADMIN".equals(user.getRole())) {
            // 查询权限
            List<VenueAdminPermission> permissions = venueAdminPermissionMapper.selectList(
                    new LambdaQueryWrapper<VenueAdminPermission>().eq(VenueAdminPermission::getUserId, adminId)
            );

            if (permissions.isEmpty()) return new Page<Venue>(page.getCurrent(), page.getSize());

            List<Long> venueIds = new ArrayList<>();
            List<Long> buildingIds = new ArrayList<>();
            List<String> campuses = new ArrayList<>();

            for (VenueAdminPermission p : permissions) {
                if ("VENUE".equals(p.getTargetType())) {
                    try { 
                        venueIds.add(Long.valueOf(p.getTargetId().replace("VENUE:", ""))); 
                    } catch (Exception e) {}
                } else if ("BUILDING".equals(p.getTargetType())) {
                    try { 
                        buildingIds.add(Long.valueOf(p.getTargetId().replace("BUILDING:", ""))); 
                    } catch (Exception e) {}
                } else if ("CAMPUS".equals(p.getTargetType())) {
                    campuses.add(p.getTargetId().replace("CAMPUS:", ""));
                }
            }

            // 校区权限转换为楼宇权限
            if (!campuses.isEmpty()) {
                buildingIds.addAll(
                    buildingMapper.selectList(new LambdaQueryWrapper<Building>().in(Building::getLocation, campuses))
                        .stream().map(Building::getId).collect(Collectors.toList())
                );
            }

            // 合并查询
            if (venueIds.isEmpty() && buildingIds.isEmpty()) {
                return new Page<Venue>(page.getCurrent(), page.getSize());
            }

            queryWrapper.and(w -> {
                boolean hasCondition = false;
                if (!venueIds.isEmpty()) {
                    w.in(Venue::getId, venueIds);
                    hasCondition = true;
                }
                if (!buildingIds.isEmpty()) {
                    if (hasCondition) w.or().in(Venue::getBuildingId, buildingIds);
                    else w.in(Venue::getBuildingId, buildingIds);
                }
            });
        } else if (user != null && "STUDENT".equals(user.getRole())) {
            // 如果是普通学生/教师，通常不应该访问 admin 接口，或者根据业务逻辑处理
            // 这里为了安全，如果是 admin 接口但角色不对，返回空
            return new Page<Venue>(page.getCurrent(), page.getSize());
        }

        queryWrapper.orderByDesc(Venue::getCreatedAt);
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public void lockVenue(Long venueId, LocalDateTime startTime, LocalDateTime endTime, String reason) {
        Venue venue = this.getById(venueId);
        if (venue == null) throw new RuntimeException("场地不存在");

        // 保存锁定记录
        VenueLock venueLock = new VenueLock();
        venueLock.setVenueId(venueId);
        venueLock.setStartTime(startTime);
        venueLock.setEndTime(endTime);
        venueLock.setReason(reason);
        venueLockService.save(venueLock);

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
