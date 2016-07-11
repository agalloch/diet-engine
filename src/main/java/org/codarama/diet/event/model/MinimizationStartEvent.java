package org.codarama.diet.event.model;

import org.codarama.diet.component.ListenableComponent;

/**
 * Created by Ayld on 7/11/16.
 */
public class MinimizationStartEvent extends MinimizationEvent {
    public MinimizationStartEvent(String message, Class<? extends ListenableComponent> by) {
        super(message, by);
    }
}
