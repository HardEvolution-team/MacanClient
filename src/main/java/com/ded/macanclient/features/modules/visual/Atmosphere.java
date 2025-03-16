package com.ded.macanclient.features.modules.visual;

import com.ded.macanclient.features.Module;
import com.ded.macanclient.features.Category;
import com.ded.macanclient.features.modules.player.FriendManager;
import com.ded.macanclient.settings.BooleanSetting;
import com.ded.macanclient.settings.ColorSetting;
import com.ded.macanclient.settings.ModeSetting;
import com.ded.macanclient.settings.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.Color;

public class Atmosphere extends Module {
    private final BooleanSetting time = new BooleanSetting("Time Editor", true, this);
    private final SliderSetting timeValue = new SliderSetting("Time", 18000f, 0f, 24000f, 1000f, this, time::getValue);
    private final BooleanSetting weather = new BooleanSetting("Weather Editor", true, this);
    private final ModeSetting weatherValue = new ModeSetting("Weather", new String[]{"Clean", "Rain", "Thunder"}, "Clean", this, weather::getValue);
    private final BooleanSetting worldColor = new BooleanSetting("World Color", true, this);
    private final ColorSetting worldColorRGB = new ColorSetting("World Color RGB", Color.WHITE, this, worldColor::getValue);
    private final BooleanSetting worldFog = new BooleanSetting("World Fog", false, this);
    private final ColorSetting worldFogRGB = new ColorSetting("World Fog RGB", Color.WHITE, this, worldFog::getValue);
    private final SliderSetting worldFogDistance = new SliderSetting("World Fog Distance", 0.10f, -1f, 0.9f, 0.1f, this, worldFog::getValue);
    private final ModeSetting renderMode = new ModeSetting("Render Mode", new String[]{"All Players", "Only Me", "Only Friends"}, "All Players", this);

    // Поля для оптимизации
    private long lastSetTime = -1; // Последнее установленное время
    private String lastWeather = null; // Последнее установленное состояние погоды

    public Atmosphere(String name, Category category) {
        super(name, category);
        settings.add(time);
        settings.add(timeValue);
        settings.add(weather);
        settings.add(weatherValue);
        settings.add(worldColor);
        settings.add(worldColorRGB);
        settings.add(worldFog);
        settings.add(worldFogRGB);
        settings.add(worldFogDistance);
        settings.add(renderMode);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!enabled || event.phase != TickEvent.Phase.END || !shouldRender()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return;

        // Управление временем
        if (time.getValue()) {
            long newTime = Math.round(timeValue.getValue());
            if (lastSetTime != newTime) {
                mc.world.setWorldTime(newTime);
                lastSetTime = newTime;
            }
        }

        // Управление погодой
        if (weather.getValue()) {
            String currentWeather = weatherValue.getMode(); // Заменил getValue на getMode
            if (!currentWeather.equals(lastWeather)) {
                switch (currentWeather) {
                    case "Rain":
                        mc.world.setRainStrength(1.0f);
                        mc.world.setThunderStrength(0.0f);
                        break;
                    case "Thunder":
                        mc.world.setRainStrength(1.0f);
                        mc.world.setThunderStrength(1.0f);
                        break;
                    default:
                        mc.world.setRainStrength(0.0f);
                        mc.world.setThunderStrength(0.0f);
                        break;
                }
                lastWeather = currentWeather;
            }
        }
    }

    @SubscribeEvent
    public void onFogColor(EntityViewRenderEvent.FogColors event) {
        if (!enabled || !shouldRender()) return;

        // Изменение цвета мира
        if (worldColor.getValue()) {
            Color color = worldColorRGB.getValue();
            event.setRed(color.getRed() / 255.0f);
            event.setGreen(color.getGreen() / 255.0f);
            event.setBlue(color.getBlue() / 255.0f);
        }
    }

    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (!enabled || !shouldRender()) return;

        // Изменение плотности тумана
        if (worldFog.getValue()) {
            event.setDensity(worldFogDistance.getValue());
            event.setCanceled(true);
        }
    }

    private boolean shouldRender() {
        switch (renderMode.getMode()) {
            case "Only Me":
                return Minecraft.getMinecraft().player != null;
            case "Only Friends":
                return FriendManager.isFriend(Minecraft.getMinecraft().player.getName());
            case "All Players":
            default:
                return true;
        }
    }
}