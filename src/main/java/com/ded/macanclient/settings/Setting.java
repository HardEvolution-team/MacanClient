package com.ded.macanclient.settings;

import com.ded.macanclient.features.Module;

import java.util.function.BooleanSupplier;

/**
 * Базовый абстрактный класс для всех настроек.
 */
public abstract class Setting<T> {
    protected T value;
    private final String name;
    private final Module module;
    private final BooleanSupplier visibility;

    public Setting(String name, Module module) {
        this.name = name;
        this.module = module;
        this.visibility = () -> true; // Значение по умолчанию
    }

    public Setting(String name, Module module, BooleanSupplier visibility) {
        this.name = name;
        this.module = module;
        this.visibility = visibility;
    }

    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return visibility.getAsBoolean();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    // Новый геттер для module
    public Module getModule() {
        return module;
    }
}