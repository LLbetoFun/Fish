package com.fun.inject;

import com.fun.inject.transform.GameClassTransformer;

import java.util.HashMap;
import java.util.Map;

public class In9ectManager {
    public static final Map<String, byte[]> classes = new HashMap<>();
    public static Native instrumentation;
    public static GameClassTransformer transformer;
    public static MinecraftType minecraftType=MinecraftType.VANILLA;
    public static MinecraftVersion minecraftVersion=MinecraftVersion.VER_189;
    public final static String[] selfClasses=new String[]{"com.fun","org.newdawn","javax.vecmath","com.sun.jna"};
    public static ClassLoader classLoader;

    public static Class<?> findClass(String name) throws ClassNotFoundException {
      return classLoader.loadClass(name.replace('/','.'));
    }

    public static void magic(){
        Bootstrap.magic();
    }
}
