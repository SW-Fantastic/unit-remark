package org.swdc.unitremark;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;

public class UnitDocument<T> {

    private Map<String, String> imageURLIdMap = new HashMap<>();
    private Map<String, ByteBuffer> images = new HashMap<>();

    private String title;

    private T source;

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public String traceImageResource(String url) {
        String resourceId = UUID.randomUUID().toString();
        try {
            URL theURL = new URL(url);
            String path = theURL.getPath();

            if (imageURLIdMap.containsKey(path)) {
                return imageURLIdMap.get(path);
            }

            String subfix = "";
            int index = path.lastIndexOf(".");
            subfix = path.substring(index);
            resourceId = resourceId + subfix;

            imageURLIdMap.put(path,resourceId);

            HttpURLConnection connection = (HttpURLConnection) theURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.connect();

            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream bot = new ByteArrayOutputStream();
                inputStream.transferTo(bot);
                bot.flush();

                ByteBuffer buffer = ByteBuffer.allocate(bot.size());
                buffer.put(bot.toByteArray());
                images.put(resourceId,buffer);
            }

            connection.disconnect();
        } catch (Exception e) {
            return null;
        }
        return resourceId;
    }


    void setSource(T source) {
        this.source = source;
    }

    public List<String> getImages() {
        return new ArrayList<>(images.keySet());
    }

    public ByteBuffer getImage(String id) {
        return images.get(id);
    }

    public T getSource() {
        return source;
    }

}
