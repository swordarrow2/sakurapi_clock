package com.meng.gui;

import com.meng.i18n.I18nContent;
import com.meng.i18n.I18nManager;
import com.meng.service.MemoryTheme;
import com.meng.service.ThemeItem;
import com.meng.service.ThemeService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Main GUI window for the theme package editor.
 * Thin Swing layer — all business logic delegated to {@link ThemeService}.
 */
public class ThemeEditorFrame extends JFrame {

    private final ThemeService service;

    private final DefaultListModel<String> themeListModel = new DefaultListModel<>();
    private final JList<String> themeList = new JList<>(themeListModel);
    private final ConfigEditorPanel configEditor;
    private final DraggablePreviewPanel previewPanel;
    private final JLabel statusBar = new JLabel();

    // UI components for language switching
    private JPanel leftPanel;

    // Menu items for language switching
    private JMenu themeMenu;
    private JMenu editMenu;
    private JMenu langMenu;
    private JMenu helpMenu;
    private JMenuItem newThemeItem;
    private JMenuItem packItem;
    private JMenuItem unpackItem;
    private JMenuItem exitItem;
    private JMenuItem saveItem;
    private JMenuItem openFolderItem;
    private JMenuItem refreshItem;
    private JMenuItem aboutItem;

    private String currentThemeName; // name of currently selected DIR theme

