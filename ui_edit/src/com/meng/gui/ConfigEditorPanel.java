package com.meng.gui;

import com.meng.i18n.I18nContent;
import com.meng.i18n.I18nManager;
import com.meng.service.ConfigKeyInfo;
import com.meng.service.ThemeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Panel for editing flat dot-notation config files.
 * Pure Swing UI — all data access goes through {@link ThemeService}.
 * No direct dependency on ConfigParser or file I/O.
 *
 * File-type keys (ending with .file, .font, etc.) show a "Browse..." button
 * that opens a file chooser and copies the selected file into the theme directory.
 */
public class ConfigEditorPanel extends JPanel {
    private final ThemeService service;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel statusLabel;
    private final JButton addRowBtn;
    private final JButton removeRowBtn;
    private final JButton saveBtn;
    private final JButton openBtn;
    private Runnable onConfigChanged;  // Callback for preview update
    private Runnable onSaveAction;     // Callback for save button
    private Runnable onOpenFolderAction; // Callback for open folder button

    public ConfigEditorPanel(ThemeService service) {
        this.service = service;
        setLayout(new BorderLayout(5, 5));

        I18nContent i18n = I18nManager.getInstance().getCurrent();

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        saveBtn = new JButton(i18n.getSaveButtonText());
        saveBtn.addActionListener(e -> {
            if (onSaveAction != null) onSaveAction.run();
        });
        toolbar.add(saveBtn);

        openBtn = new JButton(i18n.getOpenFolderButtonText());
        openBtn.addActionListener(e -> {
            if (onOpenFolderAction != null) onOpenFolderAction.run();
        });
        toolbar.add(openBtn);

        toolbar.add(new JLabel(" | "));
        
        addRowBtn = new JButton("+ " + i18n.getAddKeyButtonText());
        addRowBtn.addActionListener(e -> addKey());
        toolbar.add(addRowBtn);

        removeRowBtn = new JButton("- " + i18n.getRemoveKeyButtonText());
        removeRowBtn.addActionListener(e -> removeSelectedKey());
        toolbar.add(removeRowBtn);

        add(toolbar, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{i18n.getColumnKey(), i18n.getColumnValue()}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column != 1) return false;
                // Disable editing for background.file when dynamic background is active
                String key = (String) getValueAt(row, 0);
                if ("background.file".equals(key) && service.isCurrentDynamicBackground()) {
                    return false;
                }
                return true;
            }
        };
        
        // Add table model listener for real-time preview update
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 1) {
                // Value column changed, notify for preview update
                notifyConfigChanged();
            }
        });
        
        table = new JTable(tableModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row >= 0 && col == 0) {
                    String key = (String) getValueAt(row, 0);
                    return ConfigKeyInfo.getInstance().getDescription(key);
                }
                return null;
            }
        };

        // Mouse listener for clickable info icon
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col != 0) return; // Only handle Key column

                int row = table.rowAtPoint(e.getPoint());
                String key = (String) table.getValueAt(row, 0);
                String desc = ConfigKeyInfo.getInstance().getDescription(key);
                if (desc.isEmpty()) return;

                // Check if click is on info icon area (right side of cell)
                int cellWidth = table.getColumnModel().getColumn(0).getWidth();
                int iconWidth = 20; // Approximate icon width
                int clickX = e.getX();
                if (clickX > cellWidth - iconWidth) {
                    // Show help dialog
                    I18nContent i18n = I18nManager.getInstance().getCurrent();
                    JOptionPane.showMessageDialog(table,
                            "<html><b>" + key + "</b><br><br>" + desc + "</html>",
                            i18n.getConfigKeyHelpTitle(),
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(0).setMinWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);

        // Key column renderer: key text + info icon (shows tooltip with description)
        table.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
            private final JPanel panel = new JPanel(new BorderLayout(2, 0));
            private final JLabel keyLabel = new JLabel();
            private final JLabel infoIcon = new JLabel("ℹ");

            {
                panel.setOpaque(true);
                keyLabel.setOpaque(false);
                keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD));
                keyLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
                infoIcon.setOpaque(false);
                infoIcon.setFont(infoIcon.getFont().deriveFont(Font.PLAIN, 12f));
                infoIcon.setForeground(new Color(100, 150, 200));
                infoIcon.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 5));
                panel.add(keyLabel, BorderLayout.CENTER);
                panel.add(infoIcon, BorderLayout.EAST);
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                String key = value != null ? value.toString() : "";
                keyLabel.setText(key);
                // Show info icon only if there's a description for this key
                infoIcon.setVisible(!ConfigKeyInfo.getInstance().getDescription(key).isEmpty());
                
                if (isSelected) {
                    panel.setBackground(t.getSelectionBackground());
                    keyLabel.setForeground(t.getSelectionForeground());
                    infoIcon.setForeground(t.getSelectionForeground());
                } else {
                    panel.setBackground(t.getBackground());
                    keyLabel.setForeground(t.getForeground());
                    infoIcon.setForeground(new Color(100, 150, 200));
                }
                return panel;
            }
        });

        // Custom value column: file keys get file editor, others use default text field
        table.getColumnModel().getColumn(1).setCellRenderer(new ValueRenderer());
        table.getColumnModel().getColumn(1).setCellEditor(new ValueEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("No config loaded");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        add(statusLabel, BorderLayout.SOUTH);
    }

    /**
     * Reload table data from the service's current config.
     * Called by ThemeEditorFrame when a theme is selected.
     */
    public void updateFromService() {
        tableModel.setRowCount(0);
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        String filePath = service.getCurrentThemePath();
        if (filePath == null || filePath.isEmpty()) {
            statusLabel.setText(i18n.getStatusNoConfig());
            return;
        }
        for (Map.Entry<String, String> entry : service.getFlatValues().entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        statusLabel.setText(i18n.getStatusLoadedPrefix() + filePath);
    }

    /**
     * Write all table edits back to the service.
     * Call this before service.saveCurrentConfig().
     */
    public void flushTableEdits() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        Map<String, String> flatValues = new LinkedHashMap<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String key = (String) tableModel.getValueAt(i, 0);
            String value = (String) tableModel.getValueAt(i, 1);
            if (key != null && !key.trim().isEmpty()) {
                flatValues.put(key.trim(), value != null ? value.trim() : "");
            }
        }
        service.setAllValues(flatValues);
    }

    public void clear() {
        tableModel.setRowCount(0);
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        statusLabel.setText(i18n.getStatusNoConfig());
    }
    
    /**
     * Update a key-value pair in the table.
     * If the key exists, updates its value; otherwise adds a new row.
     */
    public void updateKeyValue(String key, String value) {
        // Find existing row with this key
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingKey = (String) tableModel.getValueAt(i, 0);
            if (existingKey != null && existingKey.equals(key)) {
                tableModel.setValueAt(value, i, 1);
                return;
            }
        }
        // Key not found, add new row
        tableModel.addRow(new Object[]{key, value});
    }

    /**
     * Set callback for config changes (for preview update).
     */
    public void setOnConfigChanged(Runnable callback) {
        this.onConfigChanged = callback;
    }

    /**
     * Set callback for save button.
     */
    public void setOnSaveAction(Runnable callback) {
        this.onSaveAction = callback;
    }

    /**
     * Set callback for open folder button.
     */
    public void setOnOpenFolderAction(Runnable callback) {
        this.onOpenFolderAction = callback;
    }

    /**
     * Notify that config has changed.
     */
    private void notifyConfigChanged() {
        if (onConfigChanged != null) {
            SwingUtilities.invokeLater(onConfigChanged);
        }
    }

    /**
     * Update UI text after language change.
     */
    public void updateUIText() {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        setBorder(BorderFactory.createTitledBorder(i18n.getConfigEditorTitle()));
        saveBtn.setText(i18n.getSaveButtonText());
        openBtn.setText(i18n.getOpenFolderButtonText());
        addRowBtn.setText("+ " + i18n.getAddKeyButtonText());
        removeRowBtn.setText("- " + i18n.getRemoveKeyButtonText());
        table.getColumnModel().getColumn(0).setHeaderValue(i18n.getColumnKey());
        table.getColumnModel().getColumn(1).setHeaderValue(i18n.getColumnValue());
        table.getTableHeader().repaint();
    }

    // ========== Private ==========

    /**
     * Shows a dropdown with all available config keys and key prefixes.
     * Selecting a prefix adds all keys under it; selecting a single key adds just that one.
     */
    private void addKey() {
        // Collect existing keys in table
        Set<String> existingKeys = new HashSet<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String k = (String) tableModel.getValueAt(i, 0);
            if (k != null) existingKeys.add(k.trim());
        }

        // Collect unique prefixes (first segment before the dot)
        Set<String> allPrefixes = new TreeSet<>();
        for (String fullKey : ConfigKeyInfo.getInstance().getKeys()) {
            int dot = fullKey.indexOf('.');
            if (dot > 0) {
                allPrefixes.add(fullKey.substring(0, dot));
            }
        }

        // Build options list: prefixes first, then individual keys, sorted
        java.util.List<AddKeyOption> options = new ArrayList<>();

        // Add prefix options (only if not all sub-keys are already in the table)
        for (String prefix : allPrefixes) {
            boolean anyMissing = false;
            for (String fullKey : ConfigKeyInfo.getInstance().getKeys()) {
                if (fullKey.startsWith(prefix + ".") && !existingKeys.contains(fullKey)) {
                    anyMissing = true;
                    break;
                }
            }
            if (anyMissing) {
                options.add(new AddKeyOption(prefix, true));
            }
        }

        // Add individual key options
        for (String fullKey : ConfigKeyInfo.getInstance().getKeys()) {
            if (!existingKeys.contains(fullKey)) {
                options.add(new AddKeyOption(fullKey, false));
            }
        }

        if (options.isEmpty()) {
            I18nContent i18n = I18nManager.getInstance().getCurrent();
            JOptionPane.showMessageDialog(this,
                    i18n.getNothingToAddMessage(),
                    i18n.getNothingToAddTitle(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show dropdown dialog
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        AddKeyOption selected = (AddKeyOption) JOptionPane.showInputDialog(
                this,
                i18n.getAddKeyMessage(),
                i18n.getAddKeyTitle(),
                JOptionPane.PLAIN_MESSAGE,
                null,
                options.toArray(),
                options.get(0));

        if (selected == null) return;

        if (selected.isPrefix) {
            // Add all keys under this prefix
            for (String fullKey : ConfigKeyInfo.getInstance().getKeys()) {
                if (fullKey.startsWith(selected.name + ".") && !existingKeys.contains(fullKey)) {
                    tableModel.addRow(new Object[]{fullKey, getDefaultValue(fullKey)});
                    existingKeys.add(fullKey);
                }
            }
        } else {
            // Add single key
            tableModel.addRow(new Object[]{selected.name, getDefaultValue(selected.name)});
        }
    }

    /**
     * Get the default value for a key.
     * First checks loaded config, then falls back to ConfigKeyInfo default.
     */
    private String getDefaultValue(String key) {
        // First check loaded config
        Map<String, String> allValues = service.getFlatValues();
        if (allValues != null && allValues.containsKey(key)) {
            return allValues.get(key);
        }
        // Fall back to ConfigKeyInfo default
        return ConfigKeyInfo.getInstance().getDefaultValue(key);
    }

    private void removeSelectedKey() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            tableModel.removeRow(row);
        }
    }

    /**
     * Internal option representation for the Add Key dropdown.
     * Can represent either a prefix (group) or a single full key.
     */
    private static class AddKeyOption {
        final String name;
        final boolean isPrefix;

        AddKeyOption(String name, boolean isPrefix) {
            this.name = name;
            this.isPrefix = isPrefix;
        }

        @Override
        public String toString() {
            return isPrefix ? name + ".* (add all " + name + " keys)" : name;
        }
    }

    // ===================== Value Cell Renderer =====================

    /**
     * Renders the value cell.
     * - For file keys: shows value text + "..." indicator
     * - For color keys: shows value text + color preview box
     * - For background.file when dynamic: shows "[Dynamic BG - disabled]" with gray color
     */
    private class ValueRenderer extends JPanel implements TableCellRenderer {
        private final JLabel valueLabel = new JLabel();
        private final JLabel iconLabel = new JLabel();
        private final JLabel colorPreviewBox = new JLabel();

        ValueRenderer() {
            setLayout(new BorderLayout(4, 0));
            setOpaque(true);
            iconLabel.setFont(iconLabel.getFont().deriveFont(16f));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 4));
            valueLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            colorPreviewBox.setPreferredSize(new Dimension(20, 20));
            colorPreviewBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            colorPreviewBox.setOpaque(true);
            add(valueLabel, BorderLayout.CENTER);
            add(iconLabel, BorderLayout.EAST);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            String key = (String) t.getValueAt(row, 0);
            String valueStr = value != null ? value.toString() : "";
            valueLabel.setText(valueStr);

            if (isSelected) {
                setBackground(t.getSelectionBackground());
                setForeground(t.getSelectionForeground());
            } else {
                setBackground(t.getBackground());
                setForeground(t.getForeground());
            }
            valueLabel.setForeground(getForeground());

            // Dynamic background: show disabled state for background.file
            if ("background.file".equals(key) && service.isCurrentDynamicBackground()) {
                I18nContent i18n = I18nManager.getInstance().getCurrent();
                valueLabel.setText("[" + i18n.getDynamicBgDisabledHint() + "]");
                valueLabel.setForeground(isSelected ? getForeground() : Color.GRAY);
                iconLabel.setText("");
                return this;
            }

            // Color key: show color preview
            if (isColorKey(key)) {
                Color c = parseColorValue(valueStr);
                if (c != null) {
                    colorPreviewBox.setBackground(c);
                } else {
                    colorPreviewBox.setBackground(Color.WHITE);
                }
                remove(iconLabel);
                add(colorPreviewBox, BorderLayout.EAST);
            } else if (ConfigKeyInfo.getInstance().isFileKey(key) && !valueStr.isEmpty()) {
                iconLabel.setText("...");
                iconLabel.setForeground(new Color(80, 120, 200));
                remove(colorPreviewBox);
                add(iconLabel, BorderLayout.EAST);
            } else {
                iconLabel.setText("");
                remove(colorPreviewBox);
                add(iconLabel, BorderLayout.EAST);
            }

            return this;
        }

        private boolean isColorKey(String key) {
            return key != null && key.toLowerCase().contains("color");
        }

        private Color parseColorValue(String value) {
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
    }

    // ===================== Value Cell Editor =====================

    /**
     * Custom cell editor for the Value column.
     * - For file keys: shows a text field (direct path input) + "Browse..." button
     *   If the user types an external file path, it's automatically copied into the theme.
     * - For color keys: shows a text field + color picker button
     * - For background.file when dynamic background: shows disabled text field
     * - For other keys: shows a plain text field
     */
    private class ValueEditor extends AbstractCellEditor implements TableCellEditor {
        // Shared components for file key editing
        private final JPanel fileEditorPanel;
        private final JTextField fileTextField;
        private final JButton browseBtn;
        // Shared components for color key editing
        private final JPanel colorEditorPanel;
        private final JTextField colorTextField;
        private final JButton colorPickerBtn;
        private final JLabel colorPreview;
        // Shared components for numeric key editing
        private final JPanel numericEditorPanel;
        private final JTextField numericTextField;
        private final JButton increaseBtn;
        private final JButton decreaseBtn;
        // Shared component for plain text editing
        private final JTextField plainTextField;
        // The actual editor component to return
        private Component currentEditor;
        private String currentKey;

        ValueEditor() {
            I18nContent i18n = I18nManager.getInstance().getCurrent();
            
            // File key editor: text field + Browse button
            fileEditorPanel = new JPanel(new BorderLayout(4, 0));
            fileTextField = new JTextField();
            browseBtn = new JButton(i18n.getBrowseButtonText());
            browseBtn.setPreferredSize(new Dimension(85, 24));
            browseBtn.setMargin(new Insets(0, 4, 0, 4));
            browseBtn.addActionListener(e -> browseFile());
            fileEditorPanel.add(fileTextField, BorderLayout.CENTER);
            fileEditorPanel.add(browseBtn, BorderLayout.EAST);

            // Color key editor: text field + color preview + picker button
            colorEditorPanel = new JPanel(new BorderLayout(4, 0));
            colorTextField = new JTextField();
            colorPreview = new JLabel();
            colorPreview.setPreferredSize(new Dimension(24, 24));
            colorPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            colorPreview.setOpaque(true);
            colorPickerBtn = new JButton("...");
            colorPickerBtn.setPreferredSize(new Dimension(30, 24));
            colorPickerBtn.setMargin(new Insets(0, 4, 0, 4));
            colorPickerBtn.addActionListener(e -> pickColor());
            JPanel colorRightPanel = new JPanel(new BorderLayout(2, 0));
            colorRightPanel.add(colorPreview, BorderLayout.CENTER);
            colorRightPanel.add(colorPickerBtn, BorderLayout.EAST);
            colorEditorPanel.add(colorTextField, BorderLayout.CENTER);
            colorEditorPanel.add(colorRightPanel, BorderLayout.EAST);

            // Numeric key editor: text field + increase/decrease buttons
            numericEditorPanel = new JPanel(new BorderLayout(4, 0));
            numericTextField = new JTextField();
            increaseBtn = new JButton("+");
            increaseBtn.setPreferredSize(new Dimension(30, 24));
            increaseBtn.setMargin(new Insets(0, 4, 0, 4));
            increaseBtn.addActionListener(e -> adjustNumericValue(numericTextField, 1, currentKey));
            decreaseBtn = new JButton("-");
            decreaseBtn.setPreferredSize(new Dimension(30, 24));
            decreaseBtn.setMargin(new Insets(0, 4, 0, 4));
            decreaseBtn.addActionListener(e -> adjustNumericValue(numericTextField, -1, currentKey));
            JPanel numericRightPanel = new JPanel(new BorderLayout(2, 0));
            numericRightPanel.add(decreaseBtn, BorderLayout.WEST);
            numericRightPanel.add(increaseBtn, BorderLayout.EAST);
            numericEditorPanel.add(numericTextField, BorderLayout.CENTER);
            numericEditorPanel.add(numericRightPanel, BorderLayout.EAST);

            // Plain text editor
            plainTextField = new JTextField();
        }
        
        /**
         * Check if a key is a numeric key (can be adjusted by +/- buttons).
         */
        private boolean isNumericKey(String key) {
            if (key == null) return false;
            return key.endsWith(".x") || key.endsWith(".y") 
                || key.endsWith(".size") || key.endsWith(".outlineSize")
                || key.equals("cfg.delay") || key.equals("hardware.screen_width") 
                || key.equals("hardware.screen_height");
        }
        
        /**
         * Adjust numeric value in text field by +/- buttons.
         * @param field the text field
         * @param direction 1 for increase, -1 for decrease
         * @param key the config key (to determine step size)
         */
        private void adjustNumericValue(JTextField field, int direction, String key) {
            String text = field.getText();
            try {
                int value = Integer.parseInt(text.trim());
                // Determine step size based on key type
                int step = 5; // Default: 5 pixels for position
                if (key.endsWith(".size") || key.endsWith(".outlineSize")) {
                    step = 1; // 1 for font size and outline
                } else if (key.equals("cfg.delay")) {
                    step = 1; // 1 frame for delay
                } else if (key.startsWith("hardware.")) {
                    step = 10; // 10 for screen dimensions
                }
                value += direction * step;
                // Clamp to minimum 0 for most values
                if (value < 0 && !key.equals("hardware.screen_width") && !key.equals("hardware.screen_height")) {
                    value = 0;
                }
                field.setText(String.valueOf(value));
                // Trigger preview update
                notifyConfigChanged();
            } catch (NumberFormatException ex) {
                // Not a valid integer, ignore
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object value,
                                                     boolean isSelected, int row, int column) {
            currentKey = (String) t.getValueAt(row, 0);
            String valueStr = value != null ? value.toString() : "";

            if (ConfigKeyInfo.getInstance().isFileKey(currentKey)) {
                // Dynamic background: disable editing for background.file
                boolean isDynamicBgFile = "background.file".equals(currentKey)
                        && service.isCurrentDynamicBackground();
                fileTextField.setText(valueStr);
                fileTextField.setEnabled(!isDynamicBgFile);
                browseBtn.setEnabled(!isDynamicBgFile);
                if (isDynamicBgFile) {
                    I18nContent i18n = I18nManager.getInstance().getCurrent();
                    fileTextField.setText("[" + i18n.getDynamicBgDisabledHint() + "]");
                }
                currentEditor = fileEditorPanel;
            } else if (isColorKey(currentKey)) {
                // Color key: show color editor
                colorTextField.setText(valueStr);
                updateColorPreview(valueStr);
                currentEditor = colorEditorPanel;
            } else if (isNumericKey(currentKey)) {
                // Numeric key: show numeric editor with +/- buttons
                numericTextField.setText(valueStr);
                currentEditor = numericEditorPanel;
            } else {
                plainTextField.setText(valueStr);
                currentEditor = plainTextField;
            }

            // Start editing with focus on the text field
            SwingUtilities.invokeLater(() -> {
                JTextField focusField = getFocusTextField();
                if (focusField != null) {
                    focusField.requestFocusInWindow();
                    if (focusField.isEnabled()) {
                        focusField.selectAll();
                    }
                }
            });

            return currentEditor;
        }

        private JTextField getFocusTextField() {
            if (currentEditor == fileEditorPanel) return fileTextField;
            if (currentEditor == colorEditorPanel) return colorTextField;
            if (currentEditor == numericEditorPanel) return numericTextField;
            return plainTextField;
        }

        /**
         * Check if a key is a color key (contains "color" in the key name).
         */
        private boolean isColorKey(String key) {
            return key != null && key.toLowerCase().contains("color");
        }

        /**
         * Update the color preview label based on the current text value.
         */
        private void updateColorPreview(String value) {
            Color c = parseColorValue(value);
            if (c != null) {
                colorPreview.setBackground(c);
            } else {
                colorPreview.setBackground(Color.WHITE);
            }
        }

        /**
         * Parse color from config value.
         * Supports formats: "0xRRGGBBAA", "0xRRGGBB", "R,G,B,A", "R,G,B"
         */
        private Color parseColorValue(String value) {
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
         * Open color picker dialog and update the text field.
         */
        private void pickColor() {
            Color currentColor = parseColorValue(colorTextField.getText());
            if (currentColor == null) {
                currentColor = Color.WHITE;
            }
            // For color picker, we need to ignore alpha (use opaque color)
            Color opaqueColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue());
            
            Color selected = JColorChooser.showDialog(colorEditorPanel, "Choose Color", opaqueColor);
            if (selected != null) {
                // Preserve original alpha if it was set
                int alpha = currentColor.getAlpha();
                Color finalColor = new Color(selected.getRed(), selected.getGreen(), selected.getBlue(), alpha);
                
                // Format as "R,G,B,A" (matching the config format)
                String formatted = finalColor.getRed() + "," + finalColor.getGreen() + "," 
                                 + finalColor.getBlue() + "," + finalColor.getAlpha();
                colorTextField.setText(formatted);
                updateColorPreview(formatted);
                
                // Trigger preview update
                notifyConfigChanged();
            }
        }

        @Override
        public Object getCellEditorValue() {
            if (currentEditor == fileEditorPanel) {
                String text = fileTextField.getText();
                I18nContent i18n = I18nManager.getInstance().getCurrent();
                String disabledHint = "[" + i18n.getDynamicBgDisabledHint() + "]";
                // If user typed an external file path (contains path separator), copy it into theme
                if (text != null && !text.isEmpty() && !text.equals(disabledHint)) {
                    String processed = processFileInput(text);
                    return processed != null ? processed : text;
                }
                return text;
            } else if (currentEditor == colorEditorPanel) {
                return colorTextField.getText();
            } else if (currentEditor == numericEditorPanel) {
                return numericTextField.getText();
            }
            return plainTextField.getText();
        }

        /**
         * Process file input: if the text looks like an external path (contains separator),
         * copy the file into the theme and return the theme-relative filename.
         * Otherwise return the text as-is (already a theme-relative filename).
         */
        private String processFileInput(String input) {
            if (input == null || input.trim().isEmpty()) return input;
            input = input.trim();

            // Check if it looks like an external path (contains path separator)
            boolean isExternalPath = input.contains(File.separator) || input.contains("/");
            if (!isExternalPath) return input;

            // Verify the file exists
            File f = new File(input);
            if (!f.exists() || !f.isFile()) {
                // Not a valid external path, return as-is (might be a typo)
                return input;
            }

            // Copy the file into the theme
            String fileName = service.copyFileToTheme(input);
            if (fileName != null) {
                return fileName;
            }
            // If copy failed, return original input
            return input;
        }

        private void browseFile() {
            // Use AWT FileDialog — native Windows file chooser with editable address bar
            I18nContent i18n = I18nManager.getInstance().getCurrent();
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(ConfigEditorPanel.this);
            FileDialog dialog = new FileDialog(frame, i18n.getChooseFileTitle(), FileDialog.LOAD);
            String[] exts = ConfigKeyInfo.getInstance().getFileExtensions(currentKey);
            if (exts.length > 0) {
                // Build filter like "*.jpg;*.png"
                dialog.setFile("*." + String.join(";*.", exts));
            }
            dialog.setVisible(true);

            String dir = dialog.getDirectory();
            String file = dialog.getFile();
            if (dir == null || file == null) return;

            String fullPath = dir + file;
            String fileName = service.copyFileToTheme(fullPath);
            if (fileName != null) {
                fileTextField.setText(fileName);
            } else {
                JOptionPane.showMessageDialog(ConfigEditorPanel.this,
                        i18n.getNoThemeMessage(),
                        i18n.getNoThemeTitle(), JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
