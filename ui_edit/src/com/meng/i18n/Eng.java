package com.meng.i18n;

import java.util.*;

/**
 * English language implementation.
 */
public class Eng extends I18nContent {

    // Static language info
    public static String NAME = "English";
    public static String CODE = "en";

    private final Map<String, String> keyInfoMap;

    public Eng() {
        keyInfoMap = new LinkedHashMap<>();
        initKeyInfo();
    }

    private void initKeyInfo() {
        keyInfoMap.put("cfg.name", "String: Theme display name");
        keyInfoMap.put("cfg.delay", "Integer: Animation frame delay (frames per background switch)");
        keyInfoMap.put("background.file", "Filename: Static background image (jpg/png/bmp). Disabled when background.dir is set (dynamic BG)");
        keyInfoMap.put("background.dir", "Directory: Dynamic background folder containing multiple images. When set, background.file is disabled");
        keyInfoMap.put("hardware.screen_width", "Integer: Screen width in pixels");
        keyInfoMap.put("hardware.screen_height", "Integer: Screen height in pixels");
        keyInfoMap.put("gui.show_cursor", "Boolean: 0=hide cursor, 1=show cursor");
        keyInfoMap.put("performance.max_fps", "Integer: Max FPS limit. Use 0 for unlimited");
        keyInfoMap.put("time.font", "Filename: Font file (ttf/ttc/otf), FreeType fonts supported");
        keyInfoMap.put("time.size", "Integer: Text size in pixels");
        keyInfoMap.put("time.color", "Color: 0xRRGGBBAA(hex), RRGGBBAA(hex), 0xRRGGBB(hex), RRGGBB(hex), R,G,B,A(dec) or R,G,B(dec)");
        keyInfoMap.put("time.colorOutline", "Color: Same format as color");
        keyInfoMap.put("time.outlineSize", "Integer: Outline size in pixels");
        keyInfoMap.put("time.x", "Integer: X position on screen");
        keyInfoMap.put("time.y", "Integer: Y position on screen");
        keyInfoMap.put("date.font", "Filename: Font file (ttf/ttc/otf), FreeType fonts supported");
        keyInfoMap.put("date.size", "Integer: Text size in pixels");
        keyInfoMap.put("date.color", "Color: Same format as time.color");
        keyInfoMap.put("date.colorOutline", "Color: Same format as color");
        keyInfoMap.put("date.outlineSize", "Integer: Outline size in pixels");
        keyInfoMap.put("date.x", "Integer: X position on screen");
        keyInfoMap.put("date.y", "Integer: Y position on screen");
        keyInfoMap.put("fps.font", "Filename: Font file (ttf/ttc/otf), FreeType fonts supported");
        keyInfoMap.put("fps.size", "Integer: Text size in pixels");
        keyInfoMap.put("fps.color", "Color: Same format as time.color");
        keyInfoMap.put("fps.colorOutline", "Color: Same format as color");
        keyInfoMap.put("fps.outlineSize", "Integer: Outline size in pixels");
        keyInfoMap.put("fps.x", "Integer: X position on screen");
        keyInfoMap.put("fps.y", "Integer: Y position on screen");
        keyInfoMap.put("cpu_state.font", "Filename: Font file (ttf/ttc/otf), FreeType fonts supported");
        keyInfoMap.put("cpu_state.size", "Integer: Text size in pixels");
        keyInfoMap.put("cpu_state.color", "Color: Same format as time.color");
        keyInfoMap.put("cpu_state.colorOutline", "Color: Same format as color");
        keyInfoMap.put("cpu_state.outlineSize", "Integer: Outline size in pixels");
        keyInfoMap.put("cpu_state.x", "Integer: X position on screen");
        keyInfoMap.put("cpu_state.y", "Integer: Y position on screen");
        keyInfoMap.put("memory_state.font", "Filename: Font file (ttf/ttc/otf), FreeType fonts supported");
        keyInfoMap.put("memory_state.size", "Integer: Text size in pixels");
        keyInfoMap.put("memory_state.color", "Color: Same format as time.color");
        keyInfoMap.put("memory_state.colorOutline", "Color: Same format as color");
        keyInfoMap.put("memory_state.outlineSize", "Integer: Outline size in pixels");
        keyInfoMap.put("memory_state.x", "Integer: X position on screen");
        keyInfoMap.put("memory_state.y", "Integer: Y position on screen");
        keyInfoMap.put("storage_state.font", "Filename: Font file (ttf/ttc/otf), FreeType fonts supported");
        keyInfoMap.put("storage_state.size", "Integer: Text size in pixels");
        keyInfoMap.put("storage_state.color", "Color: Same format as time.color");
        keyInfoMap.put("storage_state.colorOutline", "Color: Same format as color");
        keyInfoMap.put("storage_state.outlineSize", "Integer: Outline size in pixels");
        keyInfoMap.put("storage_state.x", "Integer: X position on screen");
        keyInfoMap.put("storage_state.y", "Integer: Y position on screen");
    }

