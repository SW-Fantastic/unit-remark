package org.swdc.unitremark;

import org.jsoup.nodes.Element;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public abstract class UnitAbstractStrategies<T> {

    public abstract String getExtension();

    public Map<String, SourceGenerateStrategy<T>> getStrategies() {
        Map<String, SourceGenerateStrategy<T>> strategyMap = new HashMap<>();
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            UnitStrategy unit = method.getAnnotation(UnitStrategy.class);
            if (unit == null) {
                continue;
            }
            Parameter[] params = method.getParameters();
            if (
                    params.length == 3 &&
                    params[0].getType() == UnitContext.class &&
                    params[1].getType() == Element.class &&
                    params[2].getType() == UnitDocument.class
            ) {
                for (String tag: unit.matches()) {
                    if (strategyMap.containsKey(tag)) {
                        throw new RuntimeException("the tag has duplicated : " + tag);
                    }
                    strategyMap.put(tag,(ctx,elem,tracer) -> {
                        try {
                            return (T) method.invoke(this,ctx,elem,tracer);
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        }
                    });
                }
            }
        }
        return strategyMap;
    }

}
