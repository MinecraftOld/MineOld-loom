package cn.frish2021.task.mapping;

import cn.frish2021.task.base.Task;
import cn.frish2021.util.MappingUtil;
import cn.frish2021.util.MappingUtils;
import cn.frish2021.util.game.GameUtil;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class DeMappingTask extends Task {
    @TaskAction
    public void run() {
        try {
            MappingUtil.init(extension);
            File clientDeObf = GameUtil.getClientDeObfFile(extension);
            File client = GameUtil.getClientFile(extension);
            if (client.exists()) {
                MappingUtils.deobf(client, clientDeObf);
            } else {
                getProject().getLogger().lifecycle("没有混淆前文件");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