    @Override
    public String getEditorKeyInfo(String key) {
        return keyInfoMap.getOrDefault(key, "");
    }

    // ========== Menu Bar ==========

    @Override
    public String getMenuTheme() { return "Theme"; }

    @Override
    public String getMenuEdit() { return "Edit"; }

    @Override
    public String getMenuLanguage() { return "Language"; }

    @Override
    public String getMenuHelp() { return "Help"; }

    @Override
    public String getMenuNewTheme() { return "New Theme"; }

    @Override
    public String getMenuPackTheme() { return "Pack Theme (.tar)"; }

    @Override
    public String getMenuUnpackTar() { return "Unpack .tar"; }

    @Override
    public String getMenuExit() { return "Exit"; }

    @Override
    public String getMenuSaveConfig() { return "Save Config"; }

    @Override
    public String getMenuOpenFolder() { return "Open Theme Folder"; }

    @Override
    public String getMenuRefreshList() { return "Refresh Theme List"; }

    @Override
    public String getMenuAbout() { return "About"; }

    // ========== Config Editor Panel ==========

    @Override
    public String getConfigEditorTitle() { return "Config Editor"; }

    @Override
    public String getColumnKey() { return "Key"; }

    @Override
    public String getColumnValue() { return "Value"; }

    @Override
    public String getStatusNoConfig() { return "No config loaded"; }

    @Override
    public String getStatusLoadedPrefix() { return "Loaded: "; }

    @Override
    public String getConfigKeyHelpTitle() { return "Config Key Help"; }

    @Override
    public String getAddKeyTitle() { return "Add Key"; }

    @Override
    public String getAddKeyMessage() {
        return "Select key(s) to add:\n" +
                "  \u2022 Prefix options (e.g. \"cfg\") add all keys under that type\n" +
                "  \u2022 Individual keys add just that one key";
    }

    @Override
    public String getNothingToAddTitle() { return "Nothing to Add"; }

    @Override
    public String getNothingToAddMessage() { return "All available keys have already been added."; }

    @Override
    public String getBrowseButtonText() { return "Browse..."; }

    @Override
    public String getChooseFileTitle() { return "Choose File"; }

    @Override
    public String getNoThemeTitle() { return "No Theme"; }

    @Override
    public String getNoThemeMessage() { return "No theme loaded. Please select a theme first."; }

    @Override
    public String getDynamicBgDisabledHint() { return "Dynamic BG - file selection disabled"; }

    // ========== Theme Editor Frame ==========

    @Override
    public String getThemeEditorTitle() { return "Theme Package Editor"; }

    @Override
    public String getThemesBorderTitle() { return "Themes"; }

    @Override
    public String getPreviewTabTitle() { return "Preview"; }

    @Override
    public String getSelectThemeLabel() { return "Select Theme:"; }

    @Override
    public String getSaveButtonText() { return "Save Config"; }

    @Override
    public String getOpenFolderButtonText() { return "Open Folder"; }

    @Override
    public String getSaveAsButtonText() { return "Save As..."; }

    @Override
    public String getNewThemeButtonText() { return "New Theme"; }

    @Override
    public String getDeleteThemeButtonText() { return "Delete Theme"; }

    @Override
    public String getAddKeyButtonText() { return "Add Key"; }

