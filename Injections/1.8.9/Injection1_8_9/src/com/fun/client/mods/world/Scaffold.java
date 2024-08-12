package com.fun.client.mods.world;

import com.fun.client.FunGhostClient;
import com.fun.client.mods.Category;
import com.fun.client.mods.VModule;
import com.fun.client.settings.Setting;
import com.fun.client.utils.Rotation.Rotation;
import com.fun.eventapi.event.events.EventStrafe;
import com.fun.eventapi.event.events.EventUpdate;
import com.fun.utils.math.MathHelper;
import com.fun.utils.rotation.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

public class Scaffold extends VModule {
    public Scaffold() {
        super("Scaffold", Category.World);
    }

    public Setting sameY = new Setting("SameY", this, false);
    public Setting searchRotYDiff = new Setting("SearchRotYDiff", this, 90, 0, 180, true);
    public Setting searchRotXDiff = new Setting("SearchRotXDiff", this, 1, 0, 5, false);

    public Setting rotationSpeed = new Setting("RotationSpeed", this, 90, 0, 180, false);
    public Setting rotationSpeed2 = new Setting("RotationSpeed2", this, 90, 0, 180, false);
    public Setting keepSprint = new Setting("KeepSprint", this, false);
    public Setting telly = new Setting("Telly(Don't use it,may not bypass)", this, false);
    public Setting rayCast = new Setting("Ray Cast", this, false);
    public Setting rayCastMode = new Setting("RayCastMode", this, "Strict", new String[]{"Strict", "Static"}){
        @Override
        public boolean isVisible() {
            return rayCast.getValBoolean();
        }
    };

    @Override
    public void onStrafe(EventStrafe event) {
        super.onStrafe(event);
        event.strafe*= (float) (moveSpeed.getValDouble()/100f);
        event.forward*= (float) (moveSpeed.getValDouble()/100f);
    }

    public Setting moveSpeed = new Setting("MoveSpeed", this, 90, 0, 100, true);

    public PlaceInfo placeInfo;
    public Vector2f targetRotation=new Vector2f();
    public Vector2f currentRotation=new Vector2f();

    public static boolean isScaffoldBlock(ItemStack itemStack) {
        if (itemStack == null)
            return false;

        if (itemStack.stackSize <= 0)
            return false;

        if (!(itemStack.getItem() instanceof ItemBlock))
            return false;

        ItemBlock itemBlock = (ItemBlock) itemStack.getItem();

        // whitelist
        if (itemBlock.getBlock() == Blocks.glass)
            return true;

        // only fullblock
        return true;
    }
    public EnumFacing getSideHit(BlockPos currentPos, BlockPos sideBlock) {
        int xDiff = sideBlock.getX() - currentPos.getX();
        int yDiff = sideBlock.getY() - currentPos.getY();
        int zDiff = sideBlock.getZ() - currentPos.getZ();
        if(sameY.getValBoolean())yDiff=0;
        EnumFacing facing = yDiff <= -0.5 ? EnumFacing.UP : xDiff <= -1 ? EnumFacing.EAST : xDiff >= 1 ? EnumFacing.WEST : zDiff <= -1 ? EnumFacing.SOUTH : zDiff >= 1 ? EnumFacing.NORTH : yDiff >= 0.5 ?EnumFacing.DOWN:null;
        if (xDiff == 0 && yDiff == 0 && zDiff == 0) {
            return null;
        }
        return facing;
    }

    @Override
    public void onUpdate(EventUpdate event) {
        super.onUpdate(event);
        if(!keepSprint.getValBoolean())mc.thePlayer.setSprinting(false);

        placeInfo=finnPlaceInfo();

        double speed=rotationSpeed.getValDouble()+(rotationSpeed2.getValDouble()-rotationSpeed.getValDouble())*Math.random();

        if(placeInfo!=null){
            targetRotation=placeInfo.rotation;
        }
        if(telly.getValBoolean()&&!mc.thePlayer.onGround) targetRotation=new Vector2f(mc.thePlayer.rotationPitch,mc.thePlayer.rotationYaw);
        currentRotation= RotationUtils.limitAngleChange(new Rotation(currentRotation),new Rotation(targetRotation), (float) speed).toVec2f();
        FunGhostClient.rotationManager.setRation(currentRotation);


        int currentItem=getSlot();
        mc.thePlayer.inventory.currentItem=currentItem;
        MovingObjectPosition result=ray(currentRotation,5);
        if (placeInfo==null||rayCast.getValBoolean() && result == null || rayCast.getValBoolean() && result != null && !((MovingObjectPosition) result).getBlockPos().equals(placeInfo.hitResult.getBlockPos()) || rayCast.getValBoolean() && rayCastMode.getValString().equalsIgnoreCase("Strict")
                && result != null && ((MovingObjectPosition) result).sideHit != placeInfo.hitResult.sideHit)
            return;
        if (placeInfo != null && isScaffoldBlock(mc.thePlayer.inventory.getCurrentItem()) ) {

            if (mc.playerController.onPlayerRightClick(mc.thePlayer,mc.theWorld,
                    mc.thePlayer.inventory.getCurrentItem(), placeInfo.hitResult.getBlockPos(),placeInfo.hitResult.sideHit, placeInfo.hitResult.hitVec))
            {
                mc.thePlayer.swingItem();
            }
        }

    }

