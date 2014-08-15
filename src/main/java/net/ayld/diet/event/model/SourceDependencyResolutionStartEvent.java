package net.ayld.diet.event.model;

import net.ayld.diet.component.ListenableComponent;

public class SourceDependencyResolutionStartEvent extends OperationStartEvent {

	public SourceDependencyResolutionStartEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
