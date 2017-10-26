package org.codarama.diet.component;

import org.codarama.diet.event.model.ComponentEvent;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.eventbus.EventBus;

/**
 * Intended for extension.
 * <p>
 * Extenders should use the given event bus to notify listeners for progress updates.
 * The definition of what a 'progress update' is depends on the context of the extender.
 *
 * @see {@link ComponentEvent}
 */
public abstract class ListenableComponent {

    protected EventBus eventBus;

    @Required
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
