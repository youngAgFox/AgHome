import * as Database from "/db.js";
import * as Logger from "/log.js";
// TODO add different menus / groups

const itemInputs = [];
const QUANTITY_MIN = 0;
const QUANTITY_INIT = 1;
const QUANTITY_MAX = 99;
const NAME_MAXLENGTH = 50;
let checkbox_id_seq = 1;

// store data
let stores = {};


const connectionStatusLabel = document.getElementById(
    "connection-status-label"
);
const connectionErrorImage = document.getElementById("connection-error-img");
const connectingImage = document.getElementById("connecting-img");
const connectedImage = document.getElementById("connected-img");

Database.connect(initializeFromDatabase, databaseClosed, databaseError);

function initializeFromDatabase() {
    Logger.trace("Initializing from database");
    connectingImage.classList.toggle("hidden", true);
    connectedImage.classList.toggle("hidden", false);
    connectionStatusLabel.classList.toggle("closed", false);
    connectionStatusLabel.innerText = "Connected";
    setDisabledInputs(false);
    Database.requestStoreNames(initStores, initStoresError);
}

function initStores(names) {
    Logger.info("init stores: ", names.value);
}

function initStoresError(error) {
    Logger.error("Failed to populate stores: " + error.error_msg);
}

function databaseError(error) {
    Logger.error("(Database): ", error);
}

function databaseClosed() {
    connectionErrorImage.classList.toggle("hidden", false);
    connectingImage.classList.toggle("hidden", true);
    connectionStatusLabel.classList.toggle("closed", true);
    connectionStatusLabel.innerText = "Connection Error!";

    // redisable inputs
    setDisabledInputs(true);
}

const inventory = document.getElementById("inventory");
const inventoryAddBtn = document.getElementById("inv-add");
const inventoryAddForm = document.getElementById("inv-add-form");
const inventoryFormName = document.getElementById("inv-item-name");
const inventoryFormQuantity = document.getElementById("inv-item-quantity");
const inventoryFormLastAdded = document.getElementById("inv-item-last-added");
const inventoryFormCancelBtn = document.getElementById("inv-add-form-cancel-btn");
const inventoryFormSubmitBtn = document.getElementById("inv-add-form-submit-btn");
const toggleFridgeBtn = document.getElementById("toggle-fridge-btn");
const toggleStoreBtn = document.getElementById("toggle-store-btn");
const body = document.getElementsByTagName("body")[0];
const store = document.getElementById("store");
const storeAddBtn = document.getElementById("store-add-btn");
const storeSelect = document.getElementById("store-select");
const storeAddInput = document.getElementById("store-add-input");

inventoryAddBtn.addEventListener("click", toggleAddInventoryItem);
inventoryFormCancelBtn.addEventListener("click", toggleAddInventoryItem);
inventoryFormSubmitBtn.addEventListener("click", handleAddInvSubmit);

toggleFridgeBtn.addEventListener("click", () => toggleFridgeStoreButton(toggleFridgeBtn, toggleStoreBtn));
toggleStoreBtn.addEventListener("click", () => toggleFridgeStoreButton(toggleStoreBtn, toggleFridgeBtn));
toggleStoreBtn.addEventListener("click", () => toggleStoreStyle("store"));
toggleFridgeBtn.addEventListener("click", () => toggleStoreStyle("fridge"));

storeAddBtn.addEventListener("click", () => createNewStore(storeAddInput.value));

storeSelect.addEventListener("change", loadStore);

function createStoreIfNew(data, isFromUser = false) {
    if (data.error_ind !== false) {
        Logger.error("Failed to create store: ", data.error_msg);
        return;
    }
    let storeItem;
    if (stores[id] === undefined || stores[data.id] === null) {
        Logger.info("Creating store:", data);
        storeItem = document.createElement("option");
        storeItem.value = data.id;
        storeItem.innerText = data.name;
        stores[data.id] = storeItem;
    }
    storeItem = stores[data.id];
    if (isFromUser) {
        storeItem.selected = true;
    }
    storeSelect.appendChild(storeItem);
    loadStore();
}

function loadStore() {
    const name = storeSelect.value;
    Logger.info("Loading store:", name);
}

function createStoreData(data) {
    Logger.info("Creating store data:", data);

}

function createInventoryItem(data) {
    Logger.info("Creating inventory item:", data);
    
}

Database.setHandler("create_store", (data) => createStoreIfNew(data, true));
Database.setHandler("", createStoreData);
Database.setHandler("create_inv_item", createInventoryItem);

function setDisabledInputs(isDisabled) {
    inventoryAddBtn.disabled = isDisabled;
    inventoryFormSubmitBtn.disabled = isDisabled;
    storeAddBtn.disabled = isDisabled;
    storeAddInput.disabled = isDisabled;
    storeSelect.disabled = isDisabled;
}

function getLoadedStores() {
    const stores = [];
    let storeName;
    for (const child of storeSelect.children) {
        storeName = child.innerText.trim();
        if (!storeName.isBlank()) {
            stores.push(storeName);
        } else {
            console.log("WARNING: Blank names in store Select element");
        }
    }
    console.log("Loaded stores:", stores);
    return stores;
}

function createNewStore(storeName) {
    const stores = getLoadedStores();
    if (stores.includes(storeName)) {
        alert("The stores already contains a name that resolves to '" + storeName + "'")
        return;
    }
    Database.requestCreateStore(storeName, createStoreIfNew, (errorMessage) => {
        console.log(`WARNING: Failed to create store '${storeName}': ${errorMessage}`);
    });
}

