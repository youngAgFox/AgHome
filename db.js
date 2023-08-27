const _wsUri = "ws://" + window.location.host + "/Server";
let _socket = null;
let _isConnected = false;

export function connect() {
    if (null != _socket) {
        throw Error("Socket already initialized");
    }
    _socket = new WebSocket(_wsUri);
    _socket.addEventListener("open", onOpen);
    _socket.addEventListener("close", onClose);
    _socket.addEventListener("message", onMessage);
    _socket.addEventListener("error", onError);
    console.log("Initialized db socket");

    return new Promise(function connectPromise(resolve, reject) {
        _socket.addEventListener("open", function resolveOnOpen() {
            resolve();
        });
        _socket.addEventListener("close", function resolveOnClose(err) {
            reject(err);
        });
    });
}

export function close() {
    if (null == _socket) {
        throw Error("Cannot close an unopened Socket");
    }
    _socket.close();
    _socket = null;
}

export function sendDataRequest() {
    _socket.send("dataRequest");
}

export function requestCreateStore(storeName) {
    _socket.send(`createStore:"${storeName}"`);
}

function onOpen(event) {
    console.log("Opened connection: " + JSON.stringify(event));
    _isConnected = true;
}

function onClose(event) {
    console.log("Closed connection: " + JSON.stringify(event));
    _isConnected = false;
}

function onMessage(event) {
    console.log("Received message: " + event.data);
}

function onError(event) {
    console.log("Encountered error on connection: " + JSON.stringify(event));
}

export function isConnected() {
    return _isConnected;
}

export function deleteItem(invItem) {}

export function addEventListener(eventType, func) {
    if (null === _socket) {
        throw Error("Cannot add event listeners before initializing database");
    }
    _socket.addEventListener(eventType, func);
}
