package com.ded.macanclient.features.modules.visual;

import com.ded.macanclient.features.Module;
import com.ded.macanclient.features.Category;
import com.ded.macanclient.features.modules.player.FriendManager;
import com.ded.macanclient.settings.BooleanSetting;
import com.ded.macanclient.settings.ColorSetting;
import com.ded.macanclient.settings.ModeSetting;
import com.ded.macanclient.settings.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class ChinaHatModule extends Module {
    // Основные настройки размера и позиции
    private final SliderSetting widthSetting = new SliderSetting("Width", 1.0f, 0.1f, 3.0f, 0.05f, this);
    private final SliderSetting heightSetting = new SliderSetting("Height", 0.3f, 0.05f, 2.0f, 0.05f, this);
    private final SliderSetting offsetYSetting = new SliderSetting("Offset Y", 0.4f, -1.0f, 2.0f, 0.05f, this);
    private final SliderSetting alphaSetting = new SliderSetting("Alpha", 1.0f, 0.0f, 1.0f, 0.05f, this);

    // Настройки вращения
    private final BooleanSetting enableRotation = new BooleanSetting("Enable Rotation", true, this);
    private final SliderSetting rotationSpeed = new SliderSetting("Rotation Speed", 1.0f, 0.1f, 5.0f, 0.1f, this, enableRotation::getValue);

    // Настройки градиента
    private final BooleanSetting enableGradient = new BooleanSetting("Enable Gradient", true, this);
    private final ColorSetting startColorSetting = new ColorSetting("Start Color", new Color(0xFF0000), this, enableGradient::getValue);
    private final ColorSetting endColorSetting = new ColorSetting("End Color", new Color(0x0000FF), this, enableGradient::getValue);
    private final ModeSetting gradientDirection = new ModeSetting("Gradient Direction",
            new String[]{"Top to Bottom", "Side to Side", "Radial"},
            "Top to Bottom",
            this,
            enableGradient::getValue
    );
    private final SliderSetting gradientIntensity = new SliderSetting("Gradient Intensity", 1.0f, 0.0f, 1.0f, 0.1f, this, enableGradient::getValue);

    // Настройки эффектов
    private final BooleanSetting enablePulse = new BooleanSetting("Enable Pulse", false, this);
    private final SliderSetting pulseSpeed = new SliderSetting("Pulse Speed", 1.0f, 0.1f, 3.0f, 0.1f, this, enablePulse::getValue);
    private final SliderSetting pulseAmplitude = new SliderSetting("Pulse Amplitude", 0.1f, 0.0f, 0.5f, 0.05f, this, enablePulse::getValue);

    // Настройка рендеринга
    private final ModeSetting renderMode = new ModeSetting("Render Mode",
            new String[]{"All Players", "Only Me", "Only Friends"},
            "All Players",
            this
    );

    public ChinaHatModule(String name, Category category) {
        super(name, category);
        // Добавляем все настройки в список
        settings.add(widthSetting);
        settings.add(heightSetting);
        settings.add(offsetYSetting);
        settings.add(alphaSetting);
        settings.add(enableRotation);
        settings.add(rotationSpeed);
        settings.add(enableGradient);
        settings.add(startColorSetting);
        settings.add(endColorSetting);
        settings.add(gradientDirection);
        settings.add(gradientIntensity);
        settings.add(enablePulse);
        settings.add(pulseSpeed);
        settings.add(pulseAmplitude);
        settings.add(renderMode);
    }

    @Override
    public void onEnable() {
        System.out.println("ChinaHat enabled");
    }

    @Override
    public void onDisable() {
        System.out.println("ChinaHat disabled");
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        if (!enabled || !shouldRender(event.getEntityPlayer())) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        EntityPlayer player = event.getEntityPlayer();
        if (player.isInvisible()) return;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        // Получаем рендерер игрока и модель для доступа к голове
        RenderPlayer render = event.getRenderer();
        ModelPlayer model = render.getMainModel();

        // Устанавливаем углы ротации модели игрока
        float partialTicks = event.getPartialRenderTick();
        model.setRotationAngles(partialTicks, 0, 0, player.ticksExisted, player.rotationYawHead, player.rotationPitch, player);

        // Интерполированная позиция игрока
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;
        GlStateManager.translate(x, y, z);

        // Перемещаем к позиции головы до ротации
        float headHeight = player.height - 0.2f; // Базовая высота до головы
        GlStateManager.translate(0, headHeight, 0); // Смещаем к голове

        // Применяем ротацию тела игрока
        float bodyYaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        GlStateManager.rotate(-bodyYaw, 0, 1, 0); // Поворот тела

        // Применяем ротацию головы
        float headYaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
        float headPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        GlStateManager.rotate(-(headYaw - bodyYaw), 0, 1, 0); // Поворот по горизонтали
        GlStateManager.rotate(headPitch, 1, 0, 0); // Поворот по вертикали

        // Дополнительное смещение для посадки шляпы (после ротации)
        GlStateManager.translate(0, offsetYSetting.getValue() + 0.1f, 0); // Фиксированное смещение над головой

        // Эффект пульсации
        float pulseScale = 1.0f;
        if (enablePulse.getValue()) {
            float pulse = (float) Math.sin(System.currentTimeMillis() * pulseSpeed.getValue() / 1000.0) * pulseAmplitude.getValue();
            pulseScale = 1.0f + pulse;
        }
        GlStateManager.scale(pulseScale, 1.0f, pulseScale);

        // Основные цвета
        float width = widthSetting.getValue();
        float height = heightSetting.getValue();
        Color startColor = startColorSetting.getValue();
        Color endColor = endColorSetting.getValue();
        float startRed = startColor.getRed() / 255.0f;
        float startGreen = startColor.getGreen() / 255.0f;
        float startBlue = startColor.getBlue() / 255.0f;
        float startAlpha = (startColor.getAlpha() / 255.0f) * alphaSetting.getValue();
        float endRed = endColor.getRed() / 255.0f;
        float endGreen = endColor.getGreen() / 255.0f;
        float endBlue = endColor.getBlue() / 255.0f;
        float endAlpha = (endColor.getAlpha() / 255.0f) * alphaSetting.getValue();

        // Динамический градиент для визуализации вращения
        long time = System.currentTimeMillis();
        float rotationOffset = (float) (time * rotationSpeed.getValue() / 1000.0); // Динамическое смещение
        float baseAngle = (float) Math.toRadians(headYaw); // Угол головы для синхронизации

        // Рендеринг шляпы с градиентом
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        // Вершина шляпы (центр)
        GL11.glColor4f(startRed, startGreen, startBlue, startAlpha);
        GL11.glVertex3d(0, height, 0);

        for (int i = 0; i <= 360; i += 10) {
            double angle = Math.toRadians(i) + rotationOffset + baseAngle; // Смещаем угол для эффекта вращения
            double hatX = Math.cos(angle) * width;
            double hatZ = Math.sin(angle) * width;

            // Динамический градиент, зависящий от угла
            float gradientProgress = (float) ((Math.cos(angle) + 1) / 2); // От 0 до 1 в зависимости от косинуса
            gradientProgress = (float) Math.max(0, Math.min(1, gradientProgress + 0.2f * Math.sin(time * 0.001))); // Добавляем легкую пульсацию

            float interpolatedRed = startRed + (endRed - startRed) * gradientProgress;
            float interpolatedGreen = startGreen + (endGreen - startGreen) * gradientProgress;
            float interpolatedBlue = startBlue + (endBlue - startBlue) * gradientProgress;
            float interpolatedAlpha = startAlpha + (endAlpha - startAlpha) * gradientProgress;

            GL11.glColor4f(interpolatedRed, interpolatedGreen, interpolatedBlue, interpolatedAlpha);
            GL11.glVertex3d(hatX, 0, hatZ);
        }
        GL11.glEnd();

        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    // Новый вспомогательный метод для рендеринга геометрии шляпы
    private void renderHatGeometry(float width, float height, Color startColor, Color endColor) {
        float startRed = startColor.getRed() / 255.0f;
        float startGreen = startColor.getGreen() / 255.0f;
        float startBlue = startColor.getBlue() / 255.0f;
        float startAlpha = (startColor.getAlpha() / 255.0f) * alphaSetting.getValue();
        float endRed = endColor.getRed() / 255.0f;
        float endGreen = endColor.getGreen() / 255.0f;
        float endBlue = endColor.getBlue() / 255.0f;
        float endAlpha = (endColor.getAlpha() / 255.0f) * alphaSetting.getValue();

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glColor4f(startRed, startGreen, startBlue, startAlpha);
        GL11.glVertex3d(0, height, 0); // Вершина шляпы
        for (int i = 0; i <= 360; i += 10) {
            double angle = Math.toRadians(i);
            double hatX = Math.cos(angle) * width;
            double hatZ = Math.sin(angle) * width;

            if (enableGradient.getValue()) {
                float gradientProgress = 0f;
                switch (gradientDirection.getMode()) { // Заменил getValue на getMode
                    case "Top to Bottom":
                        gradientProgress = (float) (height - Math.abs(0)) / height;
                        break;
                    case "Side to Side":
                        gradientProgress = (float) (angle / (2 * Math.PI));
                        break;
                    case "Radial":
                        double distFromCenter = Math.sqrt(hatX * hatX + hatZ * hatZ) / width;
                        gradientProgress = (float) distFromCenter;
                        break;
                }
                gradientProgress *= gradientIntensity.getValue();

                float interpolatedRed = startRed + (endRed - startRed) * gradientProgress;
                float interpolatedGreen = startGreen + (endGreen - startGreen) * gradientProgress;
                float interpolatedBlue = startBlue + (endBlue - startBlue) * gradientProgress;
                float interpolatedAlpha = startAlpha + (endAlpha - startAlpha) * gradientProgress;

                GL11.glColor4f(interpolatedRed, interpolatedGreen, interpolatedBlue, interpolatedAlpha);
            } else {
                GL11.glColor4f(endRed, endGreen, endBlue, endAlpha);
            }
            GL11.glVertex3d(hatX, 0, hatZ);
        }
        GL11.glEnd();
    }

    private boolean shouldRender(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return false;

        switch (renderMode.getMode()) { // Заменил getValue на getMode
            case "Only Me":
                return player == mc.player;
            case "Only Friends":
                return FriendManager.isFriend(player.getName());
            case "All Players":
            default:
                return true;
        }
    }
}