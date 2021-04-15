class Wsh {
    ws;

    handlers = {};

    constructor(url) {
        this.ws = new WebSocket(`wss://${window.location.host}/ws`);
        this.ws.onmessage = this.messageHandler.bind(this);
        this.ws.onopen = this.openHandler.bind(this);
        this.ws.onclose = this.closeHandler.bind(this);
        this.ws.onerror = this.closeHandler.bind(this);
    }

    messageHandler(data) {
        this.emit("message", [
            JSON.parse(data.data)
        ]);
    }

    openHandler() {
        this.emit("open", []);
    }

    closeHandler() {
        this.emit("close", []);
    }

    emit(event, args) {
        if (!this.handlers[event]) this.handlers[event] = [];

        for (let handler of this.handlers[event]) {
            handler(...args);
        }
    }

    on(event, handler) {
        if (!this.handlers[event]) this.handlers[event] = [];
        this.handlers[event].push(handler);
    }
}

export default Wsh;