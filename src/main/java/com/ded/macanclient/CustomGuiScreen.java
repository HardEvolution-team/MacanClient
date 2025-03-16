package com.ded.macanclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Главный класс графического интерфейса клиента.
 * Открывается по нажатию RIGHT_SHIFT, закрывается по нажатию ESCAPE.
 */
public class CustomGuiScreen extends GuiScreen {
    private float animationProgress = 0.0f;
    private boolean opening = true;
    private Map<Category, List<ModuleWidget>> categorizedWidgets = new HashMap<>();

    public CustomGuiScreen() {
        for (Category category : Category.values()) {
            categorizedWidgets.put(category, new ArrayList<>());
        }

        for (Module module : ModuleManager.getModules()) {
            categorizedWidgets.get(module.getCategory()).add(new ModuleWidget(module, 0, 0));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (opening) {
            animationProgress = Math.min(1.0f, animationProgress + partialTicks * 0.1f);
        } else {
            animationProgress = Math.max(0.0f, animationProgress - partialTicks * 0.1f);
            if (animationProgress <= 0) {
                mc.displayGuiScreen(null);
                return;
            }
        }

        GL11.glPushMatrix();
        GL11.glScalef(animationProgress, animationProgress, 1.0f);
        drawRect(0, 0, width, height, 0x80000000);
        GL11.glPopMatrix();

        int startX = 20;
        int categoryWidth = 150;
        for (Category category : Category.values()) {
            List<ModuleWidget> widgets = categorizedWidgets.get(category);
            if (widgets.isEmpty()) continue;

            GL11.glPushMatrix();
            GL11.glScalef(animationProgress, animationProgress, 1.0f);
            Minecraft mc = Minecraft.getMinecraft();
            mc.fontRenderer.drawStringWithShadow(category.getName(), startX, 10, 0xFFFFFF);

            int y = 30;
            for (ModuleWidget widget : widgets) {
                widget.setPosition(startX, y);
                widget.render(mouseX, mouseY, partialTicks, partialTicks);
                y += widget.getHeight() + 5;
            }
            GL11.glPopMatrix();

            startX += categoryWidth;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_ESCAPE) {
            opening = false;
        } else {
            // Передаем нажатие клавиши виджетам для обработки бинда
            for (List<ModuleWidget> widgets : categorizedWidgets.values()) {
                for (ModuleWidget widget : widgets) {
                    widget.keyTyped(typedChar, keyCode);
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (List<ModuleWidget> widgets : categorizedWidgets.values()) {
            for (ModuleWidget widget : widgets) {
                widget.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (List<ModuleWidget> widgets : categorizedWidgets.values()) {
            for (ModuleWidget widget : widgets) {
                widget.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (List<ModuleWidget> widgets : categorizedWidgets.values()) {
            for (ModuleWidget widget : widgets) {
                widget.mouseDragged(mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}