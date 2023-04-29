package cn.frish2021.task.mapping;

import cn.frish2021.task.base.Task;
import cn.frish2021.util.MappingUtil;
import cn.frish2021.util.game.GameUtil;
import cn.frish2021.util.mixin.MixinMapping;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarFile;


public class ReMappingTask extends Task {
    @TaskAction
    public void run() {
        try {
            MappingUtil.init(extension);
            File classes = new File(getProject().getBuildDir(), "classes");
            MappingUtil.analyze(new JarFile(GameUtil.getClientDeObfFile(extension)));
            Files.walkFileTree(classes.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toFile().getName().endsWith(".class")) {
                        InputStream inputStream = file.toUri().toURL().openStream();
                        MappingUtil.classAnalyze(inputStream);
                        inputStream.close();
                    }
                    return super.visitFile(file, attrs);
                }
            });

            JsonObject mixinReMap = new JsonObject();
            JsonObject mixinMappings = new JsonObject();
            if (extension.loader.mixinRefmap != null) {
                Files.walkFileTree(classes.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toFile().getName().endsWith(".class")) {
                            InputStream inputStream = file.toUri().toURL().openStream();
                            MixinMapping mixinMapping = new MixinMapping();
                            mixinMapping.accept(inputStream);
                            MappingUtil.superHashMap.put(mixinMapping.className, new ArrayList<>(mixinMapping.mixins));
                            for (String mixin : mixinMapping.mixins) {

                                JsonObject mapping = new JsonObject();

                                mixinMapping.methods.forEach((descriptor, methods) -> {
                                    for (String method : methods) {
                                        if (method.contains("(")) {
                                            mapping.addProperty(method, getMethodObf(mixin, method, false));
                                        } else {
                                            mapping.addProperty(method, getMethodObf(mixin, method + descriptor.replace("Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;", ""), false));
                                        }
                                    }
                                });

                                for (String mixinTarget : mixinMapping.targets) {
                                    if (!mixinTarget.contains("field:")) {
                                        String targetClass = mixinTarget.substring(1, mixinTarget.indexOf(";"));

                                        String targetMethod = getMethodObf(targetClass, mixinTarget.substring(mixinTarget.indexOf(";") + 1), false);
                                        if (targetMethod == null) {
                                            continue;
                                        }
                                        mapping.addProperty(mixinTarget, targetMethod);
                                    } else {
                                        String left = mixinTarget.split("field:")[0];
                                        String right = mixinTarget.split("field:")[1];
                                        String targetClass = MappingUtil.classCleanToObfMap.get(left.substring(1, left.indexOf(";")));
                                        String targetField = MappingUtil.classCleanToObfMap.get(right.substring(1, right.indexOf(";")));

                                        if (targetClass == null || targetField == null) {
                                            continue;
                                        }

                                        mapping.addProperty(mixinTarget, "L" + targetClass + ";field:L" + targetField + ";");
                                    }
                                }

                                for (Map.Entry<String, String> entry : mixinMapping.accessors.entrySet()) {

                                    String fieldName = MappingUtil.fieldCleanToObfMap.get(mixin + "/" + entry.getValue());

                                    if (fieldName == null) {
                                        continue;
                                    }

                                    if (entry.getKey().contains(";")) {
                                        String arg;
                                        if (!entry.getKey().contains(")V")) {
                                            arg = entry.getKey().substring(entry.getKey().lastIndexOf(")") + 1);
                                        } else {
                                            arg = entry.getKey().substring(entry.getKey().indexOf("(") + 1, entry.getKey().lastIndexOf(")"));
                                        }

                                        arg = arg.substring(1, arg.lastIndexOf(";"));
                                        arg = MappingUtil.classCleanToObfMap.get(arg);
                                        if (arg == null) {
                                            continue;
                                        }
                                        mapping.addProperty(entry.getValue(), fieldName.split("/")[1] + ":L" + arg + ";");
                                    } else {
                                        mapping.addProperty(entry.getValue(), entry.getKey());
                                    }
                                }

                                for (Map.Entry<String, String> entry : mixinMapping.invokes.entrySet()) {
                                    mapping.addProperty(entry.getValue(), getMethodObf(mixin, entry.getValue() + entry.getKey(), false));
                                }
                                mixinMappings.add(mixinMapping.className, mapping);
                            }
                            inputStream.close();
                        }
                        return super.visitFile(file, attrs);
                    }
                });
                mixinReMap.add("mappings", mixinMappings);
            }

            JavaPluginConvention java = (JavaPluginConvention) getProject().getConvention().getPlugins().get("java");
            File resourceDir = new File(getProject().getBuildDir(), "resources");
            for (SourceSet sourceSet : java.getSourceSets()) {
                if (!resourceDir.exists()) {
                    resourceDir.mkdir();
                }
                File dir = new File(resourceDir, sourceSet.getName());
                if (!dir.exists()) {
                    dir.mkdir();
                }

                if (extension.loader.mixinRefmap != null) {
                    FileUtils.write(new File(dir, extension.loader.mixinRefmap), new GsonBuilder().setPrettyPrinting().create().toJson(mixinReMap), StandardCharsets.UTF_8);
                }
            }
            Files.walkFileTree(classes.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toFile().getName().endsWith(".class")) {
                        InputStream inputStream = file.toUri().toURL().openStream();
                        byte[] bytes = MappingUtil.classMapping(inputStream, MappingUtil.getMap(false));
                        Files.write(file, bytes);
                        inputStream.close();
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getMethodObf(String klass, String method, boolean only) {
        String methodName = method.substring(0, method.indexOf("("));
        String methodDescriptor = method.substring(method.indexOf("("));
        String methodObf = MappingUtil.methodCleanToObfMap.get(klass + "/" + methodName + " " + methodDescriptor);
        if (methodObf == null) {
            return null;
        }
        if (!only) {
            methodObf = "L" + methodObf.split(" ")[0].replace("/", ";") + methodObf.split(" ")[1];
        } else {
            methodObf = methodObf.split(" ")[0];
            methodObf = methodObf.substring(methodObf.lastIndexOf("/") + 1);
        }
        return methodObf;
    }
}
