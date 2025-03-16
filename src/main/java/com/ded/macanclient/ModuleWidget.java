package com.ded.macanclient;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.Gui.drawRect;

public class ModuleWidget {
    private final Module module;
    private int x, y;
    private int width = 120;
    private int height = 12;
    private boolean expanded = false;
    private int expandedHeight = 15;
    private boolean draggingSlider = false;
    private Setting<?> draggedSetting = null;
    private boolean waitingForBind = false;
    private ColorSetting activeHexSetting = null;
    private long lastBlinkTime = 0;
    private boolean cursorVisible = true;
    private ColorSetting activeColorSetting = null;

    public ModuleWidget(Module module, int x, int y) {
        this.module = module;
        this.x = x;
        this.y = y;
        updateExpandedHeight();
    }

    private void updateExpandedHeight() {
        int visibleSettings = 0;
        for (Setting<?> setting : module.getSettings()) {
            try {
                if (setting.isVisible()) {
                    visibleSettings++;
                }
            } catch (Exception e) {
                System.err.println("Error checking visibility for setting " + setting.getName() + ": " + e.getMessage());
            }
        }
        expandedHeight = 15 + visibleSettings * 15;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(int mouseX, int mouseY, float partialTicks, float guiAlpha) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBlinkTime >= 500) {
            cursorVisible = !cursorVisible;
            lastBlinkTime = currentTime;
        }

        int backgroundColor = module.isEnabled() ? 0xFF4CAF50 : 0xFF555555;
        int textColor = 0xFFFFFF;

        boolean isHovered = isMouseOver(mouseX, mouseY);
        if (isHovered) {
            backgroundColor = module.isEnabled() ? 0xFF66BB6A : 0xFF777777;
        }

        drawRect(x, y, x + width, y + height, applyAlpha(backgroundColor, guiAlpha));
        drawRect(x, y, x + width, y + 1, applyAlpha(0xFF000000, guiAlpha)); // Top border
        drawRect(x, y + height - 1, x + width, y + height, applyAlpha(0xFF000000, guiAlpha)); // Bottom border
        drawRect(x, y, x + 1, y + height, applyAlpha(0xFF000000, guiAlpha)); // Left border
        drawRect(x + width - 1, y, x + width, y + height, applyAlpha(0xFF000000, guiAlpha)); // Right border

        Minecraft mc = Minecraft.getMinecraft();
        String moduleName = truncateString(mc.fontRenderer, module.getName(), width - 6);
        mc.fontRenderer.drawStringWithShadow(moduleName, x + 3, y + 2, textColor);

