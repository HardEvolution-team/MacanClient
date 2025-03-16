package com.ded.macanclient.features.modules.visual;

import com.ded.macanclient.features.modules.player.FriendManager;
import com.ded.macanclient.settings.ModeSetting;
import com.ded.macanclient.features.Module;
import com.ded.macanclient.features.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;

public class DamageParticles extends Module {
    private final java.util.Map<EntityLivingBase, Float> healthMap = new java.util.HashMap<>();
    private final ArrayDeque<Particles> particles = new ArrayDeque<>();

    private final ModeSetting renderMode = new ModeSetting("Render Mode", new String[]{"All Players", "Only Me", "Only Friends"}, "All Players", this);

    public DamageParticles(String name, Category category) {
        super(name, category);
        settings.add(renderMode);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("DamageParticles enabled");
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        particles.clear();
        System.out.println("DamageParticles disabled");
    }

    @SubscribeEvent
    public void onEntityUpdate(RenderWorldLastEvent event) {
        for (Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == Minecraft.getMinecraft().player) continue;
            if (!shouldRenderForEntity((EntityLivingBase) entity)) continue;
            if (!healthMap.containsKey(entity)) healthMap.put((EntityLivingBase) entity, ((EntityLivingBase) entity).getHealth());
            float prevHealth = healthMap.get(entity);
            float health = ((EntityLivingBase) entity).getHealth();
            if (prevHealth != health) {
                boolean heal = health > prevHealth;
                boolean crit = entity.hurtResistantTime < 18 || Minecraft.getMinecraft().player.motionY < 0 && !Minecraft.getMinecraft().player.onGround;
                String color = heal ? "\247a" : crit ? "\247c" : "\247e";
                String text = prevHealth - health < 0.0f ? color + roundToPlace((prevHealth - health) * -1.0f, 1) : color + roundToPlace(prevHealth - health, 1);
                Location location = new Location((EntityLivingBase) entity);
                location.setY(entity.getEntityBoundingBox().minY + (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) / 2.0);
                location.setX(location.getX() - 0.5 + new java.util.Random().nextInt(5) * 0.1);
                location.setZ(location.getZ() - 0.5 + new java.util.Random().nextInt(5) * 0.1);
                particles.add(new Particles(location, text));
                healthMap.put((EntityLivingBase) entity, health);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) return;

        // Обновление здоровья и добавление частиц
        for (Entity entity : mc.world.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == mc.player) continue;
            if (!shouldRenderForEntity((EntityLivingBase) entity)) continue;
            if (!healthMap.containsKey(entity)) healthMap.put((EntityLivingBase) entity, ((EntityLivingBase) entity).getHealth());
            float prevHealth = healthMap.get(entity);
            float health = ((EntityLivingBase) entity).getHealth();
            if (prevHealth != health) {
                boolean heal = health > prevHealth;
                boolean crit = entity.hurtResistantTime < 18 || mc.player.motionY < 0 && !mc.player.onGround;
                String color = heal ? "\247a" : crit ? "\247c" : "\247e";
                String text = prevHealth - health < 0.0f ? color + roundToPlace((prevHealth - health) * -1.0f, 1) : color + roundToPlace(prevHealth - health, 1);
                Location location = new Location((EntityLivingBase) entity);
                location.setY(entity.getEntityBoundingBox().minY + (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) / 2.0);
                location.setX(location.getX() - 0.5 + new java.util.Random().nextInt(5) * 0.1);
                location.setZ(location.getZ() - 0.5 + new java.util.Random().nextInt(5) * 0.1);
                particles.add(new Particles(location, text));
                healthMap.put((EntityLivingBase) entity, health);
            }
        }

        // Рендеринг частиц
        for (Particles p : particles) {
            double x = p.location.getX() - mc.getRenderManager().viewerPosX;
            double y = p.location.getY() - mc.getRenderManager().viewerPosY;
            double z = p.location.getZ() - mc.getRenderManager().viewerPosZ;
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(1.0f, -1500000.0f);
            GL11.glTranslated(x, y, z);
            GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            float textY = mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f;
            GL11.glRotatef(mc.getRenderManager().playerViewX, textY, 0.0f, 0.0f);
            double size = 0.03;
            GL11.glScalef((float) -size, (float) -size, (float) size);
            GL11.glDepthMask(false);
            mc.fontRenderer.drawStringWithShadow(p.text, -(mc.fontRenderer.getStringWidth(p.text) / 2), -(mc.fontRenderer.FONT_HEIGHT - 1), 0);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDepthMask(true);
            GL11.glPolygonOffset(1.0f, 1500000.0f);
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPopMatrix();
        }

        // Анимация и удаление частиц
        for (var iterator = particles.iterator(); iterator.hasNext(); ) {
            Particles update = iterator.next();
            update.ticks++;
            if (update.ticks <= 10) update.location.setY(update.location.getY() + update.ticks * 0.005);
            if (update.ticks > 20) iterator.remove();
        }
    }
    @SubscribeEvent
    public void onUpdate(RenderWorldLastEvent event) {
        for (var iterator = particles.iterator(); iterator.hasNext(); ) {
            Particles update = iterator.next();
            update.ticks++;
            if (update.ticks <= 10) update.location.setY(update.location.getY() + update.ticks * 0.005);
            if (update.ticks > 20) iterator.remove();
        }
    }

    public static double roundToPlace(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean shouldRenderForEntity(EntityLivingBase entity) {
        String playerName = entity.getName();
        if (renderMode.getValue().equals("Only Me")) {
            return playerName.equals(Minecraft.getMinecraft().player.getName());
        } else if (renderMode.getValue().equals("Only Friends")) {
            return FriendManager.isFriend(playerName);
        }
        return true;
    }


    public static class Particles {
        public int ticks;
        public Location location;
        public String text;

        public Particles(Location location, String text) {
            this.location = location;
            this.text = text;
            this.ticks = 0;
        }
    }

    public static class Location {
        private double x;
        private double y;
        private double z;

        public Location(EntityLivingBase entity) {
            this.x = entity.posX;
            this.y = entity.posY;
            this.z = entity.posZ;
        }

        public void setX(double x) { this.x = x; }
        public void setY(double y) { this.y = y; }
        public void setZ(double z) { this.z = z; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
    }
}