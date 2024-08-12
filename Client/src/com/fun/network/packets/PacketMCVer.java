package com.fun.network.packets;

import com.fun.inject.In9ectManager;
import com.fun.inject.MinecraftVersion;
import com.fun.network.IPacket;
import com.fun.network.IReviewable;

import static com.fun.network.TCPServer.getVersion;

public class PacketMCVer implements IPacket, IReviewable {
    public String ver;

    public PacketMCVer(String ver) {
        super();
        this.ver=ver;
    }

    @Override
    public void process() {
        In9ectManager.minecraftVersion= getVersion();
        ver= String.valueOf(In9ectManager.minecraftVersion.ordinal());
        //System.out.println(ver);
    }

    @Override
    public void review() {
        In9ectManager.minecraftVersion= MinecraftVersion.values()[Integer.parseInt(ver)];
    }


}
