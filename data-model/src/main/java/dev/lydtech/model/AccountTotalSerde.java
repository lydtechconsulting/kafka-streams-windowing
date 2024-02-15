package dev.lydtech.model;

import dev.lydtech.model.AccountTotal;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class AccountTotalSerde extends Serdes.WrapperSerde<AccountTotal> {

    public AccountTotalSerde() {
        super(new JsonSerializer<>(), new JsonDeserializer<>(AccountTotal.class));
    }
}
