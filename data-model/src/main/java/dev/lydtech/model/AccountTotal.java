package dev.lydtech.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountTotal {

        private UUID accountId;
        @Builder.Default
        private Long totalAmount=0L;
        private String currency;

//    public static AccountTotal.AccountTotalBuilder builder(AccountTotal copy){
//        AccountTotal.AccountTotalBuilder accountTotalBuilder =new AccountTotal.AccountTotalBuilder();
//        accountTotalBuilder.accountId = copy.accountId;
//        accountTotalBuilder.amount = copy.amount;
//        accountTotalBuilder.currency = copy.currency;
//        return accountTotalBuilder;
//    }
//
//    public static AccountTotal.AccountTotalBuilder builder(){
//        return new AccountTotal.AccountTotalBuilder();
//    }

}
