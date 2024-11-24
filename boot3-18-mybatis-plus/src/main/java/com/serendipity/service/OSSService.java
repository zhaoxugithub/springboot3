package com.serendipity.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class OSSService {
    // 阿里云OSS的Endpoint
    private final String ENDPOINT = "oss-cn-hangzhou.aliyuncs.com";
    // 阿里云OSS的AccessKeyId
    private final String ACCESSKEYID = "LTAI5tLuKJCHxpaL6n1g27Ez";
    // 阿里云OSS的AccessKeySecret
    private final String ACCESSKEYSECRET = "g7nBN2WlCzyHmi1ie0CDl1QrzfmBMk";
    // 阿里云OSS的BucketName
    private final String BUCKETNAME = "zhaoxu-bucket01";

    public String upload(String fileName, InputStream fileContent) {
        OSS oss = new OSSClientBuilder().build(ENDPOINT, ACCESSKEYID, ACCESSKEYSECRET);
        oss.putObject(new PutObjectRequest(BUCKETNAME, fileName, fileContent));
        // 设置URL过期时间1个小时
        Date expireTime = new Date(System.currentTimeMillis() + 3600 * 1000);
        URL url = oss.generatePresignedUrl(BUCKETNAME, fileName, expireTime);
        System.out.println("url:" + url.toString());
        return url.toString();
    }

    public static void main(String[] args) {
        String str = "test content";
        InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        String upload = new OSSService().upload("test.txt", inputStream);
        System.out.println(upload);
    }
}
