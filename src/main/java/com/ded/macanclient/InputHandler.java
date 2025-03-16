package com.ded.macanclient;


import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import static com.ded.macanclient.ModuleManager.getModules;

/**
 * Обработчик ввода для управления модулями и GUI.
 */

public class InputHandler {
    private ClickGUI clickGUI;
    private boolean[] keyStates;

    public InputHandler() {
        clickGUI = new ClickGUI();
        keyStates = new boolean[256];
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().player == null) return;

        int keyCode = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
        boolean keyDown = Keyboard.getEventKeyState();

        System.out.println("Key event: keyCode=" + keyCode + ", keyDown=" + keyDown + ", currentScreen=" + (Minecraft.getMinecraft().currentScreen != null));

        // Открытие/закрытие GUI
        if (keyCode == Keyboard.KEY_RSHIFT && keyDown && !keyStates[keyCode]) {
            if (Minecraft.getMinecraft().currentScreen == null) {
                Minecraft.getMinecraft().displayGuiScreen(clickGUI);
                System.out.println("Opening GUI");
            } else if (Minecraft.getMinecraft().currentScreen == clickGUI) {
                Minecraft.getMinecraft().displayGuiScreen(null);
                System.out.println("Closing GUI, resetting key states");
                for (int i = 0; i < keyStates.length; i++) {
                    keyStates[i] = false;
                }
            }
            keyStates[keyCode] = true;
        } else if (keyCode == Keyboard.KEY_RSHIFT && !keyDown) {
            keyStates[keyCode] = false;
        }

        // Обработка биндов модулей (только если нет активного экрана)
        if (Minecraft.getMinecraft().currentScreen == null && keyDown && !keyStates[keyCode]) {
            for (Module module : getModules()) {
                int keyBind = module.getKeyBind();
                if (keyBind != -1 && keyCode == keyBind) {
                    module.toggle();
                    System.out.println("Toggling " + module.getName() + " with bind " + Keyboard.getKeyName(keyBind));
                    keyStates[keyBind] = true;
                }
            }
        }

        // Сбрасываем состояние клавиш при отпускании
        if (!keyDown) {
            keyStates[keyCode] = false;
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null) return;

        int y = 10;
        for (Module module : getModules()) {
            if (module.isEnabled()) {
                Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(module.getName(), 10, y, 0xFFFFFF);
                y += 10;
            }
        }
    }
}