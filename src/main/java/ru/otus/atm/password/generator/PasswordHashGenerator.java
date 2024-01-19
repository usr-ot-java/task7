package ru.otus.atm.password.generator;

public interface PasswordHashGenerator {
    String generateHash(String password);
}
