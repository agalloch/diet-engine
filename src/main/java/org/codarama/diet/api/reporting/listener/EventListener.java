package org.codarama.diet.api.reporting.listener;

import org.codarama.diet.event.model.ComponentEvent;

/**
 * Implementers of the interface can subscribe to be notified for events during minimization.
 *
 * @see org.codarama.diet.api.ListenerRegistrar
 * @see org.codarama.diet.event.model.ComponentEvent
 *
 * Created by Ayld on 7/10/16.
 */
public interface EventListener<E extends ComponentEvent> {

    void on(E event);
}
