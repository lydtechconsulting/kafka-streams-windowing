package dev.lydtech.streams.windows.tumbling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@SpringBootApplication
public class TumblingWindowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TumblingWindowApplication.class, args);
	}

}
