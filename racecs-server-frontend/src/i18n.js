import i18n from "i18next";
import {initReactI18next} from "react-i18next";
import detector from "i18next-browser-languagedetector";
import backend from "i18next-xhr-backend";

i18n.use(detector).use(backend).use(initReactI18next).init({
    interpolation: {
        escapeValue: false
    },
    defaultNS: "translation"
});

export default i18n;