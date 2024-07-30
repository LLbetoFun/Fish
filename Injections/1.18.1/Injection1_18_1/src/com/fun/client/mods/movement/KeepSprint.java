package com.fun.client.mods.movement;

import com.fun.client.mods.Category;
import com.fun.client.mods.Module;
import com.fun.client.mods.VModule;
import com.fun.eventapi.event.events.EventUpdate;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class KeepSprint extends VModule {
    public KeepSprint() {
        super(InputConstants.KEY_F, "KeepSprint", Category.Movement);
    }
}

