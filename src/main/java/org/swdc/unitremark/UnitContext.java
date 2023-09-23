package org.swdc.unitremark;

/**
 * HTML的处理上下文。
 * 提供HTML处理功能的入口。
 */
public interface UnitContext<S> {

    <T extends SourceGenerateStrategy<S>> T getStrategy(String tagName);

    void register(String tagName, SourceGenerateStrategy<S> strategy);

}
