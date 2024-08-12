package com.fun.inject.magic;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Vector;
import com.fun.hook.Hooks;


import com.fun.inject.transform.IClassTransformer;
import com.fun.inject.NativeUtils;

import com.fun.inject.injection.asm.api.Transformers;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.tree.*;


public class AntiClassScanner {

    public static void killBMW() {

        System.out.println("BWM,is my dream ride");
        IClassTransformer transformer = new IClassTransformer() {

            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if (isBWMClass(classBeingRedefined)) {
                    ClassReader reader = new ClassReader(classfileBuffer);
                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                    ClassVisitor visitor = new ClassVisitor(Opcodes.ASM5, writer) {
                        boolean isBadMethod = false;

                        @Override
                        public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                String[] exceptions) {
                            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                            Type type = Type.getMethodType(desc);
                            if (type.getReturnType().equals(Type.BOOLEAN_TYPE))
                                for (Type arg : type.getArgumentTypes()) {
                                    if (arg.getClassName().equals("java.lang.Class")) {
                                        isBadMethod = true;
                                        break;
                                    }
                                }
                            return new AdviceAdapter(access, mv, access, name, desc) {
                                protected void onMethodEnter() {
                                    if (isBadMethod) {
                                        visitLdcInsn(false);
                                        visitInsn(Opcodes.IRETURN);
                                    }
                                };

                                public void visitMethodInsn(int opcode, String owner, String name, String desc,
                                        boolean itf) {
                                    if (owner.equals("java/lang/Field") && name.equals("get")) {
                                        owner = Type.getInternalName(AntiClassScanner.class);
                                        desc = "(Ljava/lang/Field;" + desc.substring(1);
                                        opcode = Opcodes.INVOKESTATIC;
                                    }
                                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                                };
                            };
                        }
                    };
                    reader.accept(visitor, ClassReader.EXPAND_FRAMES);
                }
                return classfileBuffer;
            }

        };
        NativeUtils.transformers.add(transformer);
        for (Class<?> clazz : NativeUtils.getAllLoadedClasses()) {
            if (isBWMClass(clazz)&&NativeUtils.isModifiableClass(clazz))
                try {
                    NativeUtils.retransformClass(clazz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        NativeUtils.transformers.remove(transformer);
        NativeUtils.doneTransform();
    }
    public static void fuckBWM(){

        IClassTransformer transformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            System.out.println(className);
            if(className.replace('.','/').equals("java/lang/Class")) {
                ClassNode node= Transformers.node(classfileBuffer);
                for(MethodNode mn: node.methods){
                    if(mn.name.equals("getName")){
                        LabelNode l=new LabelNode();
                        InsnList insnList=new InsnList();
                        insnList.add(new VarInsnNode(Opcodes.ALOAD,0));
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class),"hookGetClassName","(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/String;"));
                        AbstractInsnNode insnNode=mn.instructions.get(mn.instructions.size()-1);
                        for (int i = 0; i < mn.instructions.size(); ++i) {
                            AbstractInsnNode n = mn.instructions.get(i);
                            if(n.getOpcode()==Opcodes.ARETURN){
                                insnNode=n;
                                break;
                            }
                        }
                        mn.instructions.insertBefore(insnNode,insnList);
                    }
                    if(mn.name.equals("getProtectionDomain")){
                        System.out.println("getProtectionDomain found");
                        LabelNode l=new LabelNode();
                        InsnList insnList=new InsnList();
                        insnList.add(new VarInsnNode(Opcodes.ALOAD,0));
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class),"hookGetProtectionDomain","(Ljava/security/ProtectionDomain;Ljava/lang/Class;)Ljava/security/ProtectionDomain;"));
                        AbstractInsnNode insnNode=mn.instructions.get(mn.instructions.size()-1);
                        for (int i = 0; i < mn.instructions.size(); ++i) {
                            AbstractInsnNode n = mn.instructions.get(i);
                            if(n.getOpcode()==Opcodes.ARETURN){
                                insnNode=n;
                                break;
                            }
                        }
                        mn.instructions.insertBefore(insnNode,insnList);
                    }
                    if(mn.name.equals("forName")){
                        LabelNode l=new LabelNode();
                        InsnList insnList=new InsnList();
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class),"hookFindClass","(Ljava/lang/Class;)Ljava/lang/Class;"));
                        AbstractInsnNode insnNode=mn.instructions.get(mn.instructions.size()-1);
                        for (int i = 0; i < mn.instructions.size(); ++i) {
                            AbstractInsnNode n = mn.instructions.get(i);
                            if(n.getOpcode()==Opcodes.ARETURN){
                                insnNode=n;
                                break;
                            }
                        }
                        mn.instructions.insertBefore(insnNode,insnList);
                    }

                }
                byte[] newBytes=Transformers.rewriteClass(node);
                try {
                    FileUtils.writeByteArrayToFile(new File(System.getProperty("user.home")+"/.fish",className + ".class"),
                            newBytes);
                } catch (IOException e) {throw new RuntimeException(e);}
                return newBytes;
            }


            if(className.replace('/','.').equals("java.lang.reflect.Field")){
                ClassNode node= Transformers.node(classfileBuffer);
                for(MethodNode mn: node.methods) {
                    if (mn.name.equals("getName")) {
                        LabelNode l=new LabelNode();
                        InsnList insnList=new InsnList();
                        insnList.add(new VarInsnNode(Opcodes.ALOAD,0));
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class),"hookGetFieldName","(Ljava/lang/String;Ljava/lang/reflect/Field;)Ljava/lang/String;"));
                        AbstractInsnNode insnNode=mn.instructions.get(mn.instructions.size()-1);
                        for (int i = 0; i < mn.instructions.size(); ++i) {
                            AbstractInsnNode n = mn.instructions.get(i);
                            if(n.getOpcode()==Opcodes.ARETURN){
                                insnNode=n;
                                break;
                            }
                        }
                        mn.instructions.insertBefore(insnNode,insnList);
                    }
                    if (mn.name.equals("get")) {
                        LabelNode l=new LabelNode();
                        InsnList insnList=new InsnList();
                        insnList.add(new VarInsnNode(Opcodes.ALOAD,0));
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class),"hookGetFieldValue","(Ljava/lang/Object;Ljava/lang/reflect/Field;)Ljava/lang/Object;"));
                        AbstractInsnNode insnNode=mn.instructions.get(mn.instructions.size()-1);
                        for (int i = 0; i < mn.instructions.size(); ++i) {
                            AbstractInsnNode n = mn.instructions.get(i);
                            if(n.getOpcode()==Opcodes.ARETURN){
                                insnNode=n;
                                break;
                            }
                        }
                        mn.instructions.insertBefore(insnNode,insnList);
                    }
                }
                byte[] newBytes=Transformers.rewriteClass(node);
                try {
                    FileUtils.writeByteArrayToFile(new File(System.getProperty("user.home")+"/.fish",className + ".class"),
                            newBytes);
                } catch (IOException e) {throw new RuntimeException(e);}
                return newBytes;
            }


            if(className.replace('/','.').equals("java.lang.reflect.Method")){
                ClassNode node= Transformers.node(classfileBuffer);
                for(MethodNode mn: node.methods) {
                    if (mn.name.equals("getName")) {
                        LabelNode l=new LabelNode();
                        InsnList insnList=new InsnList();
                        insnList.add(new VarInsnNode(Opcodes.ALOAD,0));
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class),"hookGetMethodName","(Ljava/lang/String;Ljava/lang/reflect/Method;)Ljava/lang/String;"));
                        AbstractInsnNode insnNode=mn.instructions.get(mn.instructions.size()-1);
                        for (int i = 0; i < mn.instructions.size(); ++i) {
                            AbstractInsnNode n = mn.instructions.get(i);
                            if(n.getOpcode()==Opcodes.ARETURN){
                                insnNode=n;
                                break;
                            }
                        }
                        mn.instructions.insertBefore(insnNode,insnList);
                    }
                }
                byte[] newBytes=Transformers.rewriteClass(node);
                try {
                    FileUtils.writeByteArrayToFile(new File(System.getProperty("user.home")+"/.fish",className + ".class"),
                            newBytes);
                } catch (IOException e) {throw new RuntimeException(e);}
                return newBytes;
            }
            return classfileBuffer;
        };
        NativeUtils.transformers.add(transformer);
        if(NativeUtils.isModifiableClass(Class.class))NativeUtils.retransformClass(Class.class);
        if(NativeUtils.isModifiableClass(Method.class))NativeUtils.retransformClass(Method.class);
        if(NativeUtils.isModifiableClass(Field.class))NativeUtils.retransformClass(Field.class);
        NativeUtils.transformers.remove(transformer);
        NativeUtils.doneTransform();


    }

    public static Object get(Field field, Object object) {
        try {
            Object value = field.get(object);
            if (object instanceof ClassLoader &&(value instanceof ArrayList))
                value = new ArrayList<>();
            if (object instanceof ClassLoader &&(value instanceof Vector[]))
                value = new Vector[]{};
            if(object.getClass().getName().contains("URLClassPath") &&value instanceof ArrayList){
                value=new ArrayList<>();
            }
            //(field.getName().startsWith("class")||
            //field.getName().startsWith("path")||field.getName().startsWith("url"))
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isBWMClass(Class<?> clazz) {
        if (clazz.getProtectionDomain() != null
                &&clazz.getProtectionDomain().getCodeSource()!=null
                && clazz.getProtectionDomain().getCodeSource().getLocation()!=null
                && clazz.getProtectionDomain().getCodeSource().getLocation().toString().contains("/mods/"))
            return true;
        return false;
    }

}
