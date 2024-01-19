package ru.otus.atm.service;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class AuthenticationData {
    private final String cardNumber;
    private final String pinCode;
}
