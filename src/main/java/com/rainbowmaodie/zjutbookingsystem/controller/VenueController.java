package com.rainbowmaodie.zjutbookingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rainbowmaodie.zjutbookingsystem.common.Result;
import com.rainbowmaodie.zjutbookingsystem.entity.Venue;
import com.rainbowmaodie.zjutbookingsystem.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/venue")
public class VenueController {

    @Autowired
    private VenueService venueService;

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

    @PostMapping("/save")
    public Result<String> save(@RequestBody Venue venue) {
        venueService.saveOrUpdate(venue);
        return Result.success("保存成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        venueService.removeById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/status")
    public Result<String> updateStatus(@RequestBody Map<String, Object> data) {
        Long id = Long.valueOf(data.get("id").toString());
        if (data.containsKey("status")) {
            // 普通状态切换
            Venue venue = venueService.getById(id);
            venue.setStatus((Integer) data.get("status"));
            venueService.updateById(venue);
            return Result.success("状态更新成功");
        } else if (data.containsKey("startTime") && data.containsKey("endTime")) {
            // 分时锁定 logic
            LocalDateTime start = LocalDateTime.parse(data.get("startTime").toString());
            LocalDateTime end = LocalDateTime.parse(data.get("endTime").toString());
            String reason = (String) data.get("reason");
            venueService.lockVenue(id, start, end, reason);
            return Result.success("分时锁定成功，受影响的预约已取消并通知用户");
        }
        return Result.error("参数错误");
    }
}
