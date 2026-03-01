package com.rainbowmaodie.zjutbookingsystem.controller;

import com.rainbowmaodie.zjutbookingsystem.common.Result;
import com.rainbowmaodie.zjutbookingsystem.dto.UserDTO;
import com.rainbowmaodie.zjutbookingsystem.entity.User;
import com.rainbowmaodie.zjutbookingsystem.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<UserDTO> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        try {
            User user = userService.login(username, password);
            UserDTO userDTO = new UserDTO();
            if (user != null) {
                BeanUtils.copyProperties(user, userDTO);
            }
            return Result.success(userDTO);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        try {
            userService.register(user);
            return Result.success("注册成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/password")
    public Result<String> changePassword(@RequestBody Map<String, Object> data) {
        try {
            Long userId = Long.valueOf(data.get("userId").toString());
            String oldPassword = (String) data.get("oldPassword");
            String newPassword = (String) data.get("newPassword");
            userService.changePassword(userId, oldPassword, newPassword);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<UserDTO> getInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            return Result.success(userDTO);
        }
        return Result.error("用户不存在");
    }

    @PostMapping("/signature")
    public Result<String> updateSignature(@RequestBody Map<String, Object> data) {
        try {
            Long userId = Long.valueOf(data.get("userId").toString());
            String signatureUrl = (String) data.get("signatureUrl");
            userService.updateSignature(userId, signatureUrl);
            return Result.success("电子签名更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
