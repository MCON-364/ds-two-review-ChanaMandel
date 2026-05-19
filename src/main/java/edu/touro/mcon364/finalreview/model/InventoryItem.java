package edu.touro.mcon364.finalreview.model;

public record InventoryItem(String sku, String category, int quantity, int reorderLevel) {
    public InventoryItem {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("sku is required");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("category is required");
        if (quantity < 0) throw new IllegalArgumentException("quantity cannot be negative");
        if (reorderLevel < 0) throw new IllegalArgumentException("reorderLevel cannot be negative");
    }
}
