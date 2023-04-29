package cn.frish2021.task.base;

import cn.frish2021.extension.PluginExtension;
import org.gradle.api.DefaultTask;

public class Task extends DefaultTask {
    public PluginExtension extension;
    public Task() {
        extension = getProject().getExtensions().getByType(PluginExtension.class);
    }
}
