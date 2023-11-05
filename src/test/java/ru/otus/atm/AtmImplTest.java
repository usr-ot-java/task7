package ru.otus.atm;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.atm.account.Account;
import ru.otus.atm.account.AccountImpl;
import ru.otus.atm.cash.CashSection;
import ru.otus.atm.cash.CashSectionImpl;
import ru.otus.atm.exception.CashSectionOverflowException;
import ru.otus.atm.exception.InsufficientBalanceException;
import ru.otus.atm.widthdraw.CashWithdrawalAlgorithm;
import ru.otus.atm.widthdraw.GreedyCashWithdrawalAlgorithm;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AtmImplTest {

    private static final CashWithdrawalAlgorithm greedyCashWithdrawalAlgorithm = new GreedyCashWithdrawalAlgorithm();
    private static CashSection[] cashSections;

    @BeforeAll
    static void init() {
        cashSections = new CashSection[Denomination.values().length];
        cashSections[0] = new CashSectionImpl(Denomination.TEN, 5);
        cashSections[1] = new CashSectionImpl(Denomination.ONE_HUNDRED, 5);
        cashSections[2] = new CashSectionImpl(Denomination.FIVE_HUNDRED, 5);
        cashSections[3] = new CashSectionImpl(Denomination.ONE_THOUSAND, 5);
        cashSections[4] = new CashSectionImpl(Denomination.FIVE_THOUSAND, 5);
    }

    @Test
    @DisplayName("Attempt to pass null as cashWithdrawalAlgorithm")
    void testAtmImplInitWrong1() {
        assertThrows(NullPointerException.class, () -> new AtmImpl(null));
    }

    @Test
    @DisplayName("Attempt to pass not all cash sections")
    void testAtmImplInitWrong2() {
        assertThrows(IllegalArgumentException.class, () -> new AtmImpl(greedyCashWithdrawalAlgorithm));
    }

    @Test
    @DisplayName("Attempt to pass not all cash sections")
    void testAtmImplInitWrong3() {
        assertThrows(IllegalArgumentException.class, () ->
                new AtmImpl(greedyCashWithdrawalAlgorithm, new CashSectionImpl(Denomination.TEN, 1))
        );
    }

    @Test
    @DisplayName("Correct initialization of AtmImpl()")
    void testAtmImplInitCorrect() {
        new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);
    }

    @Test
    @DisplayName("Total Balance")
    void testGetTotalBalance() {
        Atm atm = new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);
        assertEquals(BigInteger.valueOf(33050L), atm.getTotalBalance());
    }

    @Test
    @DisplayName("Inserting 1 cash sections")
    void testInsertCashSection() {
        Atm atm = new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);
        atm.insertCashSection(new CashSectionImpl(Denomination.TEN, 10L));
        assertEquals(BigInteger.valueOf(33100L), atm.getTotalBalance());
    }

    @Test
    @DisplayName("Inserting 2 cash sections")
    void testInsertCashSections() {
        Atm atm = new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);
        atm.insertCashSections(
                new CashSectionImpl(Denomination.TEN, 10L), new CashSectionImpl(Denomination.ONE_HUNDRED)
        );
        assertEquals(BigInteger.valueOf(32600L), atm.getTotalBalance());
    }

    @Test
    @DisplayName("Get cash sections")
    void testGetCashSections() {
        Atm atm = new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);

        List<CashSection> expectedCashSections = List.of(new CashSectionImpl(Denomination.TEN, 5),
                new CashSectionImpl(Denomination.ONE_HUNDRED, 5),
                new CashSectionImpl(Denomination.FIVE_HUNDRED, 5),
                new CashSectionImpl(Denomination.ONE_THOUSAND, 5),
                new CashSectionImpl(Denomination.FIVE_THOUSAND, 5));
        List<CashSection> actualCashSections = atm.getCashSections();
        assertListsEqualIgnoreOrder(expectedCashSections, actualCashSections);
    }

    @Test
    @DisplayName("Get account balance")
    void testGetAccountBalance() {
        Atm atm = new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);
        Account account = new AccountImpl(1L, BigInteger.valueOf(100));
        assertEquals(BigInteger.valueOf(100L), atm.getAccountBalance(account));
    }

    @Test
    @DisplayName("Successful withdraw account's money")
    void testWithdrawMoneySuccess1() throws InsufficientBalanceException {
        Atm atm = new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);
        Account account = new AccountImpl(1L, BigInteger.valueOf(1600));

        Map<Denomination, Long> actualResult = atm.withdrawMoney(account, BigInteger.valueOf(1600));
        Map<Denomination, Long> expectedResult = Map.of(Denomination.ONE_THOUSAND, 1L,
                Denomination.FIVE_HUNDRED, 1L, Denomination.ONE_HUNDRED, 1L);

        assertEquals(expectedResult, actualResult);
        assertEquals(BigInteger.ZERO, account.getBalance());
        assertEquals(BigInteger.valueOf(31450L), atm.getTotalBalance());
    }

    @Test
    @DisplayName("Unsuccessful withdraw account's money because of insufficient balance")
    void testWithdrawMoneyNotSuccess1() {
        Atm atm = new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);
        Account account = new AccountImpl(1L, BigInteger.valueOf(1000));

        assertThrows(InsufficientBalanceException.class, () -> atm.withdrawMoney(account, BigInteger.valueOf(1100)));

        assertEquals(BigInteger.valueOf(1000L), account.getBalance());
        assertEquals(BigInteger.valueOf(33050L), atm.getTotalBalance());
    }

    @Test
    @DisplayName("Unsuccessful withdraw account's money because of the requested sum")
    void testWithdrawMoneyNotSuccess2() throws InsufficientBalanceException {
        Atm atm = new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);
        Account account = new AccountImpl(1L, BigInteger.valueOf(1000));

        // Banknote 5 does not exist
        Map<Denomination, Long> actualResult = atm.withdrawMoney(account, BigInteger.valueOf(955));
        assertNull(actualResult);
        assertEquals(BigInteger.valueOf(1000L), account.getBalance());
        assertEquals(BigInteger.valueOf(33050L), atm.getTotalBalance());
    }

    @Test
    @DisplayName("Successful money deposit")
    void testMoneyDepositSuccess() throws CashSectionOverflowException {
        Atm atm = new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);
        Account account = new AccountImpl(1L, BigInteger.valueOf(1000));

        Map<Denomination, Long> cash = Map.of(Denomination.ONE_THOUSAND, 1L);
        atm.depositMoney(account, cash);

        List<CashSection> expectedCashSections = List.of(new CashSectionImpl(Denomination.TEN, 5),
                new CashSectionImpl(Denomination.ONE_HUNDRED, 5),
                new CashSectionImpl(Denomination.FIVE_HUNDRED, 5),
                new CashSectionImpl(Denomination.ONE_THOUSAND, 6),
                new CashSectionImpl(Denomination.FIVE_THOUSAND, 5));

        assertEquals(BigInteger.valueOf(2000L), account.getBalance());
        assertEquals(BigInteger.valueOf(34050L), atm.getTotalBalance());
        assertListsEqualIgnoreOrder(expectedCashSections, atm.getCashSections());
    }

    @Test
    @DisplayName("Unsuccessful money deposit")
    void testMoneyDepositWrong() {
        Atm atm = new AtmImpl(greedyCashWithdrawalAlgorithm, cashSections);
        Account account = new AccountImpl(1L, BigInteger.valueOf(1000));

        assertThrows(CashSectionOverflowException.class, () ->
                atm.depositMoney(account, Map.of(Denomination.FIVE_THOUSAND, Long.MAX_VALUE))
        );

        assertEquals(BigInteger.valueOf(1000L), account.getBalance());
        assertEquals(BigInteger.valueOf(33050L), atm.getTotalBalance());
    }

    private static void assertListsEqualIgnoreOrder(List<?> expected, List<?> actual) {
        assertTrue(expected.size() == actual.size()
                && expected.containsAll(actual)
                && actual.containsAll(expected));
    }


}
