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

public class vscodeRuns extends Task {
    @TaskAction
    public void run() {
        getProject().getLogger().lifecycle("凑数东西");
    }
}
