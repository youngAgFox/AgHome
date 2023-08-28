import * as Database from "/db.js";

const INVENTORY_ITEM_SK = "INV_ITM_SK";

class InventoryItem {
    constructor(
        id,
        name,
        quantity = 0,
        preferredAmount = 1,
        lowThreshold = 3,
        date = Date()
    ) {
        this.id = id;
        this.quantity = quantity;
        this.name = name;
        this.isSuspended = false;
        this.inventoryGroup = "ingredient";
        this.preferredAmount = preferredAmount;
        this.lowThreshold = lowThreshold;
        this.lastAdded = new Date(date);
    }
}

export function createInvItem(func, args = {}) {
    if (undefined === args || undefined === args.name || null === args.name) {
        throw new Error("'name' is required");
    }
    Database.requestNextSurrogateKey(INVENTORY_ITEM_SK, (key) =>
        func(
            new InventoryItem(
                key,
                name,
                args.quantity ?? 1,
                args.preferredAmount ?? 1,
                args.lowThreshold ?? 3,
                args.date ?? Date()
            )
        )
    );
}
