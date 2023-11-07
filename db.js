import * as Logger from "/log.js";

const _wsUri = "ws://" + window.location.host + "/Server";
const _is_date_field_regex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/;
let _socket = null;
let _isConnected = false;
let _request_seq = 0;
let _requests = [];
let _handlers = {};
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
    Logger.trace("Initialized db socket");
}

export function close() {
    if (null == _socket) {
        throw Error("Cannot close an unopened Socket");
    }
    _socket.close();
    _socket = null;
    Logger.trace("Manually closing socket");
}

export function setHandler(key, func) {
    _handlers[key] = func;
}

export function requestStoreData(name, func, errorFunc) {
    sendRequest("get_store_data", {name}, func, errorFunc);
}

export function requestStoreNames(func, errorFunc) {
    sendRequest("get_all_store", {}, func, errorFunc);
}

export function requestShelfNames(func, errorFunc) {
    sendRequest("get_all_shelf", {}, func, errorFunc);
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

function sendRequest(command, args = {}, func = logUnhandledFunction, errorFunc = logUnhandledFunction) {
    Logger.debug("Args:", command, args, func, errorFunc);
    _requests.push({request_seq: _request_seq, func, errorFunc});
    for (const [key, value] of Object.entries(args)) {
        args[key] = serialize(value);
    }
    args['request_seq'] = _request_seq++;
    args['command'] = command;
    const request = JSON.stringify(args);
    Logger.debug(args, " -> ", request);
    _socket.send(request);
}

function logUnhandledFunction(e) {
    console.warn("Unhandled response function called: ", JSON.stringify(e));
}

function onOpen(event) {
    _isConnected = true;
    _onConnect(event);
    Logger.info("Opened connection: ", JSON.stringify(event));
}

function onClose(event) {
    _isConnected = false;
    _onClose(event);
    Logger.warn("Closed connection: ", JSON.stringify(event));
}

// message {command, parameters}
function onMessage(event) {
    let rawMessage;
    try {
        rawMessage = JSON.parse(event.data);
    } catch (error) {
        Logger.error("Error parsing event.data json", event);
        return;
    }
    Logger.debug(`Message received:`, rawMessage);
    const message = deserializeParameters(rawMessage);
    Logger.debug(`Message deserialized:`, rawMessage);

    switch (message.command) {
        case "response":
            if(message.request_seq !== undefined) {
                const ongoingRequests = [];
                for (const request of _requests) {
                    if (request.request_seq === message.request_seq) {
                        if (message.error_ind === false) {
                            request.func(deserializeParameters(message));
                        } else {
                            request.errorFunc(deserializeParameters(message));
                        }
                    } else {
                        ongoingRequests.push(request);
                    }
                }
                _requests = ongoingRequests;
            } else {
                Logger.error(`Received '${message.command}' with undefined 'request_seq': ${deserializeParameters(message)}`);
            }
            break;
        default: 
            if (message.command !== undefined && message.command !== null
                && _handlers[message.command] !== undefined && _handlers[message.command] !== null) {
                    _handlers[message.command](message);
            } else {
                Logger.error(`Received unregistered command '${message.command}'`, message);
            }
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

function serialize(object) {
    if (object instanceof Date) {
        return serializeDate(object);
    }
    return  object.toString();
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

function onError(event) {
    console.log("Encountered error on connection: " + JSON.stringify(event));
    _onError(event);
}

export function isConnected() {
    return _isConnected;
}

