package ru.otus.atm.widthdraw;

import ru.otus.atm.Denomination;
import ru.otus.atm.account.Account;
import ru.otus.atm.cash.CashSection;
import ru.otus.atm.exception.InsufficientBalanceException;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GreedyCashWithdrawalAlgorithm implements CashWithdrawalAlgorithm {

    private static final Comparator<Denomination> COMPARATOR = new Comparator<Denomination>() {
        @Override
        public int compare(Denomination o1, Denomination o2) {
            return o2.getVal() - o1.getVal();
        }
    };

    @Override
    public Map<Denomination, Long> withdrawMoney(Map<Denomination, CashSection> cashStorage,
                                                 BigInteger totalBalance,
                                                 Account account,
                                                 BigInteger requestedSum) throws InsufficientBalanceException {
        if (requestedSum.compareTo(account.getBalance()) >= 1) {
            throw new InsufficientBalanceException(
                    String.format("Requested sum is greater than account's balance for accountId = %d", account.getId())
            );
        }

        if (requestedSum.compareTo(totalBalance) >= 1) {
            return null;
        }

        TreeMap<Denomination, CashSection> cashSections = new TreeMap<>(COMPARATOR);
        cashSections.putAll(cashStorage);
        BigInteger remaining = requestedSum;

        Map<Denomination, Long> result = new HashMap<>();
        for (var entry : cashSections.entrySet()) {
            Denomination denomination = entry.getKey();
            long banknotesAvailableAmount = entry.getValue().getCurrentAmountOfBanknotes();

            BigInteger sum = remaining.subtract(remaining.mod(BigInteger.valueOf(denomination.getVal())));
            // Max.of(banknotesAvailableAmount, sum / denomination.getVal())
            BigInteger banknotesAmount = BigInteger.valueOf(banknotesAvailableAmount)
                    .min(sum.divide(BigInteger.valueOf(denomination.getVal())));

            if (banknotesAmount.longValue() > 0) {
                result.put(denomination, banknotesAmount.longValue());
                remaining = remaining.subtract(
                        BigInteger.valueOf(banknotesAmount.longValue()).multiply(BigInteger.valueOf(denomination.getVal()))
                );
            }

            if (remaining.equals(BigInteger.ZERO)) {
                break;
            }
        }

        if (remaining.equals(BigInteger.ZERO)) {
            account.withdrawMoney(requestedSum);
            return result;
        }
        return null;
    }

}
