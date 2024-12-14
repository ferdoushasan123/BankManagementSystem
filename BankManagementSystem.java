import java.io.*;
import java.util.*;

class BankAccount implements Serializable {
    private String accountHolderName;
    private String accountNumber;
    private String password;
    private double balance;
    private List<String> transactionHistory;

    public BankAccount(String accountHolderName, String accountNumber, String password) {
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.password = password;
        this.balance = 0.0;
        this.transactionHistory = new ArrayList<>();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public double getBalance() {
        return balance;
    }

    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            transactionHistory.add("Deposit: " + amount);
            System.out.println("Deposit successful. New balance: " + String.format("%.2f", balance));
        } else {
            System.out.println("Invalid deposit amount. Please enter a positive value.");
        }
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            transactionHistory.add("Withdraw: " + amount);
            System.out.println("Withdrawal successful. New balance: " + String.format("%.2f", balance));
        } else if (amount > balance) {
            throw new InsufficientFundsException("Insufficient funds. Current balance: " + balance);
        } else {
            System.out.println("Invalid withdrawal amount. Please enter a positive value.");
        }
    }

    public void printTransactionHistory() {
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (String transaction : transactionHistory) {
                System.out.println(transaction);
            }
        }
    }

    public void addTransaction(String transaction) {
        transactionHistory.add(transaction);
    }
}

class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

public class BankManagementSystem {
    private static final String DATA_FILE = "accounts.dat";
    private static Map<String, BankAccount> accounts = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);
    private static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) {
        loadAccounts();
        while (true) {
            displayMainMenu();
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n========== Bank Management System ==========");
        System.out.println("1. Create Bank Account");
        System.out.println("2. Login to Account");
        System.out.println("3. Admin Login");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");

        int choice = getValidChoice(1, 4);

        switch (choice) {
            case 1:
                createAccount();
                break;
            case 2:
                loginAccount();
                break;
            case 3:
                adminLogin();
                break;
            case 4:
                saveAccounts();
                System.out.println("Thank you for using the Bank Management System. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static int getValidChoice(int min, int max) {
        int choice = -1;
        while (choice < min || choice > max) {
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice < min || choice > max) {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
        return choice;
    }

    private static void createAccount() {
        System.out.print("Enter Account Holder Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();

        if (accounts.containsKey(accountNumber)) {
            System.out.println("Account number already exists. Please try again.");
            return;
        }

        System.out.print("Set Password: ");
        String password = scanner.nextLine();

        BankAccount account = new BankAccount(name, accountNumber, password);
        accounts.put(accountNumber, account);
        System.out.println("Account created successfully.");
    }

    private static void loginAccount() {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();

        BankAccount account = accounts.get(accountNumber);
        if (account == null) {
            System.out.println("Account not found. Please try again.");
            return;
        }

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (!account.authenticate(password)) {
            System.out.println("Incorrect password. Access denied.");
            return;
        }

        manageAccount(account);
    }

    private static void manageAccount(BankAccount account) {
        while (true) {
            System.out.println("\n========== Account Menu ==========");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Check Balance");
            System.out.println("4. View Transaction History");
            System.out.println("5. Transfer Funds");
            System.out.println("6. Update Password");
            System.out.println("7. Logout");
            System.out.print("Enter your choice: ");

            int choice = getValidChoice(1, 7);

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter amount to deposit: ");
                        double depositAmount = getValidAmount();
                        account.deposit(depositAmount);
                        break;
                    case 2:
                        System.out.print("Enter amount to withdraw: ");
                        double withdrawAmount = getValidAmount();
                        account.withdraw(withdrawAmount);
                        break;
                    case 3:
                        System.out.println("Available balance: " + String.format("%.2f", account.getBalance()));
                        break;
                    case 4:
                        account.printTransactionHistory();
                        break;
                    case 5:
                        transferFunds(account);
                        break;
                    case 6:
                        updatePassword(account);
                        break;
                    case 7:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InsufficientFundsException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void transferFunds(BankAccount senderAccount) {
        System.out.print("Enter recipient's account number: ");
        String recipientAccountNumber = scanner.nextLine();
        BankAccount recipientAccount = accounts.get(recipientAccountNumber);

        if (recipientAccount == null) {
            System.out.println("Recipient account not found. Please try again.");
            return;
        }

        System.out.print("Enter amount to transfer: ");
        double transferAmount = getValidAmount();

        try {
            senderAccount.withdraw(transferAmount);
            recipientAccount.deposit(transferAmount);
            senderAccount.addTransaction("Transferred " + transferAmount + " to " + recipientAccountNumber);
            recipientAccount.addTransaction("Received " + transferAmount + " from " + senderAccount.getAccountNumber());
            System.out.println("Transfer successful.");
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void updatePassword(BankAccount account) {
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();

        if (!account.authenticate(currentPassword)) {
            System.out.println("Incorrect password. Cannot update.");
            return;
        }
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        account.setPassword(newPassword);
        System.out.println("Password updated successfully.");
    }

    private static void adminLogin() {
        System.out.print("Enter Admin Password: ");
        String password = scanner.nextLine();

        if (!ADMIN_PASSWORD.equals(password)) {
            System.out.println("Incorrect admin password. Access denied.");
            return;
        }

        manageAdmin();
    }

    private static void manageAdmin() {
        while (true) {
            System.out.println("\n========== Admin Menu ==========");
            System.out.println("1. View All Accounts");
            System.out.println("2. Delete an Account");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");

            int choice = getValidChoice(1, 3);

            switch (choice) {
                case 1:
                    viewAllAccounts();
                    break;
                case 2:
                    deleteAccount();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts to display.");
        } else {
            System.out.println("Account Holder Name | Account Number | Balance");
            for (BankAccount account : accounts.values()) {
                System.out.println(account.getAccountHolderName() + " | " +
                        account.getAccountNumber() + " | " +
                        String.format("%.2f", account.getBalance()));
            }
        }
    }

    private static void deleteAccount() {
        System.out.print("Enter the account number to delete: ");
        String accountNumber = scanner.nextLine();

        if (accounts.remove(accountNumber) != null) {
            System.out.println("Account deleted successfully.");
        } else {
            System.out.println("Account not found. Please try again.");
        }
    }

    private static double getValidAmount() {
        double amount = -1;
        while (amount <= 0) {
            try {
                amount = Double.parseDouble(scanner.nextLine());
                if (amount <= 0) {
                    System.out.println("Amount must be greater than zero. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid amount.");
            }
        }
        return amount;
    }

    private static void loadAccounts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            accounts = (Map<String, BankAccount>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No existing data found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading account data: " + e.getMessage());
        }
    }

    private static void saveAccounts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(accounts);
        } catch (IOException e) {
            System.out.println("Error saving account data: " + e.getMessage());
        }
    }
}