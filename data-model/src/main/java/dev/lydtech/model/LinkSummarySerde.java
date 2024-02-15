package dev.lydtech.model;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class LinkSummarySerde extends Serdes.WrapperSerde<LinkSummary> {

    public LinkSummarySerde() {
        super(new JsonSerializer<>(), new JsonDeserializer<>(LinkSummary.class));
    }
}
