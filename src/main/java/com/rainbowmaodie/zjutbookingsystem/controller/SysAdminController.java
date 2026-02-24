package com.rainbowmaodie.zjutbookingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rainbowmaodie.zjutbookingsystem.common.Result;
import com.rainbowmaodie.zjutbookingsystem.entity.Announcement;
import com.rainbowmaodie.zjutbookingsystem.entity.Building;
import com.rainbowmaodie.zjutbookingsystem.entity.User;
import com.rainbowmaodie.zjutbookingsystem.entity.VenueAdminBuilding;
import com.rainbowmaodie.zjutbookingsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys")
public class SysAdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private VenueAdminBuildingService venueAdminBuildingService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private NotificationService notificationService;

    // --- 用户管理 ---
    @GetMapping("/users/page")
    public Result<Page<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(User::getUsername, keyword).or().like(User::getRealName, keyword);
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
    public Result<String> deleteBuilding(@PathVariable Long id) {
        buildingService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/permissions/{adminId}")
    public Result<List<VenueAdminBuilding>> getAdminPermissions(@PathVariable Long adminId) {
        return Result.success(venueAdminBuildingService.list(
                new LambdaQueryWrapper<VenueAdminBuilding>().eq(VenueAdminBuilding::getUserId, adminId)
        ));
    }

    @PostMapping("/permissions/assign")
    public Result<String> assignPermission(@RequestBody Map<String, Object> data) {
        Long adminId = Long.valueOf(data.get("adminId").toString());
        List<Integer> buildingIds = (List<Integer>) data.get("buildingIds");
        
        // 先删除旧的
        venueAdminBuildingService.remove(new LambdaQueryWrapper<VenueAdminBuilding>().eq(VenueAdminBuilding::getUserId, adminId));
        
        // 插入新的
        for (Integer bId : buildingIds) {
            VenueAdminBuilding vab = new VenueAdminBuilding();
            vab.setUserId(adminId);
            vab.setBuildingId(Long.valueOf(bId));
            venueAdminBuildingService.save(vab);
        }
        return Result.success("分配成功");
    }

    // --- 公告管理 ---
    @PostMapping("/announcements/publish")
    public Result<String> publishAnnouncement(@RequestBody Announcement announcement) {
        announcementService.save(announcement);
        return Result.success("发布成功");
    }
}
