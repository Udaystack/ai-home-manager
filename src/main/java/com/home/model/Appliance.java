package com.home.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class Appliance {

    private String id;
    private String name;             // e.g. "Samsung Refrigerator"
    private String brand;
    private String model;
    private String location;         // Kitchen, Garage, etc.

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate warrantyExpiry;

    private double purchasePriceUsd;
    private String serialNumber;
    private String notes;
    private int expectedLifespanYears;

    public Appliance() {}

    public Appliance(String id, String name, String brand, String model, String location,
                     LocalDate purchaseDate, LocalDate warrantyExpiry,
                     double purchasePriceUsd, String serialNumber,
                     int expectedLifespanYears, String notes) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.location = location;
        this.purchaseDate = purchaseDate;
        this.warrantyExpiry = warrantyExpiry;
        this.purchasePriceUsd = purchasePriceUsd;
        this.serialNumber = serialNumber;
        this.expectedLifespanYears = expectedLifespanYears;
        this.notes = notes;
    }

    public int getAgeYears() {
        if (purchaseDate == null) return 0;
        return (int) purchaseDate.until(LocalDate.now(), java.time.temporal.ChronoUnit.YEARS);
    }

    public boolean isWarrantyActive() {
        return warrantyExpiry != null && !warrantyExpiry.isBefore(LocalDate.now());
    }

    public boolean isNearEndOfLife() {
        if (purchaseDate == null || expectedLifespanYears <= 0) return false;
        int age = getAgeYears();
        return age >= (int)(expectedLifespanYears * 0.8);
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public LocalDate getWarrantyExpiry() { return warrantyExpiry; }
    public void setWarrantyExpiry(LocalDate warrantyExpiry) { this.warrantyExpiry = warrantyExpiry; }

    public double getPurchasePriceUsd() { return purchasePriceUsd; }
    public void setPurchasePriceUsd(double purchasePriceUsd) { this.purchasePriceUsd = purchasePriceUsd; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getExpectedLifespanYears() { return expectedLifespanYears; }
    public void setExpectedLifespanYears(int expectedLifespanYears) { this.expectedLifespanYears = expectedLifespanYears; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s | %s | Age: %d yrs | Warranty: %s",
                id, brand, name, location, getAgeYears(),
                isWarrantyActive() ? "Active until " + warrantyExpiry : "Expired");
    }
}
