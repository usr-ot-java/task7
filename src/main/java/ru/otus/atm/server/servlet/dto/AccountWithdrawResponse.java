package ru.otus.atm.server.servlet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AccountWithdrawResponse {
    @JsonProperty("status")
    Boolean status;
}
