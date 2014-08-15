package net.ayld.diet.event.model;

import net.ayld.diet.component.ListenableComponent;

public class OperationStartEvent extends ComponentEvent {

	public OperationStartEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