        if (expanded) {
            int offsetY = y + height;
            drawRect(x, offsetY, x + width, offsetY + expandedHeight, applyAlpha(0xFF444444, guiAlpha));

            String bindText = "Bind: " + (module.getKeyBind() == -1 ? "None" : Keyboard.getKeyName(module.getKeyBind()));
            if (waitingForBind) bindText = "Press a key...";
            bindText = truncateString(mc.fontRenderer, bindText, width - 50);
            drawRect(x + 3, offsetY + 2, x + width - 35, offsetY + 15, applyAlpha(0xFF333333, guiAlpha));
            mc.fontRenderer.drawStringWithShadow(bindText, x + 5, offsetY + 4, 0xFFFFFF);

            drawRect(x + width - 30, offsetY + 2, x + width - 15, offsetY + 15, applyAlpha(0xFF555555, guiAlpha));
            mc.fontRenderer.drawStringWithShadow("Bind", x + width - 28, offsetY + 4, 0xFFFFFF);
            drawRect(x + width - 14, offsetY + 2, x + width - 3, offsetY + 15, applyAlpha(0xFFFF5555, guiAlpha));
            mc.fontRenderer.drawStringWithShadow("Unbind", x + width - 12, offsetY + 4, 0xFFFFFF);
            offsetY += 15;

            List<Setting> settingsCopy = new ArrayList<>(module.getSettings()); // Исправлено: List<Setting> вместо List<Setting<?>>
            for (Setting<?> setting : settingsCopy) {
                try {
                    if (!setting.isVisible()) continue;

                    if (setting instanceof FloatSetting && !(setting instanceof SliderSetting)) {
                        FloatSetting floatSetting = (FloatSetting) setting;
                        float value = floatSetting.getValue();
                        float min = floatSetting.getMin();
                        float max = floatSetting.getMax();
                        float range = max - min;
                        float normalizedValue = (value - min) / range;

                        int sliderWidth = 50;
                        int sliderX = x + width - sliderWidth - 5;
                        int sliderY = offsetY + 4;
                        drawRect(sliderX, sliderY, sliderX + sliderWidth, sliderY + 8, applyAlpha(0xFF666666, guiAlpha));
                        int handleX = (int) (sliderX + normalizedValue * sliderWidth);
                        drawRect(handleX - 2, sliderY - 1, handleX + 2, sliderY + 9, applyAlpha(0xFFFFFFFF, guiAlpha));

                        String displayText = truncateString(mc.fontRenderer, setting.getName() + ": " + String.format("%.1f", value), width - sliderWidth - 10);
                        mc.fontRenderer.drawStringWithShadow(displayText, x + 5, offsetY + 2, 0xFFFFFF);
                    } else if (setting instanceof IntegerSetting) {
                        IntegerSetting integerSetting = (IntegerSetting) setting;
                        int value = integerSetting.getValue();
                        int min = integerSetting.getMin();
                        int max = integerSetting.getMax();
                        float range = max - min;
                        float normalizedValue = (value - min) / range;

                        int sliderWidth = 50;
                        int sliderX = x + width - sliderWidth - 5;
                        int sliderY = offsetY + 4;
                        drawRect(sliderX, sliderY, sliderX + sliderWidth, sliderY + 8, applyAlpha(0xFF666666, guiAlpha));
                        int handleX = (int) (sliderX + normalizedValue * sliderWidth);
                        drawRect(handleX - 2, sliderY - 1, handleX + 2, sliderY + 9, applyAlpha(0xFFFFFFFF, guiAlpha));

                        String displayText = truncateString(mc.fontRenderer, setting.getName() + ": " + value, width - sliderWidth - 10);
                        mc.fontRenderer.drawStringWithShadow(displayText, x + 5, offsetY + 2, 0xFFFFFF);
                    } else if (setting instanceof SliderSetting) {
                        SliderSetting sliderSetting = (SliderSetting) setting;
                        float value = sliderSetting.getValue();
                        float min = sliderSetting.getMin();
                        float max = sliderSetting.getMax();
                        float range = max - min;
                        float normalizedValue = (value - min) / range;

                        int sliderWidth = 50;
                        int sliderX = x + width - sliderWidth - 5;
                        int sliderY = offsetY + 4;
                        drawRect(sliderX, sliderY, sliderX + sliderWidth, sliderY + 8, applyAlpha(0xFF666666, guiAlpha));
                        int handleX = (int) (sliderX + normalizedValue * sliderWidth);
                        drawRect(handleX - 2, sliderY - 1, handleX + 2, sliderY + 9, applyAlpha(0xFFFFFFFF, guiAlpha));

                        String displayText = truncateString(mc.fontRenderer, setting.getName() + ": " + String.format("%.1f", value), width - sliderWidth - 10);
                        mc.fontRenderer.drawStringWithShadow(displayText, x + 5, offsetY + 2, 0xFFFFFF);
                    } else if (setting instanceof BooleanSetting) {
                        BooleanSetting booleanSetting = (BooleanSetting) setting;
                        String displayText = truncateString(mc.fontRenderer, setting.getName() + ": " + (booleanSetting.getValue() ? "On" : "Off"), width - 25);
                        int buttonColor = booleanSetting.getValue() ? 0xFF4CAF50 : 0xFF666666;
                        drawRect(x + width - 20, offsetY + 2, x + width - 5, offsetY + 12, applyAlpha(buttonColor, guiAlpha));
                        mc.fontRenderer.drawStringWithShadow(displayText, x + 5, offsetY + 2, 0xFFFFFF);
                    } else if (setting instanceof ColorSetting) {
                        ColorSetting colorSetting = (ColorSetting) setting;
                        String hexText = "#" + colorSetting.getHexInput();
                        if (colorSetting == activeHexSetting && cursorVisible) hexText += "|";
                        hexText = truncateString(mc.fontRenderer, setting.getName() + ": " + hexText, width - 25);
                        mc.fontRenderer.drawStringWithShadow(hexText, x + 5, offsetY + 2, 0xFFFFFF);
                        int colorBoxX = x + width - 20;
                        int colorBoxY = offsetY + 2;
                        drawRect(colorBoxX, colorBoxY, x + width - 5, offsetY + 12, applyAlpha(colorSetting.getValue().getRGB(), guiAlpha));

                        if (activeColorSetting == colorSetting) {
                            renderColorPalette(colorBoxX, colorBoxY + 15, colorSetting);
                        }
                    } else if (setting instanceof StringSetting) {
                        StringSetting stringSetting = (StringSetting) setting;
                        String displayText = truncateString(mc.fontRenderer, setting.getName() + ": " + stringSetting.getValue(), width - 35);
                        mc.fontRenderer.drawStringWithShadow(displayText, x + 5, offsetY + 2, 0xFFFFFF);

                        drawRect(x + width - 30, offsetY + 2, x + width - 20, offsetY + 12, applyAlpha(0xFF555555, guiAlpha));
                        mc.fontRenderer.drawStringWithShadow("<", x + width - 27, offsetY + 3, 0xFFFFFF);
                        drawRect(x + width - 18, offsetY + 2, x + width - 8, offsetY + 12, applyAlpha(0xFF555555, guiAlpha));
                        mc.fontRenderer.drawStringWithShadow(">", x + width - 15, offsetY + 3, 0xFFFFFF);
                    }
                    offsetY += 15;
                } catch (Exception e) {
                    System.err.println("Error rendering setting " + setting.getName() + " in module " + setting.getModule().getName() + ": " + e.getMessage());
                }
            }
        }
    }

    private String truncateString(net.minecraft.client.gui.FontRenderer font, String text, int maxWidth) {
        if (font.getStringWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        while (font.getStringWidth(text + ellipsis) > maxWidth && text.length() > 0) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipsis;
    }

    private void renderColorPalette(int paletteX, int paletteY, ColorSetting colorSetting) {
        int paletteWidth = 60;
        int paletteHeight = 60;
        int offsetY = 30;

        Minecraft mc = Minecraft.getMinecraft();
        int screenWidth = mc.displayWidth;
        int screenHeight = mc.displayHeight;
        int adjustedPaletteY = paletteY + offsetY;

        // Поднимаем палитру (уменьшаем Y) и сдвигаем вправо (увеличиваем X)
        adjustedPaletteY -= 30; // Поднимаем на 10 пикселей выше
        paletteX += 25; // Сдвигаем на 10 пикселей вправо

        // Корректируем, чтобы не выходить за пределы экрана
        if (adjustedPaletteY + paletteHeight > screenHeight) {
            adjustedPaletteY = paletteY - paletteHeight - 10;
        }

        if (paletteX + paletteWidth > screenWidth) {
            paletteX = screenWidth - paletteWidth - 5;
        }

        adjustedPaletteY = Math.max(0, adjustedPaletteY);

        drawRect(paletteX - 2, adjustedPaletteY - 2, paletteX + paletteWidth + 2, adjustedPaletteY + paletteHeight + 2, applyAlpha(0xFF333333, 0.9f));
        for (int px = 0; px < paletteWidth; px++) {
            for (int py = 0; py < paletteHeight; py++) {
                float hue = (float) px / paletteWidth;
                float brightness = 1.0f - (float) py / paletteHeight;
                Color color = Color.getHSBColor(hue, 1.0f, brightness);
                drawRect(paletteX + px, adjustedPaletteY + py, paletteX + px + 1, adjustedPaletteY + py + 1, color.getRGB());
            }
        }
    }

    private int applyAlpha(int color, float alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) (alpha * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseButton == 0) module.toggle();
            else if (mouseButton == 1) {
                expanded = !expanded;
                updateExpandedHeight();
            }
        }

        if (expanded) {
            int offsetY = y + height;
            if (mouseX >= x + width - 30 && mouseX <= x + width - 15 && mouseY >= offsetY + 2 && mouseY <= offsetY + 15 && mouseButton == 0) {
                waitingForBind = true;
            }
            if (mouseX >= x + width - 14 && mouseX <= x + width - 3 && mouseY >= offsetY + 2 && mouseY <= offsetY + 15 && mouseButton == 0) {
                module.setKeyBind(-1);
                waitingForBind = false;
            }
            offsetY += 15;

            List<Setting> settingsCopy = new ArrayList<>(module.getSettings()); // Исправлено: List<Setting> вместо List<Setting<?>>
            for (Setting<?> setting : settingsCopy) {
                try {
                    if (!setting.isVisible()) continue;

                    if (setting instanceof FloatSetting && !(setting instanceof SliderSetting)) {
                        FloatSetting floatSetting = (FloatSetting) setting;
                        int sliderWidth = 50;
                        int sliderX = x + width - sliderWidth - 5;
                        int sliderY = offsetY + 4;
                        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth && mouseY >= sliderY - 1 && mouseY <= sliderY + 9 && mouseButton == 0) {
                            draggingSlider = true;
                            draggedSetting = floatSetting;
                        }
                    } else if (setting instanceof IntegerSetting) {
                        IntegerSetting integerSetting = (IntegerSetting) setting;
                        int sliderWidth = 50;
                        int sliderX = x + width - sliderWidth - 5;
                        int sliderY = offsetY + 4;
                        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth && mouseY >= sliderY - 1 && mouseY <= sliderY + 9 && mouseButton == 0) {
                            draggingSlider = true;
                            draggedSetting = integerSetting;
                        }
                    } else if (setting instanceof SliderSetting) {
                        SliderSetting sliderSetting = (SliderSetting) setting;
                        int sliderWidth = 50;
                        int sliderX = x + width - sliderWidth - 5;
                        int sliderY = offsetY + 4;
                        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth && mouseY >= sliderY - 1 && mouseY <= sliderY + 9 && mouseButton == 0) {
                            draggingSlider = true;
                            draggedSetting = sliderSetting;
                        }
                    } else if (setting instanceof BooleanSetting) {
                        BooleanSetting booleanSetting = (BooleanSetting) setting;
                        if (mouseX >= x + width - 20 && mouseX <= x + width - 5 && mouseY >= offsetY + 2 && mouseY <= offsetY + 12 && mouseButton == 0) {
                            booleanSetting.setValue(!booleanSetting.getValue());
                            if (setting.getModule() instanceof KillAuraModule) {
                                ((KillAuraModule) setting.getModule()).onSettingChanged(booleanSetting);
                            }
                        }
                    } else if (setting instanceof ColorSetting) {
                        ColorSetting colorSetting = (ColorSetting) setting;
                        int colorBoxX = x + width - 20;
                        int colorBoxY = offsetY + 2;
                        if (mouseX >= x + 5 && mouseX <= x + width - 40 && mouseY >= offsetY && mouseY <= offsetY + 12 && mouseButton == 0) {
                            activeHexSetting = colorSetting;
                            cursorVisible = true;
                            lastBlinkTime = System.currentTimeMillis();
                        } else if (mouseX >= colorBoxX && mouseX <= x + width - 5 && mouseY >= colorBoxY && mouseY <= offsetY + 12 && mouseButton == 0) {
                            activeColorSetting = (activeColorSetting == colorSetting) ? null : colorSetting;
                        } else if (activeColorSetting == colorSetting) {
                            int paletteX = colorBoxX;
                            int paletteY = colorBoxY + 15;
                            int paletteWidth = 60;
                            int paletteHeight = 60;
                            if (mouseX >= paletteX && mouseX <= paletteX + paletteWidth && mouseY >= paletteY && mouseY <= paletteY + paletteHeight) {
                                float hue = (float) (mouseX - paletteX) / paletteWidth;
                                float brightness = 1.0f - (float) (mouseY - paletteY) / paletteHeight;
                                Color selectedColor = Color.getHSBColor(hue, 1.0f, brightness);
                                colorSetting.setValue(selectedColor);
                            } else {
                                activeColorSetting = null;
                            }
                        }
                    } else if (setting instanceof StringSetting) {
                        StringSetting stringSetting = (StringSetting) setting;
                        if (mouseX >= x + width - 30 && mouseX <= x + width - 20 && mouseY >= offsetY + 2 && mouseY <= offsetY + 12 && mouseButton == 0) {
                            stringSetting.cyclePrevious();
                        }
                        if (mouseX >= x + width - 18 && mouseX <= x + width - 8 && mouseY >= offsetY + 2 && mouseY <= offsetY + 12 && mouseButton == 0) {
                            stringSetting.cycleNext();
                        }
                    }
                    offsetY += 15;
                } catch (Exception e) {
                    System.err.println("Error handling click for setting " + setting.getName() + " in module " + setting.getModule().getName() + ": " + e.getMessage());
                }
            }
        }
    }
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        draggingSlider = false;
        draggedSetting = null;
    }

    public void mouseDragged(int mouseX, int mouseY) {
        if (draggingSlider && draggedSetting != null) {
            int sliderWidth = 50;
            int sliderX = x + width - sliderWidth - 5;
            float normalizedValue = (float) (mouseX - sliderX) / sliderWidth;
            normalizedValue = Math.max(0, Math.min(1, normalizedValue));

            try {
                if (draggedSetting instanceof FloatSetting && !(draggedSetting instanceof SliderSetting)) {
                    FloatSetting floatSetting = (FloatSetting) draggedSetting;
                    float min = floatSetting.getMin();
                    float max = floatSetting.getMax();
                    float newValue = min + normalizedValue * (max - min);
                    floatSetting.setValue(newValue);
                } else if (draggedSetting instanceof IntegerSetting) {
                    IntegerSetting integerSetting = (IntegerSetting) draggedSetting;
                    int min = integerSetting.getMin();
                    int max = integerSetting.getMax();
                    int newValue = Math.round(min + normalizedValue * (max - min));
                    newValue = Math.max(min, newValue);
                    integerSetting.setValue(newValue);
                } else if (draggedSetting instanceof SliderSetting) {
                    SliderSetting sliderSetting = (SliderSetting) draggedSetting;
                    float min = sliderSetting.getMin();
                    float max = sliderSetting.getMax();
                    float newValue = min + normalizedValue * (max - min);
                    sliderSetting.setValue(newValue);
                }
            } catch (Exception e) {
                System.err.println("Error dragging setting " + draggedSetting.getName() + " in module " + draggedSetting.getModule().getName() + ": " + e.getMessage());
            }
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (waitingForBind) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                waitingForBind = false;
            } else {
                module.setKeyBind(keyCode);
                waitingForBind = false;
            }
            return;
        }

        if (activeHexSetting != null) {
            if (keyCode == Keyboard.KEY_BACK && activeHexSetting.getHexInput().length() > 0) {
                activeHexSetting.setHexInput(activeHexSetting.getHexInput().substring(0, activeHexSetting.getHexInput().length() - 1));
            } else if (keyCode == Keyboard.KEY_RETURN) {
                activeHexSetting = null;
            } else if (keyCode != Keyboard.KEY_ESCAPE) {
                String hexChar = keyCodeToHexChar(keyCode);
                if (hexChar != null) {
                    String newHex = activeHexSetting.getHexInput() + hexChar;
                    if (newHex.length() <= 6) activeHexSetting.setHexInput(newHex);
                    if (newHex.length() == 6) activeHexSetting = null;
                }
            }
        }
    }

    private String keyCodeToHexChar(int keyCode) {
        if (keyCode >= Keyboard.KEY_0 && keyCode <= Keyboard.KEY_9) return String.valueOf(keyCode - Keyboard.KEY_0);
        else if (keyCode >= Keyboard.KEY_A && keyCode <= Keyboard.KEY_F) return String.valueOf((char) ('A' + (keyCode - Keyboard.KEY_A)));
        else if (keyCode >= Keyboard.KEY_NUMPAD0 && keyCode <= Keyboard.KEY_NUMPAD9) return String.valueOf(keyCode - Keyboard.KEY_NUMPAD0);
        return null;
    }

    private boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public int getHeight() {
        return expanded ? height + expandedHeight : height;
    }
}