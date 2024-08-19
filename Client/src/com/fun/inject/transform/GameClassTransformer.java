package com.fun.inject.transform;

import com.fun.inject.Bootstrap;
import com.fun.inject.injection.asm.api.Inject;
import com.fun.inject.injection.asm.api.Mixin;
import com.fun.inject.injection.asm.api.Transformer;
import com.fun.inject.injection.asm.api.Transformers;
import com.fun.inject.mapper.Mapper;
import com.fun.utils.version.methods.Methods;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class GameClassTransformer implements IClassTransformer {
    public static ClassNode node(byte[] bytes) {
        if (bytes != null && bytes.length != 0) {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.EXPAND_FRAMES);
            return node;
        }

        return null;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        for (Transformer transformer : Transformers.transformers) {
            if (transformer.clazz == classBeingRedefined && loader == Bootstrap.classLoader) {
                transformer.oldBytes = classfileBuffer;

                try {
                    FileUtils.writeByteArrayToFile(new File(System.getProperty("user.home"), transformer.getName() + "Old.class"), classfileBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ClassNode node = Transformers.node(transformer.oldBytes);

                //System.out.println("1");
                for (Method method : transformer.getClass().getDeclaredMethods()) {
                    //System.out.println("2");
                    //Agent.System.out.println(method.toString());
                    if (method.isAnnotationPresent(Inject.class)) {

                        if (method.getParameterCount() != 1 || !MethodNode.class.isAssignableFrom(method.getParameterTypes()[0]))
                            continue;

                        Inject inject = method.getAnnotation(Inject.class);

                        String methodToModify = inject.method();
                        String desc = inject.descriptor();

                        String obfName = Mapper.getObfMethod(methodToModify, transformer.getName(), desc);
                        String obfDesc = Mapper.getObfMethodDesc(desc);//Mappings.getObfMethod(methodToModify);
                        if (obfName == null || obfName.isEmpty()) {
                            //System.out.println("Could not find {} in class {}", methodToModify, transformer.getName());
                            continue;
                        }

                        System.out.println(obfDesc + " " + obfName);
                        // huh???
                        for (MethodNode mNode : node.methods) {
                            //System.out.println(mNode.name+mNode.desc);
                            if (mNode.name.equals(obfName) && mNode.desc.equals(obfDesc)) {
                                try {
                                    method.invoke(transformer, mNode);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    } else if (method.isAnnotationPresent(Mixin.class)) {
                        if (method.getParameterCount() != 1)
                            continue;

                        Mixin inject = method.getAnnotation(Mixin.class);
                        Methods methodInfo = inject.method();
                        String name = methodInfo.getName();
                        String desc = methodInfo.getDescriptor();
                        boolean trans = false;
                        for (MethodNode mNode : node.methods) {


                            if (mNode.name.equals(name) && mNode.desc.equals(desc)) {
                                try {
                                    method.invoke(transformer, mNode);

                                    trans = true;
                                    //System.out.println("transformed "+method.getName());

                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    //System.out.println("Failed to invoke method {} {}", e.getMessage(), e.getStackTrace()[0]);
                                    //e.printStackTrace();
                                }

                                break;
                            }
                        }
                        if (!trans) {
                            //System.out.println("method {}{} not trans", name,desc);
                            try {
                                MethodNode mn = (MethodNode) method.invoke(transformer, node(transformer.oldBytes));
                                node.methods.add(mn);
                            } catch (Exception e) {
                                //System.out.println("Failed to add method {} {}", method.getParameterTypes(), method.getName());

                            }
                        }
                    }


                }

                byte[] newBytes = Transformers.rewriteClass(node);
                if (newBytes == null) {
                    System.out.println(className + " rewriteClass failed");
                    return null;
                }
                File fo = new File(System.getProperty("user.home"), transformer.getName() + ".class");


                try {

                    FileUtils.writeByteArrayToFile(fo, newBytes.clone());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("transformer:" + transformer.getName() + " bytes in " + fo.getAbsolutePath());

                transformer.newBytes = newBytes;
                return transformer.newBytes;
            }
        }
        return null;
    }
}