function toggleStoreStyle(btnType) {
    console.log("Toggling store style: " + btnType);
    body.classList.toggle("store-style", btnType == "store");
    store.classList.toggle("hidden", btnType == "fridge");
    inventory.classList.toggle("hidden", btnType == "store");
}

function toggleFridgeStoreButton(toggleTarget, other) {
    if (toggleTarget.classList.contains("toggled")) {
        return;
    }
    toggleTarget.classList.toggle("toggled", true);
    other.classList.toggle("toggled", false);
}

function handleAddInvSubmit() {
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

// Reads in new item parameters from form, returning object
function readNewInventoryItem() {
    const invItem = {
        name: inventoryFormName.value,
        quantity: inventoryFormQuantity.value,
        lastAdded: isBlankOrNull(inventoryFormLastAdded.value) ? new Date(Date()) : new Date(inventoryFormLastAdded.value)
    }; 
    return invItem;
}

function isBlankOrNull(str) {
    if (null === str || undefined === str) {
        return true;
    }
    str = str.trim();
    return "" === str;
}

function validateInventoryItem(invItem) {
    if (undefined === invItem || null === invItem) {
        throw new Error("Inventory item is null undefined.");
    }
    if (invItem.quantity < QUANTITY_MIN || invItem.quantity > QUANTITY_MAX) {
        throw new Error(
            `Inventory item quantity (${invItem.quantity}) out of bounds [${QUANTITY_MIN}, ${QUANTITY_MAX}].`
        );
    }
    if (null === invItem.name || invItem.name.length > NAME_MAXLENGTH) {
        throw new Error(
            `Inventory item does not have a valid filled name '${invItem.name}'.`
        );
    }
    return true;
}

function addInventoryItem(invItemParams) {
    if (invItemParams === undefined) {
        throw Error("invItemParameters are required");
    }
    Database.requestCreateInventoryItem(invItemParams, createAndAddInventoryModel, 
            e => alert("Failed to create inventory item with fields: " + JSON.stringify(invItemParams) 
            + " :: " + JSON.stringify(e)));
}

function createAndAddInventoryModel(invItem) {
    const item = document.createElement("div");
    item.classList.add("inventory-item");
    item.style = "flex-wrap: wrap;";

    const itemName = document.createElement("label");
    itemName.innerText = invItem.name;
    itemName.style = "flex-grow: 1; color: white; font-weight: bold;";
    item.appendChild(itemName);

    const secondary = document.createElement("div");
    secondary.style =
        "padding: 0; background-color: none; display: flex; flex-wrap: nowrap; align-self: stretch; gap: 10px; justify-content: center;";

    const dateLabel = document.createElement("label");
    dateLabel.innerText = formatDate(invItem.lastAdded);

    addQuantityControl(invItem, item, secondary);
    addSuspendCheckbox(invItem, item, secondary);

    const deleteButton = document.createElement("button");
    deleteButton.title = "Delete the item."
    deleteButton.classList.add("inventory-item-delete-button", "icon-button");
    deleteButton.addEventListener("click", () => deleteInvItem(item, invItem));
    secondary.appendChild(deleteButton);

    const addToStoreButton = document.createElement("button");
    addToStoreButton.title = "Add the item to the store list.";
    addToStoreButton.classList.add("inventory-item-to-store-button", "icon-button");
    addToStoreButton.addEventListener("click", () => toggleItemToStore(invItem, addToStoreButton));
    secondary.appendChild(addToStoreButton);

    item.appendChild(secondary);

    inventory.insertBefore(item, inventoryAddBtn);
}

function toggleItemToStore(invItem, addToStoreButton) {
    if (null === checkedOutStore) {
        alert("There is no checked out store list. Please check one out first.");
        return;
    }
    const isRemoving = addToStoreButton.classList.contains("toggled");
    addToStoreButton.classList.toggle("toggled");
    addToStoreButton.title = isRemoving ? "Add the item to the store list." : "Remove the item from the store list.";
    if (isRemoving) {
        
    } else {
        addStoreItem(invItem);
    }
}

function addStoreItem(invItem) {
    const item = document.createElement("div");
    item.innerText = invItem.name;
    store.appendChild(item);
}

function deleteStoreItem(storeItemDiv, invItem) {

}

function deleteInvItem(invItemDiv, invItem) {
    if (!Database.isConnected()) {
        alert("Failed to delete item - database is not connected.");
        return;
    }
    console.log("Deleting InvItem: " + JSON.stringify(invItem));
    invItemDiv.remove();
    Database.deleteItem(invItem);
}

function addSuspendCheckbox(invItem, classTarget, appendTarget) {
    const container = document.createElement("div");
    container.style = "background-color: inherit;";

    const checkboxLabel = document.createElement("label");
    const nextCheckboxId = `suspend-checkbox-${checkbox_id_seq++}`;
    checkboxLabel.setAttribute("for", nextCheckboxId);
    checkboxLabel.innerText = "Suspend";
    checkboxLabel.classList.add("inventory-item-sublabel");
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
    quant.style = "width: 3rem;";
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
        console.log("Bad date", date);
        return "?";
    }

    return `${date.getMonth() + 1}/${date.getDate()}/${date.getFullYear()}`;
}
