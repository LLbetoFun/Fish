package com.fun.network.packets;

import com.fun.client.FunGhostClient;
import com.fun.client.mods.RegisterManager;
import com.fun.client.settings.SettingsManager;
import com.fun.inject.Bootstrap;
import com.fun.inject.InjectorUtils;
import com.fun.inject.Main;
import com.fun.inject.MinecraftType;
import com.fun.inject.mapper.Mapper;
import com.fun.network.IPacket;

import java.io.File;

public class PacketInit implements IPacket{


    public PacketInit() {
        super();

    }

    @Override
    public void process() {
        if(!Bootstrap.isAgent) {
            File injection = Mapper.mapJar(new File(new File(Main.path), "/injections/" + Bootstrap.minecraftVersion.injection), MinecraftType.NONE);
            //System.out.println("injection: " + injection.getAbsolutePath());
            InjectorUtils.addToSystemClassLoaderSearch(injection.getAbsolutePath());
            FunGhostClient.registerManager = new RegisterManager();
            FunGhostClient.settingsManager = new SettingsManager();
        }


    }

}
