package com.ded.macanclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Shaders extends Module {
    private final BooleanSetting blur = new BooleanSetting("Blur", true, this);
    private final SliderSetting blurRadius = new SliderSetting("Blur Radius", 8f, 1f, 50f, 1f, this, blur::getValue);
    private final SliderSetting blurCompression = new SliderSetting("Blur Compression", 2f, 1f, 50f, 1f, this, blur::getValue);
    private final BooleanSetting shadow = new BooleanSetting("Shadow", true, this);
    private final SliderSetting shadowRadius = new SliderSetting("Shadow Radius", 10f, 1f, 20f, 1f, this, shadow::getValue);
    private final SliderSetting shadowOffset = new SliderSetting("Shadow Offset", 1f, 1f, 15f, 1f, this, shadow::getValue);
    private final BooleanSetting bloom = new BooleanSetting("Bloom", false, this);
    private final SliderSetting glowRadius = new SliderSetting("Bloom Radius", 3f, 1f, 10f, 1f, this, bloom::getValue);
    private final SliderSetting glowOffset = new SliderSetting("Bloom Offset", 1f, 1f, 10f, 1f, this, bloom::getValue);
    private Framebuffer stencilFramebuffer;

    private final ModeSetting renderMode = new ModeSetting("Render Mode", new String[]{"All Players", "Only Me", "Only Friends"}, "All Players", this);

    public Shaders(String name, Category category) {
        super(name, category);
        settings.add(blur);
        settings.add(blurRadius);
        settings.add(blurCompression);
        settings.add(shadow);
        settings.add(shadowRadius);
        settings.add(shadowOffset);
        settings.add(bloom);
        settings.add(glowRadius);
        settings.add(glowOffset);
        settings.add(renderMode);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        setupFramebuffers();
        System.out.println("Shaders enabled");
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        if (stencilFramebuffer != null) {
            stencilFramebuffer.deleteFramebuffer();
            stencilFramebuffer = null;
        }
        System.out.println("Shaders disabled");
    }

    private void setupFramebuffers() {
        Minecraft mc = Minecraft.getMinecraft();
        if (stencilFramebuffer == null || stencilFramebuffer.framebufferWidth != mc.displayWidth || stencilFramebuffer.framebufferHeight != mc.displayHeight) {
            if (stencilFramebuffer != null) {
                stencilFramebuffer.deleteFramebuffer();
            }
            stencilFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
            stencilFramebuffer.setFramebufferFilter(9729); // GL_LINEAR
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !shouldRender()) return;
        renderShaders();
    }

    private void renderShaders() {
        Minecraft mc = Minecraft.getMinecraft();
        setupFramebuffers();

        // Сохраняем текущее состояние OpenGL
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        if (blur.getValue()) {
            stencilFramebuffer.bindFramebuffer(true);
            // Заглушка для Blur (добавь реализацию позже)
            System.out.println("Blur rendering with radius: " + blurRadius.getValue() + ", compression: " + blurCompression.getValue());
            mc.getFramebuffer().bindFramebuffer(true); // Возвращаем основной Framebuffer
        }

        if (bloom.getValue()) {
            stencilFramebuffer = createFrameBuffer(stencilFramebuffer, true);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(true);
            // Заглушка для Bloom
            System.out.println("Bloom rendering with radius: " + glowRadius.getValue() + ", offset: " + glowOffset.getValue());
            mc.getFramebuffer().bindFramebuffer(true); // Возвращаем основной Framebuffer
        }

        if (shadow.getValue()) {
            stencilFramebuffer = createFrameBuffer(stencilFramebuffer, true);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(true);
            // Заглушка для Shadow
            System.out.println("Shadow rendering with radius: " + shadowRadius.getValue() + ", offset: " + shadowOffset.getValue());
            mc.getFramebuffer().bindFramebuffer(true); // Возвращаем основной Framebuffer
        }

        // Восстанавливаем состояние OpenGL
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean useDepth) {
        Minecraft mc = Minecraft.getMinecraft();
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, useDepth);
            framebuffer.setFramebufferFilter(9729); // GL_LINEAR
        }
        return framebuffer;
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