module swdc.unit.remark {

    requires java.xml;
    requires org.slf4j;
    requires org.jsoup;

    exports org.swdc.unitremark;
    exports org.swdc.unitremark.strategies;

    provides java.nio.charset.spi.CharsetProvider
            with org.swdc.unitremark.ChineseCharsetProvider;

}

