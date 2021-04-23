const fs = require('fs');

class Settings {
    settings;

    constructor() {
        if (fs.existsSync("data.json")) {
            this.settings = JSON.parse(fs.readFileSync("data.json", {
                encoding: "utf-8"
            }));
        } else {
            this.settings = {};
        }
    }

    write() {
        fs.writeFile("data.json", JSON.stringify(this.settings, null, 4), () => {});
    }

    set(key, value) {
        this.settings[key] = value;
        this.write();
    }

    get(key, defaultValue) {
        if (this.settings[key]) return this.settings[key];
        return defaultValue;
    }
}

module.exports = new Settings();