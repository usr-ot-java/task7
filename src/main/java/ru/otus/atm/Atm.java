package ru.otus.atm;

import ru.otus.atm.account.Account;
import ru.otus.atm.cash.CashSection;
import ru.otus.atm.exception.CashSectionOverflowException;
import ru.otus.atm.exception.InsufficientBalanceException;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface Atm {
    void insertCashSection(CashSection cashSection);

    void insertCashSections(CashSection... cashSection);

    BigInteger getTotalBalance();

    List<CashSection> getCashSections();

    Map<Denomination, Long> withdrawMoney(Account account, BigInteger requestedSum) throws InsufficientBalanceException;

    void depositMoney(Account account, Map<Denomination, Long> cash) throws CashSectionOverflowException;

    BigInteger getAccountBalance(Account account);
}
