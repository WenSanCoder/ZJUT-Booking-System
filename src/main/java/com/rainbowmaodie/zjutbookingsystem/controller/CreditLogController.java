package com.rainbowmaodie.zjutbookingsystem.controller;

import com.rainbowmaodie.zjutbookingsystem.entity.CreditLog;
import com.rainbowmaodie.zjutbookingsystem.service.CreditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/credit-logs")
public class CreditLogController {

    @Autowired
    private CreditLogService creditLogService;

    @GetMapping
    public List<CreditLog> list() {
        return creditLogService.list();
    }
}
