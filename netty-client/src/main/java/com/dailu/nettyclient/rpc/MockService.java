package com.dailu.nettyclient.rpc;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class MockService {
    public Collection<String> getData(int page) {
        if (page == 20) {
            return Collections.emptyList();
        }
        return Collections.singletonList(String.valueOf(page));
    }
}
