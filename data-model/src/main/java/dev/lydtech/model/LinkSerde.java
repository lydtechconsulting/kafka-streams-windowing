package dev.lydtech.model;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class LinkSerde extends Serdes.WrapperSerde<Link> {

    public LinkSerde() {
        super(new JsonSerializer<>(), new JsonDeserializer<>(Link.class));
    }
}
