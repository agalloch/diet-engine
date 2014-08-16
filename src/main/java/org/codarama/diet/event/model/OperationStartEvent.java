package org.codarama.diet.event.model;

import org.codarama.diet.component.ListenableComponent;

public class OperationStartEvent extends ComponentEvent {

	public OperationStartEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
