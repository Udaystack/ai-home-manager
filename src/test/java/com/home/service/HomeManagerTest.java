package com.home.service;

import com.home.model.*;
import com.home.model.MaintenanceTask.*;
import com.home.storage.StorageService;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HomeManagerTest {

    private static StorageService storage;
    private static TaskService    taskService;
    private static BillService    billService;
    private static String         taskId;
    private static String         billId;

    @BeforeAll
    static void setup() {
        System.setProperty("user.home", System.getProperty("java.io.tmpdir") + "/home-test");
        storage     = new StorageService();
        taskService = new TaskService(storage);
        billService = new BillService(storage);
    }

    // ── Task Tests ────────────────────────────────────────────────────────────

    @Test @Order(1)
    void testAddTask() {
        MaintenanceTask t = taskService.addTask(
                "Replace HVAC filter", "Replace 16x20x1 filter",
                Category.HVAC, Priority.HIGH,
                LocalDate.now().plusDays(7), 25.0, "Check every 3 months", true, 90);
        assertNotNull(t.getId());
        assertEquals("Replace HVAC filter", t.getTitle());
        assertEquals(Status.PENDING, t.getStatus());
        taskId = t.getId();
    }

    @Test @Order(2)
    void testGetAllTasks() {
        List<MaintenanceTask> tasks = taskService.getAllTasks();
        assertFalse(tasks.isEmpty());
    }

    @Test @Order(3)
    void testGetPendingTasks() {
        List<MaintenanceTask> pending = taskService.getPendingTasks();
        assertTrue(pending.stream().noneMatch(t -> t.getStatus() == Status.DONE));
    }

    @Test @Order(4)
    void testOverdueDetection() {
        // Add an overdue task
        taskService.addTask("Check smoke detectors", null, Category.SAFETY,
                Priority.URGENT, LocalDate.now().minusDays(5), 0, null, false, 0);
        List<MaintenanceTask> overdue = taskService.getOverdueTasks();
        assertFalse(overdue.isEmpty(), "Should have at least one overdue task");
    }

    @Test @Order(5)
    void testMarkComplete_createsNextRecurrence() {
        int countBefore = taskService.getAllTasks().size();
        boolean done = taskService.markComplete(taskId, 22.50, "Self");
        assertTrue(done);
        // Recurring task should create a new one
        int countAfter = taskService.getAllTasks().size();
        assertEquals(countBefore + 1, countAfter, "Recurring task should spawn a new task");
    }

    @Test @Order(6)
    void testDeleteTask() {
        assertTrue(taskService.deleteTask(taskId));
        assertFalse(taskService.findById(taskId).isPresent());
    }

    @Test @Order(7)
    void testCostCalculations() {
        taskService.addTask("Roof inspection", null, Category.STRUCTURAL,
                Priority.MEDIUM, LocalDate.now().plusDays(30), 350.0, null, false, 0);
        double cost = taskService.getTotalEstimatedCost();
        assertTrue(cost > 0, "Should have pending costs");
    }

    // ── Bill Tests ────────────────────────────────────────────────────────────

    @Test @Order(8)
    void testAddBill() {
        Bill b = billService.addBill(Bill.BillType.ELECTRICITY, "Austin Energy",
                145.50, LocalDate.now().plusDays(10), true, 30, "Auto-pay enrolled");
        assertNotNull(b.getId());
        assertEquals(Bill.PaymentStatus.UNPAID, b.getPaymentStatus());
        billId = b.getId();
    }

    @Test @Order(9)
    void testMarkPaid_createsNextBill() {
        int countBefore = billService.getAllBills().size();
        assertTrue(billService.markPaid(billId));
        int countAfter = billService.getAllBills().size();
        assertEquals(countBefore + 1, countAfter, "Recurring bill should spawn next cycle");
    }

    @Test @Order(10)
    void testMonthlyTotal() {
        double monthly = billService.getMonthlyTotal();
        assertTrue(monthly > 0, "Monthly total should be positive");
    }

    @Test @Order(11)
    void testUpcomingBills() {
        billService.addBill(Bill.BillType.WATER, "City Water",
                55.00, LocalDate.now().plusDays(5), true, 30, null);
        List<Bill> upcoming = billService.getUpcomingBills(14);
        assertFalse(upcoming.isEmpty());
    }

    // ── Appliance Tests ───────────────────────────────────────────────────────

    @Test @Order(12)
    void testApplianceAgeAndEOL() {
        Appliance old = new Appliance("A001", "Refrigerator", "GE", "GSS25GSHSS",
                "Kitchen", LocalDate.now().minusYears(12),
                LocalDate.now().minusYears(9), 1200.0, "SN123456", 13, null);
        assertTrue(old.getAgeYears() >= 12);
        assertTrue(old.isNearEndOfLife(), "12-year-old appliance with 13yr lifespan should be near EOL");
        assertFalse(old.isWarrantyActive(), "Warranty expired 9 years ago");
    }

    @Test @Order(13)
    void testApplianceWarrantyActive() {
        Appliance newApp = new Appliance("A002", "Dishwasher", "Bosch", "SHPM88Z75N",
                "Kitchen", LocalDate.now().minusYears(1),
                LocalDate.now().plusYears(2), 900.0, null, 12, null);
        assertTrue(newApp.isWarrantyActive());
        assertFalse(newApp.isNearEndOfLife());
    }

    // ── Profile Tests ─────────────────────────────────────────────────────────

    @Test @Order(14)
    void testHomeProfileAge() {
        HomeProfile profile = new HomeProfile();
        profile.setBuiltYear(1995);
        assertTrue(profile.getHomeAgeYears() >= 29);
    }
}
