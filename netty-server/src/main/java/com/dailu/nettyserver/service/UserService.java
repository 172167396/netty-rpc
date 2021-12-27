package com.dailu.nettyserver.service;

import com.dailu.nettycommon.entity.UserDTO;

public interface UserService {
    UserDTO getUser(String id);
}
