package com.meng.gui;

import com.meng.i18n.I18nContent;
import com.meng.i18n.I18nManager;
import com.meng.service.MemoryTheme;
import com.meng.service.ThemeItem;
import com.meng.service.ThemeService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Main GUI window for the theme package editor.
 * Thin Swing layer — all business logic delegated to {@link ThemeService}.
 */
public class ThemeEditorFrame extends JFrame {

    private final ThemeService service;

    private final DefaultListModel<String> themeListModel = new DefaultListModel<>();
    private final JList<String> themeList = new JList<>(themeListModel);
    private final ConfigEditorPanel configEditor;
    private final JLabel imagePreviewLabel = new JLabel();
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
        
        // Set callback for real-time preview update
        configEditor.setOnConfigChanged(() -> {
            // Flush table edits to service and update preview
            configEditor.flushTableEdits();
            updatePreview();
        });
        
        // Set callbacks for toolbar buttons
        configEditor.setOnSaveAction(() -> saveConfig());
        configEditor.setOnOpenFolderAction(() -> openThemeFolder());

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
        JPanel previewPanel = new JPanel(new BorderLayout(5, 5));
        previewPanel.setBorder(BorderFactory.createTitledBorder(i18n.getPreviewTabTitle()));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        previewPanel.add(imagePreviewLabel, BorderLayout.CENTER);
        rightSplit.setRightComponent(previewPanel);

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

