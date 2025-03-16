package com.ded.macanclient;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final Category category;
    protected boolean enabled = false;
    private int keyBind = -1;
    protected final List<Setting> settings = new ArrayList<>();

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            onEnable();
            MinecraftForge.EVENT_BUS.register(this);
        } else {
            onDisable();
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public int getKeyBind() {
        return keyBind;
    }

    public void setKeyBind(int keyBind) {
        this.keyBind = keyBind;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    protected void onEnable() {
        System.out.println(name + " enabled");
    }

    protected void onDisable() {
        System.out.println(name + " disabled");
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null) return;
        if (keyBind == -1) return;
        if (org.lwjgl.input.Keyboard.isKeyDown(keyBind)) {
            toggle();
        }
    }

    public void onSettingChanged(Setting<?> setting) {
        // Дочерние классы могут переопределить
    }
}