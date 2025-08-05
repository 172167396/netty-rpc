package com.dailu.nettyclient.controller;

import com.dailu.nettyclient.config.ClientInitConfig;
import com.dailu.nettyclient.handler.NettyClientHandler;
import com.dailu.nettyclient.rpc.BankService;
import com.dailu.nettyclient.rpc.MockService;
import com.dailu.nettyclient.rpc.UserService;
import com.dailu.nettyclient.utils.CompletionExecutorWrapper;
import com.dailu.nettyclient.utils.PaginatedJob;
import com.dailu.nettyclient.utils.R;
import com.dailu.nettycommon.entity.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.Future;

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

    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(BankService.class);
        enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
            System.out.println("...........");
            return String.valueOf(Math.random());
        });
        BankService bankService = (BankService)enhancer.create();
        System.out.println(bankService.getBankName());
    }

}
