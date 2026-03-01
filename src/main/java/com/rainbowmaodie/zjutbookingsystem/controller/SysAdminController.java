package com.rainbowmaodie.zjutbookingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rainbowmaodie.zjutbookingsystem.common.Result;
import com.rainbowmaodie.zjutbookingsystem.entity.*;
import com.rainbowmaodie.zjutbookingsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sys")
@CrossOrigin
public class SysAdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private VenueService venueService;

    @Autowired
    private VenueAdminPermissionService venueAdminPermissionService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private VenueLockService venueLockService;

    @Autowired
    private NotificationService notificationService;

    // --- 用户管理 ---
    @GetMapping("/users/page")
    public Result<Page<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getRealName, keyword));
        }
        if (role != null && !role.isEmpty()) {
            queryWrapper.eq(User::getRole, role);
        }
        return Result.success(userService.page(new Page<>(current, size), queryWrapper));
    }

    @PostMapping("/users/save")
    public Result<String> saveUser(@RequestBody User user) {
        userService.saveOrUpdate(user);
        return Result.success("保存成功");
    }

    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/users/notify")
    public Result<String> notifyUser(@RequestBody Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String title = (String) data.get("title");
        String content = (String) data.get("content");
        notificationService.sendNotification(userId, title, content, "SYSTEM");
        return Result.success("通知已发送");
    }

    // --- 楼宇管辖管理 ---
    @GetMapping("/buildings/list")
    public Result<List<Building>> getBuildingList() {
        return Result.success(buildingService.list());
    }

    @PostMapping("/buildings/save")
    public Result<String> saveBuilding(@RequestBody Building building) {
        buildingService.saveOrUpdate(building);
        return Result.success("保存成功");
    }

    @DeleteMapping("/buildings/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteBuilding(@PathVariable Long id) {
        // 获取该楼宇下的所有场地/教室
        List<Venue> venues = venueService.list(new LambdaQueryWrapper<Venue>().eq(Venue::getBuildingId, id));
        if (venues != null && !venues.isEmpty()) {
            List<Long> venueIds = venues.stream().map(Venue::getId).collect(Collectors.toList());
            
            // 删除场地对应的锁
            venueLockService.remove(new LambdaQueryWrapper<VenueLock>().in(VenueLock::getVenueId, venueIds));
            // 删除场地对应的预约
            bookingService.remove(new LambdaQueryWrapper<Booking>().in(Booking::getVenueId, venueIds));
            
            // 删除场地的权限
            for (Long vid : venueIds) {
                venueAdminPermissionService.remove(new LambdaQueryWrapper<VenueAdminPermission>()
                        .eq(VenueAdminPermission::getTargetType, "VENUE")
                        .eq(VenueAdminPermission::getTargetId, "VENUE:" + vid));
            }
            
            // 删除场地/教室
            venueService.removeBatchByIds(venueIds);
        }
        
        // 删除关联的楼宇级管理员权限
        venueAdminPermissionService.remove(new LambdaQueryWrapper<VenueAdminPermission>()
                .eq(VenueAdminPermission::getTargetType, "BUILDING")
                .eq(VenueAdminPermission::getTargetId, "BUILDING:" + id));
                
        // 最后删除楼宇
        buildingService.removeById(id);
        return Result.success("删除成功");
    }

    // --- 权限管理 ---
    @GetMapping("/permissions/{adminId}")
    public Result<List<VenueAdminPermission>> getAdminPermissions(@PathVariable Long adminId) {
        return Result.success(venueAdminPermissionService.list(
                new LambdaQueryWrapper<VenueAdminPermission>().eq(VenueAdminPermission::getUserId, adminId)
        ));
    }

    @PostMapping("/permissions/assign")
    public Result<String> assignPermission(@RequestBody Map<String, Object> data) {
        Long adminId = Long.valueOf(data.get("adminId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, String>> permissions = (List<Map<String, String>>) data.get("permissions");
        
        // 先删除该管理员的所有旧权限
        venueAdminPermissionService.remove(new LambdaQueryWrapper<VenueAdminPermission>()
                .eq(VenueAdminPermission::getUserId, adminId));
        
        // 批量插入新的层级化权限 (校区/楼宇/场地)
        if (permissions != null && !permissions.isEmpty()) {
            for (Map<String, String> p : permissions) {
                VenueAdminPermission vap = new VenueAdminPermission();
                vap.setUserId(adminId);
                vap.setTargetType(p.get("targetType"));
                vap.setTargetId(p.get("targetId"));
                venueAdminPermissionService.save(vap);
            }
        }
        return Result.success("分配成功");
    }

    @PostMapping("/permissions/add")
    public Result<String> addPermission(@RequestBody Map<String, Object> data) {
        Long adminId = Long.valueOf(data.get("adminId").toString());
        String targetType = (String) data.get("targetType");
        String targetId = (String) data.get("targetId");

        VenueAdminPermission vap = new VenueAdminPermission();
        vap.setUserId(adminId);
        vap.setTargetType(targetType);
        vap.setTargetId(targetId);
        venueAdminPermissionService.save(vap);
        
        return Result.success("授权成功");
    }
}
