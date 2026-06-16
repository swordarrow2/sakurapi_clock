package com.meng;

import com.meng.gui.ThemeEditorFrame;

import javax.swing.*;

/**
 * SakuraPI Clock - Theme Package Editor
 * A GUI tool for editing, packing, and unpacking theme packages.
 * Mirrors the --pack / --unpack logic from sakurapi_clock.cpp.
 */
public class Main {

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch GUI
        SwingUtilities.invokeLater(() -> {
            ThemeEditorFrame frame = new ThemeEditorFrame();
            frame.setVisible(true);
        });
    }
}
