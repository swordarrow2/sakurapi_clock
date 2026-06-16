package com.meng.i18n;

import java.util.*;

/**
 * Internationalization manager.
 * Manages available languages and current language content.
 */
public class I18nManager {

    private static final I18nManager INSTANCE = new I18nManager();

    private final Map<String, I18nContent> languages;
    private I18nContent current;

    private I18nManager() {
        languages = new LinkedHashMap<>();
        // Register default languages using static fields
        register(Eng.CODE, new Eng());
        register(Chn.CODE, new Chn());
        // Set default language based on system locale
        String sysLang = Locale.getDefault().getLanguage();
        if (hasLanguage(sysLang)) {
            setCurrent(sysLang);
        } else {
            setCurrent(Eng.CODE);
        }
    }

    /**
     * Get the singleton instance.
     */
    public static I18nManager getInstance() {
        return INSTANCE;
    }

    /**
     * Register a language with explicit code.
     */
    public void register(String code, I18nContent content) {
        languages.put(code, content);
    }

    /**
     * Set current language by code.
     */
    public void setCurrent(String code) {
        I18nContent content = languages.get(code);
        if (content != null) {
            current = content;
        }
    }

    /**
     * Get current language content.
     */
    public I18nContent getCurrent() {
        return current;
    }

    /**
     * Get all registered language codes.
     */
    public Set<String> getLanguageCodes() {
        return languages.keySet();
    }

    /**
     * Get all registered language contents.
     */
    public Collection<I18nContent> getLanguages() {
        return languages.values();
    }

    /**
     * Get language content by code.
     */
    public I18nContent getLanguage(String code) {
        return languages.get(code);
    }

    /**
     * Check if a language is registered.
     */
    public boolean hasLanguage(String code) {
        return languages.containsKey(code);
    }
}