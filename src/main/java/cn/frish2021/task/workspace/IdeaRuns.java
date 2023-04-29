package cn.frish2021.task.workspace;

import cn.frish2021.task.base.Task;
import cn.frish2021.util.game.GameUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class IdeaRuns extends Task {
    @TaskAction
    public void run() {
        StringBuilder vmArgs = new StringBuilder("-Djava.library.path=" + GameUtil.getClientNativeFileDir(extension).getAbsolutePath());
        StringBuilder programArgs = new StringBuilder();

        if (extension.loader.tweaker != null) {
            programArgs.append("--tweakClass").append(" ").append(extension.loader.tweaker).append(" ");
        }
        programArgs.append("--assetsDir").append(" ").append(GameUtil.getClientAssetsDir(extension).getAbsolutePath()).append(" ");
        programArgs.append("--assetIndex").append(" ").append(extension.minecraft.gameVersion).append(" ");
        programArgs.append("--version").append(" ").append("mineold").append(" ");
        programArgs.append("--accessToken").append(" ").append("0").append(" ");
        programArgs.append("--gameDir").append(" ").append(getProject().getRootProject().file("run").getAbsolutePath()).append(" ");

        try {
            String idea = IOUtils.toString(Objects.requireNonNull(IdeaRuns.class.getResourceAsStream("/runClient.xml")), StandardCharsets.UTF_8);

            idea = idea.replace("%NAME%", "runClient");
            idea = idea.replace("%MAIN_CLASS%", extension.minecraft.mainClass);
            idea = idea.replace("%IDEA_MODULE%", getModule());
            idea = idea.replace("%PROGRAM_ARGS%", programArgs.toString().replaceAll("\"", "&quot;"));
            idea = idea.replace("%VM_ARGS%", vmArgs.toString().replaceAll("\"", "&quot;"));
            String projectPath = getProject() == getProject().getRootProject() ? "" : getProject().getPath().replace(":", "_");
            File ideaConfigurationDir = getProject().getRootProject().file(".idea");
            File runConfigurations = new File(ideaConfigurationDir, "runConfigurations");
            File clientRunConfiguration = new File(runConfigurations, "runClient" + projectPath + ".xml");
            if (!runConfigurations.exists()) {
                runConfigurations.mkdir();
            }
            FileUtils.write(clientRunConfiguration, idea, StandardCharsets.UTF_8);
            File run = getProject().getRootProject().file("run");
            if (!run.exists()) {
                run.mkdir();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getModule() {
        Project project = getProject();
        StringBuilder stringBuilder = new StringBuilder(project.getName() + ".main");
        while ((project = project.getParent()) != null) {
            stringBuilder.insert(0, project.getName() + ".");
        }
        return stringBuilder.toString();
    }
}
