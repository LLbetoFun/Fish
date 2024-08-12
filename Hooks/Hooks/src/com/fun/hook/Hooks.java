package com.fun.hook;



import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class Hooks {
    static {
        Printer.pw.println(System.getProperty("sun.java.command"));
    }
    public static String hookGetClassName(String name,Class<?> c){
        if(name.contains("com.fun")) {
            StackTraceElement[] stackTrace=new RuntimeException().getStackTrace();
            for (int i = 0, stackTraceLength = stackTrace.length; i < stackTraceLength; i++) {
                StackTraceElement st = stackTrace[i];
                String className = st.getClassName();
                if (className.contains("com.fun")&&!className.contains("com.fun.hook.Hooks")) {
                    return name;
                }
            }
            Printer.pw.println("for BWM i'm sorry:"+name);
            return "java.lang.System";
        }
        return name;
    }
    public static ProtectionDomain hookGetProtectionDomain(ProtectionDomain pd, Class<?> c){
        if(pd == null||pd.getCodeSource()==null||pd.getCodeSource().getLocation()==null||pd.getCodeSource().getLocation().getFile()==null)return null;
        if(pd.getCodeSource().getLocation().getFile().contains("fish")) {
            StackTraceElement[] stackTrace=new RuntimeException().getStackTrace();
            for (int i = 0, stackTraceLength = stackTrace.length; i < stackTraceLength; i++) {
                StackTraceElement st = stackTrace[i];
                String className = st.getClassName();
                if (className.contains("com.fun")&&!className.contains("com.fun.hook.Hooks")) {
                    return pd;
                }
            }
            Printer.pw.println("for BWM i'm sorry:"+pd.getCodeSource().getLocation().getFile());
            return null;
        }
        return pd;
    }
    public static Class<?> hookFindClass(Class<?> c) throws ClassNotFoundException {
        if(c.getName().contains("com.fun")) {
            StackTraceElement[] stackTrace=new RuntimeException().getStackTrace();
            Printer.pw.println("fuck:"+c.getName());
            for (int i = 0, stackTraceLength = stackTrace.length; i < stackTraceLength; i++) {
                StackTraceElement st = stackTrace[i];
                String className = st.getClassName();
                if (className.contains("com.fun")&&!className.contains("com.fun.hook.Hooks")) {
                    return c;
                }
            }
            Printer.pw.println("for BWM i'm sorry:"+c.getName());
            throw new ClassNotFoundException();
        }
        return c;
    }
    public static String hookGetMethodName(String methodName, Method method) throws ClassNotFoundException {
        Class<?> c=method.getDeclaringClass();
        String name=c.getName();
        if(name.contains("com.fun")) {
            StackTraceElement[] stackTrace=new RuntimeException().getStackTrace();
            for (int i = 0, stackTraceLength = stackTrace.length; i < stackTraceLength; i++) {
                StackTraceElement st = stackTrace[i];
                String className = st.getClassName();
                if (className.contains("com.fun")&&!className.contains("com.fun.hook.Hooks")) {
                    return methodName;
                }
            }
            Printer.pw.println("for BWM i'm sorry:"+name);
            return "sm";
        }
        return methodName;
    }
    public static String hookGetFieldName(String fieldName, Field field) throws ClassNotFoundException {
        Class<?> c=field.getDeclaringClass();
        String name=c.getName();
        if(name.contains("com.fun")) {
            StackTraceElement[] stackTrace=new RuntimeException().getStackTrace();
            for (int i = 0, stackTraceLength = stackTrace.length; i < stackTraceLength; i++) {
                StackTraceElement st = stackTrace[i];
                String className = st.getClassName();
                if (className.contains("com.fun")&&!className.contains("com.fun.hook.Hooks")) {
                    return fieldName;
                }
            }
            Printer.pw.println("for BWM i'm sorry:"+name);
            return "sf";
        }
        return fieldName;
    }
    public static Object hookGetFieldValue(Object value,Field field){
        Class<?> c=field.getDeclaringClass();
        String name=c.getName();
        if(name.contains("com.fun")) {
            StackTraceElement[] stackTrace=new RuntimeException().getStackTrace();
            for (int i = 0, stackTraceLength = stackTrace.length; i < stackTraceLength; i++) {
                StackTraceElement st = stackTrace[i];
                String className = st.getClassName();
                if (className.contains("com.fun")&&!className.contains("com.fun.hook.Hooks")) {
                    return value;
                }
            }
            Printer.pw.println("for BWM i'm sorry:"+name);
            return null;
        }
        return value;
    }

}
