package com.meng.service;

/**
 * Pure data class representing a theme entry in the list.
 * Can be either a theme directory (DIR) or a .tar package (TAR).
 * No Swing dependency.
 */
public class ThemeItem {
    public enum Type { DIR, TAR }

    public final Type type;
    public final String name;

    public ThemeItem(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Display string used in the JList.
     */
    public String getDisplayName() {
        return "[" + (type == Type.DIR ? "DIR" : "TAR") + "] " + name;
    }

    /**
     * Resolve the theme name from a display string like "[DIR] theme_sanae".
     */
    public static ThemeItem fromDisplayName(String displayName) {
        if (displayName == null) return null;
        if (displayName.startsWith("[DIR] ")) {
            return new ThemeItem(Type.DIR, displayName.substring(6));
        }
        if (displayName.startsWith("[TAR] ")) {
            return new ThemeItem(Type.TAR, displayName.substring(6));
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThemeItem)) return false;
        ThemeItem item = (ThemeItem) o;
        return type == item.type && name.equals(item.name);
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + name.hashCode();
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
