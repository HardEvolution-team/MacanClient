package com.ded.macanclient.config;

import com.ded.macanclient.features.Module;
import com.ded.macanclient.features.ModuleManager;
import com.ded.macanclient.settings.*;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Менеджер конфигурации для сохранения и загрузки настроек.
 */
public class ConfigManager {
    private final File configFile = new File("config/customclient.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void saveConfig() {
        try {
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }

            JsonObject config = new JsonObject();
            for (Module module : ModuleManager.getModules()) {
                JsonObject moduleConfig = new JsonObject();
                moduleConfig.addProperty("enabled", module.isEnabled());
                moduleConfig.addProperty("bind", module.getKeyBind());

                JsonObject settingsConfig = new JsonObject();
                for (Setting setting : module.getSettings()) {
                    if (setting instanceof FloatSetting) {
                        settingsConfig.addProperty(setting.getName(), ((FloatSetting) setting).getValue());
                    } else if (setting instanceof IntegerSetting) {
                        settingsConfig.addProperty(setting.getName(), ((IntegerSetting) setting).getValue());
                    } else if (setting instanceof BooleanSetting) {
                        settingsConfig.addProperty(setting.getName(), ((BooleanSetting) setting).getValue());
                    } else if (setting instanceof ColorSetting) {
                        settingsConfig.addProperty(setting.getName(), ((ColorSetting) setting).getHexInput());
                    } else if (setting instanceof SliderSetting) { // Добавляем поддержку SliderSetting
                        settingsConfig.addProperty(setting.getName(), ((SliderSetting) setting).getValue());
                    }
                }
                moduleConfig.add("settings", settingsConfig);
                config.add(module.getName(), moduleConfig);
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                gson.toJson(config, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            saveConfig();
            return;
        }

        try {
            String jsonString = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
            JsonElement jsonElement = new JsonParser().parse(new StringReader(jsonString));

            if (!jsonElement.isJsonObject()) {
                System.err.println("Invalid JSON format in config file, resetting to default");
                saveConfig();
                return;
            }

            JsonObject config = jsonElement.getAsJsonObject();

            for (Module module : ModuleManager.getModules()) {
                if (config.has(module.getName())) {
                    JsonObject moduleConfig = config.getAsJsonObject(module.getName());
                    if (moduleConfig.has("enabled")) {
                        module.setEnabled(moduleConfig.get("enabled").getAsBoolean());
                    }
                    if (moduleConfig.has("bind")) {
                        module.setKeyBind(moduleConfig.get("bind").getAsInt());
                    }
                    if (moduleConfig.has("settings")) {
                        JsonObject settingsConfig = moduleConfig.getAsJsonObject("settings");
                        for (Setting setting : module.getSettings()) {
                            if (settingsConfig.has(setting.getName())) {
                                try {
                                    if (setting instanceof FloatSetting) {
                                        ((FloatSetting) setting).setValue(settingsConfig.get(setting.getName()).getAsFloat());
                                    } else if (setting instanceof IntegerSetting) {
                                        ((IntegerSetting) setting).setValue(settingsConfig.get(setting.getName()).getAsInt());
                                    } else if (setting instanceof BooleanSetting) {
                                        ((BooleanSetting) setting).setValue(settingsConfig.get(setting.getName()).getAsBoolean());
                                    } else if (setting instanceof ColorSetting) {
                                        ((ColorSetting) setting).setHexInput(settingsConfig.get(setting.getName()).getAsString());
                                    } else if (setting instanceof SliderSetting) { // Добавляем поддержку SliderSetting
                                        ((SliderSetting) setting).setValue(settingsConfig.get(setting.getName()).getAsFloat());
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error loading setting " + setting.getName() + " for module " + module.getName() + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error loading config: " + e.getMessage());
            saveConfig();
        }
    }
}