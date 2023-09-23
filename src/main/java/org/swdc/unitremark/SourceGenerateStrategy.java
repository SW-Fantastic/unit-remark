package org.swdc.unitremark;

import org.jsoup.nodes.Element;

/**
 * 文本生成策略，用于转换HTML的元素到需要的文本形式，
 * 例如Markdown，如果有必要也可以转换为其他任意形式。
 */
public interface SourceGenerateStrategy<T> {

    T generate(UnitContext<T> context, Element element, UnitDocument tracer);

}
