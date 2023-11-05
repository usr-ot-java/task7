package ru.otus.atm;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.otus.atm.account.Account;
import ru.otus.atm.cash.CashSection;
import ru.otus.atm.exception.CashSectionOverflowException;
import ru.otus.atm.exception.InsufficientBalanceException;
import ru.otus.atm.widthdraw.CashWithdrawalAlgorithm;

import java.math.BigInteger;
import java.util.*;

@EqualsAndHashCode
@ToString
public class AtmImpl implements Atm {
    private static final Set<Denomination> denominationSet = Set.copyOf(List.of(Denomination.values()));

    private final CashWithdrawalAlgorithm cashWithdrawalAlgorithm;
    // 1 banknote type -> 1 cash section with that exact type
    private final Map<Denomination, CashSection> cashStorage = new HashMap<>();

    public AtmImpl(CashWithdrawalAlgorithm cashWithdrawalAlgorithm, CashSection... cashSections) {
        Objects.requireNonNull(cashWithdrawalAlgorithm);

        Set<Denomination> givenDenominationSet = new HashSet<>();
        Arrays.stream(cashSections).forEach(c -> givenDenominationSet.add(c.getDenomination()));
        if (givenDenominationSet.size() != denominationSet.size()) {
            throw new IllegalArgumentException(
                    String.format("Cash sections are incorrectly loaded. Expected amount of denominations is %d, but got %d",
                            denominationSet.size(), cashSections.length)
            );
        }

        Arrays.stream(cashSections)
                .forEach(cashSection -> cashStorage.put(cashSection.getDenomination(), cashSection));
        this.cashWithdrawalAlgorithm = cashWithdrawalAlgorithm;
    }

    @Override
    public synchronized void insertCashSection(CashSection cashSection) {
        insertCashSections(cashSection);
    }

    @Override
    public synchronized void insertCashSections(CashSection... cashSections) {
        // Assume that old cache sections are automatically removed and new ones are added
        Arrays.stream(cashSections)
                .forEach(cashSection -> cashStorage.put(cashSection.getDenomination(), cashSection));
    }

    @Override
    public synchronized BigInteger getTotalBalance() {
        BigInteger total = BigInteger.ZERO;
        for (var entry : cashStorage.entrySet()) {
            long numberOfBanknotes = entry.getValue().getCurrentAmountOfBanknotes();
            Denomination denomination = entry.getKey();
            total = total.add(
                    BigInteger.valueOf(denomination.getVal()).multiply(BigInteger.valueOf(numberOfBanknotes))
            );
        }
        return total;
    }

    @Override
    public List<CashSection> getCashSections() {
        return new ArrayList<>(cashStorage.values());
    }

    @Override
    public synchronized Map<Denomination, Long> withdrawMoney(Account account, BigInteger requestedSum)
            throws InsufficientBalanceException {
        Map<Denomination, Long> result = cashWithdrawalAlgorithm.withdrawMoney(cashStorage,
                getTotalBalance(), account, requestedSum);

        if (result == null) {
            return null;
        }

        result.forEach((key, value) -> cashStorage.put(key, cashStorage.get(key).issueBanknotes(value)));
        return result;
    }

    @Override
    public synchronized void depositMoney(Account account, Map<Denomination, Long> cash)
            throws CashSectionOverflowException {
        // Validation stage
        for (var entry : cash.entrySet()) {
            Denomination denomination = entry.getKey();
            long amount = entry.getValue();
            if (amount > 0) {
                if (!cashStorage.get(denomination).canAddBanknotes(amount)) {
                    throw new CashSectionOverflowException(
                            String.format("Attempt by account (accountId = %d) to overflow the cash section for denomination = %d",
                                    account.getId(), denomination.getVal())
                    );
                }
            }
        }

        for (var entry : cash.entrySet()) {
            Denomination denomination = entry.getKey();
            long amount = entry.getValue();
            if (amount > 0) {
                cashStorage.put(denomination, cashStorage.get(denomination).addBanknotes(amount));
            }
        }
        BigInteger amount = cash.entrySet().stream()
                        .map(e -> BigInteger.valueOf(e.getKey().getVal()).multiply(BigInteger.valueOf(e.getValue())))
                        .reduce(BigInteger.ZERO, BigInteger::add);
        account.depositMoney(amount);
    }

    @Override
    public BigInteger getAccountBalance(Account account) {
        return account.getBalance();
    }

}
