package ru.otus;

import ru.otus.exception.AtmException;
import ru.otus.model.Banknote;
import ru.otus.model.Denomination;
import ru.otus.service.AtmService;
import ru.otus.service.AtmServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final AtmService atm = new AtmServiceImpl();

    public static void main(String[] args) {
        System.out.println("===== ATM Emulator =====");

        // Initial loading of ATM
        loadInitialCash();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> depositMenu(scanner);
                    case "2" -> withdrawMenu(scanner);
                    case "3" -> atm.printBalance();
                    case "4" -> {
                        System.out.println("Shutting down ATM.");
                        return;
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            }
        }
    }

    private static void loadInitialCash() {
        List<Banknote> initialCash = new ArrayList<>();
        // Загрузим по 10 банкнок каждого номинала для демо
        for (Denomination denom : Denomination.values()) {
            for (int i = 0; i < 10; i++) {
                initialCash.add(new Banknote(denom));
            }
        }
        try {
            atm.acceptBanknotes(initialCash);
            System.out.println("ATM initialized successfully!\n");
        } catch (AtmException e) {
            System.err.println("Failed to initialize ATM: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. Deposit banknotes");
        System.out.println("2. Withdraw cash");
        System.out.println("3. Show balance");
        System.out.println("4. Exit");
        System.out.print("Choose option: ");
    }

    private static void depositMenu(Scanner scanner) {
        System.out.print("Enter banknotes (comma-separated, e.g., 100,500,1000): ");
        String input = scanner.nextLine();

        try {
            List<Banknote> banknotes = parseBanknotes(input);
            atm.acceptBanknotes(banknotes);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (AtmException e) {
            System.err.println("ATM error: " + e.getMessage());
        }
    }

    private static void withdrawMenu(Scanner scanner) {
        System.out.print("Enter amount to withdraw: ");
        try {
            int amount = Integer.parseInt(scanner.nextLine());
            List<Banknote> dispensed = atm.withdraw(amount);
            System.out.printf("Take your cash: %d₽ (%d banknotes)%n", amount, dispensed.size());
        } catch (NumberFormatException e) {
            System.err.println("Invalid amount. Please enter a number.");
        } catch (AtmException e) {
            System.err.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private static List<Banknote> parseBanknotes(String input) {
        List<Banknote> result = new ArrayList<>();
        String[] parts = input.split(",");

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            try {
                int value = Integer.parseInt(part);
                Denomination denom = Denomination.fromValue(value);
                result.add(new Banknote(denom));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number: " + part);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid denomination: " + part +
                        ". Valid denominations: 50, 100, 200, 500, 1000, 2000, 5000");
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("No valid banknotes to deposit");
        }
        return result;
    }


}
