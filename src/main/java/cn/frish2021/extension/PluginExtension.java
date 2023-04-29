package cn.frish2021.extension;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.gradle.api.Project;

import java.io.File;

public class PluginExtension {
    public Minecraft minecraft = new Minecraft();
    public MineOld loader = new MineOld();
    public String modid = null;
    public String version = null;
    private final Project project;

    public PluginExtension(Project project) {
        this.project = project;
    }

    public File getUserCache() {
        File file = new File(project.getGradle().getGradleUserHomeDir(), "caches" + File.separator + "Minecraft-old");
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }
    public Minecraft minecraft(Closure closure) {
        project.configure(minecraft, closure);
        return minecraft;
    }

    public MineOld loader(Closure closure) {
        project.configure(loader, closure);
        return loader;
    }

    public static class Minecraft extends GroovyObjectSupport {
        public String gameVersion = "1.3.1";
        public String mainClass = "net.minecraft.launchwrapper.Launch";
    }

    public static class MineOld extends GroovyObjectSupport {
        public String mixinRefmap = "mixin.refmap.json";
        public String tweaker = "cn.frish2021.base.launch.MMLTweaker";
    }
}
