const _wsUri = "ws://" + window.location.host + "/Server";
let _socket = null;
let _isConnected = false;
let _requestSeq = 0;
const _requests = [];
let _onConnect;
let _onClose;
let _onError;

export function connect(funcOnConnect, funcOnClose, funcOnError) {
    if (null != _socket) {
        throw Error("Socket already initialized");
    }
    _onClose = funcOnClose;
    _onConnect = funcOnConnect;
    _onError = funcOnError;
    _socket = new WebSocket(_wsUri);
    _socket.addEventListener("open", onOpen);
    _socket.addEventListener("close", onClose);
    _socket.addEventListener("message", onMessage);
    _socket.addEventListener("error", onError);
    console.log("Initialized db socket");
}

export function close() {
    if (null == _socket) {
        throw Error("Cannot close an unopened Socket");
    }
    _socket.close();
    _socket = null;
}

export function sendStoreDataRequest(func, errorFunc) {
    sendRequest("sendStores", func, errorFunc);
}

export function sendInventoryDataRequest(func, errorFunc) {
    sendRequest("sendInventory", func, errorFunc);
}

export function requestCreateStore(storeName, func, errorFunc) {
    sendRequest("createStore", {storeName}, func, errorFunc);
}

export function requestNextSurrogateKey(key, func, errorFunc) {
    sendRequest("surrogateKey", {key}, func, errorFunc);
}

function sendRequest(data, params = {}, func = logUnhandledFunction, errorFunc = logUnhandledFunction) {
    _requests.push({requestSeq: _requestSeq, func, errorFunc});
    let paramsStr = `requestSeq=${_requestSeq++}`;
    for (const [key, value] of Object.entries(params)) {
        paramsStr += `;${key}=${value}`;
    }
    _socket.send(`${data}?${paramsStr}`);
}

function logUnhandledFunction(e) {
    console.log("WARNING: unhandled response function called: " + JSON.stringify(e));
}

function onOpen(event) {
    console.log("Opened connection: " + JSON.stringify(event));
    _isConnected = true;
    _onConnect(event);
}

function onClose(event) {
    console.log("Closed connection: " + JSON.stringify(event));
    _isConnected = false;
    _onClose(event);
}

// message {command, parameters}
function onMessage(event) {
    const message = readMessage(event.data);
    console.log(`Received message: ${message.command} :: ${JSON.stringify(message.parameters)}`);
    switch (message.command) {
        case "error":
        case "response":
            if(message.parameters.requestSeq !== undefined) {
                for (const request of _requests) {
                    if (request.requestSeq === message.parameters.requestSeq) {
                        if (message.command === "response") {
                            request.func(message.parameters);
                        } else {
                            request.errorFunc(message.parameters.value);
                        }
                    }
                }
            } else {
                console.log(`WARNING: recieved '${message.command}' with undefined 'requestSeq': ${message.parameters.value}`);
            }
            break;
        case "createStore":
            break;
        case "createInvItem":
            break;
        case "deleteStore":
            break;
        case "deleteInvItem":
            break;
    }
}

function readMessage(rawMessage) {
    const command = rawMessage.split("?", 1);
    const paramPairs = rawMessage.substr(rawMessage.indexOf("?") + 1).split(";");
    const parameters = paramPairs .reduce((prev, value) => {
        const parts = value.split("=");
        prev[parts[0]] = parts[1];
        return prev;
    }, {/* Pass new object as initial arg */});
    return {command, parameters};
}

function onError(event) {
    console.log("Encountered error on connection: " + JSON.stringify(event));
    _onError(event);
}

export function isConnected() {
    return _isConnected;
}

export function deleteItem(invItem) {}
