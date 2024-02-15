package dev.lydtech.datacreator;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class DataCreatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataCreatorApplication.class, args);
	}

}
