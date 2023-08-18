const wsUri = "ws://" + window.location.host + "/Serve";
export class DatabaseManager {
    constructor() {
        this.socket = null;
    }

    connect() {
        if (null != this.socket) {
            throw Error("Socket already initialized");
        }
        this.socket = new WebSocket(wsUri);
        this.socket.addEventListener("open", this.onOpen);
        this.socket.addEventListener("close", this.onClose);
        this.socket.addEventListener("message", this.onMessage);
        this.socket.addEventListener("error", this.onError);
        console.log("Initialized db socket");

        const socket = this.socket;
        return new Promise(function connectPromise(resolve, reject) {
            socket.addEventListener("open", function resolveOnOpen() {
                resolve();
            });
            socket.addEventListener("close", function resolveOnClose(err) {
                reject(err);
            });
        });
    }

    close() {
        if (null == this.socket) {
            throw Error("Cannot close an unopened Socket");
        }
        this.socket.close();
        this.socket = null;
    }

    sendDataRequest() {
        this.socket.send("dataRequest");
    }

    onOpen(event) {
        console.log("Opened connection: " + JSON.stringify(event));
    }

    onClose(event) {
        console.log("Closed connection: " + JSON.stringify(event));
    }

    onMessage(event) {
        console.log("Received message: " + event.data);
    }

    onError(event) {
        console.log(
            "Encountered error on connection: " + JSON.stringify(event)
        );
    }
}