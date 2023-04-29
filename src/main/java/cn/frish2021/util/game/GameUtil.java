package cn.frish2021.util.game;

import cn.frish2021.extension.PluginExtension;
import cn.frish2021.util.UrlUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.*;

import static cn.frish2021.util.UrlUtil.readFile;
import static cn.frish2021.util.UrlUtil.readString;

public class GameUtil {

    public static File getMinecraftDir() {
        File minecraftFolder;
        if (getOsName().contains("win")) {
            minecraftFolder = new File(System.getenv("APPDATA"), ".minecraft");
        } else if (getOsName().contains("mac")) {
            minecraftFolder = new File(System.getProperty("user.home"), "Library" + "/" + "Application Support" + "/" + "minecraft");
        } else {
            minecraftFolder = new File(System.getProperty("user.home"), ".minecraft");
        }
        return minecraftFolder;
    }

    private static String getOsName() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT);
    }

    public static File getLocalClient(PluginExtension extension) {
        return new File(getMinecraftDir(), "versions" + "/" + extension.minecraft.gameVersion + "/" + extension.minecraft.gameVersion + ".jar");
    }

    public static File getMappingJarFi(PluginExtension extension) {
        return new File(getMappingDir(extension), "versions" + "/" + extension.minecraft.gameVersion + "/" + extension.minecraft.gameVersion + "-mapping.jar");
    }

    public static File getGameDir(PluginExtension extension) {
        File game = new File(extension.getUserCache(), "game");
        if (!game.exists()) {
            game.mkdir();
        }
        return game;
    }

    public static File getClientFile(PluginExtension extension) {
        return new File(getGameDir(extension), extension.minecraft.gameVersion + "/" + extension.minecraft.gameVersion + "-client.jar");
    }

    public static File getMappingJarFile(PluginExtension extension) {
        return new File(getMappingDir(extension), "versions" + "/" + extension.minecraft.gameVersion + "/" + extension.minecraft.gameVersion + "-mapping.jar");
    }

    public static File getClientDeObfFile(PluginExtension extension) {
        return new File(getGameDir(extension), extension.minecraft.gameVersion + "/" + extension.minecraft.gameVersion + "-client-deobf.jar");
    }

    public static File getClientDeObfSourceFile(PluginExtension extension) {
        return new File(getGameDir(extension), extension.minecraft.gameVersion + "/" + extension.minecraft.gameVersion + "-client-deobf-source.jar");
    }

    public static String getRelease() {
        return new Gson().fromJson(UrlUtil.readString(UrlUtil.GAME_URL), JsonObject.class).get("latest").getAsJsonObject().get("release").getAsString();
    }

    public static String getSnapshot() {
        return new Gson().fromJson(UrlUtil.readString(UrlUtil.GAME_URL), JsonObject.class).get("snapshot").getAsJsonObject().get("release").getAsString();
    }

    public static String getJson(String version) {
        String jsonUrl = "";
        for (JsonElement versions : new Gson().fromJson(UrlUtil.readString(UrlUtil.GAME_URL), JsonObject.class).get("versions").getAsJsonArray()) {
            if (versions.getAsJsonObject().get("id").getAsString().equals(version)) {
                jsonUrl = versions.getAsJsonObject().get("url").getAsString();
            }
        }
        return UrlUtil.readString(jsonUrl);
    }

    public static File getMappingDir(PluginExtension extension) {
        File mapping = new File(extension.getUserCache(), "mapping");
        if (!mapping.exists()) {
            mapping.mkdir();
        }
        return mapping;
    }

    public static File getClientMappingFile(PluginExtension extension) {
        return new File(getMappingDir(extension), extension.minecraft.gameVersion + "-client.txt");
    }

    public static byte[] getClientJar(PluginExtension extension) {
        return readFile(new Gson().fromJson(getJson(extension.minecraft.gameVersion), JsonObject.class).get("downloads").getAsJsonObject().get("client").getAsJsonObject().get("url").getAsString());
    }

    public static byte[] getMappingJar(PluginExtension extension) {
        return readFile(new Gson().fromJson(getJson(extension.minecraft.gameVersion), JsonObject.class).get("downloads").getAsJsonObject().get("client_mappings_jar").getAsJsonObject().get("url").getAsString());
    }
    public static String getClientMapping(PluginExtension extension) {
        return readString(new Gson().fromJson(getJson(extension.minecraft.gameVersion), JsonObject.class).getAsJsonObject("downloads").getAsJsonObject().get("client_mappings").getAsJsonObject().get("url").getAsString());
    }

    public static String getClientAssets(PluginExtension extension) {
        return readString(new Gson().fromJson(getJson(extension.minecraft.gameVersion), JsonObject.class).get("assetIndex").getAsJsonObject().get("url").getAsString());
    }

    public static File getClientAssetsDir(PluginExtension extension) {
        File assets = new File(extension.getUserCache(), "assets");
        if (!assets.exists()) {
            assets.mkdir();
        }
        return assets;
    }

    public static File getClientAssetsIndexesDir(PluginExtension extension) {
        File indexes = new File(getClientAssetsDir(extension), "indexes");
        if (!indexes.exists()) {
            indexes.mkdir();
        }
        return indexes;
    }

    public static File getClientAssetsObjectsDir(PluginExtension extension) {
        File objects = new File(getClientAssetsDir(extension), "objects");
        if (!objects.exists()) {
            objects.mkdir();
        }
        return objects;
    }

    public static File getClientAssetsSkinsDir(PluginExtension extension) {
        File objects = new File(getClientAssetsDir(extension), "skins");
        if (!objects.exists()) {
            objects.mkdir();
        }
        return objects;
    }

    public static File getClientAssetsIndexFile(PluginExtension extension) {
        return new File(getClientAssetsIndexesDir(extension), extension.minecraft.gameVersion + ".json");
    }

    public static File getClientAssetsObjectFile(PluginExtension extension, String name) {
        File file = new File(getClientAssetsObjectsDir(extension), name.substring(0, 2));
        if (!file.exists()) {
            file.mkdir();
        }
        return new File(file, name);
    }

    public static File getLocalClientAssetsDir() {
        return new File(getMinecraftDir(), "assets");
    }

    public static File getLocalClientAssetsObjectsDir() {
        return new File(getLocalClientAssetsDir(), "objects");
    }

    public static File getLocalClientAssetsObjectFile(String name) {
        return new File(getLocalClientAssetsObjectsDir(), name.substring(0, 2) + "/" + name);
    }

    public static File getLocalClientAssetsSkinsDir() {
        return new File(getLocalClientAssetsDir(), "skins");
    }

    public static File getClientNativeDir(PluginExtension extension) {
        File file = new File(GameUtil.getGameDir(extension), extension.minecraft.gameVersion + "/" + extension.minecraft.gameVersion + "-native");
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public static File getClientNativeJarDir(PluginExtension extension) {
        File jars = new File(GameUtil.getClientNativeDir(extension), "jars");
        if (!jars.exists()) {
            jars.mkdir();
        }
        return jars;
    }

    public static File getClientNativeFileDir(PluginExtension extension) {
        File jars = new File(GameUtil.getClientNativeDir(extension), "natives");
        if (!jars.exists()) {
            jars.mkdir();
        }
        return jars;
    }

    public static List<String> getLibraries(PluginExtension extension) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        for (JsonElement jsonElement : new Gson().fromJson(getJson(extension.minecraft.gameVersion), JsonObject.class).get("libraries").getAsJsonArray()) {
            if (jsonElement.getAsJsonObject().has("natives")) {
                continue;
            }

            String name = jsonElement.getAsJsonObject().get("name").getAsString();
            linkedHashMap.put(name.substring(0, name.lastIndexOf(":")), name.substring(name.lastIndexOf(":")));
        }
        List<String> libraries = new ArrayList<>();
        for (Map.Entry<String, String> stringStringEntry : linkedHashMap.entrySet()) {
            libraries.add(stringStringEntry.getKey() + stringStringEntry.getValue());
        }
        return libraries;
    }

    public static List<String> getNatives(PluginExtension extension) {
        List<String> libraries = new ArrayList<>();
        for (JsonElement jsonElement : new Gson().fromJson(getJson(extension.minecraft.gameVersion), JsonObject.class).get("libraries").getAsJsonArray()) {
            JsonObject downloads = jsonElement.getAsJsonObject().get("downloads").getAsJsonObject();
            if (downloads.has("classifiers")) {
                String name = "natives-linux";
                if (getOsName().contains("win")) {
                    name = "natives-windows";
                } else if (getOsName().contains("mac")) {
                    name = "natives-macos";
                }
                JsonObject classifiers = downloads.get("classifiers").getAsJsonObject();
                if (classifiers.has(name)) {
                    libraries.add(downloads.get("classifiers").getAsJsonObject().get(name).getAsJsonObject().get("url").getAsString());
                }
            }
        }
        return libraries;
    }
}
