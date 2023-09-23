package org.swdc.unitremark;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.UUID;

public class UnitMarkdownStrategies extends UnitAbstractStrategies<String> {

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
            "script","meta","link","style"
    })
    public String metadata(UnitContext<String> ctx, Element e, UnitDocument tracer) {
        return "";
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "h1","h2","h3","h4","h5","h6"
    })
    public String header(UnitContext<String> ctx, Element e, UnitDocument tracer) {
        String num = e.tagName().toLowerCase().replace("h", "");
        Integer numVal = Integer.parseInt(num);
        String text = reverseGenerate(ctx,e.childNodes(),tracer);
        if (text.isBlank()) {
            return "";
        } else {
            String generated = "#".repeat(numVal) + " " + e.text();
           return "\r\n" +  generated + "\r\n";
        }
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
            return "![theImage-" + UUID.randomUUID() + "](" + src + ")";
        } else {
            String id = document.traceImageResource(src);
            if (id != null) {
                return "![theImage-" + id+ "](@image/" + id + ")";
            }
            return "![theImage-" + UUID.randomUUID() + "](" + src + ")";
        }
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "pre"
    })
    public String preformat(UnitContext<String> ctx, Element e, UnitDocument document) {
        if (!e.hasText()) {
            return reverseGenerate(ctx,e.childNodes(),document);
        }
        return  "\r\n``` \r\n" + e.text() + "\r\n```\r\n\r\n";
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "blockquote"
    })
    public String blockquote(UnitContext<String> ctx, Element e, UnitDocument document) {
        String text = reverseGenerate(ctx,e.childNodes(),document);
        if (text.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String[] lines =  text.split("\r\n");
        for (String line : lines) {
            if (!line.isBlank()) {
                sb.append(" > ").append(line).append("\r\n");
            }
        }
        return sb.append("\r\n").toString();
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "q","kbd"
    })
    String quote(UnitContext<String> ctx, Element e, UnitDocument document) {
        String text = reverseGenerate(ctx,e.childNodes(),document);
        if (text.isBlank()) {
            return "";
        }
        text = text.trim();
        return "`" + text + "`";
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "ul","ol","dl"
    })
    public String list(UnitContext<String> ctx, Element e, UnitDocument document) {
        String listTag = e.tagName().toLowerCase();
        String itemTag = "";
        if (listTag.equals("ul") || listTag.equals("ol")) {
            itemTag = "li";
        } else if (listTag.equals("dl")) {
            itemTag = "dt";
        }
        Elements items = e.select("> " + itemTag);
        if (items.size() == 0) {
            return "";
        }
        int curr = 0;
        StringBuilder text = new StringBuilder();
        for (Element element : items) {
            String content = reverseGenerate(ctx,element.childNodes(),document);
            if (!content.isBlank()) {
                if (e.tagName().equalsIgnoreCase("ol")) {
                    text.append(" ").append(++curr).append(".").append(content.trim()).append("\r\n");
                } else {
                    text.append(" - ").append(content.trim()).append("\r\n");
                }
            }
        }

        return "\r\n\r\n" + text + "\r\n\r\n";
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "table"
    })
    public String table(UnitContext<String> ctx, Element e, UnitDocument document) {
        Elements elements = e.select("> tr");
        if (elements.size() == 0) {
            elements = e.select("tbody > tr");
            if (elements.size() == 0) {
                return "";
            }
        }
        StringBuilder tableBuilder = new StringBuilder();
        int count = 0;
        boolean header = false;
        for (Element row: elements) {
            Elements cells = row.select("> th");
            if (cells.size() == 0) {
                cells = row.select("> td");
            }
            if (count == 0) {
                count = cells.size();
                if (count == 0) {
                    return "";
                }
            }
            for (int idx = 0; idx < count; idx ++) {
                if (idx == 0) {
                    tableBuilder.append("|");
                }
                Element element = cells.get(idx);
                String content = element.text();
                if (content.isBlank()) {
                    content = "  ";
                }
                tableBuilder.append(content).append("|");
            }
            tableBuilder.append("\r\n");
            if (!header) {
                for (int idx = 0; idx < count; idx ++) {
                    if(idx == 0) {
                        tableBuilder.append("|:--:|");
                    } else {
                        tableBuilder.append(":--:|");
                    }
                }
                tableBuilder.append("\r\n");
                header = true;
            }
        }
        return "\r\n\r\n" + tableBuilder + "\r\n\r\n";
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "div","body","html"
    })
    public String div(UnitContext<String> ctx, Element e, UnitDocument document) {
        String rst = reverseGenerate(ctx,e.childNodes(),document);
        if (rst.isBlank()) {
            return "";
        }
        return rst;
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "b","strong"
    })
    public String b(UnitContext<String> ctx, Element e, UnitDocument document) {
        String content = reverseGenerate(ctx,e.childNodes(),document);
        if (content.isBlank()) {
            return "";
        }
        return "**" + content + "**";
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "p","span"
    })
    public String p(UnitContext<String> ctx, Element e, UnitDocument document) {
        String content = reverseGenerate(ctx,e.childNodes(),document);
        if (content.isBlank()) {
            return "";
        }
        return "\r\n" + content + "\r\n";
    }

    @UnitStrategy(strategyFor = UnitTextDocumentGenerator.class, matches = {
            "a"
    })
    public String a(UnitContext<String> ctx, Element e, UnitDocument document) {
        String content = reverseGenerate(ctx,e.childNodes(),document).trim();
        if (content.isBlank()) {
            return "";
        } else {
            String link = "[" + content + "]";
            String href = "";
            if (e.hasAttr("ref")) {
                href = e.attr("abs:href");
            } else {
                for (Attribute attr : e.attributes()) {
                    if (attr.getKey().toLowerCase().contains("href")) {
                        href = e.attr("abs:" + attr.getKey());
                        break;
                    }
                }
            }
            if (href.isBlank()) {
                return content;
            } else {
                link = link + "(" + href + ")";
            }
            return link;
        }
    }


    @Override
    public String getExtension() {
        return "md";
    }
}
