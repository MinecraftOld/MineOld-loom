package cn.frish2021.util;

import cn.frish2021.extension.PluginExtension;
import cn.frish2021.util.game.GameUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.objectweb.asm.Opcodes.ASM9;

public class MappingUtil {
    public static final HashMap<String, String> classObfToCleanMap = new HashMap<>();
    public static final HashMap<String, String> classCleanToObfMap = new HashMap<>();

    public static final HashMap<String, String> fieldObfToCleanMap = new HashMap<>();
    public static final HashMap<String, String> fieldCleanToObfMap = new HashMap<>();

    public static final HashMap<String, String> methodObfToCleanMap = new HashMap<>();
    public static final HashMap<String, String> methodCleanToObfMap = new HashMap<>();
    public static final Map<String, ArrayList<String>> superHashMap = new HashMap<String, ArrayList<String>>();
    public static boolean init = false;

    public static void init(PluginExtension extension) throws IOException {
        if (init) {
            return;
        }

        List<String> f = Files.readAllLines(GameUtil.getClientMappingFile(extension).toPath());

        for (String line : f) {
            String[] lines = line.split("\\r\\n|\\n");
            int line1 = 0;
            for (String string : lines) {
                line1++;
                String[] arg = string.trim().split(" ");
                String type = arg[0];
                try {
                    switch (type) {
                        case "CL:": {
                            String obf = arg[1];
                            String clean = arg[2];
                            classObfToCleanMap.put(obf, clean);
                            classCleanToObfMap.put(clean, obf);
                            break;
                        }
                        case "FD:": {
                            String obf = arg[1];
                            String clean = arg[2];
                            fieldObfToCleanMap.put(obf, clean);
                            fieldCleanToObfMap.put(clean, obf);
                            break;
                        }
                        case "MD:": {
                            String obf = arg[1];
                            String obfDescription = arg[2];
                            String clean = arg[3];
                            String cleanDescription = arg[4];
                            methodObfToCleanMap.put(obf + " " + obfDescription, clean + " " + cleanDescription);
                            methodCleanToObfMap.put(clean + " " + cleanDescription, obf + " " + obfDescription);
                            break;
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
        init = true;
    }

    public static Map<String, String> getMap(boolean clean) {
        final HashMap<String, String> map = new HashMap<>();
        //Class Map
        if (clean) {
            map.putAll(classObfToCleanMap);
        } else {
            map.putAll(classCleanToObfMap);
        }

        //Field Map
        for (Map.Entry<String, String> stringStringEntry : fieldObfToCleanMap.entrySet()) {
            String key = clean ? stringStringEntry.getKey() : stringStringEntry.getValue();
            String value = clean ? stringStringEntry.getValue() : stringStringEntry.getKey();
            String className = key.substring(0, key.lastIndexOf("/"));
            String fieldName = key.substring(key.lastIndexOf("/") + 1);
            map.put(className + "." + fieldName, value.substring(value.lastIndexOf("/") + 1));
        }

        //method map
        for (Map.Entry<String, String> stringStringEntry : methodObfToCleanMap.entrySet()) {
            String[] methodObfSplit = (clean ? stringStringEntry.getKey() : stringStringEntry.getValue()).split(" ");
            String[] methodCleanSplit = (clean ? stringStringEntry.getValue() : stringStringEntry.getKey()).split(" ");
            String methodObfClass = methodObfSplit[0].substring(0, methodObfSplit[0].lastIndexOf("/"));
            String methodObfName = methodObfSplit[0].substring(methodObfSplit[0].lastIndexOf("/") + 1);
            String methodCleanName = methodCleanSplit[0].substring(methodCleanSplit[0].lastIndexOf("/") + 1);
            map.put(methodObfClass + "." + methodObfName + methodObfSplit[1], methodCleanName);
        }
        return map;
    }

    public static byte[] classMapping(InputStream inputStream, Map<String, String> map) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        ClassWriter classWriter = new ClassWriter(0);
        ClassRemapper classRemapper = new ClassRemapper(new ClassVisitor(ASM9, classWriter) {
        }, new SimpleRemapper(map) {
            @Override
            public String mapFieldName(String owner, String name, String descriptor) {
                String remappedName = map(owner + '.' + name);
                if (remappedName == null) {
                    if (superHashMap.get(owner) != null) {
                        for (String s : superHashMap.get(owner)) {
                            String rn = mapFieldName(s, name, descriptor);
                            if (rn != null) {
                                return rn;
                            }
                        }
                    }
                }
                return remappedName == null ? name : remappedName;
            }

            @Override
            public String mapMethodName(String owner, String name, String descriptor) {
                String remappedName = map(owner + '.' + name + descriptor);
                if (remappedName == null) {
                    if (superHashMap.get(owner) != null) {
                        for (String s : superHashMap.get(owner)) {
                            String rn = mapMethodName(s, name, descriptor);
                            if (rn != null) {
                                return rn;
                            }
                        }
                    }
                }
                return remappedName == null ? name : remappedName;
            }
        });
        classReader.accept(classRemapper, 0);
        return classWriter.toByteArray();
    }

    public static void analyze(JarFile jarFile) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                classAnalyze(jarFile.getInputStream(jarEntry));
            }
        }
    }

    public static void classAnalyze(InputStream inputStream) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        classReader.accept(new ClassVisitor(ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                ArrayList<String> strings = new ArrayList<>();
                if (superHashMap.containsKey(name)) {
                    if (superName != null) {
                        if (!superHashMap.get(name).contains(superName)) {
                            strings.add(superName);
                        }
                    }

                    if (interfaces != null) {
                        for (String anInterface : interfaces) {
                            if (!superHashMap.get(name).contains(anInterface)) {
                                strings.add(anInterface);
                            }
                        }
                    }
                    superHashMap.get(name).addAll(strings);
                } else {
                    if (superName != null) {
                        strings.add(superName);
                    }

                    if (interfaces != null) {
                        Collections.addAll(strings, interfaces);
                    }
                    superHashMap.put(name, strings);
                }
                super.visit(version, access, name, signature, superName, interfaces);
            }
        }, 0);
    }
}
