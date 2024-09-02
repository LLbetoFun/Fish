package com.fun.inject.injection.asm.transformers;


import com.fun.eventapi.EventManager;
import com.fun.eventapi.event.events.EventAttackReach;
import com.fun.eventapi.event.events.EventBlockReach;
import com.fun.client.FunGhostClient;
import com.fun.eventapi.event.events.EventRender3D;
import com.fun.inject.injection.asm.api.Inject;
import com.fun.inject.injection.asm.api.Transformer;
import com.fun.inject.Mappings;
import com.fun.inject.mapper.Mapper;
import com.fun.utils.RenderManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;


public class EntityRendererTransformer extends Transformer {
    public EntityRendererTransformer() {
        super("net/minecraft/client/renderer/GameRenderer");
    }

    @Inject(method = "renderLevel", descriptor = "(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void renderWorldPass(MethodNode methodNode) {
        AbstractInsnNode ldcNode = null;
        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode a = methodNode.instructions.get(i);
            if (a instanceof MethodInsnNode) {
                MethodInsnNode m = (MethodInsnNode) a;
                if (m.owner.equals(Mappings.getObfClass("net/minecraft/util/profiling/ProfilerFiller"))
                        && m.name.equals(Mappings.getObfMethod("m_6182_"))) { // endStartSection
                    ldcNode = a;
                }
            }
        }


        InsnList list = new InsnList();

        list.add(new VarInsnNode(FLOAD, 1));
        list.add(new VarInsnNode(ALOAD, 4));
        list.add(new MethodInsnNode(INVOKESTATIC,Type.getInternalName(EntityRendererTransformer.class),"onRender3D", Mapper.getObfMethodDesc("(FLjava/lang/Object;)V")));

        methodNode.instructions.insert(ldcNode, list);



    }

    @Inject(method = "pick", descriptor = "(F)V")
    public void getMouseOver(MethodNode methodNode) {

        InsnList list = new InsnList();
        LdcInsnNode ldc = null;
        MethodInsnNode min = null;
        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode x = methodNode.instructions.get(i);
            if(x instanceof MethodInsnNode){//func_78757_d,getBlockReachDistance,0,player reach distance = 4F
                if(Mappings.getObfMethod("m_105286_").equals(((MethodInsnNode) x).name)&&
                ((MethodInsnNode) x).owner.equals(Mappings.getObfClass("net/minecraft/client/multiplayer/MultiPlayerGameMode")))
                    min= (MethodInsnNode) x;
            }
            if (x instanceof LdcInsnNode) {
                LdcInsnNode t = (LdcInsnNode) x;

                if (t.cst instanceof Double && ((Double) t.cst) == 3.0) {
                    ldc = t;
                }

            }
        }

        if (ldc == null) return;
        methodNode.instructions.insert(min,new MethodInsnNode(INVOKESTATIC, Type.getInternalName(EntityRendererTransformer.class), "onBlockReach", "(F)F"));
        //methodNode.instructions.remove(min);
        methodNode.instructions.insert(ldc, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(EntityRendererTransformer.class), "onAttackReach", "()D"));
//        methodNode.instructions.insert(ldc, new VarInsnNode(ALOAD, 23));
        methodNode.instructions.remove(ldc);


    }

    public static double onAttackReach(){

        EventAttackReach e=new EventAttackReach(3.0d);
        EventManager.call(e);
        return e.reach;
    }
    public static float onBlockReach(float f){

        EventBlockReach e=new EventBlockReach(f);
        EventManager.call(e);
        return (float) e.reach;
    }
    public static void onRender3D(float f, Object pose){
        RenderManager.currentPoseStack= (PoseStack) pose;
        EventManager.call(new EventRender3D((f)));

    }
}
