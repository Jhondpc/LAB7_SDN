package com.example.wssdn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EntityScan(basePackages = "com.example.wssdn")
public class WsSdnApplication {

	public static void main(String[] args) {
		SpringApplication.run(WsSdnApplication.class, args);
	}

}
