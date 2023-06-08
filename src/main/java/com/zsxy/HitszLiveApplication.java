package com.zsxy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.zsxy.mapper")
@SpringBootApplication
public class HitszLiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(HitszLiveApplication.class, args);
    }

}
