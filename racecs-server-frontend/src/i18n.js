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
    fallbackLng: "en",
    defaultNS: "translation"
}

let setLng = localStorage.getItem("locale");
if (setLng && setLng !== "system") options.lng = setLng;

i18n.use(detector).use(backend).use(initReactI18next).init(options);

let getLocale = (locale) => {
    let l = "en";
    if (locale) {
        l = locale.toLowerCase().replace("_", "-");
    }
    if (l === "nl") l = "nl-nl";
    if (l === "gt") l = "en";

    return l;
}

i18n.on("initialized", (options) => {
    numeral.locale(getLocale(options.lng));

    document.title = i18n.t("PAGE_TITLE");
});
i18n.on("languageChanged", (lng) => {
    numeral.locale(getLocale(lng));

    document.title = i18n.t("PAGE_TITLE");
});

export default i18n;