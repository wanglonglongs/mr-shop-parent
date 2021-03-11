package com.baidu;

import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @ClassName RunUserServerApplication
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/3/10
 * @Version V1.0
 **/
@SpringBootApplication
@EnableEurekaClient
@MapperScan(value = "com.baidu.shop.mapper")
public class RunUserServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunUserServerApplication.class);
    }
}
