package com.example.wssdn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackageClasses = {WsSdnApplication.class, com.example.wssdn.entity.BlackListIP.class})
public class WsSdnApplication {
	public static void main(String[] args) {
		SpringApplication.run(WsSdnApplication.class, args);
	}
}
