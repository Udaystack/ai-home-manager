package com.home.cli;

import com.home.model.*;
import com.home.model.MaintenanceTask.*;
import com.home.service.*;
import com.home.storage.StorageService;
import com.home.util.ConsoleUI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class HomeManagerApp {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Scanner      scanner;
    private final StorageService storage;
    private final TaskService  taskService;
    private final BillService  billService;
    private AIService          aiService;

    public HomeManagerApp() {
        this.scanner      = new Scanner(System.in);
        this.storage      = new StorageService();
        this.taskService  = new TaskService(storage);
        this.billService  = new BillService(storage);
    }

    public static void main(String[] args) {
        ConsoleUI.init();
        new HomeManagerApp().run();
        ConsoleUI.shutdown();
    }

    public void run() {
        ConsoleUI.printBanner();
        setupApiKey();
        ensureProfile();
        showDashboard();

        boolean running = true;
        while (running) {
            ConsoleUI.printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1"  -> addTask();
                case "2"  -> viewTasks();
                case "3"  -> completeTask();
                case "4"  -> addBill();
                case "5"  -> viewBills();
                case "6"  -> payBill();
                case "7"  -> addAppliance();
                case "8"  -> viewAppliances();
                case "9"  -> homeHealthReport();
                case "10" -> seasonalChecklist();
                case "11" -> diagnoseProblem();
                case "12" -> analyzeBills();
                case "13" -> askQuestion();
                case "14" -> manageProfile();
                case "15" -> showDashboard();
                case "0"  -> { ConsoleUI.printInfo("Take good care of your home! Goodbye."); running = false; }
                default   -> ConsoleUI.printWarning("Invalid choice. Enter 0–15.");
            }
        }
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void setupApiKey() {
        String key = System.getenv("ANTHROPIC_API_KEY");
        if (key == null || key.isBlank()) {
            ConsoleUI.printWarning("ANTHROPIC_API_KEY not set.");
            System.out.print("Enter Anthropic API key (or Enter to skip AI features): ");
            key = scanner.nextLine().trim();
        }
        if (!key.isBlank()) {
            aiService = new AIService(key);
            ConsoleUI.printSuccess("AI features enabled.");
        } else {
            ConsoleUI.printWarning("Running without AI features.");
        }
    }

    private void ensureProfile() {
        if (storage.loadProfile() == null) {
            ConsoleUI.printInfo("No home profile found. Let's set it up!");
            manageProfile();
        } else {
            ConsoleUI.printSuccess("Welcome back! Home loaded: " + storage.loadProfile().getAddress());
        }
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    private void showDashboard() {
        int pending   = taskService.getPendingTasks().size();
        int overdue   = taskService.getOverdueTasks().size();
        int unpaid    = billService.getUnpaidBills().size();
        double billsDue = billService.getUpcomingBills(14).stream()
                .mapToDouble(Bill::getAmountUsd).sum();
        long eol = storage.loadAppliances().stream().filter(Appliance::isNearEndOfLife).count();
        double monthly = billService.getMonthlyTotal();
        ConsoleUI.printDashboard(pending, overdue, unpaid, billsDue, (int) eol, monthly);
    }

    // ── Tasks ─────────────────────────────────────────────────────────────────

    private void addTask() {
        ConsoleUI.printHeader("ADD MAINTENANCE TASK");

        System.out.println("Categories: " + Arrays.toString(Category.values()));
        System.out.print("Category [OTHER]: ");
        Category cat = parseEnum(Category.class, scanner.nextLine().trim(), Category.OTHER);

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (title.isBlank()) { ConsoleUI.printError("Title required."); return; }

        System.out.print("Description (optional): ");
        String desc = scanner.nextLine().trim();

        System.out.println("Priorities: " + Arrays.toString(Priority.values()));
        System.out.print("Priority [MEDIUM]: ");
        Priority priority = parseEnum(Priority.class, scanner.nextLine().trim(), Priority.MEDIUM);

        System.out.print("Due date (yyyy-MM-dd) [today]: ");
        LocalDate due = readDateOrToday();

        System.out.print("Estimated cost $ [0]: ");
        double cost = readDoubleOrDefault(0);

        System.out.print("Recurring task? (yes/no) [no]: ");
        boolean recurring = "yes".equalsIgnoreCase(scanner.nextLine().trim());
        int recurDays = 0;
        if (recurring) {
            System.out.print("Repeat every how many days? [365]: ");
            recurDays = readIntOrDefault(365);
        }

        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();

        MaintenanceTask task = taskService.addTask(title, desc, cat, priority, due, cost,
                notes.isBlank() ? null : notes, recurring, recurDays);
        ConsoleUI.printSuccess("Task saved! ID: " + task.getId());
    }

    private void viewTasks() {
        ConsoleUI.printHeader("ALL MAINTENANCE TASKS");
        List<MaintenanceTask> tasks = taskService.getAllTasks();
        if (tasks.isEmpty()) { ConsoleUI.printInfo("No tasks yet."); return; }

        System.out.printf("  %-8s %-22s %-8s %-10s %s%n", "ID", "TITLE", "PRIORITY", "DUE", "COST");
        System.out.println("  " + "─".repeat(62));
        for (MaintenanceTask t : tasks) {
            ConsoleUI.printTaskRow(t.getId(), t.getTitle(), t.getPriority().name(),
                    t.getStatus().name(),
                    t.getDueDate() != null ? t.getDueDate().toString() : "—",
                    t.getEstimatedCostUsd());
        }
        System.out.printf("%n  Pending cost: $%.2f  |  Spent this year: $%.2f%n",
                taskService.getTotalEstimatedCost(), taskService.getTotalActualCostThisYear());

        System.out.print("\nEnter task ID for details (or Enter to go back): ");
        String id = scanner.nextLine().trim();
        if (!id.isBlank()) taskService.findById(id).ifPresentOrElse(
                this::printTaskDetail, () -> ConsoleUI.printError("Not found: " + id));
    }

    private void printTaskDetail(MaintenanceTask t) {
        ConsoleUI.printHeader("TASK — " + t.getId());
        System.out.println("Title       : " + t.getTitle());
        System.out.println("Category    : " + t.getCategory());
        System.out.println("Priority    : " + t.getPriority());
        System.out.println("Status      : " + t.getStatus());
        System.out.println("Due         : " + t.getDueDate());
        System.out.println("Est. Cost   : $" + t.getEstimatedCostUsd());
        System.out.println("Actual Cost : $" + t.getActualCostUsd());
        System.out.println("Contractor  : " + (t.getContractor() != null ? t.getContractor() : "—"));
        System.out.println("Recurring   : " + (t.isRecurring() ? "Every " + t.getRecurringDays() + " days" : "No"));
        System.out.println("Description : " + (t.getDescription() != null ? t.getDescription() : "—"));
        System.out.println("Notes       : " + (t.getNotes() != null ? t.getNotes() : "—"));
    }

    private void completeTask() {
        viewTasks();
        System.out.print("Enter task ID to mark complete: ");
        String id = scanner.nextLine().trim();
        System.out.print("Actual cost $ [0]: ");
        double cost = readDoubleOrDefault(0);
        System.out.print("Contractor name (optional): ");
        String contractor = scanner.nextLine().trim();
        if (taskService.markComplete(id, cost, contractor))
            ConsoleUI.printSuccess("Task marked complete!" + (taskService.findById(id).map(t -> t.isRecurring() ? " Next occurrence scheduled." : "").orElse("")));
        else ConsoleUI.printError("Task not found.");
    }

    // ── Bills ─────────────────────────────────────────────────────────────────

    private void addBill() {
        ConsoleUI.printHeader("ADD BILL");
        System.out.println("Types: " + Arrays.toString(Bill.BillType.values()));
        System.out.print("Bill type [OTHER]: ");
        Bill.BillType type = parseEnum(Bill.BillType.class, scanner.nextLine().trim(), Bill.BillType.OTHER);

        System.out.print("Provider/Company: ");
        String provider = scanner.nextLine().trim();

        System.out.print("Amount $: ");
        double amount = readDoubleOrDefault(0);

        System.out.print("Due date (yyyy-MM-dd) [today]: ");
        LocalDate due = readDateOrToday();

        System.out.print("Recurring? (yes/no) [yes]: ");
        boolean recurring = !"no".equalsIgnoreCase(scanner.nextLine().trim());
        int cycle = 30;
        if (recurring) {
            System.out.print("Billing cycle in days [30]: ");
            cycle = readIntOrDefault(30);
        }

        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();

        Bill bill = billService.addBill(type, provider, amount, due, recurring, cycle, notes.isBlank() ? null : notes);
        ConsoleUI.printSuccess("Bill saved! ID: " + bill.getId());
    }

    private void viewBills() {
        ConsoleUI.printHeader("ALL BILLS");
        List<Bill> bills = billService.getAllBills();
        if (bills.isEmpty()) { ConsoleUI.printInfo("No bills yet."); return; }

        System.out.printf("  %-8s %-14s %-16s %-10s %-12s %s%n",
                "ID", "TYPE", "PROVIDER", "AMOUNT", "STATUS", "DUE");
        System.out.println("  " + "─".repeat(70));
        bills.forEach(b -> ConsoleUI.printBillRow(b.getId(), b.getBillType().name(),
                b.getProvider(), b.getAmountUsd(), b.getPaymentStatus().name(),
                b.getDueDate() != null ? b.getDueDate().toString() : "—"));
        System.out.printf("%n  Estimated monthly total: $%.2f  |  Annual: $%.2f%n",
                billService.getMonthlyTotal(), billService.getAnnualTotal());
    }

    private void payBill() {
        viewBills();
        System.out.print("Enter bill ID to mark paid: ");
        String id = scanner.nextLine().trim();
        if (billService.markPaid(id)) ConsoleUI.printSuccess("Bill marked as paid!");
        else ConsoleUI.printError("Bill not found.");
    }

    // ── Appliances ────────────────────────────────────────────────────────────

    private void addAppliance() {
        ConsoleUI.printHeader("ADD APPLIANCE");
        System.out.print("Name (e.g. Refrigerator): "); String name = scanner.nextLine().trim();
        System.out.print("Brand: ");  String brand  = scanner.nextLine().trim();
        System.out.print("Model: ");  String model  = scanner.nextLine().trim();
        System.out.print("Location (e.g. Kitchen): "); String loc = scanner.nextLine().trim();
        System.out.print("Purchase date (yyyy-MM-dd) [unknown]: ");
        String dateStr = scanner.nextLine().trim();
        LocalDate purchaseDate = null;
        if (!dateStr.isBlank()) { try { purchaseDate = LocalDate.parse(dateStr, DATE_FMT); } catch (DateTimeParseException ignored) {} }
        System.out.print("Warranty expiry (yyyy-MM-dd) [none]: ");
        String warStr = scanner.nextLine().trim();
        LocalDate warranty = null;
        if (!warStr.isBlank()) { try { warranty = LocalDate.parse(warStr, DATE_FMT); } catch (DateTimeParseException ignored) {} }
        System.out.print("Purchase price $ [0]: "); double price = readDoubleOrDefault(0);
        System.out.print("Serial number (optional): "); String serial = scanner.nextLine().trim();
        System.out.print("Expected lifespan years [10]: "); int life = readIntOrDefault(10);
        System.out.print("Notes (optional): "); String notes = scanner.nextLine().trim();

        String id = "A" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Appliance a = new Appliance(id, name, brand, model, loc, purchaseDate, warranty,
                price, serial.isBlank() ? null : serial, life, notes.isBlank() ? null : notes);
        storage.addAppliance(a);
        ConsoleUI.printSuccess("Appliance saved! ID: " + id);
    }

    private void viewAppliances() {
        ConsoleUI.printHeader("APPLIANCES");
        List<Appliance> list = storage.loadAppliances();
        if (list.isEmpty()) { ConsoleUI.printInfo("No appliances yet."); return; }
        list.forEach(a -> {
            String eol = a.isNearEndOfLife() ? " ⚠ NEAR END OF LIFE" : "";
            String war = a.isWarrantyActive() ? " ✔ WARRANTY ACTIVE" : "";
            System.out.println("  " + a + eol + war);
        });
    }

    // ── AI Features ───────────────────────────────────────────────────────────

    private void homeHealthReport() {
        if (!checkAI()) return;
        ConsoleUI.printInfo("Generating full home health report... (this may take a moment)");
        try {
            String result = aiService.generateHomeHealthReport(
                    storage.loadProfile(), taskService.getAllTasks(),
                    billService.getAllBills(), storage.loadAppliances());
            ConsoleUI.printAIResponse(result);
        } catch (Exception e) { ConsoleUI.printError("AI error: " + e.getMessage()); }
    }

    private void seasonalChecklist() {
        if (!checkAI()) return;
        ConsoleUI.printInfo("Generating seasonal maintenance checklist...");
        try {
            String result = aiService.generateSeasonalChecklist(storage.loadProfile());
            ConsoleUI.printAIResponse(result);
        } catch (Exception e) { ConsoleUI.printError("AI error: " + e.getMessage()); }
    }

    private void diagnoseProblem() {
        if (!checkAI()) return;
        System.out.print("Describe the problem: ");
        String problem = scanner.nextLine().trim();
        if (problem.isBlank()) return;
        ConsoleUI.printInfo("Diagnosing...");
        try {
            String result = aiService.diagnoseProblem(storage.loadProfile(), problem);
            ConsoleUI.printAIResponse(result);
        } catch (Exception e) { ConsoleUI.printError("AI error: " + e.getMessage()); }
    }

    private void analyzeBills() {
        if (!checkAI()) return;
        ConsoleUI.printInfo("Analyzing bill spending...");
        try {
            String result = aiService.analyzeBillSpending(storage.loadProfile(), billService.getAllBills());
            ConsoleUI.printAIResponse(result);
        } catch (Exception e) { ConsoleUI.printError("AI error: " + e.getMessage()); }
    }

    private void askQuestion() {
        if (!checkAI()) return;
        System.out.print("Your home question: ");
        String q = scanner.nextLine().trim();
        if (q.isBlank()) return;
        ConsoleUI.printInfo("Consulting AI...");
        try {
            String result = aiService.askHomeQuestion(storage.loadProfile(),
                    taskService.getAllTasks(), billService.getAllBills(),
                    storage.loadAppliances(), q);
            ConsoleUI.printAIResponse(result);
        } catch (Exception e) { ConsoleUI.printError("AI error: " + e.getMessage()); }
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    private void manageProfile() {
        ConsoleUI.printHeader("HOME PROFILE");
        HomeProfile p = storage.loadProfile();

        System.out.print("Address" + def(p, p != null ? p.getAddress() : null) + ": ");
        String address = readOrDefault(p != null ? p.getAddress() : "");

        System.out.print("Home type (House/Apartment/Condo/Townhouse)" + def(p, p != null ? p.getHomeType() : null) + ": ");
        String type = readOrDefault(p != null ? p.getHomeType() : "House");

        System.out.print("Year built" + def(p, p != null ? String.valueOf(p.getBuiltYear()) : null) + ": ");
        int built = readIntOrDefault(p != null ? p.getBuiltYear() : 2000);

        System.out.print("Square footage" + def(p, p != null ? String.valueOf(p.getSquareFootage()) : null) + ": ");
        double sqft = readDoubleOrDefault(p != null ? p.getSquareFootage() : 0);

        System.out.print("Bedrooms [" + (p != null ? p.getBedrooms() : 3) + "]: ");
        int beds = readIntOrDefault(p != null ? p.getBedrooms() : 3);

        System.out.print("Bathrooms [" + (p != null ? p.getBathrooms() : 2) + "]: ");
        int baths = readIntOrDefault(p != null ? p.getBathrooms() : 2);

        System.out.print("Roof type (Shingle/Tile/Metal/Flat) [Shingle]: ");
        String roof = readOrDefault(p != null ? p.getRoofType() : "Shingle");

        System.out.print("Roof age in years [0]: ");
        int roofAge = readIntOrDefault(p != null ? p.getRoofAgeYears() : 0);

        System.out.print("Heating (Gas/Electric/Heat Pump) [Gas]: ");
        String heating = readOrDefault(p != null ? p.getHeatingType() : "Gas");

        System.out.print("Cooling (Central AC/Mini-split/Window Units) [Central AC]: ");
        String cooling = readOrDefault(p != null ? p.getCoolingType() : "Central AC");

        System.out.print("Has basement? (yes/no) [no]: ");
        boolean basement = "yes".equalsIgnoreCase(scanner.nextLine().trim());

        System.out.print("Has garage? (yes/no) [no]: ");
        boolean garage = "yes".equalsIgnoreCase(scanner.nextLine().trim());

        System.out.print("Has pool? (yes/no) [no]: ");
        boolean pool = "yes".equalsIgnoreCase(scanner.nextLine().trim());

        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();

        HomeProfile profile = new HomeProfile();
        profile.setAddress(address); profile.setHomeType(type); profile.setBuiltYear(built);
        profile.setSquareFootage(sqft); profile.setBedrooms(beds); profile.setBathrooms(baths);
        profile.setRoofType(roof); profile.setRoofAgeYears(roofAge);
        profile.setHeatingType(heating); profile.setCoolingType(cooling);
        profile.setHasBasement(basement); profile.setHasGarage(garage); profile.setHasPool(pool);
        profile.setNotes(notes.isBlank() ? null : notes);
        storage.saveProfile(profile);
        ConsoleUI.printSuccess("Home profile saved! Age: " + profile.getHomeAgeYears() + " years");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean checkAI() {
        if (aiService == null) {
            ConsoleUI.printError("AI features not enabled. Restart and provide your API key.");
            return false;
        }
        return true;
    }

    private String readOrDefault(String def) {
        String v = scanner.nextLine().trim(); return v.isBlank() ? def : v;
    }
    private int readIntOrDefault(int def) {
        String v = scanner.nextLine().trim();
        if (v.isBlank()) return def;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return def; }
    }
    private double readDoubleOrDefault(double def) {
        String v = scanner.nextLine().trim();
        if (v.isBlank()) return def;
        try { return Double.parseDouble(v); } catch (NumberFormatException e) { return def; }
    }
    private LocalDate readDateOrToday() {
        String v = scanner.nextLine().trim();
        if (v.isBlank()) return LocalDate.now();
        try { return LocalDate.parse(v, DATE_FMT); }
        catch (DateTimeParseException e) { ConsoleUI.printWarning("Invalid date, using today."); return LocalDate.now(); }
    }
    private <T extends Enum<T>> T parseEnum(Class<T> clazz, String val, T def) {
        if (val == null || val.isBlank()) return def;
        try { return Enum.valueOf(clazz, val.toUpperCase()); }
        catch (IllegalArgumentException e) { return def; }
    }
    private String def(Object existing, String val) {
        return (existing != null && val != null) ? " [" + val + "]" : "";
    }
}
