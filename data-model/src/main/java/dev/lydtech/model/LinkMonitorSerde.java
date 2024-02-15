package dev.lydtech.model;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class LinkMonitorSerde extends Serdes.WrapperSerde<LinkMonitor> {

    public LinkMonitorSerde() {
        super(new JsonSerializer<>(), new JsonDeserializer<>(LinkMonitor.class));
    }
}
