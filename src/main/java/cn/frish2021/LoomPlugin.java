package cn.frish2021;

import cn.frish2021.extension.PluginExtension;
import cn.frish2021.task.loader.DownloadAssetsTask;
import cn.frish2021.task.loader.DownloadGameTask;
import cn.frish2021.task.mapping.DeMappingTask;
import cn.frish2021.task.mapping.DownloadMapping;
import cn.frish2021.task.mapping.ReMappingTask;
import cn.frish2021.task.workspace.EclipesRuns;
import cn.frish2021.task.workspace.IdeaRuns;
import cn.frish2021.task.workspace.vscodeRuns;
import cn.frish2021.util.UrlUtil;
import cn.frish2021.util.game.GameUtil;
import org.gradle.api.Project;

public class LoomPlugin extends PluginBase {
    @Override
    public void apply(Project project) {
        project.getLogger().lifecycle("MineOld-loom 1.0.2");
        project.getExtensions().create("setting", PluginExtension.class, project);

        project.afterEvaluate(after -> {
            after.getRepositories().maven(mavenArtifactRepository -> {
                mavenArtifactRepository.setName("minecraft");
                mavenArtifactRepository.setUrl(UrlUtil.GAME_LIBRARIES);
            });

            after.getRepositories().maven(mavenArtifactRepository -> {
                mavenArtifactRepository.setName("minecraft-old");
                mavenArtifactRepository.setUrl("https://minecraftold.github.io/maven/");
            });

            after.getRepositories().maven(mavenArtifactRepository -> {
                mavenArtifactRepository.setName("SpongePowered");
                mavenArtifactRepository.setUrl("https://repo.spongepowered.org/repository/maven-public/");
            });

            after.getRepositories().mavenCentral();
            after.getRepositories().mavenLocal();

            PluginExtension extension = after.getExtensions().getByType(PluginExtension.class);
            GameUtil.getLibraries(extension).forEach(library -> after.getDependencies().add("implementation", library));
            after.getDependencies().add("implementation", "org.spongepowered:mixin:0.8");
            after.getDependencies().add("implementation", "com.google.code.gson:gson:2.8.9");
            after.getDependencies().add("implementation", "com.google.guava:guava:31.0.1-jre");
            after.getDependencies().add("implementation", "org.ow2.asm:asm-tree:9.2");
            after.getDependencies().add("implementation", "org.ow2.asm:asm-commons:9.2");
            after.getDependencies().add("implementation", "org.ow2.asm:asm-util:9.2");
            after.getDependencies().add("implementation", "org.apache.logging.log4j:log4j-core:2.0-beta9");
            after.getDependencies().add("implementation", "org.apache.logging.log4j:log4j-api:2.0-beta9");

            after.getPlugins().apply("java");
            after.getPlugins().apply("idea");
            after.getTasks().create("DownloadGame", DownloadGameTask.class, downloadGameTask -> downloadGameTask.setGroup("mineold-loom"));
            after.getTasks().create("DownloadMapping", DownloadMapping.class, downloadMapping -> downloadMapping.setGroup("mineold-loom"));
            after.getTasks().create("DownloadAssets", DownloadAssetsTask.class, downloadAssetsTask -> downloadAssetsTask.setGroup("mineold-loom"));
            after.getTasks().create("DeMapping", DeMappingTask.class, deMappingTask -> deMappingTask.setGroup("mapping"));
            after.getTasks().create("ReMappingClass", ReMappingTask.class, reMappingTask -> reMappingTask.setGroup("mapping"));
            after.getTasks().create("GenIdea", IdeaRuns.class, ideaRuns -> ideaRuns.setGroup("workspace"));
            after.getTasks().create("GenEclipse", EclipesRuns.class, eclipesRuns -> eclipesRuns.setGroup("workspace"));
            after.getTasks().create("GenVscode", vscodeRuns.class, vscodeRuns -> vscodeRuns.setGroup("workspace"));

            after.getTasks().getByName("GenIdea").finalizedBy(
                    after.getTasks().getByName("DownloadMapping"),
                    after.getTasks().getByName("DownloadGame"),
                    after.getTasks().getByName("DownloadAssets"),
                    after.getTasks().getByName("DeMapping")
            );

            after.getTasks().getByName("compileJava").finalizedBy(after.getTasks().getByName("ReMappingClass"));

            after.getDependencies().add("compileOnly", after.getDependencies().create(after.files(GameUtil.getClientDeObfFile(extension).getAbsolutePath())));
            after.getDependencies().add("runtimeOnly", after.getDependencies().create(after.files(GameUtil.getClientFile(extension).getAbsolutePath())));
            after.getDependencies().add("runtimeOnly", after.getDependencies().create(after.files(GameUtil.getMappingJarFi(extension).getAbsolutePath())));
        });
    }
}
