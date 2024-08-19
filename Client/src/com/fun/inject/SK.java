package com.fun.inject;

import com.fun.eventapi.event.events.Event;
import com.fun.inject.transform.GameClassTransformer;


import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Map;

public class SK {
    public static void sm(){
        event.add(new net.minecraft.A.A());
    }
    public static final String sf="1";

    public static Map<String, byte[]> classes;//= new HashMap<>();
    public static Native instrumentation;
    public static GameClassTransformer transformer;
    public static String VER_1_8_9="1.8.9";
    public static MinecraftType minecraftType;//=MinecraftType.VANILLA;
    public static MinecraftVersion minecraftVersion;//=MinecraftVersion.VER_189;
    public static String[] selfClasses;//=new String[]{"com.fun","org.newdawn","javax.vecmath","com.sun.jna"};
    public static ClassLoader classLoader;
    public static ArrayList event=new ArrayList();;

    public void redefineClass(Class<?> clazz, byte[] bytes){

    }
}
