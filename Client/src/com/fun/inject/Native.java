package com.fun.inject;


import com.fun.inject.transform.GameClassTransformer;
import com.fun.inject.transform.IClassTransformer;

public class Native{
    public Class<?> nativeUtils;
    public Native(Class<?> nativeUtilsClazz){
        nativeUtils=nativeUtilsClazz;
    }
    public Native(){
    }
    public void addTransformer(IClassTransformer transformer, boolean b) {
        NativeUtils.transformers.add(transformer);
    }


    public void addTransformer(IClassTransformer transformer) {
        /*try {
            Class<?> nativeUtils=ClassLoader.getSystemClassLoader().loadClass("injection.com.fun.NativeUtils");
            Field ft= nativeUtils.getDeclaredField("transformers");
            ArrayList<ClassFileTransformer> tList= (ArrayList<ClassFileTransformer>) ft.get(null);
            tList.add(transformer);

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {

        }*/
        NativeUtils.transformers.add(transformer);
    }




    public void retransformClasses(Class<?>... classes){
        for(Class<?> kls:classes)
            NativeUtils.retransformClass0(kls);
    }




    public Class<?>[] getAllLoadedClasses() {
        return NativeUtils.getAllLoadedClasses().toArray(new Class[0]);
    }






    public void redefineClass(Class<?> clazz, byte[] bytes) {
        //ReflectionUtils.invokeMethod(nativeUtils,"redefineClass",new Class[]{Class.class,byte[].class},clazz,bytes);
        NativeUtils.redefineClass(clazz,bytes);
    }
    public void doneTransform(){
        NativeUtils.doneTransform();
    }

    public void removeTransformer(GameClassTransformer transformer) {
        NativeUtils.transformers.remove(transformer);
    }
}
