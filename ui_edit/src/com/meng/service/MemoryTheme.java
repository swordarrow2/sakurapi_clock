package com.meng.service;

import com.meng.tools.ConfigParser;

import java.io.File;
import java.util.*;

/**
 * In-memory representation of a theme.
 * All theme data (config + files) lives in memory.
 * Edits modify this in-memory copy; saving persists to disk.
 */
public class MemoryTheme {

    public enum SourceType { DIR, TAR }

    public final String name;
    public final SourceType sourceType;
    public final String sourcePath;

    private final ConfigParser config;
    private final List<MemoryFile> files;

    // ===================== Construction =====================

    public MemoryTheme(String name, SourceType sourceType, String sourcePath,
                       ConfigParser config, List<MemoryFile> files) {
        this.name = name;
        this.sourceType = sourceType;
        this.sourcePath = sourcePath;
        this.config = config;
        this.files = files;
    }

    /** Create an empty theme for "new theme" flow. */
    public static MemoryTheme createEmpty(String themeName, String dirPath) {
        ConfigParser cfg = new ConfigParser();
        return new MemoryTheme(themeName, SourceType.DIR, dirPath,
                               cfg, new ArrayList<>());
    }

    // ===================== Config Access =====================

    public ConfigParser getConfig() {
        return config;
    }

    public String getConfigFilePath() {
        if (sourceType == SourceType.TAR) {
            return sourcePath + " (tar)";
        }
        return sourcePath + File.separator + "config.ini";
    }

    // ===================== File Access =====================

    public List<MemoryFile> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public MemoryFile getFile(String name) {
        for (MemoryFile f : files) {
            if (f.name.equals(name)) return f;
        }
        return null;
    }

    public MemoryFile getFirstImage() {
        for (MemoryFile f : files) {
            if (f.isImage()) return f;
        }
        return null;
    }

    public void addFile(MemoryFile file) {
        files.add(file);
    }

    public void removeFile(String name) {
        files.removeIf(f -> f.name.equals(name));
    }

    public List<MemoryFile> getImagesInDir(String dirName) {
        String prefix = dirName.endsWith("/") ? dirName : dirName + "/";
        List<MemoryFile> result = new ArrayList<>();
        for (MemoryFile f : files) {
            if (f.name.startsWith(prefix) && f.isImage()) {
                result.add(f);
            }
        }
        return result;
    }

    // ===================== Background Detection =====================

    public boolean hasDynamicBackground() {
        String dir = config.get("background", "dir", "");
        return !dir.isEmpty();
    }

    public String getBackgroundValue() {
        String dir = config.get("background", "dir", "");
        if (!dir.isEmpty()) return dir;
        return config.get("background", "file", "");
    }

    /**
     * Get the path to the first background image (for preview).
     * Returns null if no image found.
     */
    public MemoryFile getBackgroundImage() {
        String bgDir = config.get("background", "dir", "");
        if (!bgDir.isEmpty()) {
            List<MemoryFile> imgs = getImagesInDir(bgDir);
            return imgs.isEmpty() ? null : imgs.get(0);
        }
        String bgFile = config.get("background", "file", "");
        if (!bgFile.isEmpty()) {
            return getFile(bgFile);
        }
        return null;
    }

    // ===================== Dynamic Background Info =====================

    public String getDynamicBackgroundInfo() {
        String bgDir = config.get("background", "dir", "");
        if (bgDir.isEmpty()) return null;

        List<MemoryFile> images = getImagesInDir(bgDir);
        int frameCount = images.size();

        // Count subdirectories (animation sequences)
        Set<String> subdirs = new HashSet<>();
        String prefix = bgDir.endsWith("/") ? bgDir : bgDir + "/";
        for (MemoryFile f : files) {
            if (f.isDirectory && f.name.startsWith(prefix)) {
                String sub = f.name.substring(prefix.length());
                if (!sub.contains("/")) {
                    subdirs.add(sub);
                }
            }
        }
        int styleCount = subdirs.size();

        StringBuilder info = new StringBuilder();
        info.append("Dynamic background: ").append(bgDir).append("/\n");
        info.append("  Frames: ").append(frameCount);
        if (styleCount > 0) {
            info.append(" (").append(styleCount).append(" animation sequences)");
        }
        info.append("\n");
        return info.toString();
    }

    // ===================== Preview =====================

    public String generatePreview() {
        StringBuilder sb = new StringBuilder();
        sb.append("Theme: ").append(name).append("\n");
        sb.append("Source: ").append(getConfigFilePath()).append("\n");
        sb.append("========================================\n");

        // Show config.ini inline
        String configText = config.serialize();
        if (configText != null && !configText.isEmpty()) {
            sb.append(">>> config.ini (text) <<<\n");
            sb.append("========================================\n");
            sb.append(configText);
            if (!configText.endsWith("\n")) sb.append("\n");
            sb.append("========================================\n");
        }

        // Dynamic background info
        String dynInfo = getDynamicBackgroundInfo();
        if (dynInfo != null) {
            sb.append(dynInfo);
            sb.append("========================================\n");
        }

        // List all files
        for (MemoryFile f : files) {
            if (f.isDirectory) {
                sb.append("  ").append(f.name).append("/  \t[directory]\n");
            } else {
                sb.append("  ").append(f.name);
                sb.append("  \t[").append(f.getFileType()).append("]");
                sb.append("  \t").append(formatSize(f.data != null ? f.data.length : 0)).append("\n");
            }
        }

        if (configText == null || configText.isEmpty()) {
            sb.append("\n(no config.ini)\n");
        }

        return sb.toString();
    }

    // ===================== File Entry Class =====================

    public static class MemoryFile {
        public final String name;
        public final byte[] data;
        public final boolean isDirectory;
        private final String fileType;

        public MemoryFile(String name, byte[] data, boolean isDirectory) {
            this.name = name;
            this.data = data;
            this.isDirectory = isDirectory;
            this.fileType = guessFileType(name);
        }

        public boolean isImage() {
            return "image".equals(fileType);
        }

        public boolean isText() {
            return "text".equals(fileType);
        }

        public boolean isFont() {
            return "font".equals(fileType);
        }

        public String getFileType() {
            return fileType;
        }

        private static String guessFileType(String fileName) {
            if (fileName == null) return "binary";
            String lower = fileName.toLowerCase();
            if (lower.endsWith("/") || lower.endsWith(".dir")) return "directory";
            if (lower.endsWith(".png") || lower.endsWith(".jpg")
                    || lower.endsWith(".jpeg") || lower.endsWith(".bmp")
                    || lower.endsWith(".gif")) return "image";
            if (lower.endsWith(".ttf") || lower.endsWith(".otf")
                    || lower.endsWith(".woff")) return "font";
            if (lower.endsWith(".ini") || lower.endsWith(".txt")
                    || lower.endsWith(".json") || lower.endsWith(".xml")
                    || lower.endsWith(".cfg") || lower.endsWith(".conf")
                    || lower.endsWith(".log") || lower.endsWith(".md")) return "text";
            return "binary";
        }
    }

    // ===================== Utility =====================

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
