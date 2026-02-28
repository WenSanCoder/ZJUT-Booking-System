package com.rainbowmaodie.zjutbookingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rainbowmaodie.zjutbookingsystem.entity.Booking;
import com.rainbowmaodie.zjutbookingsystem.entity.User;
import com.rainbowmaodie.zjutbookingsystem.entity.VenueAdminPermission;
import com.rainbowmaodie.zjutbookingsystem.entity.Building;
import com.rainbowmaodie.zjutbookingsystem.entity.Venue;
import com.rainbowmaodie.zjutbookingsystem.mapper.BookingMapper;
import com.rainbowmaodie.zjutbookingsystem.mapper.VenueAdminPermissionMapper;
import com.rainbowmaodie.zjutbookingsystem.mapper.BuildingMapper;
import com.rainbowmaodie.zjutbookingsystem.service.BookingService;
import com.rainbowmaodie.zjutbookingsystem.service.UserService;
import com.rainbowmaodie.zjutbookingsystem.vo.BookingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    @Autowired
    private UserService userService;

    @Autowired
    private VenueAdminPermissionMapper venueAdminPermissionMapper;

    @Autowired
    private BuildingMapper buildingMapper;

    @Override
    public Page<BookingVO> getBookingPage(Page<Booking> page, String applicant, String status, String startDate, String endDate, Long userId) {
        List<Long> buildingIds = new ArrayList<>();
        List<Long> venueIds = new ArrayList<>();
        
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null && "VENUE_ADMIN".equals(user.getRole())) {
                List<VenueAdminPermission> permissions = venueAdminPermissionMapper.selectList(
                        new LambdaQueryWrapper<VenueAdminPermission>().eq(VenueAdminPermission::getUserId, userId)
                );
                
                if (permissions.isEmpty()) {
                    return new Page<BookingVO>();
                }

                List<String> campuses = new ArrayList<>();
                for (VenueAdminPermission p : permissions) {
                    try {
                        String targetId = p.getTargetId();
                        if ("VENUE".equals(p.getTargetType())) {
                            venueIds.add(Long.valueOf(targetId.replace("VENUE:", "")));
                        } else if ("BUILDING".equals(p.getTargetType())) {
                            buildingIds.add(Long.valueOf(targetId.replace("BUILDING:", "")));
                        } else if ("CAMPUS".equals(p.getTargetType())) {
                            campuses.add(targetId.replace("CAMPUS:", ""));
                        }
                    } catch (NumberFormatException e) {
                        // 忽略格式错误的ID
                    }
                }

                if (!campuses.isEmpty()) {
                    buildingIds.addAll(
                        buildingMapper.selectList(new LambdaQueryWrapper<Building>().in(Building::getLocation, campuses))
                            .stream().map(Building::getId).collect(Collectors.toList())
                    );
                }

                if (venueIds.isEmpty() && buildingIds.isEmpty()) {
                    return new Page<BookingVO>(page.getCurrent(), page.getSize());
                }
            }
        }
        
        List<Long> finalBuildingIds = buildingIds.isEmpty() ? null : buildingIds;
        List<Long> finalVenueIds = venueIds.isEmpty() ? null : venueIds;
        return this.baseMapper.selectBookingVOPage(page, applicant, status, startDate, endDate, finalBuildingIds, finalVenueIds);
    }

    @Override
    public void approve(Long id, Long adminId) {
        Booking booking = this.getById(id);
        if (booking == null) throw new RuntimeException("预约记录不存在");
        
        // 权限校验
        checkPermission(booking.getVenueId(), adminId);
        
        // 冲突校验：检查是否已有同一时段同一场地的 APPROVED 预约
        Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getVenueId, booking.getVenueId())
                .eq(Booking::getStatus, "APPROVED")
                .and(w -> w.lt(Booking::getStartTime, booking.getEndTime())
                          .gt(Booking::getEndTime, booking.getStartTime())));
        if (count > 0) {
            throw new RuntimeException("该时段该场地已有其他已批准的预约，请先取消冲突预约后再试");
        }

        booking.setStatus("APPROVED");
        this.updateById(booking);
        // 这里后续可以添加生成PDF签名凭证的异步任务
    }

    @Override
    public void reject(Long id, String reason, Long adminId) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("驳回申请必须填写理由");
        }
        Booking booking = this.getById(id);
        if (booking == null) throw new RuntimeException("预约记录不存在");
        
        checkPermission(booking.getVenueId(), adminId);
        
        booking.setStatus("REJECTED");
        booking.setRejectReason(reason);
        this.updateById(booking);
    }

    @Autowired
    private com.rainbowmaodie.zjutbookingsystem.mapper.VenueMapper venueMapper;

    private void checkPermission(Long venueId, Long adminId) {
        if (adminId == null) throw new RuntimeException("请先登录");
        User user = userService.getById(adminId);
        if (user == null) throw new RuntimeException("管理员用户不存在");
        if ("SYS_ADMIN".equals(user.getRole())) return; // 系统管理员拥有顶级权限

        if (!"VENUE_ADMIN".equals(user.getRole())) {
            throw new RuntimeException("当前用户无权审批预约");
        }

        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) throw new RuntimeException("场地不存在");

        List<VenueAdminPermission> permissions = venueAdminPermissionMapper.selectList(
                new LambdaQueryWrapper<VenueAdminPermission>().eq(VenueAdminPermission::getUserId, adminId)
        );

        boolean hasPermission = false;
        for (VenueAdminPermission p : permissions) {
            String targetId = p.getTargetId();
            if ("VENUE".equals(p.getTargetType()) && ("VENUE:" + venueId).equals(targetId)) {
                hasPermission = true;
                break;
            }
            if ("BUILDING".equals(p.getTargetType()) && ("BUILDING:" + venue.getBuildingId()).equals(targetId)) {
                hasPermission = true;
                break;
            }
            if ("CAMPUS".equals(p.getTargetType())) {
                Building building = buildingMapper.selectById(venue.getBuildingId());
                if (building != null && ("CAMPUS:" + building.getLocation()).equals(targetId)) {
                    hasPermission = true;
                    break;
                }
            }
        }

        if (!hasPermission) {
            throw new RuntimeException("您没有权限审批该场地的预约");
        }
    }
}
