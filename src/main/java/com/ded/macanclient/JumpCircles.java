package com.ded.macanclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JumpCircles extends Module {
    private final List<JumpRenderer> circles = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private boolean isActuallyEnabled = false;

    // Основные настройки для кругов (ещё сильнее уменьшены)
    private final SliderSetting maxTime = new SliderSetting("Max Time", 1500, 1000, 3000, 25f, this);
    private final SliderSetting radius = new SliderSetting("Radius", 1.0f, 0.5f, 2.0f, 0.1f, this);
    private final SliderSetting animationSpeed = new SliderSetting("Animation Speed", 0.5f, 0.3f, 1.0f, 0.1f, this);
    private final SliderSetting thickness = new SliderSetting("Thickness", 0.02f, 0.01f, 0.1f, 0.01f, this);
    private final SliderSetting heightOffset = new SliderSetting("Height Offset", 0.0f, 0.0f, 0.2f, 0.01f, this);
    private final SliderSetting circleCount = new SliderSetting("Circle Count", 1, 1, 2, 1.0f, this);
    private final SliderSetting alpha = new SliderSetting("Alpha", 0.3f, 0.1f, 0.8f, 0.1f, this);
    private final SliderSetting detailLevel = new SliderSetting("Detail Level", 2, 1, 5, 1.0f, this);
    private final SliderSetting lineWidth = new SliderSetting("Line Width", 1.0f, 0.5f, 2.0f, 0.1f, this);
    private final BooleanSetting antiAlias = new BooleanSetting("Anti-Alias", false, this);

    // Цветовые настройки
    private final ColorSetting startColor = new ColorSetting("Start Color", Color.WHITE, this);
    private final ColorSetting endColor = new ColorSetting("End Color", Color.CYAN, this);
    private final BooleanSetting gradientEnabled = new BooleanSetting("Gradient Enabled", false, this);

    // Настройки для частиц (ещё сильнее уменьшены)
    private final BooleanSetting enableParticles = new BooleanSetting("Enable Particles", false, this);
    private final SliderSetting particleCount = new SliderSetting("Particle Count", 5, 1, 20, 1.0f, this);
    private final SliderSetting particleSize = new SliderSetting("Particle Size", 0.01f, 0.005f, 0.05f, 0.005f, this);
    private final SliderSetting particleSpeed = new SliderSetting("Particle Speed", 0.02f, 0.01f, 0.1f, 0.01f, this);
    private final SliderSetting particleLifeTime = new SliderSetting("Particle Life Time", 300, 100, 1000, 50f, this);
    private final ColorSetting particleColor = new ColorSetting("Particle Color", Color.YELLOW, this);

    // Дополнительные визуальные настройки (упрощены)
    private final ModeSetting renderMode = new ModeSetting("Render Mode", new String[]{"All Players", "Only Me", "Only Friends"}, "Only Me", this);
    private final ModeSetting circleStyle = new ModeSetting("Circle Style", new String[]{"Solid", "Dashed"}, "Solid", this);

    // Файл для сохранения настроек
    private static final File CONFIG_FILE = new File("config/jumpcircles.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private boolean hasJumped = false;

    public JumpCircles(String name, Category category) {
        super(name, category);
        settings.add(maxTime);
        settings.add(radius);
        settings.add(animationSpeed);
        settings.add(thickness);
        settings.add(heightOffset);
        settings.add(circleCount);
        settings.add(alpha);
        settings.add(detailLevel);
        settings.add(lineWidth);
        settings.add(antiAlias);
        settings.add(startColor);
        settings.add(endColor);
        settings.add(gradientEnabled);
        settings.add(enableParticles);
        settings.add(particleCount);
        settings.add(particleSize);
        settings.add(particleSpeed);
        settings.add(particleLifeTime);
        settings.add(particleColor);
        settings.add(renderMode);
        settings.add(circleStyle);

        // Загрузка настроек при инициализации
        loadSettings();
    }

    // Сохранение настроек в файл
    private void saveSettings() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            SettingsData data = new SettingsData();
            data.maxTime = maxTime.getValue();
            data.radius = radius.getValue();
            data.animationSpeed = animationSpeed.getValue();
            data.thickness = thickness.getValue();
            data.heightOffset = heightOffset.getValue();
            data.circleCount = circleCount.getValue();
            data.alpha = alpha.getValue();
            data.detailLevel = detailLevel.getValue();
            data.lineWidth = lineWidth.getValue();
            data.antiAlias = antiAlias.getValue();
            data.startColor = startColor.getValue().getRGB();
            data.endColor = endColor.getValue().getRGB();
            data.gradientEnabled = gradientEnabled.getValue();
            data.enableParticles = enableParticles.getValue();
            data.particleCount = particleCount.getValue();
            data.particleSize = particleSize.getValue();
            data.particleSpeed = particleSpeed.getValue();
            data.particleLifeTime = particleLifeTime.getValue();
            data.particleColor = particleColor.getValue().getRGB();
            data.renderMode = (String) renderMode.getValue();
            data.circleStyle = (String) circleStyle.getValue();

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                gson.toJson(data, writer);
            }
            debugLog("Settings saved successfully.");
        } catch (IOException e) {
            debugLog("Failed to save settings: " + e.getMessage());
        }
    }

    // Загрузка настроек из файла
    private void loadSettings() {
        if (!CONFIG_FILE.exists()) {
            saveSettings(); // Создаём файл с настройками по умолчанию, если его нет
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            SettingsData data = gson.fromJson(reader, SettingsData.class);
            if (data != null) {
                maxTime.setValue(data.maxTime);
                radius.setValue(data.radius);
                animationSpeed.setValue(data.animationSpeed);
                thickness.setValue(data.thickness);
                heightOffset.setValue(data.heightOffset);
                circleCount.setValue(data.circleCount);
                alpha.setValue(data.alpha);
                detailLevel.setValue(data.detailLevel);
                lineWidth.setValue(data.lineWidth);
                antiAlias.setValue(data.antiAlias);
                startColor.setValue(new Color(data.startColor));
                endColor.setValue(new Color(data.endColor));
                gradientEnabled.setValue(data.gradientEnabled);
                enableParticles.setValue(data.enableParticles);
                particleCount.setValue(data.particleCount);
                particleSize.setValue(data.particleSize);
                particleSpeed.setValue(data.particleSpeed);
                particleLifeTime.setValue(data.particleLifeTime);
                particleColor.setValue(new Color(data.particleColor));
                renderMode.setValue(data.renderMode); // Исправлено: setMode -> setValue
                circleStyle.setValue(data.circleStyle); // Исправлено: setMode -> setValue
                debugLog("Settings loaded successfully.");
            }
        } catch (IOException e) {
            debugLog("Failed to load settings: " + e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        isActuallyEnabled = true;
        MinecraftForge.EVENT_BUS.register(this);
        debugLog("JumpCircles enabled");
    }

    @Override
    public void onDisable() {
        isActuallyEnabled = false;
        MinecraftForge.EVENT_BUS.unregister(this);
        circles.clear();
        particles.clear();
        saveSettings();
        debugLog("JumpCircles disabled");
    }

    @SubscribeEvent
    public void onJump(LivingEvent.LivingJumpEvent event) {
        if (!isActuallyEnabled) return;
        if (event.getEntityLiving() == Minecraft.getMinecraft().player) {
            hasJumped = true;
        }
    }

    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        if (!isActuallyEnabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player.onGround && hasJumped) {
            for (int i = 0; i < circleCount.getValue(); i++) {
                addCircleForEntity(mc.player, i);
            }
            hasJumped = false;
        }

        circles.removeIf(circle -> circle.getDeltaTime() >= maxTime.getValue());
        particles.removeIf(particle -> particle.getAge() >= particleLifeTime.getValue());

        if (circles.isEmpty() && particles.isEmpty()) return;

        updateParticles();

        setupDraw(() -> {
            circles.forEach(circle -> doCircle(circle.pos, radius.getValue(), circle.getDeltaTime() / maxTime.getValue(), circle.getIndex() * 30, circle.getCircleIndex()));
            renderParticles();
        });
    }

    private void doCircle(Vec3d pos, double maxRadius, float deltaTime, int index, int circleIndex) {
        float adjustedDeltaTime = deltaTime * animationSpeed.getValue();
        adjustedDeltaTime = Math.max(0.0f, Math.min(1.0f, adjustedDeltaTime));

        float waveDelta = valWave01(1.0F - adjustedDeltaTime);
        float alphaPC = (float) easeOutCirc(valWave01(1 - adjustedDeltaTime));
        if (adjustedDeltaTime < 0.5f) alphaPC *= (float) easeInOutExpo(alphaPC);
        float circleRadius = (float) ((adjustedDeltaTime > 0.5f ? easeOutElastic(waveDelta * waveDelta) : easeOutBack(waveDelta)) * maxRadius);

        if (enableParticles.getValue() && random.nextFloat() < 0.05f) {
            spawnParticles(pos, circleRadius, index);
        }

        Minecraft mc = Minecraft.getMinecraft();
        double x = pos.x - mc.getRenderManager().viewerPosX;
        double y = pos.y - mc.getRenderManager().viewerPosY;
        double z = pos.z - mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // Сбрасываем повороты камеры
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        // Применяем поворот для горизонтального круга
        GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);

        GlStateManager.glLineWidth(lineWidth.getValue());
        if (antiAlias.getValue()) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Color adjustedStartColor = animateColor(startColor.getValue(), adjustedDeltaTime);
        Color adjustedEndColor = animateColor(endColor.getValue(), adjustedDeltaTime);
        float startRed = adjustedStartColor.getRed() / 255.0f;
        float startGreen = adjustedStartColor.getGreen() / 255.0f;
        float startBlue = adjustedStartColor.getBlue() / 255.0f;
        float endRed = adjustedEndColor.getRed() / 255.0f;
        float endGreen = adjustedEndColor.getGreen() / 255.0f;
        float endBlue = adjustedEndColor.getBlue() / 255.0f;
        float adjustedAlpha = alphaPC * alpha.getValue();

        int angleStep = Math.max(1, (int) (10 / detailLevel.getValue()));

        debugLog("Rendering circle with style: " + circleStyle.getValue());

        String style = (String) circleStyle.getValue();
        if ("Solid".equals(style)) {
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 360; i += angleStep) {
                double angle = Math.toRadians(i);
                double dx = Math.cos(angle) * circleRadius;
                double dz = Math.sin(angle) * circleRadius;
                float gradientProgress = gradientEnabled.getValue() ? (float) i / 360.0f : 0;
                float r = startRed + (endRed - startRed) * gradientProgress;
                float g = startGreen + (endGreen - startGreen) * gradientProgress;
                float b = startBlue + (endBlue - startBlue) * gradientProgress;
                buffer.pos(dx, 0, dz).color(r, g, b, (int) (255 * adjustedAlpha)).endVertex();
            }
            tessellator.draw();
        } else if ("Dashed".equals(style)) {
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < 360; i += angleStep) {
                if (i % (angleStep * 4) < angleStep * 2) {
                    double angle = Math.toRadians(i);
                    double dx = Math.cos(angle) * circleRadius;
                    double dz = Math.sin(angle) * circleRadius;
                    float gradientProgress = gradientEnabled.getValue() ? (float) i / 360.0f : 0;
                    float r = startRed + (endRed - startRed) * gradientProgress;
                    float g = startGreen + (endGreen - startGreen) * gradientProgress;
                    float b = startBlue + (endBlue - startBlue) * gradientProgress;
                    buffer.pos(dx, 0, dz).color(r, g, b, (int) (255 * adjustedAlpha)).endVertex();
                }
            }
            tessellator.draw();
        }

        if (antiAlias.getValue()) {
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.popMatrix();
    }

    private void setupDraw(Runnable render) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        GlStateManager.disableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        render.run();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void addCircleForEntity(Entity entity, int circleIndex) {
        if (!shouldRenderForEntity(entity)) return;
        Vec3d vec = getVec3dFromEntity(entity).add(0.0, heightOffset.getValue(), 0.0);
        BlockPos pos = new BlockPos(vec);
        if (Minecraft.getMinecraft().world.getBlockState(pos).getBlock() == Blocks.SNOW) {
            vec = vec.add(0.0, 0.125, 0.0);
        }
        circles.add(new JumpRenderer(vec, circles.size(), circleIndex));
    }

    private Vec3d getVec3dFromEntity(Entity entity) {
        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        double dx = entity.posX - entity.lastTickPosX;
        double dy = entity.posY - entity.lastTickPosY;
        double dz = entity.posZ - entity.lastTickPosZ;
        return new Vec3d(entity.lastTickPosX + dx * partialTicks, entity.lastTickPosY + dy * partialTicks, entity.lastTickPosZ + dz * partialTicks);
    }

    private boolean shouldRenderForEntity(Entity entity) {
        String playerName = entity.getName();
        debugLog("Checking render mode: " + renderMode.getValue() + " for player: " + playerName);
        String mode = (String) renderMode.getValue();
        if ("Only Me".equals(mode)) {
            return playerName.equals(Minecraft.getMinecraft().player.getName());
        } else if ("Only Friends".equals(mode)) {
            return FriendManager.isFriend(playerName);
        } else {
            return true;
        }
    }

    private Color animateColor(Color baseColor, float progress) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        int alpha = (int) (baseColor.getAlpha() * (1 - progress));
        alpha = Math.max(0, Math.min(255, alpha));
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
    }

    private void spawnParticles(Vec3d pos, float circleRadius, int index) {
        Float count = particleCount.getValue();
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * circleRadius;
            double x = pos.x + Math.cos(angle) * radius;
            double y = pos.y + 0.1;
            double z = pos.z + Math.sin(angle) * radius;
            double vx = Math.cos(angle) * particleSpeed.getValue();
            double vy = random.nextDouble() * 0.02;
            double vz = Math.sin(angle) * particleSpeed.getValue();
            particles.add(new Particle(new Vec3d(x, y, z), new Vec3d(vx, vy, vz), particleColor.getValue()));
        }
    }

    private void updateParticles() {
        for (Particle particle : particles) {
            particle.update();
        }
    }

    private void renderParticles() {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        debugLog("Rendering " + particles.size() + " particles");

        Minecraft mc = Minecraft.getMinecraft();
        for (Particle particle : particles) {
            double x = particle.pos.x - mc.getRenderManager().viewerPosX;
            double y = particle.pos.y - mc.getRenderManager().viewerPosY;
            double z = particle.pos.z - mc.getRenderManager().viewerPosZ;

            float alpha = 1.0f - (particle.age / particleLifeTime.getValue());
            Color color = particle.color;
            float r = color.getRed() / 255.0f;
            float g = color.getGreen() / 255.0f;
            float b = color.getBlue() / 255.0f;
            float a = alpha * (color.getAlpha() / 255.0f);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            float size = particleSize.getValue();
            buffer.pos(x - size, y, z - size).color(r, g, b, a).endVertex();
            buffer.pos(x - size, y, z + size).color(r, g, b, a).endVertex();
            buffer.pos(x + size, y, z + size).color(r, g, b, a).endVertex();
            buffer.pos(x + size, y, z - size).color(r, g, b, a).endVertex();
            tessellator.draw();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static class JumpRenderer {
        private final long startTime = System.currentTimeMillis();
        private final Vec3d pos;
        private final int index;
        private final int circleIndex;

        private JumpRenderer(Vec3d pos, int index, int circleIndex) {
            this.pos = pos;
            this.index = index;
            this.circleIndex = circleIndex;
        }

        private float getDeltaTime() {
            return (float) (System.currentTimeMillis() - startTime);
        }

        private int getIndex() {
            return this.index;
        }

        private int getCircleIndex() {
            return this.circleIndex;
        }
    }

    private static class Particle {
        private Vec3d pos;
        private Vec3d velocity;
        private final Color color;
        private float age;

        private Particle(Vec3d pos, Vec3d velocity, Color color) {
            this.pos = pos;
            this.velocity = velocity;
            this.color = color;
            this.age = 0;
        }

        private void update() {
            pos = pos.add(velocity);
            velocity = velocity.add(0, -0.001, 0);
            age++;
        }

        private float getAge() {
            return age;
        }
    }

    private static class SettingsData {
        float maxTime;
        float radius;
        float animationSpeed;
        float thickness;
        float heightOffset;
        float circleCount;
        float alpha;
        float detailLevel;
        float lineWidth;
        boolean antiAlias;
        int startColor;
        int endColor;
        boolean gradientEnabled;
        boolean enableParticles;
        float particleCount;
        float particleSize;
        float particleSpeed;
        float particleLifeTime;
        int particleColor;
        String renderMode;
        String circleStyle;
    }

    private float valWave01(float value) {
        return (value > 0.5f ? 1 - value : value) * 2.0f;
    }

    private double easeOutCirc(double value) {
        return Math.sqrt(1 - Math.pow(value - 1, 2));
    }

    private double easeOutElastic(double value) {
        double c4 = (2 * Math.PI) / 3;
        return value <= 0 ? 0 : value >= 1 ? 1 : Math.pow(2, -10 * value) * Math.sin((value * 10 - 0.75) * c4) + 1;
    }

    private double easeOutBack(double value) {
        double c1 = 1.70158, c3 = c1 + 1;
        return 1 + c3 * Math.pow(value - 1, 3) + c1 * Math.pow(value - 1, 2);
    }

    private double easeInOutExpo(double value) {
        return value <= 0 ? 0 : value >= 1 ? 1 : value < 0.5 ? Math.pow(2, 20 * value - 10) / 2 : (2 - Math.pow(2, -20 * value + 10)) / 2;
    }

    private void debugLog(String message) {
        System.out.println("[JumpCircles Debug] " + message);
    }

    @Override
    public void onSettingChanged(Setting<?> setting) {
        saveSettings();
    }
}