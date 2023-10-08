const fridgeStoreBtn = document.getElementById("fridge-store-button");
const fridgeHeader = document.getElementById("fridge-header");
const storeHeader = document.getElementById("store-header");
const fridge = document.getElementById("fridge");
const inventory = document.getElementById("inventory");
const store = document.getElementById("store");
const body = document.getElementsByTagName("body")[0];
const toggleStoreBtn = document.getElementById("toggle-store-btn");
const invStoreContainer = document.getElementById("inventory-store-container");

window.addEventListener("resize", resizeWindow);
// init the window without resize event
resizeWindow();

function resizeWindow() {
    const width = window.visualViewport.width;
    const height = window.visualViewport.height;
    const isFullWidth = width > 1000;

    // things to hide when full width
    fridgeStoreBtn.classList.toggle("hidden", isFullWidth);

    // things to show when full width
    fridgeHeader.classList.toggle("hidden", !isFullWidth);
    storeHeader.classList.toggle("hidden", !isFullWidth);

    // handle body grid when full screen
    invStoreContainer.classList.toggle("dual", isFullWidth);

    // if we resize the window to large, force remove store-style
    if (isFullWidth) {
        body.classList.toggle("store-style", false);
        inventory.classList.toggle("hidden", false);
        store.classList.toggle("hidden", false);
    } else if (toggleStoreBtn.classList.contains("toggled")) {
        // if we resize the window to small, and the store button is toggled, restore store elems (add store-style to body, hide inventory)
        body.classList.toggle("store-style", true);
        inventory.classList.toggle("hidden", true);
    } else {
        store.classList.toggle("hidden", true);
    }
}