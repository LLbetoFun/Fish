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
    public static String newPackage=generateRandomString(16);
    public static String[] selfClasses = new String[]{"com.fun","org.newdawn","javax.vecmath","org.objectweb"};

    public static boolean isSelfClass(String name){
        //NativeUtils.messageBox(name,"Fish");
        //System.out.println(name+" hook");

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
        ClassWriter cw = new MyClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
        ClassRemapper crm=new ClassRemapper(cw,new ReMapper());
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(crm, ClassReader.EXPAND_FRAMES);

        return cw.toByteArray();
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
            Manifest manifest = new Manifest();
            try {
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, getObfClass("com/fun/inject/Main").replace('/','.'));
            }
            catch(NullPointerException e){

            }
            JarOutputStream jos=new JarOutputStream(Files.newOutputStream(targetJar.toPath()),manifest);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                try {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")&&isSelfClass(entry.getName())) {
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

    private static String getObfClass(String replace) {
        if(replace.endsWith("inject/InjectorUtils"))return replace;

        for (String s:getSelfClasses()){
            replace=replace.replace(s.replace('.','/'),newPackage).replace('.','/');
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
