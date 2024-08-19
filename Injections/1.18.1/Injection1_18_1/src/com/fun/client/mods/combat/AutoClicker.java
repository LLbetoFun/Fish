package com.fun.client.mods.combat;

import com.fun.client.mods.Category;
import com.fun.client.mods.VModule;
import com.fun.client.settings.Setting;
import com.fun.eventapi.event.events.EventUpdate;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import static com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT;
import static com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT;

public class AutoClicker extends VModule {
    public AutoClicker() {
        super("AutoClicker", Category.Combat);
    }

    public Setting leftCPS = new Setting("LeftCPS", this, 12, 0, 20, true);
    public Setting rightCPS = new Setting("RightCPS", this, 12, 0, 20, true);
    public Setting leftEnabled = new Setting("LeftClick", this, true);
    public Setting rightEnabled = new Setting("RightClick", this, true);

    @Override
    public void onUpdate(EventUpdate event) {
        super.onUpdate(event);
        handleClick(leftEnabled, leftCPS, InputConstants.MOUSE_BUTTON_LEFT);
        handleClick(rightEnabled, rightCPS, InputConstants.MOUSE_BUTTON_RIGHT);
    }

    private void handleClick(Setting enabled, Setting cps, int button) {
        if (enabled.getValBoolean() && isButtonPressed(button)) {
            if (Math.random() < cps.getValDouble() / 20 && mc.screen == null && mc.level != null) {
                sendClick(button);
            }
        }
    }

    private boolean isButtonPressed(int button) {
        return (button == MOUSE_BUTTON_LEFT && mc.mouseHandler.isLeftPressed()) ||
                (button == MOUSE_BUTTON_RIGHT && mc.mouseHandler.isRightPressed());
    }

    public void sendClick(int button) {
        KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(button));
    }
}

