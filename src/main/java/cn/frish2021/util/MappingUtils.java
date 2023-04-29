package cn.frish2021.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class MappingUtils {
    public static void deobf(File input,File out) throws IOException {
        JarFile jarFile = new JarFile(input);
        Map<String, String> map = MappingUtil.getMap(true);
        MappingUtil.analyze(jarFile);
        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(out));
        while (jarEntryEnumeration.hasMoreElements()) {
            JarEntry jarEntry = jarEntryEnumeration.nextElement();
            if (jarEntry.isDirectory() || jarEntry.getName().contains("META-INF")) {
                continue;
            }
            if (jarEntry.getName().endsWith(".class")) {
                byte[] accept = MappingUtil.classMapping(jarFile.getInputStream(jarEntry), map);
                String substring = jarEntry.getName().substring(0, jarEntry.getName().lastIndexOf(".class"));
                JarEntry classEntry = new JarEntry(map.getOrDefault(substring, substring) + ".class");
                jarOutputStream.putNextEntry(classEntry);
                jarOutputStream.write(accept);
            } else {
                JarEntry file = new JarEntry(jarEntry.getName());
                jarOutputStream.putNextEntry(file);
                jarOutputStream.write(IOUtils.toByteArray(jarFile.getInputStream(jarEntry)));
            }
        }
        jarOutputStream.closeEntry();
        jarOutputStream.close();
    }
}