    public ThemeEditorFrame() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        super.setTitle("SakuraPI Clock - " + i18n.getThemeEditorTitle());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 700);
        setLocationRelativeTo(null);

        // Paths matching the C++ logic
        String baseDir = System.getProperty("user.dir");
        service = new ThemeService(baseDir, "../themes", "themes");
        configEditor = new ConfigEditorPanel(service);
        previewPanel = new DraggablePreviewPanel(service);
        
        // Set callback for real-time preview update
        configEditor.setOnConfigChanged(() -> {
            // Flush table edits to service and update preview
            configEditor.flushTableEdits();
            updatePreview();
        });
        
        // Set callbacks for toolbar buttons
        configEditor.setOnSaveAction(() -> saveConfig());
        configEditor.setOnOpenFolderAction(() -> openThemeFolder());
        
        // Set callback for position updates from draggable preview
        previewPanel.setPositionUpdateCallback((prefix, newX, newY) -> {
            updatePositionInConfig(prefix, newX, newY);
        });
        
        // Set callback for color updates from draggable preview
        previewPanel.setColorUpdateCallback((prefix, colorValue, outlineColorValue) -> {
            updateColorInConfig(prefix, colorValue, outlineColorValue);
        });

        initMenuBar();
        initUI();
        refreshThemeList();
        updateUIText();
    }

    // ========== Menu Bar ==========

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        I18nContent i18n = I18nManager.getInstance().getCurrent();

        // ---- Theme ----
        themeMenu = new JMenu(i18n.getMenuTheme());
        newThemeItem = new JMenuItem(i18n.getMenuNewTheme());
        newThemeItem.addActionListener(e -> newTheme());
        themeMenu.add(newThemeItem);
        themeMenu.addSeparator();

        packItem = new JMenuItem(i18n.getMenuPackTheme());
        packItem.addActionListener(e -> packTheme());
        themeMenu.add(packItem);

        unpackItem = new JMenuItem(i18n.getMenuUnpackTar());
        unpackItem.addActionListener(e -> unpackTar());
        themeMenu.add(unpackItem);
        themeMenu.addSeparator();

        exitItem = new JMenuItem(i18n.getMenuExit());
        exitItem.addActionListener(e -> System.exit(0));
        themeMenu.add(exitItem);
        menuBar.add(themeMenu);

        // ---- Edit ----
        editMenu = new JMenu(i18n.getMenuEdit());
        saveItem = new JMenuItem(i18n.getMenuSaveConfig());
        saveItem.addActionListener(e -> saveConfig());
        editMenu.add(saveItem);

        openFolderItem = new JMenuItem(i18n.getMenuOpenFolder());
        openFolderItem.addActionListener(e -> openThemeFolder());
        editMenu.add(openFolderItem);
        editMenu.addSeparator();

        refreshItem = new JMenuItem(i18n.getMenuRefreshList());
        refreshItem.addActionListener(e -> refreshThemeList());
        editMenu.add(refreshItem);
        menuBar.add(editMenu);

        // ---- Language ----
        langMenu = new JMenu(i18n.getMenuLanguage());
        for (I18nContent lang : I18nManager.getInstance().getLanguages()) {
            JMenuItem langItem = new JMenuItem(lang.getLanguageName());
            langItem.setActionCommand(lang.getLanguageCode());
            langItem.addActionListener(this::onLanguageChanged);
            langMenu.add(langItem);
        }
        menuBar.add(langMenu);

        // ---- Help ----
        helpMenu = new JMenu(i18n.getMenuHelp());
        aboutItem = new JMenuItem(i18n.getMenuAbout());
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void onLanguageChanged(ActionEvent e) {
        String code = e.getActionCommand();
        I18nManager.getInstance().setCurrent(code);
        updateUIText();
        configEditor.updateUIText();
    }

    private void updateUIText() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        setTitle("SakuraPI Clock - " + i18n.getThemeEditorTitle());

        // Update menus
        themeMenu.setText(i18n.getMenuTheme());
        newThemeItem.setText(i18n.getMenuNewTheme());
        packItem.setText(i18n.getMenuPackTheme());
        unpackItem.setText(i18n.getMenuUnpackTar());
        exitItem.setText(i18n.getMenuExit());

        editMenu.setText(i18n.getMenuEdit());
        saveItem.setText(i18n.getMenuSaveConfig());
        openFolderItem.setText(i18n.getMenuOpenFolder());
        refreshItem.setText(i18n.getMenuRefreshList());

        langMenu.setText(i18n.getMenuLanguage());

        helpMenu.setText(i18n.getMenuHelp());
        aboutItem.setText(i18n.getMenuAbout());

        // Update UI components
        leftPanel.setBorder(BorderFactory.createTitledBorder(i18n.getThemesBorderTitle()));
        configEditor.updateUIText();
    }

    // ========== UI Layout ==========

    private void initUI() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(250);
        mainSplit.setResizeWeight(0.25);

        // Left: theme list
        leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder(i18n.getThemesBorderTitle()));
        themeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        themeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onThemeSelected();
        });
        leftPanel.add(new JScrollPane(themeList), BorderLayout.CENTER);
        mainSplit.setLeftComponent(leftPanel);

        // Right: config editor + preview (horizontal split)
        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplit.setDividerLocation(600);
        rightSplit.setResizeWeight(0.5);

        // Config editor panel
        JPanel editorPanel = new JPanel(new BorderLayout(5, 5));
        editorPanel.add(configEditor, BorderLayout.CENTER);
        rightSplit.setLeftComponent(editorPanel);

        // Preview panel (right of editor)
        JPanel previewContainer = new JPanel(new BorderLayout(5, 5));
        previewContainer.setBorder(BorderFactory.createTitledBorder(i18n.getPreviewTabTitle()));
        previewContainer.add(new JScrollPane(previewPanel), BorderLayout.CENTER);
        rightSplit.setRightComponent(previewContainer);

        mainSplit.setRightComponent(rightSplit);

        add(mainSplit, BorderLayout.CENTER);
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);
    }

    // ========== Theme List ==========

    private void refreshThemeList() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        themeListModel.clear();
        currentThemeName = null;
        configEditor.clear();

        List<ThemeItem> items = service.scanThemes();
        for (ThemeItem item : items) {
            themeListModel.addElement(item.getDisplayName());
        }
        statusBar.setText(" " + i18n.getStatusFoundThemes(items.size()));
    }

    private void onThemeSelected() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        String display = themeList.getSelectedValue();
        if (display == null) return;
        ThemeItem item = service.resolveItem(display);
        if (item == null) return;

        previewPanel.clear();
        configEditor.clear();

        // Load theme into MemoryTheme (DIR or TAR — same in-memory model)
        String err;
        if (item.type == ThemeItem.Type.DIR) {
            currentThemeName = item.name;
            err = service.loadThemeConfig(item.name);
        } else {
            currentThemeName = null; // TAR, don't track for "open folder"
            err = service.loadTarConfig(item.name);
        }

        if (err != null) {
            previewPanel.clear();
            statusBar.setText(" " + item.name + " — " + err);
            return;
        }

        // Update config editor and preview
        configEditor.updateFromService();
        updatePreview();

        // Status bar with dynamic background info
        String typeTag = item.type == ThemeItem.Type.TAR ? " (tar)" : "";
        if (service.isDynamicBackground(item.name)) {
            String dynInfo = service.getDynamicBackgroundInfo(item.name);
            String shortInfo = dynInfo != null ? dynInfo.replace("\n", " | ").trim() : i18n.getDynamicBackgroundText();
            statusBar.setText(" " + item.name + typeTag + " — " + shortInfo);
        } else {
            statusBar.setText(" " + item.name + typeTag + " — " + i18n.getStaticBackgroundText());
        }
    }

    /**
     * Update preview image with current config overlay.
     */
    private void updatePreview() {
        MemoryTheme theme = service.getCurrentTheme();
        if (theme == null) {
            previewPanel.clear();
            return;
        }
        
        // Show image preview (from in-memory bytes for TAR / filesystem for DIR)
        if (theme.sourceType == MemoryTheme.SourceType.DIR) {
            String imgPath = service.getBackgroundImagePath(theme.name);
            if (imgPath != null) previewPanel.loadImage(imgPath);
            else previewPanel.clear();
        } else {
            byte[] imgData = theme.getBackgroundImage() != null ? theme.getBackgroundImage().data : null;
            if (imgData != null) previewPanel.loadImageFromBytes(imgData);
            else previewPanel.clear();
        }
    }

    // ========== Menu / Toolbar Actions ==========

    private void saveConfig() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        configEditor.flushTableEdits();

        String err = service.saveCurrentConfig();
        if (err == null) {
            statusBar.setText(" " + i18n.getStatusConfigSaved());
            JOptionPane.showMessageDialog(this, i18n.getSavedMessage(), i18n.getSavedTitle(),
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, err, i18n.getErrorTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void packTheme() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        String display = themeList.getSelectedValue();
        if (display == null || !display.startsWith("[DIR] ")) {
            JOptionPane.showMessageDialog(this,
                    i18n.getNoThemeSelectedMessage(),
                    i18n.getNoThemeSelectedTitle(), JOptionPane.WARNING_MESSAGE);
            return;
        }
        String themeName = display.substring(6);

        // Confirm overwrite
        if (service.tarExists(themeName)) {
            int r = JOptionPane.showConfirmDialog(this,
                    i18n.getConfirmOverwriteMessage(), i18n.getConfirmOverwriteTitle(),
                    JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
        }

        // Save unsaved changes
        if (service.isConfigModified()) {
            int r = JOptionPane.showConfirmDialog(this,
                    i18n.getUnsavedChangesMessage(),
                    i18n.getUnsavedChangesTitle(), JOptionPane.YES_NO_CANCEL_OPTION);
            if (r == JOptionPane.CANCEL_OPTION) return;
            if (r == JOptionPane.YES_OPTION) service.saveCurrentConfig();
        }

        String err = service.packTheme(themeName);
        if (err == null) {
            statusBar.setText(" " + i18n.getStatusPacked(themeName));
            JOptionPane.showMessageDialog(this,
                    i18n.getPackSuccessfulMessage() + " '" + themeName + "'", i18n.getPackSuccessfulTitle(),
                    JOptionPane.INFORMATION_MESSAGE);
            refreshThemeList();
        } else {
            JOptionPane.showMessageDialog(this, err, i18n.getPackFailedTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void unpackTar() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        String display = themeList.getSelectedValue();
        if (display == null || !display.startsWith("[TAR] ")) {
            JOptionPane.showMessageDialog(this,
                    i18n.getNoTarSelectedMessage(),
                    i18n.getNoTarSelectedTitle(), JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tarName = display.substring(6);
        String themeName = tarName.contains(".") ? tarName.substring(0, tarName.lastIndexOf('.')) : tarName;

        // Confirm overwrite
        if (service.themeDirExists(themeName)) {
            int r = JOptionPane.showConfirmDialog(this,
                    i18n.getConfirmOverwriteMessage(),
                    i18n.getConfirmOverwriteTitle(), JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
        }

        String err = service.unpackTar(tarName);
        if (err == null) {
            statusBar.setText(" " + i18n.getStatusUnpacked(themeName));
            JOptionPane.showMessageDialog(this,
                    i18n.getUnpackSuccessfulMessage() + service.getThemeDirPath(themeName),
                    i18n.getUnpackSuccessfulTitle(), JOptionPane.INFORMATION_MESSAGE);
            refreshThemeList();
            selectListItem("[DIR] " + themeName);
        } else {
            JOptionPane.showMessageDialog(this, err, i18n.getUnpackFailedTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void newTheme() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        String name = JOptionPane.showInputDialog(this,
                i18n.getNewThemeNamePrompt(), i18n.getNewThemeDialogTitle(), JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        String err = service.createNewTheme(name.trim());
        if (err == null) {
            statusBar.setText(" " + i18n.getStatusCreatedTheme(name.trim()));
            refreshThemeList();
            selectListItem("[DIR] " + name.trim());
        } else {
            JOptionPane.showMessageDialog(this, err, i18n.getErrorTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openThemeFolder() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        if (currentThemeName == null) {
            JOptionPane.showMessageDialog(this, i18n.getNoThemeMessage(), i18n.getInfoTitle(),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Desktop.getDesktop().open(new File(service.getThemeDirPath(currentThemeName)));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, i18n.getCannotOpenFolderMessage() + e.getMessage(),
                    i18n.getErrorTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAbout() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        JOptionPane.showMessageDialog(this,
                i18n.getAboutMessage(),
                i18n.getAboutTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    // ========== Helpers ==========

    private void selectListItem(String displayName) {
        for (int i = 0; i < themeListModel.size(); i++) {
            if (themeListModel.get(i).equals(displayName)) {
                themeList.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Update position values in config when text element is dragged.
     * Updates both the service and the config editor table.
     */
    private void updatePositionInConfig(String prefix, int newX, int newY) {
        // Update service values
        service.setValue(prefix + ".x", String.valueOf(newX));
        service.setValue(prefix + ".y", String.valueOf(newY));
        
        // Update config editor table
        configEditor.updateKeyValue(prefix + ".x", String.valueOf(newX));
        configEditor.updateKeyValue(prefix + ".y", String.valueOf(newY));
        
        // Refresh preview to show updated positions
        previewPanel.repaint();
        
        // Update status bar
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        statusBar.setText(" " + i18n.getStatusPositionUpdated(prefix, newX, newY));
    }
    
    /**
     * Update color values in config when auto-detected from background.
     * Updates both the service and the config editor table.
     */
    private void updateColorInConfig(String prefix, String colorValue, String outlineColorValue) {
        // Update service values
        service.setValue(prefix + ".color", colorValue);
        service.setValue(prefix + ".colorOutline", outlineColorValue);
        
        // Update config editor table
        configEditor.updateKeyValue(prefix + ".color", colorValue);
        configEditor.updateKeyValue(prefix + ".colorOutline", outlineColorValue);
        
        // Update status bar
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        statusBar.setText(" " + i18n.getStatusColorUpdated(prefix));
    }
}