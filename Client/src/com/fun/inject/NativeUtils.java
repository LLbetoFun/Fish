package com.fun.inject;

import com.fun.inject.transform.IClassTransformer;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;


public class NativeUtils {
    public static ArrayList<IClassTransformer> transformers=new ArrayList<>();
    public static ArrayList<ClassDefiner> classDefiners=new ArrayList<>();
    public static final int JVMTI_CLASSFILEHOOK=54;
    public static final int JVMTI_ENABLE=1;
    public static final int JVMTI_DISABLE=0;


    public static byte[] transform(  ClassLoader         loader,
                                            String              className,
                                            Class<?>            classBeingRedefined,
                                            ProtectionDomain protectionDomain,
                                     byte[]              classfileBuffer){
        try {
            NativeUtils.setEventNotificationMode(JVMTI_DISABLE,JVMTI_CLASSFILEHOOK);
            for (IClassTransformer t : transformers) {
                byte[] b = t.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
                if (b != null) {
                    classDefiners.add(new ClassDefiner(classBeingRedefined,b));
                    NativeUtils.setEventNotificationMode(JVMTI_ENABLE,JVMTI_CLASSFILEHOOK);
                    return classfileBuffer;
                }
            }
            NativeUtils.setEventNotificationMode(JVMTI_ENABLE,JVMTI_CLASSFILEHOOK);
            return classfileBuffer;
        }
        catch (Exception e){
            e.printStackTrace();
            NativeUtils.setEventNotificationMode(JVMTI_ENABLE,JVMTI_CLASSFILEHOOK);
            return classfileBuffer;
        }
    }
    public static class ClassDefiner{
        public Class<?> clazz;
        public byte[] bytes;

        public ClassDefiner(Class<?> c , byte[] b) {
            clazz=c;bytes=b;
        }
    }
    public static native void setEventNotificationMode(int state,int event);

    public static native ArrayList<Class<?>> getAllLoadedClasses();
    public static native int redefineClass(Class<?> clazz,byte[] bytes);
    public static void retransformClass(Class<?> clazz){
        NativeUtils.setEventNotificationMode(1,54);
        NativeUtils.retransformClass0(clazz);
        NativeUtils.setEventNotificationMode(0,54);
    }
    public static void doneTransform(){
        ArrayList<NativeUtils.ClassDefiner> classDefiners = NativeUtils.classDefiners;
        for (int i = 0, classDefinersSize = classDefiners.size(); i < classDefinersSize; i++) {
            NativeUtils.ClassDefiner classDefiner = classDefiners.get(i);
            System.out.println("redefine:"+classDefiner.clazz+" error:"+redefineClass(classDefiner.clazz, classDefiner.bytes));
        }
        NativeUtils.classDefiners.clear();
    }
    public static native void retransformClass0(Class<?> clazz);
    public static native void clickMouse(int event);
    public static native void destroy();
    public static native void loadJar(URLClassLoader cl, URL url);
    public static native void messageBox(String msg,String title);
    public static native void addToSystemClassLoaderSearch(String path);
    public static native void addToBootstrapClassLoaderSearch(String path);

    public static native Class<?> defineClass(ClassLoader cl,byte[] bytes);
    public static native boolean isModifiableClass(Class<?> clazz);



}
