package dev.lydtech.windows.sliding;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
public class SlidingWindowConfiguration {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, "sliding-window-app");
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(REPLICATION_FACTOR_CONFIG, 1);

        return new KafkaStreamsConfiguration(props);
    }



//    @Bean
//    KeyValueStore simpleStateStore() {
//        return Stores.keyValueStoreBuilder(
//                Stores.inMemoryKeyValueStore("myInMemoryKeyStore"), Serdes.String(), new PaymentSerde()).build();
//    }


//    @Bean
//    PaymentSerde paymentSerde() {
//            return new PaymentSerde();
//    }
}
