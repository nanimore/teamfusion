package com.zoro.teamfusion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@MapperScan("com.zoro.teamfusion.mapper")
@EnableScheduling
public class TeamFusionApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamFusionApplication.class, args);
//        Calendar c = Calendar.getInstance();
    }

}
