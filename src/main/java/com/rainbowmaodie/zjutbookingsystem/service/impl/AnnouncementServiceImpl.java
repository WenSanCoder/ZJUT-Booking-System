package com.rainbowmaodie.zjutbookingsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rainbowmaodie.zjutbookingsystem.entity.Announcement;
import com.rainbowmaodie.zjutbookingsystem.mapper.AnnouncementMapper;
import com.rainbowmaodie.zjutbookingsystem.service.AnnouncementService;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
}
