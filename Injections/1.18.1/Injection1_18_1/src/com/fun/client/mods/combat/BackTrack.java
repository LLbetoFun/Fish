package com.fun.client.mods.combat;

import com.fun.client.mods.Category;
import com.fun.client.mods.VModule;
import com.fun.client.settings.Setting;
import com.fun.eventapi.event.events.EventPacket;
import com.fun.eventapi.event.events.EventRender2D;
import com.fun.eventapi.event.events.EventRender3D;
import com.fun.eventapi.event.events.EventUpdate;
import com.fun.inject.mapper.Mapper;
import com.fun.inject.utils.ReflectionUtils;
import com.fun.utils.RenderManager;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class BackTrack extends VModule {

    private final Setting mode = new Setting("Mode",this, "Tick" ,new String[]{"Tick"});
    private final Setting amount = new Setting("Amount", this,1.0, 1.0, 3.0, false);
    private final Setting range = new Setting("Range",this, 3.0, 1.0, 5.0, false);
    private final Setting interval = new Setting("interval tick",this, 1, 0, 10, true);
    private Entity target;
    private Vec3 realTargetPosition = new Vec3(0.0D, 0.0D, 0.0D);
    int tick = 0;

    public BackTrack() {
        super("BackTrack", Category.Combat);
    }

    public void onDisable() {
        target = null;
        tick = 0;
    }


    @Override
    public void onUpdate(EventUpdate event) {
        super.onUpdate(event);
        if (this.tick <= this.interval.getValDouble())
            this.tick++;
        if(target!=null){
            System.out.println("realPosition: " + realTargetPosition);
            System.out.println("fakePosition: " + target.position());
        }
        if (target != null
                && mc.player.distanceTo(target) <= this.range.getValDouble()
                && (new Vec3(target.getX(), target.getY(), target.getZ())).distanceTo(this.realTargetPosition) < this.amount.getValDouble()
                && this.tick > this.interval.getValDouble()) {
            target.setPosRaw(target.xo,target.yo,target.zo);
            System.out.println("tick: " + tick);
            tick = 0;
        }
    }

    @Override
    public void onRender3D(EventRender3D event) {
        super.onRender3D(event);
        if(target!=null){
            
        }
    }

    @Override
    public void onPacket(EventPacket e) {
        if (e.packet instanceof ClientboundTeleportEntityPacket) {//S18PacketEntityTeleport
            ClientboundTeleportEntityPacket s18 = (ClientboundTeleportEntityPacket) e.packet;
            if (target != null && s18.getId() == target.getId()) {
                realTargetPosition = new Vec3(s18.getX(), s18.getY(), s18.getZ());
               // System.out.println("moveEntity");
            }
        }
        if(e.packet instanceof ClientboundMoveEntityPacket){
            ClientboundMoveEntityPacket s14 = (ClientboundMoveEntityPacket) e.packet;
            int id = (int) ReflectionUtils.getFieldValue(e.packet, Mapper.getObfField("entityId","net/minecraft/network/protocol/game/ClientboundMoveEntityPacket"));

            if (target != null && id == target.getId()) {
                realTargetPosition=realTargetPosition.add(s14.getXa()/4096d,s14.getYa()/4096d,s14.getZa()/4096d);
                        //new Vec3(s14. / 32.0D, s14.getY() / 32.0D, s14.getZ() / 32.0D);
                ///System.out.println("moveEntity"+"xa: "+s14.getXa()+"ya: "+s14.getYa()+"za: "+s14.getZa());
            }
        }
        if(e.packet instanceof ServerboundInteractPacket){
            int id = (int) ReflectionUtils.getFieldValue(e.packet, Mapper.getObfField("entityId","net/minecraft/network/protocol/game/ServerboundInteractPacket"));
            target=mc.level.getEntity(id);
            realTargetPosition=new Vec3(0,0,0);
            //System.out.println("target entity: "+target);
        }
    }
}
