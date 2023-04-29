package cn.frish2021.task.loader;

import cn.frish2021.task.base.Task;
import cn.frish2021.util.game.GameUtil;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class DownloadGameTask extends Task {
    @TaskAction
    public void downloadGame() {
        File localClient = GameUtil.getLocalClient(extension);
        try {
            if (localClient.exists()) {
                FileUtils.copyFile(localClient, GameUtil.getClientFile(extension));
            } else {
                if (!GameUtil.getClientFile(extension).exists()) {
                    FileUtils.writeByteArrayToFile(GameUtil.getClientFile(extension), GameUtil.getClientJar(extension));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
