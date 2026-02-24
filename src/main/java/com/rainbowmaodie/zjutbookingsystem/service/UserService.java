package com.rainbowmaodie.zjutbookingsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rainbowmaodie.zjutbookingsystem.entity.User;
import com.rainbowmaodie.zjutbookingsystem.dto.UserDTO;

public interface UserService extends IService<User> {
    User login(String username, String password);
    void register(User user);
    void changePassword(Long userId, String oldPassword, String newPassword);
}
