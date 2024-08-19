
import com.azul.crs.client.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class Mapper {
    public static HashMap<String,String> classMap = new HashMap<>();


    static {
        classMap.put("com/fun/inject/Bootstrap","com/fun/inject/Bootstrap");
        classMap.put("com/fun/inject/InjectorUtils","com/fun/inject/InjectorUtils");
        classMap.put("com/fun/inject/Main","com/fun/inject/Main");
    }
    public static byte[] getAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();

        return buffer.toByteArray();
    }
    public static byte[] mapBytes(byte[] bytes){
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        //System.out.println("start remap: "+classNode.name);
        for (MethodNode methodNode : classNode.methods) {

            methodNode.desc = getObfMethodDesc(methodNode.desc);
            for (AbstractInsnNode insnNode : methodNode.instructions) {
                if (insnNode instanceof MethodInsnNode) {
                    ((MethodInsnNode) insnNode).owner = getObfClass(((MethodInsnNode) insnNode).owner);

                    ((MethodInsnNode) insnNode).desc = getObfMethodDesc(((MethodInsnNode) insnNode).desc);
                }
                if (insnNode instanceof FieldInsnNode) {
                    ((FieldInsnNode) insnNode).owner = getObfClass(((FieldInsnNode) insnNode).owner);

                    ((FieldInsnNode) insnNode).desc = getObfFieldDesc(((FieldInsnNode) insnNode).desc);

                }
                if (insnNode instanceof TypeInsnNode) {
                    ((TypeInsnNode) insnNode).desc = getObfClass(((TypeInsnNode) insnNode).desc);
                }
                if(insnNode instanceof LdcInsnNode) {
                    if(((LdcInsnNode) insnNode).cst instanceof Type){
                        ((LdcInsnNode) insnNode).cst=Type.getType(getObfFieldDesc(((Type) ((LdcInsnNode) insnNode).cst).getDescriptor()));
                    }
                    if(((LdcInsnNode) insnNode).cst instanceof String){
                        if(((String) ((LdcInsnNode) insnNode).cst).contains("com/fun/")){
                            ((LdcInsnNode) insnNode).cst=obf((String) ((LdcInsnNode) insnNode).cst);
                        }
                    }
                }
                if(insnNode instanceof InvokeDynamicInsnNode){
                    ((InvokeDynamicInsnNode) insnNode).desc=getObfMethodDesc(((InvokeDynamicInsnNode) insnNode).desc);
                    Object[] bsmArgs = ((InvokeDynamicInsnNode) insnNode).bsmArgs;
                    for (int i = 0, bsmArgsLength = bsmArgs.length; i < bsmArgsLength; i++) {
                        Object a = bsmArgs[i];
                        if (a instanceof Type) {
                            Type b =Type.getType(getObfDesc(((Type) a).getDescriptor()));
                            bsmArgs[i]= b;
                            //System.out.println("type:"+b);//bsmArgs[i]=Type.getType(get)
                        }
                        if(a instanceof Handle){
                            Handle b=new Handle(((Handle) a).getTag()
                                    ,getObfClass(((Handle) a).getOwner()),
                                    ((Handle) a).getName(),
                                    getObfDesc(((Handle) a).getDesc()),
                                    ((Handle) a).isInterface());
                            bsmArgs[i]=b;
                            //System.out.println("handler:"+ ((Handle) a).getOwner()+" "+ ((Handle) a).getName()+" "+ ((Handle) a).getDesc());
                        }
                        //todo
                    }
                    //System.out.println("____"+((InvokeDynamicInsnNode) insnNode).name+" "+((InvokeDynamicInsnNode) insnNode).desc);
                }

            }
            for(LocalVariableNode l:methodNode.localVariables){
                l.desc = getObfDesc(l.desc);
            }
        }
        for(FieldNode fieldNode : classNode.fields){

            fieldNode.desc = getObfFieldDesc(fieldNode.desc);

        }

        classNode.name = getObfClass(classNode.name);
        classNode.superName = getObfClass(classNode.superName);
        classNode.sourceFile = null;
        classNode.sourceDebug=null;
        classNode.signature=null;
        ClassWriter writer = new MyClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        //System.out.println("obf:" + classReader.getClassName()+":"+classNode.name);
        //System.out.println(classNode.name);
        return writer.toByteArray();
    }
    public static String getObfClass(String mcpName){
        if(!mcpName.replace('.','/').startsWith("com/fun/"))return mcpName;
        boolean isArray = mcpName.contains("[]");
        String cn=mcpName.endsWith("[]")?mcpName.substring(0,mcpName.length()-2):mcpName;
        String t=classMap.get(cn);
        if(t==null){
            String newClassName="net/minecraft/"+generateRandomString(32);
            classMap.put(newClassName,cn);
            classMap.put(cn,newClassName);
            System.out.println(newClassName+" "+cn);
            t=newClassName;
        }
        return t+(isArray?"[]":"");
    }
    public static boolean isMethodDesc(String desc){
        return(desc.contains("(")&&desc.contains(")"));
    }
    public static boolean isDesc(String desc){
        return(desc.contains("L")&&desc.contains(";"));
    }
    public static String getObfDesc(String desc){
        if(isMethodDesc(desc)){
            return getObfMethodDesc(desc);
        }
        return getObfFieldDesc(desc);
    }
    public static String obf(String mcpName){
        if(isDesc(mcpName)){
            return getObfDesc(mcpName);
        }
        return getObfClass(mcpName);
    }
    public static String getObfMethodDesc(String desc){
        org.objectweb.asm.Type[] args = org.objectweb.asm.Type.getArgumentTypes(desc);
        StringBuilder sb=new StringBuilder();
        sb.append("(");
        for(org.objectweb.asm.Type t:args){
            if(t.getSort()== org.objectweb.asm.Type.OBJECT){
                sb.append("L");
                sb.append(getObfClass(t.getInternalName()));
                sb.append(";");
            }
            else if(t.getSort()== Type.ARRAY&&t.getElementType().getSort()== org.objectweb.asm.Type.OBJECT){
                sb.append("[L");
                sb.append(getObfClass(t.getElementType().getInternalName()));
                sb.append(";");

            }
            else{
                sb.append(t.getDescriptor());
            }
        }
        sb.append(")");
        org.objectweb.asm.Type t= org.objectweb.asm.Type.getReturnType(desc);
        if(t.getSort()== org.objectweb.asm.Type.OBJECT){
            sb.append("L");
            sb.append(getObfClass(t.getInternalName()));
            sb.append(";");
        }
        else if(t.getSort()== Type.ARRAY&&t.getElementType().getSort()== org.objectweb.asm.Type.OBJECT){
            sb.append("[L");
            sb.append(getObfClass(t.getElementType().getInternalName()));
            sb.append(";");

        }
        else{
            sb.append(t.getDescriptor());
        }
        return sb.toString();
    }
    public static String getObfFieldDesc(String desc){
        StringBuilder sb=new StringBuilder();
        Type t=Type.getType(desc);
        if(t.getSort()== org.objectweb.asm.Type.OBJECT){
            sb.append("L");
            sb.append(getObfClass(t.getInternalName()));
            sb.append(";");
        }
        else if(t.getSort()== Type.ARRAY&&t.getElementType().getSort()== org.objectweb.asm.Type.OBJECT){
            sb.append("[L");
            sb.append(getObfClass(t.getElementType().getInternalName()));
            sb.append(";");

        }
        else{
            sb.append(t.getDescriptor());
        }
        return sb.toString();
    }
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKMNOPQRSTUVWXYZabcdefghijklLmnopqrstuvwxyz0123456789";//操傻逼妖猫又在撸管ḀḁḂḃḄḅḆḇḈḉḊḋḌḍḎḏḐḑḒḓḔḕḖḗḘḙḚḛḜḝḞḟḠḡḢḣḤḥḦḧḨḩḪḫḬḭḮḯḰḱḲḳḴḵḶḷḸḹḺḻḼḽḾḿṀṁṂṃṄṅṆṇṈṉṊṋṌṍṎṏṐṑṒṓṔṕṖṗṘṙṚṛṜṝṞṟṠṡṢṣṤṥṦṧṨṩṪṫṬṭṭṮṯṰṱṲṳṴṵṶṷṸṹṺṻṼṽṾṿẀẁẂẃẄẅẆẇẈẉẊẋẌẍẎẏẐẑẒẓẔẕẖẗẘẙẚẠạẢảẤấẦầẨẩẪẫẬằẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹ";
        //
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }
    public static File mapJar(File jarIn){
        //if(mcType==MinecraftType.NONE)return jarIn;
        File targetJar =new File(jarIn.getAbsolutePath()+".tmp");
        try(JarFile jar = new JarFile(jarIn)){
            if(targetJar.exists()){
                targetJar.delete();
            }
            targetJar.createNewFile();

            //JarFile target=new JarFile(tj);
            JarOutputStream jos=new JarOutputStream(Files.newOutputStream(targetJar.toPath()));
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                try {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")&&entry.getName().startsWith("com/fun")) {
                        InputStream is = jar.getInputStream(entry);
                        byte[] b = mapBytes(getAllBytes(is));
                        jos.putNextEntry(new ZipEntry(getObfClass(entry.getName().replace(".class",""))+".class"));
                        jos.write(b);
                        jos.closeEntry();


                    }
                    else{
                        InputStream is = jar.getInputStream(entry);
                        byte[] b = getAllBytes(is);
                        jos.putNextEntry(new ZipEntry(entry.getName()));
                        jos.write(b);
                        jos.closeEntry();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            //target.close();
            jos.close();



        }
        catch(Exception e){
            e.printStackTrace();
        }
        if(jarIn.exists()){
            jarIn.delete();
        }
        targetJar.renameTo(jarIn);
        return targetJar;
    }


}
