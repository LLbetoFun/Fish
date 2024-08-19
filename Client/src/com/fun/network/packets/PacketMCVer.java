package com.fun.network.packets;

import com.fun.inject.Bootstrap;
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
        Bootstrap.minecraftVersion= getVersion();
        ver= String.valueOf(Bootstrap.minecraftVersion.ordinal());
        //System.out.println(ver);
    }

    @Override
    public void review() {
        Bootstrap.minecraftVersion= MinecraftVersion.values()[Integer.parseInt(ver)];
    }


}
