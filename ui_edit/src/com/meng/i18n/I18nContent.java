package com.meng.i18n;

/**
 * Abstract base class for internationalization content.
 * Subclasses implement getXXX() methods to return localized strings.
 */
public abstract class I18nContent {

    // ========== Menu Bar ==========

    /** Menu: Theme */
    public abstract String getMenuTheme();

    /** Menu: Edit */
    public abstract String getMenuEdit();

    /** Menu: Language */
    public abstract String getMenuLanguage();

    /** Menu: Help */
    public abstract String getMenuHelp();

    /** Menu item: New Theme */
    public abstract String getMenuNewTheme();

    /** Menu item: Pack Theme (.tar) */
    public abstract String getMenuPackTheme();

    /** Menu item: Unpack .tar */
    public abstract String getMenuUnpackTar();

    /** Menu item: Exit */
    public abstract String getMenuExit();

    /** Menu item: Save Config */
    public abstract String getMenuSaveConfig();

    /** Menu item: Open Theme Folder */
    public abstract String getMenuOpenFolder();

    /** Menu item: Refresh Theme List */
    public abstract String getMenuRefreshList();

    /** Menu item: About */
    public abstract String getMenuAbout();

    // ========== Config Editor Panel ==========

    /** Config Editor panel title */
    public abstract String getConfigEditorTitle();

    /** Table column: Key */
    public abstract String getColumnKey();

    /** Table column: Value */
    public abstract String getColumnValue();

    /** Status: No config loaded */
    public abstract String getStatusNoConfig();

    /** Status: Loaded prefix (e.g. "Loaded: ") */
    public abstract String getStatusLoadedPrefix();

    /** Help dialog title */
    public abstract String getConfigKeyHelpTitle();

    /** Add Key dialog title */
    public abstract String getAddKeyTitle();

    /** Add Key dialog message */
    public abstract String getAddKeyMessage();

    /** Nothing to Add dialog title */
    public abstract String getNothingToAddTitle();

    /** Nothing to Add dialog message */
    public abstract String getNothingToAddMessage();

    /** Browse button text */
    public abstract String getBrowseButtonText();

    /** File chooser dialog title */
    public abstract String getChooseFileTitle();

    /** No theme warning title */
    public abstract String getNoThemeTitle();

    /** No theme warning message */
    public abstract String getNoThemeMessage();

    /** Dynamic background disabled hint */
    public abstract String getDynamicBgDisabledHint();

    // ========== Theme Editor Frame ==========

    /** Theme Editor frame title */
    public abstract String getThemeEditorTitle();

    /** Themes border title */
    public abstract String getThemesBorderTitle();

    /** Preview tab title */
    public abstract String getPreviewTabTitle();

    /** Select theme label */
    public abstract String getSelectThemeLabel();

    /** Save button text */
    public abstract String getSaveButtonText();

    /** Open Folder button text */
    public abstract String getOpenFolderButtonText();

    /** Save As button text */
    public abstract String getSaveAsButtonText();

    /** New Theme button text */
    public abstract String getNewThemeButtonText();

    /** Delete Theme button text */
    public abstract String getDeleteThemeButtonText();

    /** Add Key button text */
    public abstract String getAddKeyButtonText();

    /** Remove Key button text */
    public abstract String getRemoveKeyButtonText();

    // ========== Dialog Titles & Messages ==========

    /** Saved success message */
    public abstract String getSavedMessage();

    /** Saved dialog title */
    public abstract String getSavedTitle();

    /** Error title */
    public abstract String getErrorTitle();

    /** Info title */
    public abstract String getInfoTitle();

    /** No theme selected title */
    public abstract String getNoThemeSelectedTitle();

    /** No theme selected message */
    public abstract String getNoThemeSelectedMessage();

    /** No tar selected title */
    public abstract String getNoTarSelectedTitle();

    /** No tar selected message */
    public abstract String getNoTarSelectedMessage();

    /** Confirm overwrite title */
    public abstract String getConfirmOverwriteTitle();

    /** Confirm overwrite message */
    public abstract String getConfirmOverwriteMessage();

    /** Unsaved changes title */
    public abstract String getUnsavedChangesTitle();

    /** Unsaved changes message */
    public abstract String getUnsavedChangesMessage();

    /** Pack successful title */
    public abstract String getPackSuccessfulTitle();

    /** Pack successful message */
    public abstract String getPackSuccessfulMessage();

    /** Pack failed title */
    public abstract String getPackFailedTitle();

    /** Unpack successful title */
    public abstract String getUnpackSuccessfulTitle();

    /** Unpack successful message */
    public abstract String getUnpackSuccessfulMessage();

    /** Unpack failed title */
    public abstract String getUnpackFailedTitle();

    /** Save As dialog title */
    public abstract String getSaveAsDialogTitle();

    /** New Theme dialog title */
    public abstract String getNewThemeDialogTitle();

    /** New Theme name prompt */
    public abstract String getNewThemeNamePrompt();

    /** Delete confirm title */
    public abstract String getDeleteConfirmTitle();

    /** Delete confirm message */
    public abstract String getDeleteConfirmMessage();

    /** Cannot open folder message */
    public abstract String getCannotOpenFolderMessage();

    /** About dialog title */
    public abstract String getAboutTitle();

    /** About dialog message */
    public abstract String getAboutMessage();

    // ========== Status Bar ==========

    /** Status: Found X theme(s) */
    public abstract String getStatusFoundThemes(int count);

    /** Status: Config saved */
    public abstract String getStatusConfigSaved();

    /** Status: Packed X.tar */
    public abstract String getStatusPacked(String name);

    /** Status: Unpacked X */
    public abstract String getStatusUnpacked(String name);

    /** Status: Created new theme X */
    public abstract String getStatusCreatedTheme(String name);

    /** Status: Position updated */
    public abstract String getStatusPositionUpdated(String prefix, int x, int y);

    /** Status: Color updated */
    public abstract String getStatusColorUpdated(String prefix);

    /** Status: Dynamic background */
    public abstract String getDynamicBackgroundText();

    /** Status: Static background */
    public abstract String getStaticBackgroundText();

    // ========== Preview ==========

    /** Preview: Theme directory text */
    public abstract String getThemeDirectoryText();

    /** Preview: Tar file text */
    public abstract String getTarFileText();

    /** Preview: Cannot preview */
    public abstract String getCannotPreviewText();

    /** Preview: Cannot decode image */
    public abstract String getCannotDecodeImageText();

    /** Preview: No preview available */
    public abstract String getNoPreviewAvailableText();

    // ========== Language Info ==========

    /** Language name (e.g. "English") */
    public abstract String getLanguageName();

    /** Language code (e.g. "en") */
    public abstract String getLanguageCode();

    // ========== Config Key Info ==========

    /**
     * Get editor key info/description for a config key.
     * Returns empty string if key not found.
     */
    public abstract String getEditorKeyInfo(String key);
}