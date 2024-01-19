package ru.otus.atm.repository;

import ru.otus.atm.account.Account;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> getAccount(String cardNumber);
}
