import java.io.*;
import java.time.*;
import java.util.*;

public class expense_tracker {

    static class Expense implements Serializable {
        private String category;
        private double amount;
        private LocalDate date;

        public Expense(String category, double amount, LocalDate date) {
            this.category = category;
            setAmount(amount);
            this.date = date;
        }

        public String getCategory() { return category; }
        public LocalDate getDate() { return date; }
        public double getAmount() { return amount; }

        public void setAmount(double amount) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
            this.amount = amount;
        }

        public String displaySummary() {
            return "Category: " + category + ", Amount: Rs" + amount + ", Date: " + date;
        }
    }

    static class OneTimeExpense extends Expense {
        public OneTimeExpense(String category, double amount, LocalDate date) {
            super(category, amount, date);
        }

        @Override
        public String displaySummary() {
            return super.displaySummary() + " | Type: One-Time";
        }
    }

    static class RecurringExpense extends Expense {
        private LocalDate nextDueDate;

        public RecurringExpense(String category, double amount, LocalDate date, LocalDate nextDueDate) {
            super(category, amount, date);
            this.nextDueDate = nextDueDate;
        }

        @Override
        public String displaySummary() {
            return super.displaySummary() + " | Next Due: " + nextDueDate;
        }
    }

    static class ExpenseManager {
        private List<Expense> expenses = new ArrayList<Expense>();

        public void addExpense(Expense e) { expenses.add(e); }

        public void displayAll() {
            if (expenses.isEmpty()) {
                System.out.println("No expenses found.");
                return;
            }
            for (Expense e : expenses) System.out.println(e.displaySummary());
        }

        public void filterByCategory(String category) {
            boolean found = false;
            for (Expense e : expenses) {
                if (e.getCategory().equalsIgnoreCase(category)) {
                    System.out.println(e.displaySummary());
                    found = true;
                }
            }
            if (!found) System.out.println("No expenses found for " + category);
        }

        public void saveToFile(String filename) throws IOException {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
            try {
                oos.writeObject(expenses);
            } finally {
                oos.close();
            }
        }

        @SuppressWarnings("unchecked")
        public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
            File file = new File(filename);
            if (!file.exists()) return;
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
            try {
                expenses = (List<Expense>) ois.readObject();
            } finally {
                ois.close();
            }
        }

        public List<Expense> getExpenses() { return expenses; }
    }

    static class ReportGenerator {
        public void monthlyReport(List<Expense> expenses) {
            if (expenses.isEmpty()) {
                System.out.println("No data for report.");
                return;
            }
            Map<Month, Double> monthTotals = new TreeMap<Month, Double>();
            for (Expense e : expenses) {
                Month m = e.getDate().getMonth();
                Double prev = monthTotals.get(m);
                if (prev == null) prev = 0.0;
                monthTotals.put(m, prev + e.getAmount());
            }
            System.out.println("\nMONTHLY REPORT");
            for (Map.Entry<Month, Double> entry : monthTotals.entrySet()) {
                System.out.printf("%-10s : Rs%.2f%n", entry.getKey(), entry.getValue());
            }
        }

        public void categoryReport(List<Expense> expenses) {
            if (expenses.isEmpty()) {
                System.out.println("No data for report.");
                return;
            }
            Map<String, Double> categoryTotals = new TreeMap<String, Double>();
            for (Expense e : expenses) {
                String c = e.getCategory();
                Double prev = categoryTotals.get(c);
                if (prev == null) prev = 0.0;
                categoryTotals.put(c, prev + e.getAmount());
            }
            System.out.println("\nCATEGORY REPORT");
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                System.out.printf("%-15s : Rs%.2f%n", entry.getKey(), entry.getValue());
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ExpenseManager manager = new ExpenseManager();
        ReportGenerator report = new ReportGenerator();
        String filename = "expenses.dat";

        try { manager.loadFromFile(filename); }
        catch (Exception e) { System.out.println("Starting fresh — no previous data file."); }

        while (true) {
            System.out.println("\n-------------------------------");
            System.out.println("PERSONAL EXPENSE TRACKER");
            System.out.println("-------------------------------");
            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. Filter by Category");
            System.out.println("4. Monthly Report");
            System.out.println("5. Category Report");
            System.out.println("6. Save & Exit");
            System.out.print("Enter choice: ");

            int choice;
            try { choice = sc.nextInt(); sc.nextLine(); }
            catch (Exception e) { System.out.println("Invalid input! Enter a number."); sc.nextLine(); continue; }

            try {
                switch (choice) {
                    case 1: {
                        System.out.print("Enter category: ");
                        String cat = sc.nextLine();
                        System.out.print("Enter amount: ");
                        double amt = sc.nextDouble(); sc.nextLine();
                        System.out.print("Enter date (yyyy-mm-dd): ");
                        LocalDate date = LocalDate.parse(sc.nextLine());
                        System.out.print("Is it recurring? (y/n): ");
                        String recurring = sc.nextLine();
                        Expense e;
                        if (recurring.equalsIgnoreCase("y")) {
                            System.out.print("Enter next due date (yyyy-mm-dd): ");
                            LocalDate next = LocalDate.parse(sc.nextLine());
                            e = new RecurringExpense(cat, amt, date, next);
                        } else {
                            e = new OneTimeExpense(cat, amt, date);
                        }
                        manager.addExpense(e);
                        System.out.println("Expense added successfully!");
                        break;
                    }
                    case 2:
                        manager.displayAll();
                        break;
                    case 3:
                        System.out.print("Enter category to filter: ");
                        manager.filterByCategory(sc.nextLine());
                        break;
                    case 4:
                        report.monthlyReport(manager.getExpenses());
                        break;
                    case 5:
                        report.categoryReport(manager.getExpenses());
                        break;
                    case 6:
                        manager.saveToFile(filename);
                        System.out.println("Expenses saved. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice!");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
