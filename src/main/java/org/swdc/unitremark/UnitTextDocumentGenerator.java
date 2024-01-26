package org.swdc.unitremark;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class UnitTextDocumentGenerator implements UnitContext<String> {

    private ConcurrentHashMap<String, SourceGenerateStrategy> strategies = new ConcurrentHashMap<>();

    private String subfix;

    private Logger logger = LoggerFactory.getLogger(UnitTextDocumentGenerator.class);

    public UnitTextDocumentGenerator(UnitAbstractStrategies<String> textStrategy) {
        subfix = textStrategy.getExtension();
        Map<String, SourceGenerateStrategy<String>> strategyMap = textStrategy.getStrategies();
        for (Map.Entry<String, SourceGenerateStrategy<String>> entry : strategyMap.entrySet()) {
            register(entry.getKey(),entry.getValue());
        }
    }

    @Override
    public <T extends SourceGenerateStrategy<String>> T getStrategy(String tagName) {
        return (T)strategies.get(tagName);
    }

    @Override
    public void register(String tagName, SourceGenerateStrategy<String> strategy) {
        if (strategies.containsKey(tagName)) {
            throw new RuntimeException("the tag : " + tagName + " has duplicated.");
        }
        strategies.put(tagName,strategy);
    }


    private String generate(UnitDocument tracer, Element element) {
        return getStrategy(element.tagName()).generate(this,element,tracer);
    }

    public UnitDocument<String> generateFromURL(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            /*if (page == null) {
                return null;
            }
            String text = page.getWebResponse().getContentAsString();
            Document doc = Jsoup.parse(text,url);*/
            UnitDocument<String> result = new UnitDocument<>();
            result.setTitle(doc.title());
            String source = generate(result, doc.tagName("html"));
            result.setSource(source);

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public UnitDocument<String> generateFromSource(String url, String source) {
        Document doc = Jsoup.parse(source,url);
        UnitDocument<String> result = new UnitDocument<>();
        result.setTitle(doc.title());
        String data = generate(result, doc.tagName("html"));
        result.setSource(data);
        return result;
    }

    public void saveDocument(UnitDocument<String> document, File file) {
        if (file.exists()) {
            file.delete();
        }
        try {
            if(!file.createNewFile()) {
                return;
            }
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zout = new ZipOutputStream(fos);

            ZipEntry entry = new ZipEntry("index." + subfix);
            zout.putNextEntry(entry);
            zout.write(document.getSource().getBytes(StandardCharsets.UTF_8));
            zout.closeEntry();

            for (String bufferId : document.getImages()) {
                ZipEntry target = new ZipEntry("@image/" + bufferId);
                zout.putNextEntry(target);
                zout.write(document.getImage(bufferId).array());
                zout.closeEntry();
            }

            zout.flush();
            zout.close();

        } catch (Throwable e) {
            logger.error("fail to load resource", e);
            throw new RuntimeException(e);
        }

    }


}
