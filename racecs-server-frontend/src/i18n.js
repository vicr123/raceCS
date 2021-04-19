import i18n from "i18next";
import {initReactI18next} from "react-i18next";
import detector from "i18next-browser-languagedetector";
import backend from "i18next-xhr-backend";

let options = {
    interpolation: {
        escapeValue: false
    },
    defaultNS: "translation"
}

let setLng = localStorage.getItem("locale");
if (setLng && setLng !== "system") options.lng = setLng;

i18n.use(detector).use(backend).use(initReactI18next).init(options);

export default i18n;