package com.dailu.nettyclient.rpc;

import com.dailu.nettyclient.aspect.annotation.RpcClient;
import com.dailu.nettycommon.entity.UserDTO;

@RpcClient(destiny = "userService")
public interface UserService {
    UserDTO getUser(String id);
}
