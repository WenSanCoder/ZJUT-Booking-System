package com.rainbowmaodie.zjutbookingsystem.service;

import com.rainbowmaodie.zjutbookingsystem.entity.Notification;
import com.rainbowmaodie.zjutbookingsystem.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Async
    public void sendBookingCancellation(Long userId, String username, String venueName, String timeRange, String reason) {
        String content = String.format("由于 %s, 您预约的 %s (%s) 已被取消。", reason, venueName, timeRange);
        saveNotification(userId, "预约取消通知", content, "BOOKING");
        log.info("通知已保存并进入发送队列: 用户 {}", username);
    }

    @Async
    public void sendNotification(Long userId, String title, String content, String type) {
        saveNotification(userId, title, content, type);
    }

    private void saveNotification(Long userId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }
}
