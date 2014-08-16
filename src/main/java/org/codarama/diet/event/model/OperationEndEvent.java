package org.codarama.diet.event.model;

import org.codarama.diet.component.ListenableComponent;

public class OperationEndEvent extends ComponentEvent {

	public OperationEndEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
