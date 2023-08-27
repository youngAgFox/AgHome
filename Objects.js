export class InventoryItem {
    constructor(
        name,
        quantity = 0,
        preferredAmount = 1,
        lowThreshold = 3,
        date = Date()
    ) {
        this.quantity = quantity;
        this.name = name;
        this.isSuspended = false;
        this.inventoryGroup = "ingredient";
        this.preferredAmount = preferredAmount;
        this.lowThreshold = lowThreshold;
        this.lastAdded = new Date(date);
    }
}