package ru.otus.atm.cash;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.otus.atm.Denomination;
import ru.otus.atm.exception.CashSectionOverflowException;

@ToString
@EqualsAndHashCode
public class CashSectionImpl implements CashSection {

    private static final long MAX_BANKNOTES_AMOUNT = Long.MAX_VALUE;
    private final Denomination denomination;
    private final long banknotes;

    public CashSectionImpl(Denomination denomination) {
        this(denomination, 0);
    }

    public CashSectionImpl(Denomination denomination, long amount) {
        this.denomination = denomination;
        this.banknotes = amount;
    }

    @Override
    public long getCurrentAmountOfBanknotes() {
        return banknotes;
    }

    @Override
    public long getMaxNumberOfBanknotes() {
        return MAX_BANKNOTES_AMOUNT;
    }

    @Override
    public boolean canAddBanknotes(long amount) {
        long currentAmount = getCurrentAmountOfBanknotes();
        long newAmount = currentAmount + amount;
        return getMaxNumberOfBanknotes() >= newAmount && newAmount >= currentAmount;
    }

    @Override
    public CashSection addBanknotes(long amount) throws CashSectionOverflowException {
        if (!canAddBanknotes(amount)) {
            throw new CashSectionOverflowException(
                    String.format("Attempt to overflow the cash section for denomination = %d", denomination.getVal())
            );
        }
        return new CashSectionImpl(denomination, getCurrentAmountOfBanknotes() + amount);
    }

    @Override
    public CashSection issueBanknotes(long amount) {
        long currentAmount = getCurrentAmountOfBanknotes();
        if (amount > currentAmount) {
            throw new IllegalArgumentException(
                    String.format("Cannot issue banknotes amount (%d) more than have (%d)", amount, currentAmount)
            );
        }
        return new CashSectionImpl(denomination, currentAmount - amount);
    }

    @Override
    public Denomination getDenomination() {
        return denomination;
    }
}
