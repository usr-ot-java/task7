package ru.otus.atm.account;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.otus.atm.exception.InsufficientBalanceException;

import java.math.BigInteger;

@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class AccountImpl implements Account {

    private final long id;
    private BigInteger balance;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public BigInteger getBalance() {
        return balance;
    }

    @Override
    public synchronized void depositMoney(BigInteger amount) {
        balance = balance.add(amount);
    }

    @Override
    public synchronized void withdrawMoney(BigInteger amount) throws InsufficientBalanceException {
        if (amount.compareTo(balance) >= 1) {
            throw new InsufficientBalanceException(
                    String.format("Cannot withdraw %d since balance is %d for accountId = %d", amount, balance, id)
            );
        }
        balance = balance.subtract(amount);
    }
}
