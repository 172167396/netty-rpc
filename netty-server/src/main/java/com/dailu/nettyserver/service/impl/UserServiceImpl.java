package com.dailu.nettyserver.service.impl;

import com.dailu.nettycommon.entity.UserDTO;
import com.dailu.nettyserver.service.UserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserDTO getUser(String id) {
        if ("1".equals(id)) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new UserDTO(id, UUID.randomUUID().toString(), "安徽合肥");
    }

    @Override
    public String sendMsg(String msg) {
        return "server端已收到！";
    }
}
