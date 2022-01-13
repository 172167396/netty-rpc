package com.dailu.nettyserver.service.impl;

import com.dailu.nettycommon.entity.UserDTO;
import com.dailu.nettyserver.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserDTO getUser(String id) {
        return new UserDTO(id,"张三","安徽合肥");
    }

    @Override
    public String sendMsg(String msg) {
        return "server端已收到！";
    }
}
