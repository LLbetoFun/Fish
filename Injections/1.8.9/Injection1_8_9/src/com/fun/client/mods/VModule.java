package com.fun.client.mods;

import com.fun.inject.Bootstrap;
import com.fun.inject.mapper.SideOnly;
import net.minecraft.client.Minecraft;

public class VModule extends Module {
    public Minecraft mc;
    public VModule(String nameIn, Category category) {
        super(nameIn, category);
        if(Bootstrap.isAgent)mc = Minecraft.getMinecraft();

    }
    @SideOnly(SideOnly.Type.AGENT)
    public void setup(){
    }

    public VModule(int keyIn, String nameIn, Category categoryIn) {
        super(keyIn, nameIn, categoryIn);
    }
}
