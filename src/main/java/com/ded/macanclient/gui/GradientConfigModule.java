package com.ded.macanclient.gui;



import com.ded.macanclient.features.Category;
import com.ded.macanclient.features.Module;
import com.ded.macanclient.settings.ColorSetting;
import com.ded.macanclient.settings.FloatSetting;

import java.awt.Color;

/**
 * Модуль для настройки градиентов с реалтайм-превью.
 */

public class GradientConfigModule extends Module {
    private ColorSetting startColorSetting = new ColorSetting("Start Color", new Color(0xFF5555), this);
    private ColorSetting endColorSetting = new ColorSetting("End Color", new Color(0xFF5333), this);
    private FloatSetting tabAlphaSetting = new FloatSetting("Tab Alpha", 0.8f, 0.0f, 1.0f, this);
    private FloatSetting guiAlphaSetting = new FloatSetting("GUI Alpha", 0.8f, 0.0f, 1.0f, this);
    public GradientConfigModule(String name, Category category) {
        super(name, category);
        settings.add(startColorSetting);
        settings.add(endColorSetting);
        settings.add(tabAlphaSetting);
        settings.add(guiAlphaSetting);
    }

    public Color getStartColor() {
        return startColorSetting.getValue();
    }

    public Color getEndColor() {
        return endColorSetting.getValue();
    }

    public float getTabAlpha() {
        return tabAlphaSetting.getValue();
    }

    public float getGuiAlpha() {
        return guiAlphaSetting.getValue();
    }

    @Override
    public void onEnable() {}
    @Override
    public void onDisable() {}
}