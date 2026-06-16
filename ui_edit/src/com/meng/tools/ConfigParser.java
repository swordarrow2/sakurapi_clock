package com.meng.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Simple INI-style config parser for theme config.ini files.
 * Each section contains key=value pairs, comments start with #.
 */
public class ConfigParser {

    private final Map<String, Map<String, String>> sections = new LinkedHashMap<>();
    private String filePath;
    private boolean modified = false;

    public ConfigParser() {
    }

    /**
     * Load a config.ini file from the filesystem.
     * Supports flat dot-notation format: section.key = value
     */
    public boolean load(String filePath) {
        this.filePath = filePath;
        sections.clear();
        File file = new File(filePath);
        if (!file.exists()) return false;

        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            parse(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Parse config from a raw string (e.g. from a tar entry).
     * Supports flat dot-notation format: section.key = value
     */
    public void parse(String content) {
        sections.clear();
        String[] lines = content.split("\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int eqPos = line.indexOf('=');
            if (eqPos > 0) {
                String flatKey = line.substring(0, eqPos).trim();
                String value = line.substring(eqPos + 1).trim();

                int dotPos = flatKey.indexOf('.');
                String section;
                String key;
                if (dotPos > 0) {
                    section = flatKey.substring(0, dotPos).trim();
                    key = flatKey.substring(dotPos + 1).trim();
                } else {
                    section = "";
                    key = flatKey;
                }

                Map<String, String> sectionMap = sections.computeIfAbsent(section, k -> new LinkedHashMap<>());
                sectionMap.put(key, value);
            }
        }
    }

    /**
     * Serialize the current config to a flat dot-notation string.
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        boolean firstSection = true;
        for (Map.Entry<String, Map<String, String>> sectionEntry : sections.entrySet()) {
            String sectionName = sectionEntry.getKey();
            Map<String, String> values = sectionEntry.getValue();
            if (values.isEmpty()) continue;

            if (!firstSection) {
                sb.append(System.lineSeparator());
            }
            firstSection = false;

            if (!sectionName.isEmpty()) {
                sb.append("# [").append(sectionName).append("]").append(System.lineSeparator());
            }

            for (Map.Entry<String, String> entry : values.entrySet()) {
                if (sectionName.isEmpty()) {
                    sb.append(entry.getKey()).append(" = ").append(entry.getValue());
                } else {
                    sb.append(sectionName).append(".").append(entry.getKey()).append(" = ").append(entry.getValue());
                }
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    /**
     * Save the config back to file in flat dot-notation format.
     */
    public boolean save() {
        return save(this.filePath);
    }

    /**
     * Save to a specific file path in flat dot-notation format.
     * Writes: section.key = value   (no [section] headers)
     */
    public boolean save(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            boolean firstSection = true;
            for (Map.Entry<String, Map<String, String>> sectionEntry : sections.entrySet()) {
                String sectionName = sectionEntry.getKey();
                Map<String, String> values = sectionEntry.getValue();

                if (values.isEmpty()) continue;

                // Add blank line separator between sections (unless first)
                if (!firstSection) {
                    writer.newLine();
                }
                firstSection = false;

                // Write section comment header
                if (!sectionName.isEmpty()) {
                    writer.write("# [" + sectionName + "]");
                    writer.newLine();
                }

                for (Map.Entry<String, String> entry : values.entrySet()) {
                    if (sectionName.isEmpty()) {
                        writer.write(entry.getKey() + " = " + entry.getValue());
                    } else {
                        writer.write(sectionName + "." + entry.getKey() + " = " + entry.getValue());
                    }
                    writer.newLine();
                }
            }
            modified = false;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get a value from a section.
     */
    public String get(String section, String key, String defaultValue) {
        Map<String, String> sectionMap = sections.get(section);
        if (sectionMap == null) return defaultValue;
        return sectionMap.getOrDefault(key, defaultValue);
    }

    /**
     * Get a value from a section with dot notation: "section.key".
     */
    public String get(String dotKey, String defaultValue) {
        int dot = dotKey.indexOf('.');
        if (dot <= 0) return defaultValue;
        return get(dotKey.substring(0, dot), dotKey.substring(dot + 1), defaultValue);
    }

    /**
     * Set a value in a section.
     */
    public void set(String section, String key, String value) {
        Map<String, String> sectionMap = sections.computeIfAbsent(section, k -> new LinkedHashMap<>());
        String old = sectionMap.put(key, value);
        if (!Objects.equals(old, value)) {
            modified = true;
        }
    }

    /**
     * Get all section names.
     */
    public Set<String> getSections() {
        return sections.keySet();
    }

    /**
     * Get all key-value pairs in a section.
     */
    public Map<String, String> getSection(String section) {
        return sections.getOrDefault(section, Collections.emptyMap());
    }

    /**
     * Check if the config has been modified since last save.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Mark the config as unmodified (after saving).
     */
    public void markUnmodified() {
        modified = false;
    }

    /**
     * Get the file path.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Set the file path.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
