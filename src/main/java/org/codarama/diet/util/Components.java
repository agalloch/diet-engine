package org.codarama.diet.util;

import org.springframework.context.ApplicationContext;

public enum Components {
    STREAM_JAR_MAKER("streamJarMaker"),
    CLASS_DEPENDENCY_RESOLVER("classDependencyResolver"),
    STREAM_DEPENDENCY_RESOLVER("classStreamResolver"),
    SOURCE_DEPENDENCY_RESOLVER("sourceDependencyResolver"),
    INDEXED_MINIMIZATION_STRATEGY("indexedMinimizationStrategy"),
    EVENT_BUS("statusUpdateEventBus");

    private final ApplicationContext context;
    private final String name;

    Components(String name) {
        this.context = Contexts.SPRING.instance();
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance() {
        return (T) context.getBean(name);
    }
}
