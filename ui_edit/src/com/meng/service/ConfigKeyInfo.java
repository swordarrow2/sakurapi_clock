package com.meng.service;

import com.meng.i18n.I18nManager;

import java.util.*;

/**
 * Configuration key definitions with descriptions and default values.
 * Decoupled from GUI layer for portability to non-standard Java environments (e.g. Android).
 */
public class ConfigKeyInfo {

    private static final ConfigKeyInfo INSTANCE = new ConfigKeyInfo();

    private final List<KeyContent> keyContents;
    private final Map<String, KeyContent> keyMap;

    private ConfigKeyInfo() {
        keyContents = new ArrayList<>();
        keyMap = new LinkedHashMap<>();
        initKeyInfo();
    }

    private void initKeyInfo() {
        // Descriptions are now from I18nManager, only store key and default value here
        add("cfg.name", "");
        add("cfg.delay", "60");
        add("background.file", "");
        add("background.dir", "");
        add("hardware.screen_width", "480");
        add("hardware.screen_height", "272");
        add("gui.show_cursor", "0");
        add("performance.max_fps", "1");
        add("time.font", "");
        add("time.size", "48");
        add("time.color", "0x7ECF2AFF");
        add("time.colorOutline", "255,255,255,255");
        add("time.outlineSize", "2");
        add("time.x", "280");
        add("time.y", "206");
        add("date.font", "");
        add("date.size", "24");
        add("date.color", "126,207,42,255");
        add("date.colorOutline", "255,255,255,255");
        add("date.outlineSize", "2");
        add("date.x", "340");
        add("date.y", "179");
        add("fps.font", "");
        add("fps.size", "12");
        add("fps.color", "0x7ECF2AFF");
        add("fps.colorOutline", "0xFFFFFFFF");
        add("fps.outlineSize", "1");
        add("fps.x", "9");
        add("fps.y", "252");
        add("cpu_state.font", "");
        add("cpu_state.size", "12");
        add("cpu_state.color", "0x7ECF2AFF");
        add("cpu_state.colorOutline", "0xFFFFFFFF");
        add("cpu_state.outlineSize", "1");
        add("cpu_state.x", "302");
        add("cpu_state.y", "2");
        add("memory_state.font", "");
        add("memory_state.size", "12");
        add("memory_state.color", "0x7ECF2AFF");
        add("memory_state.colorOutline", "0xFFFFFFFF");
        add("memory_state.outlineSize", "2");
        add("memory_state.x", "302");
        add("memory_state.y", "20");
        add("storage_state.font", "");
        add("storage_state.size", "12");
        add("storage_state.color", "0x7ECF2AFF");
        add("storage_state.colorOutline", "0xFFFFFFFF");
        add("storage_state.outlineSize", "2");
        add("storage_state.x", "302");
        add("storage_state.y", "38");
    }

    private void add(String key, String defaultValue) {
        KeyContent kc = new KeyContent(key, defaultValue);
        keyContents.add(kc);
        keyMap.put(key, kc);
    }

    /**
     * Get the singleton instance.
     */
    public static ConfigKeyInfo getInstance() {
        return INSTANCE;
    }

    /**
     * Get all key contents.
     */
    public List<KeyContent> getKeyContents() {
        return keyContents;
    }

    /**
     * Get all keys.
     */
    public Set<String> getKeys() {
        return keyMap.keySet();
    }

    /**
     * Get KeyContent for a specific key.
     */
    public KeyContent getKeyContent(String key) {
        return keyMap.get(key);
    }

    /**
     * Get description for a key (from I18nManager).
     */
    public String getDescription(String key) {
        return I18nManager.getInstance().getCurrent().getEditorKeyInfo(key);
    }

    /**
     * Get default value for a key.
     */
    public String getDefaultValue(String key) {
        KeyContent kc = keyMap.get(key);
        return kc != null ? kc.getDefaultValue() : "";
    }

    /**
     * Check if a key exists.
     */
    public boolean containsKey(String key) {
        return keyMap.containsKey(key);
    }

    /**
     * Check if a config key represents a file reference.
     */
    public boolean isFileKey(String key) {
        if (key == null) return false;
        return key.endsWith(".file") || key.endsWith(".font")
                || key.endsWith(".dir") || key.endsWith(".background");
    }

    /**
     * Get file filter description for a file key.
     */
    public String getFileFilterDescription(String key) {
        if (key == null) return "All files";
        if (key.endsWith(".font")) return "Font files (*.ttf, *.ttc, *.otf)";
        if (key.endsWith(".file") || key.endsWith(".dir") || key.endsWith(".background"))
            return "Image files (*.jpg, *.jpeg, *.png, *.bmp)";
        return "All files";
    }

    /**
     * Get file name extensions for a file key.
     */
    public String[] getFileExtensions(String key) {
        if (key == null) return new String[]{};
        if (key.endsWith(".font")) return new String[]{"ttf", "ttc", "otf"};
        if (key.endsWith(".file") || key.endsWith(".dir") || key.endsWith(".background"))
            return new String[]{"jpg", "jpeg", "png", "bmp"};
        return new String[]{};
    }
}