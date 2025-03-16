package com.ded.macanclient.features.modules.combat;

import com.ded.macanclient.features.Module;
import com.ded.macanclient.features.Category;
import com.ded.macanclient.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KillAuraModule extends Module {
    // Оптимизированные настройки
    private final IntegerSetting cpsSetting = new IntegerSetting("CPS", 8, 1, 20, this);
    private final SliderSetting rangeSetting = new SliderSetting("Range", 4.0f, 1.0f, 6.0f, 0.1f, this);
    private final BooleanSetting pvpSetting = new BooleanSetting("1.9+ PvP", true, this);
    private final BooleanSetting attackMobsSetting = new BooleanSetting("Attack Mobs", true, this);
    private final BooleanSetting antiBotSetting = new BooleanSetting("AntiBot", true, this);
    private final BooleanSetting smoothAimSetting = new BooleanSetting("Smooth Aim", true, this);
    private final BooleanSetting smoothStartSetting = new BooleanSetting("Smooth Start", false, this);
    private final StringSetting attackStyleSetting;

    // Оптимизированные переменные
    private long lastAttackTime = 0;
    private long lastPauseTime = 0;
    private long smoothStartTime = 0;
    private long smoothEndTime = 0;
    private int attackCounter = 0;
    private boolean inCombat = false;
    private long startDuration = 0;
    private long endDuration = 0;
    private final Random random = new Random();
    private final ConcurrentLinkedQueue<Entity> targetQueue = new ConcurrentLinkedQueue<>();
    private float currentYaw = 0f;
    private float currentPitch = 0f;
    private boolean anglesInitialized = false; // Флаг для отслеживания инициализации углов
    private static final float AIM_SPEED = 0.1f;
    private static final double MAX_FOV = 90.0;

    public KillAuraModule(String name, Category category) {
        super(name, category);
        attackStyleSetting = new StringSetting("Attack Style",
                new String[]{"Single", "Switch", "Multi"},
                "Single",
                this,
                () -> !pvpSetting.getValue()
        );

        updateSettingsBasedOnPvp();
    }

    private void updateSettingsBasedOnPvp() {
        settings.clear();
        ArrayList<Setting<?>> newSettings = new ArrayList<>();
        newSettings.add(pvpSetting);
        newSettings.add(rangeSetting);
        newSettings.add(attackMobsSetting);
        newSettings.add(antiBotSetting);
        newSettings.add(smoothAimSetting);
        newSettings.add(smoothStartSetting);
        if (!pvpSetting.getValue()) {
            newSettings.add(cpsSetting);
            newSettings.add(attackStyleSetting);
        }
        newSettings.add(new ModeSetting("Render Mode",
                new String[]{"All Players", "Only Me", "Only Friends"},
                "All Players",
                this
        ));
        settings.addAll(newSettings);
    }

    @Override
    protected void onEnable() {
        targetQueue.clear();
        lastAttackTime = 0;
        lastPauseTime = 0;
        attackCounter = 0;
        inCombat = false;
        smoothStartTime = System.currentTimeMillis();
        smoothEndTime = 0;
        startDuration = 1000 + random.nextInt(1000);
        endDuration = 1000 + random.nextInt(1000);
        anglesInitialized = false; // Сбрасываем флаг при включении
    }

    @Override
    protected void onDisable() {
        targetQueue.clear();
        inCombat = false;
        smoothEndTime = 0;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!enabled || event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null || mc.player.isSpectator()) return;

        // Инициализация углов поворота при первом тике после включения
        if (!anglesInitialized) {
            currentYaw = mc.player.rotationYaw;
            currentPitch = mc.player.rotationPitch;
            anglesInitialized = true;
        }

        long currentTime = System.currentTimeMillis();

        // Проверка на паузу после определенного количества атак
        if (attackCounter > 8 + random.nextInt(4)) {
            if (currentTime - lastPauseTime < 1500 + random.nextInt(1500)) {
                return;
            }
            attackCounter = 0;
            lastPauseTime = currentTime;
        }

        updateTargets(mc);
        if (targetQueue.isEmpty()) {
            handleCombatEnd(currentTime);
            return;
        }

        Entity target = targetQueue.peek();
        if (target == null || !target.isEntityAlive()) {
            targetQueue.poll();
            return;
        }

        long delay = calculateDynamicDelay(currentTime);
        if (currentTime - lastAttackTime < delay) return;

        inCombat = true;
        smoothEndTime = 0;
        smoothAim(target);
        if (shouldAttack(mc, target)) {
            performAttack(mc, target, delay);
            lastAttackTime = currentTime;
            attackCounter++;
        }
    }

    private void updateTargets(Minecraft mc) {
        targetQueue.clear();
        double closestDist = Double.MAX_VALUE;
        Entity closestTarget = null;

        for (Entity entity : mc.world.loadedEntityList) {
            if (!isValidTarget(mc, entity)) continue;

            double dist = mc.player.getDistanceSq(entity);
            if (dist <= rangeSetting.getValue() * rangeSetting.getValue() && dist < closestDist) {
                if (isVisible(mc, entity) && isInFOV(entity)) {
                    closestDist = dist;
                    closestTarget = entity;
                }
            }
        }

        if (closestTarget != null) {
            targetQueue.offer(closestTarget);
        }
    }

    private boolean isValidTarget(Minecraft mc, Entity entity) {
        if (!(entity instanceof EntityLivingBase) || entity == mc.player || !entity.isEntityAlive())
            return false;
        if (antiBotSetting.getValue() && isBot(entity))
            return false;
        return (entity instanceof EntityPlayer) ||
                (attackMobsSetting.getValue() && entity instanceof EntityMob);
    }

    private long calculateDynamicDelay(long currentTime) {
        if (pvpSetting.getValue()) {
            Minecraft mc = Minecraft.getMinecraft();
            return (long) (1000.0f / mc.player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue());
        }

        if (!smoothStartSetting.getValue() && !inCombat) {
            return (long) (1000.0f / (Math.min(cpsSetting.getValue(), 10) + random.nextFloat() * 0.5f - 0.25f));
        }

        if (inCombat) {
            long startTime = smoothStartTime;
            float progress = Math.min(1.0f, (currentTime - startTime) / (float) startDuration);
            int baseCPS = (int) (3 + progress * (cpsSetting.getValue() - 3));
            float randomFactor = 0.5f + progress * 0.5f;
            return (long) (1000.0f / (baseCPS + random.nextFloat() * randomFactor - randomFactor / 2));
        } else if (smoothEndTime > 0) {
            float endProgress = Math.min(1.0f, (currentTime - smoothEndTime) / (float) endDuration);
            int baseCPS = (int) (1 + (1.0f - endProgress) * (cpsSetting.getValue() - 1));
            float randomFactor = 0.5f + (1.0f - endProgress) * 0.5f;
            return (long) (1000.0f / (baseCPS + random.nextFloat() * randomFactor - randomFactor / 2));
        }
        return (long) (1000.0f / (Math.min(cpsSetting.getValue(), 10) + random.nextFloat() * 0.5f - 0.25f));
    }

    private void handleCombatEnd(long currentTime) {
        if (inCombat) {
            if (smoothEndTime == 0) {
                smoothEndTime = currentTime;
            }
            float endProgress = Math.min(1.0f, (currentTime - smoothEndTime) / (float) endDuration);
            if (endProgress >= 1.0f) {
                inCombat = false;
                smoothEndTime = 0;
            }
        }
    }

    private void smoothAim(Entity target) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3d targetPos = target.getPositionEyes(1.0f);
        Vec3d playerPos = mc.player.getPositionEyes(1.0f);

        double dX = targetPos.x - playerPos.x;
        double dY = targetPos.y - playerPos.y;
        double dZ = targetPos.z - playerPos.z;

        float targetYaw = (float) (Math.toDegrees(Math.atan2(dZ, dX)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(dY, Math.sqrt(dX * dX + dZ * dZ))));

        if (smoothAimSetting.getValue()) {
            currentYaw = interpolateAngle(currentYaw, targetYaw, AIM_SPEED);
            currentPitch = interpolateAngle(currentPitch, targetPitch, AIM_SPEED);
        } else {
            currentYaw = targetYaw;
            currentPitch = targetPitch;
        }

        mc.player.rotationYaw = currentYaw + (random.nextFloat() - 0.5f) * 2f;
        mc.player.rotationPitch = Math.max(-90.0f, Math.min(90.0f, currentPitch));
    }

    private float interpolateAngle(float current, float target, float speed) {
        float diff = normalizeAngle(target - current);
        return current + diff * speed;
    }

    private float normalizeAngle(float angle) {
        while (angle > 180.0f) angle -= 360.0f;
        while (angle <= -180.0f) angle += 360.0f;
        return angle;
    }

    private boolean shouldAttack(Minecraft mc, Entity target) {
        if (pvpSetting.getValue() && mc.player.getCooledAttackStrength(0.0f) < 0.95f)
            return false;
        return random.nextInt(100) >= 5; // 5% шанс промаха
    }

    private void performAttack(Minecraft mc, Entity target, long delay) {
        if (!pvpSetting.getValue()) {
            String style = attackStyleSetting.getValue();
            switch (style) {
                case "Switch":
                    targetQueue.poll();
                    targetQueue.offer(target);
                    // Intentional fallthrough
                case "Single":
                    mc.playerController.attackEntity(mc.player, target);
                    break;
                case "Multi":
                    mc.world.loadedEntityList.stream()
                            .filter(e -> isValidTarget(mc, e) && mc.player.getDistanceSq(e) <= rangeSetting.getValue() * rangeSetting.getValue())
                            .limit(3)
                            .forEach(e -> mc.playerController.attackEntity(mc.player, e));
                    break;
            }
        } else {
            mc.playerController.attackEntity(mc.player, target);
        }

        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
    }

    private boolean isInFOV(Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3d look = mc.player.getLookVec();
        Vec3d toEntity = entity.getPositionEyes(1.0f)
                .subtract(mc.player.getPositionEyes(1.0f))
                .normalize();
        return Math.toDegrees(Math.acos(look.dotProduct(toEntity))) <= MAX_FOV;
    }

    private boolean isVisible(Minecraft mc, Entity entity) {
        Vec3d start = mc.player.getPositionEyes(1.0f);
        Vec3d end = entity.getPositionEyes(1.0f);
        RayTraceResult result = mc.world.rayTraceBlocks(start, end, false, true, false);
        return result == null || result.typeOfHit != RayTraceResult.Type.BLOCK;
    }

    private boolean isBot(Entity entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        EntityPlayer player = (EntityPlayer) entity;
        Minecraft mc = Minecraft.getMinecraft();

        return antiBotSetting.getValue() && (
                player.isInvisible() && !player.isInvisibleToPlayer(mc.player) ||
                        player.ticksExisted < 30 ||
                        mc.player.connection.getPlayerInfo(player.getUniqueID()) == null
        );
    }

    @Override
    public void onSettingChanged(Setting<?> setting) {
        if (setting == pvpSetting) {
            updateSettingsBasedOnPvp();
        }
    }
}