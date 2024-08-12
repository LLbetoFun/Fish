package com.fun.client.mods.movement;

import com.fun.client.settings.Setting;
import com.fun.eventapi.event.events.EventUpdate;
import org.lwjgl.input.Keyboard;
import com.fun.client.mods.Category;
import com.fun.client.mods.Module;

import java.util.Random;

public class KeepSprint extends Module {
    public Setting changeChanceSetting;
    public KeepSprint(String nameIn) {
        super(Keyboard.KEY_R, nameIn, Category.Movement);
        this.changeChanceSetting = new Setting("Chance", this, 50.0, 0.0, 100.0, false);
    }
    @Override
    public void onUpdate(EventUpdate event) {
        super.onUpdate(event);
        // 检查sprint
        if (!mc.getPlayer().isSprinting() && mc.getPlayer().isMoving()) {
            if (shouldChangeSprint()) {
                mc.getPlayer().setSprinting(true);
            }
        }
    }
    private boolean shouldChangeSprint() {
        Random random = new Random();
        double chance = changeChanceSetting.getValDouble();
        return random.nextDouble() * 100 < chance;
    }
}