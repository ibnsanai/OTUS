package ru.otus.service;

import ru.otus.exception.AtmException;
import ru.otus.model.Banknote;
import ru.otus.model.Denomination;

import java.util.*;
import java.util.stream.Collectors;

public class AtmServiceImpl implements AtmService{
    private final Map<Denomination, CashBox> cashBoxes;

    public AtmServiceImpl() {
        this.cashBoxes = new HashMap<>();
        for (Denomination denom : Denomination.values()) {
            cashBoxes.put(denom, new CashBox(denom));
        }
    }

    @Override
    public void acceptBanknotes(List<Banknote> banknotes) throws AtmException {
        if (banknotes == null || banknotes.isEmpty()) {
            throw new AtmException("Cannot accept empty banknote list");
        }

        Map<Denomination, List<Banknote>> grouped = banknotes.stream()
                .collect(Collectors.groupingBy(Banknote::getDenomination));

        for (Map.Entry<Denomination, List<Banknote>> entry : grouped.entrySet()) {
            CashBox cashBox = cashBoxes.get(entry.getKey());
            cashBox.addBanknotes(entry.getValue());
        }

        System.out.printf("Accepted %d banknotes. Current balance: %d₽%n",
                banknotes.size(), getBalance());
    }

    @Override
    public List<Banknote> withdraw(int amount) throws AtmException {
        if (amount <= 0) {
            throw new AtmException("Withdrawal amount must be positive");
        }

        if (amount > getBalance()) {
            throw new AtmException(
                    String.format("Insufficient funds. Available: %d₽, requested: %d₽",
                            getBalance(), amount)
            );
        }

        Map<Denomination, Integer> toWithdraw = calculateOptimalWithdrawal(amount);
        if (toWithdraw == null) {
            throw new AtmException(
                    String.format("Cannot dispense exact amount %d₽ with current banknote composition", amount)
            );
        }

        System.out.println("Denomination and number of banknotes" + toWithdraw);

        List<Banknote> result = new ArrayList<>();
        for (Map.Entry<Denomination, Integer> entry : toWithdraw.entrySet()) {
            CashBox cashBox = cashBoxes.get(entry.getKey());
            List<Banknote> withdrawn = cashBox.withdrawBanknotes(entry.getValue());
            result.addAll(withdrawn);
        }

        System.out.printf("Dispensed %d₽ using %d banknotes.%n", amount, result.size());
        return result;
    }

    private Map<Denomination, Integer> calculateOptimalWithdrawal(int amount) {
        // Simple greedy approach from largest to smallest
        Map<Denomination, Integer> result = new HashMap<>();
        int remaining = amount;

        // Get denominations sorted descending
        List<Denomination> descending = Arrays.stream(Denomination.values())
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .toList();

        for (Denomination denom : descending) {
            CashBox cashBox = cashBoxes.get(denom);
            int available = cashBox.getBanknoteCount();
            int maxNeeded = remaining / denom.getValue();
            int toTake = Math.min(maxNeeded, available);

            if (toTake > 0) {
                result.put(denom, toTake);
                remaining -= toTake * denom.getValue();
            }
        }

        return remaining == 0 ? result : null;
    }

    @Override
    public int getBalance() {
        return cashBoxes.values().stream()
                .mapToInt(CashBox::getTotalValue)
                .sum();
    }

    @Override
    public void printBalance() {
        System.out.println("\n========== ATM STATUS ==========");
        System.out.printf("Total balance: %d₽%n%n", getBalance());
        for (Denomination denom : Denomination.values()) {
            CashBox cashBox = cashBoxes.get(denom);
            System.out.println(cashBox);
        }
        System.out.println("================================\n");
    }
}
