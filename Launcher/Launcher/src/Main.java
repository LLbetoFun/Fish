import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Main {
    public static void main(String[] args) {
        String jarFilePath = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        String destDirPath = ".";//System.getProperty("user.home");

        File destDir = new File(destDirPath,".fish");
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                System.err.println("Could not create destination directory: " + destDirPath);
                return;
            }
        }

        try (JarFile jarFile = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(!entry.getName().startsWith("fish"))continue;
                File destFile = new File(destDir, entry.getName());
                if (!entry.isDirectory()) {
                    extractFile(jarFile, entry, destFile);
                } else if (!destFile.exists()) {
                    if (!destFile.mkdirs()) {
                        System.err.println("Could not create directory: " + destFile);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Thread(()->{

            //traverseFolder2(destDir.getAbsolutePath());

        }).start();

    }
    public static void traverseFolder2(String path) {

        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                System.out.println("文件夹是空的!");
                return;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        //System.out.println("文件夹:" + file2.getAbsolutePath());
                        traverseFolder2(file2.getAbsolutePath());
                    } else {
                        if(file2.getAbsolutePath().endsWith(".jar")){
                            Mapper.mapJar(file2);
                        }
                        //System.out.println("文件:" + );
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
    }
    private static void extractFile(JarFile jarFile, JarEntry entry, File destFile) throws IOException {
        try (InputStream in = jarFile.getInputStream(entry);
             FileOutputStream out = new FileOutputStream(destFile))
        {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }


        }

    }
}