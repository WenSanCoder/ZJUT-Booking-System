package com.rainbowmaodie.zjutbookingsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rainbowmaodie.zjutbookingsystem.entity.Booking;
import com.rainbowmaodie.zjutbookingsystem.vo.BookingVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface BookingService extends IService<Booking> {
    Page<BookingVO> getBookingPage(Page<Booking> page, String applicant, String status, String startDate, String endDate, Long userId);
    void approve(Long id, Long adminId);
    void reject(Long id, String reason, Long adminId);
}
