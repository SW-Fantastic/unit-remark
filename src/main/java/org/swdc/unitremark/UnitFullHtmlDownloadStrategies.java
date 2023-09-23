package org.swdc.unitremark;

import org.jsoup.nodes.*;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UnitFullHtmlDownloadStrategies extends UnitAbstractStrategies<String> {

    private Charset charset = StandardCharsets.UTF_8;

    static String reverseGenerate(UnitContext<String> context, List<Node> elements, UnitDocument tracer) {
        if (elements == null || elements.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Node element: elements) {
            if (element instanceof Element) {
                Element theElement = (Element) element;
                SourceGenerateStrategy<String> strategy = context.getStrategy(theElement.tagName());
                if (strategy == null) {
                    strategy = context.getStrategy("div");
                }
                String text = strategy.generate(context,theElement,tracer);
                sb.append(text);
            } else if (element instanceof TextNode){
                TextNode node = (TextNode) element;
                sb.append(node.text());
            }
        }
        return sb.toString();
    }


    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "meta"
    })
    public String meta(UnitContext<String> ctx, Element e, UnitDocument document) {
        String content = e.attr("content");
        String charsetAttr = e.attr("charset");
        if (charsetAttr != null && !charsetAttr.isBlank()) {
            charset = Charset.forName(charsetAttr);
            return "<meta charset=\"utf8\">";
        } else if (content.toLowerCase().contains("charset")) {
            int charsetPos = content.toLowerCase().indexOf("charset");
            String charsetName = content.substring(charsetPos);
            int nextPos = charsetName.indexOf(";");
            if (nextPos > -1) {
                charsetName = charsetName.substring(0,nextPos);
            }
            charset = Charset.forName(charsetName.split("=")[1]);
            return "<meta charset=\"utf8\">";
        }
        return e.toString();
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "link"
    })
    public String link(UnitContext<String> ctx, Element e, UnitDocument document) {
        String rel = e.attr("rel").toLowerCase();
        String type = e.attr("type").toLowerCase();
        String href = e.attr("abs:href");
        if (type.equals("text/css") || rel.equals("stylesheet")) {
            String content = NetWorkUtils.extractStringFrom(href,charset);
            return "<style>" + content + "</style>";
        }
        return "";
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "img"
    })
    public String img(UnitContext<String> ctx, Element e, UnitDocument document) {
        String src = "";
        if (e.hasAttr("src")) {
            src = e.attr("abs:src");
        } else {
            for (Attribute attr : e.attributes()) {
                if (attr.getKey().toLowerCase().contains("src")) {
                    src = e.attr("abs:"+attr.getKey());
                    break;
                }
            }
        }
        if (src.isBlank()) {
            return "";
        }
        String attrs = "";
        if (e.hasAttr("class")) {
            attrs = attrs + " class=\"" + e.attr("class") + "\"";
        }
        if (e.hasAttr("style")) {
            attrs = attrs + " style=\"" + e.attr("style") + "\"";
        }
        if (e.hasAttr("id")) {
            attrs = attrs + " id=\"" + e.attr("id") + "\"";
        }
        if (document == null) {
            return "<img src=\"" + src + "\" " + attrs + " />";
        } else {

            if (e.hasAttr("data-original") && NetWorkUtils.isURL(e.attr("data-original"))) {
                src = e.attr("data-original");
            }

            String id = document.traceImageResource(src);
            if (id != null) {
                return "<img src=\"@image/" + id + "\" " + attrs + " />";
            }
            return "";
        }
    }

    @UnitStrategy(strategyFor = UnitCleanHtmlStrategies.class, matches = {
            "body","html","head","div","p"
    })
    public String div(UnitContext<String> ctx, Element e, UnitDocument document) {
        String rst = reverseGenerate(ctx,e.childNodes(),document);
        if (rst.isBlank()) {
            return "";
        }
        if (e instanceof Document) {
            return rst;
        }
        String attrs = "";
        if (e.hasAttr("class")) {
            attrs = attrs + " class=\"" + e.attr("class") + "\"";
        }
        if (e.hasAttr("style")) {
            attrs = attrs + " style=\"" + e.attr("style") + "\"";
        }
        if (e.hasAttr("id")) {
            attrs = attrs + " id=\"" + e.attr("id") + "\"";
        }
        return "<" + e.tagName() + attrs + ">" + rst + "</" + e.tagName() + ">";
    }

    @Override
    public String getExtension() {
        return "html";
    }
}
