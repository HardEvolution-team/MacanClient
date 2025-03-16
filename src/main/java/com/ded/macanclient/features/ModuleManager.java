package com.ded.macanclient.features;

import com.ded.macanclient.features.modules.combat.KillAuraModule;
import com.ded.macanclient.features.modules.player.FriendManager;
import com.ded.macanclient.gui.GradientConfigModule;
import com.ded.macanclient.features.modules.visual.Rotation;
import com.ded.macanclient.render.Shaders;
import com.ded.macanclient.features.modules.visual.Animations;
import com.ded.macanclient.features.modules.visual.Atmosphere;
import com.ded.macanclient.features.modules.visual.ChinaHatModule;
import com.ded.macanclient.features.modules.visual.JumpCircles;
import com.ded.macanclient.modules.*;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер для регистрации и управления модулями.
 */
public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // Ручная регистрация модулей
        registerModule(new GradientConfigModule("Gradient Config", Category.VISUALS));
        modules.add(new KillAuraModule("KillAura", Category.COMBAT));
        modules.add(new FriendManager("FriendManager", Category.MISCELLANEOUS));
        modules.add(new JumpCircles("JumpCircles", Category.VISUALS));
        modules.add(new Animations("Animations", Category.VISUALS));
        //modules.add(new DamageParticles("DamageParticles", Category.VISUALS));
        modules.add(new Atmosphere("Atmosphere", Category.VISUALS));
        modules.add(new Shaders("Shaders", Category.VISUALS));
        modules.add(new Rotation("Rotation", Category.VISUALS));
        modules.add(new ChinaHatModule("ChinaHat", Category.VISUALS));

    }

    private void registerModule(Module module) {
        modules.add(module);
        MinecraftForge.EVENT_BUS.register(module);
    }

    public static List<Module> getModules() {
        return modules;
    }

    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
