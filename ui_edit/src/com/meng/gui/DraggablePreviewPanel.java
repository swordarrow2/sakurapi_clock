package com.meng.gui;

import com.meng.i18n.I18nContent;
import com.meng.i18n.I18nManager;
import com.meng.service.MemoryTheme;
import com.meng.service.ThemeService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Preview panel with draggable text elements.
 * Each text element can be dragged and position updates are sent back to the editor.
 */
public class DraggablePreviewPanel extends JPanel {
    private final ThemeService service;
    private BufferedImage backgroundImage;
    private int previewWidth = -1;
    private int previewHeight = -1;
    
    // Text elements for dragging
    private final List<TextElement> textElements = new ArrayList<>();
    private TextElement selectedElement = null;
    private Point dragStart = null;
    private int originalX, originalY;
    
    // Callback for position updates
    private PositionUpdateCallback positionCallback;
    private ColorUpdateCallback colorCallback;
    
    public interface PositionUpdateCallback {
        void onPositionUpdated(String prefix, int newX, int newY);
    }
    
    public interface ColorUpdateCallback {
        void onColorUpdated(String prefix, String colorValue, String outlineColorValue);
    }
    
    public DraggablePreviewPanel(ThemeService service) {
        this.service = service;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setPreferredSize(new Dimension(400, 300));
        
        // Mouse listeners for dragging
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                // Find clicked element (reverse order to get topmost)
                for (int i = textElements.size() - 1; i >= 0; i--) {
                    TextElement elem = textElements.get(i);
                    if (elem.bounds != null && elem.bounds.contains(p)) {
                        selectedElement = elem;
                        dragStart = p;
                        originalX = elem.x;
                        originalY = elem.y;
                        repaint();
                        return;
                    }
                }
                selectedElement = null;
                repaint();
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedElement != null && dragStart != null) {
                    Point p = e.getPoint();
                    int dx = p.x - dragStart.x;
                    int dy = p.y - dragStart.y;
                    
                    // Update element position
                    selectedElement.x = originalX + dx;
                    selectedElement.y = originalY + dy;
                    
                    // Update bounds
                    updateElementBounds(selectedElement);
                    
                    repaint();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedElement != null && dragStart != null) {
                    Point p = e.getPoint();
                    int dx = p.x - dragStart.x;
                    int dy = p.y - dragStart.y;
                    
                    if (dx != 0 || dy != 0) {
                        // Calculate new position
                        int newX = originalX + dx;
                        int newY = originalY + dy;
                        
                        // Clamp to valid range
                        newX = Math.max(0, Math.min(newX, previewWidth > 0 ? previewWidth : getWidth()));
                        newY = Math.max(0, Math.min(newY, previewHeight > 0 ? previewHeight : getHeight()));
                        
                        // Notify callback
                        if (positionCallback != null) {
                            positionCallback.onPositionUpdated(selectedElement.prefix, newX, newY);
                        }
                        
                        // Update element position
                        selectedElement.x = newX;
                        selectedElement.y = newY;
                        updateElementBounds(selectedElement);
                    }
                }
                selectedElement = null;
                dragStart = null;
                repaint();
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                // Change cursor when hovering over a text element
                Point p = e.getPoint();
                boolean overElement = false;
                for (int i = textElements.size() - 1; i >= 0; i--) {
                    TextElement elem = textElements.get(i);
                    if (elem.bounds != null && elem.bounds.contains(p)) {
                        overElement = true;
                        break;
                    }
                }
                setCursor(overElement ? Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) : Cursor.getDefaultCursor());
            }
        };
        
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }
    
    public void setPositionUpdateCallback(PositionUpdateCallback callback) {
        this.positionCallback = callback;
    }
    
    public void setColorUpdateCallback(ColorUpdateCallback callback) {
        this.colorCallback = callback;
    }
    
    /**
     * Load background image from file path.
     */
    public void loadImage(String imagePath) {
        I18nContent i18n = I18nManager.getInstance().getCurrent();
        try {
            BufferedImage originalImg = ImageIO.read(new File(imagePath));
            if (originalImg == null) {
                backgroundImage = null;
                return;
            }
            backgroundImage = originalImg;
            previewWidth = getPreviewWidth();
            previewHeight = getPreviewHeight();
            if (previewWidth <= 0) previewWidth = originalImg.getWidth();
            if (previewHeight <= 0) previewHeight = originalImg.getHeight();
            setPreferredSize(new Dimension(previewWidth, previewHeight));
            revalidate();
            repaint();
        } catch (Exception e) {
            backgroundImage = null;
        }
    }
    
    /**
     * Load background image from byte array.
     */
    public void loadImageFromBytes(byte[] imageData) {
        try {
            BufferedImage originalImg = ImageIO.read(new ByteArrayInputStream(imageData));
            if (originalImg == null) {
                backgroundImage = null;
                return;
            }
            backgroundImage = originalImg;
            previewWidth = getPreviewWidth();
            previewHeight = getPreviewHeight();
            if (previewWidth <= 0) previewWidth = originalImg.getWidth();
            if (previewHeight <= 0) previewHeight = originalImg.getHeight();
            setPreferredSize(new Dimension(previewWidth, previewHeight));
            revalidate();
            repaint();
        } catch (IOException e) {
            backgroundImage = null;
        }
    }
    
    /**
     * Clear the preview.
     */
    public void clear() {
        backgroundImage = null;
        textElements.clear();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundImage == null) {
            I18nContent i18n = I18nManager.getInstance().getCurrent();
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            g.drawString(i18n.getNoPreviewAvailableText(), 10, 20);
            return;
        }
        
        int w = previewWidth > 0 ? previewWidth : getWidth();
        int h = previewHeight > 0 ? previewHeight : getHeight();
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw background image
        g2d.drawImage(backgroundImage, 0, 0, w, h, null);
        
        // Clear and rebuild text elements
        textElements.clear();
        
        // Draw text elements
        drawTextElement(g2d, "time", new SimpleDateFormat("HH:mm:ss").format(new Date()));
        drawTextElement(g2d, "date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        drawTextElement(g2d, "fps", "FPS: 60");
        drawTextElement(g2d, "cpu_state", "CPU: 45%");
        drawTextElement(g2d, "memory_state", "MEM: 128M");
        drawTextElement(g2d, "storage_state", "SD: 1.2G");
        
        // Draw selection highlight
        if (selectedElement != null && selectedElement.bounds != null) {
            g2d.setColor(new Color(0, 120, 215, 100));
            g2d.fill(selectedElement.bounds);
            g2d.setColor(new Color(0, 120, 215));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
            g2d.draw(selectedElement.bounds);
        }
        
        g2d.dispose();
    }
    
    private void drawTextElement(Graphics2D g, String prefix, String text) {
        try {
            if (service.getCurrentTheme() == null) return;
            Map<String, String> section = service.getCurrentTheme().getConfig().getSection(prefix);
            if (section.isEmpty()) return;
            
            int size = getIntValue(prefix + ".size", 12);
            int x = getIntValue(prefix + ".x", 0);
            int y = getIntValue(prefix + ".y", 0);
            Color color = parseColor(service.getValue(prefix + ".color"));
            Color outlineColor = parseColor(service.getValue(prefix + ".colorOutline"));
            int outlineSize = getIntValue(prefix + ".outlineSize", 2);
            
            // Load font
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
            
            String drawText = fontError ? prefix + " font error" : text;
            
            FontMetrics fm = g.getFontMetrics(font);
            int baselineY = y + fm.getAscent();
            int textWidth = fm.stringWidth(drawText);
            int textHeight = fm.getHeight();
            
            // Check if colors are configured
            String colorConfig = service.getValue(prefix + ".color");
            String outlineColorConfig = service.getValue(prefix + ".colorOutline");
            boolean colorNotConfigured = colorConfig == null || colorConfig.isEmpty();
            boolean outlineColorNotConfigured = outlineColorConfig == null || outlineColorConfig.isEmpty();
            
            // Auto-detect text color based on background dominant color if not configured
            // Outline is always white for better visibility
            if (color == null) {
                color = getDominantColor(x, y, textWidth, textHeight);
            }
            if (outlineColor == null) {
                outlineColor = Color.WHITE;  // Always use white outline
            }
            
            // Update config if colors were not configured
            if (colorNotConfigured || outlineColorNotConfigured) {
                String colorValue = colorToHexString(color);
                String outlineValue = colorToHexString(outlineColor);
                if (colorCallback != null) {
                    colorCallback.onColorUpdated(prefix, colorValue, outlineValue);
                }
            }
            
            // Draw outline
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
            
            // Create text element for dragging
            TextElement elem = new TextElement();
            elem.prefix = prefix;
            elem.x = x;
            elem.y = y;
            elem.text = drawText;
            elem.font = font;
            updateElementBounds(elem);
            textElements.add(elem);
            
        } catch (Exception e) {
            // Ignore drawing errors
        }
    }
    
    private void updateElementBounds(TextElement elem) {
        if (elem.font == null || elem.text == null) {
            elem.bounds = null;
            return;
        }
        FontMetrics fm = getFontMetrics(elem.font);
        int textWidth = fm.stringWidth(elem.text);
        int textHeight = fm.getHeight();
        // Bounds: x is left, y is top (baseline - ascent)
        elem.bounds = new Rectangle(elem.x, elem.y, textWidth, textHeight);
    }
    
    private Color parseColor(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            if (value.startsWith("0x") || value.startsWith("0X")) {
                long hex = Long.parseLong(value.substring(2), 16);
                if (value.length() > 8) {
                    int a = (int) ((hex >> 0) & 0xFF);
                    int b = (int) ((hex >> 8) & 0xFF);
                    int g = (int) ((hex >> 16) & 0xFF);
                    int r = (int) ((hex >> 24) & 0xFF);
                    return new Color(r, g, b, a);
                } else {
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
            // Ignore
        }
        return null;
    }
    
    private Font loadFont(String fontFile, float size) throws IOException, FontFormatException {
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
        String basePath = service.getCurrentTheme() != null ? service.getCurrentTheme().sourcePath : "";
        File file = new File(basePath, fontFile);
        if (file.exists()) {
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, file);
            return baseFont.deriveFont(size);
        }
        return new Font("SansSerif", Font.PLAIN, (int) size);
    }
    
    private int getIntValue(String key, int defaultValue) {
        try {
            String v = service.getValue(key);
            if (v != null && !v.isEmpty()) return Integer.parseInt(v);
        } catch (Exception ignored) {}
        return defaultValue;
    }
    
    private int getPreviewWidth() {
        try {
            String w = service.getValue("hardware.screen_width");
            if (w != null && !w.isEmpty()) return Integer.parseInt(w);
        } catch (Exception ignored) {}
        return -1;
    }
    
    private int getPreviewHeight() {
        try {
            String h = service.getValue("hardware.screen_height");
            if (h != null && !h.isEmpty()) return Integer.parseInt(h);
        } catch (Exception ignored) {}
        return -1;
    }
    
    /**
     * Get dominant color from background image at text position.
     * Returns the average color of the area, which represents the main tone.
     */
    private Color getDominantColor(int x, int y, int width, int height) {
        if (backgroundImage == null) {
            return Color.WHITE;
        }
        
        // Clamp coordinates to image bounds
        int imgW = backgroundImage.getWidth();
        int imgH = backgroundImage.getHeight();
        int startX = Math.max(0, Math.min(x, imgW - 1));
        int startY = Math.max(0, Math.min(y, imgH - 1));
        int endX = Math.max(0, Math.min(x + width, imgW));
        int endY = Math.max(0, Math.min(y + height, imgH));
        
        if (startX >= endX || startY >= endY) return Color.WHITE;
        
        // Sample pixels to calculate average color
        int step = Math.max(1, (endX - startX) / 10);
        long totalR = 0, totalG = 0, totalB = 0;
        int sampleCount = 0;
        
        for (int py = startY; py < endY; py += step) {
            for (int px = startX; px < endX; px += step) {
                int rgb = backgroundImage.getRGB(px, py);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                totalR += r;
                totalG += g;
                totalB += b;
                sampleCount++;
            }
        }
        
        if (sampleCount == 0) return Color.WHITE;
        
        // Calculate average color
        int avgR = (int)(totalR / sampleCount);
        int avgG = (int)(totalG / sampleCount);
        int avgB = (int)(totalB / sampleCount);
        
        return new Color(avgR, avgG, avgB);
    }
    
    /**
     * Convert Color to hex string format (0xRRGGBBAA).
     */
    private String colorToHexString(Color color) {
        if (color == null) return "";
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();
        // Format as 0xRRGGBBAA
        return String.format("0x%02X%02X%02X%02X", r, g, b, a);
    }
    
    /**
     * Represents a text element that can be dragged.
     */
    private static class TextElement {
        String prefix;
        int x;
        int y;
        String text;
        Font font;
        Rectangle bounds;
    }
}