    @Override
    public void onEnable() {
        super.onEnable();
        if(mc.thePlayer!=null) startY=MathHelper.floor_double(mc.thePlayer.posX)+1;
        //FunGhostClient.registerManager.vModuleManager.notification.post(new Notification("Plz Put Blocks In MainHand", Notification.Type.WHITE));
    }
    public boolean cannotPut(BlockPos blockPos) {
        if (blockPos == null) {
            return false;
        }
        return getBlock(blockPos) != Blocks.air && !(getBlock(blockPos) instanceof BlockLiquid);
    }
    public int startY=0;
    public PlaceInfo finnPlaceInfo(){
        BlockPos thePlayer_pos = getPos(mc.thePlayer.getPositionVector());
        BlockPos hitPos=null;
        double offset=0.4;
        EnumFacing facing = getSideHit(thePlayer_pos.add(0, sameY.getValBoolean()?
                        startY-thePlayer_pos.getY():-offset, 0), getSideBlock(thePlayer_pos.add(0, sameY.getValBoolean()?startY-thePlayer_pos.getY():
                        -offset, 0)));

        hitPos = getSideBlock(thePlayer_pos.add(0, sameY.getValBoolean()?startY-thePlayer_pos.getY():-offset, 0));
        if(hitPos==null||facing==null)return null;
        float yaw=mc.thePlayer.rotationYaw;
        ArrayList<PlaceInfo> placeInfos=new ArrayList<>();
        for(int possibleYaw = (int) ((int) (yaw-180)); possibleYaw <yaw+180; possibleYaw += (int) searchRotYDiff.getValDouble()){
            for (float possiblePitch = 90; possiblePitch > 60; possiblePitch -= (float) searchRotXDiff.getValDouble()) {
                Vector2f rotation=new Vector2f(possiblePitch,possibleYaw);
                MovingObjectPosition result= ray(rotation,5);
               if(result != null){
                   if(((MovingObjectPosition) result).sideHit==facing&&((MovingObjectPosition) result).getBlockPos().equals(hitPos)){
                       placeInfos.add(new PlaceInfo(((MovingObjectPosition) result),rotation));
                   }
               }
            }
        }

        float minDiff=Float.MAX_VALUE;
        PlaceInfo current=null;
        for (PlaceInfo info:placeInfos){
            float diff= (float) Math.hypot(RotationUtils.getAngleDifference(info.rotation.x, FunGhostClient.rotationManager.getRation().x),
                    RotationUtils.getAngleDifference(info.rotation.y, FunGhostClient.rotationManager.getRation().y));

            if(diff<minDiff){
                minDiff=diff;
                current=info;
            }
        }
        return current;
    }
    public MovingObjectPosition ray(Vector2f v, double blockReachDistance){
        Vec3 v31=getVectorForRotation(v.x,v.y);
        Vec3 v1=mc.thePlayer.getPositionEyes(1.0f);
        Vec3 v2= v1.addVector( v31.xCoord * blockReachDistance, v31.yCoord * blockReachDistance, v31.zCoord * blockReachDistance);

        return mc.theWorld.rayTraceBlocks(v1,v2, false, false, true);
    }
    public final Vec3 getVectorForRotation(float pitch, float yaw)
    {
        float f = net.minecraft.util.MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = net.minecraft.util.MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -net.minecraft.util.MathHelper.cos(-pitch * 0.017453292F);
        float f3 = net.minecraft.util.MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }
    public BlockPos getPos(Vec3 v) {
        return new BlockPos(Math.floor(v.xCoord), Math.floor(v.yCoord), Math.floor(v.zCoord));
    }
    public BlockPos getSideBlock(BlockPos currentPos) {
        BlockPos pos = currentPos;
        if (getBlock(currentPos.add(0, -1, 0)) != Blocks.air && !(getBlock(currentPos.add(0, -1, 0)) instanceof BlockLiquid))
            return currentPos.add(0, -1, 0);
        double reach_max = 5;
        double reach_min = 0;
        int floor_reach = (int) (reach_max+0.5);
        double dist = 20;
        for (int x = -floor_reach; x <= floor_reach; x++) {
            for (int y = -floor_reach; y <= floor_reach; y++) {
                for (int z = -floor_reach; z <= floor_reach; z++) {
                    BlockPos newPos = currentPos.add(x, y, z);
                    double newDist = MathHelper.sqrt_double(x * x + y * y + z * z);
                    if (getBlock(newPos) != Blocks.air && !(getBlock(newPos) instanceof BlockLiquid)
                            && mc.theWorld.getBlockState(newPos).getBlock().getMaterial().isSolid() && newDist <= dist && newDist < reach_max && newDist > reach_min) {
                        pos = currentPos.add(x, y, z);
                        dist = newDist;
                    }
                }
            }
        }
        return pos;
    }
    public Block getBlock(BlockPos pos) {
        //MCP
        return mc.theWorld.getBlockState(pos).getBlock();
    }
    public static class PlaceInfo{
        public MovingObjectPosition hitResult;

        public Vector2f rotation;

        public PlaceInfo(MovingObjectPosition result,Vector2f rotation){
            this.hitResult=result;
            this.rotation=rotation;
        }
    }
    public int getSlot() {
        for (int i = 0; i <= 8; i++) {
            if (isScaffoldBlock(mc.thePlayer.inventory.mainInventory[i])) {
                return i;
            }
        }
        return mc.thePlayer.inventory.currentItem;
    }


}
