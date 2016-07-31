package org.codarama.diet.event.model;

import org.codarama.diet.component.ListenableComponent;

public class ClassDependencyResolutionEndEvent extends OperationEndEvent {

    public ClassDependencyResolutionEndEvent(String message, Class<? extends ListenableComponent> by) {
        super(message, by);
    }
}
