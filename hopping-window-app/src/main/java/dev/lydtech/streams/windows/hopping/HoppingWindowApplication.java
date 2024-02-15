package dev.lydtech.streams.windows.hopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@SpringBootApplication
public class HoppingWindowApplication {

	public static void main(String[] args) {
		SpringApplication.run(HoppingWindowApplication.class, args);
	}

}
