package ru.otus.atm.account;

import ru.otus.atm.exception.InsufficientBalanceException;

import java.math.BigInteger;

public interface Account {
    long getId();
    BigInteger getBalance();
    void depositMoney(BigInteger amount);
    void withdrawMoney(BigInteger amount) throws InsufficientBalanceException;
}
