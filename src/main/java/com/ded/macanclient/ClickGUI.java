package com.ded.macanclient;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGUI extends GuiScreen {
    private Map<Category, List<ModuleWidget>> categoryWidgets = new HashMap<>();
    private List<Category> categories = new ArrayList<>();
    private Category selectedCategory;
    private int guiX = 50;
    private int guiY = 50;
    private int guiWidth = 500;
    private int guiHeight = 300;
    private int tabHeight = 20;
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;
    private int scrollOffset = 0;

    public ClickGUI() {
        for (Category category : Category.values()) {
            categories.add(category);
            categoryWidgets.put(category, new ArrayList<>());
        }

        for (Category category : categories) {
            int widgetY = guiY + tabHeight + 5;
            for (Module module : ModuleManager.getModules()) {
                if (module.getCategory() == category) {
                    ModuleWidget widget = new ModuleWidget(module, guiX + 5, widgetY);
                    categoryWidgets.get(category).add(widget);
                    widgetY += widget.getHeight() + 3;
                }
            }
        }

        if (!categories.isEmpty()) {
            selectedCategory = categories.get(0);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        GradientConfigModule configModule = (GradientConfigModule) ModuleManager.getModules().stream()
                .filter(m -> m instanceof GradientConfigModule).findFirst().orElse(null);
        int startColor = configModule != null ? configModule.getStartColor().getRGB() : 0xFF555555;
        int endColor = configModule != null ? configModule.getEndColor().getRGB() : 0xFF333333;
        float tabAlpha = configModule != null ? configModule.getTabAlpha() : 0.8f;
        float guiAlpha = configModule != null ? configModule.getGuiAlpha() : 0.8f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, scrollOffset, 0);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        drawGradientRect(guiX, guiY, guiX + guiWidth, guiY + guiHeight, applyAlpha(startColor, guiAlpha), applyAlpha(endColor, guiAlpha));
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();

        int tabX = guiX;
        for (Category category : categories) {
            int tabColor = (category == selectedCategory) ? interpolateColor(startColor, endColor, 0.7f) : interpolateColor(startColor, endColor, 0.5f);
            drawRect(tabX, guiY, tabX + 80, guiY + tabHeight, applyAlpha(tabColor, tabAlpha));
            mc.fontRenderer.drawStringWithShadow(category.name(), tabX + 3, guiY + 6, 0xFFFFFF); // Центрируем текст вкладок
            tabX += 80;
        }

        if (selectedCategory != null) {
            for (ModuleWidget widget : categoryWidgets.get(selectedCategory)) {
                widget.render(mouseX, mouseY - scrollOffset, partialTicks, guiAlpha);
            }
        }

        GlStateManager.popMatrix();
    }

    private int interpolateColor(int color1, int color2, float fraction) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int r = (int) (r1 + (r2 - r1) * fraction);
        int g = (int) (g1 + (g2 - g1) * fraction);
        int b = (int) (b1 + (b2 - b1) * fraction);
        return (r << 16) | (g << 8) | b;
    }

    private int applyAlpha(int color, float alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) (alpha * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void updateWidgetPositions() {
        if (selectedCategory != null) {
            int widgetY = guiY + tabHeight + 5 + scrollOffset;
            for (ModuleWidget widget : categoryWidgets.get(selectedCategory)) {
                widget.setPosition(guiX + 5, widgetY);
                widgetY += widget.getHeight() + 3;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int adjustedMouseY = mouseY - scrollOffset;

        int tabX = guiX;
        for (Category category : categories) {
            if (mouseX >= tabX && mouseX <= tabX + 80 && adjustedMouseY >= guiY && adjustedMouseY <= guiY + tabHeight) {
                selectedCategory = category;
                updateWidgetPositions();
                return;
            }
            tabX += 80;
        }

        if (mouseX >= guiX && mouseX <= guiX + guiWidth && adjustedMouseY >= guiY && adjustedMouseY <= guiY + tabHeight && mouseButton == 0) {
            dragging = true;
            dragOffsetX = mouseX - guiX;
            dragOffsetY = adjustedMouseY - guiY;
        }

        if (selectedCategory != null) {
            for (ModuleWidget widget : categoryWidgets.get(selectedCategory)) {
                widget.mouseClicked(mouseX, adjustedMouseY, mouseButton);
            }
            updateWidgetPositions();
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = false;
        if (selectedCategory != null) {
            for (ModuleWidget widget : categoryWidgets.get(selectedCategory)) {
                widget.mouseReleased(mouseX, mouseY - scrollOffset, state);
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (dragging) {
            guiX = mouseX - dragOffsetX;
            guiY = mouseY - dragOffsetY - scrollOffset;
            updateWidgetPositions();
        }

        if (selectedCategory != null) {
            for (ModuleWidget widget : categoryWidgets.get(selectedCategory)) {
                widget.mouseDragged(mouseX, mouseY - scrollOffset);
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset += wheel > 0 ? 20 : -20;
            if (selectedCategory != null) {
                int totalHeight = 0;
                for (ModuleWidget widget : categoryWidgets.get(selectedCategory)) {
                    totalHeight += widget.getHeight() + 3;
                }
                int maxScroll = Math.max(0, totalHeight - (guiHeight - tabHeight));
                scrollOffset = Math.min(0, Math.max(-maxScroll, scrollOffset));
            }
            updateWidgetPositions();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (selectedCategory != null) {
            for (ModuleWidget widget : categoryWidgets.get(selectedCategory)) {
                widget.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}