package ru.otus.service;

import ru.otus.exception.AtmException;
import ru.otus.model.Banknote;

import java.util.List;

public interface AtmService {
    void acceptBanknotes(List<Banknote> banknotes) throws AtmException;
    List<Banknote> withdraw(int amount) throws AtmException;
    int getBalance();
    void printBalance();
}
