package com.home.service;

import com.home.model.MaintenanceTask;
import com.home.model.MaintenanceTask.Priority;
import com.home.model.MaintenanceTask.Status;
import com.home.storage.StorageService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskService {

    private final StorageService storage;

    public TaskService(StorageService storage) {
        this.storage = storage;
    }

    public MaintenanceTask addTask(String title, String description,
                                   MaintenanceTask.Category category, Priority priority,
                                   LocalDate dueDate, double estimatedCost,
                                   String notes, boolean recurring, int recurringDays) {
        String id = generateId();
        MaintenanceTask task = new MaintenanceTask(id, title, description, category,
                priority, dueDate, estimatedCost, notes, recurring, recurringDays);
        storage.addTask(task);
        return task;
    }

    public List<MaintenanceTask> getAllTasks() {
        List<MaintenanceTask> tasks = storage.loadTasks();
        // Auto-mark overdue
        tasks.forEach(t -> {
            if (t.isOverdue() && t.getStatus() == Status.PENDING) {
                t.setStatus(Status.OVERDUE);
            }
        });
        // Sort: URGENT → HIGH → MEDIUM → LOW, then by due date
        tasks.sort(Comparator
                .comparing((MaintenanceTask t) -> t.getStatus() == Status.DONE ? 1 : 0)
                .thenComparing(t -> priorityOrder(t.getPriority()))
                .thenComparing(t -> t.getDueDate() != null ? t.getDueDate() : LocalDate.MAX));
        return tasks;
    }

    public List<MaintenanceTask> getPendingTasks() {
        return getAllTasks().stream()
                .filter(t -> t.getStatus() != Status.DONE)
                .collect(Collectors.toList());
    }

    public List<MaintenanceTask> getOverdueTasks() {
        return getAllTasks().stream()
                .filter(MaintenanceTask::isOverdue)
                .collect(Collectors.toList());
    }

    public List<MaintenanceTask> getUpcomingTasks(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return getAllTasks().stream()
                .filter(t -> t.getStatus() != Status.DONE)
                .filter(t -> t.getDueDate() != null && !t.getDueDate().isAfter(cutoff))
                .collect(Collectors.toList());
    }

    public boolean markComplete(String id, double actualCost, String contractor) {
        List<MaintenanceTask> tasks = storage.loadTasks();
        for (MaintenanceTask t : tasks) {
            if (t.getId().equalsIgnoreCase(id)) {
                t.setStatus(Status.DONE);
                t.setCompletedDate(LocalDate.now());
                t.setActualCostUsd(actualCost);
                if (contractor != null && !contractor.isBlank()) t.setContractor(contractor);
                // Schedule next occurrence for recurring tasks
                if (t.isRecurring() && t.getRecurringDays() > 0) {
                    LocalDate nextDue = LocalDate.now().plusDays(t.getRecurringDays());
                    MaintenanceTask next = new MaintenanceTask(
                            generateId(), t.getTitle(), t.getDescription(),
                            t.getCategory(), t.getPriority(), nextDue,
                            t.getEstimatedCostUsd(), t.getNotes(), true, t.getRecurringDays());
                    tasks.add(next);
                }
                storage.saveTasks(tasks);
                return true;
            }
        }
        return false;
    }

    public Optional<MaintenanceTask> findById(String id) {
        return storage.loadTasks().stream()
                .filter(t -> t.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public boolean deleteTask(String id) { return storage.deleteTask(id); }

    public double getTotalEstimatedCost() {
        return storage.loadTasks().stream()
                .filter(t -> t.getStatus() != Status.DONE)
                .mapToDouble(MaintenanceTask::getEstimatedCostUsd)
                .sum();
    }

    public double getTotalActualCostThisYear() {
        int year = LocalDate.now().getYear();
        return storage.loadTasks().stream()
                .filter(t -> t.getStatus() == Status.DONE)
                .filter(t -> t.getCompletedDate() != null && t.getCompletedDate().getYear() == year)
                .mapToDouble(MaintenanceTask::getActualCostUsd)
                .sum();
    }

    private int priorityOrder(Priority p) {
        return switch (p) {
            case URGENT -> 0; case HIGH -> 1; case MEDIUM -> 2; case LOW -> 3;
        };
    }

    private String generateId() {
        return "T" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
