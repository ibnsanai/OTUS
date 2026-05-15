package ru.otus.service;

import ru.otus.model.Banknote;
import ru.otus.model.Denomination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CashBox {
    private final Denomination denomination;
    private final List<Banknote> banknotes;

    public CashBox(Denomination denomination) {
        this.denomination = denomination;
        this.banknotes = new ArrayList<>();
    }

    public void addBanknotes(List<Banknote> newBanknotes) {
        if (newBanknotes == null || newBanknotes.isEmpty()) {
            return;
        }
        for (Banknote banknote : newBanknotes) {
            if (!banknote.getDenomination().equals(denomination)) {
                throw new IllegalArgumentException(
                        String.format("Cannot add %d banknote to %d cashbox",
                                banknote.getValue(), denomination.getValue())
                );
            }
        }
        banknotes.addAll(newBanknotes);
    }

    public List<Banknote> withdrawBanknotes(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        if (count > banknotes.size()) {
            throw new IllegalArgumentException(
                    String.format("Not enough %d banknotes. Available: %d",
                            denomination.getValue(), banknotes.size())
            );
        }
        List<Banknote> withdrawn = new ArrayList<>(banknotes.subList(0, count));
        banknotes.subList(0, count).clear();
        return withdrawn;
    }

    public int getTotalValue() {
        return banknotes.size() * denomination.getValue();
    }

    public int getBanknoteCount() {
        return banknotes.size();
    }

    public Denomination getDenomination() {
        return denomination;
    }

    @Override
    public String toString() {
        return String.format("%d₽: %d шт. (итого: %d₽)",
                denomination.getValue(), banknotes.size(), getTotalValue());
    }

}
