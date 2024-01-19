package ru.otus.atm;

import lombok.Getter;

public enum Denomination {
    TEN(10), ONE_HUNDRED(100), FIVE_HUNDRED(500), ONE_THOUSAND(1000), FIVE_THOUSAND(5000);

    @Getter
    private final int val;
    Denomination(int val) {
        this.val = val;
    }

    public static Denomination getByVal(int val) {
        Denomination[] denominations = Denomination.values();
        for (int i = 0; i < denominations.length; i++) {
            if (denominations[i].val == val) {
                return denominations[i];
            }
        }
        return null;
    }
}
