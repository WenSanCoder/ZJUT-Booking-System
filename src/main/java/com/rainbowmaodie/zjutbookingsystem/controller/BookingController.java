package com.rainbowmaodie.zjutbookingsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rainbowmaodie.zjutbookingsystem.common.Result;
import com.rainbowmaodie.zjutbookingsystem.entity.Booking;
import com.rainbowmaodie.zjutbookingsystem.service.BookingService;
import com.rainbowmaodie.zjutbookingsystem.vo.BookingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/page")
    public Result<Page<BookingVO>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String applicant,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long userId) {
        Page<Booking> page = new Page<>(current, size);
        return Result.success(bookingService.getBookingPage(page, applicant, status, startDate, endDate, userId));
    }

    @PostMapping("/{id}/approve")
    public Result<String> approve(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        try {
            bookingService.approve(id, userId);
            return Result.success("审批通过");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public Result<String> reject(@PathVariable Long id, @RequestBody Map<String, String> data, @RequestParam(required = false) Long userId) {
        try {
            String reason = data.get("reason");
            bookingService.reject(id, reason, userId);
            return Result.success("已驳回");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
