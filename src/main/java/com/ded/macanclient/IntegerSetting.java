package com.ded.macanclient;

import java.util.function.BooleanSupplier;

public class IntegerSetting extends Setting<Integer> {
    private final int min;
    private final int max;

    public IntegerSetting(String name, int defaultValue, int min, int max, Module module) {
        super(name, module);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
    }

    public IntegerSetting(String name, int defaultValue, int min, int max, Module module, BooleanSupplier visibility) {
        super(name, module, visibility);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public void setValue(Integer value) {
        this.value = Math.max(min, Math.min(max, value));
        Module mod = getModule();
        if (mod != null) {
            mod.onSettingChanged(this);
        }
    }
}