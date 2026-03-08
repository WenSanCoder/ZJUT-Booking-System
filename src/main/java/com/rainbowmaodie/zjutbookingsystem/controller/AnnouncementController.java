package com.rainbowmaodie.zjutbookingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rainbowmaodie.zjutbookingsystem.common.Result;
import com.rainbowmaodie.zjutbookingsystem.entity.Announcement;
import com.rainbowmaodie.zjutbookingsystem.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcement")
@CrossOrigin
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Announcement API is working");
    }

    @GetMapping("/latest")
    public Result<List<Announcement>> getLatestAnnouncements(@RequestParam(defaultValue = "5") Integer limit) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Announcement::getCreatedAt).last("LIMIT " + limit);
        return Result.success(announcementService.list(queryWrapper));
    }

    @GetMapping("/page")
    public Result<Page<Announcement>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            queryWrapper.like(Announcement::getTitle, title);
        }
        
        queryWrapper.orderByDesc(Announcement::getId);
        
        return Result.success(announcementService.page(new Page<>(current, size), queryWrapper));
    }

    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }

    @PostMapping("/publish")
    public Result<String> publish(@RequestBody Announcement announcement) {
        if (announcement.getTitle() == null || announcement.getContent() == null) {
            return Result.error("标题和内容不能为空");
        }
        announcement.setCreatedAt(java.time.LocalDateTime.now());
        announcement.setUpdatedAt(java.time.LocalDateTime.now());
        if (announcement.getPublishedBy() == null) {
            announcement.setPublishedBy(1L);
        }
        announcementService.save(announcement);
        return Result.success("发布成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody Announcement announcement) {
        announcement.setUpdatedAt(java.time.LocalDateTime.now());
        announcementService.updateById(announcement);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success("删除成功");
    }
}
