package com.ded.macanclient;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Animations extends Module {
    private final BooleanSetting old = new BooleanSetting("Old", false, this);
    private final ModeSetting type = new ModeSetting("Block Anim", new String[]{"Swank", "Swing", "Swang", "Swong", "Swaing", "Punch", "Virtue", "Push", "Stella", "Styles", "Slide", "Interia", "Ethereal", "1.7", "Sigma", "Exhibition", "Old Exhibition", "Smooth", "Moon", "Leaked", "Astolfo", "Small"}, "1.7", this, () -> !old.getValue());
    private final BooleanSetting blockWhenSwing = new BooleanSetting("Block When Swing", false, this);
    private final ModeSetting hit = new ModeSetting("Hit", new String[]{"Vanilla", "Smooth"}, "Vanilla", this, () -> !old.getValue());
    private final SliderSetting slowdown = new SliderSetting("Slow Down", 0f, -5f, 15f, 1f, this);
    private final SliderSetting downscaleFactor = new SliderSetting("Scale", 0f, 0.0f, 0.5f, 0.1f, this);
    private final BooleanSetting rotating = new BooleanSetting("Rotating", false, this, () -> !old.getValue());
    private final SliderSetting x = new SliderSetting("Item-X", 0.0f, -1.0f, 1.0f, 0.05f, this);
    private final SliderSetting y = new SliderSetting("Item-Y", 0.0f, -1.0f, 1.0f, 0.05f, this);
    private final SliderSetting z = new SliderSetting("Item-Z", 0.0f, -1.0f, 1.0f, 0.05f, this);
    private final SliderSetting bx = new SliderSetting("Block-X", 0.0f, -1.0f, 1.0f, 0.05f, this);
    private final SliderSetting by = new SliderSetting("Block-Y", 0.0f, -1.0f, 1.0f, 0.05f, this);
    private final SliderSetting bz = new SliderSetting("Block-Z", 0.0f, -1.0f, 1.0f, 0.05f, this);
    private final BooleanSetting walking = new BooleanSetting("Funny", false, this);
    private final BooleanSetting swingWhileUsingItem = new BooleanSetting("Swing While Using Item", false, this);
    private final ModeSetting renderMode = new ModeSetting("Render Mode", new String[]{"All Players", "Only Me", "Only Friends"}, "All Players", this);

    public Animations(String name, Category category) {
        super(name, category);
        settings.add(old);
        settings.add(type);
        settings.add(blockWhenSwing);
        settings.add(hit);
        settings.add(slowdown);
        settings.add(downscaleFactor);
        settings.add(rotating);
        settings.add(x);
        settings.add(y);
        settings.add(z);
        settings.add(bx);
        settings.add(by);
        settings.add(bz);
        settings.add(walking);
        settings.add(swingWhileUsingItem);
        settings.add(renderMode);
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (!enabled || !shouldRender()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return;

        // Логика рендеринга анимации (пример)
        // Здесь нужно добавить трансформации для анимации руки
        // В зависимости от выбранного типа (type.getMode()) и других настроек
        System.out.println("Rendering hand animation with type: " + type.getMode());

        // Пример: если включен режим "Old", используем старую анимацию
        if (old.getValue()) {
            // Здесь можно отменить стандартный рендеринг и применить старую анимацию
            event.setCanceled(true);
            // Добавить кастомный рендеринг
        } else {
            // Применяем кастомные трансформации в зависимости от настроек
            switch (type.getMode()) {
                case "Swank":
                    // Пример: изменяем положение руки
                    applyTransformations(event, 0.5f, 0.2f, 0.1f);
                    break;
                case "Swing":
                    applyTransformations(event, 0.3f, 0.1f, 0.0f);
                    break;
                // Добавь остальные типы анимаций
                default:
                    break;
            }
        }
    }

    private void applyTransformations(RenderHandEvent event, float xOffset, float yOffset, float zOffset) {
        // Здесь нужно использовать GlStateManager для изменения положения и поворота руки
        // Это пример, тебе нужно будет настроить под свои нужды
        // GlStateManager.pushMatrix();
        // GlStateManager.translate(xOffset + x.getValue(), yOffset + y.getValue(), zOffset + z.getValue());
        // GlStateManager.rotate(rotating.getValue() ? 45.0f : 0.0f, 0.0f, 1.0f, 0.0f);
        // GlStateManager.scale(1.0f - downscaleFactor.getValue(), 1.0f - downscaleFactor.getValue(), 1.0f - downscaleFactor.getValue());
        // GlStateManager.popMatrix();
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