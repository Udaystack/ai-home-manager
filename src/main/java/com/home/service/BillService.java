package com.home.service;

import com.home.model.Bill;
import com.home.model.Bill.PaymentStatus;
import com.home.storage.StorageService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BillService {

    private final StorageService storage;

    public BillService(StorageService storage) {
        this.storage = storage;
    }

    public Bill addBill(Bill.BillType type, String provider, double amount,
                        LocalDate dueDate, boolean recurring, int cycleDays, String notes) {
        String id = "B" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Bill bill = new Bill(id, type, provider, amount, dueDate, recurring, cycleDays, notes);
        storage.addBill(bill);
        return bill;
    }

    public List<Bill> getAllBills() {
        List<Bill> bills = storage.loadBills();
        bills.forEach(b -> {
            if (b.isOverdue()) b.setPaymentStatus(PaymentStatus.OVERDUE);
        });
        bills.sort(Comparator.comparing(b -> b.getDueDate() != null ? b.getDueDate() : LocalDate.MAX));
        return bills;
    }

    public List<Bill> getUnpaidBills() {
        return getAllBills().stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.UNPAID
                          || b.getPaymentStatus() == PaymentStatus.OVERDUE)
                .collect(Collectors.toList());
    }

    public List<Bill> getUpcomingBills(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return getAllBills().stream()
                .filter(b -> b.getPaymentStatus() != PaymentStatus.PAID)
                .filter(b -> b.getDueDate() != null && !b.getDueDate().isAfter(cutoff))
                .collect(Collectors.toList());
    }

    public boolean markPaid(String id) {
        List<Bill> bills = storage.loadBills();
        for (Bill b : bills) {
            if (b.getId().equalsIgnoreCase(id)) {
                b.setPaymentStatus(PaymentStatus.PAID);
                b.setPaidDate(LocalDate.now());
                // Schedule next bill if recurring
                if (b.isRecurring() && b.getBillingCycleDays() > 0) {
                    LocalDate nextDue = LocalDate.now().plusDays(b.getBillingCycleDays());
                    String nextId = "B" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                    Bill next = new Bill(nextId, b.getBillType(), b.getProvider(),
                            b.getAmountUsd(), nextDue, true, b.getBillingCycleDays(), b.getNotes());
                    bills.add(next);
                }
                storage.saveBills(bills);
                return true;
            }
        }
        return false;
    }

    public Optional<Bill> findById(String id) {
        return storage.loadBills().stream()
                .filter(b -> b.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public boolean deleteBill(String id) { return storage.deleteBill(id); }

    public double getMonthlyTotal() {
        return storage.loadBills().stream()
                .filter(Bill::isRecurring)
                .mapToDouble(b -> b.getAmountUsd() * (30.0 / Math.max(b.getBillingCycleDays(), 1)))
                .sum();
    }

    public double getAnnualTotal() {
        return getMonthlyTotal() * 12;
    }

    public Map<Bill.BillType, Double> getSpendingByType() {
        Map<Bill.BillType, Double> map = new EnumMap<>(Bill.BillType.class);
        storage.loadBills().stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID)
                .forEach(b -> map.merge(b.getBillType(), b.getAmountUsd(), Double::sum));
        return map;
    }
}
