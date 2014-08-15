package net.ayld.diet.event.model;

import net.ayld.diet.component.ListenableComponent;

public class OperationEndEvent extends ComponentEvent {

	public OperationEndEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
