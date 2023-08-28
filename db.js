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

export function sendDataRequest() {
    sendRequest("dataRequest");
}

export function requestCreateStore(storeName) {
    sendRequest(`createStore:"${storeName}"`);
}

export function requestNextSurrogateKey(keyName, func) {
    sendRequest("surrogateKey", `keyName=${keyName}`, func);
}

function sendRequest(data, params, func) {
    _requests.push({requestSeq: _requestSeq, func});
    _socket.send(`${data}?${params};requestSeq=${_requestSeq++}`);
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

function onMessage(event) {
    console.log("Received message: " + event.data);
}

function onError(event) {
    console.log("Encountered error on connection: " + JSON.stringify(event));
    _onError(event);
}

export function isConnected() {
    return _isConnected;
}

export function deleteItem(invItem) {}
