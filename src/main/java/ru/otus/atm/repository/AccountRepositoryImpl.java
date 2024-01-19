package ru.otus.atm.repository;

import ru.otus.atm.account.Account;

import java.util.HashMap;
import java.util.Optional;

public class AccountRepositoryImpl implements AccountRepository {
    private final HashMap<String, Account> cardNumberToAccount;

    public AccountRepositoryImpl(HashMap<String, Account> cardNumberToAccount) {
        this.cardNumberToAccount = cardNumberToAccount;
    }

    @Override
    public Optional<Account> getAccount(String cardNumber) {
        return Optional.ofNullable(cardNumberToAccount.get(cardNumber));
    }
}
