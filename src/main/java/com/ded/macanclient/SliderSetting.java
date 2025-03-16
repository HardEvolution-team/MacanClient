package com.ded.macanclient;

import java.util.function.BooleanSupplier;

public class SliderSetting extends Setting<Float> {
    private final float min;
    private final float max;
    private final float step;

    public SliderSetting(String name, float defaultValue, float min, float max, float step, Module module) {
        super(name, module);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = defaultValue;
    }

    public SliderSetting(String name, float defaultValue, float min, float max, float step, Module module, BooleanSupplier visibility) {
        super(name, module, visibility);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = defaultValue;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getStep() {
        return step;
    }

    @Override
    public void setValue(Float value) {
        value = Math.max(min, Math.min(max, value));
        value = Math.round(value / step) * step;
        super.setValue(value);
    }
}