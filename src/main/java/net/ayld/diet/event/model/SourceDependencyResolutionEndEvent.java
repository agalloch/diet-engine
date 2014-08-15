package net.ayld.diet.event.model;

import net.ayld.diet.component.ListenableComponent;

public class SourceDependencyResolutionEndEvent extends OperationEndEvent {

	public SourceDependencyResolutionEndEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
