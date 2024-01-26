package org.swdc.unitremark.strategies;

import org.jsoup.nodes.*;
import org.swdc.unitremark.*;

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
            } else if (element instanceof DataNode) {
                return element.attr(element.nodeName());
            }
        }
        return sb.toString();
    }


    @UnitStrategy( matches = {
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

    @UnitStrategy( matches = {
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

    @UnitStrategy(matches = {
            "script"
    })
    public String script(UnitContext<String> ctx, Element e, UnitDocument document) {
        return "";
    }

    @UnitStrategy( matches = {
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
        String attrs = " ";

        for (Attribute attr :e.attributes()) {
            String key = attr.getKey().toLowerCase();
            if (key.equals("class") || key.equals("style") || key.equals("id")) {
                attrs = attrs + key + " =\"" + attr.getValue() + "\" ";
            } else if (key.startsWith("data-")) {
                attrs = attrs + key + "=\"" + attr.getValue() + "\" ";
            }
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

    @UnitStrategy(matches = {
            "style"
    })
    public String style(UnitContext<String> ctx, Element e, UnitDocument document) {

        String content = reverseGenerate(ctx,e.childNodes(),document);
        String attrs = " ";

        for (Attribute attr :e.attributes()) {
            String key = attr.getKey().toLowerCase();
            if (key.equals("class") || key.equals("style") || key.equals("id")) {
                attrs = attrs + key + " =\"" + attr.getValue() + "\" ";
            } else if (key.startsWith("data-")) {
                attrs = attrs + key + "=\"" + attr.getValue() + "\" ";
            }
        }

        if (content.isBlank()) {
            return "";
        }

        return "<style " + attrs + " >" + content + "</style>";
    }

    @UnitStrategy(matches = {
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

        String attrs = " ";

        for (Attribute attr :e.attributes()) {
            String key = attr.getKey().toLowerCase();
            if (key.equals("class") || key.equals("style") || key.equals("id")) {
                attrs = attrs + key + " =\"" + attr.getValue() + "\" ";
            } else if (key.startsWith("data-")) {
                attrs = attrs + key + "=\"" + attr.getValue() + "\" ";
            }
        }
        return "<" + e.tagName() + attrs + ">" + rst + "</" + e.tagName() + ">";
    }

    @Override
    public String getExtension() {
        return "html";
    }
}
