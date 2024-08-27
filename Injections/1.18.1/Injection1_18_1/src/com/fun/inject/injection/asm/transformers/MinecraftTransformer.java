package com.fun.inject.injection.asm.transformers;

import com.fun.eventapi.EventManager;
import com.fun.eventapi.event.events.EventTick;
import com.fun.eventapi.event.events.EventView;
import com.fun.inject.injection.asm.api.Inject;
import com.fun.inject.injection.asm.api.Transformer;
import org.objectweb.asm.Type;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class MinecraftTransformer extends Transformer {
    public MinecraftTransformer() {
        super("net/minecraft/client/Minecraft");
    }
    @Inject(method = "runTick",descriptor = "(Z)V")//Minecraft/runTick (Z)V
    public void runTick(MethodNode methodNode){
        methodNode.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(MinecraftTransformer.class),"onRunTick","()V"));
    }
    public static void onRunTick(){
        EventManager.call(new EventTick());
    }
    @Inject(method = "getCameraEntity",descriptor = "()Lnet/minecraft/world/entity/Entity;")
    public void getRenderViewEntity(MethodNode methodNode){
        methodNode.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(MinecraftTransformer.class),"onGetRenderViewEntity","()V"));
    }
    //getInstance ()Lnet/minecraft/client/Minecraft;
    @Inject(method = "getInstance",descriptor = "()Lnet/minecraft/client/Minecraft;")
    public void getInstance(MethodNode methodNode){
        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode a = methodNode.instructions.get(i);
            System.out.println(a.getClass().getName()+" "+a.getOpcode());
        }
        /*InsnList list = new InsnList();
        Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        int i=0;
        while(iterator.hasNext()){
            AbstractInsnNode a=iterator.next();
            i++;
            if(!(a instanceof FieldInsnNode)&&!(a instanceof InsnNode)&&!(a instanceof LabelNode)&&!(a instanceof LineNumberNode)&&!(a instanceof VarInsnNode&&((VarInsnNode) a).var==0))iterator.remove();
        }*/




    }
    public static void onGetRenderViewEntity(){
        EventManager.call(new EventView());
    }
}
