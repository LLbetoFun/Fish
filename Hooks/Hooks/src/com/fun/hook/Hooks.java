package com.fun.hook;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class Hooks {
    static {
        Printer.pw.println(System.getProperty("sun.java.command"));
    }
    public static String hookGetClassName(String name,Class<?> c){
        StackTraceElement[] stackTrace=new RuntimeException().getStackTrace();
        if(name.contains("com.fun")) {
            for (int i = 0, stackTraceLength = stackTrace.length; i < stackTraceLength - 1; i++) {
                StackTraceElement st = stackTrace[i];
                String className = st.getClassName();
                if (className.contains("com.fun")) {
                    return name;
                }
            }
            Printer.pw.println("for BWM i'm sorry:"+name);
            return "java.lang.System";
        }
        return name;
    }
    public static ProtectionDomain hookGetProtectionDomain(ProtectionDomain pd, Class<?> c){
        StackTraceElement[] stackTrace=new RuntimeException().getStackTrace();
        if(pd.getCodeSource().getLocation().getFile().contains("fish")) {
            for (int i = 0, stackTraceLength = stackTrace.length; i < stackTraceLength - 1; i++) {
                StackTraceElement st = stackTrace[i];
                String className = st.getClassName();
                if (className.contains("com.fun")) {
                    return pd;
                }
            }
            Printer.pw.println("for BWM i'm sorry:"+pd.getCodeSource().getLocation().getFile());
            return null;
        }
        return pd;
    }

}
