package com.fun.inject.define;

import com.fun.hook.Printer;
import com.fun.inject.Bootstrap;
import com.fun.inject.NativeUtils;
import com.fun.inject.injection.asm.api.Transformers;
import com.fun.inject.utils.ReflectionUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public class Definer {
    public static ArrayList<String> definedClasses = new ArrayList<>();
    public static void defineClass(String className){
        if(className==null) return;
        className=className.replace('/', '.');
        byte[] bytes = Bootstrap.classes.get(className);
        ClassNode cn;
        try {
            cn = Transformers.node(bytes);

        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
        if(cn==null)return;
        if (!definedClasses.contains(cn.name)) {
            if (Bootstrap.isSelfClass(cn.name)) {
                definedClasses.add(cn.name);
                defineClass(cn.superName);
                for(String s:cn.interfaces){
                    defineClass(s);
                }
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("<clinit>")) {
                        for (String uc : usedClasses(mn)) {
                            defineClass(uc);
                        }
                    }
                }
                try {
                    if(ReflectionUtils.invokeMethod(Bootstrap.classLoader,"findLoadedClass",new Class[]{String.class},className)!=null)return;
                    NativeUtils.defineClass(Bootstrap.classLoader, bytes);
                }
                catch (Exception e){
                    e.printStackTrace();

                }

            }
        }

    }

    public static ArrayList<String> usedClasses(MethodNode mn){
        ArrayList<String> usedClasses = new ArrayList<>();
        for(AbstractInsnNode ain : mn.instructions.toArray()){
            if(ain instanceof MethodInsnNode){
                usedClasses.add(((MethodInsnNode) ain).owner);
            }
            if(ain instanceof FieldInsnNode){
                usedClasses.add(((FieldInsnNode) ain).owner);
            }
            if(ain instanceof TypeInsnNode){
                usedClasses.add(((TypeInsnNode) ain).desc);
            }
            if(ain instanceof LdcInsnNode){
                if(((LdcInsnNode) ain).cst instanceof Type){
                    usedClasses.add(((Type) ((LdcInsnNode) ain).cst).getInternalName());
                }
            }
        }
        return usedClasses;
    }
}
