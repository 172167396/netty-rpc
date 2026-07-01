package com.dailu.nettyclient.controller;

import com.dailu.nettyclient.rpc.MockService;
import com.dailu.nettyclient.rpc.UserService;
import com.dailu.nettycommon.entity.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
public class UserController {

    @Resource
    UserService userService;

    @Resource
    MockService mockService;

    @Resource
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @GetMapping("/getUser/{id}")
    public UserDTO get(@PathVariable String id) {
        return userService.getUser(id);
    }

    @GetMapping("/send/{msg}")
    public String send(@PathVariable String msg) throws Exception {
        return null;
    }


}
