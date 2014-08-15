package net.ayld.diet.event.model;

import net.ayld.diet.component.ListenableComponent;

public class JarExtractionStartEvent extends OperationStartEvent {

	public JarExtractionStartEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
