package org.codarama.diet.util;

import org.springframework.context.ApplicationContext;

public enum Components {
    JAR_MAKER("jarMaker"),
    LIB_JAR_EXPLODER("jarExploder"),
    EXPLICIT_JAR_EXPLODER("explicitJarExploder"),
    CLASS_DEPENDENCY_RESOLVER("classDependencyResolver"),
    SOURCE_DEPENDENCY_RESOLVER("sourceDependencyResolver"),
    DEPENDENCY_MATCHER_STRATEGY("unanimousMatcher"),
    EVENT_BUS("statusUpdateEventBus");

    private static ApplicationContext CONTEXT;

    private final String name;

    private Components(String name) {
        if (!isContextInitialized()) {
            initContext();
        }
        this.name = name;
    }

    private static boolean isContextInitialized() {
        return CONTEXT != null;
    }

    private static void initContext() {
        CONTEXT = Contexts.SPRING.instance();
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance() {
        return (T) CONTEXT.getBean(name);
    }
}
