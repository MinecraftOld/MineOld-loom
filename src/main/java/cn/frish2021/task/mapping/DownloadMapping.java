package cn.frish2021.task.mapping;

import cn.frish2021.task.base.Task;
import cn.frish2021.util.game.GameUtil;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DownloadMapping extends Task {
    @TaskAction
    public void run() {
        File clientMappingFile = GameUtil.getClientMappingFile(extension);
        if (!clientMappingFile.exists()) {
            try {
                if (!clientMappingFile.exists()) {
                    FileUtils.write(clientMappingFile, GameUtil.getClientMapping(extension), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (!GameUtil.getMappingJarFile(extension).exists()) {
                FileUtils.writeByteArrayToFile(GameUtil.getMappingJarFile(extension), GameUtil.getMappingJar(extension));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
