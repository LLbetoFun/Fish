package com.fun.utils.version.runable;

import com.fun.inject.Bootstrap;
import com.fun.inject.MinecraftType;
import com.fun.inject.MinecraftVersion;
import com.fun.utils.version.Version;

public abstract class VRunable extends Version implements Runnable{
    @Override
    public VRunable version(MinecraftVersion version, MinecraftType type) {
        return (VRunable) super.version(version, type);
    }

    @Override
    public VRunable version(MinecraftVersion version) {
        return (VRunable) super.version(version);
    }

    @Override
    public VRunable version() {
        return (VRunable) super.version();
    }
    public static void runV(VRunable... vrs){
        for(VRunable vr:vrs){
            if(vr.minecraftType== Bootstrap.minecraftType&&
            vr.minecraftVersion== Bootstrap.minecraftVersion){
                vr.run();
                return;
            }
        }
        for(VRunable vr:vrs){
            if(vr.minecraftVersion== Bootstrap.minecraftVersion){
                vr.run();
                return;
            }
        }
        vrs[0].run();
    }
}
