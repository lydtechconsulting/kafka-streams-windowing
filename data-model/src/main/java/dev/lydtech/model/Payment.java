package dev.lydtech.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

        private UUID accountId;
        private String itemId;
        private Long amount;
        private String currency;


    public static PaymentBuilder builder(Payment copy){
        PaymentBuilder paymentBuilder =new PaymentBuilder();
        paymentBuilder.accountId = copy.accountId;
        paymentBuilder.itemId = copy.itemId;
        paymentBuilder.amount = copy.amount;
        paymentBuilder.currency = copy.currency;
        return paymentBuilder;
    }

    public static PaymentBuilder builder(){
        return new PaymentBuilder();
    }

}
