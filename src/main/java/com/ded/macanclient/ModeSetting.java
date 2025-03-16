package com.ded.macanclient;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class ModeSetting extends Setting {
    private String[] modes;
    private int index;
    private final BooleanSupplier visibilitySupplier;

    public ModeSetting(String name, String[] modes, String defaultValue, Module module) {
        this(name, modes, defaultValue, module, () -> true); // По умолчанию всегда видимый
    }

    public ModeSetting(String name, String[] modes, String defaultValue, Module module, BooleanSupplier visibilitySupplier) {
        super(name, module);
        this.modes = modes;
        this.index = Arrays.asList(modes).indexOf(defaultValue);
        this.visibilitySupplier = visibilitySupplier;
        if (this.index == -1) {
            this.index = 0; // Устанавливаем первый элемент, если defaultValue не найден
        }
    }

    public String getMode() {
        return visibilitySupplier.getAsBoolean() ? modes[index] : modes[0]; // Возвращаем первый элемент, если невидимый
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = Math.max(0, Math.min(index, modes.length - 1)); // Ограничиваем индекс
    }

    public String[] getModes() {
        return modes;
    }

    public boolean isVisible() {
        return visibilitySupplier.getAsBoolean();
    }
}