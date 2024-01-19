package ru.otus.atm.server.servlet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@NoArgsConstructor
public class AccountWithdrawRequest {
    @JsonProperty(value = "amount", required = true)
    private BigInteger amount;
}
