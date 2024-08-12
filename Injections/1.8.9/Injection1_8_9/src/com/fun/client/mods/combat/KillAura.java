package com.fun.client.mods.combat;


import com.fun.client.FunGhostClient;
import com.fun.client.mods.Category;
import com.fun.client.mods.VModule;
import com.fun.client.settings.Setting;
import com.fun.client.utils.Rotation.Rotation;
import com.fun.eventapi.event.events.EventAttackReach;
import com.fun.eventapi.event.events.EventStrafe;
import com.fun.eventapi.event.events.EventUpdate;
import com.fun.inject.injection.wrapper.impl.entity.EntityWrapper;
import com.fun.utils.rotation.RotationUtils;
import net.minecraft.entity.Entity;

import javax.vecmath.Vector2f;

public class KillAura extends VModule
{
    public KillAura() {
        super("KillAura",Category.Combat);
    }
    public Vector2f targetRotation=new Vector2f();
    public Vector2f currentRotation=new Vector2f();
    public Setting rotationSpeed=new Setting("RotationSpeed",this,90,0,180,false);
    public Setting rotationSpeed2=new Setting("RotationSpeed2",this,90,0,180,false);

    public Setting slientMove=new Setting("SlientMove",this,true);
    public Setting keepSprint=new Setting("KeepSprint",this,false);
    public Setting CPS = new Setting("CPS", this, 12, 0, 20, true);
    public Setting rangeMin = new Setting("RangeMin", this, 3.0, 3.0, 6.0, false);
    public Setting rangeMax = new Setting("RangeMax", this, 6.0, 3.0, 6.0, false);

    @Override
    public void onAttackReach(EventAttackReach event) {
        super.onAttackReach(event);
        event.reach= rangeMax.getValDouble()+(rangeMin.getValDouble()-rangeMax.getValDouble())*Math.random();
    }

    @Override
    public void onUpdate(EventUpdate event) {
        super.onUpdate(event);
        if(FunGhostClient.registerManager.vModuleManager.scaffold.isRunning())return;
        EntityWrapper target= FunGhostClient.registerManager.vModuleManager.target.target;
        double speed=rotationSpeed.getValDouble()+(rotationSpeed2.getValDouble()-rotationSpeed.getValDouble())*Math.random();
        if(target!=null){

            targetRotation=AimBot.aim(mc.thePlayer.getPositionEyes(1),target.get().getPositionEyes(1));
        }
        else {
            targetRotation=new Vector2f(mc.thePlayer.rotationPitch,mc.thePlayer.rotationYaw);
            speed=180;
        }
        currentRotation= RotationUtils.limitAngleChange(new Rotation(currentRotation),new Rotation(targetRotation), (float) speed).toVec2f();

        FunGhostClient.rotationManager.setRation(currentRotation);
        if(mc.objectMouseOver != null&&target!=null&&(mc.objectMouseOver).entityHit==target.get()&&Math.random() <CPS.getValDouble()/20){
            mc.playerController.attackEntity(mc.thePlayer,mc.objectMouseOver.entityHit);
            mc.thePlayer.swingItem();
        }
        if(!keepSprint.getValBoolean())mc.thePlayer.setSprinting(false);
    }

    @Override
    public void onStrafe(EventStrafe event) {
        super.onStrafe(event);
        if(FunGhostClient.registerManager.vModuleManager.scaffold.isRunning())return;
        if(!slientMove.getValBoolean()){
            event.yaw=FunGhostClient.rotationManager.getRation().y;
            event.strafe=mc.thePlayer.movementInput.moveStrafe*0.98f;
            event.forward=mc.thePlayer.movementInput.moveForward*0.98f;
        }
    }
}
