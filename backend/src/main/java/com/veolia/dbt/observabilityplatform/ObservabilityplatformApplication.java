package com.veolia.dbt.observabilityplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class, 
    HibernateJpaAutoConfiguration.class
})
@EnableScheduling
public class ObservabilityplatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObservabilityplatformApplication.class, args);
	}

}

//Spring Application Context - this is the main manager, it's also called the "Container"
