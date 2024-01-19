package ru.otus.atm.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.atm.Atm;
import ru.otus.atm.Denomination;
import ru.otus.atm.account.Account;
import ru.otus.atm.exception.AccountNotFoundException;
import ru.otus.atm.exception.AuthenticationFailedException;
import ru.otus.atm.exception.CashSectionOverflowException;
import ru.otus.atm.exception.InsufficientBalanceException;
import ru.otus.atm.repository.AccountRepository;
import ru.otus.atm.password.validator.PasswordValidator;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Slf4j
public class AccountService {
    private final HashMap<String, String> cardNumberToHashPin;
    private final AccountRepository accountRepository;
    private final PasswordValidator passwordValidator;
    private final Atm atm;

    public synchronized Account authenticate(AuthenticationData authenticationData) {
        return authenticateAccount(authenticationData);
    }

    public synchronized BigInteger getBalance(Account account) {
        return atm.getAccountBalance(account);
    }

    public synchronized boolean depositMoney(Account account, Map<Denomination, Long> cash) {
        try {
            atm.depositMoney(account, cash);
            return true;
        } catch (CashSectionOverflowException e) {
            log.error("Failed to deposit money for accountId = {}", account.getId(), e);
            return false;
        }
    }

    public synchronized boolean withdrawMoney(Account account, BigInteger amount) {
        try {
            atm.withdrawMoney(account, amount);
            return true;
        } catch (InsufficientBalanceException e) {
            log.error("Failed to withdraw money for accountId = {}", account.getId(), e);
            return false;
        }
    }

    private Account authenticateAccount(AuthenticationData authenticationData) {
        String cardNumber = authenticationData.getCardNumber();
        if (cardNumberToHashPin.containsKey(cardNumber)) {
            String pinCode = authenticationData.getPinCode();
            if (!passwordValidator.validatePassword(pinCode, cardNumberToHashPin.get(cardNumber))) {
                throw new AuthenticationFailedException(
                        String.format("Pin code for card number `%s` is incorrect", cardNumber)
                );
            }
        }
        return accountRepository.getAccount(cardNumber).orElseThrow(() ->
                new AccountNotFoundException(String.format("Account for card number `%s` is not found", cardNumber))
        );
    }
}
