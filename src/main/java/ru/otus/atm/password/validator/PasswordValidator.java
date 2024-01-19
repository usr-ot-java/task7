package ru.otus.atm.password.validator;

public interface PasswordValidator {
    boolean validatePassword(String original, String hash);
}
