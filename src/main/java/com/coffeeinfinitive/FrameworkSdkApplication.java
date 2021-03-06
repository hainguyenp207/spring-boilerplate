package com.coffeeinfinitive;

import com.coffeeinfinitive.storage.StorageProperties;
import com.coffeeinfinitive.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
@EnableCaching
public class FrameworkSdkApplication{

	public static void main(String[] args) {
		SpringApplication.run(FrameworkSdkApplication.class, args);
	}
	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
//			storageService.deleteAll();
			storageService.init();
		};
	}
}
