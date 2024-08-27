package com.fun.inject.mapper;

import com.fun.inject.Main;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Random;
import java.util.jar.*;
import java.util.zip.ZipEntry;

public class ObfMapper {

    public static String newPackage=Main.getNewPackage();
    public static String[] selfClasses = new String[]{"com.fun","org.newdawn","javax.vecmath","org.objectweb"};


    public static String[] getSelfClasses(){
        return selfClasses;
    }






    private static String getObfClass(String replace) {
        if(newPackage==null||replace.endsWith("inject/InjectorUtils"))return replace;

        for (String s:getSelfClasses()){
            replace=replace.replace(s.replace('.','/'),newPackage);
        }
        return replace;
    }
    public static class ReMapper extends Remapper {
        @Override
        public String map(String typeName) {
            return getObfClass(typeName);
        }
    }


}
