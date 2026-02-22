package com.revplay.revplay;

import com.revplay.revplay.config.MediaProperties;
import com.revplay.revplay.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, MediaProperties.class})
public class RevplayApplication {

    public static void main(String[] args) {
        SpringApplication.run(RevplayApplication.class, args);
    }

}




