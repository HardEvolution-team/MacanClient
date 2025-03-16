package com.ded.macanclient;

import java.awt.Color;
import java.util.function.BooleanSupplier;

public class ColorSetting extends Setting<Color> {
    private String hexInput;

    public ColorSetting(String name, Color defaultValue, Module module) {
        super(name, module);
        this.value = defaultValue;
        this.hexInput = String.format("%06X", defaultValue.getRGB() & 0xFFFFFF).toUpperCase();
    }

    public ColorSetting(String name, Color defaultValue, Module module, BooleanSupplier visibility) {
        super(name, module, visibility);
        this.value = defaultValue;
        this.hexInput = String.format("%06X", defaultValue.getRGB() & 0xFFFFFF).toUpperCase();
    }

    public String getHexInput() {
        return hexInput;
    }

    public void setHexInput(String hex) {
        try {
            this.hexInput = hex.toUpperCase();
            int rgb = (int) Long.parseLong(hex, 16);
            this.value = new Color(rgb);
            Module mod = getModule();
            if (mod != null) {
                mod.onSettingChanged(this);
            }
        } catch (NumberFormatException e) {
            // Игнорируем некорректный ввод
        }
    }

    @Override
    public void setValue(Color value) {
        this.value = value;
        this.hexInput = String.format("%06X", value.getRGB() & 0xFFFFFF).toUpperCase();
        Module mod = getModule();
        if (mod != null) {
            mod.onSettingChanged(this);
        }
    }
}