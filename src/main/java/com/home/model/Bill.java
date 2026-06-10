package com.home.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class Bill {

    public enum BillType { ELECTRICITY, WATER, GAS, INTERNET, INSURANCE, MORTGAGE, RENT, HOA, PHONE, SUBSCRIPTION, OTHER }
    public enum PaymentStatus { UNPAID, PAID, OVERDUE, AUTO_PAY }

    private String id;
    private BillType billType;
    private String provider;
    private double amountUsd;
    private PaymentStatus paymentStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate paidDate;

    private String notes;
    private boolean recurring;
    private int billingCycleDays;  // typically 30

    public Bill() {}

    public Bill(String id, BillType billType, String provider, double amountUsd,
                LocalDate dueDate, boolean recurring, int billingCycleDays, String notes) {
        this.id = id;
        this.billType = billType;
        this.provider = provider;
        this.amountUsd = amountUsd;
        this.dueDate = dueDate;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.recurring = recurring;
        this.billingCycleDays = billingCycleDays;
        this.notes = notes;
    }

    public boolean isOverdue() {
        return paymentStatus == PaymentStatus.UNPAID && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    public long daysUntilDue() {
        if (dueDate == null) return Long.MAX_VALUE;
        return LocalDate.now().until(dueDate, java.time.temporal.ChronoUnit.DAYS);
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public BillType getBillType() { return billType; }
    public void setBillType(BillType billType) { this.billType = billType; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public double getAmountUsd() { return amountUsd; }
    public void setAmountUsd(double amountUsd) { this.amountUsd = amountUsd; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }

    public int getBillingCycleDays() { return billingCycleDays; }
    public void setBillingCycleDays(int billingCycleDays) { this.billingCycleDays = billingCycleDays; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | $%.2f | Due: %s | %s",
                id, billType, provider, amountUsd, dueDate, paymentStatus);
    }
}
