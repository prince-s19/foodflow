package models;

/**
 * Enumeration representing the sequential lifecycle states of an order in the processing pipeline.
 * Transitions:
 * PENDING -> PREPARING -> PREPARED -> PACKAGING -> PACKAGED -> DELIVERING -> DELIVERED
 */
public enum OrderStatus {
    PENDING("PENDING", "Waiting in Kitchen Queue", "#E0E0E0"),
    PREPARING("PREPARING", "Chef is cooking food", "#0066FF"),
    PREPARED("PREPARED", "Food cooked, ready for packaging", "#66B2FF"),
    PACKAGING("PACKAGING", "Packaging and boxing items", "#FFD700"),
    PACKAGED("PACKAGED", "Boxed and bagged, ready for courier", "#FFE866"),
    DELIVERING("DELIVERING", "Courier on the way to customer", "#00CC66"),
    DELIVERED("DELIVERED", "Order successfully delivered", "#00FF66"),
    CANCELLED("CANCELLED", "Order cancelled", "#FF3333");

    private final String displayName;
    private final String description;
    private final String hexColor;

    OrderStatus(String displayName, String description, String hexColor) {
        this.displayName = displayName;
        this.description = description;
        this.hexColor = hexColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getHexColor() {
        return hexColor;
    }

    public boolean isInKitchen() {
        return this == PENDING || this == PREPARING || this == PREPARED;
    }

    public boolean isInPackaging() {
        return this == PACKAGING || this == PACKAGED;
    }

    public boolean isInDelivery() {
        return this == DELIVERING;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
}
