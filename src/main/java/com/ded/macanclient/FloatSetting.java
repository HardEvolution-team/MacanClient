package com.ded.macanclient;

import java.util.function.BooleanSupplier;

public class FloatSetting extends Setting<Float> {
    private final float min;
    private final float max;

    public FloatSetting(String name, float defaultValue, float min, float max, Module module) {
        super(name, module);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
    }

    public FloatSetting(String name, float defaultValue, float min, float max, Module module, BooleanSupplier visibility) {
        super(name, module, visibility);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    @Override
    public void setValue(Float value) {
        this.value = Math.max(min, Math.min(max, value));
        Module mod = getModule();
        if (mod != null) {
            mod.onSettingChanged(this);
        }
    }
}