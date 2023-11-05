package ru.otus.atm.widthdraw;

import ru.otus.atm.Denomination;
import ru.otus.atm.account.Account;
import ru.otus.atm.cash.CashSection;
import ru.otus.atm.exception.InsufficientBalanceException;

import java.math.BigInteger;
import java.util.Map;

public interface CashWithdrawalAlgorithm {
    Map<Denomination, Long> withdrawMoney(Map<Denomination, CashSection> cashStorage,
                                          BigInteger totalBalance,
                                          Account account,
                                          BigInteger requestedSum) throws InsufficientBalanceException;
}
