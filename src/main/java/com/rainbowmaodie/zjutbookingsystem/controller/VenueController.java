package com.rainbowmaodie.zjutbookingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rainbowmaodie.zjutbookingsystem.common.Result;
import com.rainbowmaodie.zjutbookingsystem.entity.Building;
import com.rainbowmaodie.zjutbookingsystem.entity.User;
import com.rainbowmaodie.zjutbookingsystem.entity.Venue;
import com.rainbowmaodie.zjutbookingsystem.entity.VenueAdminPermission;
import com.rainbowmaodie.zjutbookingsystem.entity.VenueLock;
import com.rainbowmaodie.zjutbookingsystem.entity.Booking;
import com.rainbowmaodie.zjutbookingsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/venue")
@CrossOrigin
public class VenueController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private UserService userService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private VenueAdminPermissionService venueAdminPermissionService;

    @Autowired
    private VenueLockService venueLockService;
    
    @Autowired
    private BookingService bookingService;

    @GetMapping("/page")
    public Result<Page<Venue>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long userId) {
        Page<Venue> page = new Page<>(current, size);
        return Result.success(venueService.getVenuePage(page, name, buildingId, userId));
    }

    @GetMapping("/buildings")
    public Result<List<Building>> getAuthorizedBuildings(@RequestParam Long userId) {
        User admin = userService.getById(userId);
        if (admin == null) return Result.error("用户不存在");
        
        List<Building> allBuildings = buildingService.list();
        if ("SYS_ADMIN".equals(admin.getRole())) {
            return Result.success(allBuildings);
        }
        
        List<VenueAdminPermission> permissions = venueAdminPermissionService.list(
                new LambdaQueryWrapper<VenueAdminPermission>().eq(VenueAdminPermission::getUserId, userId)
        );
        
        if (permissions.isEmpty()) return Result.success(new ArrayList<>());
        
        List<String> campuses = new ArrayList<>();
        Set<Long> buildingIds = new HashSet<>();
        
        for (VenueAdminPermission p : permissions) {
            String targetIdStr = p.getTargetId();
            if ("CAMPUS".equals(p.getTargetType())) {
                campuses.add(targetIdStr.replace("CAMPUS:", ""));
            }
            if ("BUILDING".equals(p.getTargetType())) {
                try {
                    buildingIds.add(Long.valueOf(targetIdStr.replace("BUILDING:", "")));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        
        return Result.success(allBuildings.stream()
                .filter(b -> campuses.contains(b.getLocation()) || buildingIds.contains(b.getId()))
                .collect(Collectors.toList()));
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody Venue venue, @RequestParam(required = false) Long userId) {
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null && "VENUE_ADMIN".equals(user.getRole())) {
                boolean hasPermission = false;
                Building building = buildingService.getById(venue.getBuildingId());
                if (building == null) return Result.error("所属楼宇不存在");

                List<VenueAdminPermission> permissions = venueAdminPermissionService.list(
                        new LambdaQueryWrapper<VenueAdminPermission>().eq(VenueAdminPermission::getUserId, userId)
                );

                for (VenueAdminPermission p : permissions) {
                    String targetId = p.getTargetId();
                    if ("CAMPUS".equals(p.getTargetType()) && targetId.replace("CAMPUS:", "").equals(building.getLocation())) {
                        hasPermission = true;
                        break;
                    }
                    if ("BUILDING".equals(p.getTargetType()) && targetId.replace("BUILDING:", "").equals(String.valueOf(building.getId()))) {
                        hasPermission = true;
                        break;
                    }
                    if ("VENUE".equals(p.getTargetType()) && venue.getId() != null && targetId.replace("VENUE:", "").equals(String.valueOf(venue.getId()))) {
                        hasPermission = true;
                        break;
                    }
                }
                if (!hasPermission) return Result.error("无权管理该区域的场地");
            }
        }
        venueService.saveOrUpdate(venue);
        return Result.success("保存成功");
    }

    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> delete(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null && "VENUE_ADMIN".equals(user.getRole())) {
                Venue venue = venueService.getById(id);
                if (venue == null) return Result.success("删除成功");
                
                Building building = buildingService.getById(venue.getBuildingId());
                List<VenueAdminPermission> permissions = venueAdminPermissionService.list(
                        new LambdaQueryWrapper<VenueAdminPermission>().eq(VenueAdminPermission::getUserId, userId)
                );

                boolean hasPermission = false;
                for (VenueAdminPermission p : permissions) {
                    String targetId = p.getTargetId();
                    if ("CAMPUS".equals(p.getTargetType()) && building != null && targetId.replace("CAMPUS:", "").equals(building.getLocation())) {
                        hasPermission = true;
                        break;
                    }
                    if ("BUILDING".equals(p.getTargetType()) && targetId.replace("BUILDING:", "").equals(String.valueOf(venue.getBuildingId()))) {
                        hasPermission = true;
                        break;
                    }
                    if ("VENUE".equals(p.getTargetType()) && targetId.replace("VENUE:", "").equals(String.valueOf(id))) {
                        hasPermission = true;
                        break;
                    }
                }
                if (!hasPermission) return Result.error("无权删除该场地");
            }
        }
        
        // 删除场地对应的锁
        venueLockService.remove(new LambdaQueryWrapper<VenueLock>().eq(VenueLock::getVenueId, id));
        // 删除场地对应的预约
        bookingService.remove(new LambdaQueryWrapper<Booking>().eq(Booking::getVenueId, id));
        // 删除关联的管理员权限
        venueAdminPermissionService.remove(new LambdaQueryWrapper<VenueAdminPermission>()
                .eq(VenueAdminPermission::getTargetType, "VENUE")
                .eq(VenueAdminPermission::getTargetId, "VENUE:" + id));
                
        venueService.removeById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/status")
    public Result<String> updateStatus(@RequestBody Map<String, Object> data, @RequestParam(required = false) Long userId) {
        Long id = Long.valueOf(data.get("id").toString());
        
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null && "VENUE_ADMIN".equals(user.getRole())) {
                Venue venue = venueService.getById(id);
                if (venue == null) return Result.error("场地不存在");
                
                Building building = buildingService.getById(venue.getBuildingId());
                List<VenueAdminPermission> permissions = venueAdminPermissionService.list(
                        new LambdaQueryWrapper<VenueAdminPermission>().eq(VenueAdminPermission::getUserId, userId)
                );

                boolean hasPermission = false;
                for (VenueAdminPermission p : permissions) {
                    String targetId = p.getTargetId();
                    if ("CAMPUS".equals(p.getTargetType()) && building != null && targetId.replace("CAMPUS:", "").equals(building.getLocation())) {
                        hasPermission = true;
                        break;
                    }
                    if ("BUILDING".equals(p.getTargetType()) && targetId.replace("BUILDING:", "").equals(String.valueOf(venue.getBuildingId()))) {
                        hasPermission = true;
                        break;
                    }
                    if ("VENUE".equals(p.getTargetType()) && targetId.replace("VENUE:", "").equals(String.valueOf(id))) {
                        hasPermission = true;
                        break;
                    }
                }
                if (!hasPermission) return Result.error("无权操作该场地");
            }
        }

        if (data.containsKey("status")) {
            Venue venue = venueService.getById(id);
            venue.setStatus((Integer) data.get("status"));
            venueService.updateById(venue);
            return Result.success("状态更新成功");
        } else if (data.containsKey("startTime") && data.containsKey("endTime")) {
            LocalDateTime start = LocalDateTime.parse(data.get("startTime").toString());
            LocalDateTime end = LocalDateTime.parse(data.get("endTime").toString());
            String reason = (String) data.get("reason");
            venueService.lockVenue(id, start, end, reason);
            return Result.success("分时锁定成功");
        }
        return Result.error("参数错误");
    }

    @GetMapping("/{id}/locks")
    public Result<List<VenueLock>> getLocks(@PathVariable Long id) {
        return Result.success(venueLockService.list(
                new LambdaQueryWrapper<VenueLock>()
                        .eq(VenueLock::getVenueId, id)
                        .gt(VenueLock::getEndTime, LocalDateTime.now())
                        .orderByAsc(VenueLock::getStartTime)
        ));
    }

    @DeleteMapping("/locks/{lockId}")
    public Result<String> deleteLock(@PathVariable Long lockId) {
        venueLockService.removeById(lockId);
        return Result.success("解除锁定成功");
    }
}
