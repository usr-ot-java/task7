package ru.otus.atm;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletRequestListener;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.yaml.snakeyaml.Yaml;
import ru.otus.atm.account.Account;
import ru.otus.atm.account.AccountImpl;
import ru.otus.atm.cash.CashSection;
import ru.otus.atm.cash.CashSectionImpl;
import ru.otus.atm.password.generator.PasswordHashGenerator;
import ru.otus.atm.password.generator.PasswordHashGeneratorImpl;
import ru.otus.atm.password.validator.PasswordValidator;
import ru.otus.atm.password.validator.PasswordValidatorImpl;
import ru.otus.atm.repository.AccountRepository;
import ru.otus.atm.repository.AccountRepositoryImpl;
import ru.otus.atm.server.filter.AuthenticationFilter;
import ru.otus.atm.server.servlet.AccountServlet;
import ru.otus.atm.service.AccountService;
import ru.otus.atm.widthdraw.GreedyCashWithdrawalAlgorithm;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

@Slf4j
public class Main {
    private static final int port = 8080;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Map<String, Object> props;
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("application.yaml")) {
            Yaml yaml = new Yaml();
            props = yaml.load(is);
        }

        HashMap<String, Account> cardNumberToAccount = new HashMap<>();
        HashMap<String, String> cardNumberToHashPin = new HashMap<>();

        PasswordHashGenerator passwordHashGenerator = new PasswordHashGeneratorImpl();

        for (Map<String, Object> accProps : (Iterable<Map<String, Object>>) props.get("accounts")) {
            long id = ((Number) accProps.get("id")).longValue();
            BigInteger balance = BigInteger.valueOf(((Number) accProps.get("balance")).longValue());
            String cardNumber = (String) accProps.get("cardNumber");
            String pinCode = (String) accProps.get("pinCode");
            Account account = new AccountImpl(id, balance);

            cardNumberToAccount.put(cardNumber, account);
            String pinHash = passwordHashGenerator.generateHash(pinCode);
            cardNumberToHashPin.put(cardNumber, pinHash);
            log.info("Added account with id = {}", id);
        }

       Map<String, Object> atmProps = (Map<String, Object>) props.get("atm");
        List<CashSection> cashSectionList = new ArrayList<>(Denomination.values().length);
        for (Map<String, Object> cashSectionProps : (Iterable<Map<String, Object>>) atmProps.get("cash-sections")) {
            int val = (int) cashSectionProps.get("value");
            Denomination denomination = Objects.requireNonNull(Denomination.getByVal(val));
            long amount = ((Number) cashSectionProps.get("amount")).longValue();
            CashSection cashSection = new CashSectionImpl(denomination, amount);
            cashSectionList.add(cashSection);
        }

        PasswordValidator passwordValidator = new PasswordValidatorImpl();
        AccountRepository accountRepository = new AccountRepositoryImpl(cardNumberToAccount);
        CashSection[] cashSections = new CashSection[cashSectionList.size()];
        Atm atm = new AtmImpl(new GreedyCashWithdrawalAlgorithm(), cashSectionList.toArray(cashSections));
        AccountService accountService = new AccountService(cardNumberToHashPin,
                accountRepository, passwordValidator, atm);


        Server server = new Server(port);
        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/api");
        AccountServlet accountServlet = new AccountServlet(accountService);
        ServletHolder accountHolder = new ServletHolder(accountServlet);
        servletContextHandler.addServlet(accountHolder, "/account/*");
        FilterHolder authFilterHolder = new FilterHolder(new AuthenticationFilter(accountService));
        servletContextHandler.addFilter(authFilterHolder, "/account/*", EnumSet.of(DispatcherType.REQUEST));

        server.setHandler(servletContextHandler);
        server.start();
        server.join();
    }
}
