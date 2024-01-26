package org.swdc.unitremark.test;

import org.swdc.unitremark.*;
import org.swdc.unitremark.UnitDocument;
import org.swdc.unitremark.strategies.UnitEmbeddedHtmlStrategies;
import org.swdc.unitremark.strategies.UnitFullHtmlDownloadStrategies;
import org.swdc.unitremark.strategies.UnitMarkdownEmbeddedStrategies;
import org.swdc.unitremark.strategies.UnitMarkdownStrategies;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        UnitTextDocumentGenerator remark = new UnitTextDocumentGenerator(new UnitMarkdownStrategies());
        UnitDocument<String> text = remark.generateFromURL("https://zhuanlan.zhihu.com/p/644973638");
        //String text = remark.generateTextFromURL("https://blog.csdn.net/u013642500/article/details/102655124?spm=1001.2101.3001.6650.17&depth_1-utm_relevant_index=22");
        //String text = remark.generateTextFromURL("http://www.biquge5200.cc/160_160758/183052436.html");
        //UnitDocument text = remark.generateTextFromURL("https://www.w3school.com.cn/tags/tag_table.asp");
        System.err.println(text.getSource());
       // remark.saveDocument(text,new File("./text.zmd"));

        UnitTextDocumentGenerator cleanHtmlGenerator = new UnitTextDocumentGenerator(new UnitFullHtmlDownloadStrategies());
        //UnitDocument<String> cleanHtml = cleanHtmlGenerator.generateFromURL("https://zhuanlan.zhihu.com/p/644973638");
        UnitDocument<String> cleanHtml = cleanHtmlGenerator.generateFromURL("https://blog.csdn.net/u013642500/article/details/102655124?spm=1001.2101.3001.6650.17&depth_1-utm_relevant_index=22");
        //UnitDocument<String> cleanHtml = cleanHtmlGenerator.generateFromURL("https://www.w3school.com.cn/tags/tag_table.asp");
        System.err.println(cleanHtml.getSource());
        cleanHtmlGenerator.saveDocument(cleanHtml,new File("test.zhtm"));

        UnitTextDocumentGenerator embeddedGenerator  = new UnitTextDocumentGenerator(new UnitMarkdownEmbeddedStrategies());
        UnitDocument<String> document = embeddedGenerator.generateFromURL("https://juejin.cn/post/7071209824729432100");
        Files.write(Path.of("./embedded2.md"),document.getSource().getBytes(StandardCharsets.UTF_8));
    }


}
