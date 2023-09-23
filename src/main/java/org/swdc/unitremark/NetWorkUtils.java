package org.swdc.unitremark;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NetWorkUtils {

    public static boolean isURL(String url) {
        try {
            URL target = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) target.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.connect();
            connection.disconnect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractStringFrom(String url, Charset charset) {
        try {
            URL theURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) theURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.connect();

            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream bot = new ByteArrayOutputStream();
                inputStream.transferTo(bot);
                inputStream.close();
                connection.disconnect();
                return bot.toString(charset);
            }

            connection.disconnect();
            return "";
        } catch (Exception e) {
            return "";
        }
    }

}