    @Override
    public String getRemoveKeyButtonText() { return "Remove Key"; }

    // ========== Dialog Titles & Messages ==========

    @Override
    public String getSavedMessage() { return "Config saved successfully!"; }

    @Override
    public String getSavedTitle() { return "Saved"; }

    @Override
    public String getErrorTitle() { return "Error"; }

    @Override
    public String getInfoTitle() { return "Info"; }

    @Override
    public String getNoThemeSelectedTitle() { return "No Theme Selected"; }

    @Override
    public String getNoThemeSelectedMessage() { return "Please select a theme directory from the list to pack."; }

    @Override
    public String getNoTarSelectedTitle() { return "No Tar Selected"; }

    @Override
    public String getNoTarSelectedMessage() { return "Please select a .tar file from the list to unpack."; }

    @Override
    public String getConfirmOverwriteTitle() { return "Confirm Overwrite"; }

    @Override
    public String getConfirmOverwriteMessage() { return "File already exists. Overwrite?"; }

    @Override
    public String getUnsavedChangesTitle() { return "Unsaved Changes"; }

    @Override
    public String getUnsavedChangesMessage() { return "Config has unsaved changes. Save before packing?"; }

    @Override
    public String getPackSuccessfulTitle() { return "Pack Successful"; }

    @Override
    public String getPackSuccessfulMessage() { return "Successfully packed"; }

    @Override
    public String getPackFailedTitle() { return "Pack Failed"; }

    @Override
    public String getUnpackSuccessfulTitle() { return "Unpack Successful"; }

    @Override
    public String getUnpackSuccessfulMessage() { return "Successfully unpacked to: "; }

    @Override
    public String getUnpackFailedTitle() { return "Unpack Failed"; }

    @Override
    public String getSaveAsDialogTitle() { return "Save Theme As"; }

    @Override
    public String getNewThemeDialogTitle() { return "New Theme"; }

    @Override
    public String getNewThemeNamePrompt() { return "Enter theme name:"; }

    @Override
    public String getDeleteConfirmTitle() { return "Delete Theme"; }

    @Override
    public String getDeleteConfirmMessage() { return "Are you sure you want to delete this theme?"; }

    @Override
    public String getCannotOpenFolderMessage() { return "Cannot open folder: "; }

    @Override
    public String getAboutTitle() { return "About Theme Package Editor"; }

    @Override
    public String getAboutMessage() {
        return "SakuraPI Clock - Theme Package Editor\n\n" +
                "A GUI tool for editing, packing, and unpacking\n" +
                "SakuraPI Clock theme packages.\n\n" +
                "Part of the SakuraPI Clock project.";
    }

    // ========== Status Bar ==========

    @Override
    public String getStatusFoundThemes(int count) { return "Found " + count + " theme(s)"; }

    @Override
    public String getStatusConfigSaved() { return "Config saved"; }

    @Override
    public String getStatusPacked(String name) { return "Packed: " + name + ".tar"; }

    @Override
    public String getStatusUnpacked(String name) { return "Unpacked: " + name; }

    @Override
    public String getStatusCreatedTheme(String name) { return "Created new theme: " + name; }

    @Override
    public String getStatusPositionUpdated(String prefix, int x, int y) { return prefix + " position updated: (" + x + ", " + y + ")"; }

    @Override
    public String getStatusColorUpdated(String prefix) { return prefix + " color auto-set"; }

    @Override
    public String getDynamicBackgroundText() { return "Dynamic background"; }

    @Override
    public String getStaticBackgroundText() { return "Static background"; }

    // ========== Preview ==========

    @Override
    public String getThemeDirectoryText() { return "Theme directory:"; }

    @Override
    public String getTarFileText() { return "Tar file:"; }

    @Override
    public String getCannotPreviewText() { return "Cannot preview"; }

    @Override
    public String getCannotDecodeImageText() { return "Cannot decode image"; }

    @Override
    public String getNoPreviewAvailableText() { return "No preview available"; }

    // ========== Language Info ==========

    @Override
    public String getLanguageName() { return NAME; }

    @Override
    public String getLanguageCode() { return CODE; }
}