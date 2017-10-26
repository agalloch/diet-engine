package org.codarama.diet.event.model;


import org.codarama.diet.component.ListenableComponent;

public abstract class ComponentEvent {

    private final String message;
    private final Class<? extends ListenableComponent> by;

    protected ComponentEvent(String message, Class<? extends ListenableComponent> by) {
        this.message = message;
        this.by = by;
    }

    public String getMessage() {
        return message;
    }

    public Class<? extends ListenableComponent> sender() {
        return this.by;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
