package com.fun.client.mods;

import com.fun.inject.injection.wrapper.impl.MinecraftWrapper;
import net.minecraft.client.Minecraft;

public class VModule extends Module {
    public Minecraft mc= MinecraftWrapper.getInstance();;
    public VModule(String nameIn, Category category) {
        super(nameIn, category);
    }

    public VModule(int keyIn, String nameIn, Category categoryIn) {
        super(keyIn, nameIn, categoryIn);
    }
}
