package com.ded.macanclient;


import java.awt.Color;

/**
 * Компонент для выбора цвета.
 */
public class ColorPickerComponent {
    private int x, y, width = 100, height = 10;
    private Color color;

    public ColorPickerComponent(int x, int y, Color initialColor) {
        this.x = x;
        this.y = y;
        this.color = initialColor;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        // Рендеринг цветового поля (упрощенно)
        RenderUtils.renderGradient(x, y, x + width, y + height, color.getRGB(), color.getRGB());
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Логика выбора цвета (упрощенно)
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            // Здесь можно добавить выбор цвета через HSL
        }
    }

    public Color getColor() {
        return color;
    }
}