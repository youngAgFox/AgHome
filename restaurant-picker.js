let restaurants = null;
fetch("/restaurants.json").then((response) => response.json()).then((json) => restaurants = json).catch((err) => console.log(err));

const americanType = document.getElementById("type-american");
const mexicanType = document.getElementById("type-mexican");
const japaneseType = document.getElementById("type-japanese");
const italianType = document.getElementById("type-italian");
const indianType = document.getElementById("type-indian");
const vietnameseType = document.getElementById("type-vietnamese");
const hawaiianType = document.getElementById("type-hawaiian");
const typeCheckboxes = [americanType, mexicanType, japaneseType, italianType, indianType, vietnameseType, hawaiianType];

const qualitySelectorMin = document.getElementById("quality-select-min");
const qualitySelectorMax = document.getElementById("quality-select-max");
const healthSelectorMin = document.getElementById("health-select-min");
const healthSelectorMax = document.getElementById("health-select-max");

const selectAllTypeBtn = document.getElementById("select-all-type-btn");
const deselectAllTypeBtn = document.getElementById("deselect-all-type-btn");
const chooseBtn = document.getElementById("choose-btn");

selectAllTypeBtn.addEventListener("click", () => setTypeSelected(true));
deselectAllTypeBtn.addEventListener("click", () => setTypeSelected(false));
chooseBtn.addEventListener("click", () => chooseRestaurant());

function setTypeSelected(isChecked) {
    typeCheckboxes.forEach(element => {
        element.checked = isChecked;
    });
}

function chooseRestaurant() {
    let toChooseFrom = [];
    restaurants.forEach(restaurant => {
        toChooseFrom.push(restaurant);
    });
    console.log("prefilters", toChooseFrom);
    // filter out less healthy
    toChooseFrom = toChooseFrom.filter((res) => healthIsEqualTo(res, healthSelectorMin.value, healthSelectorMax.value));
    toChooseFrom = toChooseFrom.filter((res) => qualityIsEqualTo(res, qualitySelectorMin.value, qualitySelectorMax.value));
    toChooseFrom = toChooseFrom.filter((res) => typesInclude(res));
    console.log("postfilters", toChooseFrom);
    const chosen = toChooseFrom[Math.round((Math.random() * Math.max(toChooseFrom.length - 1, 0)))];
    setChosenDisplay(chosen);
}

function healthIsEqualTo(res, minHealth, maxHealth) {
    if (res.health == undefined) {
        console.log(`WARNING: 'health' is undefined for ${res.name}`);
    }
    return minHealth <= res.health  && res.health <= maxHealth;
}

function qualityIsEqualTo(res, minQuality, maxQuality) {
    if (res.quality == undefined) {
        console.log(`WARNING: 'quality' is undefined for ${res.name}`);
    }
    return minQuality <= res.quality && res.quality <= maxQuality;
}

function typesInclude(res) {
    if (res.types == undefined) {
        console.log(`WARNING: 'types' is undefined for ${res.name}`);
        return false;
    }
    for (let i = 0; i < res.types.length; i++) {
        if (typeIsChecked(res.types[i])) {
            return true;
        }
    }
    return false;
}

function typeIsChecked(type) {
    switch (type) {
        case "american": return americanType.checked;
        case "mexican": return mexicanType.checked;
        case "indian": return indianType.checked;
        case "italian": return italianType.checked;
        case "japanese": return japaneseType.checked;
        case "vietnamese": return vietnameseType.checked;
        case "hawaiian": return hawaiianType.checked;
        default: return false;
    }
}

const chosenResturantLabel = document.getElementById("chosen-restaurant-label");
const qualityLabel = document.getElementById("quality-label");
const typesLabel = document.getElementById("types-label");
const healthLabel = document.getElementById("health-label");
const foodsLabel = document.getElementById("foods-label");

function setChosenDisplay(res) {
    chosenResturantLabel.innerText = res?.name ?? "None";
    qualityLabel.innerText = qualityToString(res?.quality);
    healthLabel.innerText = healthToString(res?.health);
    if (res?.types !== undefined) {
        typesLabel.innerText = res.types.reduce(reduceCommaDelimStrings, null) ?? "None";
    } else {
        typesLabel.innerText = "Undefined";
    }
    if (res["food-types"] !== undefined) {
        foodsLabel.innerText = res["food-types"].reduce(reduceCommaDelimStrings, null) ?? "None";
    } else {
        foodsLabel.innerText = "Undefined";
    }
}

function capitalize(str) {
    if (str === undefined || str === null || str.length <= 0) {
        return str;
    }
    let c = str.charAt(0);
    return c.toUpperCase() + str.slice(1);
}

function reduceCommaDelimStrings(prev, cur) {
        if (prev === undefined || prev === null) {
            if (cur === undefined || cur === null) {
                return "None";
            }
            return capitalize(cur);
        }
        return capitalize(prev) + ", " + capitalize(cur);
}

function qualityToString(quality) {
    switch (quality) {
        case 1: return "Fast (1)";
        case 2: return "Low (2)";
        case 3: return "Medium (3)";
        case 4: return "High (4)";
        case 5: return "Fine (5)";
        default: return `None (${quality})`;
    }
}

function healthToString(health) {
    switch (health) {
        case 1: return "Unhealthy (1)";
        case 2: return "Bad (2)";
        case 3: return "Ok (3)";
        case 4: return "Good (4)";
        case 5: return "Healthy (5)";
        default: return `None (${health})`;
    }
}