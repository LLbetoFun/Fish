package com.fun.inject;



import com.fun.client.FunGhostClient;
import com.fun.client.config.ConfigModule;
import com.fun.inject.define.Definer;
import com.fun.inject.injection.asm.api.Transformer;
import com.fun.inject.injection.asm.api.Transformers;
import com.fun.inject.injection.asm.transformers.ClassLoaderTransformer;
import com.fun.inject.mapper.Mapper;
import com.fun.inject.transform.GameClassTransformer;
import com.fun.inject.utils.ReflectionUtils;
import com.fun.network.packets.PacketDestroy;
import com.fun.network.packets.PacketInit;
import com.fun.network.packets.PacketMCPath;
import com.fun.network.packets.PacketMCVer;

import com.fun.network.TCPClient;
import com.fun.network.TCPServer;
import com.fun.client.font.FontManager;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class Bootstrap {

    public static String VERSION = "1.4";
    public static String jarPath;
    public static boolean isAgent = false;
    public static int SERVERPORT = 11432;
    public static Map<String, byte[]> classes = new HashMap<>();
    public static Native instrumentation;
    public static GameClassTransformer transformer;
    public static MinecraftType minecraftType = MinecraftType.VANILLA;
    public static MinecraftVersion minecraftVersion = MinecraftVersion.VER_189;
    public static String[] selfClasses = new String[]{"com.fun","org.newdawn","javax.vecmath","com.sun.jna","org.objectweb"};
    public static ClassLoader classLoader;

    private static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1)
            outStream.write(buffer, 0, len);
        outStream.close();
        return outStream.toByteArray();

    }

    public static void cacheJar(File file) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(file.toPath()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null)
                if (!entry.isDirectory())
                    if (entry.getName().endsWith(".class"))
                        classes.put(entry.getName().replace("/", ".").substring(0, entry.getName().length() - 6), readStream(zis));
        }
    }

    public static void in9ectClassLoader(Class<?> classLoader) {
        ClassLoaderTransformer classLoaderTransformer=new ClassLoaderTransformer(classLoader);
        Transformers.transformers.add(classLoaderTransformer);
        try {
            classLoaderTransformer.oldBytes=readClazzBytes(classLoaderTransformer.clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //NativeUtils.setEventNotificationMode(1,54);
        //NativeUtils.retransformClass(classLoaderTransformer.clazz);
        //NativeUtils.setEventNotificationMode(0,54);
        //Transformers.transformers.remove(classLoaderTransformer);
        ClassReader cr=new ClassReader(classLoaderTransformer.oldBytes);
        ClassNode node = new ClassNode();
        cr.accept(node, ClassReader.EXPAND_FRAMES);
        for(MethodNode mn:node.methods){
            if(mn.name.equals("findClass")){
                LabelNode l=new LabelNode();
                InsnList insnList=new InsnList();
                insnList.add(new VarInsnNode(Opcodes.ALOAD,0));
                insnList.add(new VarInsnNode(Opcodes.ALOAD,getArgumentCount(mn.desc)));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Bootstrap.class),"hookFindClass","(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Class;"));
                insnList.add(new VarInsnNode(Opcodes.ASTORE,2));
                insnList.add(new VarInsnNode(Opcodes.ALOAD,2));
                insnList.add(new JumpInsnNode(Opcodes.IFNULL,l));
                insnList.add(new VarInsnNode(Opcodes.ALOAD,2));
                insnList.add(new InsnNode(Opcodes.ARETURN));
                insnList.add(l);
                mn.instructions.insert(insnList);
            }
            if (mn.name.equals("loadClass")) {
                LabelNode l = new LabelNode();
                for (int i = 0; i < mn.instructions.size(); ++i) {
                    AbstractInsnNode methodInsnNode = mn.instructions.get(i);
                    if (methodInsnNode instanceof MethodInsnNode) {
                        /*if (((MethodInsnNode) methodInsnNode).name.equals("containsKey")) {
                            InsnList insnList = new InsnList();

                            insnList.add(new JumpInsnNode(Opcodes.IFNE, l));
                            insnList.add(new VarInsnNode(Opcodes.ALOAD,1));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Agent.class), "isSelfClass", "(Ljava/lang/String;)Z"));
                            AbstractInsnNode ifeq = methodInsnNode.getNext();
                            mn.instructions.insert(methodInsnNode, insnList);
                            mn.instructions.insert(ifeq,l);
                            break;
                        }*/

                    }
                    if(methodInsnNode.getOpcode()==Opcodes.GOTO&&methodInsnNode instanceof JumpInsnNode){
                        LabelNode l2 = new LabelNode();
                        l= (LabelNode) methodInsnNode.getNext();
                        InsnList insnList=new InsnList();
                        insnList.add(new VarInsnNode(Opcodes.ALOAD,1));
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Bootstrap.class), "isSelfClass", "(Ljava/lang/String;)Z"));
                        insnList.add(new JumpInsnNode(Opcodes.IFEQ,l2));
                        insnList.add(new VarInsnNode(Opcodes.ALOAD,0));
                        insnList.add(new VarInsnNode(Opcodes.ALOAD,1));
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,Type.getInternalName(Bootstrap.class),"hookFindClass","(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Class;"));
                        insnList.add(new VarInsnNode(Opcodes.ASTORE,4));
                        //todo findClass
                        insnList.add(new JumpInsnNode(Opcodes.GOTO,((JumpInsnNode) methodInsnNode).label));
                        insnList.add(l2);
                        mn.instructions.insert(l,insnList);
                        break;

                    }
                }

            }
        }

        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
        try {
            node.accept(writer);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        byte[] bytes = writer.toByteArray();
        NativeUtils.redefineClass(classLoaderTransformer.clazz,bytes);
        classLoaderTransformer.newBytes=bytes;
        try {
            FileUtils.writeByteArrayToFile(new File(System.getProperty("user.home"),classLoaderTransformer.name+".class"), bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static int getArgumentCount(String methodDescriptor) {
        int argumentCount = 0;

        for(int currentOffset = 1; methodDescriptor.charAt(currentOffset) != ')'; ++argumentCount) {
            while(methodDescriptor.charAt(currentOffset) == '[') {
                ++currentOffset;
            }

            if (methodDescriptor.charAt(currentOffset++) == 'L') {
                int semiColumnOffset = methodDescriptor.indexOf(59, currentOffset);
                currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
            }
        }

        return argumentCount;
    }

    public static byte[] readClazzBytes(Class<?> c) throws IOException {

        return InjectUtils.getClassBytes(c);//(c.getName().replace('.', '/') + ".class"));
    }

    public static boolean isSelfClass(String name){
        if(Main.getNewPackage()!=null&&name.startsWith(Main.getNewPackage()))return true;
        name=name.replace('/','.');
        for (String cname : getSelfClasses()) {
            if (name.startsWith(cname))
            {
                return true;
            }
        }
        return false;
    }
    public static String[] getSelfClasses(){
        return selfClasses;
    }
    public static Class<?> hookFindClass(ClassLoader cl,String name) {
        for (String cname : getSelfClasses()) {
            if (name.replace('/', '.').startsWith(cname))
                try {
                    byte[] bytes = classes.get(name);//InjectUtils.getClassBytes(cl, name);//IOUtils.readAllBytes(ClassLoader.getSystemResourceAsStream(name.replace('.', '/') + ".class"));
                    return (Class<?>) ReflectionUtils.invokeMethod(
                            cl,
                            "defineClass",
                            new Class[]{
                                    String.class,
                                    byte[].class,
                                    int.class,
                                    int.class
                            },
                            name,
                            bytes,
                            0,
                            bytes.length
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                }


        }
        return null;
    }


    public static void getVersion(){
        TCPClient.send(Main.SERVERPORT,new PacketMCVer(null));

        try {
            Class<?> c= findClass("net.minecraft.client.Minecraft");//com/heypixel/heypixel/HeyPixel
            if(c!=null) {
                minecraftType = MinecraftType.MCP;
                if (ReflectionUtils.getFieldValue(c, minecraftVersion==MinecraftVersion.VER_1181?"f_90981_":"field_71432_P") != null)//m_91087_
                    minecraftType = MinecraftType.FORGE;
            }

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }



    private static void loadJar(URLClassLoader urlClassLoader, URL jar) {
        NativeUtils.loadJar(urlClassLoader, jar);
    }
    private static void defineClassesInCache(){
        for(String s: classes.keySet()){
            Definer.defineClass(s);
        }
    }
    public static void magic(){//启动注入线程
        new Thread(Bootstrap::inject).start();
    }
    public static native void inject();//初始化完毕后调用start
    public static void start() throws URISyntaxException, IOException, InterruptedException
    {//启动方法
                isAgent =true;
                File f=new File(System.getProperty("user.home")+"\\fish.txt");
                BufferedReader bufferedreader = new BufferedReader(new FileReader(f));
                String line="";
                while ((line = bufferedreader.readLine()) != null) {
                    jarPath=line;
                    break;
                }
                bufferedreader.close();


                instrumentation = new Native();
                boolean running = true;
                while (running) {
                    for (Object o : Thread.getAllStackTraces().keySet().toArray()) {
                        Thread thread = (Thread) o;
                        if (thread.getName().equals("Client thread")||thread.getName().equals("Render thread")) {

                            classLoader=thread.getContextClassLoader();
                            running = false;
                            break;
                        }
                    }
                }

                getVersion();
                File injection=new File(new File(jarPath).getParent(),"/injections/"+ minecraftVersion.injection);
                injection=Mapper.mapJar(injection, minecraftType);
                String hooks=new File(new File(Bootstrap.jarPath).getParent(),"hooks.jar").getAbsolutePath();
                NativeUtils.addToBootstrapClassLoaderSearch(hooks);
                NativeUtils.addToSystemClassLoaderSearch(injection.getAbsolutePath());
                try {
                    if (ClassLoader.getSystemClassLoader() != (classLoader)) {
                        if(classLoader.getClass().getName().contains("launchwrapper")|| classLoader.getClass().getSuperclass().getName().contains("ModuleClassLoader")){
                            cacheJar(injection);
                            cacheJar(new File(jarPath));
                            defineClassesInCache();

                        }
                        else{
                            loadJar((URLClassLoader) classLoader, injection.toURI().toURL());
                            loadJar((URLClassLoader) classLoader, new File(jarPath).toURI().toURL());
                        }
                    }


                    Class<?> agentClass = findClass(Bootstrap.class.getName());

                    for (Method m : agentClass.getDeclaredMethods()) {
                        if (m.getName().equals("init")) {
                            m.invoke(null, classLoader, jarPath);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            transform();

    }

    public static void transform() {
        Transformers.init();
        transformer = new GameClassTransformer();
        instrumentation.addTransformer(transformer, true);

        //NativeUtils.messageBox("native cl:"+NativeUtils.class.getClassLoader(),"Fish");

        for (Transformer transformer : Transformers.transformers) {
            try {

                if(transformer.clazz==null){
                    continue;
                }

                NativeUtils.retransformClass(transformer.clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        instrumentation.doneTransform();

        System.out.println("Transform classes successfully");


    }

    public static void init(ClassLoader cl, String jarPathIn) {
        classLoader=cl;
        jarPath=jarPathIn;
        isAgent=true;
        getVersion();
        File injection=new File(new File(jarPath).getParent(),"/injections/"+ minecraftVersion.injection);
        try {
            Mapper.readMappings(injection.getAbsolutePath(), minecraftType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Class<?> agentClass = Agent.findClass("com.fun.client.FunGhostClient");
        //System.out.println("clientcl:" + agentClass.getClassLoader());
        //Class<?> agentClass = Agent.findClass("com.fun.inject.Bootstrap");
        //System.out.println("agentcl:"+agentClass.getClassLoader());


        TCPClient.send(TCPServer.getTargetPort(),new PacketInit());
        System.out.println("client init start");
        FunGhostClient.init();
        System.out.println("client init successful");
        ConfigModule.loadConfig();
        System.out.println("config loaded");
        TCPClient.send(Main.SERVERPORT, new PacketMCPath(System.getProperty("user.dir")));//"mcpath " +
        TCPServer.startServer(SERVERPORT);
        FontManager.init();
        System.out.println("fish ghost client start!");







    }

    public static Class<?> findClass(String name) throws ClassNotFoundException {
      return classLoader.loadClass(name.replace('/','.'));
    }

    public static void destroyClient() {
        if(!isAgent){
            TCPClient.send(SERVERPORT,new PacketDestroy());
            return;
        }
        instrumentation.removeTransformer(transformer);
        Transformers.transformers.forEach(it -> {
            NativeUtils.redefineClass(it.getClazz(), it.getOldBytes());
        });
        try {
            findClass(FunGhostClient.class.getName()).getDeclaredMethod("destroyClient").invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();

        }
        NativeUtils.destroy();
    }


    ;


}
