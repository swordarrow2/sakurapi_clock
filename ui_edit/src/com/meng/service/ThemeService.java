package com.meng.service;

import com.meng.tools.ConfigParser;
import com.meng.tools.TarPacker;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Pure backend service for theme operations.
 * All theme data (config + files) is held in a {@link MemoryTheme} in-memory.
 * Edits modify the in-memory copy; call {@link #saveCurrentConfig()} to persist.
 * Contains all business logic with zero Swing/GUI dependency.
 */
public class ThemeService {

    private final String themesDir;
    private final String tarOutputDir;
    private final String baseDir;

    /** The in-memory working copy of the currently open theme. Null if nothing loaded. */
    private MemoryTheme currentTheme;

    private List<ThemeItem> cachedItems;

    public ThemeService(String baseDir, String themesDir, String tarOutputDir) {
        this.baseDir = baseDir;
        this.themesDir = themesDir;
        this.tarOutputDir = tarOutputDir;
    }

    // ===================== Accessors =====================

    /** Returns the current in-memory theme, or null if nothing loaded. */
    public MemoryTheme getCurrentTheme() {
        return currentTheme;
    }

    /** Returns true if a theme is currently loaded in memory. */
    public boolean isThemeLoaded() {
        return currentTheme != null;
    }

    // ===================== Theme Listing =====================

    public List<ThemeItem> scanThemes() {
        Set<ThemeItem> items = new LinkedHashSet<>();
        cachedItems = null;

        // Theme directories
        File dir = new File(baseDir, themesDir);
        if (dir.exists() && dir.isDirectory()) {
            File[] dirs = dir.listFiles(File::isDirectory);
            if (dirs != null) {
                for (File d : dirs) {
                    items.add(new ThemeItem(ThemeItem.Type.DIR, d.getName()));
                }
            }
        }

        // .tar files from tar output dir
        File tarDir = new File(baseDir, tarOutputDir);
        if (tarDir.exists() && tarDir.isDirectory()) {
            File[] tars = tarDir.listFiles((d, name) -> name.endsWith(".tar"));
            if (tars != null) {
                for (File t : tars) {
                    items.add(new ThemeItem(ThemeItem.Type.TAR, t.getName()));
                }
            }
        }

        // .tar files from base dir
        File curDir = new File(baseDir);
        File[] curTars = curDir.listFiles((d, name) -> name.endsWith(".tar"));
        if (curTars != null) {
            for (File t : curTars) {
                items.add(new ThemeItem(ThemeItem.Type.TAR, t.getName()));
            }
        }

        List<ThemeItem> sorted = new ArrayList<>(items);
        cachedItems = sorted;
        return sorted;
    }

    public ThemeItem resolveItem(String displayName) {
        return ThemeItem.fromDisplayName(displayName);
    }

    // ===================== Path Helpers =====================

    public String getThemeDirPath(String themeName) {
        return baseDir + File.separator + themesDir + File.separator + themeName;
    }

    public String getTarFilePath(String tarFileName) {
        String p1 = baseDir + File.separator + tarOutputDir + File.separator + tarFileName;
        if (new File(p1).exists()) return p1;
        String p2 = baseDir + File.separator + tarFileName;
        if (new File(p2).exists()) return p2;
        return p1;
    }

    public String getCurrentThemePath() {
        if (currentTheme == null) return "";
        return currentTheme.getConfigFilePath();
    }

    /**
     * Get the current theme name (short name without path/extension).
     * Returns null if nothing loaded.
     */
    public String getCurrentThemeName() {
        if (currentTheme == null) return null;
        return currentTheme.name;
    }

    // ===================== Load Theme (DIR or TAR) =====================

    /**
     * Load a theme directory into memory.
     * Reads config.ini and all files into {@link MemoryTheme}.
     * Returns error message, or null on success.
     */
    public String loadThemeConfig(String themeName) {
        String themeDir = getThemeDirPath(themeName);
        File dir = new File(themeDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return "Theme directory not found: " + themeDir;
        }

        // Read config.ini
        String configPath = themeDir + File.separator + "config.ini";
        ConfigParser config = new ConfigParser();
        if (new File(configPath).exists()) {
            config.load(configPath);
        }

        // Read all files into memory
        List<MemoryTheme.MemoryFile> files = new ArrayList<>();
        loadDirIntoMemory(dir, "", files);

        currentTheme = new MemoryTheme(themeName, MemoryTheme.SourceType.DIR, themeDir,
                                       config, files);
        return null;
    }

    /** Recursively read directory files into MemoryFile list. */
    private void loadDirIntoMemory(File dir, String prefix, List<MemoryTheme.MemoryFile> out) {
        File[] entries = dir.listFiles();
        if (entries == null) return;

        for (File f : entries) {
            String relName = prefix.isEmpty() ? f.getName() : prefix + "/" + f.getName();
            if (f.isDirectory()) {
                out.add(new MemoryTheme.MemoryFile(relName + "/", null, true));
                loadDirIntoMemory(f, relName, out);
            } else if (f.getName().equals("config.ini")) {
                continue; // config is already in MemoryTheme.config
            } else {
                try {
                    byte[] data = Files.readAllBytes(f.toPath());
                    out.add(new MemoryTheme.MemoryFile(relName, data, false));
                } catch (IOException e) {
                    System.err.println("Failed to read file: " + f.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Load a .tar theme into memory.
     * Reads all tar entries into {@link MemoryTheme}.
     * Returns error message, or null on success.
     */
    public String loadTarConfig(String tarFileName) {
        String tarFile = getTarFilePath(tarFileName);
        if (!new File(tarFile).exists()) {
            return "Tar file not found: " + tarFileName;
        }

        // Read all entries from tar
        List<TarPacker.TarEntryInfo> entries = TarPacker.readTarEntries(tarFile);

        ConfigParser config = new ConfigParser();
        List<MemoryTheme.MemoryFile> files = new ArrayList<>();

        for (TarPacker.TarEntryInfo entry : entries) {
            String name = entry.fileName.replace('\\', '/');

            if (entry.isDirectory) {
                files.add(new MemoryTheme.MemoryFile(name.endsWith("/") ? name : name + "/", null, true));
            } else if (entry.fileName.equals("config.ini") && entry.textContent != null) {
                config.parse(entry.textContent);
            } else if (entry.imageData != null) {
                files.add(new MemoryTheme.MemoryFile(name, entry.imageData, false));
            } else if (entry.textContent != null) {
                try {
                    files.add(new MemoryTheme.MemoryFile(name, entry.textContent.getBytes("UTF-8"), false));
                } catch (UnsupportedEncodingException e) {
                    files.add(new MemoryTheme.MemoryFile(name, entry.textContent.getBytes(), false));
                }
            } else if (entry.rawData != null) {
                // Binary file (font, etc.) - store raw data for saving
                files.add(new MemoryTheme.MemoryFile(name, entry.rawData, false));
            } else if (entry.fileSize > 0) {
                // File too large (> 2GB, exceeds int range) — skip
                System.err.println("Warning: skipping oversized entry " + name);
            }
        }

        // Derive theme name from tar filename (strip .tar)
        String themeName = tarFileName;
        int dotPos = themeName.lastIndexOf('.');
        if (dotPos > 0) themeName = themeName.substring(0, dotPos);

        currentTheme = new MemoryTheme(themeName, MemoryTheme.SourceType.TAR, tarFile,
                                       config, files);
        return null;
    }

    // ===================== Save =====================

    /**
     * Persist the current in-memory theme back to disk.
     * For DIR themes: writes config.ini + all files to filesystem.
     * For TAR themes: rebuilds the .tar with all in-memory files.
     * Returns error message, or null on success.
     */
    public String saveCurrentConfig() {
        if (currentTheme == null) {
            return "No theme loaded";
        }

        if (currentTheme.sourceType == MemoryTheme.SourceType.TAR) {
            return saveTarFromMemory();
        } else {
            return saveDirFromMemory();
        }
    }

    /** Save MemoryTheme to a directory (writes config.ini + all files). */
    private String saveDirFromMemory() {
        String dirPath = currentTheme.sourcePath;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return "Cannot create directory: " + dirPath;
            }
        }

        // Write config.ini
        String configText = currentTheme.getConfig().serialize();
        String configPath = dirPath + File.separator + "config.ini";
        try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(configPath), "UTF-8")) {
            w.write(configText);
        } catch (IOException e) {
            return "Failed to write config.ini: " + e.getMessage();
        }

        // Write all MemoryFiles
        for (MemoryTheme.MemoryFile file : currentTheme.getFiles()) {
            if (file.isDirectory) continue;
            if (file.data == null) continue;

            // Handle subdirectories (e.g. "bg_dir/000000.jpg")
            String fullPath = dirPath + File.separator + file.name;
            File f = new File(fullPath);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try {
                Files.write(f.toPath(), file.data);
            } catch (IOException e) {
                System.err.println("Failed to write " + file.name + ": " + e.getMessage());
                // Continue with other files
            }
        }

        // Reset modified flag
        currentTheme.getConfig().markUnmodified();
        return null;
    }

    /** Save MemoryTheme to a tar file (packs all in-memory files). */
    private String saveTarFromMemory() {
        String tarPath = currentTheme.sourcePath;
        File tmpFile = new File(tarPath + ".tmp");

        try (RandomAccessFile tarOut = new RandomAccessFile(tmpFile, "rw")) {
            // Write config.ini
            byte[] configBytes = currentTheme.getConfig().serialize().getBytes("UTF-8");
            writeTarHeader(tarOut, "config.ini", configBytes.length, '0');
            tarOut.write(configBytes);
            padToBlock(tarOut, configBytes.length);

            // Write all MemoryFiles
            for (MemoryTheme.MemoryFile file : currentTheme.getFiles()) {
                if (file.isDirectory) {
                    writeTarHeader(tarOut, file.name, 0, '5');
                } else if (file.data != null) {
                    writeTarHeader(tarOut, file.name, file.data.length, '0');
                    tarOut.write(file.data);
                    padToBlock(tarOut, file.data.length);
                }
            }

            // End-of-archive marker (two zero blocks)
            tarOut.write(new byte[1024]);

        } catch (IOException e) {
            tmpFile.delete();
            return "Failed to write tar: " + e.getMessage();
        }

        // Replace original
        File original = new File(tarPath);
        if (original.exists() && !original.delete()) {
            tmpFile.delete();
            return "Cannot overwrite original tar file";
        }
        if (!tmpFile.renameTo(original)) {
            tmpFile.delete();
            return "Cannot rename temp file to original";
        }

        currentTheme.getConfig().markUnmodified();
        return null;
    }

    // ===================== Tar Writing Helpers =====================

    private void writeTarHeader(RandomAccessFile out, String name, long size, char typeFlag) throws IOException {
        byte[] header = new byte[512];

        // Name (100 bytes)
        byte[] nameBytes = name.getBytes();
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));

        // Mode (8 bytes)
        System.arraycopy("0000644".getBytes(), 0, header, 100, 7);

        // UID
        System.arraycopy("0000000".getBytes(), 0, header, 108, 7);

        // GID
        System.arraycopy("0000000".getBytes(), 0, header, 116, 7);

        // Size (12 bytes octal)
        String sizeStr = String.format("%011o", size);
        System.arraycopy(sizeStr.getBytes(), 0, header, 124, 11);

        // MTime (12 bytes octal)
        long mtime = System.currentTimeMillis() / 1000;
        String mtimeStr = String.format("%011o", mtime);
        System.arraycopy(mtimeStr.getBytes(), 0, header, 136, 11);

        // Checksum placeholder (spaces)
        for (int i = 148; i < 156; i++) header[i] = ' ';

        // Type flag
        header[156] = (byte) typeFlag;

        // Magic
        System.arraycopy("ustar".getBytes(), 0, header, 257, 5);
        header[263] = '0';
        header[264] = '0';

        // Calculate checksum
        int checksum = 0;
        for (byte b : header) checksum += b & 0xFF;
        String chkStr = String.format("%06o", checksum);
        System.arraycopy(chkStr.getBytes(), 0, header, 148, 6);
        header[155] = ' ';

        out.write(header);
    }

    private void padToBlock(RandomAccessFile out, long dataSize) throws IOException {
        long padding = (512 - (dataSize % 512)) % 512;
        if (padding > 0) {
            out.write(new byte[(int) padding]);
        }
    }

    // ===================== Config Operations (delegate to MemoryTheme) =====================

    public List<String> getFlatKeys() {
        if (currentTheme == null) return Collections.emptyList();
        List<String> keys = new ArrayList<>();
        ConfigParser config = currentTheme.getConfig();
        List<String> sortedSections = new ArrayList<>(config.getSections());
        sortedSections.sort((a, b) -> {
            if (a.isEmpty()) return -1;
            if (b.isEmpty()) return 1;
            return a.compareTo(b);
        });

        for (String section : sortedSections) {
            Map<String, String> values = config.getSection(section);
            if (values.isEmpty()) continue;
            for (String key : values.keySet()) {
                if (section.isEmpty()) {
                    keys.add(key);
                } else {
                    keys.add(section + "." + key);
                }
            }
        }
        return keys;
    }

    public Map<String, String> getFlatValues() {
        if (currentTheme == null) return Collections.emptyMap();
        Map<String, String> result = new LinkedHashMap<>();
        ConfigParser config = currentTheme.getConfig();
        for (String section : config.getSections()) {
            Map<String, String> values = config.getSection(section);
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String flatKey = section.isEmpty() ? entry.getKey() : section + "." + entry.getKey();
                result.put(flatKey, entry.getValue());
            }
        }
        return result;
    }

    public String getValue(String flatKey) {
        if (currentTheme == null) return "";
        ConfigParser config = currentTheme.getConfig();
        int dotPos = flatKey.indexOf('.');
        if (dotPos > 0) {
            String section = flatKey.substring(0, dotPos);
            String key = flatKey.substring(dotPos + 1);
            return config.get(section, key, "");
        }
        return config.get("", flatKey, "");
    }

    public void setValue(String flatKey, String value) {
        if (currentTheme == null) return;
        ConfigParser config = currentTheme.getConfig();
        int dotPos = flatKey.indexOf('.');
        if (dotPos > 0) {
            config.set(flatKey.substring(0, dotPos), flatKey.substring(dotPos + 1), value);
        } else {
            config.set("", flatKey, value);
        }
    }

    /**
     * File-type key endings for orphan detection.
     */
    private static final String[] FILE_KEY_ENDINGS = {".file", ".font", ".dir", ".background"};

    private static boolean isFileKey(String flatKey) {
        if (flatKey == null) return false;
        for (String ending : FILE_KEY_ENDINGS) {
            if (flatKey.endsWith(ending)) return true;
        }
        return false;
    }

    /**
     * Collect current file-type key → filename mappings for orphan detection.
     * e.g., "background.file" → "sakura.png"
     */
    private Map<String, String> collectFileKeyValues() {
        Map<String, String> result = new HashMap<>();
        if (currentTheme == null) return result;
        ConfigParser config = currentTheme.getConfig();
        for (String section : config.getSections()) {
            Map<String, String> values = config.getSection(section);
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String flatKey = section.isEmpty() ? entry.getKey() : section + "." + entry.getKey();
                if (isFileKey(flatKey) && entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                    result.put(flatKey, entry.getValue().trim());
                }
            }
        }
        return result;
    }

    /**
     * Check if a filename is referenced by any config value in the current theme.
     */
    private boolean isFileNameReferencedInConfig(String fileName) {
        if (currentTheme == null || fileName == null) return false;
        ConfigParser config = currentTheme.getConfig();
        for (String section : config.getSections()) {
            Map<String, String> values = config.getSection(section);
            for (String value : values.values()) {
                if (value != null && value.trim().equals(fileName)) return true;
            }
        }
        return false;
    }

    public void setAllValues(Map<String, String> flatValues) {
        if (currentTheme == null) return;
        ConfigParser config = currentTheme.getConfig();

        // Snapshot old file-key values for orphan detection
        Map<String, String> oldFileValues = collectFileKeyValues();

        // Replace all config values
        config.getSections().clear();
        for (Map.Entry<String, String> entry : flatValues.entrySet()) {
            String flatKey = entry.getKey();
            if (flatKey == null || flatKey.trim().isEmpty()) continue;
            flatKey = flatKey.trim();
            int dotPos = flatKey.indexOf('.');
            if (dotPos > 0) {
                config.set(flatKey.substring(0, dotPos), flatKey.substring(dotPos + 1), entry.getValue());
            } else {
                config.set("", flatKey, entry.getValue());
            }
        }

        // Orphan detection: old file values that are no longer referenced
        for (Map.Entry<String, String> oldEntry : oldFileValues.entrySet()) {
            String oldFileName = oldEntry.getValue();
            if (oldFileName.isEmpty()) continue;

            // Skip if the same key still has the same value
            String newValue = flatValues.get(oldEntry.getKey());
            if (newValue != null && newValue.trim().equals(oldFileName)) continue;

            // Check if any other config key still references this file
            if (!isFileNameReferencedInConfig(oldFileName)) {
                // Remove from MemoryTheme
                currentTheme.removeFile(oldFileName);
                System.out.println("Orphan file removed from memory: " + oldFileName);

                // For DIR themes, also delete from disk
                if (currentTheme.sourceType == MemoryTheme.SourceType.DIR) {
                    String filePath = currentTheme.sourcePath + File.separator + oldFileName;
                    File diskFile = new File(filePath);
                    if (diskFile.exists() && diskFile.isFile()) {
                        diskFile.delete();
                        System.out.println("Orphan file deleted from disk: " + filePath);
                    }
                }
            }
        }
    }

    public boolean isConfigModified() {
        return currentTheme != null && currentTheme.getConfig().isModified();
    }

    // ===================== Preview =====================

    /**
     * Generate a text preview from the current in-memory theme.
     */
    public String getCurrentPreview() {
        if (currentTheme == null) return "(no theme loaded)";
        return currentTheme.generatePreview();
    }

    /**
     * Backward-compatible wrapper: get preview for a DIR theme.
     * Since all themes are loaded into MemoryTheme, delegates to {@link #getCurrentPreview()}.
     */
    public String getThemePreview(String themeName) {
        if (currentTheme != null && currentTheme.name.equals(themeName)
                && currentTheme.sourceType == MemoryTheme.SourceType.DIR) {
            return currentTheme.generatePreview();
        }
        // Fallback: load and generate
        String err = loadThemeConfig(themeName);
        if (err != null) return "Error: " + err + "\n";
        return currentTheme.generatePreview();
    }

    /**
     * Backward-compatible wrapper: get preview for a TAR theme.
     */
    public String getTarPreview(String tarFileName) {
        if (currentTheme != null && currentTheme.name.equals(
                tarFileName.contains(".") ? tarFileName.substring(0, tarFileName.lastIndexOf('.')) : tarFileName)
                && currentTheme.sourceType == MemoryTheme.SourceType.TAR) {
            return currentTheme.generatePreview();
        }
        String err = loadTarConfig(tarFileName);
        if (err != null) return "Error: " + err + "\n";
        return currentTheme.generatePreview();
    }

    // ===================== Background / Image =====================

    /**
     * Get the background image as raw bytes from the current in-memory theme.
     * Returns null if no background image found.
     */
    public byte[] getBackgroundImageBytes() {
        if (currentTheme == null) return null;
        MemoryTheme.MemoryFile bg = currentTheme.getBackgroundImage();
        return bg != null ? bg.data : null;
    }

    /**
     * Get the first image from a TAR file as bytes.
     * Works with MemoryTheme or falls back to TarPacker.
     */
    public TarPacker.TarEntryInfo getTarFirstImage(String tarFileName) {
        // If this TAR is already the current theme, use in-memory data
        if (currentTheme != null && currentTheme.sourceType == MemoryTheme.SourceType.TAR
                && (currentTheme.sourcePath.endsWith(tarFileName)
                    || currentTheme.name.equals(tarFileName.replace(".tar", "")))) {
            MemoryTheme.MemoryFile img = currentTheme.getFirstImage();
            if (img != null && img.data != null) {
                TarPacker.TarEntryInfo info = new TarPacker.TarEntryInfo(img.name, img.data.length, false);
                info.imageData = img.data;
                return info;
            }
            return null;
        }

        // Fallback: read directly from tar
        String tarFile = getTarFilePath(tarFileName);
        if (!new File(tarFile).exists()) return null;
        List<TarPacker.TarEntryInfo> entries = TarPacker.readTarEntries(tarFile);
        for (TarPacker.TarEntryInfo entry : entries) {
            if (entry.imageData != null) return entry;
        }
        return null;
    }

    /**
     * Get the background image path for the current config.
     * For DIR themes (files on disk), returns a real filesystem path.
     * For TAR themes, returns the internal name (caller should use getBackgroundImageBytes instead).
     */
    public String getBackgroundImagePath(String themeName) {
        if (currentTheme == null) return null;

        // If this is the currently loaded DIR theme, construct filesystem path
        if (currentTheme.sourceType == MemoryTheme.SourceType.DIR
                && currentTheme.name.equals(themeName)) {
            String dirPath = currentTheme.sourcePath;
            String bgDir = currentTheme.getConfig().get("background", "dir", "");
            if (!bgDir.isEmpty()) {
                String fullDir = dirPath + File.separator + bgDir;
                File d = new File(fullDir);
                if (d.isDirectory()) {
                    File[] images = d.listFiles((df, name) -> {
                        String lower = name.toLowerCase();
                        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                                || lower.endsWith(".png") || lower.endsWith(".bmp");
                    });
                    if (images != null && images.length > 0) {
                        return images[0].getAbsolutePath();
                    }
                }
                return null;
            }

            String bgFile = currentTheme.getConfig().get("background", "file", "");
            if (!bgFile.isEmpty()) {
                String filePath = dirPath + File.separator + bgFile;
                if (new File(filePath).exists()) return filePath;
            }
        }

        // For TAR themes or mismatched current theme, try to find the image from data
        return null;
    }

    /**
     * Check if the current theme has a dynamic background.
     */
    public boolean isDynamicBackground(String themeName) {
        if (currentTheme == null) return false;
        if (!currentTheme.name.equals(themeName)) return false;
        return currentTheme.hasDynamicBackground();
    }

    /**
     * Check if the currently loaded theme has a dynamic background.
     * Convenience method that doesn't require themeName.
     */
    public boolean isCurrentDynamicBackground() {
        if (currentTheme == null) return false;
        return currentTheme.hasDynamicBackground();
    }

    /**
     * Get dynamic background info for the current theme.
     */
    public String getDynamicBackgroundInfo(String themeName) {
        if (currentTheme == null) return null;
        if (!currentTheme.name.equals(themeName)) return null;
        return currentTheme.getDynamicBackgroundInfo();
    }

    // ===================== Copy File to Theme =====================

    /**
     * Copy an external file into the current MemoryTheme, also writing to disk for DIR themes.
     * Returns the simple filename on success, or null on failure.
     */
    public String copyFileToTheme(String sourceFilePath) {
        if (currentTheme == null) return null;

        File src = new File(sourceFilePath);
        if (!src.exists()) return null;

        String fileName = src.getName();

        // Read the file into memory
        try {
            byte[] data = Files.readAllBytes(src.toPath());

            // Remove existing entry with same name
            currentTheme.removeFile(fileName);

            // Add to MemoryTheme
            currentTheme.addFile(new MemoryTheme.MemoryFile(fileName, data, false));

            // For DIR themes, also write to disk immediately so the file is visible
            if (currentTheme.sourceType == MemoryTheme.SourceType.DIR) {
                String destPath = currentTheme.sourcePath + File.separator + fileName;
                Files.write(new File(destPath).toPath(), data);
            }

            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===================== Pack / Unpack =====================

    public String packTheme(String themeName) {
        String sourceDir = getThemeDirPath(themeName);
        File outDir = new File(baseDir, tarOutputDir);
        if (!outDir.exists()) outDir.mkdirs();
        String outputFile = outDir.getAbsolutePath() + File.separator + themeName + ".tar";

        if (TarPacker.packDirectory(sourceDir, outputFile)) {
            return null;
        }
        return "Failed to pack theme '" + themeName + "'";
    }

    public boolean tarExists(String themeName) {
        String outputFile = baseDir + File.separator + tarOutputDir + File.separator + themeName + ".tar";
        return new File(outputFile).exists();
    }

    public String unpackTar(String tarFileName) {
        String themeName = tarFileName;
        int dotPos = themeName.lastIndexOf('.');
        if (dotPos > 0) {
            themeName = themeName.substring(0, dotPos);
        }

        String tarFile = getTarFilePath(tarFileName);
        if (!new File(tarFile).exists()) {
            return "Tar file not found: " + tarFileName;
        }

        String targetDir = getThemeDirPath(themeName);
        if (TarPacker.unpackDirectory(tarFile, targetDir)) {
            return null;
        }
        return "Failed to unpack " + tarFileName;
    }

    public boolean themeDirExists(String themeName) {
        return new File(getThemeDirPath(themeName)).exists();
    }

    // ===================== New Theme =====================

    public String createNewTheme(String themeName) {
        if (themeName == null || themeName.trim().isEmpty()) {
            return "Theme name cannot be empty";
        }

        themeName = themeName.trim();
        File themeDir = new File(getThemeDirPath(themeName));

        if (themeDir.exists()) {
            return "Theme '" + themeName + "' already exists";
        }

        if (!themeDir.mkdirs()) {
            return "Failed to create directory: " + themeDir;
        }

        String configPath = themeDir.getAbsolutePath() + File.separator + "config.ini";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(configPath), "UTF-8"))) {
            writer.write("# [cfg]");
            writer.newLine();
            writer.write("cfg.name = " + themeName);
            writer.newLine();
            writer.write("cfg.delay = 60");
            writer.newLine();
            writer.newLine();
            writer.write("# [hardware]");
            writer.newLine();
            writer.write("hardware.screen_width = 480");
            writer.newLine();
            writer.write("hardware.screen_height = 272");
            writer.newLine();
            writer.newLine();
            writer.write("# [performance]");
            writer.newLine();
            writer.write("performance.max_fps = 1");
            writer.newLine();
            writer.newLine();
            writer.write("# [time]");
            writer.newLine();
            writer.write("time.font = font.ttf");
            writer.newLine();
            writer.write("time.size = 48");
            writer.newLine();
            writer.write("time.color = 0xFFFFFFFF");
            writer.newLine();
            writer.write("time.x = 0");
            writer.newLine();
            writer.write("time.y = 0");
            writer.newLine();
        } catch (IOException e) {
            return "Failed to create config.ini: " + e.getMessage();
        }

        // Load the new theme into memory
        return loadThemeConfig(themeName);
    }
}
