package com.fun.client.utils.Rotation;


import com.fun.utils.math.MathHelper;
import com.fun.inject.injection.wrapper.impl.MinecraftWrapper;
import javax.vecmath.Vector2f;

public class Rotation {
    public static MinecraftWrapper mc=MinecraftWrapper.get();
    public double yaw,pitch;
    public boolean tag=false;
    public Rotation(double yawIn,double pitchIn){
        this.yaw= yawIn>180||yawIn<180? MathHelper.wrapAngleTo180_double(yawIn):yawIn;
        this.pitch=pitchIn>90||pitchIn<-90?MathHelper.wrapAngleTo180_double(pitchIn):pitchIn;
    }

    @Override
    public String toString() {
        return "pitch:"+pitch+" yaw:"+yaw;
    }

    public Rotation(Vector2f v){
        this(v.y,v.x);
    }


    public Vector2f toVec2f(){
        return new Vector2f((float) pitch, (float) yaw);
    }


    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)||(obj instanceof Rotation&&((Rotation) obj).toVec2f().equals(this.toVec2f()))||(obj instanceof Vector2f&&obj.equals(this.toVec2f()));
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }
    public static Rotation player(){
        return new Rotation(mc.getPlayer().getYaw(),mc.getPlayer().getPitch());
    }
}
