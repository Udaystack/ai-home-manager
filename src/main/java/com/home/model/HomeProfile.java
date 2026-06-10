package com.home.model;

public class HomeProfile {

    private String address;
    private String homeType;          // House, Apartment, Condo, Townhouse
    private int builtYear;
    private double squareFootage;
    private int bedrooms;
    private int bathrooms;
    private String roofType;          // Shingle, Tile, Metal, Flat
    private int roofAgeYears;
    private String heatingType;       // Gas, Electric, Heat Pump
    private String coolingType;       // Central AC, Mini-split, Window Units
    private boolean hasBasement;
    private boolean hasGarage;
    private boolean hasPool;
    private String notes;

    public HomeProfile() {}

    public int getHomeAgeYears() {
        if (builtYear <= 0) return 0;
        return java.time.LocalDate.now().getYear() - builtYear;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getHomeType() { return homeType; }
    public void setHomeType(String homeType) { this.homeType = homeType; }

    public int getBuiltYear() { return builtYear; }
    public void setBuiltYear(int builtYear) { this.builtYear = builtYear; }

    public double getSquareFootage() { return squareFootage; }
    public void setSquareFootage(double squareFootage) { this.squareFootage = squareFootage; }

    public int getBedrooms() { return bedrooms; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }

    public int getBathrooms() { return bathrooms; }
    public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }

    public String getRoofType() { return roofType; }
    public void setRoofType(String roofType) { this.roofType = roofType; }

    public int getRoofAgeYears() { return roofAgeYears; }
    public void setRoofAgeYears(int roofAgeYears) { this.roofAgeYears = roofAgeYears; }

    public String getHeatingType() { return heatingType; }
    public void setHeatingType(String heatingType) { this.heatingType = heatingType; }

    public String getCoolingType() { return coolingType; }
    public void setCoolingType(String coolingType) { this.coolingType = coolingType; }

    public boolean isHasBasement() { return hasBasement; }
    public void setHasBasement(boolean hasBasement) { this.hasBasement = hasBasement; }

    public boolean isHasGarage() { return hasGarage; }
    public void setHasGarage(boolean hasGarage) { this.hasGarage = hasGarage; }

    public boolean isHasPool() { return hasPool; }
    public void setHasPool(boolean hasPool) { this.hasPool = hasPool; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return String.format("%s | %s built %d | %.0f sqft | %dBR/%dBA | Roof: %s (%d yrs old)",
                address, homeType, builtYear, squareFootage, bedrooms, bathrooms,
                roofType, roofAgeYears);
    }
}
