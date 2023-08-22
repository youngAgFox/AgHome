import {DatabaseManager as DatabaseManager} from "/db.js";
// TODO add different menus / groups

const invItemElemMap = {};
const QUANTITY_MIN = 0;
const QUANTITY_INIT = 1;
const QUANTITY_MAX = 99;
const NAME_MAXLENGTH = 50;
let checkbox_id_seq = 1;

class InventoryItem {
    constructor(name, quantity = 0, preferredAmount = 1, lowThreshold = 3, date = Date()) {
        this.quantity = quantity;
        this.name = name;
        this.isSuspended = false;
        this.inventoryGroup = "ingredient";
        this.preferredAmount = preferredAmount;
        this.lowThreshold = lowThreshold;
        this.lastAdded = new Date(date);
    }
}

const connectionStatusLabel = document.getElementById("connection-status-label");
const connectionErrorImage = document.getElementById("connection-error-img");
const connectingImage = document.getElementById("connecting-img");
const connectedImage = document.getElementById("connected-img");

const databaseManager = new DatabaseManager();
initializeFromDatabase();

async function initializeFromDatabase() {
    try {
        await databaseManager.connect();
        databaseManager.sendDataRequest();
        console.log("Initialized screen");
        connectingImage.classList.toggle("hidden");
        connectedImage.classList.toggle("hidden");
        connectionStatusLabel.innerText = "Connected";
    } catch (error) {
        console.log("Failed to populate database: " + JSON.stringify(error));
        connectingImage.classList.toggle("hidden");
        connectionErrorImage.classList.toggle("hidden");
        connectionStatusLabel.innerText = "Connection Error!";
    }
}

const inventory = document.getElementById("inventory");
const inventoryAddBtn = document.getElementById("inv-add");
const inventoryAddForm = document.getElementById("inv-add-form");
const inventoryFormName = document.getElementById("inv-item-name");
const inventoryFormQuantity = document.getElementById("inv-item-quantity");
const inventoryFormLastAdded = document.getElementById("inv-item-last-added");
const inventoryFormCancelBtn = document.getElementById("inv-add-form-cancel-btn");
const inventoryFormSubmitBtn = document.getElementById("inv-add-form-submit-btn");

inventoryAddBtn.addEventListener("click", toggleAddInventoryItem);
inventoryAddForm.addEventListener("submit", readNewInventoryItem);
inventoryFormCancelBtn.addEventListener("click", toggleAddInventoryItem);
inventoryFormSubmitBtn.addEventListener("click", handleAddInvSubmit);

// TODO Remove this out once testing is complete
addInventoryItem(new InventoryItem("Test"));

function handleAddInvSubmit(e) {
    e.preventDefault();
    if (!inventoryAddForm.reportValidity()) {
        return;
    }
    try {
        addInventoryItem(readNewInventoryItem());
    } catch (error) {
        alert(error);
    }
    toggleAddInventoryItem();
}

// Toggles the add button and form
function toggleAddInventoryItem() {
    inventoryAddBtn.classList.toggle("hidden");
    inventoryAddForm.classList.toggle("hidden");
}

// Reads in new item, then adds to inventory
function readNewInventoryItem() {
    const item = new InventoryItem(inventoryFormName.value, inventoryFormQuantity.value, new Date(inventoryFormLastAdded.value));
    validateInventoryItem(item);
    return item;
}

function validateInventoryItem(invItem) {
    if (undefined === invItem || null === invItem) {
        throw new Error("Inventory item is null undefined.");
    }
    if (!(invItem instanceof InventoryItem)) {
        throw new Error("Inventory item is not of type InventoryItem.");
    }
    if (invItem.quantity < QUANTITY_MIN || invItem.quantity > QUANTITY_MAX) {
        throw new Error(`Inventory item quantity (${invItem.quantity}) out of bounds [${QUANTITY_MIN}, ${QUANTITY_MAX}].`);
    }
    if (null === invItem.name || invItem.name.length > NAME_MAXLENGTH) {
        throw new Error(`Inventory item does not have a valid filled name '${invItem.name}'.`);
    }
    return true;
}

function addInventoryItem(invItem) {
    const item = document.createElement("div");
    item.classList.add("inventory-item");
    item.style = "flex-wrap: wrap;";
    invItemElemMap.push

    const itemName = document.createElement("label");
    itemName.innerText = invItem.name;
    itemName.style = "flex-grow: 1; color: white;";
    item.appendChild(itemName);

    const secondary = document.createElement("div");
    secondary.classList.add("inventory-item");
    secondary.style = "padding: 0; background-color: none;";

    const dateLabel = document.createElement("label");
    dateLabel.innerText = formatDate(invItem.lastAdded);

    addQuantityControl(invItem, item, secondary);
    addSuspendCheckbox(invItem, item, secondary);

    const deleteButton = document.createElement("button");
    deleteButton.classList.add("inventory-item-delete-button");
    secondary.appendChild(deleteButton);


    item.appendChild(secondary);

    inventory.insertBefore(item, inventoryAddBtn);
}

function deleteInvItem(invItem) {

}

function addSuspendCheckbox(invItem, classTarget, appendTarget) {
    const container = document.createElement("div");

    const checkboxLabel = document.createElement("label");
    const nextCheckboxId = `suspend-checkbox-${checkbox_id_seq++}`;
    checkboxLabel.setAttribute("for", nextCheckboxId)
    checkboxLabel.innerText = "Suspend";
    checkboxLabel.classList.add("inventory-item-sublabel")
    container.appendChild(checkboxLabel);

    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.id = nextCheckboxId;
    container.appendChild(checkbox);

    checkbox.addEventListener("input", function toggleSuspend() {
        invItem.isSuspended = checkbox.checked;
        checkbox.classList.toggle("suspended");
        checkboxLabel.classList.toggle("suspended");
        checkboxLabel.innerText = checkbox.checked ? "Suspended" : "Suspend";
        classTarget.classList.toggle("suspended");
    });

    appendTarget.appendChild(container);
}

function addQuantityControl(invItem, classTarget, appendTarget) {
    const container = document.createElement("div");
    container.style = "display: flex; gap: 10px;";

    const lastAddedLabel = document.createElement("label");
    lastAddedLabel.innerText = "Added";
    container.appendChild(lastAddedLabel);

    const lastAddedDisplay = document.createElement("label");
    lastAddedDisplay.innerText = formatDate(invItem.lastAdded);
    container.appendChild(lastAddedDisplay);

    const label = document.createElement("label");
    label.innerText = "Qty: ";
    container.append(label);

    const quant = document.createElement("input");
    quant.type = "number";
    quant.value = invItem.quantity;
    quant.max = QUANTITY_MAX;
    quant.value = QUANTITY_INIT;
    quant.min = QUANTITY_MIN;
    quant.style = "width: 2.25rem";
    container.append(quant);

    quant.addEventListener("input", function updateQuantity() {
        invItem.quantity = quant.value;

        if (invItem.quantity <= 0) {
            classTarget.classList.toggle("out-of-stock", true);
        } else if (invItem.quantity > 0) {
            classTarget.classList.toggle("out-of-stock", false);
        }

        if (invItem.quantity > 0) {
            invItem.lastAdded = new Date(Date());
            lastAddedDisplay.innerText = formatDate(invItem.lastAdded);
        }
    });

    appendTarget.appendChild(container);
}

function formatDate(date) {
    if (date === undefined || date === null) {
        return "?";
    }

    return `${date.getMonth()}/${date.getDate()}/${date.getFullYear()}`;
}