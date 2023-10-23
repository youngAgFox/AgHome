const _wsUri = "ws://" + window.location.host + "/Server";
const _is_date_field_regex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/;
let _socket = null;
let _isConnected = false;
let _request_seq = 0;
let _requests = [];
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

export function requestStoreData(name, func, errorFunc) {
    sendRequest("get_store_data", {name}, func, errorFunc);
}

export function requestStoreNames(func, errorFunc) {
    sendRequest("get_all_store", func, errorFunc);
}

export function requestShelfNames(func, errorFunc) {
    sendRequest("get_all_shelf", func, errorFunc);
}

export function requestShelfData(name, func, errorFunc) {
    sendRequest("get_all_shelf_inv_item", {name}, func, errorFunc);
}

export function requestCreateStore(name, func, errorFunc) {
    sendRequest("create_store", {name}, func, errorFunc);
}

export function requestCreateInventoryItem(invItem, func, errorFunc) {
    sendRequest("create_inv_item", invItem, func, errorFunc);
}

export function requestDeleteItem(invItem, func, errorFunc) {

}

function sendRequest(data, params = {}, func = logUnhandledFunction, errorFunc = logUnhandledFunction) {
    _requests.push({request_seq: _request_seq, func, errorFunc});
    let paramsStr = `request_seq=${_request_seq++}`;
    for (const [key, value] of Object.entries(params)) {
        if (value instanceof Date) {
            paramsStr += `;${key}=${serializeDate(value)}`;
        } else {
            paramsStr += `;${key}=${value}`;
        }
    }
    const request = `${data}?${paramsStr}`;
    console.log("Sending", request);
    _socket.send(request);
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
    console.log(`Received message: '${message.command}' :: ${JSON.stringify(message.parameters)}`);
    // toString() is necessary here since switch statments use strict comparison
    switch (message.command.toString()) {
        case "response":
            if(message.parameters.request_seq !== undefined) {
                const ongoingRequests = [];
                for (const request of _requests) {
                    if (request.request_seq.toString() === message.parameters.request_seq.toString()) {
                        if (message.parameters.error_ind.toString() === "false") {
                            request.func(deserializeParameters(message.parameters));
                        } else {
                            request.errorFunc(deserializeParameters(message.parameters));
                        }
                    } else {
                        ongoingRequests.push(request);
                    }
                }
                _requests = ongoingRequests;
            } else {
                console.log(`WARNING: recieved '${message.command}' with undefined 'request_seq': ${deserializeParameters(message.parameters)}`);
            }
            break;
        case "create_store":
        case "create_inv_item":
        case "delete_store":
        case "delete_inv_item":
            console.log("Recieved message with command: '" + message.command + "'");
            break;
        default: console.log("Unrecognized command: '" + message.command + "' :: " + JSON.stringify(message.parameters));
            break;
    }
}

function deserializeParameters(params) {
    for (const [key, value] of Object.entries(params)) {
        if (isDateField(value.toString())) {
            params[key] = new Date(value.toString());
        }
    }
    return params;
}

function isDateField(field) {
    return _is_date_field_regex.test(field);
}

function serializeDate(date) {
    const year = `${date.getFullYear()}`.padStart(2, "0");
    const month = `${date.getMonth() + 1}`.padStart(2, "0");
    const day = `${date.getDate()}`.padStart(2, "0");
    const hours = `${date.getHours()}`.padStart(2, "0");
    const minutes = `${date.getMinutes()}`.padStart(2, "0");
    const seconds = `${date.getSeconds()}`.padStart(2, "0");
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
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

