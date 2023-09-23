package org.swdc.unitremark;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collections;
import java.util.Iterator;

public class ChineseCharsetProvider extends CharsetProvider {

    @Override
    public Iterator<Charset> charsets() {
        return Collections.emptyIterator();
    }

    @Override
    public Charset charsetForName(String charsetName) {
        charsetName = charsetName.toLowerCase();
        if (charsetName.equals("big5")) {
            return Charset.forName("GB18030");
        }
        return Charset.defaultCharset();
    }
}
