package com.home.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class MaintenanceTask {

    public enum Priority { LOW, MEDIUM, HIGH, URGENT }
    public enum Status   { PENDING, IN_PROGRESS, DONE, OVERDUE }
    public enum Category { PLUMBING, ELECTRICAL, HVAC, APPLIANCE, GARDEN, CLEANING, STRUCTURAL, PEST, SAFETY, OTHER }

    private String id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private Status status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate completedDate;

    private double estimatedCostUsd;
    private double actualCostUsd;
    private String contractor;        // who did the work
    private String notes;
    private boolean recurring;
    private int recurringDays;        // repeat every N days if recurring

    public MaintenanceTask() {}

    public MaintenanceTask(String id, String title, String description,
                           Category category, Priority priority, LocalDate dueDate,
                           double estimatedCostUsd, String notes,
                           boolean recurring, int recurringDays) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = Status.PENDING;
        this.dueDate = dueDate;
        this.estimatedCostUsd = estimatedCostUsd;
        this.notes = notes;
        this.recurring = recurring;
        this.recurringDays = recurringDays;
    }

    public boolean isOverdue() {
        return status != Status.DONE && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    public long daysUntilDue() {
        if (dueDate == null) return Long.MAX_VALUE;
        return LocalDate.now().until(dueDate, java.time.temporal.ChronoUnit.DAYS);
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }

    public double getEstimatedCostUsd() { return estimatedCostUsd; }
    public void setEstimatedCostUsd(double estimatedCostUsd) { this.estimatedCostUsd = estimatedCostUsd; }

    public double getActualCostUsd() { return actualCostUsd; }
    public void setActualCostUsd(double actualCostUsd) { this.actualCostUsd = actualCostUsd; }

    public String getContractor() { return contractor; }
    public void setContractor(String contractor) { this.contractor = contractor; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }

    public int getRecurringDays() { return recurringDays; }
    public void setRecurringDays(int recurringDays) { this.recurringDays = recurringDays; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | Due: %s | $%.0f",
                id, title, category, priority, dueDate, estimatedCostUsd);
    }
}
