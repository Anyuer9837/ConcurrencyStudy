package com.concurrencystudy.controller;

import com.concurrencystudy.dao.UserHot;
import com.concurrencystudy.service.UserHotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-hot")
public class UserHotController {

    @Autowired
    private UserHotService userHotService;

    /**
     * 根据 uid 查询用户的热度
     * 测试 URL: GET http://localhost:8080/user-hot/1
     */
    @GetMapping("/{uid}")
    public UserHot getUserHot(@PathVariable Integer uid) {
        return userHotService.getUserByUid(uid);
    }

    /**
     * 更新用户热度
     * 测试 URL: POST http://localhost:8080/user-hot/update
     * 请求体 JSON: {"uid": 1, "hot": 20}
     */
    @PostMapping("/update")
    public String updateHot(@RequestBody UserHot userHot) {
        boolean success = userHotService.updateHot(userHot);
        return success ? "更新成功" : "更新失败";
    }
}