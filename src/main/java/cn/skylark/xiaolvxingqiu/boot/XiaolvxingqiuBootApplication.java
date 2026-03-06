package cn.skylark.xiaolvxingqiu.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XiaolvxingqiuBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaolvxingqiuBootApplication.class, args);
    }
}
