package cn.frish2021.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class UrlUtil {
    public static final String GAME_URL = "https://minecraftold.github.io/versions/version_manifest_v2.json";
    public static final String GAME_LIBRARIES = "https://libraries.minecraft.net";
    public static final String GAME_RESOURCES = "https://resources.download.minecraft.net";
    public static String readString(String link) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(link);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection connection = null;
            if (urlConnection instanceof HttpURLConnection) {
                connection = ((HttpURLConnection) urlConnection);
            }
            if (connection != null) {
                IOUtils.readLines(connection.getInputStream(), StandardCharsets.UTF_8).forEach(line -> stringBuilder.append(line).append("\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static byte[] readFile(String link) {
        byte[] bytes = null;
        try {
            URL url = new URL(link);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection connection = null;
            if (urlConnection instanceof HttpURLConnection) {
                connection = ((HttpURLConnection) urlConnection);
            }
            if (connection != null) {
                bytes = IOUtils.toByteArray(connection.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
