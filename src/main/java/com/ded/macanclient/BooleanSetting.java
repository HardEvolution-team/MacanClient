package com.ded.macanclient;


import java.util.function.BooleanSupplier;

/**
 * Настройка типа boolean.
 */

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue, Module module) {
        super(name, module);
        this.value = defaultValue;
    }

    public BooleanSetting(String name, boolean defaultValue, Module module, BooleanSupplier visibility) {
        super(name, module, visibility);
        this.value = defaultValue;
    }
}