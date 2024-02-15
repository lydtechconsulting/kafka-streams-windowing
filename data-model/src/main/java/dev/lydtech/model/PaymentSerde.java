package dev.lydtech.model;

import dev.lydtech.model.Payment;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class PaymentSerde extends Serdes.WrapperSerde<Payment> {

    public PaymentSerde() {
        super(new JsonSerializer<>(), new JsonDeserializer<>(Payment.class));
    }
}
