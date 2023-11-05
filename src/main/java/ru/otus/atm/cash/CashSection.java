package ru.otus.atm.cash;

import ru.otus.atm.Denomination;
import ru.otus.atm.exception.CashSectionOverflowException;

public interface CashSection {
    long getCurrentAmountOfBanknotes();
    long getMaxNumberOfBanknotes();
    boolean canAddBanknotes(long amount);
    CashSection addBanknotes(long amount) throws CashSectionOverflowException;
    CashSection issueBanknotes(long amount);
    Denomination getDenomination();
}
