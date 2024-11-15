package com.serendipity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@EnableAsync
@Service
public class AsyncService {

    @Autowired
    private OSSService ossService;

    @Async
    public String uploadOSS() throws InterruptedException {
        System.out.println("uploadOSS start, threadId = " + Thread.currentThread()
                                                                  .getName());
        Thread.sleep(5000);

        System.out.println("uploadOSS end, threadId = " + Thread.currentThread()
                                                                .getName());
        return "uploadOSS success";
    }

    @Async
    public void uploadOSS2(String fileName, InputStream inputStream) {
        System.out.println("t2:"+Thread.currentThread()
                                 .getName());
        ossService.upload(fileName, inputStream);
    }

}
