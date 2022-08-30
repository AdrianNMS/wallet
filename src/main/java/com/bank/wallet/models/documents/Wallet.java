package com.bank.wallet.models.documents;

import com.bank.wallet.models.utils.Audit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document("wallets")
public class Wallet extends Audit
{
    @Id
    private String id;
    private String idDocument;
    private String phoneNumber;
    private String email;
    private Float bootcoins;
}
