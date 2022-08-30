package com.bank.wallet.models.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResponseTransference
{
    private String idTransference;
    private Boolean status;
}
