package ru.otus.atm.server.servlet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class AccountDepositRequest {
    @JsonProperty(value = "cash", required = true)
    Map<Integer, Long> cash;
}
