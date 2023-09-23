package org.swdc.unitremark;

import org.jsoup.nodes.*;

import java.util.List;

public class UnitCleanHtmlStrategies extends UnitAbstractStrategies<String> {

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

    @UnitStrategy(strategyFor = UnitCleanHtmlStrategies.class, matches = {
            "div","body","html","head"
    })
    public String div(UnitContext<String> ctx, Element e, UnitDocument document) {
        String rst = reverseGenerate(ctx,e.childNodes(),document);
        if (rst.isBlank()) {
            return "";
        }
        if (e instanceof Document) {
            return rst;
        }
        return "<" + e.tagName() + ">" + rst + "</" + e.tagName() + ">";
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
        if (document == null) {
            return "<img src=\"" + src + "\" />";
        } else {
            String id = document.traceImageResource(src);
            if (id != null) {
                return "<img src=\"@image/" + id + "\" />";
            }
            return "<img src=\"" + src + "\" />";
        }
    }

    @UnitStrategy(strategyFor = UnitCleanHtmlStrategies.class, matches = {
            "a"
    })
    public String a(UnitContext<String> ctx, Element e, UnitDocument document) {
        String text = "";
        if(e.children().size() > 0) {
            text = reverseGenerate(ctx,e.childNodes(),document);
        } else {
            text = e.ownText();
        }
        return "<a href=\"#\">" + text + "</a>";
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "h1","h2","h3","h4","h5","h6"
    })
    public String header(UnitContext<String> ctx, Element e, UnitDocument tracer) {
        String text = reverseGenerate(ctx,e.childNodes(),tracer);
        return "<" + e.tagName() + ">" + text + "</" + e.tagName() + ">";
    }


    @UnitStrategy(strategyFor = UnitCleanHtmlStrategies.class,matches = {
            "button","select","input","checkbox"
    })
    public String inputs(UnitContext<String> ctx, Element e, UnitDocument document) {
        return "";
    }

    @Override
    public String getExtension() {
        return "html";
    }
}
