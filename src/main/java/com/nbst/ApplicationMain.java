package com.nbst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.nbst")
public class ApplicationMain extends SpringBootServletInitializer{

	@Override
	protected SpringApplicationBuilder configure(
			SpringApplicationBuilder application) {
		setRegisterErrorPageFilter(false);
		return application.sources(ApplicationMain.class);
	}

	public static void main(String[] args) throws Exception {
		// 启动Spring Boot项目的唯一入口
		SpringApplication springApplication = new SpringApplication(
				ApplicationMain.class);
		springApplication.run(args);
	}

}
