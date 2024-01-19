package ru.otus.atm.server.servlet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;

@AllArgsConstructor
public class AccountBalanceResponse {
    @JsonProperty("amount")
    private BigInteger amount;
}
