package com.home.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.home.model.Appliance;
import com.home.model.Bill;
import com.home.model.HomeProfile;
import com.home.model.MaintenanceTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StorageService {

    private static final String DATA_DIR      = System.getProperty("user.home") + "/.home-manager";
    private static final String TASKS_FILE    = DATA_DIR + "/tasks.json";
    private static final String BILLS_FILE    = DATA_DIR + "/bills.json";
    private static final String APPLIANCES_FILE = DATA_DIR + "/appliances.json";
    private static final String PROFILE_FILE  = DATA_DIR + "/profile.json";

    private final ObjectMapper mapper;

    public StorageService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        new File(DATA_DIR).mkdirs();
    }

    // ── Tasks ─────────────────────────────────────────────────────────────────

    public List<MaintenanceTask> loadTasks() {
        return loadList(TASKS_FILE, MaintenanceTask[].class);
    }

    public void saveTasks(List<MaintenanceTask> tasks) {
        saveList(TASKS_FILE, tasks);
    }

    public void addTask(MaintenanceTask task) {
        List<MaintenanceTask> tasks = loadTasks();
        tasks.add(task);
        saveTasks(tasks);
    }

    public boolean deleteTask(String id) {
        List<MaintenanceTask> tasks = loadTasks();
        boolean removed = tasks.removeIf(t -> t.getId().equalsIgnoreCase(id));
        if (removed) saveTasks(tasks);
        return removed;
    }

    // ── Bills ─────────────────────────────────────────────────────────────────

    public List<Bill> loadBills() {
        return loadList(BILLS_FILE, Bill[].class);
    }

    public void saveBills(List<Bill> bills) {
        saveList(BILLS_FILE, bills);
    }

    public void addBill(Bill bill) {
        List<Bill> bills = loadBills();
        bills.add(bill);
        saveBills(bills);
    }

    public boolean deleteBill(String id) {
        List<Bill> bills = loadBills();
        boolean removed = bills.removeIf(b -> b.getId().equalsIgnoreCase(id));
        if (removed) saveBills(bills);
        return removed;
    }

    // ── Appliances ────────────────────────────────────────────────────────────

    public List<Appliance> loadAppliances() {
        return loadList(APPLIANCES_FILE, Appliance[].class);
    }

    public void saveAppliances(List<Appliance> appliances) {
        saveList(APPLIANCES_FILE, appliances);
    }

    public void addAppliance(Appliance appliance) {
        List<Appliance> appliances = loadAppliances();
        appliances.add(appliance);
        saveAppliances(appliances);
    }

    public boolean deleteAppliance(String id) {
        List<Appliance> appliances = loadAppliances();
        boolean removed = appliances.removeIf(a -> a.getId().equalsIgnoreCase(id));
        if (removed) saveAppliances(appliances);
        return removed;
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    public HomeProfile loadProfile() {
        File file = new File(PROFILE_FILE);
        if (!file.exists()) return null;
        try { return mapper.readValue(file, HomeProfile.class); }
        catch (IOException e) { return null; }
    }

    public void saveProfile(HomeProfile profile) {
        try { mapper.writeValue(new File(PROFILE_FILE), profile); }
        catch (IOException e) { throw new RuntimeException("Failed to save profile", e); }
    }

    public String getDataDirectory() { return DATA_DIR; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private <T> List<T> loadList(String filePath, Class<T[]> clazz) {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();
        try {
            T[] arr = mapper.readValue(file, clazz);
            return new ArrayList<>(Arrays.asList(arr));
        } catch (IOException e) {
            System.err.println("Warning: could not load " + filePath + " — " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private <T> void saveList(String filePath, List<T> items) {
        try { mapper.writeValue(new File(filePath), items); }
        catch (IOException e) { throw new RuntimeException("Failed to save " + filePath, e); }
    }
}
