package ru.otus.atm.server.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.atm.account.Account;
import ru.otus.atm.exception.AccountNotFoundException;
import ru.otus.atm.exception.AuthenticationFailedException;
import ru.otus.atm.service.AccountService;
import ru.otus.atm.service.AuthenticationData;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class AuthenticationFilter implements Filter {
    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_START_PREFIX = "Atm:";
    private final AccountService accountService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        Account account = getAccount(req);
        if (account == null) {
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.setStatus(401);
            return;
        }
        request.setAttribute("account", account);
        chain.doFilter(request, response);
    }

    private Account getAccount(HttpServletRequest req) {
        AuthenticationData authenticationData = extractAuthenticationData(req);
        if (authenticationData == null) {
            return null;
        }
        return authenticate(authenticationData);
    }

    private AuthenticationData extractAuthenticationData(HttpServletRequest req) {
        String header = req.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(AUTH_START_PREFIX)) {
            return null;
        }
        header = header.substring(AUTH_START_PREFIX.length() + 1).trim();
        if (!header.contains(":")) {
            return null;
        }
        String cardNumber = header.substring(0, header.indexOf(":"));
        String pinCode = header.substring(header.indexOf(":") + 1);
        return new AuthenticationData(cardNumber, pinCode);
    }

    private Account authenticate(AuthenticationData authenticationData) {
        try {
            return accountService.authenticate(authenticationData);
        } catch (AuthenticationFailedException | AccountNotFoundException e) {
            log.error("Failed to authenticate account with card number {}", authenticationData.getCardNumber(), e);
        }
        return null;
    }

}
