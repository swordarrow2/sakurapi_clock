package com.meng.service;

/**
 * Configuration key content: key name and default value.
 * Description is provided by I18nManager.getEditorKeyInfo(key).
 */
public class KeyContent {
    private final String key;
    private final String defaultValue;

    public KeyContent(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}