        imagePreviewLabel.setIcon(null);
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
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText(i18n.getCannotPreviewText() + ": " + err);
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
            imagePreviewLabel.setIcon(null);
            return;
        }
        
        // Show image preview (from in-memory bytes for TAR / filesystem for DIR)
        if (theme.sourceType == MemoryTheme.SourceType.DIR) {
            String imgPath = service.getBackgroundImagePath(theme.name);
            if (imgPath != null) showImagePreview(imgPath);
            else imagePreviewLabel.setIcon(null);
        } else {
            byte[] imgData = theme.getBackgroundImage() != null ? theme.getBackgroundImage().data : null;
            if (imgData != null) showImagePreviewFromBytes(imgData);
            else imagePreviewLabel.setIcon(null);
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

    private void showImagePreview(String imagePath) {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        try {
            BufferedImage originalImg = ImageIO.read(new File(imagePath));
            if (originalImg == null) {
                imagePreviewLabel.setIcon(null);
                imagePreviewLabel.setText(i18n.getCannotDecodeImageText());
                return;
            }
            int w = getPreviewWidth();
            int h = getPreviewHeight();
            if (w <= 0) w = originalImg.getWidth();
            if (h <= 0) h = originalImg.getHeight();
            
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImg, 0, 0, w, h, null);
            drawTextOverlay(g, w, h);
            g.dispose();
            
            imagePreviewLabel.setIcon(new ImageIcon(img));
            imagePreviewLabel.setText(null);
        } catch (Exception e) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText(i18n.getCannotPreviewText());
        }
    }

    private void showImagePreviewFromBytes(byte[] imageData) {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        try {
            BufferedImage originalImg = ImageIO.read(new ByteArrayInputStream(imageData));
            if (originalImg == null) {
                imagePreviewLabel.setIcon(null);
                imagePreviewLabel.setText(i18n.getCannotDecodeImageText());
                return;
            }
            int w = getPreviewWidth();
            int h = getPreviewHeight();
            if (w <= 0) w = originalImg.getWidth();
            if (h <= 0) h = originalImg.getHeight();
            
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImg, 0, 0, w, h, null);
            drawTextOverlay(g, w, h);
            g.dispose();
            
            imagePreviewLabel.setIcon(new ImageIcon(img));
            imagePreviewLabel.setText(null);
        } catch (IOException e) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText(i18n.getCannotPreviewText());
        }
    }

    /**
     * Draw text overlay on the preview image based on config.
     */
    private void drawTextOverlay(Graphics2D g, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw time (HH:mm:ss)
        drawTextElement(g, "time", new SimpleDateFormat("HH:mm:ss").format(new Date()));
        // Draw date (yyyy-MM-dd)
        drawTextElement(g, "date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        // Draw FPS (sample value)
        drawTextElement(g, "fps", "FPS: 60");
        // Draw CPU state (sample value)
        drawTextElement(g, "cpu_state", "CPU: 45%");
        // Draw memory state (sample value)
        drawTextElement(g, "memory_state", "MEM: 128M");
        // Draw storage state (sample value)
        drawTextElement(g, "storage_state", "SD: 1.2G");
    }

    /**
     * Draw a single text element with outline.
     * Uses SDL-style coordinate system: (x, y) is top-left corner of text.
     * Returns true if drawn, false if not configured.
     */
    private boolean drawTextElement(Graphics2D g, String prefix, String text) {
        try {
            // Check if this element is configured (section exists and has keys)
            if (service.getCurrentTheme() == null) return false;
            Map<String, String> section = service.getCurrentTheme().getConfig().getSection(prefix);
            if (section.isEmpty()) return false;  // Not configured, skip drawing
            
            int size = getIntValue(prefix + ".size", 12);
            int x = getIntValue(prefix + ".x", 0);
            int y = getIntValue(prefix + ".y", 0);
            Color color = parseColor(service.getValue(prefix + ".color"));
            Color outlineColor = parseColor(service.getValue(prefix + ".colorOutline"));
            int outlineSize = getIntValue(prefix + ".outlineSize", 2);  // Default outline size is 2
            
            // Load font if specified
            Font font = null;
            boolean fontError = false;
            String fontFile = service.getValue(prefix + ".font");
            if (fontFile != null && !fontFile.isEmpty()) {
                try {
                    font = loadFont(fontFile, size);
                } catch (Exception e) {
                    fontError = true;
                    font = new Font("SansSerif", Font.PLAIN, size);
                }
            }
            if (font == null) {
                font = new Font("SansSerif", Font.PLAIN, size);
            }
            g.setFont(font);
            
            // If font error, show error message instead
            String drawText = fontError ? prefix + " font error" : text;
            
            // Get font metrics to convert from top-left to baseline
            FontMetrics fm = g.getFontMetrics(font);
            int baselineY = y + fm.getAscent();  // Convert top to baseline
            
            // Default outline color is white (255,255,255) if not configured
            if (outlineColor == null) {
                outlineColor = new Color(255, 255, 255);
            }
            
            // Draw outline first (SDL-style: 8 directions)
            if (outlineSize > 0) {
                g.setColor(fontError ? Color.RED : outlineColor);
                int[][] directions = {
                    {-outlineSize, -outlineSize}, {0, -outlineSize}, {outlineSize, -outlineSize},
                    {-outlineSize, 0}, {outlineSize, 0},
                    {-outlineSize, outlineSize}, {0, outlineSize}, {outlineSize, outlineSize}
                };
                for (int[] dir : directions) {
                    g.drawString(drawText, x + dir[0], baselineY + dir[1]);
                }
            }
            
            // Draw main text
            if (color != null || fontError) {
                g.setColor(fontError ? Color.RED : color);
                g.drawString(drawText, x, baselineY);
            }
            return true;
        } catch (Exception e) {
            // Ignore drawing errors
            return false;
        }
    }

    /**
     * Parse color from config value.
     * Supports formats: "0xRRGGBBAA", "0xRRGGBB", "R,G,B,A", "R,G,B"
     */
    private Color parseColor(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            if (value.startsWith("0x") || value.startsWith("0X")) {
                long hex = Long.parseLong(value.substring(2), 16);
                if (value.length() > 8) { // 0xRRGGBBAA
                    int a = (int) ((hex >> 0) & 0xFF);
                    int b = (int) ((hex >> 8) & 0xFF);
                    int g = (int) ((hex >> 16) & 0xFF);
                    int r = (int) ((hex >> 24) & 0xFF);
                    return new Color(r, g, b, a);
                } else { // 0xRRGGBB
                    int b = (int) ((hex >> 0) & 0xFF);
                    int g = (int) ((hex >> 8) & 0xFF);
                    int r = (int) ((hex >> 16) & 0xFF);
                    return new Color(r, g, b, 255);
                }
            } else {
                String[] parts = value.split(",");
                if (parts.length >= 3) {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    int a = parts.length >= 4 ? Integer.parseInt(parts[3].trim()) : 255;
                    return new Color(r, g, b, a);
                }
            }
        } catch (Exception e) {
            // Ignore parse errors
        }
        return null;
    }

    /**
     * Load font from file.
     */
    private Font loadFont(String fontFile, float size) throws IOException, FontFormatException {
        // Try to find font in current theme
        byte[] fontData = null;
        if (service.getCurrentTheme() != null) {
            for (MemoryTheme.MemoryFile f : service.getCurrentTheme().getFiles()) {
                if (f.name.equals(fontFile) || f.name.endsWith("/" + fontFile)) {
                    fontData = f.data;
                    break;
                }
            }
        }
        if (fontData != null) {
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(fontData));
            return baseFont.deriveFont(size);
        }
        // Try filesystem path
        String basePath = service.getCurrentTheme() != null ? service.getCurrentTheme().sourcePath : "";
        File file = new File(basePath, fontFile);
        if (file.exists()) {
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, file);
            return baseFont.deriveFont(size);
        }
        return new Font("SansSerif", Font.PLAIN, (int) size);
    }

    /**
     * Get integer config value with default.
     */
    private int getIntValue(String key, int defaultValue) {
        try {
            String v = service.getValue(key);
            if (v != null && !v.isEmpty()) return Integer.parseInt(v);
        } catch (Exception ignored) {}
        return defaultValue;
    }

    /**
     * Get the configured screen width from the current theme config.
     * Falls back to image original width if not configured.
     */
    private int getPreviewWidth() {
        try {
            String w = service.getValue("hardware.screen_width");
            if (w != null && !w.isEmpty()) return Integer.parseInt(w);
        } catch (Exception ignored) {}
        return -1;
    }

    /**
     * Get the configured screen height from the current theme config.
     * Falls back to image original height if not configured.
     */
    private int getPreviewHeight() {
        try {
            String h = service.getValue("hardware.screen_height");
            if (h != null && !h.isEmpty()) return Integer.parseInt(h);
        } catch (Exception ignored) {}
        return -1;
    }
}