package com.rainbowmaodie.zjutbookingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rainbowmaodie.zjutbookingsystem.entity.Booking;
import com.rainbowmaodie.zjutbookingsystem.vo.BookingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
    Page<BookingVO> selectBookingVOPage(Page<Booking> page, 
                                       @Param("applicant") String applicant, 
                                       @Param("status") String status,
                                       @Param("startDate") String startDate,
                                       @Param("endDate") String endDate,
                                       @Param("buildingIds") List<Long> buildingIds,
                                       @Param("venueIds") List<Long> venueIds);
}
