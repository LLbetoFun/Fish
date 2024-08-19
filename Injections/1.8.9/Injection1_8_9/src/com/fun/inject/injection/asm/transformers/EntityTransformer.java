package com.fun.inject.injection.asm.transformers;

import com.fun.client.FunGhostClient;
import com.fun.eventapi.EventManager;
import com.fun.eventapi.event.events.EventStrafe;
import com.fun.inject.Bootstrap;
import com.fun.inject.injection.asm.api.Inject;
import com.fun.inject.injection.asm.api.Mixin;
import com.fun.inject.injection.asm.api.Transformer;
import com.fun.inject.mapper.Mapper;
import com.fun.utils.version.clazz.Classes;
import com.fun.utils.version.methods.Methods;
import com.fun.inject.Mappings;
import com.fun.inject.MinecraftVersion;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public class EntityTransformer extends Transformer {
    public EntityTransformer() {
        super(Classes.Entity);
    }
    @Mixin(method = Methods.moveFlying_Entity)
    public void onMoveFly(MethodNode methodNode) {
        final int ASTRAFE = Bootstrap.minecraftVersion== MinecraftVersion.VER_189||
                Bootstrap.minecraftVersion== MinecraftVersion.VER_1710?0:1;
        InsnList list = new InsnList();
        //Agent.System.out.println("moveFlying");
        int j =0;
        list.add(new VarInsnNode(Opcodes.ALOAD,0));

        list.add(new VarInsnNode(Opcodes.FLOAD,1));
        list.add(new VarInsnNode(Opcodes.FLOAD,2+ASTRAFE));
        list.add(new VarInsnNode(Opcodes.FLOAD,3+ASTRAFE));

        list.add(new VarInsnNode(Opcodes.ALOAD,0));
        //FD: pk/s net/minecraft/entity/Entity/field_70165_t
        //FD: pk/t net/minecraft/entity/Entity/field_70163_u
        //FD: pk/u net/minecraft/entity/Entity/field_70161_v
        //FD: pk/y net/minecraft/entity/Entity/field_70177_z
        //FD: pk/z net/minecraft/entity/Entity/field_70125_A
        list.add(new FieldInsnNode(Opcodes.GETFIELD,Mappings.getObfClass("net/minecraft/entity/Entity"), Mappings.getObfField("field_70177_z"),"F"));
        //event参数

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(EntityTransformer.class),"onStrafe","(Ljava/lang/Object;FFFF)Lcom/fun/eventapi/event/events/EventStrafe;"));
        list.add(new VarInsnNode(Opcodes.ASTORE,4+ASTRAFE));
        j++;
        list.add(new VarInsnNode(Opcodes.ALOAD,4+ASTRAFE));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,"com/fun/eventapi/event/events/EventStrafe","strafe","F"));

        list.add(new VarInsnNode(Opcodes.FSTORE,1));

        list.add(new VarInsnNode(Opcodes.ALOAD,4+ASTRAFE));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,"com/fun/eventapi/event/events/EventStrafe","forward","F"));

        list.add(new VarInsnNode(Opcodes.FSTORE,2+ASTRAFE));

        list.add(new VarInsnNode(Opcodes.ALOAD,4+ASTRAFE));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,"com/fun/eventapi/event/events/EventStrafe","friction","F"));
        list.add(new VarInsnNode(Opcodes.FSTORE,3+ASTRAFE));

        ArrayList<AbstractInsnNode> rl=new ArrayList<>();
        //ArrayList<AbstractInsnNode> rload=new ArrayList<>();
        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode node = methodNode.instructions.get(i);
            if(node instanceof VarInsnNode&&((VarInsnNode) node).var>=3+ASTRAFE+j){
                ((VarInsnNode) node).var+=j;//插入偏移值;
            }
            if(node instanceof FieldInsnNode&&((FieldInsnNode) node).name.equals(Mappings.getObfField("field_70177_z"))){
                 //标记替换yaw轴
                AbstractInsnNode aload_0 = methodNode.instructions.get(i-1);
                if(aload_0 instanceof VarInsnNode){
                    ((VarInsnNode) aload_0).var=4+ASTRAFE;
                    rl.add(node);
                }

            }

        }
        methodNode.instructions.insert(list);
        int bound = rl.size();
        for (int x = 0; x < bound; x++) {
                AbstractInsnNode node = rl.get(x);
                methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, "com/fun/eventapi/event/events/EventStrafe", "yaw", "F"));
                methodNode.instructions.remove(node);
        }





    }
    /*
    public void moveRelative(float strafe, float up, float forward, float friction)
    {
        float f = strafe * strafe + up * up + forward * forward;

        if (f >= 1.0E-4F)
        {
            f = MathHelper.sqrt(f);

            if (f < 1.0F)
            {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            up = up * f;
            forward = forward * f;
            float f1 = MathHelper.sin(this.rotationYaw * 0.017453292F);
            float f2 = MathHelper.cos(this.rotationYaw * 0.017453292F);
            this.motionX += (double)(strafe * f2 - forward * f1);
            this.motionY += (double)up;
            this.motionZ += (double)(forward * f2 + strafe * f1);
        }
    }

     */
    public static EventStrafe onStrafe(Object entity,float f0, float f1, float f2, float f){
        //System.out.println("onStrafe1");
        //System.out.println("onStrafe");
        EventStrafe eventStrafe=new EventStrafe(f1,f0,f,f2);
        if(entity.getClass().getName().equals(Mappings.getObfClass("net/minecraft/client/entity/EntityPlayerSP").replace('/','.'))) EventManager.call(eventStrafe);
        return eventStrafe;
    }
    @Inject(method = "getLook",descriptor = "(F)Lnet/minecraft/util/Vec3;")
    public void getLookAngle(MethodNode methodNode){
        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode node = methodNode.instructions.get(i);
            if(node instanceof FieldInsnNode){
                if(((FieldInsnNode) node).name.equals(Mapper.getObfField("rotationYaw","net/minecraft/entity/Entity"))
                        ||((FieldInsnNode) node).name.equals(Mapper.getObfField("prevRotationYaw","net/minecraft/entity/Entity"))
                        &&((FieldInsnNode) node).desc.equals("F")){
                    methodNode.instructions.insertBefore(node,new VarInsnNode(Opcodes.FLOAD,1));
                    methodNode.instructions.insertBefore(node,new MethodInsnNode(Opcodes.INVOKESTATIC,Type.getInternalName(EntityTransformer.class),"yaw","(Ljava/lang/Object;F)F"));
                    methodNode.instructions.remove(node);
                }
                if(((FieldInsnNode) node).name.equals(Mapper.getObfField("prevRotationPitch","net/minecraft/entity/Entity"))
                        ||((FieldInsnNode) node).name.equals(Mapper.getObfField("rotationPitch","net/minecraft/entity/Entity"))
                        &&((FieldInsnNode) node).desc.equals("F")){
                    methodNode.instructions.insertBefore(node,new VarInsnNode(Opcodes.FLOAD,1));
                    methodNode.instructions.insertBefore(node,new MethodInsnNode(Opcodes.INVOKESTATIC,Type.getInternalName(EntityTransformer.class),"pitch","(Ljava/lang/Object;F)F"));
                    methodNode.instructions.remove(node);
                }
            }
        }
    }//getLook (F)Lnet/minecraft/util/Vec3;
    public static float yaw(Object entity,float f){
        if(entity instanceof EntityPlayerSP)
            return FunGhostClient.rotationManager.getRation().y;
        else if(entity instanceof Entity) return ((Entity) entity).rotationYaw;
        throw new RuntimeException("arg0 isn't Entity");
    }
    public static float pitch(Object entity,float f){//getViewVector

        if(entity instanceof EntityPlayerSP)
            return FunGhostClient.rotationManager.getRation().x;
        else if(entity instanceof Entity) return ((Entity) entity).rotationPitch;
        throw new RuntimeException("arg0 isn't Entity");
    }
}
