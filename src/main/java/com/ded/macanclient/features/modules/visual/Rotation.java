package com.ded.macanclient.features.modules.visual;

import com.ded.macanclient.features.MathUtils;
import com.ded.macanclient.features.Category;
import com.ded.macanclient.features.Module;
import com.ded.macanclient.features.modules.player.FriendManager;
import com.ded.macanclient.settings.BooleanSetting;
import com.ded.macanclient.settings.ModeSetting;
import com.ded.macanclient.settings.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Rotation extends Module {
    private final BooleanSetting body = new BooleanSetting("Render Body", true, this);
    private final BooleanSetting realistic = new BooleanSetting("Realistic", true, this, body::getValue);
    private final BooleanSetting fixAim = new BooleanSetting("Fix Aim", true, this);
    private final BooleanSetting aimPoint = new BooleanSetting("Aim Point", false, this, fixAim::getValue);
    private final SliderSetting dotSize = new SliderSetting("Size", 0.1f, 0.05f, 0.2f, 0.05f, this, () -> aimPoint.getValue());

    private double x, y, z = 0;
    private double prevX, prevY, prevZ = 0;

    private final ModeSetting renderMode = new ModeSetting("Render Mode", new String[]{"All Players", "Only Me", "Only Friends"}, "All Players", this);

    public Rotation(String name, Category category) {
        super(name, category);
        settings.add(body);
        settings.add(realistic);
        settings.add(fixAim);
        settings.add(aimPoint);
        settings.add(dotSize);
        settings.add(renderMode);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("Rotation enabled");
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        System.out.println("Rotation disabled");
    }

    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        if (aimPoint.getValue() && shouldRender()) {
            double distance = Minecraft.getMinecraft().objectMouseOver.hitVec.distanceTo(Minecraft.getMinecraft().player.getPositionEyes(event.getPartialTicks()));
            Vec3d vec31 = Minecraft.getMinecraft().player.getLook(event.getPartialTicks());
            Vec3d vec32 = Minecraft.getMinecraft().player.getPositionEyes(event.getPartialTicks()).add(vec31.x * distance, vec31.y * distance, vec31.z * distance);

            prevX = x;
            prevY = y;
            prevZ = z;

            x = vec32.x;
            y = vec32.y;
            z = vec32.z;

            Vec3d vec = new Vec3d(MathUtils.interpolate(prevX, x, event.getPartialTicks()), MathUtils.interpolate(prevY, y, event.getPartialTicks()), MathUtils.interpolate(prevZ, z, event.getPartialTicks()));
            double d = dotSize.getValue() / 2;
            AxisAlignedBB target = new AxisAlignedBB(vec.x - d, vec.y - d, vec.z - d, vec.x + d, vec.y + d, vec.z + d);
            double playerX = Minecraft.getMinecraft().player.prevPosX + (Minecraft.getMinecraft().player.posX - Minecraft.getMinecraft().player.prevPosX) * event.getPartialTicks();
            double playerY = Minecraft.getMinecraft().player.prevPosY + (Minecraft.getMinecraft().player.posY - Minecraft.getMinecraft().player.prevPosY) * event.getPartialTicks();
            double playerZ = Minecraft.getMinecraft().player.prevPosZ + (Minecraft.getMinecraft().player.posZ - Minecraft.getMinecraft().player.prevPosZ) * event.getPartialTicks();
            AxisAlignedBB axis = new AxisAlignedBB(target.minX - playerX, target.minY - playerY, target.minZ - playerZ, target.maxX - playerX, target.maxY - playerY, target.maxZ - playerZ);
            // Адаптируй drawAxisAlignedBB
            // RenderUtils.drawAxisAlignedBB(axis, true, 0xFF00FF00);
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