package com.concurrencystudy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.concurrencystudy.mapper")
public class ConcurrencystudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcurrencystudyApplication.class, args);
        System.out.println("Hello World");
	}

}
