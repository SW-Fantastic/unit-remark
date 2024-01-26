package org.swdc.unitremark.strategies;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.swdc.unitremark.UnitContext;
import org.swdc.unitremark.UnitDocument;
import org.swdc.unitremark.UnitStrategy;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class UnitMarkdownEmbeddedStrategies extends UnitMarkdownStrategies {

    @UnitStrategy(matches = {
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
        String attrs = " ";

        for (Attribute attr :e.attributes()) {
            String key = attr.getKey().toLowerCase();
            if (key.equals("class") || key.equals("style") || key.equals("id")) {
                attrs = attrs + key + " =\"" + attr.getValue() + "\" ";
            } else if (key.startsWith("data-")) {
                attrs = attrs + key + "=\"" + attr.getValue() + "\" ";
            }
        }
        if (src.isBlank()) {
            return "";
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
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

}
