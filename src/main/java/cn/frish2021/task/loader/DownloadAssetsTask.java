package cn.frish2021.task.loader;

import cn.frish2021.task.base.Task;
import cn.frish2021.util.UrlUtil;
import cn.frish2021.util.game.GameUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DownloadAssetsTask extends Task {
    @TaskAction
    public void run() {
        String clientAssets = GameUtil.getClientAssets(extension);
        getProject().getLogger().lifecycle("Download index ...");
        //index file
        if (!GameUtil.getClientAssetsIndexFile(extension).exists()) {
            try {
                FileUtils.write(GameUtil.getClientAssetsIndexFile(extension), clientAssets, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getProject().getLogger().lifecycle("Download assets ...");
        Map<String, AssetObject> objectMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> objects : new Gson().fromJson(clientAssets, JsonObject.class).get("objects").getAsJsonObject().entrySet()) {
            objectMap.put(objects.getKey(), new Gson().fromJson(objects.getValue(), AssetObject.class));
        }

        objectMap.forEach((name, assetObject) -> {
            File localClientAssetsObjectFile = GameUtil.getLocalClientAssetsObjectFile(assetObject.getHash());
            File clientAssetsObjectFile = GameUtil.getClientAssetsObjectFile(extension, assetObject.getHash());
            try {
                if (localClientAssetsObjectFile.exists()) {
                    FileUtils.copyFile(localClientAssetsObjectFile, clientAssetsObjectFile);
                } else {
                    if (!clientAssetsObjectFile.exists()) {
                        FileUtils.writeByteArrayToFile(clientAssetsObjectFile, UrlUtil.readFile(UrlUtil.GAME_RESOURCES + "/" + assetObject.getHash().substring(0, 2) + "/" + assetObject.getHash()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        getProject().getLogger().lifecycle("Download natives ...");
        File clientNativeJarDir = GameUtil.getClientNativeJarDir(extension);
        File clientNativeFileDir = GameUtil.getClientNativeFileDir(extension);

        GameUtil.getNatives(extension).forEach(link -> {
            String name = link.substring(link.lastIndexOf("/") + 1);
            File file = new File(clientNativeJarDir, name);
            try {
                if (!file.exists()) {
                    FileUtils.writeByteArrayToFile(file, UrlUtil.readFile(link));
                }

                ZipFile zipFile = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    if (zipEntry.isDirectory() || zipEntry.getName().contains("META-INF")) {
                        continue;
                    }
                    FileUtils.writeByteArrayToFile(new File(clientNativeFileDir, zipEntry.getName()), IOUtils.toByteArray(zipFile.getInputStream(zipEntry)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static class AssetObject {
        private final String hash;
        private final long size;

        private AssetObject(String hash, long size) {
            this.hash = hash;
            this.size = size;
        }

        public String getHash() {
            return hash;
        }

        public long getSize() {
            return size;
        }
    }
}
