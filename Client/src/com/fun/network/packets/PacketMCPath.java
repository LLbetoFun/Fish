package com.fun.network.packets;

import com.fun.gui.FGui;
import com.fun.gui.OldFrame;
import com.fun.inject.Main;
import com.fun.network.IPacket;
import sun.misc.Unsafe;

public class PacketMCPath implements IPacket
{
    public String path;

    public PacketMCPath(String path) {
        this.path = path;
    }

    @Override
    public void process() {
        try {
            Main.mcPath = path;
            Main.started = true;

            //FunGhostClient.init();

            //ConfigModule.loadConfig();

            //FGui.init();//
            OldFrame.init0();

            //System.out.println("fishgc Started");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
