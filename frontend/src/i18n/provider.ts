import type { I18nProvider } from "@refinedev/core";
import i18n from "./index";

export const i18nProvider: I18nProvider = {
  translate: (key, options) => {
    const result = i18n.t(key, options as Record<string, unknown>);
    return typeof result === "string" ? result : key;
  },
  changeLocale: (lang) => {
    return i18n.changeLanguage(lang);
  },
  getLocale: () => i18n.language,
};
