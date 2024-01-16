package org.swdc.unitremark;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Base64;

public class UnitEmbeddedHtmlStrategies extends UnitFullHtmlDownloadStrategies {

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

            try {
                String path = new URL(src).getPath();

                String id = document.traceImageResource(src);
                ByteBuffer buffer = document.getImage(id);
                if (buffer != null) {
                    byte[] arrays = buffer.array();
                    String base64 = Base64.getEncoder()
                            .encodeToString(arrays);
                    String meta = "data:image/" + path.substring(path.lastIndexOf(".") + 1) + ";base64," + base64;
                    if (id != null) {
                        return "<img src=\"" + meta + "\" " + attrs + " />";
                    }
                }
            } catch (Exception ex) {
            }

            return "";
        }
    }

}
