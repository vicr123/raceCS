import i18n from "i18next";
import {initReactI18next} from "react-i18next";
import detector from "i18next-browser-languagedetector";
import backend from "i18next-xhr-backend";
import numeral from "numeral";
import "numeral/locales";

let options = {
    interpolation: {
        escapeValue: false
    },
    defaultNS: "translation"
}

let setLng = localStorage.getItem("locale");
if (setLng && setLng !== "system") options.lng = setLng;

i18n.use(detector).use(backend).use(initReactI18next).init(options);

i18n.on("initialized", (options) => {
    numeral.locale(options.lng.toLowerCase().replace("_", "-"));
});
i18n.on("languageChanged", (lng) => {
    numeral.locale(lng.toLowerCase().replace("_", "-"));
});

export default i18n;