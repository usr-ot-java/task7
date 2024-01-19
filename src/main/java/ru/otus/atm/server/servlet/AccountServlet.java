package ru.otus.atm.server.servlet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.atm.Denomination;
import ru.otus.atm.account.Account;
import ru.otus.atm.server.servlet.dto.*;
import ru.otus.atm.service.AccountService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@AllArgsConstructor
@WebServlet
@Slf4j
public class AccountServlet extends HttpServlet {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ACCOUNT_BALANCE_PATH = "/balance";
    private static final String ACCOUNT_DEPOSIT_PATH = "/deposit";
    private static final String ACCOUNT_WITHDRAW_PATH = "/withdraw";

    private final AccountService accountService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleGetRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handlePostRequest(req, resp);
    }

    private void handleGetRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Account account = Objects.requireNonNull((Account) req.getAttribute("account"));
        String path = req.getPathInfo();
        switch (path) {
            case ACCOUNT_BALANCE_PATH -> {
                BigInteger balance = accountService.getBalance(account);
                String jsonResponse = serializeJsonResponse(new AccountBalanceResponse(balance));
                resp.getWriter().println(jsonResponse);
            }
        }
        resp.setHeader("Content-Type", "application/json");
    }

    private void handlePostRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Account account = Objects.requireNonNull((Account) req.getAttribute("account"));
        String path = req.getPathInfo();
        switch (path) {
            case ACCOUNT_DEPOSIT_PATH -> {
                String body = req.getReader().lines().collect(Collectors.joining());
                AccountDepositRequest depositRequest = deserializeJsonBody(body, AccountDepositRequest.class);
                if (depositRequest == null) {
                    badRequest(resp);
                    return;
                }
                Map<Denomination, Long> cash = new HashMap<>();
                for (var cashEntry : depositRequest.getCash().entrySet()) {
                    Denomination denomination = Denomination.getByVal(cashEntry.getKey());
                    if (denomination == null) {
                        badRequest(resp);
                        return;
                    }
                    cash.put(denomination, cashEntry.getValue());
                }
                boolean result = accountService.depositMoney(account, cash);
                String jsonResponse = serializeJsonResponse(new AccountDepositResponse(result));
                resp.getWriter().println(jsonResponse);
            }
            case ACCOUNT_WITHDRAW_PATH -> {
                String body = req.getReader().lines().collect(Collectors.joining());
                AccountWithdrawRequest accountWithdrawRequest = deserializeJsonBody(body, AccountWithdrawRequest.class);
                boolean result = accountService.withdrawMoney(account, accountWithdrawRequest.getAmount());
                String jsonResponse = serializeJsonResponse(new AccountWithdrawResponse(result));
                resp.getWriter().println(jsonResponse);
            }
        }
        resp.setHeader("Content-Type", "application/json");
    }

    private String serializeJsonResponse(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response to JSON", e);
            throw new RuntimeException("Failed to serialize response to JSON", e);
        }
    }

    private <T> T deserializeJsonBody(String json, Class<T> cls) {
        try {
            return objectMapper.readValue(json, cls);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize the following class {} from JSON `{}`",
                    cls.getSimpleName(), json, e);
        }
        return null;
    }

    private void badRequest(HttpServletResponse resp) {
        resp.setStatus(400);
    }
}
