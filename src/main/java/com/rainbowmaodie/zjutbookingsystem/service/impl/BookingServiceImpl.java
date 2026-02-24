package com.rainbowmaodie.zjutbookingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rainbowmaodie.zjutbookingsystem.entity.Booking;
import com.rainbowmaodie.zjutbookingsystem.entity.User;
import com.rainbowmaodie.zjutbookingsystem.entity.VenueAdminBuilding;
import com.rainbowmaodie.zjutbookingsystem.mapper.BookingMapper;
import com.rainbowmaodie.zjutbookingsystem.mapper.VenueAdminBuildingMapper;
import com.rainbowmaodie.zjutbookingsystem.service.BookingService;
import com.rainbowmaodie.zjutbookingsystem.service.UserService;
import com.rainbowmaodie.zjutbookingsystem.vo.BookingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    @Autowired
    private UserService userService;

    @Autowired
    private VenueAdminBuildingMapper venueAdminBuildingMapper;

    @Override
    public Page<BookingVO> getBookingPage(Page<Booking> page, String applicant, String status, String startDate, String endDate, Long userId) {
        List<Long> buildingIds = null;
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null && "VENUE_ADMIN".equals(user.getRole())) {
                buildingIds = venueAdminBuildingMapper.selectList(
                        new LambdaQueryWrapper<VenueAdminBuilding>().eq(VenueAdminBuilding::getUserId, userId)
                ).stream().map(VenueAdminBuilding::getBuildingId).collect(Collectors.toList());
                
                if (buildingIds.isEmpty()) {
                    return new Page<BookingVO>();
                }
            }
        }
        return this.baseMapper.selectBookingVOPage(page, applicant, status, startDate, endDate, buildingIds);
    }

    @Override
    public void approve(Long id) {
        Booking booking = this.getById(id);
        if (booking == null) throw new RuntimeException("预约记录不存在");
        booking.setStatus("APPROVED");
        this.updateById(booking);
        // 这里后续可以添加生成PDF签名凭证的异步任务
    }

    @Override
    public void reject(Long id, String reason) {
        Booking booking = this.getById(id);
        if (booking == null) throw new RuntimeException("预约记录不存在");
        booking.setStatus("REJECTED");
        booking.setRejectReason(reason);
        this.updateById(booking);
    }
}
