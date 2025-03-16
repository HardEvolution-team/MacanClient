package com.ded.macanclient.settings;

import com.ded.macanclient.features.Module;
import com.ded.macanclient.settings.Setting;

import java.util.function.BooleanSupplier;

public class StringSetting extends Setting<String> {
    private final String[] values;
    private int currentIndex;

    public StringSetting(String name, String[] values, String defaultValue, Module module) {
        super(name, module);
        this.values = values;
        this.currentIndex = getIndexOf(defaultValue);
        this.value = defaultValue;
    }

    public StringSetting(String name, String[] values, String defaultValue, Module module, BooleanSupplier visibility) {
        super(name, module, visibility);
        this.values = values;
        this.currentIndex = getIndexOf(defaultValue);
        this.value = defaultValue;
    }

    private int getIndexOf(String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) return i;
        }
        return 0;
    }

    public String[] getValues() {
        return values;
    }

    @Override
    public void setValue(String value) {
        this.currentIndex = getIndexOf(value);
        this.value = value;
        Module mod = getModule();
        if (mod != null) {
            mod.onSettingChanged(this);
        }
    }

    public String getValue() {
        return value;
    }

    // Метод для переключения на следующее значение
    public void cycleNext() {
        currentIndex = (currentIndex + 1) % values.length;
        this.value = values[currentIndex];
        Module mod = getModule();
        if (mod != null) {
            mod.onSettingChanged(this);
        }
    }

    // Метод для переключения на предыдущее значение
    public void cyclePrevious() {
        currentIndex = (currentIndex - 1 + values.length) % values.length;
        this.value = values[currentIndex];
        Module mod = getModule();
        if (mod != null) {
            mod.onSettingChanged(this);
        }
    }
}