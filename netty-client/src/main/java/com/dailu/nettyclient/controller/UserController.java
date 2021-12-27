package com.dailu.nettyclient.controller;

import com.dailu.nettyclient.rpc.UserService;
import com.dailu.nettycommon.entity.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserController {

    @Resource
    UserService userService;

    @GetMapping("/getUser/{id}")
    public UserDTO get(@PathVariable String id){
        return userService.getUser(id);
    }

}
